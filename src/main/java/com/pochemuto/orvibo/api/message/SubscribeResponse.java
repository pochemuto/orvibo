package com.pochemuto.orvibo.api.message;

import lombok.Data;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 05.11.2016
 */
@Data
public class SubscribeResponse {
    private MacAddress macAddress;
    private boolean success;
}
