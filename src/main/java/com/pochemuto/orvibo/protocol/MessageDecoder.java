package com.pochemuto.orvibo.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 04.11.2016
 */
@Slf4j
public class MessageDecoder extends ReplayingDecoder<MessageDecoder.State> {

    protected enum State {
        MAGIC, LENGTH, COMMAND, BODY
    }

    private Message message = new Message();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case MAGIC:
                ByteBuf magicBytes = in.readBytes(2);
                if (!Arrays.equals(Message.MAGIC, magicBytes.array())) {
                    throw new RuntimeException("Message doesn't contain magic number");
                }
                checkpoint(State.LENGTH);
            case LENGTH:
                short length = in.readShort();
                message.setBytes(new byte[length]);
                checkpoint(State.COMMAND);
            case COMMAND:
                ByteBuf commandBytes = in.readBytes(2);
                message.setCommandId(CommandId.fromBytes(commandBytes.array()));
                checkpoint(State.BODY);
            case BODY:
                in.readBytes(message.getBytes());
                out.add(this.message);
                reset();
            default:
                throw new Exception("Unknown decoding state: " + state());
        }


    }

    private void reset() {
        checkpoint(State.MAGIC);
        message = new Message();
    }
}
