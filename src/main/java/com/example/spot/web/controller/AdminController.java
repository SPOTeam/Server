package com.example.spot.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin", description = "Admin API")
@RestController
@RequestMapping("/spot/admin")
public class AdminController {

    @GetMapping("/reports/studies/{studyId}")
    public void getReportInStudy(@PathVariable long studyId) {
        // 메소드 구현
    }

    @GetMapping("/reports/studies")
    public void getAllReportsInStudies() {
        // 메소드 구현
    }

    @GetMapping("/reports/posts/{postId}")
    public void getReportInPost(@PathVariable long postId) {
        // 메소드 구현
    }

    @GetMapping("/reports/posts")
    public void getAllReportsInPosts() {
        // 메소드 구현
    }


}
