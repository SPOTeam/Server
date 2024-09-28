package com.example.spot.service.message;

import net.nurigo.sdk.message.response.SingleMessageSentResponse;

public interface MessageService {
    SingleMessageSentResponse sendMessage(String receiver, String code);
}
