package cc.sighs.auratip.editor.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Minimal WebSocket frame decoder (server side).
 * <p>
 * Supports:
 * - masked text frames from browser (opcode=1)
 * - close frames (opcode=8)
 */
final class EditorWsFrameDecoder extends ByteToMessageDecoder {
    private static final int MAX_PAYLOAD = 1024 * 1024;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 2) {
            return;
        }

        in.markReaderIndex();

        int b0 = in.readUnsignedByte();
        int b1 = in.readUnsignedByte();

        int opcode = b0 & 0x0F;
        boolean masked = (b1 & 0x80) != 0;
        long len = b1 & 0x7F;

        if (len == 126) {
            if (in.readableBytes() < 2) {
                in.resetReaderIndex();
                return;
            }
            len = in.readUnsignedShort();
        } else if (len == 127) {
            if (in.readableBytes() < 8) {
                in.resetReaderIndex();
                return;
            }
            len = in.readLong();
        }

        if (len < 0 || len > MAX_PAYLOAD) {
            ctx.close();
            return;
        }

        int maskLen = masked ? 4 : 0;
        if (in.readableBytes() < maskLen + (int) len) {
            in.resetReaderIndex();
            return;
        }

        byte[] mask = null;
        if (masked) {
            mask = new byte[4];
            in.readBytes(mask);
        }

        byte[] payload = new byte[(int) len];
        in.readBytes(payload);
        if (masked && mask != null) {
            for (int i = 0; i < payload.length; i++) {
                payload[i] = (byte) (payload[i] ^ mask[i & 3]);
            }
        }

        if (opcode == 0x8) { // close
            ctx.close();
            return;
        }

        if (opcode == 0x1) { // text
            out.add(new String(payload, StandardCharsets.UTF_8));
        }
        // Ignore other opcodes for now.
    }
}

