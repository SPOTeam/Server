package com.example.spot.web.dto.user;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "kakoApiClient", url="https://kapi.kakao.com")
public interface KakaoApiClient {
}
