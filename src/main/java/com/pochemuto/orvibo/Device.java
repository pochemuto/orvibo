package com.pochemuto.orvibo;

import com.pochemuto.orvibo.api.message.MacAddress;

import lombok.Data;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 04.11.2016
 */
@Data
public class Device {
    private final MacAddress mac;

    private boolean isOn;

    @Override
    public String toString() {
        return "Device [mac=" + mac + ", " + (isOn ? "ON" : "OFF") + ']';
    }
}
