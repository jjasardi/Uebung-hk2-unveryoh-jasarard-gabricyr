package ch.zhaw.pm2.multichat.client;

import ch.zhaw.pm2.multichat.protocol.ChatProtocolException;
import ch.zhaw.pm2.multichat.protocol.Config;
import ch.zhaw.pm2.multichat.protocol.Config.State;
import ch.zhaw.pm2.multichat.protocol.ConnectionHandler;
import ch.zhaw.pm2.multichat.protocol.Message;
import ch.zhaw.pm2.multichat.protocol.Message.MessageType;
import ch.zhaw.pm2.multichat.protocol.NetworkHandler;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;

public class ClientConnectionHandler extends ConnectionHandler {
    private final ChatWindowController controller;
    private State state = State.NEW;
    private String userName = getUserName();

    public ClientConnectionHandler(NetworkHandler.NetworkConnection<Message> connection,
                                   String userName,
                                   ChatWindowController controller)  {
        super(connection, userName);
        this.controller = controller;
    }

    public State getState() {
        return this.state;
    }

    public void setState (State newState) {
        this.state = newState;
        controller.stateChanged(newState);
    }

    public void run () {
        startReceiving();
    }

    public void startReceiving() {
        System.out.println("Starting Connection Handler");
        try {
            System.out.println("Start receiving data...");
            while (getConnection().isAvailable()) {
                Message data = getConnection().receive();
                processData(data);
            }
            System.out.println("Stopped recieving data");
        } catch (SocketException e) {
            System.out.println("Connection terminated locally");
            this.setState(State.DISCONNECTED);
            System.err.println("Unregistered because connection terminated" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("Connection terminated by remote");
            this.setState(State.DISCONNECTED);
            System.err.println("Unregistered because connection terminated" + e.getMessage());
        } catch(IOException e) {
            System.err.println("Communication error" + e);
        } catch(ClassNotFoundException e) {
            System.err.println("Received object of unknown type" + e.getMessage());
        }
        System.out.println("Stopped Connection Handler");
    }

    public void connect() throws ChatProtocolException {
        if (state != State.NEW) throw new ChatProtocolException("Illegal state for connect: " + state);
        this.sendData(new Message(MessageType.CONNECT, userName, Config.USER_NONE, null));
        this.setState(State.CONFIRM_CONNECT);
    }

    public void disconnect() throws ChatProtocolException {
        if (state != State.NEW && state != State.CONNECTED) throw new ChatProtocolException("Illegal state for disconnect: " + state);
        this.sendData(new Message(MessageType.DISCONNECT, userName, Config.USER_NONE, null));
        this.setState(State.CONFIRM_DISCONNECT);
    }

    public void message(String receiver, String message) throws ChatProtocolException {
        if (state != State.CONNECTED) throw new ChatProtocolException("Illegal state for message: " + state);
        this.sendData(new Message(MessageType.MESSAGE, userName, receiver, message));
    }

    @Override
    protected void handleConnect(Message message) {
        System.err.println("Illegal connect request from server");
    }

    @Override
    protected void handleConfirm(Message message) {
        if (state == State.CONFIRM_CONNECT) {
            this.userName = message.getReceiver();
            controller.setUserName(getUserName());
            controller.setServerPort(getConnection().getRemotePort());
            controller.setServerAddress(getConnection().getRemoteHost());
            controller.addInfo(message.getText());
            System.out.println("CONFIRM: " + message.getText());
            setState(State.CONNECTED);

        } else if (state == State.CONFIRM_DISCONNECT) {
            controller.addInfo(message.getText());
            System.out.println("CONFIRM: " + message.getText());
            setState(State.DISCONNECTED);

        } else {
            System.err.println("Got unexpected confirm message: " + message.getText());
        }
    }

    @Override
    protected void handleDisconnect(Message message) {
        if (state == State.DISCONNECTED) {
            System.out.println("DISCONNECT: Already in disconnected: " + message.getText());
            return;
        }
        controller.addInfo(message.getText());
        System.out.println("DISCONNECT: " + message.getText());
        setState(State.DISCONNECTED);
    }

    @Override
    protected void handleMessage(Message message) {
        if (state != State.CONNECTED) {
            System.out.println("MESSAGE: Illegal state " + state + " for message: " + message.getText());
            return;
        }
        controller.addMessage(message.getSender(), message.getReceiver(), message.getText());
        System.out.println("MESSAGE: From " + message.getSender() + " to " + message.getReceiver() + ": "+  message.getText());
    }

    @Override
    protected void handleError(Message message) {
        controller.addError(message.getText());
        System.out.println("ERROR: " + message.getText());
    }

}
