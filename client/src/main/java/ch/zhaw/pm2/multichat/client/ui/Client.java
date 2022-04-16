package ch.zhaw.pm2.multichat.client.ui;

import javafx.application.Application;

/**
 * This class contains the main method and starts {@link ClientUI}.
 */
public class Client {

    public static void main(String[] args) {
        System.out.println("Starting Client Application");
        Application.launch(ClientUI.class, args);
        System.out.println("Client Application ended");
    }
}
