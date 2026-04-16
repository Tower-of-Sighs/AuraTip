package cc.sighs.auratip.dev;

import net.minecraftforge.fml.loading.FMLEnvironment;

public final class DevEnvironment {

    private DevEnvironment() {
    }

    public static boolean isDev() {
        return !FMLEnvironment.production;
    }
}

