package LWJG.net;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocket;

/**
 * Client Manager Class to manage all current Clients connected to the Server
 * {@link #clients} All currently connected clients
 * {@link #blacklist} All currently blacklisted clients (aborted upon connection attempt)
 * {@link #defaultPerm} The default permission all clients should recieve
 * {@link #clientConnectedListeners} All Event Listeners that will be invoked once a new client has connected
 * @author Marius Baumgartner
 * @version 2025-05-03T3:58
 * @side Server
 */
public class ClientManager {
    private List<Client> clients;
    private List<String> blacklist;
    private ClientPermission defaultPerm;

    private final List<ClientConnectedListener> clientConnectedListeners = new ArrayList<>();

    public ClientManager(ClientPermission permission) {
        this.clients = new ArrayList<>();
        this.blacklist = new ArrayList<>();
        this.defaultPerm = permission;
    }

    /**
     * Adds a new Client Connected Listener
     * @param listener Listener
     */
    public void addClientConnectedListener(ClientConnectedListener listener) {
        clientConnectedListeners.add(listener);
    }

    /**
     * Removes a new Client Connected Listener
     * @param listener Listener
     */
    public void removeClientConnectedListener(ClientConnectedListener listener) {
        clientConnectedListeners.remove(listener);
    }

    /**
     * Get current Clients
     * @return List<Client> clients
     */
    public List<Client> getClients() {
        return clients;
    }

    public Client add(SSLSocket c) {
        if(true || !clients.stream().anyMatch(client -> client.getSocket().getInetAddress().getHostAddress().equals(c.getInetAddress().getHostAddress()))) {
            Client client = new Client(c, defaultPerm, 1024);
            clients.add(client);
            for (ClientConnectedListener listener : clientConnectedListeners) {
                listener.onClientConnected(client);
            }
            return client;
        }
        return null;
    }

    /**
     * Broadcast raw bytes to all Clients
     * @param opcode Listener Opcode
     * @param b Bytes
     */
    public void broadcast(byte opcode, byte[] b) {
        for (Client c : clients) {
            c.send(opcode, b);
        }
    }

    /**
     * Get Client by String ip representation
     * @param ip IP, e.g. 192.168.178.1
     * @return Client
     */
    public Client get(String ip) {
        for(Client c : clients)
            if(c.getSocket().getInetAddress().getHostAddress().equals(ip)) {
                return c;
            }
        return null;
    }

    /**
     * Close Client by IP
     * @param ip IP, e.g. 192.168.178.1
     * @return Closing Sucessful
     */
    public boolean close(String ip) {
        Client c = get(ip);
        if(c != null) { c.close(); clients.remove(c); return true; } return false;
    }

    /**
     * Checks if a client is blacklisted
     * @param ip Client ip
     * @return Is blocked
     */
    public boolean isBlocked(String ip) {
        return blacklist.contains(ip);
    }

    /**
     * Add Client to blacklist
     * @param ip
     */
    public void block(String ip) {
        if(!blacklist.contains(ip)) blacklist.add(ip);
    }

    /**
     * Remove Client from blacklist
     * @param ip
     */
    public void unblock(String ip) {
        if(blacklist.contains(ip)) blacklist.remove(ip);
    }

    /**
     * Close all clients
     */
    public void closeAllClients() {
        for(Client c : clients) c.close();
    }
}