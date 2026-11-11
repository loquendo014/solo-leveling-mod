package com.sololeveling;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** Bestias con modelo propio: Hormiga, Rey Hormiga, Cerbero, Coloso de Piedra y Caballero Rojo Sangre. */
public class SlMob extends HostileEntity {
    private int skillCd = 60;

    public SlMob(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 20;
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.add(6, new WanderAroundFarGoal(this, 0.8));
        goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8f));
        goalSelector.add(8, new LookAroundGoal(this));
        targetSelector.add(1, new RevengeGoal(this));
        targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    private boolean is(EntityType<?> t) {
        return getType() == t;
    }

    private boolean enemy(LivingEntity e) {
        if (e == this || !e.isAlive()) return false;
        if (Shadows.isShadow(this)) return e instanceof Monster && !Shadows.isShadow(e);
        if (e instanceof PlayerEntity p) return !p.isCreative() && !p.isSpectator();
        return Shadows.isShadow(e);
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit && target instanceof LivingEntity l) {
            if (is(ModEntities.ANT)) l.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 60, 0));
            else if (is(ModEntities.ANT_KING)) {
                l.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 1));
                l.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 0));
            } else if (is(ModEntities.CERBERUS)) l.setOnFireFor(4);
            else if (is(ModEntities.STONE_GOLEM)) {
                l.addVelocity(0, 0.5, 0);
                l.velocityModified = true;
            } else if (is(ModEntities.BLOOD_KNIGHT)) l.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 40, 0));
        }
        return hit;
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if (!(getWorld() instanceof ServerWorld w)) return;
        LivingEntity t = getTarget();
        if (t == null || !t.isAlive() || --skillCd > 0) return;
        double d2 = squaredDistanceTo(t);
        float dmg = (float) getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (is(ModEntities.STONE_GOLEM) && d2 < 36) {
            for (LivingEntity e : w.getEntitiesByClass(LivingEntity.class, getBoundingBox().expand(5), this::enemy)) {
                e.damage(w.getDamageSources().mobAttack(this), dmg * 0.8f);
                Vec3d push = e.getPos().subtract(getPos()).normalize();
                e.addVelocity(push.x, 0.7, push.z);
                e.velocityModified = true;
            }
            w.spawnParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.2, getZ(), 6, 2, 0.1, 2, 0);
            w.spawnParticles(ParticleTypes.CLOUD, getX(), getY() + 0.2, getZ(), 40, 2.5, 0.1, 2.5, 0.05);
            Fx.sound(w, getX(), getY(), getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 0.7f, 0.6f);
        } else if (is(ModEntities.CERBERUS) && d2 < 100) {
            Vec3d from = getEyePos(), dir = t.getEyePos().subtract(from);
            for (int i = 1; i <= 10; i++) {
                Vec3d p = from.add(dir.multiply(i / 10.0));
                w.spawnParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 4, 0.2, 0.2, 0.2, 0.02);
            }
            t.setOnFireFor(5);
            t.damage(w.getDamageSources().mobAttack(this), dmg * 0.5f);
            Fx.sound(w, getX(), getY(), getZ(), SoundEvents.ENTITY_BLAZE_SHOOT, 1f, 0.6f);
        } else if (is(ModEntities.BLOOD_KNIGHT) && d2 > 9 && d2 < 196) {
            Vec3d dir = t.getPos().subtract(getPos()).normalize();
            addVelocity(dir.x * 1.6, 0.25, dir.z * 1.6);
            velocityModified = true;
            w.spawnParticles(ParticleTypes.SWEEP_ATTACK, getX(), getY() + 1, getZ(), 3, 0.3, 0.3, 0.3, 0);
            Fx.sound(w, getX(), getY(), getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.7f);
        } else if (is(ModEntities.ANT_KING) && d2 < 400 && !Shadows.isShadow(this)
                && w.getEntitiesByClass(SlMob.class, getBoundingBox().expand(16), e -> e.is(ModEntities.ANT)).size() < 6) {
            for (int i = 0; i < 2; i++) {
                SlMob a = ModEntities.ANT.create(w);
                if (a == null) continue;
                a.refreshPositionAndAngles(getX() + random.nextInt(5) - 2, getY(), getZ() + random.nextInt(5) - 2, 0, 0);
                a.initialize(w, w.getLocalDifficulty(a.getBlockPos()), SpawnReason.MOB_SUMMONED, null);
                a.setTarget(t);
                w.spawnEntity(a);
            }
            Fx.sound(w, getX(), getY(), getZ(), SoundEvents.ENTITY_RAVAGER_ROAR, 1f, 1.6f);
        } else return;
        skillCd = 100;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (is(ModEntities.ANT) || is(ModEntities.ANT_KING)) return SoundEvents.ENTITY_SPIDER_AMBIENT;
        if (is(ModEntities.CERBERUS)) return SoundEvents.ENTITY_WOLF_GROWL;
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        if (is(ModEntities.STONE_GOLEM)) return SoundEvents.ENTITY_IRON_GOLEM_HURT;
        if (is(ModEntities.CERBERUS)) return SoundEvents.ENTITY_WOLF_HURT;
        return super.getHurtSound(source);
    }

    @Override
    protected SoundEvent getDeathSound() {
        if (is(ModEntities.STONE_GOLEM)) return SoundEvents.ENTITY_IRON_GOLEM_DEATH;
        if (is(ModEntities.CERBERUS)) return SoundEvents.ENTITY_WOLF_DEATH;
        if (is(ModEntities.ANT) || is(ModEntities.ANT_KING)) return SoundEvents.ENTITY_SPIDER_DEATH;
        return super.getDeathSound();
    }
}
