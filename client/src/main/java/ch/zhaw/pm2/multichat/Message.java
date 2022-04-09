package ch.zhaw.pm2.multichat;

public class Message {
    private MessageType type;
    private String sender;
    private String receiver;
    private String text;

    public Message(MessageType type, String sender, String receiver, String text) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
    }

    // TODO move enum in new config file
    public enum MessageType {
        INFO, MESSAGE, ERROR;
    }
}
