package com.sololeveling;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModEntities {
    public static EntityType<SlMob> ANT, ANT_KING, CERBERUS, STONE_GOLEM, BLOOD_KNIGHT;

    private static EntityType<SlMob> reg(String id, float w, float h, boolean fire, double hp, double dmg, double speed,
                                         double armor, double kb, double scale) {
        EntityType.Builder<SlMob> b = EntityType.Builder.<SlMob>create(SlMob::new, SpawnGroup.MONSTER).dimensions(w, h).maxTrackingRange(10);
        if (fire) b = b.makeFireImmune();
        EntityType<SlMob> t = Registry.register(Registries.ENTITY_TYPE, Identifier.of(SoloLeveling.MOD, id), b.build(id));
        FabricDefaultAttributeRegistry.register(t, HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, hp).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, dmg)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, speed).add(EntityAttributes.GENERIC_ARMOR, armor)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, kb).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32)
                .add(EntityAttributes.GENERIC_SCALE, scale));
        return t;
    }

    public static void init() {
        ANT = reg("ant", 1.1f, 0.9f, false, 24, 5, 0.32, 4, 0.2, 1.0);
        ANT_KING = reg("ant_king", 1.1f, 0.9f, false, 300, 14, 0.36, 12, 0.7, 2.2);
        CERBERUS = reg("cerberus", 1.3f, 1.4f, true, 200, 12, 0.33, 8, 0.6, 1.0);
        STONE_GOLEM = reg("stone_golem", 1.6f, 2.2f, false, 260, 16, 0.22, 16, 1.0, 1.0);
        BLOOD_KNIGHT = reg("blood_knight", 0.7f, 2.1f, false, 220, 13, 0.3, 14, 0.6, 1.0);
    }

    private ModEntities() {}
}
