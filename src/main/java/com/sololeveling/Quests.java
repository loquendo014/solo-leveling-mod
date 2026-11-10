package com.sololeveling;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Misión diaria: "Preparación para hacerse más fuerte". */
public final class Quests {

    private static int stat(ServerPlayerEntity p, net.minecraft.util.Identifier id) {
        return p.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(id));
    }

    private static int moveCm(ServerPlayerEntity p) {
        return stat(p, Stats.WALK_ONE_CM) + stat(p, Stats.SPRINT_ONE_CM);
    }

    public static int distance(ServerPlayerEntity p, PlayerData d) {
        return Math.max(0, (moveCm(p) - d.qMoveBase) / 100);
    }

    public static int jumps(ServerPlayerEntity p, PlayerData d) {
        return Math.max(0, stat(p, Stats.JUMP) - d.qJumpBase);
    }

    private static int today(ServerPlayerEntity p) {
        return (int) (p.getServer().getOverworld().getTimeOfDay() / 24000L);
    }

    public static void second(ServerPlayerEntity p) {
        PlayerData d = DataState.of(p);
        if (d.awakened == 0) return;
        int day = today(p);
        if (d.questDay != day) {
            boolean failed = d.questDay >= 0 && d.questDone == 0 && d.qPlay >= 600;
            if (failed && !p.isCreative() && !p.isSpectator()) penalty(p);
            d.questDay = day;
            d.questDone = 0;
            d.qKills = 0;
            d.qPlay = 0;
            d.qMoveBase = moveCm(p);
            d.qJumpBase = stat(p, Stats.JUMP);
            Fx.msg(p, Text.literal("Ha llegado la Misión Diaria: ").formatted(Formatting.WHITE)
                    .append(Text.literal("Preparación para hacerse más fuerte").formatted(Formatting.GOLD, Formatting.BOLD)));
            p.sendMessage(Text.literal("   Recorre " + S.Q_DIST + " bloques, derrota " + S.Q_KILL + " monstruos y salta " + S.Q_JUMP
                    + " veces antes del próximo amanecer.").formatted(Formatting.GRAY), false);
            Fx.sound(p, SoundEvents.BLOCK_BEACON_POWER_SELECT, 0.7f, 1.6f);
            return;
        }
        d.qPlay++;
        if (d.questDone == 0 && distance(p, d) >= S.Q_DIST && d.qKills >= S.Q_KILL && jumps(p, d) >= S.Q_JUMP) {
            complete(p, d);
        }
    }

    private static void complete(ServerPlayerEntity p, PlayerData d) {
        d.questDone = 1;
        d.points += 3;
        p.setHealth(p.getMaxHealth());
        p.getHungerManager().setFoodLevel(20);
        d.mana = d.manaMax();
        Fx.title(p, Text.literal("MISIÓN COMPLETADA").formatted(Formatting.GOLD, Formatting.BOLD),
                Text.literal("Preparación para hacerse más fuerte").formatted(Formatting.GRAY), 10, 60, 20);
        Fx.sound(p, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        Fx.part(p, ParticleTypes.TOTEM_OF_UNDYING, 1, 50, 0.6, 0.3);
        int xp = 150 + d.level * 10;
        Fx.msg(p, Text.literal("Recompensas: recuperación total, +3 puntos de estadística, +" + xp + " XP.").formatted(Formatting.YELLOW));
        Leveling.addXp(p, xp);
    }

    private static void penalty(ServerPlayerEntity p) {
        ServerWorld w = p.getServerWorld();
        Fx.title(p, Text.literal("ZONA DE PENALIZACIÓN").formatted(Formatting.DARK_RED, Formatting.BOLD),
                Text.literal("No completaste la Misión Diaria").formatted(Formatting.RED), 10, 70, 20);
        Fx.sound(p, SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, 1f, 0.7f);
        Fx.msg(p, Text.literal("Has fallado la Misión Diaria. Sobrevive a la Zona de Penalización.").formatted(Formatting.RED));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 600, 0));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 600, 1));
        for (int i = 0; i < 5; i++) {
            CaveSpiderEntity c = EntityType.CAVE_SPIDER.create(w);
            if (c == null) continue;
            double a = Math.PI * 2 * i / 5;
            c.refreshPositionAndAngles(p.getX() + Math.cos(a) * 3, p.getY(), p.getZ() + Math.sin(a) * 3, 0, 0);
            if (!w.isSpaceEmpty(c)) c.refreshPositionAndAngles(p.getX(), p.getY(), p.getZ(), 0, 0);
            c.setCustomName(Text.literal("Ciempiés de Castigo").formatted(Formatting.DARK_RED));
            var hp = c.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
            if (hp != null) hp.setBaseValue(24);
            c.setHealth(24);
            c.setTarget((PlayerEntity) p);
            w.spawnEntity(c);
        }
    }

    private Quests() {}
}
