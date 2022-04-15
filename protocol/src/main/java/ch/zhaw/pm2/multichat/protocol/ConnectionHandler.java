package ch.zhaw.pm2.multichat.protocol;

import ch.zhaw.pm2.multichat.protocol.Message.MessageType;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;

import static ch.zhaw.pm2.multichat.protocol.Config.USER_NONE;

/**
 *
 */
public abstract class ConnectionHandler implements Runnable {
    private final NetworkHandler.NetworkConnection<Message> connection;

    protected String userName = USER_NONE;


    /**
     * @param connection
     */
    protected ConnectionHandler(NetworkHandler.NetworkConnection<Message> connection) {
        this.connection = connection;
    }

    public String getUserName() {
        return this.userName;
    }

    public NetworkHandler.NetworkConnection<Message> getConnection() {
        return this.connection;
    }

    /**
     *
     */
    public void startReceiving() {
        System.out.println("Starting Connection Handler for " + userName);
        try {
            System.out.println("Start receiving data...");
            while (connection.isAvailable()) {
                Message data = connection.receive();
                processData(data);
            }
            System.out.println("Stopped recieving data");
        } catch (SocketException e) {
            System.out.println("Connection terminated locally");
            onInterrupted();
            System.out.println("Unregistered because client connection terminated: " + userName + " " + e.getMessage());
        } catch (EOFException e) {
            System.out.println("Connection terminated by remote");
            onInterrupted();
            System.out.println("Unregistered because client connection terminated: " + userName + " " + e.getMessage());
        } catch(IOException e) {
            System.err.println("Communication error: " + e);
        } catch(ClassNotFoundException e) {
            System.err.println("Received object of unknown type: " + e.getMessage());
        }
        System.out.println("Stopping Connection Handler for " + userName);
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

    protected void processData(Message message) {
        try {
            switch (message.getType()) {
                case CONNECT -> handleConnect(message);
                case CONFIRM -> handleConfirm(message);
                case DISCONNECT -> handleDisconnect(message);
                case MESSAGE -> handleMessage(message);
                case ERROR -> handleError(message);
                default -> System.out.println("Unknown data type received: " + message.getType());
            }
        } catch (ChatProtocolException e) {
            System.err.println("Error while processing data: " + e.getMessage());
            sendData(new Message(MessageType.ERROR, USER_NONE, getUserName(), e.getMessage()));
        }
    }

    /**
     *
     * @throws ChatProtocolException
     */
    protected abstract void handleConnect(Message messsage) throws ChatProtocolException;

    /**
     *
     * @param message
     */
    protected abstract void handleConfirm(Message message);

    /**
     *
     * @param message
     * @throws ChatProtocolException
     */
    protected abstract void handleDisconnect(Message message) throws ChatProtocolException;


    /**
     *
     * @param message
     * @throws ChatProtocolException
     */
    protected abstract void handleMessage(Message message) throws ChatProtocolException;


    /**
     *
     * @param message
     */
    protected abstract void handleError(Message message);

    /**
     *
     */
    protected abstract void onInterrupted();


    /**
     * @param message
     */
    public void sendData(Message message) {
        if (connection.isAvailable()) {
            try {
                connection.send(message);
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
