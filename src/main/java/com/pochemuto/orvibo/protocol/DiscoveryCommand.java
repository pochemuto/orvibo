package com.pochemuto.orvibo.protocol;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 04.11.2016
 */
public class DiscoveryCommand {
    private byte[] mac;

    @NotNull
    public CommandId getCommand() {
        return CommandId.DISCOVERY;
    }

    public void setMacAddress(@Nullable  byte[] mac) {
        if (mac != null && mac.length != 6) {
            throw new IllegalArgumentException("wrong mac address size: " + mac.length);
        }
        this.mac = mac;
    }

    @Nullable
    public byte[] getMacAddress() {
        if (mac != null) {
            return mac.clone();
        } else {
            return null;
        }
    }
}
