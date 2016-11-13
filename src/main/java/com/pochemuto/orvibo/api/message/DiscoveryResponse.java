package com.pochemuto.orvibo.api.message;

import lombok.Data;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 04.11.2016
 */
@Data
public class DiscoveryResponse {
    private MacAddress macAddress = MacAddress.empty();

    private String text;

    private boolean isOn;

    private long time;
}
