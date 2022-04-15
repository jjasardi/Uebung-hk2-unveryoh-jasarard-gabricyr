package ch.zhaw.pm2.multichat.client.ui;

import ch.zhaw.pm2.multichat.client.ClientConnectionHandler;
import ch.zhaw.pm2.multichat.client.ClientInfo;
import ch.zhaw.pm2.multichat.protocol.ChatProtocolException;
import ch.zhaw.pm2.multichat.protocol.Config;
import ch.zhaw.pm2.multichat.protocol.Message;
import ch.zhaw.pm2.multichat.protocol.NetworkHandler;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.zhaw.pm2.multichat.protocol.Config.State;

/**
 * This class is a controller for the ChatWindow. It manages the communication between view and model and defines
 * the behaviour of the GUI for user interaction.
 */
public class ChatWindowController {
    private static final Pattern MESSAGE_PATTERN = Pattern.compile( "^(?:@(\\S*))?(\\s*)(.*)$" );
    private ClientInfo clientInfo;
    private ClientConnectionHandler connectionHandler;

    private final WindowCloseHandler windowCloseHandler = new WindowCloseHandler();

    @FXML private Pane rootPane;
    @FXML private TextField serverAddressField;
    @FXML private TextField serverPortField;
    @FXML private TextField userNameField;
    @FXML private TextField messageField;
    @FXML private TextArea messageArea;
    @FXML private Button connectButton;
    @FXML private Button sendButton;
    @FXML private TextField filterValue;


    /**
     * This method binds the controls from the {@link ClientUI} with the {@link ClientInfo} properties.
     */
    @FXML
    public void initialize() {
        clientInfo = new ClientInfo();
        sendButton.setDisable(true);

        userNameField.textProperty().bindBidirectional(clientInfo.userNameProperty());
        serverPortField.textProperty().bindBidirectional(clientInfo.serverPortProperty());
        serverAddressField.textProperty().bindBidirectional(clientInfo.serverAddressProperty());
        clientInfo.isConnectedProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> connectButton.setText((newValue) ? "Disconnect" : "Connect"));
            connectionStateChanged(newValue);
        });
        clientInfo.messageListProperty().addListener(new ListChangeListener<Message>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Message> c) {
                redrawMessageList();
            }
        });
    }

    private void applicationClose() {
        connectionHandler.setState(State.DISCONNECTED);
    }

    @FXML
    private void toggleConnection() {
        if (connectionHandler == null || connectionHandler.getState() != State.CONNECTED) {
            if (checkIfNotAnonymous() && checkIfNoWhitespace()) {
                connect();
            }
        } else {
            disconnect();
        }
    }


    private boolean checkIfNoWhitespace(){
        if(userNameField.getText().matches("^(\\S*)?")) {
            return true;
        } else {
            writeError("Invalid username!");
            writeInfo("Please enter a username without containing a whitespace.");
            return false;
        }
    }

    private boolean checkIfNotAnonymous(){
        if(userNameField.getText().matches("^(Anonymous-([0-9]+))+")) {
            writeError("Invalid username!");
            writeInfo("Username can not be 'Anonymous-N'");
            return false;
        }
        return true;
    }

    private void connect() {
        try {

            startConnectionHandler();
            connectionHandler.connect();
        } catch(ChatProtocolException | IOException e) {
            writeError(e.getMessage());
        }
    }

    private void disconnect() {
        if (connectionHandler == null) {
            writeError("No connection handler");
            return;
        }
        try {
            connectionHandler.disconnect();
        } catch (ChatProtocolException e) {
            writeError(e.getMessage());
        }
    }

    @FXML
    private void message() {
        if (connectionHandler == null) {
            writeError("No connection handler");
            return;
        }
        String messageString = messageField.getText().strip();
        Matcher matcher = MESSAGE_PATTERN.matcher(messageString);
        if (matcher.find()) {
            String receiver = matcher.group(1);
            String message = matcher.group(3);
            if (receiver == null || receiver.isBlank()) receiver = Config.USER_ALL;
            try {
                connectionHandler.message(receiver, message);
                messageField.clear();
            } catch (ChatProtocolException e) {
                writeError(e.getMessage());
            }
        } else {
            writeError("Not a valid message format.");
        }
    }

    @FXML
    private void applyFilter( ) {
    	this.redrawMessageList();
    }

    private void startConnectionHandler() throws IOException {

        String serverAddress = serverAddressField.getText();
        int serverPort = Integer.parseInt(serverPortField.getText());
        connectionHandler = new ClientConnectionHandler(
            NetworkHandler.openConnection(serverAddress, serverPort),
            clientInfo);
        new Thread(connectionHandler).start();

        // register window close handler
        rootPane.getScene().getWindow().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, windowCloseHandler);
    }

    private void connectionStateChanged(boolean isConnected) {
        if (!isConnected) {
            terminateConnectionHandler();
            sendButton.setDisable(true);
            userNameField.editableProperty().set(true);
            serverAddressField.editableProperty().set(true);
            serverPortField.editableProperty().set(true);
        } else {
            sendButton.setDisable(false);
            userNameField.editableProperty().set(false);
            serverAddressField.editableProperty().set(false);
            serverPortField.editableProperty().set(false);
        }
    }

    private void terminateConnectionHandler() {
        // unregister window close handler
        rootPane.getScene().getWindow().removeEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, windowCloseHandler);
        if (connectionHandler != null) {
            connectionHandler.stopReceiving();
            connectionHandler = null;
        }
    }

    private void clearMessageArea() {
        this.messageArea.clear();
    }

    private void redrawMessageList() {
        Platform.runLater(() -> writeFilteredMessages(filterValue.getText().strip()));
    }

    private void writeError(String message) {
        this.messageArea.appendText(String.format("[ERROR] %s\n", message));
    }

    private void writeInfo(String message) {
        this.messageArea.appendText(String.format("[INFO] %s\n", message));
    }

    private void writeMessage(String sender, String receiver, String message) {
        this.messageArea.appendText(String.format("[%s -> %s] %s\n", sender, receiver, message));
    }

    private void writeFilteredMessages(String filter) {
		boolean showAll = filter == null || filter.isBlank();
        List<Message> messageList = clientInfo.messageListProperty();
		clearMessageArea();
        for (Message message : messageList) {
            String sender = Objects.requireNonNullElse(message.getSender(),"");
		    String receiver = Objects.requireNonNullElse(message.getReceiver(),"");
		    String text = Objects.requireNonNull(message.getText(), "");
			if(showAll ||
					sender.contains(filter) ||
					receiver.contains(filter) ||
					text.contains(filter))
			{
			    switch (message.getType()) {
                    case MESSAGE: writeMessage(message.getSender(), message.getReceiver(), message.getText()); break;
                    case ERROR: writeError(message.getText()); break;
                    case INFO: writeInfo(message.getText()); break;
                    default: writeError("Unexpected message type: " + message.getType());
                }
			}
        }
	}


    /**
     * This class handles the closing of the {@link ClientUI}.
     */
    class WindowCloseHandler implements EventHandler<WindowEvent> {
        @Override
        public void handle(WindowEvent event) {
            applicationClose();
        }

    }
}
