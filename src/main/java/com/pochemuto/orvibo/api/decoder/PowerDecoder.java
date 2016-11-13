package com.pochemuto.orvibo.api.decoder;

import com.pochemuto.orvibo.api.Helpers;
import com.pochemuto.orvibo.api.message.CommandId;
import com.pochemuto.orvibo.api.message.MacAddress;
import com.pochemuto.orvibo.api.message.Message;
import com.pochemuto.orvibo.api.message.PowerResponse;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 13.11.2016
 */
public class PowerDecoder extends MessageToMessageDecoder<Message> {

    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        return super.acceptInboundMessage(msg) && ((Message) msg).getCommandId() == CommandId.POWER_STATE;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        ByteBuf buf = Unpooled.wrappedBuffer(msg.getBytes());
        MacAddress macAddress = Helpers.readMacAddress(buf);
        buf.skipBytes(6); // padding
        buf.skipBytes(4); // unknown
        boolean isOn = buf.readBoolean();
        out.add(new PowerResponse(macAddress, isOn));
    }
}
