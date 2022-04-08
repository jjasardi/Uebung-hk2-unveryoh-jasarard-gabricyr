package ch.zhaw.pm2.multichat.protocol;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public abstract class ConnectionHandler {
    private final NetworkHandler.NetworkConnection<String> connection;
    private static final AtomicInteger connectionCounter = new AtomicInteger(0);
    private final int connectionId = connectionCounter.incrementAndGet();

    // Data types used for the Chat Protocol
    public static final String DATA_TYPE_CONNECT = "CONNECT";
    public static final String DATA_TYPE_CONFIRM = "CONFIRM";
    public static final String DATA_TYPE_DISCONNECT = "DISCONNECT";
    public static final String DATA_TYPE_MESSAGE = "MESSAGE";
    public static final String DATA_TYPE_ERROR = "ERROR";

    public static final String USER_NONE = "";
    public static final String USER_ALL = "*";

    private String userName = USER_NONE;

    public enum State {
        NEW, CONFIRM_CONNECT, CONNECTED, CONFIRM_DISCONNECT, DISCONNECTED;
    }

    /**
     * @param connection
     */
    public ConnectionHandler(NetworkHandler.NetworkConnection<String> connection, String userName) {
        this.connection = connection;
        this.userName = userName;
    }

    /**
     * @param connection
     */
    public ConnectionHandler(NetworkHandler.NetworkConnection<String> connection) {
        this.connection = connection;
        this.userName = "Anonymous-" + connectionId;
    }

    public String getUserName() {
        return this.userName;
    }

    public NetworkHandler.NetworkConnection<String> getConnection() {
        return this.connection;
    }

    /**
     *
     */
    public void stopReceiving() {
        System.out.println("Closing Connection Handler for " + getUserName());
        try {
            System.out.println("Stop receiving data...");
            connection.close();
            System.out.println("Stopped receiving data.");
        } catch (IOException e) {
            System.err.println("Failed to close connection." + e.getMessage());
        }
        System.out.println("Closed Connection Handler for " + getUserName());
    }


    /**
     * @param sender
     * @param receiver
     * @param type
     * @param payload
     */
    public void sendData(String sender, String receiver, String type, String payload) {
        if (connection.isAvailable()) {
            new StringBuilder();
            String data = new StringBuilder()
                .append(sender+"\n")
                .append(receiver+"\n")
                .append(type+"\n")
                .append(payload+"\n")
                .toString();
            try {
                connection.send(data);
            } catch (SocketException e) {
                System.err.println("Connection closed: " + e.getMessage());
            } catch (EOFException e) {
                System.out.println("Connection terminated by remote");
            } catch(IOException e) {
                System.err.println("Communication error: " + e.getMessage());
            }
        }
    }
}
