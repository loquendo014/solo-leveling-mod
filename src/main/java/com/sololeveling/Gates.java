package com.sololeveling;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import org.joml.Vector3f;

import java.util.*;

/** Puertas: aparecen cerca de los jugadores y desatan una mazmorra de 3 oleadas + jefe. */
public final class Gates {

    private static final Identifier ID_HP = Identifier.of(SoloLeveling.MOD, "gate_health");
    private static final Identifier ID_ATK = Identifier.of(SoloLeveling.MOD, "gate_attack");

    private static final EntityType<?>[][] WAVES = {
            {EntityType.ZOMBIE, EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER},
            {EntityType.HUSK, EntityType.SKELETON, EntityType.STRAY, EntityType.SPIDER},
            {EntityType.VINDICATOR, EntityType.SKELETON, EntityType.STRAY, EntityType.PILLAGER},
            {EntityType.WITHER_SKELETON, EntityType.VINDICATOR, EntityType.BLAZE, EntityType.PILLAGER},
            {EntityType.WITHER_SKELETON, EntityType.VINDICATOR, EntityType.BLAZE, EntityType.EVOKER},
            {EntityType.WITHER_SKELETON, EntityType.VINDICATOR, EntityType.EVOKER, EntityType.RAVAGER},
    };
    private static final double[] HP_MUL = {0, 0.5, 1.0, 1.8, 3.0, 5.0};
    private static final double[] ATK_MUL = {0, 0.25, 0.5, 0.8, 1.2, 1.8};

    private static final EntityType<?>[] BOSS_TYPE = {EntityType.ZOMBIE, EntityType.SKELETON, EntityType.VINDICATOR,
            EntityType.WITHER_SKELETON, EntityType.RAVAGER, EntityType.WITHER_SKELETON};
    private static final String[] BOSS_NAME = {"Rey Trasgo", "Arquero de Hueso Maldito", "Verdugo del Abismo",
            "Caballero de la Puerta Negra", "Bestia de la Puerta Roja", "Señor Demonio de la Torre"};
    private static final double[] BOSS_HP = {120, 180, 260, 380, 550, 900};
    private static final double[] BOSS_SCALE = {1.6, 1.5, 1.5, 1.4, 1.3, 2.0};
    private static final String[] LOOT = {"chests/simple_dungeon", "chests/abandoned_mineshaft", "chests/desert_pyramid",
            "chests/bastion_other", "chests/end_city_treasure", "chests/bastion_treasure"};
    private static final Formatting[] RANK_FMT = {Formatting.GRAY, Formatting.GREEN, Formatting.AQUA,
            Formatting.BLUE, Formatting.LIGHT_PURPLE, Formatting.GOLD};
    private static final Vector3f[] COLORS = {
            new Vector3f(0.7f, 0.7f, 0.8f), new Vector3f(0.3f, 0.9f, 0.4f), new Vector3f(0.2f, 0.8f, 1f),
            new Vector3f(0.2f, 0.3f, 1f), new Vector3f(0.8f, 0.2f, 1f), new Vector3f(1f, 0.15f, 0.1f)};

    public static int intervalSeconds = 720;
    public static boolean enabled = true;

    private static Gate gate;
    private static int timer = 360; // la primera puerta llega antes
    private static final Random RNG = new Random();

    static final class Gate {
        BlockPos pos;
        int rank;
        int state; // 1 esperando, 2 mazmorra activa
        int life = 600;
        int wave;
        int away;
        final Set<UUID> mobs = new HashSet<>();
        UUID boss;
        ServerBossBar bar;
    }

    private static Text rankName(int r) {
        return Text.literal(S.RANKS[r]).formatted(RANK_FMT[r], Formatting.BOLD);
    }

    // ------------------------------------------------------------------ ciclo

    public static void onStart(MinecraftServer server) {
        cmd(server, "kill @e[type=minecraft:text_display,tag=sl_gate_label]");
    }

    public static void onStop(MinecraftServer server) {
        if (gate != null && gate.bar != null) gate.bar.clearPlayers();
        gate = null;
    }

