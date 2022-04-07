package ch.zhaw.pm2.multichat.client;

import ch.zhaw.pm2.multichat.protocol.ChatProtocolException;
import ch.zhaw.pm2.multichat.protocol.ConnectionHandler;
import ch.zhaw.pm2.multichat.protocol.NetworkHandler;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.util.Scanner;

public class ClientConnectionHandler extends ConnectionHandler implements Runnable {
    private final ChatWindowController controller;
    private State state = State.NEW;
    private String userName = getUserName();

    public ClientConnectionHandler(NetworkHandler.NetworkConnection<String> connection,
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
                String data = getConnection().receive();
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

    public void stopReceiving() {
        System.out.println("Closing Connection Handler to Server");
        try {
            System.out.println("Stop receiving data...");
            getConnection().close();
            System.out.println("Stopped receiving data.");
        } catch (IOException e) {
            System.err.println("Failed to close connection." + e.getMessage());
        }
        System.out.println("Closed Connection Handler to Server");
    }

    private void processData(String data) {
        try {
            // parse data content
            Scanner scanner = new Scanner(data);
            String sender = null;
            String reciever = null;
            String type = null;
            String payload = null;
            if (scanner.hasNextLine()) {
                sender = scanner.nextLine();
            } else {
                throw new ChatProtocolException("No Sender found");
            }
            if (scanner.hasNextLine()) {
                reciever = scanner.nextLine();
            } else {
                throw new ChatProtocolException("No Reciever found");
            }
            if (scanner.hasNextLine()) {
                type = scanner.nextLine();
            } else {
                throw new ChatProtocolException("No Type found");
            }
            if (scanner.hasNextLine()) {
                payload = scanner.nextLine();
            }
            // dispatch operation based on type parameter
            if (type.equals(DATA_TYPE_CONNECT)) {
                System.err.println("Illegal connect request from server");
            } else if (type.equals(DATA_TYPE_CONFIRM)) {
                if (state == State.CONFIRM_CONNECT) {
                    this.userName = reciever;
                    controller.setUserName(getUserName());
                    controller.setServerPort(getConnection().getRemotePort());
                    controller.setServerAddress(getConnection().getRemoteHost());
                    controller.addInfo(payload);
                    System.out.println("CONFIRM: " + payload);
                    this.setState(State.CONNECTED);
                } else if (state == State.CONFIRM_DISCONNECT) {
                    controller.addInfo(payload);
                    System.out.println("CONFIRM: " + payload);
                    this.setState(State.DISCONNECTED);
                } else {
                    System.err.println("Got unexpected confirm message: " + payload);
                }
            } else if (type.equals(DATA_TYPE_DISCONNECT)) {
                if (state == State.DISCONNECTED) {
                    System.out.println("DISCONNECT: Already in disconnected: " + payload);
                    return;
                }
                controller.addInfo(payload);
                System.out.println("DISCONNECT: " + payload);
                this.setState(State.DISCONNECTED);
            } else if (type.equals(DATA_TYPE_MESSAGE)) {
                if (state != State.CONNECTED) {
                    System.out.println("MESSAGE: Illegal state " + state + " for message: " + payload);
                    return;
                }
                controller.addMessage(sender, reciever, payload);
                System.out.println("MESSAGE: From " + sender + " to " + reciever + ": "+  payload);
            } else if (type.equals(DATA_TYPE_ERROR)) {
                controller.addError(payload);
                System.out.println("ERROR: " + payload);
            } else {
                System.out.println("Unknown data type received: " + type);
            }
        } catch (ChatProtocolException e) {
            System.err.println("Error while processing data: " + e.getMessage());
            sendData(USER_NONE, userName, DATA_TYPE_ERROR, e.getMessage());
        }
    }

    public void connect() throws ChatProtocolException {
        if (state != State.NEW) throw new ChatProtocolException("Illegal state for connect: " + state);
        this.sendData(userName, USER_NONE, DATA_TYPE_CONNECT,null);
        this.setState(State.CONFIRM_CONNECT);
    }

    public void disconnect() throws ChatProtocolException {
        if (state != State.NEW && state != State.CONNECTED) throw new ChatProtocolException("Illegal state for disconnect: " + state);
        this.sendData(userName, USER_NONE, DATA_TYPE_DISCONNECT,null);
        this.setState(State.CONFIRM_DISCONNECT);
    }

    public void message(String receiver, String message) throws ChatProtocolException {
        if (state != State.CONNECTED) throw new ChatProtocolException("Illegal state for message: " + state);
        this.sendData(userName, receiver, DATA_TYPE_MESSAGE,message);
    }

}
