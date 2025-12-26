package cc.sighs.auratip.handler;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.data.TipData;
import cc.sighs.auratip.data.TipData.Trigger;
import cc.sighs.auratip.data.TipData.Trigger.Mode;
import cc.sighs.auratip.network.NetworkHandler;
import cc.sighs.auratip.network.ShowTipsPacket;
import com.mafuyu404.oelib.data.DataManagerBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = AuraTip.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEventHandler {
    private static final String SHOWN_TIPS_TAG = "auratip_shown_tips";

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        List<TipData> tips = DataManagerBridge.getDataList(TipData.class);
        if (tips == null || tips.isEmpty()) {
            return;
        }

        CompoundTag persistent = player.getPersistentData();
        CompoundTag shown = persistent.getCompound(SHOWN_TIPS_TAG);

        List<String> toShow = new ArrayList<>();

        for (TipData tip : tips) {
            Trigger trigger = tip.trigger();
            if (trigger == null) {
                continue;
            }
            if (!"FIRST_JOIN_WORLD".equalsIgnoreCase(trigger.type())) {
                continue;
            }

            String id = tip.id();
            Mode mode = trigger.mode();
            boolean alreadyShown = shown.getBoolean(id);

            if (mode == Mode.ONCE && alreadyShown) {
                continue;
            }

            toShow.add(id);
            if (mode == Mode.ONCE) {
                shown.putBoolean(id, true);
            }
        }

        if (!toShow.isEmpty()) {
            persistent.put(SHOWN_TIPS_TAG, shown);
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ShowTipsPacket(toShow));
        }
    }
}
