package com.pochemuto.orvibo.api.encoder;

import com.pochemuto.orvibo.api.message.CommandId;
import com.pochemuto.orvibo.api.message.MacAddress;
import com.pochemuto.orvibo.api.message.Message;
import com.pochemuto.orvibo.api.message.SubscribeCommand;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

import static com.pochemuto.orvibo.api.Helpers.reversed;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 04.11.2016
 */
public class SubscribeEncoder extends MessageToMessageEncoder<SubscribeCommand> {

    @Override
    protected void encode(ChannelHandlerContext ctx, SubscribeCommand msg, List<Object> out) throws Exception {
        Message outMessage = new Message();
        outMessage.setCommandId(CommandId.SUBSCRIBE);

        byte[] macAddress = msg.getMacAddress().getMac();
        ByteBuf buf = Unpooled.buffer(macAddress.length * 4);
        buf.writeBytes(macAddress);
        buf.writeBytes(MacAddress.PADDING);
        buf.writeBytes(reversed(macAddress));
        buf.writeBytes(MacAddress.PADDING);

        outMessage.setBytes(ByteBufUtil.getBytes(buf));

        out.add(outMessage);
    }

}
