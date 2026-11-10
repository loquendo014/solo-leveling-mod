package com.sololeveling;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class Events {

    public static boolean allowDamage(LivingEntity victim, DamageSource source, float amount) {
        Entity attacker = source.getAttacker();
        if (attacker == null) return true;
        if (Shadows.isShadow(victim)) {
            // Ni los jugadores ni otras sombras dañan a las sombras
            if (attacker instanceof PlayerEntity || Shadows.isShadow(attacker)) return false;
        }
        if (victim instanceof PlayerEntity && Shadows.isShadow(attacker)) return false;
        return true;
    }

    public static void onDeath(LivingEntity entity, DamageSource source) {
        if (!(entity.getWorld() instanceof ServerWorld w)) return;

        if (Shadows.isShadow(entity)) {
            ServerPlayerEntity o = Shadows.owner(entity);
            if (o != null) {
                Fx.msg(o, Text.literal("Una de tus sombras ha caído: ").formatted(Formatting.GRAY)
                        .append(entity.getDisplayName().copy().formatted(Formatting.DARK_PURPLE)));
            }
            w.spawnParticles(ParticleTypes.LARGE_SMOKE, entity.getX(), entity.getY() + 0.5, entity.getZ(), 30, 0.4, 0.5, 0.4, 0.03);
            return;
        }

        Entity attacker = source.getAttacker();
        ServerPlayerEntity player = null;
        boolean direct = false;
        if (attacker instanceof ServerPlayerEntity sp) {
            player = sp;
            direct = true;
        } else if (attacker != null && Shadows.isShadow(attacker)) {
            player = Shadows.owner(attacker);
        }
        if (player == null) return;

        PlayerData d = DataState.of(player);
        EntityType<?> type = entity.getType();
        boolean monster = entity instanceof Monster;

        if (monster) {
            int xp = 12 + d.level / 4;
            if (!direct) xp = xp * 2 / 3;
            if (entity.getCommandTags().contains("sl_gate_mob")) xp += 10;
            if (type == EntityType.WITHER || type == EntityType.ENDER_DRAGON || type == EntityType.WARDEN || type == EntityType.ELDER_GUARDIAN) {
                xp += 600;
                Fx.msg(player, Text.literal("¡Has derrotado a un ser legendario! +600 XP").formatted(Formatting.GOLD));
            }
            if (direct) d.qKills++;
            Leveling.addXp(player, xp);
        }

        // Alma para extraer (requiere la clase Nigromante: nivel 20)
        String name = Shadows.EXTRACTABLE.get(type);
        if (name != null && d.level >= S.SKILL_LEVEL[3]) {
            boolean elite = entity.getCommandTags().contains("sl_boss");
            Souls.add(w, entity.getPos(), type, name, elite);
        }
    }

    private Events() {}
}
