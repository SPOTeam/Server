package com.example.spot.web.dto.memberstudy.response;

import com.example.spot.domain.Member;
import com.example.spot.domain.mapping.MemberVote;
import com.example.spot.domain.study.Option;
import com.example.spot.domain.study.Vote;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class StudyVoteResponseDTO {

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class VotePreviewDTO {

        private final Long voteId;
        private final String title;

        public static VotePreviewDTO toDTO(Vote vote) {
            return VotePreviewDTO.builder()
                    .voteId(vote.getId())
                    .title(vote.getTitle())
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class VotedOptionDTO {

        private final Long voteId;
        private final Long memberId;
        private final List<OptionDTO> votedOptions;

        public static VotedOptionDTO toDTO(Vote vote, Member member, List<MemberVote> memberVotes) {
            return VotedOptionDTO.builder()
                    .voteId(vote.getId())
                    .memberId(member.getId())
                    .votedOptions(memberVotes.stream()
                            .map(memberVote -> OptionDTO.toDTO(memberVote.getOption()))
                            .toList())
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class VoteListDTO {

        private final Long studyId;
        private final List<VoteInfoDTO> votesInProgress;
        private final List<VoteInfoDTO> votesInCompletion;

        public static VoteListDTO toDTO(Long studyId, List<VoteInfoDTO> votesInProgress, List<VoteInfoDTO> votesInCompletion) {
            return VoteListDTO.builder()
                    .studyId(studyId)
                    .votesInProgress(votesInProgress)
                    .votesInCompletion(votesInCompletion)
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class VoteInfoDTO {

        private final Long voteId;
        private final String title;
        private final LocalDateTime finishedAt;
        private final Boolean isParticipated;

        public static VoteInfoDTO toDTO(Vote vote, Boolean isParticipated) {
            return VoteInfoDTO.builder()
                    .voteId(vote.getId())
                    .title(vote.getTitle())
                    .finishedAt(vote.getFinishedAt())
                    .isParticipated(isParticipated)
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class VoteDTO {

        private final Long voteId;
        private final MemberDTO creator;
        private final String title;
        private final List<OptionDTO> options;
        private final Boolean isMultipleChoice;
        private final LocalDateTime finishedAt;
        private final Boolean isParticipated;

        public static VoteDTO toDTO(Vote vote, Member member) {
            return VoteDTO.builder()
                    .voteId(vote.getId())
                    .creator(MemberDTO.toDTO(vote.getMember()))
                    .title(vote.getTitle())
                    .options(vote.getOptions().stream()
                            .map(OptionDTO::toDTO)
                            .toList())
                    .isMultipleChoice(vote.getIsMultipleChoice())
                    .finishedAt(vote.getFinishedAt())
                    .isParticipated(vote.getOptions().stream()
                            .flatMap(option -> option.getMemberVotes().stream())
                            .anyMatch(memberVote -> memberVote.getMember().equals(member)))
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class CompletedVoteDTO {

        private final Long voteId;
        private final MemberDTO creator;
        private final String title;
        private final List<VotedOptionCountDTO> optionCounts;
        private final int totalParticipants;
        private final LocalDateTime finishedAt;

        public static CompletedVoteDTO toDTO(Vote vote) {
            return CompletedVoteDTO.builder()
                    .voteId(vote.getId())
                    .creator(MemberDTO.toDTO(vote.getMember()))
                    .title(vote.getTitle())
                    .optionCounts(vote.getOptions().stream()
                            .map(VotedOptionCountDTO::toDTO)
                            .toList())
                    .totalParticipants(vote.getOptions().stream()
                            .map(VotedOptionCountDTO::toDTO)
                            .mapToInt(VotedOptionCountDTO::getCount)
                            .sum())
                    .finishedAt(vote.getFinishedAt())
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class CompletedVoteDetailDTO {

        private final Long voteId;
        private final String title;
        private final List<OptionVoterDTO> optionVoters;

        public static CompletedVoteDetailDTO toDTO(Vote vote) {
            return CompletedVoteDetailDTO.builder()
                    .voteId(vote.getId())
                    .title(vote.getTitle())
                    .optionVoters(vote.getOptions().stream()
                            .map(OptionVoterDTO::toDTO)
                            .toList())
                    .build();
        }
    }

/* ----------------------------- Private ------------------------------------- */

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    private static class VotedOptionCountDTO {

        private final Long optionId;
        private final String content;
        private final int count;

        public static VotedOptionCountDTO toDTO(Option option) {
            return VotedOptionCountDTO.builder()
                    .optionId(option.getId())
                    .content(option.getContent())
                    .count(option.getMemberVotes().size())
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    private static class OptionVoterDTO {

        private final Long optionId;
        private final String content;
        private final int count;
        private final List<MemberDTO> voters;

        public static OptionVoterDTO toDTO(Option option) {
            return OptionVoterDTO.builder()
                    .optionId(option.getId())
                    .content(option.getContent())
                    .count(option.getMemberVotes().size())
                    .voters(option.getMemberVotes().stream()
                            .map(memberVote -> MemberDTO.toDTO(memberVote.getMember()))
                            .toList())
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    private static class OptionDTO {

        private final Long optionId;
        private final String content;

        public static OptionDTO toDTO(Option option) {
            return OptionDTO.builder()
                    .optionId(option.getId())
                    .content(option.getContent())
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    private static class MemberDTO {

        private final Long memberId;
        private final String name;
        private final String profileImage;

        public static MemberDTO toDTO(Member member) {
            return MemberDTO.builder()
                    .memberId(member.getId())
                    .name(member.getName())
                    .profileImage(member.getProfileImage())
                    .build();
        }
    }


}
