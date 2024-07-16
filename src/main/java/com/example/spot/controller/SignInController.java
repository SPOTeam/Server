package com.example.spot.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "SignIn", description = "SignIn API")
@RestController
@RequestMapping("/api")
public class SignInController {
}
