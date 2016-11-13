package com.pochemuto.orvibo;

import com.pochemuto.orvibo.api.message.MacAddress;

import lombok.Data;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 04.11.2016
 */
@Data
public class Device {

    private final MacAddress macAddress;

    private transient boolean isOn;

    @Override
    public String toString() {
        return "Device [macAddress=" + macAddress + ", " + (isOn ? "ON" : "OFF") + ']';
    }
}
