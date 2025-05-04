package LWJG.net.util;

/**
 * Opcode table for all utilities
 * <b>WARNING: Java bytes are signed, so when adding custom opcodes, make sure its -127 - 128</b>
 * @author Marius Baumgartner
 * @version 2025-05-03T1:28
 */
public enum Opcodes {
    POOL((byte)-127), /* Pool Util Opcode */
    ;

    private final byte opcode;

    Opcodes(byte opcode) {
        this.opcode = opcode;
    }

    public byte getOpcode() {
        return opcode;
    }
}