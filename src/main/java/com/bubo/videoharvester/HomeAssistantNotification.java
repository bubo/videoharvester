package com.bubo.videoharvester;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@ToString
@Getter
public class HomeAssistantNotification {
    private String message;
    private String title;
    private Data data;

    public HomeAssistantNotification(String message, String title, Data data) {
        this.message = message;
        this.title = title;
        this.data = data;
    }

    @Builder
    @ToString
    @Getter
    public static class Data {
        private int ttl;
        private String priority;

        public Data(int ttl, String priority) {
            this.ttl = ttl;
            this.priority = priority;
        }
    }
}
