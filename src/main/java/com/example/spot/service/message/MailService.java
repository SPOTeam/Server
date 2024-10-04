package com.example.spot.service.message;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface MailService {

    void sendMail(HttpServletRequest request, HttpServletResponse response, String email, String code);
}
