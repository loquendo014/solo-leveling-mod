package com.sololeveling;

import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Skills {

    private static final Map<UUID, long[]> READY_AT = new HashMap<>();

    private static long now(ServerPlayerEntity p) {
        return p.getServer().getTicks();
    }

    public static int cooldownLeft(ServerPlayerEntity p, int skill) {
        long[] a = READY_AT.get(p.getUuid());
        if (a == null) return 0;
        long left = a[skill] - now(p);
        return left <= 0 ? 0 : (int) ((left + 19) / 20);
    }

    /** Comprueba nivel, enfriamiento y maná; si todo va bien, cobra el coste. */
    public static boolean pay(ServerPlayerEntity p, int skill) {
        PlayerData d = DataState.of(p);
        if (d.level < S.SKILL_LEVEL[skill]) {
            Fx.bar(p, Text.literal("🔒 " + S.SKILL_NAMES[skill] + " se desbloquea en el nivel " + S.SKILL_LEVEL[skill]).formatted(Formatting.RED));
            return false;
        }
        int cd = cooldownLeft(p, skill);
        if (cd > 0) {
            Fx.bar(p, Text.literal("⏳ " + S.SKILL_NAMES[skill] + ": " + cd + " s").formatted(Formatting.RED));
            return false;
        }
        if (d.mana < S.SKILL_MANA[skill]) {
            Fx.bar(p, Text.literal("✦ Maná insuficiente (" + d.mana + "/" + S.SKILL_MANA[skill] + ")").formatted(Formatting.BLUE));
            Fx.sound(p, SoundEvents.ENTITY_VILLAGER_NO, 0.5f, 1.4f);
            return false;
        }
        d.mana -= S.SKILL_MANA[skill];
        READY_AT.computeIfAbsent(p.getUuid(), k -> new long[7])[skill] = now(p) + S.SKILL_CD[skill] * 20L;
        return true;
    }

    public static void use(ServerPlayerEntity p, int skill) {
        if (p.isSpectator() || !p.isAlive()) return;
        switch (skill) {
            case 0 -> sprint(p);
            case 1 -> killingIntent(p);
            case 2 -> stealth(p);
            case 3 -> Shadows.arise(p);
            case 4 -> authority(p);
            case 5 -> shadowExchange(p);
            case 6 -> domain(p);
            default -> {
            }
        }
        Leveling.sync(p);
    }

    private static List<MobEntity> enemies(ServerPlayerEntity p, Box box) {
        return p.getServerWorld().getEntitiesByClass(MobEntity.class, box,
                m -> m instanceof Monster && !Shadows.isShadow(m) && m.isAlive());
    }

    private static void sprint(ServerPlayerEntity p) {
        if (!pay(p, 0)) return;
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 300, 2, false, false, true));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 300, 1, false, false, true));
        Fx.part(p, ParticleTypes.CLOUD, 0.2, 25, 0.4, 0.05);
        Fx.sound(p, SoundEvents.ENTITY_PHANTOM_FLAP, 1f, 1.5f);
        Fx.bar(p, Text.literal("» CARRERA «").formatted(Formatting.AQUA, Formatting.BOLD));
    }

    private static void killingIntent(ServerPlayerEntity p) {
        if (!pay(p, 1)) return;
        for (MobEntity m : enemies(p, p.getBoundingBox().expand(12))) {
            m.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 120, 2));
            m.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 120, 1));
            m.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 120, 0));
            Fx.part(m, ParticleTypes.ANGRY_VILLAGER, 2, 2, 0.2, 0);
        }
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 160, 0, false, false, true));
        Fx.part(p, ParticleTypes.CRIMSON_SPORE, 1, 200, 3, 0);
        Fx.part(p, ParticleTypes.LARGE_SMOKE, 1, 30, 0.6, 0.03);
        Fx.sound(p, SoundEvents.ENTITY_WARDEN_ROAR, 0.5f, 1.4f);
        Fx.bar(p, Text.literal("☠ INTENCIÓN ASESINA ☠").formatted(Formatting.DARK_RED, Formatting.BOLD));
    }

    private static void stealth(ServerPlayerEntity p) {
        if (!pay(p, 2)) return;
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 400, 0, false, false, true));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 400, 1, false, false, true));
        p.removeStatusEffect(StatusEffects.GLOWING);
        for (MobEntity m : enemies(p, p.getBoundingBox().expand(32))) {
            if (m.getTarget() == p) m.setTarget(null);
        }
        Fx.part(p, ParticleTypes.LARGE_SMOKE, 1, 40, 0.5, 0.02);
        Fx.part(p, ParticleTypes.SQUID_INK, 1, 20, 0.4, 0.05);
        Fx.sound(p, SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, 1f, 0.8f);
        Fx.bar(p, Text.literal("SIGILO").formatted(Formatting.GRAY, Formatting.BOLD));
    }

    private static void authority(ServerPlayerEntity p) {
        PlayerData d = DataState.of(p);
        Vec3d look = p.getRotationVector();
        Vec3d c = p.getEyePos().add(look.multiply(5));
        List<MobEntity> hit = enemies(p, new Box(c, c).expand(5));
        if (!pay(p, 4)) return;
        ServerWorld w = p.getServerWorld();
        float dmg = 6 + Math.max(0, (d.str - 10) / 10f);
        for (MobEntity m : hit) {
            m.addVelocity(look.x * 0.6, 1.1, look.z * 0.6);
            m.velocityModified = true;
            m.damage(w.getDamageSources().indirectMagic(p, p), dmg);
            Fx.part(m, ParticleTypes.REVERSE_PORTAL, 1, 20, 0.4, 0.2);
        }
        Fx.part(w, ParticleTypes.SONIC_BOOM, c.x, c.y, c.z, 1, 0, 0, 0, 0);
        Fx.part(w, ParticleTypes.REVERSE_PORTAL, c.x, c.y, c.z, 120, 1.5, 1, 1.5, 0.2);
        Fx.sound(p, SoundEvents.ENTITY_WARDEN_SONIC_BOOM, 0.5f, 1.4f);
        Fx.bar(p, Text.literal("AUTORIDAD DEL GOBERNANTE").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
    }

    private static void shadowExchange(ServerPlayerEntity p) {
        HitResult hr = p.raycast(30, 0, false);
        Vec3d look = p.getRotationVector();
        Vec3d target;
        if (hr.getType() == HitResult.Type.BLOCK && hr instanceof BlockHitResult bh && bh.getSide() == Direction.UP) {
            target = hr.getPos();
        } else {
            Vec3d back = hr.getPos().subtract(look.multiply(0.8));
            target = new Vec3d(back.x, back.y - 1.62, back.z);
        }
        ServerWorld w = p.getServerWorld();
        Vec3d found = null;
        for (int i = 0; i < 4 && found == null; i++) {
            Vec3d cand = target.add(0, i, 0);
            Box box = p.getBoundingBox().offset(cand.subtract(p.getPos()));
            if (w.isSpaceEmpty(p, box)) found = cand;
        }
        if (found == null) {
            Fx.bar(p, Text.literal("No hay espacio en esa dirección.").formatted(Formatting.GRAY));
            return;
        }
        if (!pay(p, 5)) return;
        Fx.part(p, ParticleTypes.LARGE_SMOKE, 1, 30, 0.3, 0.02);
        Fx.part(p, ParticleTypes.REVERSE_PORTAL, 1, 40, 0.4, 0.3);
        p.requestTeleport(found.x, found.y, found.z);
        p.fallDistance = 0;
        Shadows.teleportArmy(p);
        Fx.part(p, ParticleTypes.SCULK_SOUL, 1, 30, 0.6, 0.05);
        Fx.part(p, ParticleTypes.REVERSE_PORTAL, 1, 40, 0.4, 0.3);
        Fx.sound(p, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
    }

    private static void domain(ServerPlayerEntity p) {
        if (!pay(p, 6)) return;
        PlayerData d = DataState.of(p);
        ServerWorld w = p.getServerWorld();
        float dmg = 14 + Math.max(0, (d.str - 10) / 5f);
        for (MobEntity m : enemies(p, p.getBoundingBox().expand(14))) {
            m.damage(w.getDamageSources().indirectMagic(p, p), dmg);
            Vec3d push = m.getPos().subtract(p.getPos()).normalize().multiply(1.2);
            m.addVelocity(push.x, 0.5, push.z);
            m.velocityModified = true;
            m.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 3));
        }
        Shadows.buffArmy(p, 600);
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 400, 1, false, false, true));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 400, 1, false, false, true));
        for (int i = 0; i < 48; i++) {
            double a = Math.PI * 2 * i / 48;
            for (double r = 3; r <= 14; r += 3.5) {
                Fx.part(w, ParticleTypes.SCULK_SOUL, p.getX() + Math.cos(a) * r, p.getY() + 0.3, p.getZ() + Math.sin(a) * r, 1, 0, 0.1, 0, 0.01);
            }
        }
        Fx.part(p, ParticleTypes.LARGE_SMOKE, 0.5, 150, 4, 0.05);
        Fx.part(p, ParticleTypes.SONIC_BOOM, 1, 1, 0, 0);
        Fx.sound(p, SoundEvents.ENTITY_WITHER_SPAWN, 0.7f, 0.8f);
        Fx.sound(p, SoundEvents.ENTITY_WARDEN_ROAR, 0.6f, 0.6f);
        Fx.title(p, Text.literal("DOMINIO DEL MONARCA").formatted(Formatting.DARK_PURPLE, Formatting.BOLD), Text.empty(), 2, 30, 10);
    }

    private Skills() {}
}
