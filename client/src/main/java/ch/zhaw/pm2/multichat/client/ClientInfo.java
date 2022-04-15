package ch.zhaw.pm2.multichat.client;

import ch.zhaw.pm2.multichat.protocol.Message;
import ch.zhaw.pm2.multichat.protocol.NetworkHandler;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ClientInfo {
    private final ListProperty<Message> messages;
    private final StringProperty userName = new SimpleStringProperty();
    private final IntegerProperty serverPort = new SimpleIntegerProperty();
    private final StringProperty serverAddress = new SimpleStringProperty();
    private final BooleanProperty isConnected = new SimpleBooleanProperty();

    public ClientInfo() {
        serverAddress.set(NetworkHandler.DEFAULT_ADDRESS.getCanonicalHostName());
        serverPort.set(NetworkHandler.DEFAULT_PORT);
        isConnected.set(false);

        ObservableList<Message> observableList = FXCollections.observableArrayList();
        messages = new SimpleListProperty<>(observableList);
    }

    public final String getUserName() {
        return userName.get();
    }

    public final int getServerPort() {
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

    public final void setServerPort(int serverPort) {
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

    public IntegerProperty serverPortProperty() {
        return serverPort;
    }

    public StringProperty serverAddressProperty() {
        return serverAddress;
    }

    public BooleanProperty isConnectedProperty() {
        return isConnected;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }
}
