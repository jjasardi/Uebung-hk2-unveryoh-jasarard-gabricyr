package ch.zhaw.pm2.multichat;

import ch.zhaw.pm2.multichat.protocol.Message;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
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

    public ClientInfo() {
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

    public final void setUserName(String userName) {
        this.userName.set(userName);
    }

    public final void setServerPort(int serverPort) {
        this.serverPort.set(serverPort);
    }

    public final void setServerAddress(String serverAddress) {
        this.serverAddress.set(serverAddress);
    }

    public void addMessage(Message message){
        messages.add(message);
    }
}
