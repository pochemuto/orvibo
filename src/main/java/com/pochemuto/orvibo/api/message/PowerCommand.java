package com.pochemuto.orvibo.api.message;

import lombok.Data;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 08.11.2016
 */
@Data
public class PowerCommand {
    private final MacAddress macAddress;
    private boolean isOn;
}
