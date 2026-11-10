package com.sololeveling;

import com.sololeveling.mixin.MobEntityAccessor;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

import java.util.*;

public final class Shadows {

    public static final String TAG = "sl_shadow";
    public static final String OWNER_PREFIX = "sl_owner_";
    private static final Identifier ID_HP = Identifier.of(SoloLeveling.MOD, "shadow_health");
    private static final Identifier ID_ATK = Identifier.of(SoloLeveling.MOD, "shadow_attack");

    /** Tipos de monstruo que se pueden extraer, con su nombre. */
    public static final Map<EntityType<?>, String> EXTRACTABLE = new LinkedHashMap<>();

    static {
        EXTRACTABLE.put(EntityType.ZOMBIE, "Zombi");
        EXTRACTABLE.put(EntityType.HUSK, "Momia");
        EXTRACTABLE.put(EntityType.DROWNED, "Ahogado");
        EXTRACTABLE.put(EntityType.SKELETON, "Esqueleto");
        EXTRACTABLE.put(EntityType.STRAY, "Esqueleto Glacial");
        EXTRACTABLE.put(EntityType.WITHER_SKELETON, "Caballero Wither");
        EXTRACTABLE.put(EntityType.SPIDER, "Araña");
        EXTRACTABLE.put(EntityType.CAVE_SPIDER, "Araña de Cueva");
        EXTRACTABLE.put(EntityType.VINDICATOR, "Vindicador");
        EXTRACTABLE.put(EntityType.PILLAGER, "Saqueador");
        EXTRACTABLE.put(EntityType.RAVAGER, "Devastador");
        EXTRACTABLE.put(EntityType.BLAZE, "Blaze");
    }

    /** Caché reconstruida cada segundo: dueño -> sombras cargadas. */
    private static final Map<UUID, List<MobEntity>> ARMY = new HashMap<>();

    // ------------------------------------------------------------------ utilidades

    public static boolean isShadow(Entity e) {
        return e != null && e.getCommandTags().contains(TAG);
    }

