package cc.sighs.auratip.editor.net;

import io.netty.channel.Channel;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class EditorWsHub {
    private final Set<Channel> channels = ConcurrentHashMap.newKeySet();

    public void add(Channel ch) {
        if (ch != null) {
            channels.add(ch);
        }
    }

    public void remove(Channel ch) {
        if (ch != null) {
            channels.remove(ch);
        }
    }

    public void closeAll() {
        for (Channel ch : channels) {
            try {
                ch.close();
            } catch (Exception ignored) {
            }
        }
        channels.clear();
    }
}

