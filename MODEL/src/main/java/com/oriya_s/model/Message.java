package com.oriya_s.model;

import java.util.Objects;

public class Message {
    private String senderID;
    private String text;
    private long timestamp;

    public Message() {}

    public Message(String senderID, String text, long timestamp) {
        this.senderID = senderID;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getSenderID() {
        return senderID;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message message = (Message) o;
        return timestamp == message.timestamp && Objects.equals(senderID, message.senderID) && Objects.equals(text, message.text);
    }
}