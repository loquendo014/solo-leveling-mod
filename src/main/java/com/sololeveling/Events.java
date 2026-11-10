package com.sololeveling;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Set;

public final class Events {

    public static boolean allowDamage(LivingEntity victim, DamageSource source, float amount) {
        Entity attacker = source.getAttacker();
        if (attacker == null) return true;
        if (Shadows.isShadow(victim)) {
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
        Set<String> tags = entity.getCommandTags();
        boolean boss = tags.contains("sl_boss");

        if (entity instanceof Monster) {
            boolean magic = tags.contains("sl_gate_mob") || tags.contains("sl_magic");
            int xp = 12 + d.level / 4;
            if (!direct) xp = xp * 2 / 3;
            if (magic) xp += 10;
            if (type == EntityType.WITHER || type == EntityType.ENDER_DRAGON || type == EntityType.WARDEN || type == EntityType.ELDER_GUARDIAN) {
                xp += 600;
                d.gold += 1000;
                Fx.msg(player, Text.literal("¡Has derrotado a un ser legendario! +600 XP, +1000 de oro").formatted(Formatting.GOLD));
            }
            int gold = 2 + d.rank() + w.getRandom().nextInt(4);
            if (magic) gold *= 2;
            if (boss) gold += 150 * (d.rank() + 1);
            d.gold += gold;
            if (magic && w.getRandom().nextInt(100) < 35) entity.dropItem(ModItems.MANA_STONE);
            if (boss) {
                entity.dropStack(new ItemStack(ModItems.ESSENCE_STONE, 1 + w.getRandom().nextInt(2)));
                if (w.getRandom().nextInt(100) < 30) entity.dropItem(ModItems.POWER_FRAGMENT);
                Story.progress(player, 4, 1);
            }
            if (direct) d.qKills++;
            Story.progress(player, 0, 1);
            Leveling.addXp(player, xp);
        }

        String name = Shadows.EXTRACTABLE.get(type);
        if (name != null && d.level >= S.SKILL_LEVEL[3]) Souls.add(w, entity.getPos(), type, name, boss);
    }

    private Events() {}
}
