package com.pochemuto.orvibo;

import com.pochemuto.orvibo.protocol.DatagramEncoder;
import com.pochemuto.orvibo.protocol.DiscoveryCommand;
import com.pochemuto.orvibo.protocol.DiscoveryDecoder;
import com.pochemuto.orvibo.protocol.DiscoveryEncoder;
import com.pochemuto.orvibo.protocol.MessageDecoder;
import com.pochemuto.orvibo.protocol.MessageEncoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 31.10.2016
 */
@Slf4j
public class Orvibo {

    public static void main(String... args) throws Exception {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new DatagramEncoder())
                                    .addLast(new MessageDecoder())
                                    .addLast(new MessageEncoder())
                                    .addLast(new DiscoveryDecoder())
                                    .addLast(new DiscoveryEncoder());
                        }
                    });

            Channel ch = bootstrap.bind(10000).sync().channel();


            ch.writeAndFlush(new DiscoveryCommand()).sync();
            ch.closeFuture().await(60, TimeUnit.SECONDS);

        } finally {
            eventLoopGroup.shutdownGracefully();
        }

    }

}

@Slf4j
class Handler extends SimpleChannelInboundHandler<DatagramPacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        msg.content().readByte();
//        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.error("Error", cause);
    }
}
