package com.pochemuto.orvibo.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.pochemuto.orvibo.protocol.Helpers.dump;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 04.11.2016
 */
@Slf4j
public class DiscoveryDecoder extends MessageToMessageDecoder<Message> {

    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        return super.acceptInboundMessage(msg) && ((Message) msg).getCommandId() == CommandId.DISCOVERY;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        log.info("Discovery message received: " + msg.getCommandId());
        log.info("Data: " + dump(msg.getBytes()));
    }
}
