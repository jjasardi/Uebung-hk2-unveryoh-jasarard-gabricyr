package ch.zhaw.pm2.multichat.protocol;

import java.io.Serializable;

/**
 * This class implements {@link Serializable} and represents the data object which is sent.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = -7581180395641596879L;

    private final MessageType type;
    private String sender;
    private final String receiver;
    private final String text;

    /**
     * This constructor creates a {@link Message} obejct.
     *
     * @param type      represents the {@link MessageType}.
     * @param sender    represents the sender of the message.
     * @param receiver  represents the receiver of the message.
     * @param text      represents the text of the message.
     */
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

    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * This enum contains possible message types.
     */
    public enum MessageType {
        CONNECT, CONFIRM, DISCONNECT, INFO, MESSAGE, ERROR
    }
}
