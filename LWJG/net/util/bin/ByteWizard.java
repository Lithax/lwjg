package LWJG.net.util.bin;

public class ByteWizard {
    private byte[] bytes;

    public static byte[] toBytes(int integer) {
        byte[] result = new byte[4];
        result[0] = (byte) ((integer >> 24) & 0xFF);
        result[1] = (byte) ((integer >> 16) & 0xFF);
        result[2] = (byte) ((integer >> 8) & 0xFF);
        result[3] = (byte) (integer & 0xFF);
        return result;
    }

    public static byte[] toBytes(double _double) {
        long bits = Double.doubleToLongBits(_double);
        return toBytes(bits);
    }

    public static byte[] toBytes(long _long) {
        byte[] result = new byte[8];
        result[0] = (byte) ((_long >> 56) & 0xFF);
        result[1] = (byte) ((_long >> 48) & 0xFF);
        result[2] = (byte) ((_long >> 40) & 0xFF);
        result[3] = (byte) ((_long >> 32) & 0xFF);
        result[4] = (byte) ((_long >> 24) & 0xFF);
        result[5] = (byte) ((_long >> 16) & 0xFF);
        result[6] = (byte) ((_long >> 8) & 0xFF);
        result[7] = (byte) (_long & 0xFF);
        return result;
    }

    public static byte[] toBytes(short _short) {
        byte[] result = new byte[2];
        result[0] = (byte) ((_short >> 8) & 0xFF);
        result[1] = (byte) (_short & 0xFF);
        return result;
    }

    public static byte[] toBytes(float _float) {
        int bits = Float.floatToIntBits(_float);
        return toBytes(bits);
    }

    public static int toInteger(byte[] bytes) {
        valid(bytes, 4);
        return ((bytes[0] & 0xFF) << 24) |
               ((bytes[1] & 0xFF) << 16) |
               ((bytes[2] & 0xFF) << 8) |
               (bytes[3] & 0xFF);
    }

    public static double toDouble(byte[] bytes) {
        valid(bytes, 8);
        long bits = toLong(bytes);
        return Double.longBitsToDouble(bits);
    }

    public static long toLong(byte[] bytes) {
        valid(bytes, 8);
        return ((long)(bytes[0] & 0xFF) << 56) |
               ((long)(bytes[1] & 0xFF) << 48) |
               ((long)(bytes[2] & 0xFF) << 40) |
               ((long)(bytes[3] & 0xFF) << 32) |
               ((long)(bytes[4] & 0xFF) << 24) |
               ((long)(bytes[5] & 0xFF) << 16) |
               ((long)(bytes[6] & 0xFF) << 8) |
               (bytes[7] & 0xFF);
    }

    public static short toShort(byte[] bytes) {
        valid(bytes, 2);
        return (short) (((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF));
    }

    public static float toFloat(byte[] bytes) {
        valid(bytes, 4);
        int bits = toInteger(bytes);
        return Float.intBitsToFloat(bits);
    }

    private static void valid(byte[] bytes, int len) {
        if (bytes == null || bytes.length < len) 
            throw new IllegalArgumentException("Byte array must have at least " + len + " elements");
    }

    public ByteWizard(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] subBytes(int start, int end) {
        if(start < 0 || start > end || end > length()) throw new IllegalArgumentException("Invalid.");
        byte[] sub = new byte[end-start];
        for(int i = 0; i < sub.length; i++) {
            sub[i] = bytes[i+start];
        }
        return sub;
    }

    public byte indexOf(int i) {
        if(i < 0 || i > length()-1) throw new IllegalArgumentException("Invalid.");
        return bytes[i];
    }

    public int length() {
        return bytes.length;
    }
}