package ch.zhaw.pm2.multichat;

import java.io.Serializable;

public class Message implements Serializable {
    private final MessageType type;
    private final String sender;
    private final String receiver;
    private final String text;

    public Message(MessageType type, String sender, String receiver, String text) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
    }

    public MessageType getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getText() {
        return text;
    }

    public enum MessageType {
        INFO, MESSAGE, ERROR;
    }
}
