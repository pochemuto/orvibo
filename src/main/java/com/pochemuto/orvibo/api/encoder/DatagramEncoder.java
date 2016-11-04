package com.pochemuto.orvibo.api.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 04.11.2016
 */
@Slf4j
public class DatagramEncoder extends MessageToMessageEncoder<ByteBuf> {

    private InetSocketAddress recipient;

    public DatagramEncoder(InetSocketAddress recipient) {
        this.recipient = recipient;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        out.add(new DatagramPacket(msg.copy(), recipient));
        log.info("message was send: " + ByteBufUtil.hexDump(msg));
    }
}
