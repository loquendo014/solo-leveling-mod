package com.sololeveling;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;
import net.minecraft.world.Heightmap;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/** Bestias mágicas (variantes con nombre y tamaño), manadas nocturnas y el Mercader de la Asociación. */
public final class Mobs {
    private static final Identifier ID_HP = Identifier.of(SoloLeveling.MOD, "magic_health");
    private static final Identifier ID_ATK = Identifier.of(SoloLeveling.MOD, "magic_attack");
    private static final Map<EntityType<?>, String> NAMES = new HashMap<>();
    private static final Map<EntityType<?>, Double> SCALES = new HashMap<>();
    private static final Random RNG = new Random();
    private static final EntityType<?>[][] PACKS = {
            {EntityType.ZOMBIE, EntityType.SILVERFISH},
            {EntityType.ZOMBIE, EntityType.SPIDER, EntityType.SILVERFISH},
            {EntityType.STRAY, EntityType.SPIDER, EntityType.VINDICATOR},
            {EntityType.VINDICATOR, EntityType.WITHER_SKELETON, EntityType.PIGLIN_BRUTE},
            {EntityType.WITHER_SKELETON, EntityType.PIGLIN_BRUTE, EntityType.BLAZE},
            {EntityType.PIGLIN_BRUTE, EntityType.WITHER_SKELETON, EntityType.RAVAGER},
    };

    private static void def(EntityType<?> t, String name, double scale) {
        NAMES.put(t, name);
        SCALES.put(t, scale);
    }

    static {
        def(EntityType.ZOMBIE, "Trasgo", 0.8);
        def(EntityType.HUSK, "Trasgo del Desierto", 0.85);
        def(EntityType.SKELETON, "Arquero Esqueleto", 1.0);
        def(EntityType.STRAY, "Elfo de Hielo", 1.1);
        def(EntityType.SPIDER, "Araña Gigante", 1.4);
        def(EntityType.SILVERFISH, "Hormiga Mutante", 3.0);
        def(EntityType.VINDICATOR, "Orco Guerrero", 1.15);
        def(EntityType.PILLAGER, "Orco Ballestero", 1.1);
        def(EntityType.EVOKER, "Chamán Orco", 1.1);
        def(EntityType.PIGLIN_BRUTE, "Gran Orco", 1.35);
        def(EntityType.WITHER_SKELETON, "Caballero No-Muerto", 1.0);
        def(EntityType.BLAZE, "Demonio de Fuego", 1.2);
        def(EntityType.RAVAGER, "Cerbero", 1.1);
    }

    public static void decorate(MobEntity m) {
        String name = NAMES.get(m.getType());
        if (name == null) return;
        m.setCustomName(Text.literal(name).formatted(Formatting.RED));
        EntityAttributeInstance sc = m.getAttributeInstance(EntityAttributes.GENERIC_SCALE);
        if (sc != null) sc.setBaseValue(SCALES.get(m.getType()));
        if (m.getType() == EntityType.SILVERFISH) {
            EntityAttributeInstance hp = m.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
            if (hp != null) hp.setBaseValue(24);
            EntityAttributeInstance at = m.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (at != null) at.setBaseValue(4);
            m.setHealth(m.getMaxHealth());
        }
        if (m instanceof AbstractPiglinEntity pg) pg.setImmuneToZombification(true);
    }

    public static void tick(MinecraftServer server, int t) {
        if (t % 900 != 0) return;
        ServerWorld w = server.getOverworld();
        for (ServerPlayerEntity p : w.getPlayers()) {
            if (p.isSpectator()) continue;
            PlayerData d = DataState.of(p);
            if (d.level < 5) continue;
            int roll = RNG.nextInt(100);
            if (roll < 4) {
                merchant(w, p);
                continue;
            }
            if (w.isDay() || roll > 40) continue;
            if (!w.getEntitiesByClass(MobEntity.class, p.getBoundingBox().expand(48),
                    e -> e.getCommandTags().contains("sl_magic")).isEmpty()) continue;
            pack(w, p, Math.min(5, d.rank()));
        }
    }

