package com.sololeveling;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class Leveling {

    private static final Identifier ID_STR = Identifier.of(SoloLeveling.MOD, "strength");
    private static final Identifier ID_VIT = Identifier.of(SoloLeveling.MOD, "vitality");
    private static final Identifier ID_AGI = Identifier.of(SoloLeveling.MOD, "agility");
    private static final Identifier ID_ASPD = Identifier.of(SoloLeveling.MOD, "attack_speed");
    private static final Identifier ID_ARM = Identifier.of(SoloLeveling.MOD, "armor");
    private static final Identifier ID_LUCK = Identifier.of(SoloLeveling.MOD, "luck");

    /** Cinemática de despertar: jugador -> tick actual de la cinemática. */
    private static final Map<UUID, Integer> CINEMATIC = new HashMap<>();

    // ------------------------------------------------------------------ conexión

    public static void onJoin(ServerPlayerEntity p) {
        PlayerData d = DataState.of(p);
        applyStats(p);
        if (d.awakened == 0) {
            CINEMATIC.put(p.getUuid(), 0);
        } else {
            Fx.msg(p, Text.literal("Bienvenido de nuevo, Jugador. ").formatted(Formatting.WHITE)
                    .append(Text.literal("Pulsa [K] para abrir tu ventana de estado.").formatted(Formatting.GRAY)));
        }
        sync(p);
    }

    public static void tickCinematics(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Integer>> it = CINEMATIC.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> e = it.next();
            ServerPlayerEntity p = server.getPlayerManager().getPlayer(e.getKey());
            if (p == null) {
                it.remove();
                continue;
            }
            int t = e.getValue() + 1;
            e.setValue(t);
            switch (t) {
                case 40 -> {
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 100, 0, false, false));
                    Fx.sound(p, SoundEvents.BLOCK_BEACON_ACTIVATE, 1f, 0.6f);
                    Fx.title(p, Text.literal("[ ! ]").formatted(Formatting.AQUA, Formatting.BOLD),
                            Text.literal("Has cumplido las condiciones secretas.").formatted(Formatting.GRAY, Formatting.ITALIC),
                            10, 60, 10);
                }
                case 120 -> {
                    Fx.sound(p, SoundEvents.BLOCK_BEACON_POWER_SELECT, 1f, 0.8f);
                    Fx.title(p, Text.literal("SISTEMA").formatted(Formatting.AQUA, Formatting.BOLD),
                            Text.literal("Has sido elegido como el Jugador.").formatted(Formatting.GRAY),
                            10, 60, 10);
                }
                case 200 -> {
                    Fx.sound(p, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1f, 0.8f);
                    Fx.part(p, ParticleTypes.END_ROD, 1, 80, 0.8, 0.08);
                    Fx.title(p, Text.literal("DESPERTAR").formatted(Formatting.DARK_AQUA, Formatting.BOLD),
                            Text.literal("Bienvenido, Jugador.").formatted(Formatting.WHITE),
                            10, 70, 20);
                }
                case 280 -> {
                    it.remove();
                    PlayerData d = DataState.of(p);
                    d.awakened = 1;
                    welcome(p);
                }
                default -> {
                }
            }
        }
    }

    private static void welcome(ServerPlayerEntity p) {
        p.sendMessage(Text.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").formatted(Formatting.DARK_AQUA), false);
        Fx.msg(p, Text.literal("Bienvenido, Jugador. A partir de ahora, cada monstruo que derrotes te hará más fuerte.").formatted(Formatting.WHITE));
        p.sendMessage(Text.literal(" • ").formatted(Formatting.DARK_AQUA).append(Text.literal("Sube de nivel y reparte tus puntos de estadística.").formatted(Formatting.GRAY)), false);
        p.sendMessage(Text.literal(" • ").formatted(Formatting.DARK_AQUA).append(Text.literal("Completa la Misión Diaria... o sufre la Zona de Penalización.").formatted(Formatting.GRAY)), false);
        p.sendMessage(Text.literal(" • ").formatted(Formatting.DARK_AQUA).append(Text.literal("Conquista las Puertas que se abrirán por el mundo.").formatted(Formatting.GRAY)), false);
        p.sendMessage(Text.literal(" • ").formatted(Formatting.DARK_AQUA).append(Text.literal("Al llegar al nivel 20 descubrirás tu verdadera clase...").formatted(Formatting.GRAY)), false);
        p.sendMessage(Text.literal(" Pulsa ").formatted(Formatting.GRAY)
                .append(Text.literal("[K]").formatted(Formatting.YELLOW, Formatting.BOLD))
                .append(Text.literal(" para abrir la Ventana de Estado (teclas configurables en Controles).").formatted(Formatting.GRAY)), false);
        p.sendMessage(Text.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").formatted(Formatting.DARK_AQUA), false);
        Fx.sound(p, SoundEvents.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }

    // ------------------------------------------------------------------ estadísticas

    private static void setMod(LivingEntity e, RegistryEntry<EntityAttribute> attr, Identifier id, double value) {
        EntityAttributeInstance inst = e.getAttributeInstance(attr);
        if (inst == null) return;
        inst.removeModifier(id);
        if (value != 0) {
            inst.addTemporaryModifier(new EntityAttributeModifier(id, value, EntityAttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static void applyStats(ServerPlayerEntity p) {
        PlayerData d = DataState.of(p);
        int agi = Math.min(d.agi - 10, 150);
        setMod(p, EntityAttributes.GENERIC_ATTACK_DAMAGE, ID_STR, (d.str - 10) * 0.15);
        setMod(p, EntityAttributes.GENERIC_MAX_HEALTH, ID_VIT, Math.min(d.vit - 10, 1000));
        setMod(p, EntityAttributes.GENERIC_MOVEMENT_SPEED, ID_AGI, agi * 0.0005);
        setMod(p, EntityAttributes.GENERIC_ATTACK_SPEED, ID_ASPD, agi * 0.02);
        setMod(p, EntityAttributes.GENERIC_ARMOR, ID_ARM, Math.min((d.per - 10) * 0.15, 30));
        setMod(p, EntityAttributes.GENERIC_LUCK, ID_LUCK, (d.per - 10) * 0.05);
        if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());
    }

    public static void addStat(ServerPlayerEntity p, int stat, int amount) {
        PlayerData d = DataState.of(p);
        if (d.points < amount) {
            Fx.bar(p, Text.literal("No tienes suficientes puntos.").formatted(Formatting.RED));
            return;
        }
        d.points -= amount;
        switch (stat) {
            case 0 -> d.str += amount;
            case 1 -> d.vit += amount;
            case 2 -> d.agi += amount;
            case 3 -> d.per += amount;
            default -> d.intel += amount;
        }
        applyStats(p);
        Fx.sound(p, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.4f);
        sync(p);
    }

    // ------------------------------------------------------------------ experiencia

    public static void addXp(ServerPlayerEntity p, int amount) {
        PlayerData d = DataState.of(p);
        if (d.level >= 100) return;
        d.xp += amount;
        while (d.level < 100 && d.xp >= d.need()) {
            d.xp -= d.need();
            levelUp(p, d);
        }
        if (d.level >= 100) d.xp = 0;
    }

    private static void levelUp(ServerPlayerEntity p, PlayerData d) {
        int oldRank = d.rank();
        d.level++;
        d.points += 5;
        applyStats(p);
        p.setHealth(p.getMaxHealth());
        d.mana = d.manaMax();

        Fx.title(p, Text.literal("¡SUBISTE DE NIVEL!").formatted(Formatting.AQUA, Formatting.BOLD),
                Text.literal("Nivel ").formatted(Formatting.GRAY).append(Text.literal(String.valueOf(d.level)).formatted(Formatting.WHITE, Formatting.BOLD)),
                5, 40, 15);
        Fx.sound(p, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.4f);
        Fx.sound(p, SoundEvents.BLOCK_BEACON_POWER_SELECT, 1f, 1.2f);
        Fx.part(p, ParticleTypes.TOTEM_OF_UNDYING, 1, 60, 0.6, 0.4);
        Fx.part(p, ParticleTypes.END_ROD, 1, 30, 0.4, 0.1);
        Fx.msg(p, Text.literal("¡Has subido al nivel " + d.level + "! ").formatted(Formatting.WHITE)
                .append(Text.literal("+5 puntos de estadística").formatted(Formatting.YELLOW)));

        // Desbloqueos
        for (int i = 0; i < 7; i++) {
            if (d.level == S.SKILL_LEVEL[i]) {
                if (i == 3) jobChange(p);
                Fx.msg(p, Text.literal("Nueva habilidad adquirida: ").formatted(Formatting.WHITE)
                        .append(Text.literal(S.SKILL_NAMES[i]).formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                        .append(Text.literal("  (mira la tecla en la Ventana de Estado)").formatted(Formatting.DARK_GRAY)));
                Fx.sound(p, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f);
            }
        }

        int newRank = d.rank();
        if (newRank != oldRank) rankUp(p, newRank);
    }

    private static void jobChange(ServerPlayerEntity p) {
        Fx.title(p, Text.literal("CAMBIO DE CLASE").formatted(Formatting.DARK_PURPLE, Formatting.BOLD),
                Text.literal("Tu clase ahora es: NIGROMANTE").formatted(Formatting.LIGHT_PURPLE), 10, 80, 20);
        Fx.sound(p, SoundEvents.ENTITY_WITHER_SPAWN, 0.5f, 1.2f);
        Fx.part(p, ParticleTypes.SCULK_SOUL, 1, 120, 1.2, 0.05);
        Fx.msg(p, Text.literal("Has despertado el poder de comandar a los muertos. Cuando un monstruo caiga, su alma permanecerá unos segundos: usa ").formatted(Formatting.GRAY)
                .append(Text.literal("¡Levántate! [R]").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .append(Text.literal(" cerca de ella para convertirla en tu sombra. Agáchate al usarla para reunir a tu ejército.").formatted(Formatting.GRAY)));
    }

    private static void rankUp(ServerPlayerEntity p, int rank) {
        MutableText title = rank == 6
                ? Text.literal("MONARCA DE LAS SOMBRAS").formatted(Formatting.DARK_PURPLE, Formatting.BOLD)
                : Text.literal("RANGO ").formatted(Formatting.WHITE, Formatting.BOLD).append(Fx.rankText(rank));
        Fx.title(p, title, Text.literal("El Sistema reconoce tu poder").formatted(Formatting.GRAY, Formatting.ITALIC), 10, 80, 20);
        Fx.sound(p, SoundEvents.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 1.2f);
        MinecraftServer server = p.getServer();
        if (server == null) return;
        server.getPlayerManager().broadcast(Fx.sys().append(p.getDisplayName().copy().formatted(Formatting.WHITE))
                .append(Text.literal(rank == 6 ? " ha despertado como el MONARCA DE LAS SOMBRAS." : " ha ascendido a Cazador de Rango ").formatted(Formatting.GRAY).styled(s -> s.withBold(false)))
                .append(rank == 6 ? Text.empty() : Fx.rankText(rank)), false);
        if (rank == 6) {
            for (ServerPlayerEntity o : server.getPlayerManager().getPlayerList()) {
                Fx.sound(o, SoundEvents.ENTITY_WITHER_SPAWN, 0.6f, 0.5f);
            }
            Fx.part(p, ParticleTypes.SCULK_SOUL, 1, 400, 3, 0.05);
            Fx.part(p, ParticleTypes.REVERSE_PORTAL, 1, 300, 2, 0.5);
        }
    }

    // ------------------------------------------------------------------ cada segundo

    public static void second(ServerPlayerEntity p) {
        PlayerData d = DataState.of(p);
        int max = d.manaMax();
        d.mana = Math.min(max, d.mana + 2 + Math.max(0, (d.intel - 10) / 10));

        // Percepción: agachado con PER 25+ revela a los monstruos cercanos
        if (d.per >= 25 && p.isSneaking()) {
            double r = d.per >= 60 ? 32 : 16;
            for (MobEntity m : p.getServerWorld().getEntitiesByClass(MobEntity.class, p.getBoundingBox().expand(r),
                    m -> m instanceof Monster && !Shadows.isShadow(m))) {
                m.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 40, 0, false, false));
            }
        }

        // Monarca: ojos y cuerpo del monarca
        if (d.level >= 100) {
            if (!p.hasStatusEffect(StatusEffects.RESISTANCE))
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, StatusEffectInstance.INFINITE, 0, true, false));
            if (!p.hasStatusEffect(StatusEffects.NIGHT_VISION))
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, StatusEffectInstance.INFINITE, 0, true, false));
        }
    }

    // ------------------------------------------------------------------ sincronización

    public static void sync(ServerPlayerEntity p) {
        if (!ServerPlayNetworking.canSend(p, Net.SyncPayload.ID)) return;
        PlayerData d = DataState.of(p);
        int[] a = new int[S.SIZE];
        a[S.LEVEL] = d.level;
        a[S.XP] = d.xp;
        a[S.NEED] = d.need();
        a[S.PTS] = d.points;
        a[S.STR] = d.str;
        a[S.VIT] = d.vit;
        a[S.AGI] = d.agi;
        a[S.PER] = d.per;
        a[S.INT] = d.intel;
        a[S.MANA] = d.mana;
        a[S.MMAX] = d.manaMax();
        a[S.RANK] = d.rank();
        a[S.QDIST] = Quests.distance(p, d);
        a[S.QKILL] = d.qKills;
        a[S.QJUMP] = Quests.jumps(p, d);
        a[S.QDONE] = d.questDone;
        a[S.SHAD] = Shadows.count(p);
        a[S.SHADMAX] = d.shadowMax();
        for (int i = 0; i < 7; i++) a[S.CD + i] = Skills.cooldownLeft(p, i);
        a[S.HP] = (int) p.getMaxHealth();
        a[S.GOLD] = d.gold;
        a[S.STAGE] = d.stage;
        a[S.SPROG] = d.stage < S.STORY_TYPE.length && S.STORY_TYPE[d.stage] == 1 ? d.level : d.stageProg;
        int[] em = Story.emergency(p);
        if (em != null) {
            a[S.EM_LEFT] = em[0];
            a[S.EM_PROG] = em[1];
            a[S.EM_TARGET] = em[2];
        }
        ServerPlayNetworking.send(p, new Net.SyncPayload(a));
    }

    public static void printStatus(ServerPlayerEntity p) {
        PlayerData d = DataState.of(p);
        p.sendMessage(Text.literal("━━━━━━━━ ⟦ ESTADO ⟧ ━━━━━━━━").formatted(Formatting.AQUA, Formatting.BOLD), false);
        p.sendMessage(Text.literal(" Nivel " + d.level + "  ·  Rango ").formatted(Formatting.GRAY).append(Fx.rankText(d.rank()))
                .append(Text.literal("  ·  XP " + d.xp + "/" + d.need()).formatted(Formatting.GREEN)), false);
        p.sendMessage(Text.literal(" FUE " + d.str + "  VIT " + d.vit + "  AGI " + d.agi + "  PER " + d.per + "  INT " + d.intel).formatted(Formatting.WHITE), false);
        p.sendMessage(Text.literal(" Puntos libres: " + d.points + "  ·  Maná " + d.mana + "/" + d.manaMax()).formatted(Formatting.YELLOW), false);
        p.sendMessage(Text.literal(" (Pulsa [K] para la ventana completa)").formatted(Formatting.DARK_GRAY)
                .styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sistema"))), false);
    }

    private Leveling() {}
}
