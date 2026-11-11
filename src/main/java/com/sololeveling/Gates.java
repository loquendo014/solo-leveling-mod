package com.sololeveling;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.*;

/** Puertas: aparecen en el Overworld y llevan a una mazmorra construida en su propia dimensión. */
public final class Gates {

    public static final RegistryKey<World> DUNGEON = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(SoloLeveling.MOD, "dungeon"));
    private static final Identifier ID_HP = Identifier.of(SoloLeveling.MOD, "gate_health");
    private static final Identifier ID_ATK = Identifier.of(SoloLeveling.MOD, "gate_attack");
    private static final int RAD = 15, HGT = 10;

    private static final EntityType<?>[][] WAVES = {
            {EntityType.ZOMBIE, EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.SILVERFISH},
            {EntityType.HUSK, EntityType.SKELETON, EntityType.STRAY, EntityType.SPIDER, ModEntities.ANT},
            {EntityType.VINDICATOR, EntityType.STRAY, EntityType.PILLAGER, ModEntities.ANT},
            {EntityType.WITHER_SKELETON, EntityType.VINDICATOR, ModEntities.ANT, EntityType.PILLAGER},
            {EntityType.WITHER_SKELETON, EntityType.PIGLIN_BRUTE, EntityType.BLAZE, EntityType.EVOKER},
            {ModEntities.ANT, ModEntities.ANT, EntityType.WITHER_SKELETON, EntityType.PIGLIN_BRUTE},
    };
    private static final double[] HP_MUL = {0, 0.5, 1.0, 1.8, 3.0, 5.0};
    private static final double[] ATK_MUL = {0, 0.25, 0.5, 0.8, 1.2, 1.8};

    private static final EntityType<?>[] BOSS_TYPE = {EntityType.ZOMBIE, ModEntities.STONE_GOLEM, EntityType.VINDICATOR,
            ModEntities.BLOOD_KNIGHT, ModEntities.CERBERUS, ModEntities.ANT_KING};
    private static final String[] BOSS_NAME = {"Rey Trasgo", "Coloso de Piedra", "Verdugo del Abismo",
            "Igris el Rojo Sangre", "Cerbero, Guardián del Infierno", "Rey Hormiga"};
    private static final double[] BOSS_HP = {120, 220, 260, 380, 550, 900};
    private static final double[] BOSS_SCALE = {1.6, 1.15, 1.5, 1.15, 1.6, 2.4};
    private static final String[] THEME_NAME = {"Cripta de Piedra", "Templo del Desierto", "Caverna Helada",
            "Abismo de Pizarra", "Fortaleza Infernal", "Torre del Demonio"};
    private static final String[] LOOT = {"chests/simple_dungeon", "chests/abandoned_mineshaft", "chests/desert_pyramid",
            "chests/bastion_other", "chests/end_city_treasure", "chests/bastion_treasure"};
    private static final Formatting[] RANK_FMT = {Formatting.GRAY, Formatting.GREEN, Formatting.AQUA,
            Formatting.BLUE, Formatting.LIGHT_PURPLE, Formatting.GOLD};
    private static final Vector3f[] COLORS = {
            new Vector3f(0.7f, 0.7f, 0.8f), new Vector3f(0.3f, 0.9f, 0.4f), new Vector3f(0.2f, 0.8f, 1f),
            new Vector3f(0.2f, 0.3f, 1f), new Vector3f(0.8f, 0.2f, 1f), new Vector3f(1f, 0.15f, 0.1f)};
    // suelo, acento, muro, acento de muro, pilar, luz
    private static final Block[][] THEMES = {
            {Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS, Blocks.GLOWSTONE},
            {Blocks.SMOOTH_SANDSTONE, Blocks.CUT_SANDSTONE, Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.CUT_SANDSTONE, Blocks.SHROOMLIGHT},
            {Blocks.POLISHED_DIORITE, Blocks.PACKED_ICE, Blocks.PACKED_ICE, Blocks.BLUE_ICE, Blocks.BLUE_ICE, Blocks.SEA_LANTERN},
            {Blocks.DEEPSLATE_TILES, Blocks.CRACKED_DEEPSLATE_TILES, Blocks.DEEPSLATE_BRICKS, Blocks.SCULK, Blocks.POLISHED_DEEPSLATE, Blocks.VERDANT_FROGLIGHT},
            {Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.RED_NETHER_BRICKS, Blocks.NETHER_BRICKS, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, Blocks.GILDED_BLACKSTONE, Blocks.SHROOMLIGHT},
            {Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.POLISHED_BLACKSTONE, Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR, Blocks.PEARLESCENT_FROGLIGHT},
    };

    public static int intervalSeconds = 720;
    public static boolean enabled = true;

    private static Gate gate;
    private static int timer = 360;
    private static final Random RNG = new Random();
    private static final Map<UUID, Vec3d> RETURN = new HashMap<>();

    static final class Gate {
        BlockPos pos;
        int rank;
        int state; // 1 esperando, 2 mazmorra, 3 salida
        int life = 600;
        int wave;
        int away;
        int endTimer;
        final Set<UUID> mobs = new HashSet<>();
        UUID boss;
        ServerBossBar bar;
        ServerWorld dw;
        BlockPos center;
    }

    private static Text rankName(int r) {
        return Text.literal(S.RANKS[r]).formatted(RANK_FMT[r], Formatting.BOLD);
    }

    private static void broadcast(MinecraftServer server, Text body) {
        server.getPlayerManager().broadcast(Fx.sys().append(body.copy().styled(s -> s.withBold(false))), false);
    }

    // ------------------------------------------------------------------ ciclo

    public static void onStart(MinecraftServer server) {
        cmd(server, "kill @e[type=minecraft:text_display,tag=sl_gate_label]");
    }

    public static void onStop(MinecraftServer server) {
        if (gate != null && gate.bar != null) gate.bar.clearPlayers();
        gate = null;
    }

    public static void onJoin(ServerPlayerEntity p) {
        if (p.getWorld().getRegistryKey() == DUNGEON && (gate == null || gate.state < 2 || gate.dw != p.getWorld())) back(p);
    }

    public static boolean isDungeon(World w) {
        return w.getRegistryKey() == DUNGEON;
    }

    private static void cmd(MinecraftServer server, String c) {
        server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), c);
    }

    public static void tick(MinecraftServer server, int t) {
        ServerWorld ow = server.getOverworld();
        if (gate != null && t % 2 == 0) visuals(ow, t);
        if (t % 20 != 0) return;
        if (gate == null) {
            if (!enabled) return;
            if (++timer >= intervalSeconds) {
                timer = 0;
                trySpawn(server, null);
            }
            return;
        }
        switch (gate.state) {
            case 1 -> waiting(server, ow);
            case 2 -> {
                for (ServerPlayerEntity p : stepping(ow)) enterPlayer(p);
                dungeon(server);
            }
            default -> {
                if (--gate.endTimer <= 0) close(server);
            }
        }
    }

    public static boolean trySpawn(MinecraftServer server, ServerPlayerEntity forced) {
        ServerWorld w = server.getOverworld();
        if (forced != null && forced.getWorld() != w) return false;
        if (gate != null) close(server);
        List<ServerPlayerEntity> cands = new ArrayList<>();
        for (ServerPlayerEntity p : w.getPlayers()) {
            if (!p.isSpectator() && DataState.of(p).level >= 3) cands.add(p);
        }
        ServerPlayerEntity p = forced != null ? forced : (cands.isEmpty() ? null : cands.get(RNG.nextInt(cands.size())));
        if (p == null) return false;

        double a = RNG.nextDouble() * Math.PI * 2;
        double dist = forced != null ? 6 : 14 + RNG.nextInt(10);
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

        broadcast(server, Text.literal("⚠ ¡Se ha abierto una Puerta de Rango ").formatted(Formatting.WHITE)
                .append(rankName(rank))
                .append(Text.literal("! ").formatted(Formatting.WHITE))
                .append(Text.literal("Coordenadas: " + x + " " + y + " " + z).formatted(Formatting.YELLOW))
                .append(Text.literal(" · Entra en ella para acceder a la mazmorra. Se cerrará en 10 minutos.").formatted(Formatting.GRAY)));
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
            w.spawnParticles(dust, cx + Math.cos(spin) * h, cy + 2.1 + v, cz + Math.sin(spin) * h, 1, 0, 0, 0, 0);
        }
        w.spawnParticles(ParticleTypes.PORTAL, cx, cy + 2, cz, 12, 0.5, 1.2, 0.5, 0.6);
        w.spawnParticles(ParticleTypes.REVERSE_PORTAL, cx, cy + 2, cz, 3, 0.3, 1, 0.3, 0.02);
        if (gate.state == 1 && t % 60 == 0) Fx.sound(w, cx, cy + 1, cz, SoundEvents.BLOCK_PORTAL_AMBIENT, 0.6f, 0.5f);
        if (gate.state == 3 && gate.dw != null) {
            BlockPos c = gate.center;
            gate.dw.spawnParticles(ParticleTypes.END_ROD, c.getX() + 0.5, c.getY() + 1, c.getZ() + 0.5, 8, 0.4, 2, 0.4, 0.02);
            gate.dw.spawnParticles(ParticleTypes.REVERSE_PORTAL, c.getX() + 0.5, c.getY() + 1, c.getZ() + 0.5, 15, 1, 1.5, 1, 0.3);
        }
    }

    // ------------------------------------------------------------------ construcción del mapa

    private static void build(ServerWorld w, BlockPos c, int rank) {
        Block[] th = THEMES[rank];
        BlockState air = Blocks.AIR.getDefaultState();
        BlockPos.Mutable m = new BlockPos.Mutable();
        int cx = c.getX(), cy = c.getY(), cz = c.getZ();
        for (int dx = -RAD - 2; dx <= RAD + 2; dx++) {
            for (int dz = -RAD - 2; dz <= RAD + 2; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > RAD + 1.6) continue;
                boolean wall = d > RAD;
                for (int y = -2; y <= HGT + 1; y++) {
                    BlockState st;
                    if (wall) {
                        boolean lamp = y == 4 && ((dx + dz) & 7) == 0;
                        st = (lamp ? th[5] : (y == 1 || y == HGT / 2 + 2 || RNG.nextInt(100) < 12) ? th[3] : th[2]).getDefaultState();
                    } else if (y == -2 || y == HGT + 1) st = th[2].getDefaultState();
                    else if (y == -1) {
                        int ring = (int) d;
                        st = (d < 2.5 || ring % 5 == 0 || RNG.nextInt(100) < 8 ? th[1] : th[0]).getDefaultState();
                    } else if (y == HGT) {
                        st = (Math.floorMod(dx, 5) == 0 && Math.floorMod(dz, 5) == 0 ? th[5] : th[2]).getDefaultState();
                    } else if (y == 0 && d > RAD - 1.5 && RNG.nextInt(100) < 10) st = th[3].getDefaultState();
                    else st = air;
                    w.setBlockState(m.set(cx + dx, cy + y, cz + dz), st, Block.NOTIFY_LISTENERS);
                }
            }
        }
        for (int k = 0; k < 8; k++) {
            double a = Math.PI / 8 + k * Math.PI / 4;
            int px = (int) Math.round(Math.cos(a) * 10), pz = (int) Math.round(Math.sin(a) * 10);
            for (int ox = 0; ox < 2; ox++) {
                for (int oz = 0; oz < 2; oz++) {
                    for (int y = 0; y < HGT; y++) {
                        Block b = y == HGT - 2 || y == 2 ? th[5] : th[4];
                        w.setBlockState(m.set(cx + px + ox, cy + y, cz + pz + oz), b.getDefaultState(), Block.NOTIFY_LISTENERS);
                    }
                }
            }
        }
    }

    private static Vec3d arrival() {
        BlockPos c = gate.center;
        return new Vec3d(c.getX() + 0.5, c.getY(), c.getZ() + RAD - 3 + 0.5);
    }

    // ------------------------------------------------------------------ jugadores

    private static List<ServerPlayerEntity> stepping(ServerWorld ow) {
        List<ServerPlayerEntity> out = new ArrayList<>();
        double cx = gate.pos.getX() + 0.5, cz = gate.pos.getZ() + 0.5;
        for (ServerPlayerEntity p : ow.getPlayers()) {
            if (p.isSpectator()) continue;
            double dx = p.getX() - cx, dz = p.getZ() - cz, dy = p.getY() - gate.pos.getY();
            if (dx * dx + dz * dz < 2.5 && dy > -1 && dy < 4) out.add(p);
        }
        return out;
    }

    private static List<ServerPlayerEntity> inside() {
        List<ServerPlayerEntity> out = new ArrayList<>();
        if (gate == null || gate.dw == null) return out;
        for (ServerPlayerEntity p : gate.dw.getPlayers()) {
            if (!p.isSpectator() && p.getBlockPos().getSquaredDistance(gate.center) < 40 * 40) out.add(p);
        }
        return out;
    }

    private static void enterPlayer(ServerPlayerEntity p) {
        RETURN.put(p.getUuid(), p.getPos());
        Vec3d a = arrival();
        p.teleport(gate.dw, a.x, a.y, a.z, 180f, 0f);
        p.fallDistance = 0;
        Fx.title(p, Text.literal("⟦ " + THEME_NAME[gate.rank].toUpperCase(Locale.ROOT) + " ⟧").formatted(Formatting.DARK_PURPLE, Formatting.BOLD),
                Text.literal("Mazmorra de Rango ").formatted(Formatting.GRAY).append(rankName(gate.rank)), 10, 50, 15);
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 40, 0, false, false));
        Fx.sound(p, SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, 0.7f, 0.6f);
    }

    private static void back(ServerPlayerEntity p) {
        MinecraftServer s = p.getServer();
        if (s == null) return;
        ServerWorld ow = s.getOverworld();
        Vec3d r = RETURN.remove(p.getUuid());
        if (r == null) {
            BlockPos sp = ow.getSpawnPos();
            r = new Vec3d(sp.getX() + 0.5, ow.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, sp.getX(), sp.getZ()), sp.getZ() + 0.5);
        }
        p.teleport(ow, r.x, r.y, r.z, p.getYaw(), p.getPitch());
        p.fallDistance = 0;
        Fx.part(p, ParticleTypes.REVERSE_PORTAL, 1, 50, 0.5, 0.3);
        Fx.sound(p, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1f, 0.8f);
    }

    // ------------------------------------------------------------------ estados

    private static void waiting(MinecraftServer server, ServerWorld ow) {
        gate.life--;
        if (gate.life == 60) broadcast(server, Text.literal("La Puerta se cerrará en 1 minuto.").formatted(Formatting.GRAY));
        if (gate.life <= 0) {
            broadcast(server, Text.literal("La Puerta se ha cerrado sin ser conquistada.").formatted(Formatting.GRAY));
            close(server);
            return;
        }
        List<ServerPlayerEntity> s = stepping(ow);
        if (s.isEmpty()) return;
        ServerWorld dw = server.getWorld(DUNGEON);
        boolean own = dw != null;
        gate.dw = own ? dw : ow;
        gate.center = own ? new BlockPos(RNG.nextInt(400) * 160 - 32000, 64, RNG.nextInt(400) * 160 - 32000)
                : new BlockPos(gate.pos.getX(), 300, gate.pos.getZ());
        build(gate.dw, gate.center, gate.rank);
        gate.state = 2;
        gate.bar = new ServerBossBar(Text.literal("Mazmorra"), BossBar.Color.PURPLE, BossBar.Style.NOTCHED_10);
        for (ServerPlayerEntity p : s) enterPlayer(p);
        nextWave();
    }

    private static MobEntity spawnAt(ServerWorld w, EntityType<?> type, double x, double y, double z) {
        Entity e = type.create(w);
        if (!(e instanceof MobEntity m)) return null;
        m.refreshPositionAndAngles(x, y, z, RNG.nextFloat() * 360, 0);
        m.initialize(w, w.getLocalDifficulty(m.getBlockPos()), SpawnReason.MOB_SUMMONED, null);
        m.setPersistent();
        m.addCommandTag("sl_gate_mob");
        EntityAttributeInstance fr = m.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
        if (fr != null) fr.setBaseValue(40);
        w.spawnEntity(m);
        w.spawnParticles(ParticleTypes.REVERSE_PORTAL, m.getX(), m.getY() + 1, m.getZ(), 20, 0.3, 0.6, 0.3, 0.2);
        return m;
    }

    private static MobEntity spawnInArena(EntityType<?> type, double minR, double maxR) {
        double a = RNG.nextDouble() * Math.PI * 2, r = minR + RNG.nextDouble() * (maxR - minR);
        BlockPos c = gate.center;
        return spawnAt(gate.dw, type, c.getX() + 0.5 + Math.cos(a) * r, c.getY(), c.getZ() + 0.5 + Math.sin(a) * r);
    }

    private static void nextWave() {
        gate.wave++;
        gate.mobs.clear();
        if (gate.wave >= 4) {
            spawnBoss();
            return;
        }
        int n = 4 + gate.rank * 2;
        EntityType<?>[] pool = WAVES[gate.rank];
        for (int i = 0; i < n; i++) {
            MobEntity m = spawnInArena(pool[RNG.nextInt(pool.length)], 3, 8);
            if (m == null) continue;
            buff(m, HP_MUL[gate.rank], ATK_MUL[gate.rank]);
            Mobs.decorate(m);
            gate.mobs.add(m.getUuid());
        }
        gate.bar.setName(Text.literal("⚔ " + THEME_NAME[gate.rank] + " · Rango ").formatted(Formatting.WHITE)
                .append(rankName(gate.rank))
                .append(Text.literal(" · Oleada " + gate.wave + "/3").formatted(Formatting.WHITE)));
        gate.bar.setColor(BossBar.Color.PURPLE);
        gate.bar.setPercent(1f);
        for (ServerPlayerEntity p : inside()) {
            Fx.msg(p, Text.literal("¡Oleada " + gate.wave + "/3! ").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD)
                    .append(Text.literal(n + " monstruos emergen de las sombras.").formatted(Formatting.GRAY)));
            Fx.sound(p, SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, 0.8f, 0.7f);
        }
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

    private static void spawnBoss() {
        int r = gate.rank;
        MobEntity m = spawnInArena(BOSS_TYPE[r], 0, 1);
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
        for (ServerPlayerEntity p : inside()) {
            Fx.title(p, Text.literal("⚠ JEFE DE LA MAZMORRA ⚠").formatted(Formatting.DARK_RED, Formatting.BOLD),
                    Text.literal(BOSS_NAME[r]).formatted(RANK_FMT[r]), 5, 50, 15);
            Fx.sound(p, SoundEvents.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 0.7f);
        }
        gate.dw.spawnParticles(ParticleTypes.LARGE_SMOKE, m.getX(), m.getY() + 1, m.getZ(), 60, 0.8, 1, 0.8, 0.05);
    }

    private static void dungeon(MinecraftServer server) {
        List<ServerPlayerEntity> in = inside();
        gate.bar.clearPlayers();
        BlockPos c = gate.center;
        for (ServerPlayerEntity p : in) {
            gate.bar.addPlayer(p);
            if (p.getY() < c.getY() - 6) {
                Vec3d a = arrival();
                p.teleport(gate.dw, a.x, a.y, a.z, 180f, 0f);
                p.fallDistance = 0;
            }
        }
        if (in.isEmpty()) {
            if (++gate.away >= 90) {
                dungeonBreak(server);
                return;
            }
        } else gate.away = 0;

        int alive = 0;
        Iterator<UUID> it = gate.mobs.iterator();
        while (it.hasNext()) {
            Entity e = gate.dw.getEntity(it.next());
            if (e == null || !e.isAlive()) {
                it.remove();
                continue;
            }
            alive++;
            if (e.getBlockPos().getSquaredDistance(c) > 20 * 20) {
                e.refreshPositionAndAngles(c.getX() + 0.5, c.getY(), c.getZ() + 0.5, e.getYaw(), e.getPitch());
            }
        }

        if (gate.wave >= 4) {
            Entity boss = gate.boss == null ? null : gate.dw.getEntity(gate.boss);
            if (boss instanceof MobEntity b && b.isAlive()) {
                gate.bar.setPercent(Math.max(0f, Math.min(1f, b.getHealth() / b.getMaxHealth())));
            } else {
                clear(server, in);
            }
            return;
        }
        int total = 4 + gate.rank * 2;
        gate.bar.setPercent(Math.max(0f, Math.min(1f, alive / (float) total)));
        if (alive == 0) nextWave();
    }

    private static void clear(MinecraftServer server, List<ServerPlayerEntity> in) {
        int r = gate.rank;
        int xp = (r + 1) * (r + 1) * 100;
        int pts = r + 1;
        for (ServerPlayerEntity p : in) {
            PlayerData d = DataState.of(p);
            d.points += pts;
            d.gold += (r + 1) * 120;
            Story.progress(p, 2, 1);
            Fx.title(p, Text.literal("MAZMORRA COMPLETADA").formatted(Formatting.GOLD, Formatting.BOLD),
                    Text.literal("+" + xp + " XP · +" + pts + " puntos").formatted(Formatting.YELLOW), 10, 70, 20);
            Fx.sound(p, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            Fx.sound(p, SoundEvents.ENTITY_PLAYER_LEVELUP, 1f, 0.8f);
            Fx.part(p, ParticleTypes.TOTEM_OF_UNDYING, 1, 80, 0.8, 0.4);
            Fx.msg(p, Text.literal("Has conquistado la Puerta de Rango ").formatted(Formatting.WHITE)
                    .append(rankName(r))
                    .append(Text.literal(". Recompensas: +" + xp + " XP, +" + pts + " puntos, oro y botín. El portal de salida se abrirá en 5 segundos.").formatted(Formatting.YELLOW)));
            cmd(server, "loot give " + p.getGameProfile().getName() + " loot minecraft:" + LOOT[r]);
            if (r == 5) cmd(server, "give " + p.getGameProfile().getName() + " minecraft:nether_star");
            Leveling.addXp(p, xp);
        }
        gate.bar.clearPlayers();
        gate.state = 3;
        gate.endTimer = 6;
    }

    private static void dungeonBreak(MinecraftServer server) {
        broadcast(server, Text.literal("⚠ ¡RUPTURA DE MAZMORRA! Los monstruos de la Puerta han escapado al mundo.").formatted(Formatting.RED));
        ServerWorld ow = server.getOverworld();
        EntityType<?>[] pool = WAVES[gate.rank];
        for (int i = 0; i < 3 + gate.rank; i++) {
            int x = gate.pos.getX() + RNG.nextInt(9) - 4, z = gate.pos.getZ() + RNG.nextInt(9) - 4;
            MobEntity m = spawnAt(ow, pool[RNG.nextInt(pool.length)], x + 0.5, ow.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z), z + 0.5);
            if (m != null) {
                buff(m, HP_MUL[gate.rank], ATK_MUL[gate.rank]);
                Mobs.decorate(m);
            }
        }
        close(server);
    }

    public static void close(MinecraftServer server) {
        if (gate != null) {
            if (gate.dw != null) {
                for (UUID id : gate.mobs) {
                    Entity e = gate.dw.getEntity(id);
                    if (e != null) e.discard();
                }
                for (ServerPlayerEntity p : inside()) back(p);
            }
            if (gate.bar != null) {
                gate.bar.clearPlayers();
                gate.bar.setVisible(false);
            }
        }
        gate = null;
        timer = 0;
        cmd(server, "kill @e[type=minecraft:text_display,tag=sl_gate_label]");
    }

    private Gates() {}
}
