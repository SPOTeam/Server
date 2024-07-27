package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.service.memberstudy.MemberStudyCommandService;
import com.example.spot.service.memberstudy.MemberStudyQueryService;
import com.example.spot.validation.annotation.ExistMember;
import com.example.spot.validation.annotation.ExistStudy;
import com.example.spot.web.dto.memberstudy.request.StudyQuizRequestDTO;
import com.example.spot.web.dto.memberstudy.response.StudyQuizResponseDTO;
import com.example.spot.web.dto.memberstudy.response.StudyTerminationResponseDTO;
import com.example.spot.web.dto.memberstudy.response.StudyWithdrawalResponseDTO;
import com.example.spot.web.dto.study.response.StudyApplyResponseDTO;
import com.example.spot.web.dto.study.response.StudyMemberResponseDTO;
import com.example.spot.web.dto.study.response.StudyPostResponseDTO;
import com.example.spot.web.dto.study.response.StudyScheduleResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "MemberStudy", description = "MemberStudy API(내 스터디 관련 API)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/spot")
@Validated
public class MemberStudyController {

    private final MemberStudyQueryService memberStudyQueryService;
    private final MemberStudyCommandService memberStudyCommandService;

/* ----------------------------- 진행중인 스터디 관련 API ------------------------------------- */


    @Operation(summary = "[진행중인 스터디] 스터디 탈퇴하기", description = """ 
        ## [진행중인 스터디] 마이페이지 > 진행중 > 진행중인 스터디의 메뉴 클릭, 로그인한 회원이 현재 진행중인 스터디에서 탈퇴합니다.
        로그인한 회원이 참여하는 특정 스터디에 대해 member_study 튜플을 삭제합니다.
        """)
    @DeleteMapping("/members/{memberId}/studies/{studyId}/withdrawal")
    public ApiResponse<StudyWithdrawalResponseDTO.WithdrawalDTO> withdrawFromStudy(@PathVariable Long memberId, @PathVariable Long studyId) {
        StudyWithdrawalResponseDTO.WithdrawalDTO withdrawalDTO = memberStudyCommandService.withdrawFromStudy(memberId, studyId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_MEMBER_DELETED, withdrawalDTO);
    }

    @Operation(summary = "[진행중인 스터디] 스터디 끝내기", description = """ 
        ## [진행중인 스터디] 마이페이지 > 진행중 > 진행중인 스터디의 메뉴 클릭, 로그인한 회원이 운영중인 스터디를 끝냅니다.
        로그인한 회원이 운영하는 특정 스터디에 대해 study status OFF로 전환합니다.
        """)
    @PatchMapping("/studies/{studyId}/termination")
    public ApiResponse<StudyTerminationResponseDTO.TerminationDTO> terminateStudy(@PathVariable Long studyId) {
        StudyTerminationResponseDTO.TerminationDTO terminationDTO = memberStudyCommandService.terminateStudy(studyId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_TERMINATED, terminationDTO);
    }

//    @Operation(summary = "[데모 데이 이후 개발 -> 진행중인 스터디] 스터디 정보 수정하기", description = """
//        ## [진행중인 스터디] 마이페이지 > 진행중 > 진행중인 스터디의 메뉴 클릭, 로그인한 회원이 운영중인 스터디의 정보를 수정합니다.
//        로그인한 회원이 운영하는 특정 스터디에 대해 study 정보를 수정합니다.
//        """)
//    @PatchMapping("/studies/{studyId}")
//    public void updateStudy(@PathVariable @ExistStudy Long studyId) {
//    }

/* ----------------------------- 모집중인 스터디 관련 API ------------------------------------- */


