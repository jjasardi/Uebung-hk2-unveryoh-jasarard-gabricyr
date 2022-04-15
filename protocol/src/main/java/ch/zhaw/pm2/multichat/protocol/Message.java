package ch.zhaw.pm2.multichat.protocol;

import java.io.Serializable;

/**
 * This class implements {@link Serializable} and represents the data object which is sent.
 */
public class Message implements Serializable {
    private String sender;
    private final String receiver;
    private final MessageType type;
    private final String payload;

    /**
     * This constructor creates a {@link Message} obejct.
     *
     * @param type      represents the {@link MessageType}.
     * @param sender    represents the sender of the message.
     * @param receiver  represents the receiver of the message.
     * @param payload   represents the text of the message.
     */
    public Message(String sender, String receiver, MessageType type, String payload) {
        this.sender = sender;
        this.receiver = receiver;
        this.type = type;
        this.payload = payload;
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

    public String getPayload() {
        return payload;
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
