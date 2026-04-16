package cc.sighs.auratip.data.action;

import cc.sighs.auratip.api.action.ActionHandlers;
import cc.sighs.auratip.api.action.Actions;
import com.mojang.serialization.Dynamic;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public enum ActionExecutor implements Action.ActionVisitor<Void> {
    INSTANCE;

    public static void execute(Action action) {
        if (action == null) {
            return;
        }
        if (action instanceof Action.RunCommand rc) {
            INSTANCE.visitRunCommand(rc);
            return;
        }
        if (action instanceof Action.SimulateKey sk) {
            INSTANCE.visitSimulateKey(sk);
            return;
        }
        if (action instanceof Action.ScriptAction sa) {
            INSTANCE.visitScript(sa);
            return;
        }
        Actions.executeTyped(action);
    }

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
        ActionHandlers.execute(action.type(), params);
        return null;
    }
}
