package com.pochemuto.orvibo.api.message;

import lombok.Data;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 04.11.2016
 */
@Data
public class SubscribeCommand {
    private final MacAddress macAddress;
}
