package ch.zhaw.pm2.multichat.protocol;

/**
 * This class contains the predefined {@link Config.State}s to handle the connection and the communication.
 */
public class Config {

    /**
     * This enum contains possible {@link Config.State}s.
     */
    public enum State {
        NEW, CONFIRM_CONNECT, CONNECTED, CONFIRM_DISCONNECT, DISCONNECTED;
    }

    public static final String USER_NONE = "";
    public static final String USER_ALL = "*";
}
