package cc.sighs.auratip.editor.net;

import cc.sighs.auratip.AuraTip;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Minimal HTTP request decoder for the editor backend.
 * <p>
 * Only supports parsing request line + headers (no body).
 */
final class EditorHttpRequestDecoder extends ByteToMessageDecoder {
    private static final int MAX_HEADER_BYTES = 32 * 1024;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int readable = in.readableBytes();
        if (readable <= 0) {
            return;
        }
        if (readable > MAX_HEADER_BYTES) {
            AuraTip.LOGGER.warn("Editor HTTP header too large: {} bytes", readable);
            ctx.close();
            return;
        }

        int end = indexOfHeaderEnd(in);
        if (end < 0) {
            return;
        }

        int len = end - in.readerIndex();
        byte[] bytes = new byte[len];
        in.readBytes(bytes);
        // skip \r\n\r\n
        in.skipBytes(4);

        String headerText = new String(bytes, StandardCharsets.US_ASCII);
        String[] lines = headerText.split("\\r\\n");
        if (lines.length == 0) {
            return;
        }

        String first = lines[0].trim();
        String[] firstParts = first.split("\\s+");
        if (firstParts.length < 2) {
            return;
        }
        String method = firstParts[0];
        String uri = firstParts[1];

        Map<String, String> headers = EditorHttpRequest.headersLowerMutable();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            int colon = line.indexOf(':');
            if (colon <= 0) {
                continue;
            }
            String key = line.substring(0, colon).trim().toLowerCase();
            String value = line.substring(colon + 1).trim();
            if (!key.isEmpty()) {
                headers.put(key, value);
            }
        }

        out.add(new EditorHttpRequest(method, uri, headers));
    }

    private static int indexOfHeaderEnd(ByteBuf in) {
        int start = in.readerIndex();
        int end = in.writerIndex();
        for (int i = start; i + 3 < end; i++) {
            if (in.getByte(i) == '\r'
                    && in.getByte(i + 1) == '\n'
                    && in.getByte(i + 2) == '\r'
                    && in.getByte(i + 3) == '\n') {
                return i;
            }
        }
        return -1;
    }
}

