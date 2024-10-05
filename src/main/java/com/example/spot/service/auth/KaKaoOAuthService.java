package com.example.spot.service.auth;

import com.example.spot.web.dto.member.kakao.KaKaoOAuthToken;
import com.example.spot.web.dto.member.kakao.KaKaoOAuthToken.KaKaoOAuthTokenDTO;
import com.example.spot.web.dto.member.kakao.KaKaoUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class KaKaoOAuthService {

    @Value("${spring.OAuth2.kakao.url}")
    private String KAKAO_SNS_URL;

    @Value("${spring.OAuth2.kakao.client-id}")
    private String KAKAO_SNS_CLIENT_ID;

    @Value("${spring.OAuth2.kakao.callback-login-url}")
    private String KAKAO_SNS_CALLBACK_LOGIN_URL;


    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    /**
     * 카카오 로그인 요청 URL을 생성합니다.
     * @return 카카오 로그인 요청 URL
     */
    public String getOauthRedirectURL() {
        // 카카오 로그인 요청 URL 생성
        Map<String, Object> params = new HashMap<>();
        params.put("client_id", KAKAO_SNS_CLIENT_ID);
        params.put("redirect_uri", KAKAO_SNS_CALLBACK_LOGIN_URL);
        params.put("response_type", "code");

        // URL 파라미터 생성
        String parameterString = params.entrySet().stream()
            .map(x -> x.getKey() + "=" + x.getValue())
            .collect(Collectors.joining("&"));

        // 카카오 로그인 요청 URL
        return KAKAO_SNS_URL + "?" + parameterString;
    }

    /**
     * 카카오 로그인 요청을 합니다.
     * @param code 카카오 로그인 요청 시 발급받은 코드
     * @return 카카오 로그인 요청 결과
     */
    public ResponseEntity<String> requestAccessToken(String code) {
        String KAKAO_TOKEN_REQUEST_URL = "https://kauth.kakao.com/oauth/token";
        // 카카오 로그인 요청
        RestTemplate restTemplate = new RestTemplate();
        // 요청 파라미터
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", KAKAO_SNS_CLIENT_ID);
        params.add("redirect_uri", KAKAO_SNS_CALLBACK_LOGIN_URL);
        params.add("code", code);

        // 헤더에 Content-Type 추가
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity(params, headers);

        // 카카오 로그인 요청
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(KAKAO_TOKEN_REQUEST_URL, request, String.class);

        // 요청 결과 반환
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity;
        }

        // 요청 실패 시 null 반환
        return null;
    }

    /**
     * 카카오 로그인 요청 결과에서 AccessToken을 추출합니다.
     * @param response 카카오 로그인 요청 결과
     * @return 카카오 로그인 성공 시 반환되는 AccessToken
     * @throws JsonProcessingException JSON 파싱 예외
     */
    public KaKaoOAuthTokenDTO getAccessToken(ResponseEntity<String> response)
        throws JsonProcessingException {

        // 카카오 로그인 요청 결과에서 AccessToken 추출
        KaKaoOAuthToken.KaKaoOAuthTokenDTO kaKaoOAuthTokenDTO= objectMapper.readValue(response.getBody(),
            KaKaoOAuthTokenDTO.class);
        return kaKaoOAuthTokenDTO;
    }


    /**
     * 카카오 로그인 시 발급된 accessToken을 이용하여 사용자 정보를 요청합니다.
     * @param accessToken 카카오 로그인 시 발급된 accessToken
     * @return 사용자 정보 요청 결과
     */
    public ResponseEntity<String> requestUserInfo(String accessToken) {
        log.info("accessToken = {}", accessToken);
        // 사용자 정보 요청 URL
        String KAKAO_USER_INFO_REQUEST_URL = "https://kapi.kakao.com/v2/user/me";

        // 헤더에 accessToken 추가
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        // HttpEntity에 헤더 추가
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity(headers);

        // 사용자 정보 요청
        ResponseEntity<String> responseEntity = restTemplate.exchange(KAKAO_USER_INFO_REQUEST_URL, HttpMethod.GET, request, String.class);

        return responseEntity;
    }

    /**
     * 카카오 로그인 요청 결과에서 사용자 정보를 추출합니다.
     * @param userInfoRes 카카오 로그인 요청 결과
     * @return 카카오 로그인 성공 시 반환되는 사용자 정보
     * @throws JsonProcessingException JSON 파싱 예외
     */
    public KaKaoUser getUserInfo(ResponseEntity<String> userInfoRes)
        throws JsonProcessingException {
        // 사용자 정보 파싱
        KaKaoUser kaKaoUser = objectMapper.readValue(userInfoRes.getBody(), KaKaoUser.class);
        return kaKaoUser;
    }


}
