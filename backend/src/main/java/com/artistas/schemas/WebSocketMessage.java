package com.artistas.schemas;

import java.time.Instant;

public class WebSocketMessage<T> {

    public String type;
    public Instant timestamp;
    public T data;

    public static <T> WebSocketMessage<T> of(String type, T data) {
        WebSocketMessage<T> message = new WebSocketMessage<>();
        message.type = type;
        message.timestamp = Instant.now();
        message.data = data;
        return message;
    }
}
