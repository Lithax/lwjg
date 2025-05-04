package LWJG.net.util.pool;

import LWJG.net.Client;
import LWJG.net.ClientConnectedListener;
import LWJG.net.ClientManager;
import LWJG.net.Dispatcher;
import LWJG.net.IOEventListener;
import LWJG.net.util.Opcodes;

/**
 * Utility Class for sharing a syncronized byte pool across the network with multiple clients
 * @author Marius Baumgartner
 * @version 2025-05-03T1:28
 * @side Server
 */
public class ServerPool extends Pool {
    private ClientManager mn;

    public ServerPool(int size, ClientManager mn) {
        super(size);
        this.mn = mn;
        for(Client c : mn.getClients()) c.addEventListener(Opcodes.POOL.getOpcode(), new IOEventListener() {
            @Override
            public void onEvent(Dispatcher client, byte[] payload) {
                onRemoteWrite(payload);
            }

            @Override
            public boolean isHeavyTask() {
                return false;
            }
        });
        mn.addClientConnectedListener(new ClientConnectedListener() {
            @Override
            public void onClientConnected(Client newClient) {
                newClient.addEventListener(Opcodes.POOL.getOpcode(), new IOEventListener() {
                    @Override
                    public void onEvent(Dispatcher client, byte[] payload) {
                        onRemoteWrite(payload);
                    }

                    @Override
                    public boolean isHeavyTask() {
                        return false;
                    }
                });
            }
        });
    }

    @Override
    protected void send(byte opcode, byte[] b) {
        mn.broadcast(opcode, b);
    }
}