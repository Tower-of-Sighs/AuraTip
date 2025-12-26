package cc.sighs.auratip.data.action;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.fml.ModList;
import org.lwjgl.glfw.GLFW;

public enum ActionExecutor implements Action.ActionVisitor<Void> {
    INSTANCE;

    @Override
    public Void visitOpenGui(Action.OpenGui action) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return null;
        }

        String classPath = action.classPath();
        if ("net.minecraft.client.gui.screens.inventory.InventoryScreen".equals(classPath)) {
            Screen screen = new InventoryScreen(minecraft.player);
            minecraft.setScreen(screen);
            return null;
        }

        if (ModList.get().isLoaded("ftbquests")) {
            if ("dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen".equals(classPath) && ClientQuestFile.exists() && (!ClientQuestFile.INSTANCE.isDisableGui() || ClientQuestFile.INSTANCE.canEdit())) {
                ClientQuestFile.openGui();
                return null;
            }
        }

        try {
            Class<?> clazz = Class.forName(classPath);
            if (Screen.class.isAssignableFrom(clazz)) {
                try {
                    Screen screen = (Screen) clazz.getDeclaredConstructor().newInstance();
                    minecraft.setScreen(screen);
                } catch (NoSuchMethodException ignored) {
                    tryInvokeOpenMethod(clazz);
                }
            } else {
                tryInvokeOpenMethod(clazz);
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    @Override
    public Void visitRunCommand(Action.RunCommand action) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            String command = action.command();
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
        Minecraft minecraft = Minecraft.getInstance();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.setScreen(null);
        }
        return null;
    }

    private void tryInvokeOpenMethod(Class<?> clazz) {
        try {
            var method = clazz.getMethod("open");
            if (method.getReturnType() == void.class) {
                method.invoke(null);
            } else if (Screen.class.isAssignableFrom(method.getReturnType())) {
                Screen screen = (Screen) method.invoke(null);
                Minecraft.getInstance().setScreen(screen);
            }
        } catch (Exception ignored) {
        }
    }
}

