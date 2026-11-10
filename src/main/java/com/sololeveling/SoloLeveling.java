package com.sololeveling;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoloLeveling implements ModInitializer {
    public static final String MOD = "sololeveling";
    public static final Logger LOG = LoggerFactory.getLogger(MOD);

    @Override
    public void onInitialize() {
        Net.registerCommon();

        ServerPlayNetworking.registerGlobalReceiver(Net.ActionPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            int action = payload.action();
            context.server().execute(() -> handleAction(player, action));
        });

        ServerLifecycleEvents.SERVER_STARTED.register(Gates::onStart);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            Gates.onStop(server);
            Souls.clear();
        });
        ServerTickEvents.END_SERVER_TICK.register(SoloLeveling::tick);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> Leveling.onJoin(handler.getPlayer()));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            Leveling.applyStats(newPlayer);
            newPlayer.setHealth(newPlayer.getMaxHealth());
        });

        ServerLivingEntityEvents.AFTER_DEATH.register(Events::onDeath);
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(Events::allowDamage);
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof MobEntity m && Shadows.isShadow(m)) Shadows.setupGoals(m);
        });

        CommandRegistrationCallback.EVENT.register(Commands::register);
        LOG.info("[Solo Leveling] El Sistema ha despertado.");
    }

    private static void handleAction(ServerPlayerEntity p, int a) {
        if (a >= 1 && a <= 7) Skills.use(p, a - 1);
        else if (a >= 10 && a <= 14) Leveling.addStat(p, a - 10, 1);
        else if (a >= 15 && a <= 19) Leveling.addStat(p, a - 15, 5);
    }

    private static void tick(MinecraftServer server) {
        int t = server.getTicks();
        Souls.tick(server, t);
        Gates.tick(server, t);
        Leveling.tickCinematics(server);
        if (t % 20 == 0) {
            Shadows.rebuild(server);
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                Leveling.second(p);
                Quests.second(p);
            }
        }
        if (t % 10 == 0) {
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) Leveling.sync(p);
        }
    }
}
