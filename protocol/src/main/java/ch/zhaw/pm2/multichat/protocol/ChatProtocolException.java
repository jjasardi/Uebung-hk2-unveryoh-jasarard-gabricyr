package ch.zhaw.pm2.multichat.protocol;

/**
 * This exception class is used when an illegal term is used in the chat protocol.
 */
public class ChatProtocolException extends Exception {

    private static final long serialVersionUID = -4692082749226545693L;

    /**
     * This constructor creates a new {@link ChatProtocolException} object.
     *
     * @param message  string which contains the error message.
     */
    public ChatProtocolException(String message) {
        super(message);
    }

    public ChatProtocolException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChatProtocolException(Throwable cause) {
        super(cause);
    }

}
