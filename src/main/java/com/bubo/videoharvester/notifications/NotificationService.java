package com.bubo.videoharvester.notifications;

public interface NotificationService {

    void sendNotification(String message);

    void sendNotification(String format, Object... arguments);
}