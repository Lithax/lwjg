package LWJG.net;

import java.io.IOException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

/**
 * Utility Class for easy creation of a server with Clientmanager integretation
 * @author Marius Baumgartner
 * @version 2025-05-03T4:03
 * @side Server
 */
public class ServerHandler extends Thread {
    private final int port;
    private final int backlog;
    private volatile boolean running;
    private ClientManager mn;
    private SSLServerSocket serverSocket;

    static {
        System.setProperty("javax.net.ssl.keyStore", "server.keystore");
        System.setProperty("javax.net.ssl.keyStorePassword", "server.password");
    }

    public ServerHandler(int port, int backlog, ClientPermission defaultPermission) {
        this.port = port;
        this.backlog = backlog;
        this.running = true;
        mn = new ClientManager(defaultPermission);
    }

    @Override
    public void run() {
        try {
            serverSocket = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(port, backlog);
            serverSocket.setEnabledProtocols(new String[] { "TLSv1.3", "TLSv1.2" });
            while(running) {
                SSLSocket client = (SSLSocket) serverSocket.accept();
                if(!mn.isBlocked(client.getInetAddress().getHostAddress())) {
                    Client newClient = mn.add(client);
                    if (newClient != null) {
                        Thread.startVirtualThread(newClient);
                        System.out.println("Started virtual thread for client");
                    } else {
                        System.out.println("Failed to add client (duplicate or error");
                        if (!client.isClosed()) try { client.close(); } catch (IOException e) { /* ignore */ }
                    }
                } else System.out.println("Client connection was blocked.");
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            close();
        }
    }

    public void close() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
        mn.closeAllClients();
    }

    public ClientManager getClientManager() {
        return mn;
    }
}