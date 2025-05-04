package com.oriya_s.model;

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

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}