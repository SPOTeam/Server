package com.example.spot.domain;

import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.enums.Carrier;
import com.example.spot.domain.mapping.*;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Entity
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private Carrier carrier;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(nullable = false)
    private LocalDate birth;

    @Column
    private LocalDateTime inactive;

    @Column(nullable = false)
    private Boolean personalInfo;

    @Column(nullable = false)
    private Boolean idInfo;

    //== 스터디 희망사유 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<StudyReason> studyReasonList;

    //== 알림 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Notification> notificationList;

    //== 해당 회원에 대한 신고 내역 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberReport> memberReportList;

    //== 회원이 선호하는 테마 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberTheme> memberThemeList;

    //== 회원의 출석 목록 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberAttendance> memberAttendanceList;

    //== 회원이 참여하는 스터디 목록 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberStudy> memberStudyList;

    //== 회원이 찜한 스터디 목록 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<PreferredStudy> preferredStudyList;

    //== 회원이 선호하는 지역 목록 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<PreferredRegion> preferredRegionList;

    //== 회원이 작성한 게시글 목록 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Post> postList;

    //== 회원이 좋아요한 게시글 목록 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<LikedPost> likedPostList;

    //== 회원이 선호하는 지역 목록 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<PostReport> postReportList;

    //== 회원이 스크랩한 게시글 목록 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberScrap> memberScrapList;

    //== 회원이 작성한 게시글 댓글 목록 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Comment> commentList;

    //== 회원이 좋아요한 게시글 댓글 목록 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<LikedComment> likedCommentList;

    //== 회원이 작성한 스터디 게시글 목록 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<StudyPost> studyPostList;

    //== 회원이 좋아요한 스터디 게시글 목록 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<StudyLikedPost> studyLikedPostList;

    //== 회원이 작성한 스터디 게시글 댓글 목록 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<StudyPostComment> studyPostCommentList;

    //== 회원이 좋아요한 게시글 댓글 목록 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<StudyLikedComment> studyLikedCommentList;

    //== 회원이 생성한 투표 목록 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Vote> voteList;

    //== 회원이 투표한 항목 목록 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberVote> memberVoteList;


}
