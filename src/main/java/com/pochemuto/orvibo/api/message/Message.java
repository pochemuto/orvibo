package com.pochemuto.orvibo.api.message;

import org.jetbrains.annotations.NotNull;

import lombok.Data;

import static com.pochemuto.orvibo.api.Helpers.bytes;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 04.11.2016
 */
@Data
public class Message {
    public static final byte[] MAGIC = bytes(0x68, 0x64);

    private CommandId commandId;

    @NotNull
    private byte[] bytes = new byte[0];

    public int getLength() {
        return getHeaderLength() + getBytes().length;
    }

    public int getHeaderLength() {
        return MAGIC.length + this.getCommandId().getLength() + 2;
    }
}
