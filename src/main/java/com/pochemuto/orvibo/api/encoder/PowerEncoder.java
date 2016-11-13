package com.pochemuto.orvibo.api.encoder;

import com.pochemuto.orvibo.api.message.CommandId;
import com.pochemuto.orvibo.api.message.MacAddress;
import com.pochemuto.orvibo.api.message.Message;
import com.pochemuto.orvibo.api.message.PowerCommand;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 08.11.2016
 */
public class PowerEncoder extends MessageToMessageEncoder<PowerCommand> {
    @Override
    protected void encode(ChannelHandlerContext ctx, PowerCommand msg, List<Object> out) throws Exception {
        Message outMessage = new Message();
        outMessage.setCommandId(CommandId.POWER);

        byte[] macAddress = msg.getMacAddress().getMac();
        ByteBuf buf = Unpooled.buffer(macAddress.length * 2);
        buf.writeBytes(macAddress);
        buf.writeBytes(MacAddress.PADDING);
        buf.writeZero(4);
        buf.writeBoolean(msg.isOn());

        outMessage.setBytes(ByteBufUtil.getBytes(buf));

        out.add(outMessage);
    }
}
