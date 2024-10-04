package com.example.spot.service.message;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private DefaultMessageService defaultMessageService;

    @Value("${coolsms.api.key}")
    private String smsApiKey;

    @Value("${coolsms.api.secret}")
    private String smsSecretKey;

    @Value("${coolsms.api.sender}")
    private String sender;

    @PostConstruct
    private void init() {
        this.defaultMessageService = NurigoApp.INSTANCE.initialize(smsApiKey, smsSecretKey, "https://api.coolsms.co.kr");
    }

    @Override
    public SingleMessageSentResponse sendMessage(String receiver, String code) {
        Message message = new Message();
        message.setFrom(sender);
        message.setTo(receiver);
        message.setText("[SPOT] 인증번호는 " + code + " 입니다.");
        return this.defaultMessageService.sendOne(new SingleMessageSendingRequest(message));
    }
}
