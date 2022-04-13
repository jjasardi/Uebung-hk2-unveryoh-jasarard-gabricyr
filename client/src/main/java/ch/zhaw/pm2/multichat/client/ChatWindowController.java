package ch.zhaw.pm2.multichat.client;

import ch.zhaw.pm2.multichat.Message;
import ch.zhaw.pm2.multichat.protocol.ChatProtocolException;
import ch.zhaw.pm2.multichat.protocol.ConnectionHandler;
import ch.zhaw.pm2.multichat.protocol.NetworkHandler;
import javafx.application.Platform;
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

public class ChatWindowController {
    private final static Pattern messagePattern = Pattern.compile( "^(?:@(\\S*))?(\\s*)(.*)$" );
    private ClientConnectionHandler connectionHandler;
    private ClientMessageListDecorator messagesDecorator;

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


    @FXML
    public void initialize() {
        serverAddressField.setText(NetworkHandler.DEFAULT_ADDRESS.getCanonicalHostName());
        serverPortField.setText(String.valueOf(NetworkHandler.DEFAULT_PORT));
        stateChanged(ConnectionHandler.State.NEW);
        setClientMessageListDecorator();
    }

    private void setClientMessageListDecorator() {
        messagesDecorator = new ClientMessageListDecorator(new ClientMessageList());
        messagesDecorator.addListener(new IsObserver() {
            @Override
            public void update() {
                redrawMessageList();
            }
        });
    }

    private void applicationClose() {
        connectionHandler.setState(ConnectionHandler.State.DISCONNECTED);
    }

    @FXML
    private void toggleConnection () {
        if(userNameField.getText().matches("^(\\S*)?")) {
            if (connectionHandler == null || connectionHandler.getState() != ConnectionHandler.State.CONNECTED) {
                connect();
            } else {
                disconnect();
            }
        } else {
            writeError("Invalid username!");
            writeInfo("Please enter a username without containing a whitespace.");
            return;
        }
    }

    private void connect() {
        try {
            setClientMessageListDecorator();
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
        Matcher matcher = messagePattern.matcher(messageString);
        if (matcher.find()) {
            String receiver = matcher.group(1);
            String message = matcher.group(3);
            if (receiver == null || receiver.isBlank()) receiver = ConnectionHandler.USER_ALL;
            try {
                connectionHandler.message(receiver, message);
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
        String userName = userNameField.getText();

        String serverAddress = serverAddressField.getText();
        int serverPort = Integer.parseInt(serverPortField.getText());
        connectionHandler = new ClientConnectionHandler(
            NetworkHandler.openConnection(serverAddress, serverPort), userName,
            this);
        new Thread(connectionHandler).start();

        // register window close handler
        rootPane.getScene().getWindow().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, windowCloseHandler);
    }

    private void terminateConnectionHandler() {
        // unregister window close handler
        rootPane.getScene().getWindow().removeEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, windowCloseHandler);
        if (connectionHandler != null) {
            connectionHandler.stopReceiving();
            connectionHandler = null;
        }
    }

    public void stateChanged(ConnectionHandler.State newState) {
        // update UI (need to be run in UI thread: see Platform.runLater())
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                connectButton.setText((newState == ConnectionHandler.State.CONNECTED || newState == ConnectionHandler.State.CONFIRM_DISCONNECT) ? "Disconnect" : "Connect");
            }
        });
        if (newState == ConnectionHandler.State.DISCONNECTED) {
            terminateConnectionHandler();
        }
    }

    public void setUserName(String userName) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                userNameField.setText(userName);
            }
        });
    }

    public void setServerAddress(String serverAddress) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                serverAddressField.setText(serverAddress);
            }
        });
    }

    public void setServerPort(int serverPort) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                serverPortField.setText(Integer.toString(serverPort));
            }
        });
    }

    public void addMessage(String sender, String receiver, String message) {
        messagesDecorator.addMessage(new Message(Message.MessageType.MESSAGE, sender, receiver, message));
    }

    public void addInfo(String message) {
        messagesDecorator.addMessage(new Message(Message.MessageType.INFO, null, null, message));
    }

    public void addError(String message) {
        messagesDecorator.addMessage(new Message(Message.MessageType.ERROR, null, null, message));
    }

    public void clearMessageArea() {
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

    private void writeMessage(String sender, String reciever, String message) {
        this.messageArea.appendText(String.format("[%s -> %s] %s\n", sender, reciever, message));
    }

    private void writeFilteredMessages(String filter) {
		boolean showAll = filter == null || filter.isBlank();
        List<Message> messageList = messagesDecorator.getMessageList();
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


    class WindowCloseHandler implements EventHandler<WindowEvent> {
        public void handle(WindowEvent event) {
            applicationClose();
        }

    }




}
