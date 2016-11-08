package com.pochemuto.orvibo.api.encoder;

import com.pochemuto.orvibo.api.message.CommandId;
import com.pochemuto.orvibo.api.message.DiscoveryCommand;
import com.pochemuto.orvibo.api.message.MacAddress;
import com.pochemuto.orvibo.api.message.Message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 04.11.2016
 */
public class DiscoveryEncoder extends MessageToMessageEncoder<DiscoveryCommand> {

    @Override
    protected void encode(ChannelHandlerContext ctx, DiscoveryCommand msg, List<Object> out) throws Exception {
        Message outMessage = new Message();

        MacAddress macAddress = msg.getMacAddress();
        if (macAddress != null) {
            outMessage.setCommandId(CommandId.DISCOVERY_TARGET);
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(macAddress.getMac());
            for (int i = 0; i < 5; i++) {
                buf.writeByte(0x20);
            }
            outMessage.setBytes(ByteBufUtil.getBytes(buf));
        } else {
            outMessage.setCommandId(CommandId.DISCOVERY);
        }

        out.add(outMessage);
    }

}
