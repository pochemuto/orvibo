package com.pochemuto.orvibo.api.decoder;

import com.pochemuto.orvibo.api.message.CommandId;
import com.pochemuto.orvibo.api.message.Message;
import com.pochemuto.orvibo.api.message.SubscribeResponse;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

import static com.pochemuto.orvibo.api.Helpers.readMacAddress;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 05.11.2016
 */
public class SubscribeDecoder extends MessageToMessageDecoder<Message> {

    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        return super.acceptInboundMessage(msg) && ((Message) msg).getCommandId() == CommandId.SUBSCRIBE;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        SubscribeResponse response = new SubscribeResponse();

        if (msg.getBytes().length > 0) {
            ByteBuf buf = Unpooled.wrappedBuffer(msg.getBytes());
            response.setMacAddress(readMacAddress(buf));
            buf.skipBytes(6); // padding
            buf.skipBytes(5); // UNKNOWN
            response.setSuccess(buf.readBoolean());
        }

        out.add(response);
    }
}
