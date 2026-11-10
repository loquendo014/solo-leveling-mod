package com.sololeveling;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Utilidades de efectos visuales, sonidos y mensajes. */
public final class Fx {

    public static MutableText sys() {
        return Text.literal("[SISTEMA] ").formatted(Formatting.AQUA, Formatting.BOLD);
    }

    public static void msg(ServerPlayerEntity p, Text body) {
        p.sendMessage(sys().append(body.copy().styled(s -> s.withBold(false))), false);
    }

    public static void msg(ServerPlayerEntity p, String body) {
        msg(p, Text.literal(body).formatted(Formatting.WHITE));
    }

    public static void bar(ServerPlayerEntity p, Text t) {
        p.sendMessage(t, true);
    }

    public static void title(ServerPlayerEntity p, Text title, Text sub, int in, int stay, int out) {
        p.networkHandler.sendPacket(new TitleFadeS2CPacket(in, stay, out));
        p.networkHandler.sendPacket(new SubtitleS2CPacket(sub));
        p.networkHandler.sendPacket(new TitleS2CPacket(title));
    }

    public static void sound(Entity e, SoundEvent ev, float vol, float pitch) {
        e.getWorld().playSound(null, e.getX(), e.getY(), e.getZ(), ev, SoundCategory.PLAYERS, vol, pitch);
    }

    public static void sound(ServerWorld w, double x, double y, double z, SoundEvent ev, float vol, float pitch) {
        w.playSound(null, x, y, z, ev, SoundCategory.HOSTILE, vol, pitch);
    }

    public static <T extends ParticleEffect> void part(ServerWorld w, T p, double x, double y, double z,
                                                      int count, double dx, double dy, double dz, double speed) {
        w.spawnParticles(p, x, y, z, count, dx, dy, dz, speed);
    }

    public static <T extends ParticleEffect> void part(Entity e, T p, double yOff, int count, double d, double speed) {
        if (e.getWorld() instanceof ServerWorld w) {
            w.spawnParticles(p, e.getX(), e.getY() + yOff, e.getZ(), count, d, d, d, speed);
        }
    }

    public static MutableText rankText(int rank) {
        return Text.literal(S.RANKS[rank]).styled(s -> s.withColor(S.RANK_COLORS[rank]).withBold(true));
    }

    private Fx() {}
}
