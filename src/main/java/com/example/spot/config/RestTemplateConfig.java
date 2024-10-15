package com.example.spot.config;

import java.nio.charset.Charset;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    //HTTP get,post 요청을 날릴때 일정한 형식에 맞춰주는 template

    /**
     * RestTemplate(HTTP get,post 요청을 날릴때 일정한 형식에 맞춰주는 template)을 Bean으로 등록합니다.
     * @param restTemplateBuilder
     * @return RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
            // 요청과 응답을 로깅하기 위해 BufferingClientHttpRequestFactory를 사용합니다.
            .requestFactory(() -> new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
            // 한글 깨짐 방지를 위해 UTF-8로 인코딩합니다.
            .additionalMessageConverters(new StringHttpMessageConverter(Charset.forName("UTF-8")))
            .build();
    }
}
