package com.example.spot.repository.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spot.domain.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

}
