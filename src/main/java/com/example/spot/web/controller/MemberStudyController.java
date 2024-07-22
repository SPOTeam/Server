package com.example.spot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "MemberStudy", description = "MemberStudy API(내 스터디 관련 API)")
@RestController
@RequestMapping("/spot")
public class MemberStudyController {

/* ----------------------------- 진행중인 스터디 관련 API ------------------------------------- */

    @Operation(summary = "[진행중인 스터디] 진행중인 스터디 개수 불러오기", description = """ 
        ## [진행중인 스터디] 마이페이지에 로그인한 회원이 참여하는 스터디 개수를 불러옵니다.
        로그인한 회원이 참여하는 스터디 중 status = ON인 스터디의 개수가 반환됩니다.
        """)
    @GetMapping("/members/{memberId}/on-studies/num")
    public void getNumOfOnStudies(@PathVariable Long memberId) {
    }

    @Operation(summary = "[진행중인 스터디] 진행중인 스터디 목록 불러오기", description = """ 
        ## [진행중인 스터디] 마이페이지 > 진행중 클릭, 로그인한 회원이 진행중인 스터디 목록을 불러옵니다.
        로그인한 회원이 참여하는 스터디 중 status = ON인 스터디의 목록이 반환됩니다.
        """)
    @GetMapping("/members/{memberId}/on-studies")
    public void getAllOnStudies(@PathVariable Long memberId) {
    }

    @Operation(summary = "[진행중인 스터디] 스터디 탈퇴하기", description = """ 
        ## [진행중인 스터디] 마이페이지 > 진행중 > 진행중인 스터디의 메뉴 클릭, 로그인한 회원이 현재 진행중인 스터디에서 탈퇴합니다.
        로그인한 회원이 참여하는 특정 스터디에 대해 member_study 튜플을 삭제합니다.
        """)
    @DeleteMapping("/members/{memberId}/studies/{studyId}/withdrawal")
    public void withdrawFromStudy(@PathVariable Long memberId, @PathVariable Long studyId) {
    }

    @Operation(summary = "[진행중인 스터디] 스터디 끝내기", description = """ 
        ## [진행중인 스터디] 마이페이지 > 진행중 > 진행중인 스터디의 메뉴 클릭, 로그인한 회원이 운영중인 스터디를 끝냅니다.
        로그인한 회원이 운영하는 특정 스터디에 대해 study status OFF로 전환합니다.
        """)
    @PatchMapping("/studies/{studyId}/termination")
    public void terminateStudy(@PathVariable Long studyId) {
    }

    @Operation(summary = "[진행중인 스터디] 스터디 정보 수정하기", description = """ 
        ## [진행중인 스터디] 마이페이지 > 진행중 > 진행중인 스터디의 메뉴 클릭, 로그인한 회원이 운영중인 스터디의 정보를 수정합니다.
        로그인한 회원이 운영하는 특정 스터디에 대해 study 정보를 수정합니다.
        """)
    @PatchMapping("/studies/{studyId}")
    public void updateStudy(@PathVariable Long studyId) {
    }

/* ----------------------------- 모집중인 스터디 관련 API ------------------------------------- */


    @Operation(summary = "[모집중인 스터디] 모집중인 스터디 개수 불러오기", description = """ 
        ## [모집중인 스터디] 마이페이지에 로그인한 회원이 모집중인 스터디 개수를 불러옵니다.
        로그인한 회원이 운영하는 스터디 중 study_state = RECRUITING인 스터디의 개수가 반환됩니다.
        """)
    @GetMapping("/members/{memberId}/recruiting-studies/num")
    public void getNumOfRecruitingStudies(@PathVariable Long memberId) {
    }

