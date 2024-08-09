package com.example.spot.service.post;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.api.exception.handler.PostHandler;
import com.example.spot.domain.LikedPost;
import com.example.spot.domain.Member;
import com.example.spot.domain.Post;
import com.example.spot.domain.PostComment;
import com.example.spot.domain.enums.Board;
import com.example.spot.repository.LikedPostRepository;
import com.example.spot.repository.MemberRepository;
import com.example.spot.repository.PostCommentRepository;
import com.example.spot.repository.PostRepository;
import com.example.spot.web.dto.post.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostCommandServiceImpl implements PostCommandService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final LikedPostRepository likedPostRepository;
    private final PostCommentRepository postCommentRepository;

    private final LikedPostQueryService likedPostQueryService;

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

        return PostCreateResponse.toDTO(post, member.getIsAdmin(), likeCount);
    }
    private Post createPostEntity(PostCreateRequest postCreateRequest, Member currentMember) {

        return Post.builder()
                .isAnonymous(false)
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

        // 좋아요 수 가져오기
        long likeCount = likedPostQueryService.countByPostId(post.getId());

        return PostCreateResponse.toDTO(post, member.getIsAdmin(), likeCount);
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
                .orElseThrow(() -> new PostHandler(ErrorStatus._MEMBER_NOT_FOUND));

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


}
