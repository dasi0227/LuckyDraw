package com.dasi.domain.common;

public interface IEventPublish {

    void publish(String topic, String message);

}
