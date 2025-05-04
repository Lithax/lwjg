package LWJG.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static LWJG.net.util.bin.ByteWizard.toBytes;
import static LWJG.net.util.bin.ByteWizard.toInteger;

import javax.net.ssl.SSLSocket;

/**
 * Dispatcher Object to abstract sending and recieving
 * {@link #listeners} Includes a basic listener Map that lets utilities easiely interface with Client requests to the Server
 * <br><b>WARNING: Only 256 Possible Opcodes for now!</b>
 * @author Marius Baumgartner
 * @version 2025-05-03T1:28
 */
public class Dispatcher implements Runnable {
    private final SSLSocket socket;
    private final byte[] buffer;
    private volatile boolean running;

    private final Map<Byte, List<IOEventListener>> listeners = new ConcurrentHashMap<>();

    private static final ExecutorService backgroundTaskExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public Dispatcher(SSLSocket socket, int bufsize) {
        this.running = true;
        this.buffer = new byte[bufsize];
        this.socket = socket;
    }

    /**
     * Add new Event Listener
     * @param opcode Opcode to be invoked by
     * @param l Listener
     */
    public void addEventListener(byte opcode, IOEventListener l) {
        listeners
          .computeIfAbsent(opcode, k -> new CopyOnWriteArrayList<>())
          .add(l);
    }

    /**
     * Remove a existing Event Listener
     * @param opcode Opcode to be invoked by
     * @param l Listener
     */
    public void removeEventListener(byte opcode, IOEventListener l) {
        List<IOEventListener> lst = listeners.get(opcode);
        if (lst != null) lst.remove(l);
    }

    @Override
    public void run() {
        int payloadLen;
        byte code;
        int chunkLen;
        int readSoFar;
        int toRead;
        int r;
        try (InputStream in = socket.getInputStream()) {
            int len;
            while ((len = in.read(buffer)) != -1 && running) {
                if (len < 5) continue;
                code = buffer[0];
                payloadLen = toInteger(new byte[]{buffer[1], buffer[2], buffer[3], buffer[4]});

                ByteArrayOutputStream stream = new ByteArrayOutputStream(payloadLen);

                chunkLen = len - 5;
                if (chunkLen > 0) {
                    stream.write(buffer, 5, chunkLen);
                }

                readSoFar = chunkLen;
                while (readSoFar < payloadLen) {
                    toRead = Math.min(buffer.length, payloadLen - readSoFar);
                    r = in.read(buffer, 0, toRead);
                    if (r == -1) throw new IOException("Stream closed prematurely");
                    stream.write(buffer, 0, r); // only writes actual read instead of entire buffer
                    readSoFar += r;
                }

                final byte[] payload = stream.toByteArray();

                List<IOEventListener> lst = listeners.get(code);

                if (lst != null) 
                    for (IOEventListener l : lst) 
                        if(l.isHeavyTask())
                            backgroundTaskExecutor.submit(() -> {
                                try { l.onEvent(this, payload); } catch (Exception e) { /* handle */ }
                            });
                        else l.onEvent(this, payload);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    /**
     * Send a payload to Client
     * @param b payload
     * @param opcode opcode
     */
    public void send(byte opcode, byte[] b) {
        byte[] tmp = new byte[b.length+5];
        tmp[0] = opcode;
        byte[] len = toBytes(b.length);
        tmp[1] = len[0];
        tmp[2] = len[1];
        tmp[3] = len[2];
        tmp[4] = len[3];
        for(int i = 0; i < b.length; i++) tmp[5+i] = b[i];
        send(tmp);
    }

    /**
     * Send raw bytes to Client
     * WARNING: Does not account for opcode or len
     * @param b Bytes to be send
     */
    public void send(byte[] b) {
        try {
            socket.getOutputStream().write(b);
            socket.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get Client Socket
     * @return Socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * <b>Close Client</b>
     * Warning: After close, Client Object should be removed, as it is now unusable
     */
    public void close() {
        if(!running) return;
        try {
            running = false;
            if(socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.out.println("Error Closing Client: "+e.getMessage());
        }
    }
}