package ch.zhaw.pm2.multichat.client;

import ch.zhaw.pm2.multichat.protocol.Config;
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

/**
 * This class contains all the informations about the {@link ch.zhaw.pm2.multichat.client.ui.Client}.
 */
public class ClientInfo {
    private final ListProperty<Message> messageList;
    private final StringProperty userName = new SimpleStringProperty();
    private final IntegerProperty serverPort = new SimpleIntegerProperty();
    private final StringProperty serverAddress = new SimpleStringProperty();
    private final BooleanProperty isConnected = new SimpleBooleanProperty();

    /**
     * This constructor creates a new {@link ClientInfo} object.
     */
    public ClientInfo() {
        serverAddress.set(NetworkHandler.DEFAULT_ADDRESS.getCanonicalHostName());
        serverPort.set(NetworkHandler.DEFAULT_PORT);
        isConnected.set(false);
        userName.set(Config.USER_NONE);

        ObservableList<Message> observableList = FXCollections.observableArrayList();
        messageList = new SimpleListProperty<>(observableList);
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

    /**
     * This method returns the username as a StringProperty.
     * @return  userName
     */
    public StringProperty userNameProperty() {
        return userName;
    }

    /**
     * This method return the server port as a IntegerProperty.
     * @return  serverPort
     */
    public IntegerProperty serverPortProperty() {
        return serverPort;
    }

    /**
     * This method returns the serverAddress property.
     *
     * @return serverAddress.
     */
    public StringProperty serverAddressProperty() {
        return serverAddress;
    }

    /**
     * his method returns the isConnected property.
     *
     * @return  isConnected.
     */
    public BooleanProperty isConnectedProperty() {
        return isConnected;
    }

    /**
     * This method returns the messageList property.
     *
     * @return  messageList.
     */
    public final ListProperty<Message> messageListProperty() {
        return messageList;
    }

    /**
     * This method adds a message to the messageList.
     *
     * @param message   {@link Message} field to be added.
     */
    public void addMessage(Message message) {
        messageList.add(message);
    }
}
