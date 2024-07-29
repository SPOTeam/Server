package com.example.spot.domain;

import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.enums.Carrier;
import com.example.spot.domain.enums.LoginType;
import com.example.spot.domain.mapping.*;
import com.example.spot.domain.study.Study;
import com.example.spot.domain.study.StudyPost;
import com.example.spot.domain.study.StudyPostComment;
import com.example.spot.domain.study.Vote;
import jakarta.persistence.*;
import java.util.ArrayList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@Builder
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, length = 50, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private Carrier carrier;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(nullable = false)
    private LocalDate birth;

    @Column(nullable = false)
    private String profileImage;

    @Column
    private LocalDateTime inactive;

    @Column(nullable = false)
    private Boolean personalInfo;

    @Column(nullable = false)
    private Boolean idInfo;

    @Column(nullable = false, columnDefinition = "BIT DEFAULT 0")
    private Boolean isAdmin;

    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    //== 스터디 희망사유 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    @Builder.Default
    private List<StudyReason> studyReasonList = new ArrayList<>();

    //== 알림 ==//
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Notification> notificationList = new ArrayList<>();

    //== 해당 회원에 대한 신고 내역 ==//
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberReport> memberReportList = new ArrayList<>();

    //== 회원이 선호하는 테마 ==//
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberTheme> memberThemeList = new ArrayList<>();

    //== 회원의 출석 목록 ==//
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberAttendance> memberAttendanceList = new ArrayList<>();

    //== 회원이 참여하는 스터디 목록 ==//
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberStudy> memberStudyList = new ArrayList<>();

    //== 회원이 찜한 스터디 목록 ==//
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<PreferredStudy> preferredStudyList = new ArrayList<>();

    //== 회원이 선호하는 지역 목록 ==//
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<PreferredRegion> preferredRegionList = new ArrayList<>();

   ////== 회원이 작성한 게시글 목록 ==//
   @Builder.Default
   @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
   private List<Post> postList = new ArrayList<>();

   ////== 회원이 좋아요한 게시글 목록 ==//
   @Builder.Default
   @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
   private List<LikedPost> likedPostList = new ArrayList<>();

   ////== 회원이 선호하는 지역 목록 ==//
   @Builder.Default
   @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
   private List<PostReport> postReportList = new ArrayList<>();

   ////== 회원이 스크랩한 게시글 목록 ==//
   @Builder.Default
   @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
   private List<MemberScrap> memberScrapList = new ArrayList<>();

   ////== 회원이 작성한 게시글 댓글 목록 ==//
   @Builder.Default
   @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
   private List<PostComment> postCommentList = new ArrayList<>();

   ////== 회원이 좋아요한 게시글 댓글 목록 ==//
   @Builder.Default
   @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
   private List<LikedPostComment> likedCommentList = new ArrayList<>();

    //== 회원이 작성한 스터디 게시글 목록 ==//
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<StudyPost> studyPostList = new ArrayList<>();

    //== 회원이 좋아요한 스터디 게시글 목록 ==//
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<StudyLikedPost> studyLikedPostList = new ArrayList<>();

    //== 회원이 작성한 스터디 게시글 댓글 목록 ==//
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<StudyPostComment> studyPostCommentList = new ArrayList<>();

    //== 회원이 좋아요한 게시글 댓글 목록 ==//
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<StudyLikedComment> studyLikedCommentList = new ArrayList<>();

    //== 회원이 생성한 투표 목록 ==//
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Vote> voteList = new ArrayList<>();

    //== 회원이 투표한 항목 목록 ==//
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberVote> memberVoteList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<PreferredRegion> regions = new ArrayList<>();



/* ----------------------------- 연관관계 메소드 ------------------------------------- */

    public void addMemberStudy(MemberStudy memberStudy) {
        memberStudyList.add(memberStudy);
        memberStudy.setMember(this);
    }

    public void addPreferredRegion(PreferredRegion preferredRegion) {
        if (this.regions == null) {
            this.regions = new ArrayList<>(); // 재초기화 (안정성 추가)
        }
        this.regions.add(preferredRegion);
        preferredRegion.setMember(this); // 양방향 관계 설정
    }

    public void addMemberTheme(MemberTheme memberTheme) {
        if (this.memberThemeList == null) {
            this.memberThemeList = new ArrayList<>(); // 재초기화 (안정성 추가)
        }
        this.memberThemeList.add(memberTheme);
        memberTheme.setMember(this); // 양방향 관계 설정
    }

    public void addMemberAttendance(MemberAttendance memberAttendance) {
        if (this.memberAttendanceList == null) {
            this.memberAttendanceList = new ArrayList<>();
        }
        this.memberAttendanceList.add(memberAttendance);
        memberAttendance.setMember(this);
    }
}