    private static void cmd(MinecraftServer server, String c) {
        server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), c);
    }

    public static void tick(MinecraftServer server, int t) {
        ServerWorld w = server.getOverworld();
        if (gate != null && t % 2 == 0) visuals(w, t);
        if (t % 20 != 0) return;
        if (gate == null) {
            if (!enabled) return;
            if (++timer >= intervalSeconds) {
                timer = 0;
                trySpawn(server, null);
            }
            return;
        }
        if (gate.state == 1) waiting(server, w);
        else dungeon(server, w);
    }

    public static boolean trySpawn(MinecraftServer server, ServerPlayerEntity forced) {
        if (gate != null) close(server);
        ServerWorld w = server.getOverworld();
        List<ServerPlayerEntity> cands = new ArrayList<>();
        for (ServerPlayerEntity p : w.getPlayers()) {
            if (!p.isSpectator() && DataState.of(p).level >= 3) cands.add(p);
        }
        ServerPlayerEntity p = forced != null ? forced : (cands.isEmpty() ? null : cands.get(RNG.nextInt(cands.size())));
        if (p == null || p.getWorld() != w) return false;

        double a = RNG.nextDouble() * Math.PI * 2;
        double dist = 14 + RNG.nextInt(10);
        int x = (int) Math.floor(p.getX() + Math.cos(a) * dist);
        int z = (int) Math.floor(p.getZ() + Math.sin(a) * dist);
        int y = w.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (Math.abs(y - p.getY()) > 20) y = (int) p.getY();

        int rank = Math.min(5, S.rankOf(DataState.of(p).level));
        if (RNG.nextInt(100) < 25 && rank < 5) rank++;

        gate = new Gate();
        gate.pos = new BlockPos(x, y, z);
        gate.rank = rank;
        gate.state = 1;

        cmd(server, String.format(Locale.ROOT,
                "summon minecraft:text_display %.1f %.1f %.1f {Tags:[\"sl_gate_label\"],billboard:\"center\",background:0,shadow:1b,"
                        + "text:'[{\"text\":\"⟦ PUERTA DE RANGO \",\"color\":\"white\",\"bold\":true},{\"text\":\"%s\",\"color\":\"%s\",\"bold\":true},{\"text\":\" ⟧\",\"color\":\"white\",\"bold\":true}]'}",
                x + 0.5, y + 4.6, z + 0.5, S.RANKS[rank], RANK_FMT[rank].getName()));

        Text msg = Fx.sys().append(Text.literal("⚠ ¡Se ha abierto una Puerta de Rango ").formatted(Formatting.WHITE).styled(s -> s.withBold(false)))
                .append(rankName(rank))
                .append(Text.literal("! ").formatted(Formatting.WHITE))
                .append(Text.literal("Coordenadas: " + x + " " + y + " " + z).formatted(Formatting.YELLOW))
                .append(Text.literal(" · Entra en ella para iniciar la mazmorra. Se cerrará en 10 minutos.").formatted(Formatting.GRAY));
        server.getPlayerManager().broadcast(msg, false);
        for (ServerPlayerEntity o : server.getPlayerManager().getPlayerList()) {
            Fx.sound(o, SoundEvents.BLOCK_END_PORTAL_SPAWN, 0.4f, 0.8f);
        }
        Fx.title(p, Text.literal("⚠ PUERTA DETECTADA ⚠").formatted(Formatting.RED, Formatting.BOLD),
                Text.literal("Rango ").formatted(Formatting.GRAY).append(rankName(rank)), 10, 50, 15);
        return true;
    }

    private static void visuals(ServerWorld w, int t) {
        double cx = gate.pos.getX() + 0.5, cy = gate.pos.getY(), cz = gate.pos.getZ() + 0.5;
        DustParticleEffect dust = new DustParticleEffect(COLORS[gate.rank], 1.6f);
        double spin = t * 0.04;
        for (int i = 0; i < 28; i++) {
            double a = Math.PI * 2 * i / 28;
            double h = Math.cos(a) * 1.3;
            double v = Math.sin(a) * 2.0;
            double px = cx + Math.cos(spin) * h;
            double pz = cz + Math.sin(spin) * h;
            w.spawnParticles(dust, px, cy + 2.1 + v, pz, 1, 0, 0, 0, 0);
        }
        w.spawnParticles(ParticleTypes.PORTAL, cx, cy + 2, cz, 12, 0.5, 1.2, 0.5, 0.6);
        w.spawnParticles(ParticleTypes.REVERSE_PORTAL, cx, cy + 2, cz, 3, 0.3, 1, 0.3, 0.02);
        if (gate.state == 1 && t % 60 == 0) Fx.sound(w, cx, cy + 1, cz, SoundEvents.BLOCK_PORTAL_AMBIENT, 0.6f, 0.5f);
    }

    // ------------------------------------------------------------------ estados

    private static void waiting(MinecraftServer server, ServerWorld w) {
        gate.life--;
        if (gate.life == 60) {
            server.getPlayerManager().broadcast(Fx.sys().append(Text.literal("La Puerta se cerrará en 1 minuto.").formatted(Formatting.GRAY).styled(s -> s.withBold(false))), false);
        }
        if (gate.life <= 0) {
            server.getPlayerManager().broadcast(Fx.sys().append(Text.literal("La Puerta se ha cerrado sin ser conquistada.").formatted(Formatting.GRAY).styled(s -> s.withBold(false))), false);
            close(server);
            return;
        }
        double cx = gate.pos.getX() + 0.5, cz = gate.pos.getZ() + 0.5;
        for (ServerPlayerEntity p : w.getPlayers()) {
            if (p.isSpectator()) continue;
            double dx = p.getX() - cx, dz = p.getZ() - cz, dy = p.getY() - gate.pos.getY();
            if (dx * dx + dz * dz < 2.5 && dy > -1 && dy < 4) {
                open(server, w);
                return;
            }
        }
    }

    private static List<ServerPlayerEntity> nearby(ServerWorld w, double r) {
        List<ServerPlayerEntity> out = new ArrayList<>();
        for (ServerPlayerEntity p : w.getPlayers()) {
            if (!p.isSpectator() && p.getBlockPos().getSquaredDistance(gate.pos) < r * r) out.add(p);
        }
        return out;
    }

    private static void open(MinecraftServer server, ServerWorld w) {
        gate.state = 2;
        gate.wave = 0;
        gate.bar = new ServerBossBar(Text.literal("Mazmorra"), BossBar.Color.PURPLE, BossBar.Style.NOTCHED_10);
        for (ServerPlayerEntity p : nearby(w, 32)) {
            Fx.title(p, Text.literal("⟦ MAZMORRA ⟧").formatted(Formatting.DARK_PURPLE, Formatting.BOLD),
                    Text.literal("La Puerta se abre... ¡Elimina a todos los monstruos y al jefe!").formatted(Formatting.GRAY), 10, 50, 15);
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 40, 0, false, false));
            Fx.sound(p, SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, 0.7f, 0.6f);
        }
        nextWave(server, w);
    }

    private static void nextWave(MinecraftServer server, ServerWorld w) {
        gate.wave++;
        gate.mobs.clear();
        if (gate.wave >= 4) {
            spawnBoss(w);
            return;
        }
        int n = 4 + gate.rank * 2;
        EntityType<?>[] pool = WAVES[gate.rank];
        for (int i = 0; i < n; i++) {
            MobEntity m = spawnMob(w, pool[RNG.nextInt(pool.length)], 3, 9);
            if (m == null) continue;
            buff(m, HP_MUL[gate.rank], ATK_MUL[gate.rank]);
            Mobs.decorate(m);
            gate.mobs.add(m.getUuid());
        }
        gate.bar.setName(Text.literal("⚔ Mazmorra de Rango ").formatted(Formatting.WHITE)
                .append(rankName(gate.rank))
                .append(Text.literal(" · Oleada " + gate.wave + "/3").formatted(Formatting.WHITE)));
        gate.bar.setColor(BossBar.Color.PURPLE);
        gate.bar.setPercent(1f);
        for (ServerPlayerEntity p : nearby(w, 64)) {
            Fx.msg(p, Text.literal("¡Oleada " + gate.wave + "/3! ").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD)
                    .append(Text.literal(n + " monstruos salen de la Puerta.").formatted(Formatting.GRAY)));
            Fx.sound(p, SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, 0.8f, 0.7f);
        }
    }

    private static MobEntity spawnMob(ServerWorld w, EntityType<?> type, int minR, int maxR) {
        Entity e = type.create(w);
        if (!(e instanceof MobEntity m)) return null;
        double a = RNG.nextDouble() * Math.PI * 2;
        double r = minR + RNG.nextDouble() * (maxR - minR);
        int x = (int) Math.floor(gate.pos.getX() + Math.cos(a) * r);
        int z = (int) Math.floor(gate.pos.getZ() + Math.sin(a) * r);
        int y = w.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (Math.abs(y - gate.pos.getY()) > 6) {
            x = gate.pos.getX();
            z = gate.pos.getZ();
            y = gate.pos.getY();
        }
        m.refreshPositionAndAngles(x + 0.5, y, z + 0.5, RNG.nextFloat() * 360, 0);
        m.initialize(w, w.getLocalDifficulty(m.getBlockPos()), SpawnReason.MOB_SUMMONED, null);
        m.setPersistent();
        m.addCommandTag("sl_gate_mob");
        EntityAttributeInstance fr = m.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
        if (fr != null) fr.setBaseValue(40);
        w.spawnEntity(m);
        w.spawnParticles(ParticleTypes.REVERSE_PORTAL, m.getX(), m.getY() + 1, m.getZ(), 20, 0.3, 0.6, 0.3, 0.2);
        return m;
    }

    private static void buff(MobEntity m, double hp, double atk) {
        if (hp > 0) {
            EntityAttributeInstance i = m.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
            if (i != null) i.addPersistentModifier(new EntityAttributeModifier(ID_HP, hp, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        if (atk > 0) {
            EntityAttributeInstance i = m.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (i != null) i.addPersistentModifier(new EntityAttributeModifier(ID_ATK, atk, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        m.setHealth(m.getMaxHealth());
    }

    private static void spawnBoss(ServerWorld w) {
        int r = gate.rank;
        MobEntity m = spawnMob(w, BOSS_TYPE[r], 0, 1);
        if (m == null) return;
        EntityAttributeInstance hp = m.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (hp != null) hp.setBaseValue(BOSS_HP[r]);
        EntityAttributeInstance sc = m.getAttributeInstance(EntityAttributes.GENERIC_SCALE);
        if (sc != null) sc.setBaseValue(BOSS_SCALE[r]);
        EntityAttributeInstance kb = m.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
        if (kb != null) kb.setBaseValue(0.8);
        buff(m, 0, 0.5 + ATK_MUL[r]);
        m.setHealth(m.getMaxHealth());
        m.addCommandTag("sl_boss");
        m.setCustomName(Text.literal(BOSS_NAME[r]).formatted(RANK_FMT[r], Formatting.BOLD));
        m.setCustomNameVisible(true);
        m.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, StatusEffectInstance.INFINITE, 0, true, false));
        gate.boss = m.getUuid();
        gate.mobs.add(m.getUuid());
        gate.bar.setName(Text.literal("☠ ").formatted(Formatting.DARK_RED).append(Text.literal(BOSS_NAME[r]).formatted(RANK_FMT[r], Formatting.BOLD))
                .append(Text.literal(" ☠").formatted(Formatting.DARK_RED)));
        gate.bar.setColor(BossBar.Color.RED);
        gate.bar.setPercent(1f);
        for (ServerPlayerEntity p : nearby(w, 64)) {
            Fx.title(p, Text.literal("⚠ JEFE DE LA MAZMORRA ⚠").formatted(Formatting.DARK_RED, Formatting.BOLD),
                    Text.literal(BOSS_NAME[r]).formatted(RANK_FMT[r]), 5, 50, 15);
            Fx.sound(p, SoundEvents.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 0.7f);
        }
        w.spawnParticles(ParticleTypes.LARGE_SMOKE, m.getX(), m.getY() + 1, m.getZ(), 60, 0.8, 1, 0.8, 0.05);
    }

    private static void dungeon(MinecraftServer server, ServerWorld w) {
        List<ServerPlayerEntity> near = nearby(w, 64);
        gate.bar.clearPlayers();
        for (ServerPlayerEntity p : near) gate.bar.addPlayer(p);
        if (near.isEmpty()) {
            if (++gate.away >= 90) {
                dungeonBreak(server, w);
                return;
            }
        } else gate.away = 0;

        // Mantener a los monstruos cerca de la Puerta y contarlos
        int alive = 0;
        Iterator<UUID> it = gate.mobs.iterator();
        while (it.hasNext()) {
            Entity e = w.getEntity(it.next());
            if (e == null || !e.isAlive()) {
                it.remove();
                continue;
            }
            alive++;
            if (e.getBlockPos().getSquaredDistance(gate.pos) > 40 * 40) {
                e.refreshPositionAndAngles(gate.pos.getX() + 0.5, gate.pos.getY() + 1, gate.pos.getZ() + 0.5, e.getYaw(), e.getPitch());
            }
        }

        if (gate.wave >= 4) {
            Entity boss = gate.boss == null ? null : w.getEntity(gate.boss);
            if (boss instanceof MobEntity b && b.isAlive()) {
                gate.bar.setPercent(Math.max(0f, Math.min(1f, b.getHealth() / b.getMaxHealth())));
            } else {
                clear(server, w);
            }
            return;
        }
        int total = 4 + gate.rank * 2;
        gate.bar.setPercent(Math.max(0f, Math.min(1f, alive / (float) total)));
        if (alive == 0) nextWave(server, w);
    }

    private static void clear(MinecraftServer server, ServerWorld w) {
        int r = gate.rank;
        int xp = (r + 1) * (r + 1) * 100;
        int pts = r + 1;
        for (ServerPlayerEntity p : nearby(w, 64)) {
            DataState.of(p).points += pts;
            DataState.of(p).gold += (r + 1) * 120;
            Story.progress(p, 2, 1);
            Fx.title(p, Text.literal("MAZMORRA COMPLETADA").formatted(Formatting.GOLD, Formatting.BOLD),
                    Text.literal("+" + xp + " XP · +" + pts + " puntos").formatted(Formatting.YELLOW), 10, 70, 20);
            Fx.sound(p, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            Fx.sound(p, SoundEvents.ENTITY_PLAYER_LEVELUP, 1f, 0.8f);
            Fx.part(p, ParticleTypes.TOTEM_OF_UNDYING, 1, 80, 0.8, 0.4);
            Fx.msg(p, Text.literal("Has conquistado la Puerta de Rango ").formatted(Formatting.WHITE)
                    .append(rankName(r))
                    .append(Text.literal(". Recompensas: +" + xp + " XP, +" + pts + " puntos de estadística y botín.").formatted(Formatting.YELLOW)));
            cmd(server, "loot give " + p.getGameProfile().getName() + " loot minecraft:" + LOOT[r]);
            if (r == 5) cmd(server, "give " + p.getGameProfile().getName() + " minecraft:nether_star");
            Leveling.addXp(p, xp);
        }
        close(server);
    }

    private static void dungeonBreak(MinecraftServer server, ServerWorld w) {
        server.getPlayerManager().broadcast(Fx.sys().append(Text.literal("⚠ ¡RUPTURA DE MAZMORRA! Los monstruos de la Puerta han escapado al mundo.")
                .formatted(Formatting.RED).styled(s -> s.withBold(false))), false);
        close(server);
    }

    public static void close(MinecraftServer server) {
        if (gate != null && gate.bar != null) {
            gate.bar.clearPlayers();
            gate.bar.setVisible(false);
        }
        gate = null;
        timer = 0;
        cmd(server, "kill @e[type=minecraft:text_display,tag=sl_gate_label]");
    }

    private Gates() {}
}
