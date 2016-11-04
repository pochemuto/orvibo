package com.pochemuto.orvibo.protocol;

import java.util.Arrays;

import static com.pochemuto.orvibo.protocol.Helpers.dump;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 02.11.2016
 */
public enum CommandId {
    DISCOVERY(0x71, 0x61), TEST(0x10, 0x1);

    private final byte[] bytes;
    private final int id;

    CommandId(int... bytes) {
        this.id = code(bytes);
        this.bytes = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            this.bytes[i] = (byte) bytes[i];

        }
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    public short getLength() {
        return (short) bytes.length;
    }

    public static CommandId fromBytes(byte[] bytes) {
        for (CommandId commandId : values()) {
            if (Arrays.equals(bytes, commandId.getBytes())) {
                return commandId;
            }
        }

        throw new RuntimeException("unknown command: " + dump(bytes));
    }

    private static int code(int... bytes) {
        int c = 0;
        for (int i = bytes.length - 1; i >= 0; i--) {
            c <<= 8;
            c += bytes[i];
        }
        return c;
    }

    public static void main(String... args) {
        System.out.println(TEST.id);
    }
}
