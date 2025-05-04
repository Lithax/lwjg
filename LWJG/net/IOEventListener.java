package LWJG.net;

/**
 * IO Event Listener for when something is recieved on a socket
 * @author Marius Baumgartner
 * @version 2025-05-03T14:33
 */
public interface IOEventListener {
    /**
     * The event the will be invoked
     * @param dispatcher Dispatcher
     * @param payload Byte Payload
     */
    void onEvent(Dispatcher dispatcher, byte[] payload); /* Typecast to (Client) if needed */

    /**
     * If the Event listener process is heavy, marking it as heavy will have it started as a virtual thread, this stops possible blocking
     * @return Heavy
     */
    boolean isHeavyTask();
}