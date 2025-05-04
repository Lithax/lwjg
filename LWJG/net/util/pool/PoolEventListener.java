package LWJG.net.util.pool;

/**
 * Event that is invoked when Pool data updates
 * @author Marius Baumgartner
 * @version 2025-05-03T1:30
 */
public interface PoolEventListener {
    /**
     * Event that is invoked by the Pool Manager
     * @param newPoolData New Pool Data
     */
    void onEvent(byte[] newPoolData);
}