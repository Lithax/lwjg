package LWJG.test; // Or your test package

import LWJG.net.*;
import LWJG.core.monitor.Watchdog; // Assuming Watchdog is generally useful

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class NetPerformanceTest {

    // --- Configuration ---
    private static final int TEST_PORT = 12345;
    private static final int NUM_CLIENTS = 100; // Number of concurrent clients
    private static final int MESSAGES_PER_CLIENT_PER_SEC = 50; // Target send rate per client
    private static final int TEST_DURATION_SECONDS = 30;
    private static final int CLIENT_BUFFER_SIZE = 1024;
    private static final int SERVER_BUFFER_SIZE = 1024;
    private static final int HEAVY_TASK_SLEEP_MS = 20; // Simulate work in heavy listener

    // --- Opcodes ---
    private static final byte OP_PING = 1;
    private static final byte OP_PONG = 2;
    private static final byte OP_HEAVY_REQ = 3;
    private static final byte OP_HEAVY_RESP = 4;

    // --- Data Collection ---
    private static final AtomicLong serverMessagesReceived = new AtomicLong(0);
    private static final AtomicLong clientMessagesSent = new AtomicLong(0);
    private static final AtomicLong clientPongsReceived = new AtomicLong(0);
    private static final AtomicLong clientHeavyResponsesReceived = new AtomicLong(0);
    // Queue to store round-trip times in nanoseconds
    private static final Queue<Long> latenciesNanos = new ConcurrentLinkedQueue<>();

    // --- Synchronization ---
    private static final CountDownLatch clientsReadyLatch = new CountDownLatch(NUM_CLIENTS);
    private static final CountDownLatch testEndLatch = new CountDownLatch(1);
    private static final AtomicBoolean testRunning = new AtomicBoolean(false);


    public static void main(String[] args) throws Exception {
        System.out.println("Starting Network Performance Test...");
        System.setProperty("javax.net.ssl.keyStore", "server.keystore"); // Make sure these exist
        System.setProperty("javax.net.ssl.keyStorePassword", "server.password");
        // Client needs trust store if server cert isn't globally trusted
        System.setProperty("javax.net.ssl.trustStore", "server.keystore");
        System.setProperty("javax.net.ssl.trustStorePassword", "server.password");


        // --- Server Setup ---
        System.out.println("Setting up server...");
        ServerHandler serverHandler = new ServerHandler(TEST_PORT, 100, ClientPermission.READWRITE);
        // Add server listeners
        serverHandler.getClientManager().addClientConnectedListener(client -> {
            System.out.println("Server: Client connected " + client.getSocket().getRemoteSocketAddress());
            // Listener for PING from client
            client.addEventListener(OP_PING, new IOEventListener() {
                @Override
                public void onEvent(Dispatcher dispatcher, byte[] payload) {
                    serverMessagesReceived.incrementAndGet();
                    // Send PONG back with original timestamp payload (if any)
                    dispatcher.send(OP_PONG, payload);
                }
                @Override public boolean isHeavyTask() { return false; }
            });
            // Listener for HEAVY_REQ from client
            client.addEventListener(OP_HEAVY_REQ, new IOEventListener() {
                @Override
                public void onEvent(Dispatcher dispatcher, byte[] payload) {
                    serverMessagesReceived.incrementAndGet();
                    // Simulate work
                    try { Thread.sleep(HEAVY_TASK_SLEEP_MS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    // Send response
                    dispatcher.send(OP_HEAVY_RESP, payload); // Echo back payload (e.g., request ID)
                }
                @Override public boolean isHeavyTask() { return true; } // Mark as heavy
            });
        });
        serverHandler.start(); // Start acceptor thread
        System.out.println("Server started on port " + TEST_PORT);

        // --- Client Setup ---
        System.out.println("Setting up " + NUM_CLIENTS + " clients...");
        ExecutorService clientExecutor = Executors.newVirtualThreadPerTaskExecutor();
        List<RemoteServer> clients = new CopyOnWriteArrayList<>(); // Store client connections
        List<ClientTask> clientTasks = new ArrayList<>();

        for (int i = 0; i < NUM_CLIENTS; i++) {
            final int clientId = i;
            clientExecutor.submit(() -> {
                RemoteServer client = null;
                try {
                    client = new RemoteServer("localhost", TEST_PORT, CLIENT_BUFFER_SIZE);
                    clients.add(client);

                    // Add client listeners
                    final RemoteServer finalClient = client; // For lambda
                    client.addEventListener(OP_PONG, new IOEventListener() {
                        @Override
                        public void onEvent(Dispatcher dispatcher, byte[] payload) {
                            clientPongsReceived.incrementAndGet();
                            if (payload != null && payload.length == 8) {
                                long sendTimeNanos = bytesToLong(payload); // Decode timestamp
                                long latency = System.nanoTime() - sendTimeNanos;
                                latenciesNanos.add(latency);
                            }
                        }
                        @Override public boolean isHeavyTask() { return false; }
                    });
                     client.addEventListener(OP_HEAVY_RESP, new IOEventListener() {
                        @Override
                        public void onEvent(Dispatcher dispatcher, byte[] payload) {
                            clientHeavyResponsesReceived.incrementAndGet();
                            // Potentially check payload matches request ID
                        }
                        @Override public boolean isHeavyTask() { return false; }
                    });

                    // Start the client's read loop in a separate VT
                    Thread readerThread = Thread.startVirtualThread(client);
                    System.out.println("Client " + clientId + " connected and reader started.");

                    // Create and store the task logic
                    ClientTask task = new ClientTask(clientId, finalClient);
                    clientTasks.add(task); // Store task
                    clientsReadyLatch.countDown(); // Signal this client is ready
                    task.run(); // Run the sending loop

                } catch (IOException e) {
                    System.err.println("Client " + clientId + " failed to connect or run: " + e.getMessage());
                    if (client != null) clients.remove(client);
                    clientsReadyLatch.countDown(); // Ensure latch decreases even on error
                }
            });
        }

        // Wait for all clients to be ready (or fail)
        System.out.println("Waiting for clients to connect...");
        if (!clientsReadyLatch.await(30, TimeUnit.SECONDS)) { // Timeout
             System.err.println("Timeout waiting for clients to connect!");
             // Handle incomplete setup if necessary
        }
        System.out.println(clients.size() + " clients connected. Starting test phase...");


        // --- Run Test ---
        testRunning.set(true);
        long testStartTime = System.nanoTime();
        long testEndTime = testStartTime + TimeUnit.SECONDS.toNanos(TEST_DURATION_SECONDS);

        // Keep main thread alive while test runs
        try {
            testEndLatch.await(TEST_DURATION_SECONDS + 5, TimeUnit.SECONDS); // Wait for duration + buffer
        } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
             System.err.println("Main thread interrupted.");
        }
        testRunning.set(false); // Signal clients to stop sending
        long actualTestDurationNanos = System.nanoTime() - testStartTime;
        System.out.println("Test duration finished.");


        // --- Shutdown ---
        System.out.println("Stopping clients...");
        clientExecutor.shutdown(); // Signal executor no new tasks
        for (RemoteServer client : clients) {
            client.close(); // Close connections
        }
        try {
            if (!clientExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                System.err.println("Client executor did not terminate gracefully.");
                clientExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
             clientExecutor.shutdownNow();
             Thread.currentThread().interrupt();
        }


        System.out.println("Stopping server...");
        serverHandler.close(); // Stops acceptor and closes clients
        serverHandler.join(5000); // Wait for server acceptor thread to die
        System.out.println("Shutdown complete.");

        // --- Results ---
        printResults(actualTestDurationNanos);
    }


    // --- Client Task Logic ---
    static class ClientTask implements Runnable {
        private final int id;
        private final RemoteServer client;
        private final long sendIntervalNanos;

        ClientTask(int id, RemoteServer client) {
            this.id = id;
            this.client = client;
            this.sendIntervalNanos = TimeUnit.SECONDS.toNanos(1) / MESSAGES_PER_CLIENT_PER_SEC;
        }

        @Override
        public void run() {
            while (!testRunning.get()) {
                try {
                    Thread.sleep(50); // Wait actively
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("ClientTask " + id + " interrupted before starting.");
                    return; // Exit if interrupted while waiting
                }
            }
            System.out.println("ClientTask " + id + " starting send loop.");
            long lastSendTime = System.nanoTime();
            int msgCounter = 0;
            while (testRunning.get() && !client.getSocket().isClosed()) {
                long now = System.nanoTime();
                if (now - lastSendTime >= sendIntervalNanos) {
                    try {
                        // Send a mix of messages
                        if (msgCounter % 5 == 0) { // Send heavy request periodically
                            byte[] reqId = longToBytes(System.nanoTime()); // Use timestamp as simple ID
                            client.send(OP_HEAVY_REQ, reqId);
                        } else { // Send ping
                            byte[] timestamp = longToBytes(System.nanoTime());
                            client.send(OP_PING, timestamp);
                        }
                        clientMessagesSent.incrementAndGet();
                        lastSendTime = now;
                        msgCounter++;
                    } catch (Exception e) { // Catch errors during send
                        System.err.println("Client " + id + " error sending: " + e.getMessage());
                        // Optionally break or slow down on errors
                    }
                }

                // Small sleep to prevent busy-waiting when interval is large
                long sleepTime = TimeUnit.NANOSECONDS.toMillis(sendIntervalNanos - (System.nanoTime() - lastSendTime)) -1;
                 if (sleepTime > 0) {
                    try { Thread.sleep(sleepTime); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                 } else {
                    Thread.yield(); // Yield if we are behind schedule
                 }
            }
            System.out.println("ClientTask " + id + " exiting send loop.");
        }
    }

    // --- Utility Methods ---
    private static byte[] longToBytes(long l) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(l);
        return buffer.array();
    }

    private static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes, 0, Long.BYTES); // Ensure exactly 8 bytes are read
        buffer.flip();
        return buffer.getLong();
    }

    private static void printResults(long durationNanos) {
        double durationSec = durationNanos / 1_000_000_000.0;
        long totalPongs = clientPongsReceived.get();
        long totalHeavy = clientHeavyResponsesReceived.get();
        long totalSent = clientMessagesSent.get();
        long totalReceivedServer = serverMessagesReceived.get();

        System.out.println("\n--- Test Results ---");
        System.out.printf("Duration: %.2f seconds\n", durationSec);
        System.out.printf("Clients: %d, Msg Rate/Client: %d/s\n", NUM_CLIENTS, MESSAGES_PER_CLIENT_PER_SEC);
        System.out.printf("Target Total Send Rate: %d/s\n", NUM_CLIENTS * MESSAGES_PER_CLIENT_PER_SEC);
        System.out.printf("Actual Total Messages Sent (Client): %d (%.2f/s)\n", totalSent, totalSent / durationSec);
        System.out.printf("Total Messages Received (Server): %d (%.2f/s)\n", totalReceivedServer, totalReceivedServer / durationSec);
        System.out.printf("Client PONGs Received: %d\n", totalPongs);
        System.out.printf("Client HEAVY_RESP Received: %d\n", totalHeavy);


        // Latency Analysis
        if (!latenciesNanos.isEmpty()) {
            long minLatency = Long.MAX_VALUE;
            long maxLatency = Long.MIN_VALUE;
            long sumLatency = 0;
            int count = latenciesNanos.size(); // Get size before potentially clearing

            // Calculate min, max, sum using stream for conciseness
             minLatency = latenciesNanos.stream().min(Long::compare).orElse(0L);
             maxLatency = latenciesNanos.stream().max(Long::compare).orElse(0L);
             // Summing can be large, careful with overflow if test runs extremely long
             // Using reduce is safer than mapToLong().sum() for potential overflow
             sumLatency = latenciesNanos.stream().reduce(0L, Long::sum);


            double avgLatency = (double) sumLatency / count;

            System.out.println("\n--- Latency (Round Trip Nanos) ---");
            System.out.printf("Samples: %d\n", count);
            System.out.printf("Min Latency: %,d ns (%.3f ms)\n", minLatency, minLatency / 1_000_000.0);
            System.out.printf("Max Latency: %,d ns (%.3f ms)\n", maxLatency, maxLatency / 1_000_000.0);
            System.out.printf("Avg Latency: %,.0f ns (%.3f ms)\n", avgLatency, avgLatency / 1_000_000.0);

            // Optional: Percentiles (requires sorting, more complex)
            // List<Long> sortedLatencies = new ArrayList<>(latenciesNanos);
            // Collections.sort(sortedLatencies);
            // long p95 = sortedLatencies.get((int)(count * 0.95));
            // long p99 = sortedLatencies.get((int)(count * 0.99));
            // System.out.printf("P95 Latency: %,d ns (%.3f ms)\n", p95, p95 / 1_000_000.0);
            // System.out.printf("P99 Latency: %,d ns (%.3f ms)\n", p99, p99 / 1_000_000.0);

        } else {
            System.out.println("\n--- Latency (Round Trip Nanos) ---");
            System.out.println("No latency samples recorded (no PONGs received?).");
        }
        System.out.println("--------------------");
    }
}