    @Operation(summary = "[모집중인 스터디] 스터디별 신청 회원 목록 불러오기", description = """ 
        ## [모집중인 스터디] 마이페이지 > 모집중 > 스터디 클릭, 로그인한 회원이 모집중인 스터디에 신청한 회원 목록을 불러옵니다.
        로그인한 회원이 모집중인 특정 스터디에 대해 member_study의 application_status가 APPLIED인 회원 목록이 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/applicants")
    @Parameter(name = "studyId", description = "모집중인 스터디의 ID를 입력 받습니다.", required = true)
    public ApiResponse<StudyMemberResponseDTO> getAllApplicants(@PathVariable @ExistStudy Long studyId) {
        return ApiResponse.onSuccess(SuccessStatus._STUDY_APPLICANT_FOUND,
            memberStudyQueryService.findStudyApplicants(studyId));
    }

    @Operation(summary = "[모집중인 스터디] 스터디 신청 정보(이름, 자기소개) 불러오기", description = """ 
        ## [모집중인 스터디] 마이페이지 > 모집중 > 스터디 > 신청 회원 클릭, 로그인한 회원이 모집중인 스터디에 신청한 회원의 정보를 불러옵니다.
        로그인한 회원이 모집중인 특정 스터디에 신청한 회원의 정보(member.name & member_study.introduction)가 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/applicants/{applicantId}")
    @Parameter(name = "studyId", description = "모집중인 스터디의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "applicantId", description = "신청자의 ID를 입력 받습니다.", required = true)
    public ApiResponse<StudyMemberResponseDTO.StudyApplyMemberDTO> getApplicantInfo(
        @PathVariable @ExistStudy Long studyId,
        @PathVariable @ExistMember Long applicantId) {
        return ApiResponse.onSuccess(SuccessStatus._STUDY_APPLICANT_FOUND,
            memberStudyQueryService.findStudyApplication(studyId, applicantId));
    }

    @Operation(summary = "[모집중인 스터디] 스터디 신청 처리하기", description = """ 
        ## [모집중인 스터디] 마이페이지 > 모집중 > 스터디 > 신청 회원 > 거절 클릭, 로그인한 회원이 모집중인 스터디에 신청한 회원을 처리합니다.
        isAccept가 true인 경우 member_study에서 application_status를 APPROVE로 수정합니다.
        isAccept가 false인 경우 member_study에서 application_status를 REJECTED로 수정합니다.
        스터디 신청 처리 결과를 응답으로 반환합니다. 
        """)
    @PostMapping("/studies/{studyId}/applicants/{applicantId}")
    @Parameter(name = "studyId", description = "모집중인 스터디의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "applicantId", description = "신청자의 ID를 입력 받습니다.", required = true)
    public ApiResponse<StudyApplyResponseDTO> rejectApplicant(
        @PathVariable @ExistStudy Long studyId,
        @PathVariable @ExistMember Long applicantId,
        @RequestParam boolean isAccept) {
        return ApiResponse.onSuccess(SuccessStatus._STUDY_APPLICANT_UPDATED,
            memberStudyCommandService.acceptAndRejectStudyApply(applicantId, studyId, isAccept));
    }


/* ----------------------------- 스터디 상세 정보 관련 API ------------------------------------- */

    @Operation(summary = "[스터디 상세 정보] 스터디 최근 공지 1개 불러오기", description = """ 
        ## [스터디 상세 정보] 내 스터디 > 스터디 클릭, 로그인한 회원이 참여하는 특정 스터디의 최근 공지 1개를 불러옵니다.
        study_post의 announced_at이 가장 최근인 공지 1개가 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/announce")
    public ApiResponse<StudyPostResponseDTO> getRecentAnnouncement(@PathVariable @ExistStudy Long studyId) {
        StudyPostResponseDTO studyPostResponseDTO = memberStudyQueryService.findStudyAnnouncementPost(studyId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_FOUND, studyPostResponseDTO);


    }

    @Operation(summary = "[스터디 상세 정보] 다가오는 모임 목록 불러오기", description = """ 
        ## [스터디 상세 정보] 내 스터디 > 스터디 클릭, 로그인한 회원이 참여하는 특정 스터디의 다가오는 모임 목록을 페이징 조회 합니다.
        현재 시점 이후에 진행되는 모임 일정의 목록을 schedule에서 반환합니다.
        """)
    @GetMapping("/studies/{studyId}/upcoming-schedules")
    public ApiResponse<StudyScheduleResponseDTO> getUpcomingSchedules(
        @PathVariable @ExistStudy Long studyId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "1") int size){
        StudyScheduleResponseDTO studyScheduleResponseDTO = memberStudyQueryService.findStudySchedule(studyId, PageRequest.of(page, size));
        return ApiResponse.onSuccess(SuccessStatus._STUDY_SCHEDULE_FOUND, studyScheduleResponseDTO);
    }

    @Operation(summary = "[스터디 상세 정보] 스터디에 참여하는 회원 목록 불러오기", description = """ 
        ## [스터디 상세 정보] 로그인한 회원이 참여하는 특정 스터디의 회원 목록을 전체 합니다.
        member_study에서 application_status=APPROVED인 회원의 목록(이름, 프로필 사진 포함)이 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/members")
    public ApiResponse<StudyMemberResponseDTO> getStudyMembers(
        @PathVariable @ExistStudy Long studyId){
        StudyMemberResponseDTO studyMemberResponseDTO = memberStudyQueryService.findStudyMembers(studyId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_MEMBER_FOUND, studyMemberResponseDTO);
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
    public ApiResponse<StudyQuizResponseDTO.QuizDTO> createAttendanceQuiz(@PathVariable Long studyId, @RequestBody StudyQuizRequestDTO.QuizDTO quizRequestDTO) {
        StudyQuizResponseDTO.QuizDTO quizResponseDTO = memberStudyCommandService.createAttendanceQuiz(studyId, quizRequestDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_QUIZ_CREATED, quizResponseDTO);
    }

    @Operation(summary = "[스터디 출석체크] 출석 체크하기", description = """ 
        ## [스터디 출석체크] 내 스터디 > 스터디 > 캘린더 > 이미지 클릭, 로그인한 회원이 참여하는 스터디에서 오늘의 퀴즈를 풀어 출석을 체크합니다.
        특정 시점의 quiz에 대해 member_attendance 튜플을 추가합니다.
        """)
    @PostMapping("/studies/{studyId}/quizzes/{quizId}")
    public ApiResponse<StudyQuizResponseDTO.AttendanceDTO> attendantStudy(@PathVariable Long studyId, @PathVariable Long quizId,
                                        @RequestBody StudyQuizRequestDTO.AttendanceDTO attendanceRequestDTO) {
        StudyQuizResponseDTO.AttendanceDTO attendanceResponseDTO = memberStudyCommandService.attendantStudy(studyId, quizId, attendanceRequestDTO);
        if (attendanceResponseDTO.getIsCorrect()) {
            return ApiResponse.onSuccess(SuccessStatus._STUDY_ATTENDANCE_CREATED_CORRECT_ANSWER, attendanceResponseDTO);
        } else {
            return ApiResponse.onSuccess(SuccessStatus._STUDY_ATTENDANCE_CREATED_WRONG_ANSWER, attendanceResponseDTO);
        }
    }

    @Operation(summary = "[스터디 출석체크] 출석 삭제하기", description = """ 
        ## [스터디 출석체크] 회원의 출석 정보를 삭제합니다. (화면 X)
        PathVariable을 통해 전달받은 정보를 바탕으로 출석 정보를 삭제합니다.
        """)
    @DeleteMapping("/studies/{studyId}/quizzes/{quizId}/members/{memberId}")
    public void deleteAttendance() {}

    @Operation(summary = "[스터디 출석체크] 출석 퀴즈 삭제하기", description = """ 
        ## [스터디 출석체크] 기한이 지난 출석 퀴즈를 삭제합니다. (화면 X)
        PathVariable을 통해 전달받은 정보를 바탕으로 출석 퀴즈를 삭제합니다.
        """)
    @DeleteMapping("/studies/{studyId}/quizzes/{quizId}")
    public void deleteAttendanceQuiz() {}



/* ----------------------------- 스터디 회원 신고 관련 API ------------------------------------- */

    @Operation(summary = "[스터디 회원 신고] 스터디원 신고하기", description = """ 
        ## [스터디 회원 신고] 로그인한 회원이 참여하는 스터디의 다른 회원을 신고합니다.
        member_report에 피신고자의 member_id를 포함하여 새로운 튜플을 추가합니다.
        """)
    @PostMapping("/studies/{studyId}/members/{memberId}")
    public void reportStudyMember(@PathVariable Long memberId, @PathVariable Long studyId) {
    }
}
