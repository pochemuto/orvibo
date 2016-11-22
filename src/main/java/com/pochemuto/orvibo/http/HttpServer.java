package com.pochemuto.orvibo.http;

import com.pochemuto.orvibo.Orvibo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 13.11.2016
 */
@Slf4j
public class HttpServer {
    public static final int DEFAULT_PORT = 4352;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private final Orvibo orvibo;
    private final int port;

    public HttpServer(Orvibo orvibo, int port) {
        this.orvibo = orvibo;
        this.port = port;
    }
    public HttpServer(Orvibo orvibo) {
        this(orvibo, DEFAULT_PORT);
    }

    public void init() {
        try {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();

            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new HttpRequestDecoder());
                            p.addLast(new HttpResponseEncoder());
                            p.addLast(new HttpHandler(orvibo));
                        }
                    });

            b.bind(port).sync().channel();

            log.info("Http server started on " + port);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
