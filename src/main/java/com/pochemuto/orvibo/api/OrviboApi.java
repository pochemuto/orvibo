package com.pochemuto.orvibo.api;

import com.pochemuto.orvibo.api.decoder.DiscoveryDecoder;
import com.pochemuto.orvibo.api.decoder.MessageDecoder;
import com.pochemuto.orvibo.api.decoder.SubscribeDecoder;
import com.pochemuto.orvibo.api.encoder.DatagramEncoder;
import com.pochemuto.orvibo.api.encoder.DiscoveryEncoder;
import com.pochemuto.orvibo.api.encoder.MessageEncoder;
import com.pochemuto.orvibo.api.encoder.PowerEncoder;
import com.pochemuto.orvibo.api.encoder.SubscribeEncoder;
import com.pochemuto.orvibo.api.message.DiscoveryCommand;
import com.pochemuto.orvibo.api.message.DiscoveryResponse;
import com.pochemuto.orvibo.api.message.PowerCommand;
import com.pochemuto.orvibo.api.message.SubscribeCommand;
import com.pochemuto.orvibo.api.message.SubscribeResponse;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 31.10.2016
 */
@Slf4j
public class OrviboApi {

    private static final int PORT = 10000;
    private static final InetSocketAddress RECIPIENT = new InetSocketAddress("255.255.255.255", PORT);
    private Channel channel;

    public static void main(String... args) throws Exception {
        OrviboApi orviboApi = new OrviboApi();
        orviboApi.init();
    }

    public void init() throws Exception {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel ch) throws Exception {
                        initChannelPipeline(ch);
                    }
                });

        channel = bootstrap.bind(PORT).sync().channel();
    }

    private void initChannelPipeline(NioDatagramChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new DatagramEncoder(RECIPIENT))
                .addLast(new MessageDecoder())
                .addLast(new MessageEncoder())
                .addLast(new DiscoveryDecoder())
                .addLast(new DiscoveryEncoder())
                .addLast(new SubscribeDecoder())
                .addLast(new SubscribeEncoder())
                .addLast(new PowerEncoder())
                .addLast(new MessageHandler<>(this::received, DiscoveryResponse.class))
                .addLast(new MessageHandler<>(this::received, SubscribeResponse.class));
    }

    private Consumer<DiscoveryResponse> discoveryHandler;
    private Consumer<SubscribeResponse> subscribeHandler;

    public OrviboApi onDiscovery(Consumer<DiscoveryResponse> hander) {
        this.discoveryHandler = hander;
        return this;
    }

    public OrviboApi onSubscribe(Consumer<SubscribeResponse> hander) {
        this.subscribeHandler = hander;
        return this;
    }

    private void send0(Object message) {
        channel.writeAndFlush(message);
    }

    public void send(DiscoveryCommand command) {
        send0(command);
    }

    public void send(SubscribeCommand command) {
        send0(command);
    }

    public void send(PowerCommand command) {
        send0(command);
    }

    private void received(DiscoveryResponse response) {
        log.debug("discovery response received " + response);
        if (discoveryHandler != null) {
            discoveryHandler.accept(response);
        }
    }

    private void received(SubscribeResponse response) {
        log.debug("subscribe response received " + response);
        if (subscribeHandler != null) {
            subscribeHandler.accept(response);
        }
    }
}
