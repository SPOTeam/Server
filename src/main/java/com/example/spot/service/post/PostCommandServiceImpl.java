package com.example.spot.service.post;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.api.exception.handler.PostHandler;
import com.example.spot.domain.*;
import com.example.spot.domain.enums.Board;
import com.example.spot.domain.enums.PostStatus;
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
    private final PostReportRepository postReportRepository;

    private final LikedPostQueryService likedPostQueryService;
    private final LikedPostCommentQueryService likedPostCommentQueryService;

    /**
     * 게시글을 생성합니다.
     * @param memberId 게시글을 작성하는 회원 ID
     * @param postCreateRequest 생성할 게시글 정보
     * @return 생성된 게시글 정보
     * @throws MemberHandler 회원을 찾을 수 없는 경우
     * @throws PostHandler 관리자 권한이 없는 경우 (관리자만 공지글 작성 가능)
     */
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
        // 게시글 저장
        post = postRepository.save(post);

        // 게시글 생성 정보 반환
        return PostCreateResponse.toDTO(post);
    }
    /**
     * 게시글 객체를 생성합니다.
     * @param postCreateRequest 생성할 게시글 정보
     * @param currentMember 게시글을 작성하는 회원 정보
     * @return 생성된 게시글 객체
     */
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

    /**
     * 게시글을 수정합니다.
     * @param memberId 게시글을 수정하는 회원 ID
     * @param postId 변경할 게시글 ID
     * @param postUpdateRequest 수정할 게시글 정보
     * @return 수정된 게시글 정보
     * @throws MemberHandler 회원을 찾을 수 없는 경우
     * @throws PostHandler 게시글을 찾을 수 없는 경우
     * @throws PostHandler 현재 수정하는 회원과 게시글 작성자가 일치하지 않을 경우
     * @throws PostHandler 관리자 권한이 없는 경우 (관리자만 공지글 수정 가능)
     */
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

        // 게시글 수정
        post.edit(postUpdateRequest);

        // 수정된 게시글 정보 반환
        return PostCreateResponse.toDTO(post);
    }

    /**
     * 게시글을 삭제합니다.
     * @param memberId 게시글을 수정하는 회원 ID
     * @param postId 변경할 게시글 ID
     * @throws MemberHandler 회원을 찾을 수 없는 경우
     * @throws PostHandler 게시글을 찾을 수 없는 경우
     * @throws PostHandler 현재 삭제하는 회원과 게시글 작성자가 일치하지 않을 경우
     */
    @Transactional
    @Override
    public void deletePost(Long memberId, Long postId) {

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

    /**
     * 게시글에 좋아요를 합니다.
     * @param postId 좋아요할 게시글 ID
     * @param memberId 회원 ID
     * @return 좋아요한 게시글 ID와 게시글의 현재 좋아요 수
     * @throws MemberHandler 회원을 찾을 수 없는 경우
     * @throws PostHandler 게시글을 찾을 수 없는 경우
     * @throws PostHandler 이미 해당 게시글에 좋아요를 누른 경우
     */
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

        // 좋아요 객체 생성 및 저장
        LikedPost likedPost = LikedPost.builder()
                .post(post)
                .member(member)
                .build();

        likedPostRepository.saveAndFlush(likedPost);

        // 게시글의 현재 좋아요 수 조회
        long likeCount = likedPostQueryService.countByPostId(postId);

        // 좋아요 결과 반환
        return PostLikeResponse.builder()
                .postId(post.getId())
                .likeCount(likeCount)
                .build();
    }

    /**
     * 게시글 좋아요를 취소합니다.
     * @param postId 좋아요를 취소할 게시글 ID
     * @param memberId 회원 ID
     * @return 좋아요를 취소한 게시글 ID와 게시글의 현재 좋아요 수
     * @throws MemberHandler 회원을 찾을 수 없는 경우
     * @throws PostHandler 게시글을 찾을 수 없는 경우
     * @throws PostHandler 좋아요 하지 않은 게시글일 경우
     */
    @Transactional
    @Override
    public PostLikeResponse cancelPostLike(Long postId, Long memberId) {
        // 회원 정보 가져오기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_NOT_FOUND));
        // 좋아요 여부 확인
        LikedPost likedPost = likedPostRepository.findByMemberIdAndPostId(member.getId(), post.getId())
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_NOT_LIKED));

        // 좋아요 객체 삭제
        likedPostRepository.delete(likedPost);
        likedPostRepository.flush();

        // 게시글의 현재 좋아요 수 조회
        long likeCount = likedPostQueryService.countByPostId(postId);

        // 좋아요 취소 결과 반환
        return PostLikeResponse.builder()
                .postId(post.getId())
                .likeCount(likeCount)
                .build();
    }

    /**
     * 게시글에 댓글을 생성합니다.
     * @param postId 댓글을 작성할 게시글 ID
     * @param memberId 회원 ID
     * @param request 작성할 댓글 정보
     * @return 작성된 댓글 정보와 익명여부 반환
     * @throws PostHandler 게시글을 찾을 수 없는 경우
     * @throws MemberHandler 회원을 찾을 수 없는 경우
     */
    @Transactional
    @Override
    public CommentCreateResponse createComment(Long postId, Long memberId, CommentCreateRequest request) {
        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_NOT_FOUND));

        // 회원 정보 가져오기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        if (request.getParentCommentId() == null) {
            // 부모 댓글 생성
            PostComment comment = PostComment.builder()
                    .content(request.getContent())
                    .isAnonymous(request.isAnonymous())
                    .post(post)
                    .parentComment(null)
                    .member(member)
                    .build();

            // 댓글 객체 저장
            comment = postCommentRepository.saveAndFlush(comment);
            post.addComment(comment);
            post.plusCommentNum();

            // 생성된 댓글 정보 반환
            return CommentCreateResponse.toDTO(comment);

        } else {
            // 자식 댓글 생성
            PostComment parentComment = postCommentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new PostHandler(ErrorStatus._POST_PARENT_COMMENT_NOT_FOUND));

            PostComment comment = PostComment.builder()
                    .content(request.getContent())
                    .isAnonymous(request.isAnonymous())
                    .post(post)
                    .parentComment(parentComment)
                    .member(member)
                    .build();

            // 댓글 객체 저장
            comment = postCommentRepository.saveAndFlush(comment);
            post.addComment(comment);
            post.plusCommentNum();

            // 생성된 댓글 정보와 부모 댓글 ID 반환
            return CommentCreateResponse.toDTOwithParent(comment, parentComment.getId());
        }
    }

    /**
     * 게시글 댓글에 좋아요를 합니다.
     * @param commentId 좋아요할 댓글 ID
     * @param memberId 회원 ID
     * @return 좋아요한 댓글 ID와 게시글의 현재 좋아요와 싫어요 수
     * @throws PostHandler 댓글을 찾을 수 없는 경우
     * @throws MemberHandler 회원을 찾을 수 없는 경우
     * @throws PostHandler 이미 해당 댓글에 좋아요를 한 경우
     */
    @Transactional
    @Override
    public CommentLikeResponse likeComment(Long commentId, Long memberId) {
        // 댓글 조회하기
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_COMMENT_NOT_FOUND));

        // 회원 정보 가져오기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 댓글 좋아요 여부 확인
        if (likedPostCommentRepository.findByMemberIdAndPostCommentIdAndIsLikedTrue(memberId, commentId).isPresent()) {
            throw new PostHandler(ErrorStatus._POST_COMMENT_ALREADY_LIKED);
        }

        // 댓글 좋아요 객체 생성 및 저장 (isLiked가 true면 좋아요, false면 싫어요)
        LikedPostComment likedPostComment = LikedPostComment.builder()
                .postComment(comment)
                .member(member)
                .isLiked(true)
                .build();

        likedPostCommentRepository.saveAndFlush(likedPostComment);

        // 댓글 좋아요 수 조회
        long likeCount = likedPostCommentQueryService.countByPostCommentIdAndIsLikedTrue(commentId);

        // 댓글 싫어요 수 조회
        long disLikeCount = likedPostCommentRepository.countByPostCommentIdAndIsLikedFalse(commentId);

        // 댓글 좋아요 결과 반환
        return CommentLikeResponse.toDTO(comment.getId(), likeCount, disLikeCount);
    }

    /**
     * 게시글 댓글에 좋아요를 취소합니다.
     * @param commentId 좋아요 취소할 댓글 ID
     * @param memberId 회원 ID
     * @return 좋아요 취소한 댓글 ID와 게시글의 현재 좋아요와 싫어요 수
     * @throws PostHandler 댓글을 찾을 수 없는 경우
     * @throws MemberHandler 회원을 찾을 수 없는 경우
     * @throws PostHandler 좋아요를 하지 않은 댓글일 경우
     */
    @Transactional
    @Override
    public CommentLikeResponse cancelCommentLike(Long commentId, Long memberId) {
        // 댓글 조회하기
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_COMMENT_NOT_FOUND));

        // 회원 정보 가져오기
        memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 댓글 좋아요 여부 확인
        LikedPostComment likedPostComment = likedPostCommentRepository.findByMemberIdAndPostCommentIdAndIsLikedTrue(memberId, commentId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_COMMENT_NOT_LIKED));

        // 댓글 좋아요 객체 삭제 및 즉시 반영
        likedPostCommentRepository.delete(likedPostComment);
        likedPostCommentRepository.flush();

        // 댓글 좋아요 수 조회
        long likeCount = likedPostCommentQueryService.countByPostCommentIdAndIsLikedTrue(commentId);

        // 댓글 싫어요 수 조회
        long disLikeCount = likedPostCommentRepository.countByPostCommentIdAndIsLikedFalse(commentId);

        // 댓글 좋아요 취소 결과 반환
        return CommentLikeResponse.toDTO(comment.getId(), likeCount, disLikeCount);
    }

    /**
     * 게시글 댓글에 싫어요를 합니다.
     * @param commentId 싫어요할 댓글 ID
     * @param memberId 회원 ID
     * @return 싫어요한 댓글 ID와 게시글의 현재 좋아요와 싫어요 수
     * @throws PostHandler 댓글을 찾을 수 없는 경우
     * @throws MemberHandler 회원을 찾을 수 없는 경우
     * @throws PostHandler 이미 해당 댓글에 싫어요를 한 경우
     */
    @Transactional
    @Override
    public CommentLikeResponse dislikeComment(Long commentId, Long memberId) {
        // 댓글 조회
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_COMMENT_NOT_FOUND));

        // 회원 정보 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 싫어요 여부 확인
        if (likedPostCommentRepository.findByMemberIdAndPostCommentIdAndIsLikedFalse(memberId, commentId).isPresent()) {
            throw new PostHandler(ErrorStatus._POST_COMMENT_ALREADY_DISLIKED);
        }

        // 싫어요 객체 생성 및 저장 (isLiked가 true면 좋아요, false면 싫어요)
        LikedPostComment dislikedPostComment = LikedPostComment.builder()
                .postComment(comment)
                .member(member)
                .isLiked(false)
                .build();

        likedPostCommentRepository.saveAndFlush(dislikedPostComment);

        // 댓글 좋아요 수 조회
        long likeCount = likedPostCommentQueryService.countByPostCommentIdAndIsLikedTrue(commentId);

        // 댓글 싫어요 수 조회
        long disLikeCount = likedPostCommentRepository.countByPostCommentIdAndIsLikedFalse(commentId);

        // 댓글 싫어요 결과 반환
        return CommentLikeResponse.toDTO(comment.getId(), likeCount, disLikeCount);
    }

    /**
     * 게시글 댓글에 싫어요를 취소합니다.
     * @param commentId 싫어요 취소할 댓글 ID
     * @param memberId 회원 ID
     * @return 싫어요 취소한 댓글 ID와 게시글의 현재 좋아요와 싫어요 수
     * @throws PostHandler 댓글을 찾을 수 없는 경우
     * @throws MemberHandler 회원을 찾을 수 없는 경우
     * @throws PostHandler 싫어요를 하지 않은 댓글일 경우
     */
    @Transactional
    @Override
    public CommentLikeResponse cancelCommentDislike(Long commentId, Long memberId) {
        // 댓글 조회
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_COMMENT_NOT_FOUND));

        // 회원 정보 조회
        memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 싫어요 여부 확인
        LikedPostComment dislikedPostComment = likedPostCommentRepository.findByMemberIdAndPostCommentIdAndIsLikedFalse(memberId, commentId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_COMMENT_NOT_DISLIKED));

        // 싫어요 객체 삭제 및 즉시 반영
        likedPostCommentRepository.delete(dislikedPostComment);
        likedPostCommentRepository.flush();

        // 댓글 좋아요 수 조회
        long likeCount = likedPostCommentQueryService.countByPostCommentIdAndIsLikedTrue(commentId);

        // 댓글 싫어요 수 조회
        long disLikeCount = likedPostCommentRepository.countByPostCommentIdAndIsLikedFalse(commentId);

        // 댓글 싫어요 취소 결과 반환
        return CommentLikeResponse.toDTO(comment.getId(), likeCount, disLikeCount);
    }

    /**
     * 게시글을 스크랩 합니다.
     * @param postId 스크랩할 게시글 ID
     * @param memberId 회원 ID
     * @return 스크랩한 게시글 ID와 스크랩 수
     * @throws PostHandler 게시글을 찾을 수 없는 경우
     * @throws MemberHandler 회원을 찾을 수 없는 경우
     * @throws PostHandler 이미 해당 게시글을 스크랩 한 경우
     */
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
        MemberScrap memberScrap = MemberScrap.builder()
                .member(member)
                .post(post)
                .build();

        memberScrapRepository.saveAndFlush(memberScrap);

        // 스크랩된 리스트의 갯수를 조회하여 스크랩 수 계산
        long scrapCount = memberScrapRepository.countByPostId(postId);

        // 스크랩 결과 반환
        return ScrapPostResponse.builder()
                .postId(post.getId())
                .scrapCount(scrapCount)
                .build();
    }

    /**
     * 게시글 스크랩을 취소합니다.
     * @param postId 스크랩 취소할 게시글 ID
     * @param memberId 회원 ID
     * @return 스크랩 취소한 게시글 ID와 스크랩 수
     * @throws PostHandler 게시글을 찾을 수 없는 경우
     * @throws MemberHandler 회원을 찾을 수 없는 경우
     * @throws PostHandler 스크랩하지 않은 게시글인 경우
     */
    @Transactional
    @Override
    public ScrapPostResponse cancelPostScrap(Long postId, Long memberId) {
        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_NOT_FOUND));

        // 회원 정보 가져오기
        memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 스크랩 여부 확인
        MemberScrap memberScrap = memberScrapRepository.findByMemberIdAndPostId(memberId, postId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_NOT_SCRAPPED));

        // 스크랩 삭제 및 즉시 반영
        memberScrapRepository.delete(memberScrap);
        memberScrapRepository.flush();

        // 스크랩된 리스트의 갯수를 조회하여 스크랩 수 계산
        long scrapCount = memberScrapRepository.countByPostId(postId);

        // 스크랩 취소 결과 반환
        return ScrapPostResponse.builder()
                .postId(post.getId())
                .scrapCount(scrapCount)
                .build();
    }

    /**
     * 게시글 스크랩 여러개를 한번에 취소합니다.
     * @param request 취소할 스크랩 ID 리스트
     * @return 스크랩 취소 결과 반환
     */
    @Transactional
    @Override
    public ScrapsPostDeleteResponse cancelPostScraps(ScrapAllDeleteRequest request) {
        // 현재 로그인한 회원 조회
        Long currentMemberId = getCurrentUserId();

        // 삭제할 List<Long> scrapIds cancelPostScrap() 순회
        List<ScrapPostResponse> deletePostResponses = request.getDeletePostIds().stream().map(
                deletePostId -> cancelPostScrap(deletePostId, currentMemberId)
        ).toList();

        // 스크랩 취소 결과 반환
        return ScrapsPostDeleteResponse.builder()
                .cancelScraps(deletePostResponses)
                .build();
    }

    @Override
    public PostReportResponse reportPost(Long postId, Long memberId) {

        // 동일한 게시글에 대한 중복 신고 방지
        if (postReportRepository.existsByPostIdAndMemberId(postId, memberId)) {
            throw new PostHandler(ErrorStatus._POST_ALREADY_REPORTED);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_NOT_FOUND));

        if (post.getMember().getId().equals(memberId)) {
            throw new PostHandler(ErrorStatus._POST_REPORT_SELF);
        }

        PostReport postReport = PostReport.builder()
                .postStatus(PostStatus.신고접수)
                .post(post)
                .member(member).build();

        postReportRepository.save(postReport);

        return PostReportResponse.toDTO(postId, memberId);
    }
}
