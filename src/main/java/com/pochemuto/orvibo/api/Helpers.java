package com.pochemuto.orvibo.api;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 02.11.2016
 */
public class Helpers {
    private Helpers() {
    }

    public static byte[] bytes(int... ints) { // helper function
        byte[] result = new byte[ints.length];
        for (int i = 0; i < ints.length; i++) {
            result[i] = (byte) ints[i];
        }
        return result;
    }

    public static String dump(byte[] bytes) {
        return ByteBufUtil.hexDump(Unpooled.wrappedBuffer(bytes));
    }
}
