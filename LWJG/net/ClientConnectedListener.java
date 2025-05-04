package LWJG.net;

/**
 * Listener for when a Client connects
 * @author Marius Baumgartner
 * @version 2025-05-03T3:47
 */
public interface ClientConnectedListener {
    /**
     * Gets invoked once the ClientManager establishes a new client connection
     * @param newClient The new client
     */
    void onClientConnected(Client newClient);
}