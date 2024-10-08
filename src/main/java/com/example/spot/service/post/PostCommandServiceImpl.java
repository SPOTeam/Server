package com.example.spot.service.post;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.api.exception.handler.PostHandler;
import com.example.spot.domain.*;
import com.example.spot.domain.enums.Board;
import com.example.spot.domain.mapping.MemberScrap;
import com.example.spot.repository.*;
import com.example.spot.web.dto.post.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.example.spot.security.utils.SecurityUtils.getCurrentUserId;

@Service
@RequiredArgsConstructor
public class PostCommandServiceImpl implements PostCommandService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final LikedPostRepository likedPostRepository;
    private final PostCommentRepository postCommentRepository;
    private final LikedPostCommentRepository likedPostCommentRepository;
    private final MemberScrapRepository memberScrapRepository;

    private final LikedPostQueryService likedPostQueryService;
    private final LikedPostCommentQueryService likedPostCommentQueryService;


    @Transactional
    @Override
    public PostCreateResponse createPost(Long memberId, PostCreateRequest postCreateRequest) {

        // 회원 정보 가져오기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 공지"SPOT_ANNOUNCEMENT" 게시글은 관리자만 생성 가능
        if (postCreateRequest.getType() == Board.SPOT_ANNOUNCEMENT && !member.getIsAdmin()) {
            throw new PostHandler(ErrorStatus._FORBIDDEN); // 관리자만 접근 가능
        }

        // Post 객체 생성 및 연관 관계 설정
        Post post = createPostEntity(postCreateRequest, member);
        post = postRepository.save(post);

        int likeCount = 0;

        return PostCreateResponse.toDTO(post);
    }
    private Post createPostEntity(PostCreateRequest postCreateRequest, Member currentMember) {

        return Post.builder()
                .isAnonymous(postCreateRequest.isAnonymous())
                .title(postCreateRequest.getTitle())
                .content(postCreateRequest.getContent())
                .scrapNum(0)
                .commentNum(0)
                .hitNum(0)
                .board(postCreateRequest.getType())
                .member(currentMember)
                .build();
    }


    @Transactional
    @Override
    public PostCreateResponse updatePost(Long memberId, Long postId, PostUpdateRequest postUpdateRequest) {

        // 회원 정보 가져오기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_NOT_FOUND));

        // 현재 멤버와 게시글 작성자 일치 여부 확인
        if (!post.getMember().getId().equals(member.getId())) {
            throw new PostHandler(ErrorStatus._POST_NOT_AUTHOR);
        }

        // 공지"SPOT_ANNOUNCEMENT" 게시글은 관리자만 가능
        if (postUpdateRequest.getType() == Board.SPOT_ANNOUNCEMENT && !member.getIsAdmin()) {
            throw new PostHandler(ErrorStatus._FORBIDDEN);
        }

        post.edit(postUpdateRequest);

        return PostCreateResponse.toDTO(post);
    }


    @Transactional
    @Override
    public void deletePost(Long memberId, Long postId) {
        // 임시 Mock data, 추후에 시큐리티를 통해 Member 추출
        //Long memberId = 0L;

        // 회원 정보 가져오기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_NOT_FOUND));

        // 현재 멤버와 게시글 작성자 일치 여부 확인
        if (!post.getMember().getId().equals(member.getId())) {
            throw new PostHandler(ErrorStatus._POST_NOT_AUTHOR); // 권한 없음을 나타내는 에러 처리
        }
        // 게시글 삭제
        postRepository.delete(post);
    }

    @Transactional
    @Override
    public PostLikeResponse likePost(Long postId, Long memberId) {
        // 회원 정보 가져오기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_NOT_FOUND));
        //좋아요 여부 확인
        if (likedPostRepository.findByMemberIdAndPostId(memberId, postId).isPresent()) {
            throw new PostHandler(ErrorStatus._POST_ALREADY_LIKED);
        }

        LikedPost likedPost = new LikedPost(post, member);
        likedPostRepository.saveAndFlush(likedPost);

        long likeCount = likedPostQueryService.countByPostId(postId);

        return PostLikeResponse.builder()
                .postId(post.getId())
                .likeCount(likeCount)
                .build();
    }

    @Transactional
    @Override
    public PostLikeResponse cancelPostLike(Long postId, Long memberId) {
        // 회원 정보 가져오기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_NOT_FOUND));
        //좋아요 여부 확인
        LikedPost likedPost = likedPostRepository.findByMemberIdAndPostId(member.getId(), post.getId())
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_NOT_LIKED));

        likedPostRepository.delete(likedPost);

        likedPostRepository.flush();

        long likeCount = likedPostQueryService.countByPostId(postId);

        return PostLikeResponse.builder()
                .postId(post.getId())
                .likeCount(likeCount)
                .build();
    }

    @Transactional
    @Override
    public CommentCreateResponse createComment(Long postId, Long memberId, CommentCreateRequest request) {
        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_NOT_FOUND));

        // 회원 정보 가져오기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        //부모 댓글아이디
        Long parentCommentId = request.getParentCommentId();
        Optional<PostComment> optionalParentComment = postCommentRepository.findById(parentCommentId);

        if (optionalParentComment.isPresent()) {
            PostComment parentComment = optionalParentComment.get();
            // 자식댓글 생성
            PostComment comment = PostComment.builder()
                    .content(request.getContent())
                    .isAnonymous(request.isAnonymous())
                    .post(post)
                    .parentComment(parentComment)
                    .member(member)
                    .build();

            postCommentRepository.saveAndFlush(comment);
            return CommentCreateResponse.toDTOwithParent(comment, parentComment.getId());

        } else {
            // 부모댓글 생성
            PostComment comment = PostComment.builder()
                    .content(request.getContent())
                    .isAnonymous(request.isAnonymous())
                    .post(post)
                    .member(member)
                    .build();

            postCommentRepository.saveAndFlush(comment);
            return CommentCreateResponse.toDTO(comment);
        }
    }


    //게시글 댓글 좋아요
    @Transactional
    @Override
    public CommentLikeResponse likeComment(Long commentId, Long memberId) {
        //댓글 조회하기
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_COMMENT_NOT_FOUND));

        //회원 정보 가져오기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        //좋아요 여부 확인
        if (likedPostCommentRepository.findByMemberIdAndPostCommentIdAndIsLikedTrue(memberId, commentId).isPresent()) {
            throw new PostHandler(ErrorStatus._POST_COMMENT_ALREADY_LIKED);
        }

        LikedPostComment likedPostComment = new LikedPostComment(comment, member, true);
        likedPostCommentRepository.saveAndFlush(likedPostComment);

        long likeCount = likedPostCommentQueryService.countByPostCommentIdAndIsLikedTrue(commentId);

        long disLikeCount = likedPostCommentRepository.countByPostCommentIdAndIsLikedFalse(commentId);

        return CommentLikeResponse.toDTO(comment.getId(), likeCount, disLikeCount);
    }

    //게시글 댓글 좋아요 취소
    @Transactional
    @Override
    public CommentLikeResponse cancelCommentLike(Long commentId, Long memberId) {
        //댓글 조회하기
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_COMMENT_NOT_FOUND));

        //회원 정보 가져오기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        //좋아요 여부 확인
        LikedPostComment likedPostComment = likedPostCommentRepository.findByMemberIdAndPostCommentIdAndIsLikedTrue(memberId, commentId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_COMMENT_NOT_LIKED));

        likedPostCommentRepository.delete(likedPostComment);
        likedPostCommentRepository.flush();

        long likeCount = likedPostCommentQueryService.countByPostCommentIdAndIsLikedTrue(commentId);

        long disLikeCount = likedPostCommentRepository.countByPostCommentIdAndIsLikedFalse(commentId);

        return CommentLikeResponse.toDTO(comment.getId(), likeCount, disLikeCount);
    }

    //게시글 댓글 싫어요
    @Transactional
    @Override
    public CommentLikeResponse dislikeComment(Long commentId, Long memberId) {
        //댓글 조회
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_COMMENT_NOT_FOUND));

        //회원 정보
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        //싫어요 여부
        if (likedPostCommentRepository.findByMemberIdAndPostCommentIdAndIsLikedFalse(memberId, commentId).isPresent()) {
            throw new PostHandler(ErrorStatus._POST_COMMENT_ALREADY_DISLIKED);
        }

        LikedPostComment dislikedPostComment = new LikedPostComment(comment, member, false);
        likedPostCommentRepository.saveAndFlush(dislikedPostComment);

        long likeCount = likedPostCommentQueryService.countByPostCommentIdAndIsLikedTrue(commentId);

        long disLikeCount = likedPostCommentRepository.countByPostCommentIdAndIsLikedFalse(commentId);

        return CommentLikeResponse.toDTO(comment.getId(), likeCount, disLikeCount);
    }

    //게시글 댓글 싫어요 취소
    @Transactional
    @Override
    public CommentLikeResponse cancelCommentDislike(Long commentId, Long memberId) {
        //댓글 조회
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_COMMENT_NOT_FOUND));

        //회원 정보
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        //싫어요 여부
        LikedPostComment dislikedPostComment = likedPostCommentRepository.findByMemberIdAndPostCommentIdAndIsLikedFalse(memberId, commentId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_COMMENT_NOT_DISLIKED));

        likedPostCommentRepository.delete(dislikedPostComment);
        likedPostCommentRepository.flush();

        long likeCount = likedPostCommentQueryService.countByPostCommentIdAndIsLikedTrue(commentId);

        long disLikeCount = likedPostCommentRepository.countByPostCommentIdAndIsLikedFalse(commentId);

        return CommentLikeResponse.toDTO(comment.getId(), likeCount, disLikeCount);
    }

    // 게시글 스크랩
    @Transactional
    @Override
    public ScrapPostResponse scrapPost(Long postId, Long memberId) {
        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_NOT_FOUND));

        // 회원 정보 가져오기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 스크랩 여부 확인
        if (memberScrapRepository.findByMemberIdAndPostId(memberId, postId).isPresent()) {
            throw new PostHandler(ErrorStatus._POST_ALREADY_SCRAPPED);
        }

        // 스크랩 정보 저장
        MemberScrap memberScrap = new MemberScrap(post, member);
        memberScrapRepository.saveAndFlush(memberScrap);

        // 스크랩된 리스트의 갯수를 조회하여 스크랩 수 계산
        long scrapCount = memberScrapRepository.countByPostId(postId);

        return ScrapPostResponse.builder()
                .postId(post.getId())
                .scrapCount(scrapCount)
                .build();
    }


    @Transactional
    @Override
    public ScrapPostResponse cancelPostScrap(Long postId, Long memberId) {
        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_NOT_FOUND));

        // 회원 정보 가져오기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        //스크랩 여부
        MemberScrap memberScrap = memberScrapRepository.findByMemberIdAndPostId(memberId, postId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_NOT_SCRAPPED));

        //스크랩 삭제
        memberScrapRepository.delete(memberScrap);
        memberScrapRepository.flush();

        // 스크랩된 리스트의 갯수를 조회하여 스크랩 수 계산
        long scrapCount = memberScrapRepository.countByPostId(postId);

        return ScrapPostResponse.builder()
                .postId(post.getId())
                .scrapCount(scrapCount)
                .build();
    }

    @Transactional
    @Override
    public ScrapsPostDeleteResponse cancelPostScraps(ScrapAllDeleteRequest request) {
        Long currentMemberId = getCurrentUserId();

        List<ScrapPostResponse> deletePostResponses = request.getDeletePostIds().stream().map(
                deletePostId -> cancelPostScrap(deletePostId, currentMemberId)
        ).toList();

        return ScrapsPostDeleteResponse.builder()
                .cancelScraps(deletePostResponses)
                .build();
    }


}
