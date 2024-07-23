package com.example.spot.web.controller;


import com.example.spot.validation.annotation.ExistMember;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping("/")
    public String test() {
        return "Hello World!";
    }

    @GetMapping("/member/{memberId}")
    public String test2(@PathVariable @ExistMember Long memberId){

        return "Hello World!";
    }
}