    @Operation(summary = "[모집중인 스터디] 모집중인 스터디 목록 불러오기", description = """ 
        ## [모집중인 스터디] 마이페이지 > 모집중 클릭, 로그인한 회원이 모집중인 스터디 목록을 불러옵니다.
        로그인한 회원이 운영하는 스터디 중 study_state = RECRUITING인 스터디의 목록이 반환됩니다.
        """)
    @GetMapping("/members/{memberId}/recruiting-studies")
    public void getAllRecruitingStudies(@PathVariable Long memberId) {
    }

    @Operation(summary = "[모집중인 스터디] 스터디별 신청 회원 목록 불러오기", description = """ 
        ## [모집중인 스터디] 마이페이지 > 모집중 > 스터디 클릭, 로그인한 회원이 모집중인 스터디에 신청한 회원 목록을 불러옵니다.
        로그인한 회원이 모집중인 특정 스터디에 대해 member_study의 application_status가 APPLIED인 회원 목록이 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/applicants")
    public void getAllApplicants(@PathVariable Long studyId) {
    }

    @Operation(summary = "[모집중인 스터디] 스터디 신청 정보(이름, 자기소개) 불러오기", description = """ 
        ## [모집중인 스터디] 마이페이지 > 모집중 > 스터디 > 신청 회원 클릭, 로그인한 회원이 모집중인 스터디에 신청한 회원의 정보를 불러옵니다.
        로그인한 회원이 모집중인 특정 스터디에 신청한 회원의 정보(member.name & member_study.introduction)가 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/applicants/{applicantId}")
    public void getApplicantInfo(@PathVariable Long studyId, @PathVariable Long applicantId) {
    }

    @Operation(summary = "[모집중인 스터디] 스터디 신청 거절하기", description = """ 
        ## [모집중인 스터디] 마이페이지 > 모집중 > 스터디 > 신청 회원 > 거절 클릭, 로그인한 회원이 모집중인 스터디에 신청한 회원을 거절합니다.
        로그인한 회원이 모집중인 특정 스터디에 신청한 회원을 member_study에서 삭제합니다.
        """)
    @DeleteMapping("/studies/{studyId}/applicants/{applicantId}")
    public void rejectApplicant(@PathVariable Long studyId, @PathVariable Long applicantId) {
    }

    @Operation(summary = "[모집중인 스터디] 스터디 신청 수락하기", description = """ 
        ## [모집중인 스터디] 마이페이지 > 모집중 > 스터디 > 신청 회원 > 수락 클릭, 로그인한 회원이 모집중인 스터디에 신청한 회원을 수락합니다.
        로그인한 회원이 모집중인 특정 스터디에 신청한 회원의 application_status를 APPROVED로 수정합니다.
        """)
    @PatchMapping("/studies/{studyId}/applicants/{applicantId}")
    public void acceptApplicant(@PathVariable Long studyId, @PathVariable Long applicantId) {
    }

/* ----------------------------- 신청한 스터디 관련 API ------------------------------------- */

    @Operation(summary = "[신청한 스터디] 신청한 스터디 개수 불러오기", description = """ 
        ## [신청한 스터디] 마이페이지에 로그인한 회원이 신청한 스터디 개수를 불러옵니다.
        로그인한 회원이 신청한 스터디(ApplicationStatus = APPLIED)의 개수가 반환됩니다.
        """)
    @GetMapping("/members/{memberId}/applied-studies/num")
    public void getNumOfAppliedStudies(@PathVariable Long memberId) {
    }

    @Operation(summary = "[신청한 스터디] 신청한 스터디 목록 불러오기", description = """ 
        ## [신청한 스터디] 마이페이지 > 신청한, 로그인한 회원이 신청한 스터디 목록을 불러옵니다.
        로그인한 회원이 신청한 스터디(ApplicationStatus = APPLIED)의 목록이 반환됩니다.
        """)
    @GetMapping("/members/{memberId}/applied-studies")
    public void getAppliedStudies(@PathVariable Long memberId) {
    }

/* ----------------------------- 스터디 상세 정보 관련 API ------------------------------------- */

    @Operation(summary = "[스터디 상세 정보] 스터디 최근 공지 1개 불러오기", description = """ 
        ## [스터디 상세 정보] 내 스터디 > 스터디 클릭, 로그인한 회원이 참여하는 특정 스터디의 최근 공지 1개를 불러옵니다.
        study_post의 announced_at이 가장 최근인 공지 1개가 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/announce")
    public void getRecentAnnouncement(@PathVariable Long studyId) {
    }

    @Operation(summary = "[스터디 상세 정보] 다가오는 모임 목록 불러오기", description = """ 
        ## [스터디 상세 정보] 내 스터디 > 스터디 클릭, 로그인한 회원이 참여하는 특정 스터디의 다가오는 모임 목록을 불러옵니다.
        현재 시점 이후에 진행되는 모임 일정의 목록을 schedule에서 반환합니다.
        """)
    @GetMapping("/studies/{studyId}/upcoming-schedules")
    public void getUpcomingSchedules(@PathVariable Long studyId) {
    }

    @Operation(summary = "[스터디 상세 정보] 스터디에 참여하는 회원 목록 불러오기", description = """ 
        ## [스터디 상세 정보] 로그인한 회원이 참여하는 특정 스터디의 회원 목록을 불러옵니다.
        member_study에서 application_status=APPROVED인 회원의 목록(이름, 프로필 사진 포함)이 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/members")
    public void getStudyMembers(@PathVariable Long studyId) {
    }


/* ----------------------------- 스터디 일정 관련 API ------------------------------------- */

    @Operation(summary = "[스터디 일정] 월별 일정 불러오기", description = """ 
        ## [스터디 일정] 내 스터디 > 스터디 > 캘린더 클릭, 로그인한 회원이 참여하는 특정 스터디의 일정을 월 단위로 불러옵니다.
        처음 캘린더를 클릭하면 오늘 날짜가 포함된 연/월에 해당하는 일정 목록이 schedule에서 반환됩니다.
        캘린더를 넘기면 해당 연/월에 해당하는 일정 목록이 schedule에서 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/schedules")
    public void getMonthlySchedules(@PathVariable Long studyId) {
    }

    @Operation(summary = "[스터디 일정] 일정 추가하기", description = """ 
        ## [스터디 일정] 내 스터디 > 스터디 > 캘린더 > 추가 버튼 클릭, 로그인한 회원이 운영하는 특정 스터디에 일정을 추가합니다.
        로그인한 회원이 owner인 경우 schedule에 새로운 일정을 등록합니다.
        """)
    @PostMapping("/members/{memberId}/studies/{studyId}/schedules")
    public void addSchedule(@PathVariable Long memberId, @PathVariable Long studyId) {
    }


/* ----------------------------- 스터디 게시글 관련 API ------------------------------------- */

    @Operation(summary = "[스터디 게시글] 글 목록 불러오기", description = """ 
        ## [스터디 게시글] 내 스터디 > 스터디 > 게시판 클릭, 로그인한 회원이 참여하는 특정 스터디의 게시글 목록을 불러옵니다.
        로그인한 회원이 참여하는 특정 스터디의 study_post 목록이 최신순으로 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/posts")
    public void getAllPosts(@PathVariable Long studyId) {
    }

    @Operation(summary = "[스터디 게시글] 게시글 불러오기", description = """ 
        ## [스터디 게시글] 내 스터디 > 스터디 > 게시판 > 게시글 클릭, 로그인한 회원이 참여하는 특정 스터디의 게시글을 불러옵니다.
        로그인한 회원이 참여하는 특정 스터디의 study_post 정보가 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/posts/{studyPostId}")
    public void getPost(@PathVariable Long studyId, @PathVariable Long studyPostId) {
    }

    @Operation(summary = "[스터디 게시글] 좋아요 개수 불러오기", description = """ 
        ## [스터디 게시글] 내 스터디 > 스터디 > 게시판 > 게시글 클릭, 로그인한 회원이 참여하는 특정 스터디의 게시글에 눌린 좋아요 개수를 불러옵니다.
        로그인한 회원이 참여하는 특정 스터디의 study_post의 like_num이 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/posts/{studyPostId}/like-num")
    public void getLikeNum(@PathVariable Long studyId, @PathVariable Long studyPostId) {
    }

    @Operation(summary = "[스터디 게시글] 댓글 개수 불러오기", description = """ 
        ## [스터디 게시글] 내 스터디 > 스터디 > 게시판 > 게시글 클릭, 로그인한 회원이 참여하는 특정 스터디의 게시글에 달린 댓/답글 개수를 불러옵니다.
        로그인한 회원이 참여하는 특정 스터디의 study_post의 comment_num이 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/posts/{studyPostId}/comment-num")
    public void getCommentNum(@PathVariable Long studyId, @PathVariable Long studyPostId) {
    }

    @Operation(summary = "[스터디 게시글] 조회수 불러오기", description = """ 
        ## [스터디 게시글] 내 스터디 > 스터디 > 게시판 > 게시글 클릭, 로그인한 회원이 참여하는 특정 스터디의 게시글에 대한 조회수를 불러옵니다.
        로그인한 회원이 참여하는 특정 스터디의 study_post의 hit_num이 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/posts/{studyPostId}/hit-num")
    public void getHitNum(@PathVariable Long studyId, @PathVariable Long studyPostId) {
    }

    @Operation(summary = "[스터디 게시글] 전체 댓글 불러오기", description = """ 
        ## [스터디 게시글] 내 스터디 > 스터디 > 게시판 > 게시글 클릭, 로그인한 회원이 참여하는 특정 스터디의 게시글에 달린 모든 댓글을 불러옵니다.
        특정 study_post에 대한 comment(댓/답글) 목록이 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/posts/{studyPostId}/comments")
    public void getAllComments(@PathVariable Long studyId, @PathVariable Long studyPostId) {
    }

    @Operation(summary = "[스터디 게시글] 게시글 작성하기", description = """ 
        ## [스터디 게시글] 내 스터디 > 스터디 > 게시판 > 작성 버튼 클릭, 로그인한 회원이 참여하는 특정 스터디에서 새로운 게시글을 등록합니다.
        스터디에 참여하는 회원이 작성한 게시글을 study_post에 저장합니다.
        """)
    @PostMapping("/members/{memberId}/studies/{studyId}/posts")
    public void createPost(@PathVariable Long memberId, @PathVariable Long studyId) {
    }

/* ----------------------------- 스터디 투표 관련 API ------------------------------------- */

    @Operation(summary = "[스터디 투표] 투표 목록 불러오기", description = """ 
        ## [스터디 투표] 내 스터디 > 스터디 > 투표 클릭, 로그인한 회원이 참여하는 특정 스터디의 투표 목록을 불러옵니다.
        진행 중(finished_at 이전)인 투표 목록과 마감(finished_at 이후)된 투표 목록을 구분하여 반환합니다.
        """)
    @GetMapping("/studies/{studyId}/votes")
    public void getAllVotes(@PathVariable Long studyId) {
    }

    @Operation(summary = "[스터디 투표] 투표 불러오기", description = """ 
        ## [스터디 투표] 내 스터디 > 스터디 > 투표 > 특정 투표 클릭, 로그인한 회원이 참여하는 특정 스터디의 투표를 불러옵니다.
        특정 vote에 대한 항목 및 기본 정보가 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/votes{voteId}")
    public void getVote(@PathVariable Long studyId, @PathVariable Long voteId) {
    }

    @Operation(summary = "[스터디 투표] 투표 생성하기", description = """ 
        ## [스터디 투표] 내 스터디 > 스터디 > 투표 > 작성 버튼 클릭, 로그인한 회원이 참여하는 특정 스터디에서 새로운 투표를 등록합니다.
        스터디에 참여하는 회원이 생성한 투표를 vote에 저장합니다.
        """)
    @PostMapping("/members/{memberId}/studies/{studyId}/votes")
    public void createVote(@PathVariable Long memberId, @PathVariable Long studyId) {
    }

    @Operation(summary = "[스터디 투표] 투표하기", description = """ 
        ## [스터디 투표] 내 스터디 > 스터디 > 투표 > 특정 투표 클릭, 로그인한 회원이 참여하는 스터디에서 특정 항목에 투표합니다.
        member_vote에 투표 정보를 저장합니다.
        """)
    @PostMapping("/members/{memberId}/studies/{studyId}/votes/{voteId}/options/{optionId}")
    public void vote(@PathVariable Long memberId, @PathVariable Long studyId, @PathVariable Long voteId, @PathVariable Long optionId) {
    }

/* ----------------------------- 스터디 갤러리 관련 API ------------------------------------- */

    @Operation(summary = "[스터디 갤러리] 스터디 이미지 목록 불러오기", description = """ 
        ## [스터디 갤러리] 내 스터디 > 스터디 > 갤러리 클릭, 로그인한 회원이 참여하는 스터디의 이미지 목록을 불러옵니다.
        study_post에 존재하는 모든 게시글의 이미지를 최신순으로 반환합니다.
        """)
    @GetMapping("/studies/{studyId}/posts/images")
    public void getAllStudyImages(@PathVariable Long studyId) {
    }

    @Operation(summary = "[스터디 갤러리] 스터디 이미지 불러오기", description = """ 
        ## [스터디 갤러리] 내 스터디 > 스터디 > 갤러리 > 이미지 클릭, 로그인한 회원이 참여하는 스터디의 특정 이미지를 불러옵니다.
        특정 study_post의 image 하나를 반환합니다.
        """)
    @GetMapping("/studies/{studyId}/posts/{studyPostId}/images/{studyPostImageId}")
    public void getStudyImage(@PathVariable Long studyId, @PathVariable Long studyPostId, @PathVariable Long studyPostImageId) {
    }

/* ----------------------------- 스터디 출석체크 관련 API ------------------------------------- */

    @Operation(summary = "[스터디 출석체크] 출석 퀴즈 생성하기", description = """ 
        ## [스터디 출석체크] 내 스터디 > 스터디 > 캘린더 > 출석체크 > 퀴즈 만들기 클릭, 로그인한 회원이 운영하는 스터디에 퀴즈를 생성합니다.
        로그인한 회원이 스터디장인 경우 quiz에 새로운 퀴즈를 생성합니다.
        """)
    @PostMapping("/studies/{studyId}/quizzes")
    public void createAttendanceQuiz(@PathVariable Long studyId) {
    }

    @Operation(summary = "[스터디 출석체크] 출석 체크하기", description = """ 
        ## [스터디 출석체크] 내 스터디 > 스터디 > 캘린더 > 이미지 클릭, 로그인한 회원이 참여하는 스터디에서 오늘의 퀴즈를 풀어 출석을 체크합니다.
        특정 시점의 quiz에 대해 member_attendance 튜플을 추가합니다.
        """)
    @PostMapping("/members/{memberId}/studies/{studyId}/quizzes/{quizId}")
    public void attendantStudy(@PathVariable Long memberId, @PathVariable Long studyId, @PathVariable Long quizId) {
    }

/* ----------------------------- 스터디 회원 신고 관련 API ------------------------------------- */

    @Operation(summary = "[스터디 회원 신고] 스터디원 신고하기", description = """ 
        ## [스터디 회원 신고] 로그인한 회원이 참여하는 스터디의 다른 회원을 신고합니다.
        member_report에 피신고자의 member_id를 포함하여 새로운 튜플을 추가합니다.
        """)
    @PostMapping("/studies/{studyId}/members/{memberId}")
    public void reportStudyMember(@PathVariable Long memberId, @PathVariable Long studyId) {
    }
}
