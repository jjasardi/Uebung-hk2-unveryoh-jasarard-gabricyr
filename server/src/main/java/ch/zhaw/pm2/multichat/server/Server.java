package ch.zhaw.pm2.multichat.server;

import ch.zhaw.pm2.multichat.protocol.Message;
import ch.zhaw.pm2.multichat.protocol.NetworkHandler;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;


/**
 * This class represents the multichat {@link Server} and contains the main method. <br>
 * It also contains the thread for the {@link Server} object.
 */
public class Server {

    // Server connection
    private NetworkHandler.NetworkServer<Message> networkServer;

    // Connection registry
    private Map<String,ServerConnectionHandler> connections = new HashMap<>();

    public static void main(String[] args) {
        // Parse arguments for server port.
        try {
            int port;
            switch (args.length) {
                case 0 -> port = NetworkHandler.DEFAULT_PORT;
                case 1 -> port = Integer.parseInt(args[0]);
                default -> {
                    System.out.println("Illegal number of arguments:  [<ServerPort>]");
                    return;
                }
            }
            // Initialize server
            final Server server = new Server(port);

            // This adds a shutdown hook running a cleanup task if the JVM is terminated (kill -HUP, Ctrl-C,...)
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(200);
                        System.out.println("Shutdown initiated...");
                        server.terminate();
                    } catch (InterruptedException e) {
                        System.out.println("Warning: Shutdown interrupted. " + e);
                    } finally {
                        System.out.println("Shutdown complete.");
                    }
                }
            });

            // Start server
            server.start();
        } catch (IOException e) {
            System.err.println("Error while starting server." + e.getMessage());
        }
    }

    /**
     * This constructor creates a {@link Server} object.
     *
     * @param serverPort    given server port.
     * @throws IOException  exception if an error occures while creating the {@link Server} the new port.
     */
    public Server(int serverPort) throws IOException {
        // Open server connection
        System.out.println("Create server connection");
        networkServer = NetworkHandler.createServer(serverPort);
        System.out.println("Listening on " + networkServer.getHostAddress() + ":" + networkServer.getHostPort());
    }

    private void start() {
        System.out.println("Server started.");
        try {
            while (!networkServer.isClosed()) {
                 NetworkHandler.NetworkConnection<Message> connection = networkServer.waitForConnection();

                 Runnable connectionHandler = new ServerConnectionHandler(connection, connections);
                 Thread connectionHandlerThread = new Thread(connectionHandler);
                 connectionHandlerThread.start();

                 System.out.println(String.format("Connected new Client %s with IP:Port <%s:%d>",
                     ((ServerConnectionHandler) connectionHandler).getUserName(),
                     connection.getRemoteHost(),
                     connection.getRemotePort()
                 ));
            }
        } catch(SocketException e) {
            System.out.println("Server connection terminated");
        }
        catch (IOException e) {
            System.err.println("Communication error " + e);
        }
        // close server
        System.out.println("Server Stopped.");
    }

    /**
     * This method closes the {@link ch.zhaw.pm2.multichat.protocol.NetworkHandler.NetworkServer}.
     */
    public void terminate() {
        try {
            System.out.println("Close server port.");
            networkServer.close();
        } catch (IOException e) {
            System.err.println("Failed to close server connection: " + e);
        }
    }

}
