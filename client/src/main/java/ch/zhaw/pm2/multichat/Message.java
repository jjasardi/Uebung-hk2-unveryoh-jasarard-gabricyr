package ch.zhaw.pm2.multichat;

public class Message {
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

    // TODO move enum in new config file
    public enum MessageType {
        INFO, MESSAGE, ERROR;
    }
}
