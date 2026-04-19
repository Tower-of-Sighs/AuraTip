package cc.sighs.auratip.editor.client;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.editor.net.EditorNettyServer;
import cc.sighs.auratip.editor.preview.EditorPreviewApplier;
import cc.sighs.auratip.editor.preview.EditorPreviewScreen;
import cc.sighs.auratip.editor.preview.EditorRadialPreviewApplier;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Client-side entrypoint for the visual editor.
 */
public final class EditorClient {
    private static final Path DEV_EDITOR_HTML = Path.of("src/main/resources/assets/auratip/web/editor.html");

    private static EditorNettyServer server;

    private EditorClient() {
    }

    public static boolean isOpen() {
        return server != null && server.isRunning();
    }

    public static void open(String mode) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        if (server == null) {
            server = new EditorNettyServer();
        }

        if (!server.isRunning()) {
            server.start();
        }

        mc.setScreen(new EditorPreviewScreen(EditorClient::close));

        // Ensure a preview is visible immediately, even before the first web push.
        if ("radial".equalsIgnoreCase(mode)) {
            EditorRadialPreviewApplier.applyDefaultPreview();
        } else {
            EditorPreviewApplier.applyDefaultPreview();
        }

        openBrowser(server.getPort());
        AuraTip.LOGGER.info("AuraTip editor opened (mode={}, port={})", mode, server.getPort());
    }

    public static void close() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof EditorPreviewScreen) {
            mc.setScreen(null);
        }
        if (server != null) {
            server.stop();
        }
        AuraTip.LOGGER.info("AuraTip editor closed");
    }

    private static void openBrowser(int port) {
        // Prefer opening the dev workspace HTML directly (so edits refresh easily),
        // but fall back to the embedded HTTP server URL when not available.
        String url;
        if (Files.isRegularFile(DEV_EDITOR_HTML)) {
            url = DEV_EDITOR_HTML.toAbsolutePath().toUri() + "?wsPort=" + port;
        } else {
            url = "http://127.0.0.1:" + port + "/editor.html";
        }

        try {
            Util.getPlatform().openUri(new URI(url));
        } catch (Exception e) {
            AuraTip.LOGGER.warn("Failed to open editor browser URL: {}", url, e);
        }
    }
}
