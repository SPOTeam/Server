package com.example.spot.service.post;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.PostHandler;
import com.example.spot.domain.Post;
import com.example.spot.domain.PostComment;
import com.example.spot.domain.enums.Board;
import com.example.spot.domain.mapping.MemberScrap;
import com.example.spot.repository.MemberScrapRepository;
import com.example.spot.repository.PostCommentRepository;
import com.example.spot.repository.PostRepository;
import com.example.spot.web.dto.post.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.spot.security.utils.SecurityUtils.getCurrentUserId;


@Service
@RequiredArgsConstructor
public class PostQueryServiceImpl implements PostQueryService {

    @Value("${image.post.anonymous.profile}")
    private String DEFAULT_PROFILE_IMAGE_URL;

    private final PostRepository postRepository;
    private final LikedPostQueryService likedPostQueryService;
    private final PostCommentRepository postCommentRepository;
    private final LikedPostCommentQueryService likedPostCommentQueryService;
    private final MemberScrapRepository memberScrapRepository;


    @Transactional
    @Override
    public PostSingleResponse getPostById(Long postId, boolean likeOrScrap) {
        // 게시글 단건 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_NOT_FOUND));

        // 게시글이 신고되었는지 확인 - 수정 예정
//        if (isPostReported(post)) {
//            throw new PostHandler(ErrorStatus._POST_REPORTED);
//        }

        // 조회수 증가는 일반 조회시에(likeOrScrap이 false일 때)만 실행
        if (!likeOrScrap) {
            post.viewHit();
        }

        // 좋아요 수 조회
        long likeCount = likedPostQueryService.countByPostId(postId);

        //현재 사용자 좋아요 여부
        boolean likedByCurrentUser = likedPostQueryService.existsByMemberIdAndPostId(post.getId());

        // 스크랩 수 조회
        long scrapCount = memberScrapRepository.countByPostId(postId);

        // 현재 사용자 스크랩 여부
        Long currentUserId = getCurrentUserId();
        boolean scrapedByCurrentUser = memberScrapRepository.existsByMemberIdAndPostId(currentUserId, postId);

        //댓글
        CommentResponse commentResponse = getCommentsByPostId(post.getId());

        // 조회된 게시글을 PostSingleResponse로 변환하여 반환
        return PostSingleResponse.toDTO(post, likeCount, scrapCount, commentResponse, likedByCurrentUser, scrapedByCurrentUser, DEFAULT_PROFILE_IMAGE_URL);
    }

    @Transactional(readOnly = true)
    @Override
    public PostPagingResponse getPagingPosts(String type, Pageable pageable) {
        //게시글 페이징 조회
        Board boardType = Board.findByValue(type);
//        if (boardType == null) {
//            throw new PostHandler(ErrorStatus._INVALID_BOARD_TYPE);
//        }
        // 신고되지 않은 게시글만 조회 -구현 예정
        Page<Post> postPage;

        if (boardType == Board.ALL) {
            // ALL 타입일 경우 모든 게시글 조회
            postPage = postRepository.findByPostReportListIsEmpty(pageable);
        } else {
            // 특정 게시판 타입의 게시글 조회
            postPage = postRepository.findByBoardAndPostReportListIsEmpty(boardType, pageable);
        }

        List<PostPagingDetailResponse> postResponses = postPage.getContent().stream()
                .map(post -> {
                    long likeCount = likedPostQueryService.countByPostId(post.getId());
                    //현재 사용자 좋아요 여부
                    boolean likedByCurrentUser = likedPostQueryService.existsByMemberIdAndPostId(post.getId());
                    long scrapCount = memberScrapRepository.countByPostId(post.getId());
                    Long currentUserId = getCurrentUserId();
                    boolean scrapedByCurrentUser = memberScrapRepository.existsByMemberIdAndPostId(currentUserId, post.getId());
                    return PostPagingDetailResponse.toDTO(post, likeCount, scrapCount, likedByCurrentUser, scrapedByCurrentUser);
                })
                .toList();

        return PostPagingResponse.builder()
                .postType(type)
                .postResponses(postResponses)
                .totalPage(postPage.getTotalPages())
                .totalElements(postPage.getTotalElements())
                .isFirst(postPage.isFirst())
                .isLast(postPage.isLast())
                .build();
    }

    // 게시글이 신고되었는지 확인하는 메서드
    private boolean isPostReported(Post post) {
        return !post.getPostReportList().isEmpty();
    }

