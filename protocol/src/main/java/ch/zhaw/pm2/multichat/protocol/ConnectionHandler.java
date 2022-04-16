package ch.zhaw.pm2.multichat.protocol;

import ch.zhaw.pm2.multichat.protocol.Message.MessageType;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;

import static ch.zhaw.pm2.multichat.protocol.Config.USER_NONE;

/**
 * This abstract class contains methods for the communication between server and client.
 */
public abstract class ConnectionHandler implements Runnable {
    private final NetworkHandler.NetworkConnection<Message> connection;

    protected String userName = USER_NONE;

    /**
     * This constructor initializes the {@link ch.zhaw.pm2.multichat.protocol.NetworkHandler.NetworkConnection} field.
     *
     * @param connection  {@link ch.zhaw.pm2.multichat.protocol.NetworkHandler.NetworkConnection} field to be initialized.
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

    @Override
    public void run () {
        startReceiving();
    }

    /**
     * This method receives the messages. Different actions are executed depending on the exceptions catched.
     */
    public void startReceiving() {
        System.out.println("Starting Connection Handler for " + userName);
        try {
            System.out.println("Start receiving data...");
            while (connection.isAvailable()) {
                Message data = connection.receive();
                processData(data);
            }
            System.out.println("Stopped receiving data");
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
     * This method stops receiving the messages by closing the current connection.
     */
    public void stopReceiving() {
        System.out.println("Closing Connection Handler for " + userName);
        try {
            System.out.println("Stop receiving data...");
            connection.close();
            System.out.println("Stopped receiving data.");
        } catch (IOException e) {
            System.err.println("Failed to close connection." + e.getMessage());
        }
        System.out.println("Closed Connection Handler for " + userName);
    }

    /**
     * This method reads the message. Then it handles differently depending on the {@link MessageType}.
     *
     * @param message  {@link Message} field to be read.
     */
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
            sendData(new Message(USER_NONE, userName, MessageType.ERROR, e.getMessage()));
        }
    }

    /**
     * This abstract method is called when the {@link MessageType} is {@link MessageType#CONNECT}. <br>
     * It handles it differently depending on the subclass.
     *
     * @param messsage                 {@link Message} field.
     * @throws ChatProtocolException   if an exception occures in the chat protocol, it will be thrown.
     */
    protected abstract void handleConnect(Message messsage) throws ChatProtocolException;

    /**
     * This abstract method is called when the {@link MessageType} is {@link MessageType#CONFIRM}. <br>
     * It handles it differently depending on the subclass.
     *
     * @param message  {@link Message} field.
     */
    protected abstract void handleConfirm(Message message);

    /**
     * This abstract method is called when the {@link MessageType} is {@link MessageType#DISCONNECT}. <br>
     * It handles it differently depending on the subclass.
     *
     * @param message                  {@link Message} field.
     * @throws ChatProtocolException   if an exception occures in the chat protocol, it will be thrown.
     */
    protected abstract void handleDisconnect(Message message) throws ChatProtocolException;


    /**
     * This abstract method is called when the {@link MessageType} is {@link MessageType#MESSAGE}. <br>
     * It handles it differently depending on the subclass.
     *
     * @param message                  {@link Message} field.
     * @throws ChatProtocolException   if an exception occures in the chat protocol, it will be thrown.
     */
    protected abstract void handleMessage(Message message) throws ChatProtocolException;


    /**
     * This abstract method is called when the {@link MessageType} is {@link MessageType#ERROR}. <br>
     * It handles it differently depending on the subclass.
     *
     * @param message  {@link Message} field.
     */
    protected abstract void handleError(Message message);

    /**
     * This method is called when the connection is interrupted.
     * It handles it differently depending on the subclass.
     */
    protected abstract void onInterrupted();


    /**
     * This method sends the {@link Message} object over the {@link ch.zhaw.pm2.multichat.protocol.NetworkHandler.NetworkConnection}.
     *
     * @param message  {@link Message} to be sent.
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
