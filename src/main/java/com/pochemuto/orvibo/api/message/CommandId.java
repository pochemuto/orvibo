package com.pochemuto.orvibo.api.message;

import java.util.Arrays;

import static com.pochemuto.orvibo.api.Helpers.dump;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 02.11.2016
 */
public enum CommandId {
    DISCOVERY(0x71, 0x61), DISCOVERY_TARGET(0x71, 0x67), SUBSCRIBE(0x63, 0x6C), POWER(0x64, 0x63), POWER_STATE(0x73, 0x66);

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

        throw new RuntimeException("unknown message: " + dump(bytes));
    }

    private static int code(int... bytes) {
        int c = 0;
        for (int i = bytes.length - 1; i >= 0; i--) {
            c <<= 8;
            c += bytes[i];
        }
        return c;
    }
}
