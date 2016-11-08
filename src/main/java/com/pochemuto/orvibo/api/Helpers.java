package com.pochemuto.orvibo.api;

import com.pochemuto.orvibo.api.message.MacAddress;

import org.jetbrains.annotations.NotNull;

import io.netty.buffer.ByteBuf;
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

    public static String dump(@NotNull byte[] bytes) {
        String hexDump = ByteBufUtil.hexDump(Unpooled.wrappedBuffer(bytes));
        StringBuilder sb = new StringBuilder(hexDump.length() / 2 - 1);
        sb.append(hexDump);
        for (int i = sb.length() - 2; i > 0; i -= 2) {
            sb.insert(i, ' ');
        }
        return sb.toString();
    }

    @NotNull
    public static byte[] reversed(@NotNull byte[] bytes) {
        byte[] result = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            result[bytes.length - i - 1] = bytes[i];
        }
        return result;
    }

    public static MacAddress readMacAddress(ByteBuf byteBuf) {
        byte[] bytes = new byte[6];
        byteBuf.readBytes(bytes);
        return new MacAddress(bytes);
    }
}
