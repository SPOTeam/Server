package com.example.spot.service.auth;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.web.dto.member.naver.NaverCallback;
import com.example.spot.web.dto.member.naver.NaverMember;
import com.example.spot.web.dto.member.naver.NaverOAuthToken;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class NaverOAuthService {

    @Value("${spring.OAuth2.naver.callback-url}")
    private String NAVER_CALL_BACK_URL;

    @Value("${spring.OAuth2.naver.client-id}")
    private String NAVER_CLIENT_ID;

    @Value("${spring.OAuth2.naver.client-secret}")
    private String NAVER_CLIENT_SECRET;

    @Value("${spring.OAuth2.naver.csrf-token}")
    private String CSRF_TOKEN;

    /**
     * 네이버 로그인 인증 요청 URL을 생성하는 메서드입니다.
     * 네이버에서 발급받은 client id와 callback url을 쿼리로 포함하여 String 타입의 URL을 반환합니다.
     * @return NAVER_OAUTH_URL
     */
    public String getNaverAuthorizeUrl() {
        String NAVER_OAUTH_URL = "https://nid.naver.com/oauth2.0/authorize";
        return UriComponentsBuilder.fromHttpUrl(NAVER_OAUTH_URL)
                .queryParam("response_type", "code")
                .queryParam("client_id", NAVER_CLIENT_ID)
                .queryParam("redirect_uri", URLEncoder.encode(NAVER_CALL_BACK_URL, StandardCharsets.UTF_8))
                .queryParam("state", URLEncoder.encode(CSRF_TOKEN, StandardCharsets.UTF_8))
                .build()
                .toUriString();
    }

    /**
     * 네이버 액세스 토큰을 발급하고 해당 액세스 토큰을 통해 네이버 프로필을 조회하는 메서드입니다.
     * 내부적으로 issueNaverAccessToken, getNaverProfile 메서드를 수행합니다.
     * @param request : HttpServlet Request
     * @param response : HttpServlet Response
     * @param naverCallback : Callback 함수 성공시 반환되는 요소(code, state, error, error_description)
     * @return 네이버 프로필 정보
     */
    public NaverMember.ResponseDTO getNaverMember(HttpServletRequest request, HttpServletResponse response, NaverCallback naverCallback) throws JsonProcessingException {

        // 네이버 액세스 토큰 발급
        String accessToken = issueNaverAccessToken(naverCallback.getCode());
        ObjectMapper mapper = new ObjectMapper();
        NaverOAuthToken.NaverTokenIssuanceDTO naverTokenIssuanceDTO
                = mapper.readValue(accessToken, NaverOAuthToken.NaverTokenIssuanceDTO.class);

        // 네이버 프로필 반환
        String naverMember = getNaverProfile(naverTokenIssuanceDTO);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(naverMember, NaverMember.ResponseDTO.class);
    }

    /**
     * 네이버 액세스 토큰을 발급하는 메서드입니다.
     * @param authorizationCode : Callback 함수로부터 반환된 authorizationCode
     * @return String 토큰 정보
     *         (access_token, refresh_token, token_type, expires_in, error, error_description)
     */
    private String issueNaverAccessToken(String authorizationCode) {

        String NAVER_ACCESS_TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
        String urlString = UriComponentsBuilder.fromHttpUrl(NAVER_ACCESS_TOKEN_URL)
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", NAVER_CLIENT_ID)
                .queryParam("client_secret", NAVER_CLIENT_SECRET)
                .queryParam("code", authorizationCode)
                .queryParam("state", URLEncoder.encode(CSRF_TOKEN,StandardCharsets.UTF_8))
                .build()
                .toUriString();
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            return extractResponse(con);
        } catch (Exception e) {
            throw new MemberHandler(ErrorStatus._NAVER_ACCESS_TOKEN_ISSUANCE_FAILED);
        }
    }

    /**
     * 토큰 정보를 파라미터로 받아 네이버 프로필을 조회하는 메서드입니다.
     * @param naverTokenIssuanceDTO : 토큰 객체 (access_token, refresh_token, token_type, expires_in, error, error_description)
     * @return String 프로필 정보
     */
    private String getNaverProfile(NaverOAuthToken.NaverTokenIssuanceDTO naverTokenIssuanceDTO) {

        String NAVER_PROFILE_URL = "https://openapi.naver.com/v1/nid/me";
        String accessToken = naverTokenIssuanceDTO.getAccessToken();
        String tokenType = naverTokenIssuanceDTO.getTokenType();

        try {
            URL url = new URL(NAVER_PROFILE_URL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", tokenType + " " + accessToken);

            return extractResponse(con);
        } catch (Exception e) {
            throw new MemberHandler(ErrorStatus._UNABLE_TO_RETRIEVE_NAVER_PROFILE);
        }
    }

    /**
     * HTTP 응답을 버퍼를 통해 읽어오는 메서드입니다.
     * @param con : HttpURLConnection (HTTP 연결 정보)
     * @return String Response
     */
    private static String extractResponse(HttpURLConnection con) throws IOException {

        int responseCode = con.getResponseCode();
        BufferedReader br;

        if (responseCode == HttpURLConnection.HTTP_OK) {
            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {
            br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }

        String line;
        StringBuilder response = new StringBuilder();
        while ((line = br.readLine()) != null) {
            response.append(line);
        }

        br.close();
        return response.toString();
    }


}
