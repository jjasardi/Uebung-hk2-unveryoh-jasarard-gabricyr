package ch.zhaw.pm2.multichat.client.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * This class extends {@link Application} and links the ChatWindow.fxml file.
 */
public class ClientUI extends Application {

    @Override
    public void start(Stage primaryStage) {
        chatWindow(primaryStage);
    }

    private void chatWindow(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatWindow.fxml"));
            Pane rootPane = loader.load();
            
            Scene scene = new Scene(rootPane);
            
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(420);
            primaryStage.setMinHeight(250);
            primaryStage.setTitle("Multichat Client");
            primaryStage.show();
        } catch(Exception e) {
            System.err.println("Error starting up UI" + e.getMessage());
        }
    }
}
