package LWJG.net;

import java.io.IOException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Client side of the Client Server interaction
 * {@link #listeners} Includes a basic listener Map that lets utilities easiely interface with Client requests to the Server
 * <br><b>WARNING: Only 256 Possible Opcodes for now!</b>
 * @author Marius Baumgartner
 * @version 2025-05-03T1:28
 * @side Client
 */
public class RemoteServer extends Dispatcher {
    private final int port;
    private final String ip;

    public RemoteServer(String ip, int port, int bufsize) throws IOException {
        super(createSocket(ip, port), bufsize);
        this.port = port;
        this.ip = ip;
    }

    private static SSLSocket createSocket(String ip, int port) throws IOException {
        SSLSocket s = (SSLSocket) SSLSocketFactory.getDefault().createSocket(ip, port);
        s.setEnabledProtocols(new String[] { "TLSv1.3", "TLSv1.2" });
        return s;
    }

    public String getIP() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}