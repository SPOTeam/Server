package com.example.spot.web.dto.member.naver;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class NaverMember {

    @Getter
    @RequiredArgsConstructor
    public static class RequestDTO {
        private final String id;
        private final String name;
        private final String email;
        private final String profileImage;
        private final String gender;
        private final String birthDay;
        private final String birthYear;
    }

    @Getter
    public static class ResponseDTO {

        private final String resultCode;
        private final String message;
        private final ProfileResponse response;

        @JsonCreator
        public ResponseDTO(
                @JsonProperty("resultcode") String resultCode,
                @JsonProperty("message") String message,
                @JsonProperty("response") ProfileResponse response) {
            this.resultCode = resultCode;
            this.message = message;
            this.response = response;
        }
    }

    @Getter
    public static class ProfileResponse {

        private final String id;
        private final String name;
        private final String nickname;
        private final String email;
        private final String profileImage;
        private final String gender;
        private final String birthDay;
        private final String birthYear;

        @JsonCreator
        public ProfileResponse(
                @JsonProperty("id") String id,
                @JsonProperty("name") String name,
                @JsonProperty("nickname") String nickname,
                @JsonProperty("email") String email,
                @JsonProperty("profile_image") String profileImage,
                @JsonProperty("gender") String gender,
                @JsonProperty("birthday") String birthDay,
                @JsonProperty("birthyear") String birthYear) {
            this.id = id;
            this.name = name;
            this.nickname = nickname;
            this.email = email;
            this.profileImage = profileImage;
            this.gender = gender;
            this.birthDay = birthDay;
            this.birthYear = birthYear;
        }
    }


}
