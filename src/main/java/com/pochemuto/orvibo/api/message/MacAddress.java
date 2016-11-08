package com.pochemuto.orvibo.api.message;

import com.pochemuto.orvibo.api.Helpers;

import lombok.Data;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 05.11.2016
 */
@Data
public class MacAddress {
    private static final byte[] EMPTY_MAC = new byte[6];

    private final byte[] mac;

    private static final MacAddress EMPTY = new MacAddress(EMPTY_MAC);

    public static final byte[] PADDING = new byte[] {0x20, 0x20, 0x20, 0x20, 0x20, 0x20};

    public static MacAddress empty() {
        return EMPTY;
    }

    public static MacAddress fromString(String str) {
        str = str.replaceAll(" ", "");
        if (str.length() != 6 * 2) {
            throw new IllegalArgumentException("wrong hex length: " + str);
        }
        byte[] mac = new byte[6];
        for (int i = 0; i < mac.length; i++) {
            mac[i] = Integer.valueOf(str.substring(i * 2, i * 2 + 2), 16).byteValue();
        }
        return new MacAddress(mac);
    }

    public MacAddress(byte[] mac) {
        Objects.requireNonNull(mac);
        if (mac.length != 6) {
            throw new IllegalArgumentException("wrong mac address size: " + mac.length);
        }
        this.mac = mac;
    }

    public boolean isEmpty() {
        return Arrays.equals(mac, EMPTY_MAC);
    }

    @Override
    public String toString() {
        return Helpers.dump(mac);
    }
}
