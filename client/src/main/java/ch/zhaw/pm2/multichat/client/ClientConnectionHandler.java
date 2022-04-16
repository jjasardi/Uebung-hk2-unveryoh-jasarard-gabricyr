package ch.zhaw.pm2.multichat.client;

import ch.zhaw.pm2.multichat.protocol.ChatProtocolException;
import ch.zhaw.pm2.multichat.protocol.Config;
import ch.zhaw.pm2.multichat.protocol.Config.State;
import ch.zhaw.pm2.multichat.protocol.ConnectionHandler;
import ch.zhaw.pm2.multichat.protocol.Message;
import ch.zhaw.pm2.multichat.protocol.Message.MessageType;
import ch.zhaw.pm2.multichat.protocol.NetworkHandler;

/**
 * This class manages the connection and the communication with the server. <br>
 * It also executes different actions depending on the type of the message received.
 */
public class ClientConnectionHandler extends ConnectionHandler {
    private final ClientInfo clientInfo;
    private State state = State.NEW;

    /**
     * This constructor creates a new {@link ClientConnectionHandler} object.
     *
     * @param connection    the {@link ch.zhaw.pm2.multichat.protocol.NetworkHandler.NetworkConnection}
     *                      which will be given to the superclass constructor.
     * @param clientInfo    {@link ClientInfo} field.
     */
    public ClientConnectionHandler(NetworkHandler.NetworkConnection<Message> connection,
                                   ClientInfo clientInfo)  {
        super(connection);
        this.clientInfo = clientInfo;
    }

    public State getState() {
        return this.state;
    }

    public void setState (State newState) {
        this.state = newState;
        if (newState == State.CONNECTED) {
            clientInfo.setIsConnected(true);
        } else if (newState == State.DISCONNECTED) {
            clientInfo.setIsConnected(false);
        }
    }

    @Override
    public void run () {
        startReceiving();
    }

    /**
     * This method sends a message to the server with the username and the {@link MessageType#CONNECT} type. <br>
     * Then it updates the {@link State} to {@link State#CONFIRM_CONNECT}.
     *
     * @throws ChatProtocolException   exception if the {@link State} is not {@link State#NEW}.
     */
    public void connect() throws ChatProtocolException {
        if (state != State.NEW) throw new ChatProtocolException("Illegal state for connect: " + state);
        this.sendData(new Message(MessageType.CONNECT, clientInfo.getUserName(), Config.USER_NONE, null));
        this.setState(State.CONFIRM_CONNECT);
    }

    /**
     * This method sends a message to the server to disconnect the current user. <br>
     * Then it updates the {@link State} to {@link State#CONFIRM_DISCONNECT}.
     *
     * @throws ChatProtocolException   exception if the {@link State} is not {@link State#NEW} and not {@link State#CONNECTED}.
     */
    public void disconnect() throws ChatProtocolException {
        if (state != State.NEW && state != State.CONNECTED) throw new ChatProtocolException("Illegal state for disconnect: " + state);
        this.sendData(new Message(MessageType.DISCONNECT, clientInfo.getUserName(), Config.USER_NONE, null));
        this.setState(State.CONFIRM_DISCONNECT);
    }

    /**
     * This method sends a message to the receiver.
     *
     * @param receiver --> wird sowieso angepasst beim refactoring (Message message)
     * @param message  --> wird sowieso angepasst beim refactoring (Message message)
     * @throws ChatProtocolException    exception if the {@link State} is not {@link State#CONNECTED}.
     */
    public void message(String receiver, String message) throws ChatProtocolException {
        if (state != State.CONNECTED) throw new ChatProtocolException("Illegal state for message: " + state);
        this.sendData(new Message(MessageType.MESSAGE, clientInfo.getUserName(), receiver, message));
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
