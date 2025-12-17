package com.dasi.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public abstract class BaseEvent<T> {

    public abstract EventMessage<T> buildEventMessage(T data);

    public abstract String getTopic();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventMessage<T> {
        private String messageId;
        private String time;
        private T data;
    }

}
