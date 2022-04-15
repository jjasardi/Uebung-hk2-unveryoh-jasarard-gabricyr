package ch.zhaw.pm2.multichat.client;

import ch.zhaw.pm2.multichat.protocol.ChatProtocolException;
import ch.zhaw.pm2.multichat.protocol.Config;
import ch.zhaw.pm2.multichat.protocol.Config.State;
import ch.zhaw.pm2.multichat.protocol.ConnectionHandler;
import ch.zhaw.pm2.multichat.protocol.Message;
import ch.zhaw.pm2.multichat.protocol.Message.MessageType;
import ch.zhaw.pm2.multichat.ClientInfo;
import ch.zhaw.pm2.multichat.protocol.NetworkHandler;

public class ClientConnectionHandler extends ConnectionHandler {
    private final ClientInfo clientInfo;
    private State state = State.NEW;
    private String userName = getUserName();

    public ClientConnectionHandler(NetworkHandler.NetworkConnection<Message> connection,
                                   String userName,
                                   ClientInfo clientInfo)  {
        super(connection, userName);
        this.clientInfo = clientInfo;
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
            clientInfo.setUserName(getUserName());
            clientInfo.setServerPort(getConnection().getRemotePort());
            clientInfo.setServerAddress(getConnection().getRemoteHost());
            addInfo(message.getText());
            System.out.println("CONFIRM: " + message.getText());
            setState(State.CONNECTED);

        } else if (state == State.CONFIRM_DISCONNECT) {
            addInfo(message.getText());
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
        addInfo(message.getText());
        System.out.println("DISCONNECT: " + message.getText());
        setState(State.DISCONNECTED);
    }

    @Override
    protected void handleMessage(Message message) {
        if (state != State.CONNECTED) {
            System.out.println("MESSAGE: Illegal state " + state + " for message: " + message.getText());
            return;
        }
        addMessage(message.getSender(), message.getReceiver(), message.getText());
        System.out.println("MESSAGE: From " + message.getSender() + " to " + message.getReceiver() + ": "+  message.getText());
    }

    @Override
    protected void handleError(Message message) {
        addError(message.getText());
        System.out.println("ERROR: " + message.getText());
    }

    @Override
    protected void onInterrupted() {
        this.setState(State.DISCONNECTED);
    }

    private void addMessage(String sender, String receiver, String message) {
        clientInfo.addMessage(new Message(Message.MessageType.MESSAGE, sender, receiver, message));
    }

    private void addInfo(String message) {
        clientInfo.addMessage(new Message(Message.MessageType.INFO, null, null, message));
    }

    private void addError(String message) {
        clientInfo.addMessage(new Message(Message.MessageType.ERROR, null, null, message));
    }
}