    public static UUID ownerOf(Entity e) {
        for (String t : e.getCommandTags()) {
            if (t.startsWith(OWNER_PREFIX)) {
                try {
                    return UUID.fromString(t.substring(OWNER_PREFIX.length()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return null;
    }

    public static ServerPlayerEntity owner(Entity e) {
        UUID id = ownerOf(e);
        MinecraftServer server = e.getServer();
        if (id == null || server == null) return null;
        return server.getPlayerManager().getPlayer(id);
    }

    public static List<MobEntity> army(ServerPlayerEntity p) {
        return ARMY.getOrDefault(p.getUuid(), Collections.emptyList());
    }

    public static int count(ServerPlayerEntity p) {
        return army(p).size();
    }

    public static void rebuild(MinecraftServer server) {
        ARMY.clear();
        for (ServerWorld w : server.getWorlds()) {
            for (Entity e : w.iterateEntities()) {
                if (e instanceof MobEntity m && m.isAlive() && isShadow(m)) {
                    UUID o = ownerOf(m);
                    if (o != null) ARMY.computeIfAbsent(o, k -> new ArrayList<>()).add(m);
                }
            }
        }
        // Las sombras siguen a su señor incluso entre dimensiones
        for (Map.Entry<UUID, List<MobEntity>> en : ARMY.entrySet()) {
            ServerPlayerEntity p = server.getPlayerManager().getPlayer(en.getKey());
            if (p == null || p.isSpectator()) continue;
            for (MobEntity m : en.getValue()) {
                if (m.getWorld() != p.getWorld()) {
                    m.teleportTo(new TeleportTarget(p.getServerWorld(), p.getPos(), Vec3d.ZERO, m.getYaw(), 0, TeleportTarget.NO_OP));
                }
            }
        }
    }

    // ------------------------------------------------------------------ IA

    public static void setupGoals(MobEntity m) {
        MobEntityAccessor acc = (MobEntityAccessor) m;
        acc.sl$getTargetSelector().clear(g -> true);
        acc.sl$getGoalSelector().clear(g -> g instanceof GuardGoal || g instanceof FollowGoal);
        acc.sl$getGoalSelector().add(1, new GuardGoal(m));
        acc.sl$getGoalSelector().add(3, new FollowGoal(m));
        if (m.getTarget() instanceof PlayerEntity) m.setTarget(null);
    }

    private static boolean validEnemy(MobEntity self, LivingEntity t) {
        if (t == null || !t.isAlive() || t == self) return false;
        if (t instanceof PlayerEntity) return false;
        if (isShadow(t)) return false;
        return t.getWorld() == self.getWorld() && self.squaredDistanceTo(t) < 32 * 32;
    }

    /** Elige el objetivo: quien ataca al señor, a quien ataca el señor, o el monstruo más cercano. */
    static class GuardGoal extends Goal {
        private final MobEntity mob;
        private int cooldown;

        GuardGoal(MobEntity mob) {
            this.mob = mob;
        }

        @Override
        public boolean canStart() {
            return true;
        }

        @Override
        public boolean shouldContinue() {
            return true;
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity cur = mob.getTarget();
            if (cur != null && !validEnemy(mob, cur)) {
                mob.setTarget(null);
                cur = null;
            }
            if (--cooldown > 0) return;
            cooldown = 10;
            ServerPlayerEntity o = owner(mob);
            if (o != null) {
                LivingEntity att = o.getAttacker();
                if (validEnemy(mob, att)) {
                    mob.setTarget(att);
                    return;
                }
                LivingEntity hit = o.getAttacking();
                if (validEnemy(mob, hit) && o.age - o.getLastAttackTime() < 100) {
                    mob.setTarget(hit);
                    return;
                }
            }
            if (cur != null) return;
            List<MobEntity> near = mob.getWorld().getEntitiesByClass(MobEntity.class, mob.getBoundingBox().expand(16),
                    e -> e instanceof Monster && !isShadow(e) && e.isAlive());
            MobEntity best = null;
            double bd = Double.MAX_VALUE;
            for (MobEntity e : near) {
                double dd = mob.squaredDistanceTo(e);
                if (dd < bd) {
                    bd = dd;
                    best = e;
                }
            }
            if (best != null) mob.setTarget(best);
        }
    }

    /** Sigue al señor cuando no hay combate. */
    static class FollowGoal extends Goal {
        private final MobEntity mob;

        FollowGoal(MobEntity mob) {
            this.mob = mob;
            setControls(EnumSet.of(Goal.Control.MOVE));
        }

        private boolean ok(double minDist) {
            ServerPlayerEntity o = owner(mob);
            return o != null && !o.isSpectator() && mob.getTarget() == null
                    && o.getWorld() == mob.getWorld() && mob.squaredDistanceTo(o) > minDist * minDist;
        }

        @Override
        public boolean canStart() {
            return ok(8);
        }

        @Override
        public boolean shouldContinue() {
            return ok(3);
        }

        @Override
        public void tick() {
            ServerPlayerEntity o = owner(mob);
            if (o == null) return;
            if (mob.squaredDistanceTo(o) > 28 * 28) {
                mob.getNavigation().stop();
                mob.refreshPositionAndAngles(o.getX(), o.getY(), o.getZ(), mob.getYaw(), mob.getPitch());
            } else if (mob.age % 10 == 0) {
                mob.getNavigation().startMovingTo(o, 1.3);
            }
        }

        @Override
        public void stop() {
            mob.getNavigation().stop();
        }
    }

    // ------------------------------------------------------------------ habilidad ¡Levántate!

    public static void arise(ServerPlayerEntity p) {
        PlayerData d = DataState.of(p);
        if (p.isSneaking()) {
            recall(p);
            return;
        }
        List<Souls.Soul> near = Souls.near(p, 8);
        if (near.isEmpty()) {
            Fx.bar(p, Text.literal("No hay almas que extraer cerca.").formatted(Formatting.GRAY));
            return;
        }
        if (!Skills.pay(p, 3)) return;

        Fx.title(p, Text.literal("¡LEVÁNTATE!").formatted(Formatting.DARK_PURPLE, Formatting.BOLD), Text.empty(), 2, 20, 8);
        Fx.sound(p, SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, 1f, 0.6f);
        int count = count(p);
        int max = d.shadowMax();
        for (Souls.Soul s : near) {
            if (count >= max) {
                Fx.msg(p, Text.literal("Tu ejército está al máximo (" + max + "). Sube de nivel o INT para comandar más sombras.").formatted(Formatting.GRAY));
                break;
            }
            ServerWorld w = p.getServerWorld();
            int chance = 45 + d.level / 2 - (s.elite ? 25 : 0);
            if (p.getRandom().nextInt(100) < chance) {
                if (raise(p, s)) count++;
                Souls.remove(s);
            } else {
                s.tries++;
                Fx.part(w, ParticleTypes.SMOKE, s.pos.x, s.pos.y + 0.5, s.pos.z, 20, 0.3, 0.5, 0.3, 0.02);
                Fx.sound(w, s.pos.x, s.pos.y, s.pos.z, SoundEvents.BLOCK_FIRE_EXTINGUISH, 0.6f, 0.6f);
                if (s.tries >= 3) {
                    Souls.remove(s);
                    Fx.msg(p, Text.literal("El alma de " + s.name + " se ha desvanecido.").formatted(Formatting.GRAY));
                } else {
                    Fx.msg(p, Text.literal("La extracción ha fallado. Intentos restantes: " + (3 - s.tries)).formatted(Formatting.GRAY));
                }
            }
        }
        Leveling.sync(p);
    }

    private static boolean raise(ServerPlayerEntity p, Souls.Soul s) {
        ServerWorld w = p.getServerWorld();
        Entity e = s.type.create(w);
        if (!(e instanceof MobEntity m)) return false;
        PlayerData d = DataState.of(p);
        m.refreshPositionAndAngles(s.pos.x, s.pos.y, s.pos.z, p.getYaw(), 0);
        m.initialize(w, w.getLocalDifficulty(m.getBlockPos()), SpawnReason.MOB_SUMMONED, null);
        if (m instanceof ZombieEntity z) z.setBaby(false);

        m.addCommandTag(TAG);
        m.addCommandTag(OWNER_PREFIX + p.getUuidAsString());
        m.setPersistent();
        m.setCanPickUpLoot(false);
        String prefix = s.elite ? "Comandante" : "Sombra";
        m.setCustomName(Text.literal(prefix + " · " + s.name).formatted(Formatting.DARK_PURPLE, Formatting.BOLD));

        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        helmet.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x141414, false));
        m.equipStack(EquipmentSlot.HEAD, helmet);
        m.setEquipmentDropChance(EquipmentSlot.HEAD, 0f);
        if (m instanceof ZombieEntity && m.getMainHandStack().isEmpty()) {
            m.equipStack(EquipmentSlot.MAINHAND, new ItemStack(s.elite ? Items.NETHERITE_SWORD : Items.IRON_SWORD));
        }
        m.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0f);

        double hpMul = d.level * 0.04 + (s.elite ? 1.0 : 0);
        double atkMul = d.level * 0.02 + (s.elite ? 0.5 : 0);
        addMul(m, EntityAttributes.GENERIC_MAX_HEALTH, ID_HP, hpMul);
        addMul(m, EntityAttributes.GENERIC_ATTACK_DAMAGE, ID_ATK, atkMul);
        EntityAttributeInstance fr = m.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
        if (fr != null) fr.setBaseValue(32);
        if (s.elite) {
            EntityAttributeInstance sc = m.getAttributeInstance(EntityAttributes.GENERIC_SCALE);
            if (sc != null) sc.setBaseValue(1.35);
            m.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, StatusEffectInstance.INFINITE, 0, true, false));
        }
        m.setHealth(m.getMaxHealth());
        m.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, StatusEffectInstance.INFINITE, 0, true, false));
        m.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, StatusEffectInstance.INFINITE, 0, true, false));

        Scoreboard sb = w.getScoreboard();
        Team team = sb.getTeam("sl_shadows");
        if (team == null) {
            team = sb.addTeam("sl_shadows");
            team.setColor(Formatting.DARK_PURPLE);
        }
        sb.addScoreHolderToTeam(m.getNameForScoreboard(), team);

        w.spawnEntity(m);

        Fx.part(w, ParticleTypes.SCULK_SOUL, s.pos.x, s.pos.y + 1, s.pos.z, 40, 0.4, 1, 0.4, 0.05);
        Fx.part(w, ParticleTypes.REVERSE_PORTAL, s.pos.x, s.pos.y + 1, s.pos.z, 80, 0.5, 1, 0.5, 0.5);
        Fx.part(w, ParticleTypes.LARGE_SMOKE, s.pos.x, s.pos.y + 0.5, s.pos.z, 30, 0.5, 0.5, 0.5, 0.02);
        Fx.sound(w, s.pos.x, s.pos.y, s.pos.z, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, 1f, 0.6f);
        Fx.msg(p, Text.literal("¡Extracción exitosa! ").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD)
                .append(Text.literal(prefix + " · " + s.name).formatted(Formatting.DARK_PURPLE))
                .append(Text.literal(" se une a tu ejército.").formatted(Formatting.GRAY)));
        return true;
    }

    private static void addMul(MobEntity m, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attr,
                               Identifier id, double v) {
        EntityAttributeInstance inst = m.getAttributeInstance(attr);
        if (inst == null || v == 0) return;
        inst.removeModifier(id);
        inst.addPersistentModifier(new EntityAttributeModifier(id, v, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    public static void recall(ServerPlayerEntity p) {
        List<MobEntity> list = army(p);
        if (list.isEmpty()) {
            Fx.bar(p, Text.literal("No tienes sombras.").formatted(Formatting.GRAY));
            return;
        }
        for (MobEntity m : list) {
            if (m.getWorld() == p.getWorld()) {
                m.setTarget(null);
                m.getNavigation().stop();
                m.refreshPositionAndAngles(p.getX(), p.getY(), p.getZ(), m.getYaw(), m.getPitch());
            }
        }
        Fx.part(p, ParticleTypes.LARGE_SMOKE, 0.5, 60, 1.5, 0.02);
        Fx.part(p, ParticleTypes.SCULK_SOUL, 0.5, 30, 1.5, 0.02);
        Fx.sound(p, SoundEvents.ENTITY_EVOKER_CAST_SPELL, 1f, 0.6f);
        Fx.bar(p, Text.literal("Tu ejército acude a tu sombra.").formatted(Formatting.DARK_PURPLE));
    }

    public static void buffArmy(ServerPlayerEntity p, int ticks) {
        for (MobEntity m : army(p)) {
            m.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, ticks, 1));
            m.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, ticks, 1));
            m.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, ticks, 1));
            Fx.part(m, ParticleTypes.SCULK_SOUL, 1, 10, 0.4, 0.02);
        }
    }

    public static void teleportArmy(ServerPlayerEntity p) {
        for (MobEntity m : army(p)) {
            if (m.getWorld() == p.getWorld()) {
                m.getNavigation().stop();
                m.refreshPositionAndAngles(p.getX(), p.getY(), p.getZ(), m.getYaw(), m.getPitch());
            }
        }
    }

    private Shadows() {}
}
