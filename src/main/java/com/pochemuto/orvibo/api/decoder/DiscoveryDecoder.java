package com.pochemuto.orvibo.api.decoder;

import com.pochemuto.orvibo.api.message.CommandId;
import com.pochemuto.orvibo.api.message.DiscoveryResponse;
import com.pochemuto.orvibo.api.message.Message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.pochemuto.orvibo.api.Helpers.readMacAddress;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 04.11.2016
 */
@Slf4j
public class DiscoveryDecoder extends MessageToMessageDecoder<Message> {

    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        CommandId commandId = ((Message) msg).getCommandId();
        return super.acceptInboundMessage(msg) &&
                (commandId == CommandId.DISCOVERY || commandId == CommandId.DISCOVERY_TARGET);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        DiscoveryResponse response = new DiscoveryResponse();

        if (msg.getBytes().length > 0) {
            ByteBuf buf = Unpooled.wrappedBuffer(msg.getBytes());
            byte zero = buf.readByte(); // zero-byte
            if (zero != 0) {
                log.info("skip discovery outgoing message");
                return;
            }
            response.setMacAddress(readMacAddress(buf));
            buf.skipBytes(6); // padding
            buf.skipBytes(6); // macAddress LE
            buf.skipBytes(6); // padding

            byte[] bin = new byte[6];
            buf.readBytes(bin);
            response.setText(new String(bin, StandardCharsets.US_ASCII));

            response.setTime(buf.readUnsignedMedium());
            buf.skipBytes(1);

            response.setOn(buf.readBoolean());
        }
        out.add(response);
    }
}
