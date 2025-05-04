package LWJG.net.util.pool;

/**
 * Utility Class for syncing with a syncronized byte server pool across the network
 * @author Marius Baumgartner
 * @version 2025-05-03T1:28
 * @side Client
 */
public class ClientPool extends Pool {
    public ClientPool(int size) {
        super(size);
    }

    @Override
    protected void send(byte opcode, byte[] b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'send'");
    }
}