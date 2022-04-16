package ch.zhaw.pm2.multichat.server;

import ch.zhaw.pm2.multichat.protocol.Config;
import ch.zhaw.pm2.multichat.protocol.ChatProtocolException;
import ch.zhaw.pm2.multichat.protocol.ConnectionHandler;
import ch.zhaw.pm2.multichat.protocol.Message;
import ch.zhaw.pm2.multichat.protocol.Message.MessageType;
import ch.zhaw.pm2.multichat.protocol.NetworkHandler;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static ch.zhaw.pm2.multichat.protocol.Config.State;

public class ServerConnectionHandler extends ConnectionHandler {
    private final Map<String,ServerConnectionHandler> connectionRegistry;
    private static final AtomicInteger CONNECTION_COUNTER= new AtomicInteger(1);
    private static int connectionID = CONNECTION_COUNTER.get();
    private State state = State.NEW;

    public ServerConnectionHandler (NetworkHandler.NetworkConnection<Message> connection,
                                   Map<String,ServerConnectionHandler> registry) {
        super(connection);
        checkIfUserNameIsSet();
        Objects.requireNonNull(connection, "Connection must not be null");
        Objects.requireNonNull(registry, "Registry must not be null");
        this.connectionRegistry = registry;
    }

    private void checkIfUserNameIsSet() {
        if (Config.USER_NONE.equals(userName)) {
            this.userName = "Anonymous-" + connectionID;
        }
    }

    @Override
    protected void handleConnect(Message message) throws ChatProtocolException {
        if (this.state != State.NEW) {
            throw new ChatProtocolException("Illegal state for connect request: " + state);
        }
        if (message.getReceiver() == null || message.getSender().isBlank()) {
            message.setSender(this.userName);
            connectionID = CONNECTION_COUNTER.incrementAndGet();
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
    protected void onInterrupted() {
        connectionRegistry.remove(userName);
    }

    @Override
    public void run() {
        startReceiving();
    }
}
