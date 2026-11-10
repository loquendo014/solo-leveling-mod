package com.sololeveling;

import net.minecraft.entity.EntityType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/** Almas que quedan tras la muerte de un monstruo, listas para ser extraídas. */
public final class Souls {

    public static final class Soul {
        public final RegistryKey<World> dim;
        public final Vec3d pos;
        public final EntityType<?> type;
        public final String name;
        public final boolean elite;
        public final long expires;
        public int tries;

        Soul(RegistryKey<World> dim, Vec3d pos, EntityType<?> type, String name, boolean elite, long expires) {
            this.dim = dim;
            this.pos = pos;
            this.type = type;
            this.name = name;
            this.elite = elite;
            this.expires = expires;
        }
    }

    private static final List<Soul> SOULS = new ArrayList<>();

    public static void add(ServerWorld w, Vec3d pos, EntityType<?> type, String name, boolean elite) {
        SOULS.add(new Soul(w.getRegistryKey(), pos, type, name, elite, w.getServer().getTicks() + (elite ? 1200 : 400)));
        w.spawnParticles(ParticleTypes.SOUL, pos.x, pos.y + 0.6, pos.z, 12, 0.25, 0.4, 0.25, 0.02);
    }

    public static void remove(Soul s) {
        SOULS.remove(s);
    }

    public static List<Soul> near(ServerPlayerEntity p, double r) {
        List<Soul> out = new ArrayList<>();
        for (Soul s : SOULS) {
            if (s.dim == p.getWorld().getRegistryKey() && s.pos.squaredDistanceTo(p.getPos()) <= r * r) out.add(s);
        }
        return out;
    }

    public static void tick(MinecraftServer server, int t) {
        if (SOULS.isEmpty()) return;
        SOULS.removeIf(s -> t > s.expires);
        if (t % 4 != 0) return;
        for (Soul s : SOULS) {
            ServerWorld w = server.getWorld(s.dim);
            if (w == null) continue;
            w.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, s.pos.x, s.pos.y + 0.7, s.pos.z, 2, 0.12, 0.25, 0.12, 0.005);
            w.spawnParticles(ParticleTypes.SCULK_SOUL, s.pos.x, s.pos.y + 0.5, s.pos.z, 1, 0.15, 0.2, 0.15, 0.01);
            if (s.elite) {
                w.spawnParticles(ParticleTypes.REVERSE_PORTAL, s.pos.x, s.pos.y + 1, s.pos.z, 6, 0.3, 0.6, 0.3, 0.05);
            }
        }
    }

    public static void clear() {
        SOULS.clear();
    }

    private Souls() {}
}
