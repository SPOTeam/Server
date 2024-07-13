package com.example.controller;


import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    public String test() {
        return "Hello World!";
    }

}
