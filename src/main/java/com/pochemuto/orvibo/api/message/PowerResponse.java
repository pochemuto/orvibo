package com.pochemuto.orvibo.api.message;

import lombok.Data;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 13.11.2016
 */
@Data
public class PowerResponse {
    private final MacAddress macAddress;
    private final boolean isOn;
}
