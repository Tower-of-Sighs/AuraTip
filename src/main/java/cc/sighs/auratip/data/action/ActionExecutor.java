package cc.sighs.auratip.data.action;

import cc.sighs.auratip.compat.kubejs.radiamenu.action.ActionScriptRegistry;
import com.mojang.serialization.Dynamic;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public enum ActionExecutor implements Action.ActionVisitor<Void> {
    INSTANCE;

    @Override
    public Void visitRunCommand(Action.RunCommand action) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            var command = action.command();
            if (command.startsWith("/")) {
                command = command.substring(1);
            }
            minecraft.player.connection.sendCommand(command);
        }
        return null;
    }

    @Override
    public Void visitSimulateKey(Action.SimulateKey action) {
        int keyCode = action.keyCode();
        var minecraft = Minecraft.getInstance();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.setScreen(null);
        }
        return null;
    }

    @Override
    public Void visitScript(Action.ScriptAction action) {
        Map<String, Dynamic<?>> params = new HashMap<>();
        if (action.params() != null) {
            params.putAll(action.params());
        }
        ActionScriptRegistry.execute(action.type(), params);
        return null;
    }
}
