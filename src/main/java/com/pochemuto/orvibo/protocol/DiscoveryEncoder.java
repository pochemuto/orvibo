package com.pochemuto.orvibo.protocol;

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
        outMessage.setCommandId(msg.getCommand());

        byte[] macAddress = msg.getMacAddress();
        if (macAddress != null) {
            outMessage.setBytes(macAddress);
        }

        out.add(outMessage);
    }

}
