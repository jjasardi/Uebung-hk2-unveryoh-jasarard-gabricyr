package ch.zhaw.pm2.multichat.client;

import ch.zhaw.pm2.multichat.protocol.Config;
import ch.zhaw.pm2.multichat.protocol.Message;
import ch.zhaw.pm2.multichat.protocol.NetworkHandler;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ClientInfo {
    private final ListProperty<Message> messageList;
    private final StringProperty userName = new SimpleStringProperty();
    private final StringProperty serverPort = new SimpleStringProperty();
    private final StringProperty serverAddress = new SimpleStringProperty();
    private final BooleanProperty isConnected = new SimpleBooleanProperty();

    public ClientInfo() {
        serverAddress.set(NetworkHandler.DEFAULT_ADDRESS.getCanonicalHostName());
        serverPort.set(String.valueOf(NetworkHandler.DEFAULT_PORT));
        isConnected.set(false);
        userName.set(Config.USER_NONE);

        ObservableList<Message> observableList = FXCollections.observableArrayList();
        messageList = new SimpleListProperty<>(observableList);
    }

    public final String getUserName() {
        return userName.get();
    }

    public final String getServerPort() {
        return serverPort.get();
    }

    public final String getServerAddress() {
        return serverAddress.get();
    }

    public final boolean getIsConnected() {
        return isConnected.get();
    }

    public final void setUserName(String userName) {
        this.userName.set(userName);
    }

    public final void setServerPort(String serverPort) {
        this.serverPort.set(serverPort);
    }

    public final void setServerAddress(String serverAddress) {
        this.serverAddress.set(serverAddress);
    }

    public final void setIsConnected(boolean isConnected) {
        this.isConnected.set(isConnected);
    }

    public StringProperty userNameProperty() {
        return userName;
    }

    public StringProperty serverPortProperty() {
        return serverPort;
    }

    public StringProperty serverAddressProperty() {
        return serverAddress;
    }

    public BooleanProperty isConnectedProperty() {
        return isConnected;
    }

    public final ListProperty<Message> messageListProperty() {
        return messageList;
    }

    public void addMessage(Message message) {
        messageList.add(message);
    }
}