    @Transactional(readOnly = true)
    @Override
    public PostBest5Response getPostBest(String sortType) {
        //인기글 조회
        if (sortType.equals("REAL_TIME")) {
            // 실시간 조회 후 리턴
            List<Post> posts = postRepository.findTopByRealTimeScore();

//            if (posts.isEmpty()) {
//                throw new PostHandler(ErrorStatus._POST_NOT_FOUND); // 혹은 적절한 오류 타입으로 변경
//            }


            AtomicInteger rankCounter = new AtomicInteger(1);

            List<PostBest5DetailResponse> responses = posts.stream()
                    .map(post -> PostBest5DetailResponse.from(post, rankCounter.getAndIncrement()))
                    .toList();

            return PostBest5Response.builder()
                    .sortType("REAL_TIME")
                    .postBest5Responses(responses)
                    .build();
        }

        if (sortType.equals("RECOMMEND")) {
            // 추천수(좋아요수) 조회 후 리턴
            List<Post> posts = postRepository.findTopByOrderByLikeNumDesc();
            AtomicInteger rankCounter = new AtomicInteger(1);

            List<PostBest5DetailResponse> responses = posts.stream()
                    .map(post -> PostBest5DetailResponse.from(post, rankCounter.getAndIncrement()))
                    .toList();

            return PostBest5Response.builder()
                    .sortType("RECOMMEND")
                    .postBest5Responses(responses)
                    .build();
        }

        if (sortType.equals("COMMENT")) {
            // 댓글 수 조회 후 리턴
            List<Post> posts = postRepository.findTopByOrderByCommentCountDesc();
            AtomicInteger rankCounter = new AtomicInteger(1);

            List<PostBest5DetailResponse> responses = posts.stream()
                    .map(post -> PostBest5DetailResponse.from(post, rankCounter.getAndIncrement()))
                    .toList();

            return PostBest5Response.builder()
                    .sortType("COMMENT")
                    .postBest5Responses(responses)
                    .build();

        }

        // 무조건 에러
        throw new PostHandler(ErrorStatus._INVALID_SORT_TYPE);
    }


    @Transactional(readOnly = true)
    @Override
    public PostRepresentativeResponse getRepresentativePosts() {
        //대표게시글 조회
        List<Post> posts = postRepository.findRepresentativePosts();

        List<PostRepresentativeDetailResponse> responses = posts.stream()
                .map(PostRepresentativeDetailResponse::toDTO)
                .toList();

        return PostRepresentativeResponse.builder()
                .responses(responses)
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public PostAnnouncementResponse getPostAnnouncements() {
        //공지
        List<Post> posts = postRepository.findAnnouncementPosts();
        AtomicInteger rankCounter = new AtomicInteger(1);

        List<PostBest5DetailResponse> responses = posts.stream()
                .map(post -> PostBest5DetailResponse.from(post, rankCounter.getAndIncrement()))
                .toList();

        return PostAnnouncementResponse.builder()
                .responses(responses)
                .build();
    }

    // 게시글 별 댓글 조회
    @Transactional(readOnly = true)
    @Override
    public CommentResponse getCommentsByPostId(Long postId) {
        List<PostComment> comments = postCommentRepository.findByPostId(postId);

        List<CommentDetailResponse> commentResponses = comments.stream()
                .map(comment -> {
                    long likeCount = likedPostCommentQueryService.countByPostCommentIdAndIsLikedTrue(comment.getId());
                    //현재 사용자 댓글 좋아요 여부
                    boolean likedByCurrentUser = likedPostCommentQueryService.existsByMemberIdAndPostCommentIdAndIsLikedTrue(comment.getId());
                    //현재 사용자 댓글 싫어요 여부
                    boolean dislikedByCurrentUser = likedPostCommentQueryService.existsByMemberIdAndPostCommentIdAndIsLikedFalse(comment.getId());
                    return CommentDetailResponse.toDTO(comment, likeCount, likedByCurrentUser, dislikedByCurrentUser, DEFAULT_PROFILE_IMAGE_URL);
                })
                .toList();

        return CommentResponse.builder()
                .comments(commentResponses)
                .build();
    }

    //스크랩 게시글 조회
    @Transactional(readOnly = true)
    @Override
    public PostPagingResponse getScrapPagingPost(String type, Pageable pageable) {
        Long currentUserId = getCurrentUserId();

        Page<MemberScrap> postScrapPage;

        Board boardType = Board.findByValue(type);

        if (boardType == Board.ALL) {
            // ALL 타입일 경우 모든 게시글 조회
            postScrapPage = memberScrapRepository.findByMemberId(currentUserId, pageable);
        } else {
            // 특정 게시판 타입의 게시글 조회
            postScrapPage = memberScrapRepository.findByMemberIdAndPost_Board(currentUserId, boardType, pageable);
        }

        List<Post> scrapPosts = postScrapPage.getContent().stream()
                .map(MemberScrap::getPost)
                .toList();

        List<PostPagingDetailResponse> postResponses = scrapPosts.stream()
                .map(post -> {
                    long likeCount = likedPostQueryService.countByPostId(post.getId());
                    //현재 사용자 좋아요 여부
                    boolean likedByCurrentUser = likedPostQueryService.existsByMemberIdAndPostId(post.getId());
                    long scrapCount = memberScrapRepository.countByPostId(post.getId());
                    boolean scrapedByCurrentUser = memberScrapRepository.existsByMemberIdAndPostId(currentUserId, post.getId());
                    return PostPagingDetailResponse.toDTO(post, likeCount, scrapCount, likedByCurrentUser, scrapedByCurrentUser);
                })
                .toList();

        return PostPagingResponse.builder()
                .postType(type)
                .postResponses(postResponses)
                .totalPage(postScrapPage.getTotalPages())
                .totalElements(postScrapPage.getTotalElements())
                .isFirst(postScrapPage.isFirst())
                .isLast(postScrapPage.isLast())
                .build();
    }
}
