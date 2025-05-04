package LWJG.net;

import javax.net.ssl.SSLSocket;

/**
 * Server Client Object, abstracts client sockets for easier handling
 * {@link #listeners} Includes a basic listener Map that lets utilities easiely interface with Client requests to the Server
 * <br><b>WARNING: Only 256 Possible Opcodes for now!</b>
 * @author Marius Baumgartner
 * @version 2025-05-03T1:28
 * @side Server
 */
public class Client extends Dispatcher {
    private ClientPermission permission;

    public Client(SSLSocket socket, ClientPermission perm, int bufsize) {
        super(socket, bufsize);
        this.permission = perm;
    }

    /**
     * Get Permissions
     * @return Permissions
     */
    public ClientPermission getPermission() {
        return permission;
    }

    /**
     * Set Permissions
     * @param permission New Permission
     */
    public void setPermission(ClientPermission permission) {
        this.permission = permission;
    }
}