package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.service.s3.S3ImageService;
import com.example.spot.web.dto.util.response.ImageResponse;
import com.example.spot.web.dto.util.response.ImageResponse.ImageUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/spot")
@Validated
public class UtilController {

    private final S3ImageService s3ImageService;

    @PostMapping(value = "/util/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Tag(name = "이미지 업로드")
    @Operation(summary = "[이미지 업로드] 이미지 업로드", description = """
        ## [이미지 업로드] 이미지를 업로드합니다. 
        이미지를 업로드하면 이미지의 URL과 업로드 시간이 반환됩니다.
        여러 개의 이미지를 업로드할 수 있습니다.
        """)
    public ApiResponse<ImageUploadResponse> uploadImages(
        @RequestPart(value = "images") List<MultipartFile> images
    ) {
        ImageResponse.ImageUploadResponse imageUploadResponse = s3ImageService.uploadImages(images);
        return ApiResponse.onSuccess(SuccessStatus._IMAGE_UPLOADED, imageUploadResponse);
    }

    @GetMapping("/current-env")
    @Tag(name = "배포")
    @Operation(summary = "[배포 관련] 무중단 배포를 위한 현재 환경 확인", description = """
        ## [배포 관련] 현재 서버의 환경을 확인합니다.
        현재 서버의 환경을 확인하여 무중단 배포를 위한 환경을 확인합니다.
        """)
    public String getCurrentEnvironment() {
        return System.getProperty("server.color");
    }


}
