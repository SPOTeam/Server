package com.example.spot.repository.verification;

public interface VerificationCodeRepository {

    public void addVerificationCode(String phone, String code);

    public String getVerificationCode(String phone);

    public String addTempToken(String tempToken);
}
