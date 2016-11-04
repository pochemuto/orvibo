package com.pochemuto.orvibo.api.encoder;

import com.pochemuto.orvibo.api.message.Message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 04.11.2016
 */
@Slf4j
public class MessageEncoder extends MessageToMessageEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes(Message.MAGIC);
        buf.writeShort(msg.getLength());
        buf.writeBytes(msg.getCommandId().getBytes());
        buf.writeBytes(msg.getBytes());
        out.add(buf);
    }
}
