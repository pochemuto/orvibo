package com.pochemuto.orvibo.api.decoder;

import com.pochemuto.orvibo.api.message.CommandId;
import com.pochemuto.orvibo.api.message.Message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 04.11.2016
 */
@Slf4j
public class MessageDecoder extends MessageToMessageDecoder<DatagramPacket> {

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
        Message message = new Message();
        ByteBuf content = msg.content();
        ByteBuf magicBytes = content.readBytes(2);
        if (!magicBytes.equals(Unpooled.wrappedBuffer(Message.MAGIC))) {
            throw new RuntimeException("Message doesn't contain magic number");
        }

        int length = content.readShort();
        ByteBuf commandBytes = content.readBytes(2);

        message.setCommandId(CommandId.fromBytes(ByteBufUtil.getBytes(commandBytes)));

        message.setBytes(new byte[length - message.getHeaderLength()]);
        content.readBytes(message.getBytes());
        out.add(message);
    }

}
