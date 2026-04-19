package cc.sighs.auratip.editor.net;

import cc.sighs.auratip.AuraTip;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Local HTTP + WebSocket server for the visual editor.
 * <p>
 * Bound to 127.0.0.1 only.
 */
public final class EditorNettyServer {
    private final AtomicBoolean running = new AtomicBoolean(false);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private int port;

    private final EditorWsHub wsHub = new EditorWsHub();

    public boolean isRunning() {
        return running.get();
    }

    public int getPort() {
        return port;
    }

    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(1);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new EditorHttpRequestDecoder())
                                    .addLast(new EditorHttpHandler(wsHub));
                        }
                    });

            ChannelFuture bind = bootstrap.bind(new InetSocketAddress("127.0.0.1", 0)).syncUninterruptibly();
            serverChannel = bind.channel();
            port = ((InetSocketAddress) serverChannel.localAddress()).getPort();
            AuraTip.LOGGER.info("EditorNettyServer started on 127.0.0.1:{}", port);
        } catch (Exception e) {
            AuraTip.LOGGER.error("Failed to start EditorNettyServer", e);
            stop();
        }
    }

    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        try {
            if (serverChannel != null) {
                serverChannel.close().syncUninterruptibly();
            }
        } catch (Exception ignored) {
        }

        wsHub.closeAll();

        try {
            if (workerGroup != null) {
                workerGroup.shutdownGracefully().syncUninterruptibly();
            }
        } catch (Exception ignored) {
        }

        try {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully().syncUninterruptibly();
            }
        } catch (Exception ignored) {
        }

        bossGroup = null;
        workerGroup = null;
        serverChannel = null;
        port = 0;
        AuraTip.LOGGER.info("EditorNettyServer stopped");
    }
}
