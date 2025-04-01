package com.example.spot.service.post;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.api.exception.handler.PostHandler;
import com.example.spot.domain.Post;
import com.example.spot.domain.PostComment;
import com.example.spot.domain.enums.Board;
import com.example.spot.domain.enums.PostStatus;
import com.example.spot.domain.mapping.MemberScrap;
import com.example.spot.repository.MemberScrapRepository;
import com.example.spot.repository.PostCommentRepository;
import com.example.spot.repository.PostReportRepository;
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
    private final PostReportRepository postReportRepository;

    /**
     * 게시글 단건 조회 : 게시글 1개의 상세 정보를 댓글 리스트와 함께 조회합니다.
     * @param postId 조회할 게시글 ID
     * @return 조회한 게시글의 정보와 좋아요/스크랩 수, 댓글 리스트, 현재 사용자의 좋아요/스크랩 여부, 프로필 이미지 반환
     * @throws PostHandler 게시글을 찾을 수 없는 경우
     */
    @Transactional
    @Override
    public PostSingleResponse getPostById(Long postId, boolean likeOrScrap) {
        // 게시글 단건 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_NOT_FOUND));

        // 신고 후 삭제 처리된 게시글 조회 불가
        if (postReportRepository.existsByPostIdAndPostStatus(postId, PostStatus.삭제)) {
            throw new PostHandler(ErrorStatus._POST_REPORTED);
        }

        // 조회수 증가는 일반 조회시에(likeOrScrap이 false일 때)만 실행
        if (!likeOrScrap) {
            post.viewHit();
        }

        // 좋아요 수 조회
        long likeCount = likedPostQueryService.countByPostId(postId);

        // 현재 사용자 좋아요 여부
        boolean likedByCurrentUser = likedPostQueryService.existsByMemberIdAndPostId(post.getId());

        // 스크랩 수 조회
        long scrapCount = memberScrapRepository.countByPostId(postId);

        // 현재 사용자 스크랩 여부
        Long currentUserId = getCurrentUserId();
        boolean scrapedByCurrentUser = memberScrapRepository.existsByMemberIdAndPostId(currentUserId, postId);

        // 현재 사용자가 게시글 작성자인지 여부
        boolean createdByCurrentUser = currentUserId.equals(post.getMember().getId());

        // 해당 게시글의 댓글 조회
        CommentResponse commentResponse = getCommentsByPostId(post.getId());

        // 조회된 게시글을 PostSingleResponse로 변환하여 반환 (익명처리일 경우 프로필 이미지를 DEFAULT_PROFILE_IMAGE_URL로 반환)
        return PostSingleResponse.toDTO(post, likeCount, scrapCount, commentResponse, likedByCurrentUser, scrapedByCurrentUser, createdByCurrentUser, DEFAULT_PROFILE_IMAGE_URL);
    }

    /**
     * 게시글 페이징 조회 : 게시글 종류별로 게시글 목록을 페이징 조회합니다.
     * @param type 게시글 종류
     * @param pageable 페이지 정보
     * @return 게시글 타입과 게시글 목록, 페이지 정보 반환
     */
    @Transactional(readOnly = true)
    @Override
    public PostPagingResponse getPagingPosts(String type, Pageable pageable) {
        // 게시글 종류 조회
        Board boardType = Board.findByValue(type);

//        if (boardType == null) {
//            throw new PostHandler(ErrorStatus._INVALID_BOARD_TYPE);
//        }

        Page<Post> postPage;
        if (boardType == Board.ALL) {
            // ALL 타입일 경우 모든 게시글 조회
            postPage = postRepository.findByPostReportListIsEmpty(pageable);
        } else {
            // 특정 게시판 타입의 게시글 조회
            postPage = postRepository.findByBoardAndPostReportListIsEmpty(boardType, pageable);
        }

        // PostPagingDetailResponse를 묶어서 응답 리스트 생성 (좋아요 수, 좋아요여부, 스크랩 수, 스크랩여부 포함)
        List<PostPagingDetailResponse> postResponses = postPage.getContent().stream()
                .map(post -> {
                    long likeCount = likedPostQueryService.countByPostId(post.getId());
                    boolean likedByCurrentUser = likedPostQueryService.existsByMemberIdAndPostId(post.getId());
                    long scrapCount = memberScrapRepository.countByPostId(post.getId());
                    Long currentUserId = getCurrentUserId();
                    boolean scrapedByCurrentUser = memberScrapRepository.existsByMemberIdAndPostId(currentUserId, post.getId());
                    return PostPagingDetailResponse.toDTO(post, likeCount, scrapCount, likedByCurrentUser, scrapedByCurrentUser);
                })
                .toList();

        // 게시글 목록 반환
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

    /**
     * 인기글 종류(실시간, 추천순, 댓글순)에 따라 게시글을 상위 5개씩 조회합니다.
     * @param sortType 인기글 종류
     * @return 상위 5개의 인기글 목록 반환
     * @throws PostHandler 인기글 종류를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    @Override
    public PostBest5Response getPostBest(String sortType) {
        // 인기글 종류가 REAL_TIME일 경우 댓글수, 좋아요수, 조회수를 합산하여 상위 5개 조회
        if (sortType.equals("REAL_TIME")) {
            // 인기글 조회 및 순위 생성
            List<Post> posts = postRepository.findTopByRealTimeScore();
            AtomicInteger rankCounter = new AtomicInteger(1);

//            if (posts.isEmpty()) {
//                throw new PostHandler(ErrorStatus._POST_NOT_FOUND); // 혹은 적절한 오류 타입으로 변경
//            }

            // PostBest5DetailResponse를 묶어서 리스트로 응답 생성
            List<PostBest5DetailResponse> responses = posts.stream()
                    .map(post -> PostBest5DetailResponse.from(post, rankCounter.getAndIncrement()))
                    .toList();

            // 인기글 타입과 목록 반환
            return PostBest5Response.builder()
                    .sortType("REAL_TIME")
                    .postBest5Responses(responses)
                    .build();
        }

        // 인기글 종류가 RECOMMEND일 경우 좋아요수 상위 5개 조회
        if (sortType.equals("RECOMMEND")) {
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

        // 인기글 종류가 COMMENT일 경우 댓글수 상위 5개 조회
        if (sortType.equals("COMMENT")) {
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

        // 게시글 종류가 RECOMMEND, COMMENT 중에 없을 경우 무조건 에러
        throw new PostHandler(ErrorStatus._INVALID_SORT_TYPE);
    }

    /**
     * 게시글 종류 별로 가장 최신 게시글을 1개씩 조회합니다.
     * @return 게시글 종류와 각 최신 게시글 목록 반환
     */
    @Transactional(readOnly = true)
    @Override
    public PostRepresentativeResponse getRepresentativePosts() {
        // 대표 게시글 조회
        List<Post> posts = postRepository.findRepresentativePosts();

        // PostRepresentativeDetailResponse를 묶어서 리스트로 응답 생성
        List<PostRepresentativeDetailResponse> responses = posts.stream()
                .map(PostRepresentativeDetailResponse::toDTO)
                .toList();

        // 대표 게시글 목록 반환
        return PostRepresentativeResponse.builder()
                .responses(responses)
                .build();
    }

    /**
     * 최신 공지 5개를 조회합니다.
     * @return 최신순 5개의 공지글 목록 반환
     */
    @Transactional(readOnly = true)
    @Override
    public PostAnnouncementResponse getPostAnnouncements() {
        // 공지글 조회 및 순위 생성
        List<Post> posts = postRepository.findAnnouncementPosts();
        AtomicInteger rankCounter = new AtomicInteger(1);

        // PostBest5DetailResponse를 묶어서 리스트로 응답 생성
        List<PostBest5DetailResponse> responses = posts.stream()
                .map(post -> PostBest5DetailResponse.from(post, rankCounter.getAndIncrement()))
                .toList();

        // 공지글 목록 반환
        return PostAnnouncementResponse.builder()
                .responses(responses)
                .build();
    }

    /**
     * 조회한 게시글의 댓글 리스트을 조회합니다.
     * @param postId 조회한 게시글 ID
     * @return 댓글 리스트 반환
     */
    @Transactional(readOnly = true)
    @Override
    public CommentResponse getCommentsByPostId(Long postId) {
        // 해당 게시글 Id의 댓글 조회
        List<PostComment> comments = postCommentRepository.findCommentsByPostId(postId);

        // CommentDetailResponse를 묶어서 응답 리스트 생성 (댓글 좋아요수, 댓글 좋아요/싫어요 여부 포함)
        List<CommentDetailResponse> commentResponses = comments.stream()
                .map(comment -> {
                    long likeCount = likedPostCommentQueryService.countByPostCommentIdAndIsLikedTrue(comment.getId());
                    boolean likedByCurrentUser = likedPostCommentQueryService.existsByMemberIdAndPostCommentIdAndIsLikedTrue(comment.getId());
                    boolean dislikedByCurrentUser = likedPostCommentQueryService.existsByMemberIdAndPostCommentIdAndIsLikedFalse(comment.getId());
                    return CommentDetailResponse.toDTO(comment, likeCount, likedByCurrentUser, dislikedByCurrentUser, DEFAULT_PROFILE_IMAGE_URL);
                })
                .toList();

        // 댓글 목록 반환
        return CommentResponse.builder()
                .comments(commentResponses)
                .build();
    }

    /**
     * 스크랩한 게시글을 게시글 종류 별로 페이징 조회합니다.
     * @param type 게시글 종류
     * @param pageable 페이지 정보
     * @return 게시글 종류와 스크랩한 게시글 목록, 페이지 정보 반환
     */
    @Transactional(readOnly = true)
    @Override
    public PostPagingResponse getScrapPagingPost(String type, Pageable pageable) {
        // 현재 사용자 조회
        Long currentUserId = getCurrentUserId();

        // 게시글 종류 조회
        Board boardType = Board.findByValue(type);

        Page<MemberScrap> postScrapPage;
        if (boardType == Board.ALL) {
            // ALL 타입일 경우 스크랩 된 모든 게시글 조회
            postScrapPage = memberScrapRepository.findByMemberId(currentUserId, pageable);
        } else {
            // 특정 게시판 타입의 스크랩 된 게시글 조회
            postScrapPage = memberScrapRepository.findByMemberIdAndPost_Board(currentUserId, boardType, pageable);
        }
        List<Post> scrapPosts = postScrapPage.getContent().stream()
                .map(MemberScrap::getPost)
                .toList();

        // PostPagingDetailResponse를 묶어서 응답 리스트 생성 (댓글 좋아요수, 댓글 좋아요/싫어요 여부 포함)
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

        // 스크랩 된 게시글 목록 반환
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
