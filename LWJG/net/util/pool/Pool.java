package LWJG.net.util.pool;

import java.util.List;

import LWJG.net.util.Opcodes;

import java.util.ArrayList;
import static LWJG.net.util.bin.ByteWizard.toBytes;
import static LWJG.net.util.bin.ByteWizard.toInteger;

/**
 * Utility Class for syncing a byte pool across the network
 * {@link #pool} Byte pool that is synced
 * {@link #listeners} Listeners that are invoked when pool is updated
 * @author Marius Baumgartner
 * @version 2025-05-03T3:12
 */
public abstract class Pool {
    private byte[] pool;

    private final List<PoolEventListener> listeners = new ArrayList<>();

    public Pool(int size) {
        if(size < 1) throw new IllegalArgumentException("Size cannot be below 1.");
        this.pool = new byte[size];
    }

    /**
     * Add a new Pool event listener
     * @param listener New Listener
     */
    public void addListener(PoolEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a Pool event listener
     * @param listener Listener to be removed
     */
    public void removeListener(PoolEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Overwrite old data with new in the Pool
     * @param bytes Byte Data
     * @param offset The Offset of where to write the Byte Data
     */
    public final void write(byte[] bytes, int offset) {
        writeBytes(bytes, offset);
        invokeListeners();
        onLocalWrite(bytes, offset);
    }

    /**
     * Write Bytes to pool
     * @param bytes Byte Data
     * @param offset The Offset of where to write the Byte Data
     */
    protected void writeBytes(byte[] bytes, int offset) {
        if(bytes.length+offset>pool.length) throw new IllegalArgumentException("Out Of Range.");
        for(; offset < bytes.length; offset++) {
            pool[offset] = bytes[offset];
        }
    }

    /**
     * Invoke all attached Listeners
     */
    protected void invokeListeners() {
        for(PoolEventListener l : listeners) l.onEvent(pool);
    }

    /**
     * Sync byte pool update to all participants
     * @param bytes Byte Data
     * @param offset The Offset of where to write the Byte Data
     */
    protected void onLocalWrite(byte[] bytes, int offset) {
        byte[] offsetBytes = toBytes(offset);
        byte[] b = new byte[bytes.length+4];
        for(int i = 0; i < bytes.length; i++) b[i] = bytes[i];
        for(int i = 0; i < 4; i++) b[bytes.length+i] = offsetBytes[i];
        send(Opcodes.POOL.getOpcode(), b);
    }

    /**
     * Update byte pool after recieving from other participants
     * @param bytes Byte Data
     * @param offset The Offset of where to write the Byte Data
     */
    protected void onRemoteWrite(byte[] b) {
        byte[] tmp = new byte[4];
        byte[] bytes = new byte[b.length-4];
        for(int i = 0; i < 4; i++) tmp[i] = b[i];
        for(int i = 0; i < bytes.length; i++) bytes[i] = b[4+i];
        int offset = toInteger(tmp);
        writeBytes(bytes, offset);
    }

    /**
     * Send raw bytes
     * @param b Bytes
     */
    protected abstract void send(byte opcode, byte[] b);

    /**
     * Get Byte Pool
     * @return Byte Pool
     */
    public byte[] getPool() {
        return pool;
    }
}