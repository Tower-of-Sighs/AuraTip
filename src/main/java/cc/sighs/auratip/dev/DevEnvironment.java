package cc.sighs.auratip.dev;

import net.neoforged.fml.loading.FMLEnvironment;

public final class DevEnvironment {

    private DevEnvironment() {
    }

    public static boolean isDev() {
        return !FMLEnvironment.isProduction();
    }
}
