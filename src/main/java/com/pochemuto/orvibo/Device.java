package com.pochemuto.orvibo;

import com.pochemuto.orvibo.api.Helpers;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 04.11.2016
 */
public class Device {
    private final byte[] mac;

    @Getter
    @Setter
    private boolean isOn;

    public Device(byte[] mac) {
        this.mac = mac;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return Arrays.equals(mac, device.mac);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(mac);
    }

    @Override
    public String toString() {
        return "Device [mac=" + Helpers.dump(mac) + ", " + (isOn ? "ON" : "OFF") + ']';
    }
}
