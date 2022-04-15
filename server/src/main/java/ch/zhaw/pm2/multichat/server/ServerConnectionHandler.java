package ch.zhaw.pm2.multichat.server;

import ch.zhaw.pm2.multichat.protocol.Config;
import ch.zhaw.pm2.multichat.protocol.Message;
import ch.zhaw.pm2.multichat.protocol.Message.MessageType;
import ch.zhaw.pm2.multichat.protocol.ChatProtocolException;
import ch.zhaw.pm2.multichat.protocol.ConnectionHandler;
import ch.zhaw.pm2.multichat.protocol.NetworkHandler;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.util.Map;
import java.util.Objects;

import static ch.zhaw.pm2.multichat.protocol.Config.State;

public class ServerConnectionHandler extends ConnectionHandler implements Runnable {
    private final NetworkHandler.NetworkConnection<Message> connection;
    private final Map<String,ServerConnectionHandler> connectionRegistry;

    private String userName = getUserName();
    private Config.State state = Config.State.NEW;

    public ServerConnectionHandler (NetworkHandler.NetworkConnection<Message> connection,
                                   Map<String,ServerConnectionHandler> registry) {
        super(connection);
        Objects.requireNonNull(connection, "Connection must not be null");
        Objects.requireNonNull(registry, "Registry must not be null");
        this.connection = connection;
        this.connectionRegistry = registry;
    }

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
            connectionRegistry.remove(userName);
            System.out.println("Unregistered because client connection terminated: " + userName + " " + e.getMessage());
        } catch (EOFException e) {
            System.out.println("Connection terminated by remote");
            connectionRegistry.remove(userName);
            System.out.println("Unregistered because client connection terminated: " + userName + " " + e.getMessage());
        } catch(IOException e) {
            System.err.println("Communication error: " + e);
        } catch(ClassNotFoundException e) {
            System.err.println("Received object of unknown type: " + e.getMessage());
        }
        System.out.println("Stopping Connection Handler for " + userName);
    }

    @Override
    protected void handleConnect(Message message) throws ChatProtocolException {
        if (this.state != State.NEW) {
            throw new ChatProtocolException("Illegal state for connect request: " + state);
        }
        if (message.getReceiver() == null || message.getSender().isBlank()) {
            message.setSender(this.userName);
        }
        if (connectionRegistry.containsKey(message.getSender())) {
            throw new ChatProtocolException("User name already taken: " + message.getSender());
        }
        this.userName = message.getSender();
        connectionRegistry.put(userName, this);
        sendData(new Message(MessageType.CONFIRM, Config.USER_NONE, userName, "Registration successfull for " + userName));
        this.state = State.CONNECTED;
    }

    @Override
    protected void handleConfirm(Message message) {
        System.out.println("Not expecting to receive a CONFIRM request from client");
    }

    @Override
    protected void handleDisconnect(Message message) throws ChatProtocolException {
        if (state == State.DISCONNECTED) {
            throw new ChatProtocolException("Illegal state for disconnect request: " + state);
        }
        if (state == State.CONNECTED) {
            connectionRegistry.remove(this.userName);
        }
        sendData(new Message(MessageType.CONFIRM, Config.USER_NONE, userName, "Confirm disconnect of " + userName));
        this.state = State.DISCONNECTED;
        this.stopReceiving();
    }

    @Override
    protected void handleMessage(Message message) throws ChatProtocolException {
        if (state != State.CONNECTED) {
            throw new ChatProtocolException("Illegal state for message request: " + state);
        }
        if (Config.USER_ALL.equals(message.getReceiver())) {
            for (ServerConnectionHandler handler : connectionRegistry.values()) {
                handler.sendData(new Message(message.getType(), message.getSender(), message.getReceiver(), message.getText()));
            }
        } else {
            ServerConnectionHandler handler = connectionRegistry.get(message.getReceiver());
            if (handler != null) {
                handler.sendData(new Message(message.getType(), message.getSender(), message.getReceiver(), message.getText()));
            } else {
                this.sendData(new Message(MessageType.ERROR, Config.USER_NONE, userName, "Unknown User: " + message.getReceiver()));
            }
        }
    }

    @Override
    protected void handleError(Message message) {
        System.err.println("Received error from client (" + message.getSender() + "): " + message.getText());
    }

    @Override
    public void run() {
        startReceiving();
    }
}