    private static void pack(ServerWorld w, ServerPlayerEntity p, int rank) {
        EntityType<?> type = PACKS[rank][RNG.nextInt(PACKS[rank].length)];
        double a = RNG.nextDouble() * Math.PI * 2, dist = 18 + RNG.nextInt(10);
        int bx = (int) Math.floor(p.getX() + Math.cos(a) * dist), bz = (int) Math.floor(p.getZ() + Math.sin(a) * dist);
        int n = type == EntityType.RAVAGER ? 1 : 2 + RNG.nextInt(3);
        for (int i = 0; i < n; i++) {
            int x = bx + RNG.nextInt(5) - 2, z = bz + RNG.nextInt(5) - 2;
            int y = w.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (Math.abs(y - p.getY()) > 12) return;
            Entity e = type.create(w);
            if (!(e instanceof MobEntity m)) return;
            m.refreshPositionAndAngles(x + 0.5, y, z + 0.5, RNG.nextFloat() * 360, 0);
            m.initialize(w, w.getLocalDifficulty(m.getBlockPos()), SpawnReason.EVENT, null);
            decorate(m);
            mul(m, EntityAttributes.GENERIC_MAX_HEALTH, ID_HP, rank * 0.3);
            mul(m, EntityAttributes.GENERIC_ATTACK_DAMAGE, ID_ATK, rank * 0.2);
            m.setHealth(m.getMaxHealth());
            m.addCommandTag("sl_magic");
            m.setTarget(p);
            w.spawnEntity(m);
        }
        Fx.bar(p, Text.literal("Sientes la presencia de bestias mágicas cerca...").formatted(Formatting.DARK_RED));
    }

    private static void mul(MobEntity m, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attr,
                            Identifier id, double v) {
        EntityAttributeInstance i = m.getAttributeInstance(attr);
        if (i == null || v <= 0 || i.hasModifier(id)) return;
        i.addPersistentModifier(new EntityAttributeModifier(id, v, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    private static TradeOffer offer(net.minecraft.item.Item cost, int n, ItemStack sell) {
        return new TradeOffer(new TradedItem(cost, n), sell, 8, 1, 0f);
    }

    private static void merchant(ServerWorld w, ServerPlayerEntity p) {
        WanderingTraderEntity tr = EntityType.WANDERING_TRADER.create(w);
        if (tr == null) return;
        double a = RNG.nextDouble() * Math.PI * 2;
        int x = (int) Math.floor(p.getX() + Math.cos(a) * 6), z = (int) Math.floor(p.getZ() + Math.sin(a) * 6);
        int y = w.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (Math.abs(y - p.getY()) > 8) return;
        tr.refreshPositionAndAngles(x + 0.5, y, z + 0.5, 0, 0);
        tr.setDespawnDelay(6000);
        tr.setCustomName(Text.literal("Mercader de la Asociación").formatted(Formatting.GOLD));
        tr.setCustomNameVisible(true);
        TradeOfferList o = tr.getOffers();
        o.clear();
        o.add(offer(ModItems.MANA_STONE, 2, new ItemStack(ModItems.HEALTH_POTION)));
        o.add(offer(ModItems.MANA_STONE, 2, new ItemStack(ModItems.MANA_POTION)));
        o.add(offer(ModItems.MANA_STONE, 6, new ItemStack(ModItems.RANDOM_BOX)));
        o.add(offer(ModItems.MANA_STONE, 10, new ItemStack(ModItems.GATE_KEY)));
        o.add(offer(ModItems.MANA_STONE, 16, new ItemStack(Items.DIAMOND, 2)));
        o.add(offer(ModItems.ESSENCE_STONE, 1, new ItemStack(ModItems.ELIXIR)));
        o.add(offer(ModItems.ESSENCE_STONE, 3, new ItemStack(ModItems.POWER_FRAGMENT)));
        w.spawnEntity(tr);
        Fx.msg(p, Text.literal("Un Mercader de la Asociación ha llegado cerca de ti. Acepta piedras de maná y de esencia.").formatted(Formatting.GOLD));
        Fx.sound(p, SoundEvents.ENTITY_WANDERING_TRADER_YES, 1f, 1f);
    }

    private Mobs() {}
}
