package com.pochemuto.orvibo.api.message;

import org.jetbrains.annotations.NotNull;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 04.11.2016
 */
public class SubscribeCommand {
    private byte[] mac;

    public void setMacAddress(@NotNull byte[] mac) {
        if (mac.length != 6) {
            throw new IllegalArgumentException("wrong mac address size: " + mac.length);
        }
        this.mac = mac;
    }

    @NotNull
    public byte[] getMacAddress() {
        return mac.clone();
    }
}
