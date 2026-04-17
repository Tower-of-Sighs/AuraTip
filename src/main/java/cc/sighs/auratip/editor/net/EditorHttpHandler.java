package cc.sighs.auratip.editor.net;

import cc.sighs.auratip.AuraTip;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Minimal HTTP handler for the editor backend (serves editor.html and upgrades to WebSocket).
 * <p>
 * This intentionally avoids Netty's http/websocket codecs because Minecraft's bundled Netty
 * set (4.1.82) doesn't include netty-codec-http in the moddev artifact manifest.
 */
final class EditorHttpHandler extends SimpleChannelInboundHandler<EditorHttpRequest> {
    private static final String EDITOR_PATH = "/assets/auratip/web/editor.html";
    private static final String WEB_ROOT = "/assets/auratip/web";
    private static final String TOKEN_WS_PORT = "__AURATIP_WS_PORT__";
    private static final String WS_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    private final EditorWsHub wsHub;

    EditorHttpHandler(EditorWsHub wsHub) {
        this.wsHub = wsHub;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EditorHttpRequest req) {
        String method = req.method();
        if (!"GET".equalsIgnoreCase(method)) {
            sendStatus(ctx, 405, "Method Not Allowed");
            return;
        }

        String path = req.path();
        if (path == null || path.isEmpty() || "/".equals(path)) {
            sendRedirect(ctx, "/editor.html");
            return;
        }

        if ("/ws".equals(path) && isWebSocketUpgrade(req)) {
            upgradeToWebSocket(ctx, req);
            return;
        }

        if ("/editor.html".equals(path)) {
            sendEditorHtml(ctx);
            return;
        }

        if (path.startsWith("/lang/") && path.endsWith(".json")) {
            sendWebResource(ctx, path, "application/json; charset=utf-8");
            return;
        }

        sendStatus(ctx, 404, "Not Found");
    }

    private static boolean isWebSocketUpgrade(EditorHttpRequest req) {
        String upgrade = req.headerLower("upgrade");
        if (!"websocket".equalsIgnoreCase(upgrade)) {
            return false;
        }
        String connection = req.headerLower("connection");
        // Usually "Upgrade", but may be "keep-alive, Upgrade"
        return connection.toLowerCase().contains("upgrade");
    }

    private void upgradeToWebSocket(ChannelHandlerContext ctx, EditorHttpRequest req) {
        String key = req.headerLower("sec-websocket-key");
        if (key == null || key.isBlank()) {
            sendStatus(ctx, 400, "Bad Request");
            return;
        }

        String accept;
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hash = sha1.digest((key.trim() + WS_GUID).getBytes(StandardCharsets.US_ASCII));
            accept = Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            AuraTip.LOGGER.warn("WS accept hash failed", e);
            sendStatus(ctx, 500, "Internal Server Error");
            return;
        }

        String response =
                "HTTP/1.1 101 Switching Protocols\r\n" +
                        "Upgrade: websocket\r\n" +
                        "Connection: Upgrade\r\n" +
                        "Sec-WebSocket-Accept: " + accept + "\r\n" +
                        "\r\n";

        ctx.writeAndFlush(Unpooled.copiedBuffer(response, CharsetUtil.US_ASCII))
                .addListener((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        ctx.close();
                        return;
                    }

                    ChannelPipeline p = ctx.pipeline();
                    // We are now in WebSocket mode on this connection.
                    if (p.get(EditorHttpRequestDecoder.class) != null) {
                        p.remove(EditorHttpRequestDecoder.class);
                    }
                    p.remove(this);

                    p.addLast(new EditorWsFrameDecoder());
                    p.addLast(new EditorWsFrameEncoder());
                    p.addLast(new EditorWsHandler(wsHub));
                });
    }

    private static void sendEditorHtml(ChannelHandlerContext ctx) {
        try (InputStream in = AuraTip.class.getResourceAsStream(EDITOR_PATH)) {
            if (in == null) {
                sendStatus(ctx, 404, "Not Found");
                return;
            }

            byte[] bytes = in.readAllBytes();
            String html = new String(bytes, StandardCharsets.UTF_8);
            int port = ((InetSocketAddress) ctx.channel().localAddress()).getPort();
            html = html.replace(TOKEN_WS_PORT, String.valueOf(port));
            byte[] out = html.getBytes(StandardCharsets.UTF_8);

            String header =
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/html; charset=utf-8\r\n" +
                            "Content-Length: " + out.length + "\r\n" +
                            "Cache-Control: no-store\r\n" +
                            "Connection: close\r\n" +
                            "\r\n";
            ByteBuf buf = Unpooled.buffer(header.length() + out.length);
            buf.writeCharSequence(header, CharsetUtil.US_ASCII);
            buf.writeBytes(out);
            ctx.writeAndFlush(buf).addListener(ChannelFutureListener.CLOSE);
        } catch (Exception e) {
            AuraTip.LOGGER.warn("Failed to serve editor.html", e);
            sendStatus(ctx, 500, "Internal Server Error");
        }
    }

    private static void sendWebResource(ChannelHandlerContext ctx, String requestPath, String contentType) {
        if (requestPath == null || requestPath.isBlank()) {
            sendStatus(ctx, 404, "Not Found");
            return;
        }
        // basic path sanitation: only allow "one folder deep" under /lang/
        if (requestPath.contains("..") || requestPath.contains("\\") || requestPath.contains("%")) {
            sendStatus(ctx, 400, "Bad Request");
            return;
        }

        String resourcePath = WEB_ROOT + requestPath;
        try (InputStream in = AuraTip.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                sendStatus(ctx, 404, "Not Found");
                return;
            }

            byte[] out = in.readAllBytes();
            String header =
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + contentType + "\r\n" +
                            "Content-Length: " + out.length + "\r\n" +
                            "Cache-Control: no-store\r\n" +
                            "Connection: close\r\n" +
                            "\r\n";

            ByteBuf buf = Unpooled.buffer(header.length() + out.length);
            buf.writeCharSequence(header, CharsetUtil.US_ASCII);
            buf.writeBytes(out);
            ctx.writeAndFlush(buf).addListener(ChannelFutureListener.CLOSE);
        } catch (Exception e) {
            AuraTip.LOGGER.warn("Failed to serve web resource {}", requestPath, e);
            sendStatus(ctx, 500, "Internal Server Error");
        }
    }

    private static void sendRedirect(ChannelHandlerContext ctx, String location) {
        String response =
                "HTTP/1.1 302 Found\r\n" +
                        "Location: " + location + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
        ctx.writeAndFlush(Unpooled.copiedBuffer(response, CharsetUtil.US_ASCII)).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendStatus(ChannelHandlerContext ctx, int code, String text) {
        byte[] bytes = (code + " " + text).getBytes(StandardCharsets.UTF_8);
        String header =
                "HTTP/1.1 " + code + " " + text + "\r\n" +
                        "Content-Type: text/plain; charset=utf-8\r\n" +
                        "Content-Length: " + bytes.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";

        ByteBuf buf = Unpooled.buffer(header.length() + bytes.length);
        buf.writeCharSequence(header, CharsetUtil.US_ASCII);
        buf.writeBytes(bytes);
        ctx.writeAndFlush(buf).addListener(ChannelFutureListener.CLOSE);
    }
}
