package com.pochemuto.orvibo.http;

import com.pochemuto.orvibo.Device;
import com.pochemuto.orvibo.Orvibo;
import com.pochemuto.orvibo.api.message.MacAddress;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.CharsetUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 13.11.2016
 */

@Slf4j
public class HttpHandler extends SimpleChannelInboundHandler<Object> {
    private final Orvibo orvibo;
    private HttpRequest request;
    private final int timeout = 1000;
    /** Buffer that stores the response content */
    private final StringBuilder buf = new StringBuilder();
    private final Pattern DIGITS = Pattern.compile("[0-9]+");

    public HttpHandler(Orvibo orvibo) {
        this.orvibo = orvibo;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof HttpRequest) {
                HttpRequest request = this.request = (HttpRequest) msg;
                if (HttpUtil.is100ContinueExpected(request)) {
                    send100Continue(ctx);
                }

                QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
                buf.setLength(0);
                log.info("Received request: " + queryDecoder.path());
                Request r = parseRequest(queryDecoder.path());
                switch (r.getCommand()) {
                    case "on":
                        orvibo.setPower(r.getMacAddress(), true).get(timeout, TimeUnit.MILLISECONDS);
                        break;
                    case "off":
                        orvibo.setPower(r.getMacAddress(), false).get(timeout, TimeUnit.MILLISECONDS);
                        break;
                    case "toggle":
                        orvibo.toggle(r.getMacAddress()).get(timeout, TimeUnit.MILLISECONDS);
                        break;
                    case "state":
                        orvibo.getDevices().stream()
                                .filter(d -> d.getMacAddress().equals(r.getMacAddress()))
                                .map(d -> d.isOn() ? "1" : "0")
                                .findAny()
                                .ifPresent(buf::append);
                        break;
                    case "list":
                    default:
                        int n = 0;
                        for (Device device : orvibo.getDevices()) {
                            buf.append(n++).append(' ')
                                    .append(device.getMacAddress().toString().replaceAll("\\s+", "")).append(' ')
                                    .append(device.isOn() ? "ON" : "OFF").append('\n');
                        }
                }

            }
            if (msg instanceof HttpContent) {
                if (msg instanceof LastHttpContent) {
                    LastHttpContent trailer = (LastHttpContent) msg;
                    if (!writeResponse(trailer, ctx)) {
                        // If keep-alive is off, close the connection once the content is fully written.
                        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                    }
                }
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            buf.append("error: ").append(e.getMessage());
        }
    }

    private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
        // Decide whether to close the connection or not.
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, currentObj.decoderResult().isSuccess()? OK : BAD_REQUEST,
                Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            // Add keep alive header as per:
            // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        // Encode the cookie.
        String cookieString = request.headers().get(HttpHeaderNames.COOKIE);
        if (cookieString != null) {
            Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieString);
            if (!cookies.isEmpty()) {
                // Reset the cookies if necessary.
                for (Cookie cookie: cookies) {
                    response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
                }
            }
        } else {
            // Browser sent no cookie.  Add some.
            response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode("key1", "value1"));
            response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode("key2", "value2"));
        }
        // Write the response.
        ctx.write(response);
        return keepAlive;
    }
    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private Request parseRequest(String path) {
        String[] split = path.substring(1).split("/");
        Request request = new Request(split[0]);
        if (split.length > 1) {
            String arg = split[1];
            if (DIGITS.matcher(arg).matches()) {
                request.setDeviceId(Integer.parseInt(arg));
            } else {
                MacAddress mac = MacAddress.fromString(arg);
                request.setMacAddress(mac);
            }
        }
        return request;

    }

    @Data
    private static class Request {
        private final String command;
        private MacAddress macAddress;
        private int deviceId;
    }
}