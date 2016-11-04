package com.pochemuto.orvibo.api;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.function.Consumer;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 04.11.2016
 */
public class MessageHandler<I> extends SimpleChannelInboundHandler<I> {
    private final Consumer<I> consumer;

    public MessageHandler(Consumer<I> consumer, Class<I> messageType) {
        super(messageType);
        this.consumer = consumer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, I msg) throws Exception {
        consumer.accept(msg);
    }

}
