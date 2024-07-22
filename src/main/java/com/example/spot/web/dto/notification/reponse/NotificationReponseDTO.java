package com.example.spot.web.dto.notification.reponse;

import com.example.spot.domain.enums.NotifyType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationReponseDTO {

    private long id;
    private long userId;
    private NotifyType notifyType;
    private String status;
    private String date;
}
