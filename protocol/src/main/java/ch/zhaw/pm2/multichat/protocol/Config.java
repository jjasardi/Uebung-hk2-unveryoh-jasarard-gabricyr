package ch.zhaw.pm2.multichat.protocol;

/**
 *
 */
public class Config {

    public enum State {
        NEW, CONFIRM_CONNECT, CONNECTED, CONFIRM_DISCONNECT, DISCONNECTED;
    }

    public static final String USER_NONE = "";
    public static final String USER_ALL = "*";
}
