package com.example.spot.service.post;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.Post;
import com.example.spot.repository.MemberRepository;
import com.example.spot.repository.PostRepository;
import com.example.spot.web.dto.post.PostCreateRequest;
import com.example.spot.web.dto.post.PostCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostCommandServiceImpl implements PostCommandService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    @Override
    public PostCreateResponse createPost(PostCreateRequest postCreateRequest) {

        // ToDo 임시 Mock data, 추후에 시큐리티를 통해 Member 추출
        Long memberId = 0L;

        /*
        유저 정보를 Controller에서 받을 거면 @UserPrincipal로 받으실 건지, 아니면 SecurityUtil에서 현재 토큰으로 접근한 사용자에 대한 정보를 꺼내실건지 궁금합니다.
         */

        // 회원 정보 가져오기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // Post 객체 생성 및 연관 관계 설정
        Post post = createPostEntity(postCreateRequest, member);
        post = postRepository.save(post);

        return PostCreateResponse.toDTO(post, member.getIsAdmin());
    }
    private Post createPostEntity(PostCreateRequest postCreateRequest, Member currentMember) {

        return Post.builder()
                .isAnonymous(false) // 필요에 따라 설정
                .title(postCreateRequest.getTitle())
                .content(postCreateRequest.getContent())
                .hit(0)
                .likeNum(0)
                .commentNum(0)
                .hitNum(0)
                .board(postCreateRequest.getType())
                .member(currentMember) // 연관 관계 설정
                .build();
    }


}
