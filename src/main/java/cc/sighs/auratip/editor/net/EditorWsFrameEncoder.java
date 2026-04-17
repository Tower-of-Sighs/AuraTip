package cc.sighs.auratip.editor.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

/**
 * Minimal WebSocket text frame encoder (server -> browser).
 * <p>
 * Server frames must be unmasked.
 */
final class EditorWsFrameEncoder extends MessageToByteEncoder<String> {

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) {
        if (msg == null) {
            msg = "";
        }
        byte[] payload = msg.getBytes(StandardCharsets.UTF_8);
        int len = payload.length;

        out.writeByte(0x81); // FIN + text

        if (len <= 125) {
            out.writeByte(len);
        } else if (len <= 0xFFFF) {
            out.writeByte(126);
            out.writeShort(len);
        } else {
            out.writeByte(127);
            out.writeLong(len);
        }

        out.writeBytes(payload);
    }
}

