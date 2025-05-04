package LWJG.core.monitor;

public class Watchdog extends Thread {
    private volatile long last;
    private final int msInterval;

    public Watchdog(int msInterval) {
        super();
        this.msInterval = msInterval*1000;
    }

    public void kick() {
        this.last = System.currentTimeMillis();
    }

    @Override
    public void run() {
        while(true)
            if(System.currentTimeMillis() - last > msInterval) {
                System.err.println("Watchdog timeout!");
                System.exit(1);
            }
    }
}