package com.sololeveling.client;

import com.sololeveling.ModEntities;
import com.sololeveling.SlMob;
import com.sololeveling.SoloLeveling;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

/** Generado: modelos 3D de las bestias. */
public final class ModelDefs {
    public static TexturedModelData ant() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("thorax", ModelPartBuilder.create().uv(36, 0).cuboid(-3.0F, -2.0F, -4.0F, 6.0F, 4.0F, 8.0F), ModelTransform.of(0.0F, 18.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addChild("abdomen", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -3.0F, 0.0F, 8.0F, 6.0F, 10.0F).uv(22, 16).cuboid(-3.0F, -4.0F, 2.0F, 6.0F, 1.0F, 6.0F), ModelTransform.of(0.0F, 17.0F, 4.0F, 0.25F, 0.0F, 0.0F));
        root.addChild("head", ModelPartBuilder.create().uv(0, 16).cuboid(-3.0F, -3.0F, -5.0F, 6.0F, 5.0F, 5.0F).uv(54, 16).cuboid(-3.0F, 0.0F, -8.0F, 1.0F, 1.0F, 3.0F).uv(0, 26).cuboid(2.0F, 0.0F, -8.0F, 1.0F, 1.0F, 3.0F).uv(8, 26).cuboid(-3.0F, -2.0F, -6.0F, 1.0F, 1.0F, 1.0F).uv(12, 26).cuboid(2.0F, -2.0F, -6.0F, 1.0F, 1.0F, 1.0F).uv(46, 16).cuboid(-2.0F, -7.0F, -5.0F, 1.0F, 4.0F, 1.0F).uv(50, 16).cuboid(1.0F, -7.0F, -5.0F, 1.0F, 4.0F, 1.0F), ModelTransform.of(0.0F, 17.0F, -4.0F, 0.0F, 0.0F, 0.0F));
        root.addChild("leg_l0", ModelPartBuilder.create().uv(16, 26).cuboid(-1.0F, -1.0F, -1.0F, 10.0F, 1.0F, 1.0F), ModelTransform.of(3.0F, 18.0F, -3.0F, 0.0F, 0.0F, 0.6F));
        root.addChild("leg_r0", ModelPartBuilder.create().uv(38, 26).cuboid(-9.0F, -1.0F, -1.0F, 10.0F, 1.0F, 1.0F), ModelTransform.of(-3.0F, 18.0F, -3.0F, 0.0F, 0.0F, -0.6F));
        root.addChild("leg_l1", ModelPartBuilder.create().uv(0, 30).cuboid(-1.0F, -1.0F, -1.0F, 10.0F, 1.0F, 1.0F), ModelTransform.of(3.0F, 18.0F, 0.0F, 0.0F, 0.0F, 0.6F));
        root.addChild("leg_r1", ModelPartBuilder.create().uv(22, 30).cuboid(-9.0F, -1.0F, -1.0F, 10.0F, 1.0F, 1.0F), ModelTransform.of(-3.0F, 18.0F, 0.0F, 0.0F, 0.0F, -0.6F));
        root.addChild("leg_l2", ModelPartBuilder.create().uv(0, 32).cuboid(-1.0F, -1.0F, -1.0F, 10.0F, 1.0F, 1.0F), ModelTransform.of(3.0F, 18.0F, 3.0F, 0.0F, 0.0F, 0.6F));
        root.addChild("leg_r2", ModelPartBuilder.create().uv(22, 32).cuboid(-9.0F, -1.0F, -1.0F, 10.0F, 1.0F, 1.0F), ModelTransform.of(-3.0F, 18.0F, 3.0F, 0.0F, 0.0F, -0.6F));
        return TexturedModelData.of(data, 64, 64);
    }
    public static TexturedModelData ant_king() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("thorax", ModelPartBuilder.create().uv(36, 0).cuboid(-3.0F, -2.0F, -4.0F, 6.0F, 4.0F, 8.0F), ModelTransform.of(0.0F, 18.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addChild("abdomen", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -3.0F, 0.0F, 8.0F, 6.0F, 10.0F).uv(22, 16).cuboid(-3.0F, -4.0F, 2.0F, 6.0F, 1.0F, 6.0F), ModelTransform.of(0.0F, 17.0F, 4.0F, 0.25F, 0.0F, 0.0F));
        root.addChild("head", ModelPartBuilder.create().uv(0, 16).cuboid(-3.0F, -3.0F, -5.0F, 6.0F, 5.0F, 5.0F).uv(44, 33).cuboid(-3.0F, 0.0F, -8.0F, 1.0F, 1.0F, 3.0F).uv(52, 33).cuboid(2.0F, 0.0F, -8.0F, 1.0F, 1.0F, 3.0F).uv(14, 40).cuboid(-3.0F, -2.0F, -6.0F, 1.0F, 1.0F, 1.0F).uv(18, 40).cuboid(2.0F, -2.0F, -6.0F, 1.0F, 1.0F, 1.0F).uv(36, 33).cuboid(-2.0F, -7.0F, -5.0F, 1.0F, 4.0F, 1.0F).uv(40, 33).cuboid(1.0F, -7.0F, -5.0F, 1.0F, 4.0F, 1.0F).uv(6, 40).cuboid(-3.0F, -5.0F, -4.0F, 1.0F, 2.0F, 1.0F).uv(0, 40).cuboid(-1.0F, -6.0F, -4.0F, 2.0F, 3.0F, 1.0F).uv(10, 40).cuboid(2.0F, -5.0F, -4.0F, 1.0F, 2.0F, 1.0F), ModelTransform.of(0.0F, 17.0F, -4.0F, 0.0F, 0.0F, 0.0F));
        root.addChild("leg_l0", ModelPartBuilder.create().uv(22, 40).cuboid(-1.0F, -1.0F, -1.0F, 10.0F, 1.0F, 1.0F), ModelTransform.of(3.0F, 18.0F, -3.0F, 0.0F, 0.0F, 0.6F));
        root.addChild("leg_r0", ModelPartBuilder.create().uv(0, 44).cuboid(-9.0F, -1.0F, -1.0F, 10.0F, 1.0F, 1.0F), ModelTransform.of(-3.0F, 18.0F, -3.0F, 0.0F, 0.0F, -0.6F));
        root.addChild("leg_l1", ModelPartBuilder.create().uv(22, 44).cuboid(-1.0F, -1.0F, -1.0F, 10.0F, 1.0F, 1.0F), ModelTransform.of(3.0F, 18.0F, 0.0F, 0.0F, 0.0F, 0.6F));
        root.addChild("leg_r1", ModelPartBuilder.create().uv(0, 46).cuboid(-9.0F, -1.0F, -1.0F, 10.0F, 1.0F, 1.0F), ModelTransform.of(-3.0F, 18.0F, 0.0F, 0.0F, 0.0F, -0.6F));
        root.addChild("leg_l2", ModelPartBuilder.create().uv(22, 46).cuboid(-1.0F, -1.0F, -1.0F, 10.0F, 1.0F, 1.0F), ModelTransform.of(3.0F, 18.0F, 3.0F, 0.0F, 0.0F, 0.6F));
        root.addChild("leg_r2", ModelPartBuilder.create().uv(0, 48).cuboid(-9.0F, -1.0F, -1.0F, 10.0F, 1.0F, 1.0F), ModelTransform.of(-3.0F, 18.0F, 3.0F, 0.0F, 0.0F, -0.6F));
        root.addChild("wing_l", ModelPartBuilder.create().uv(0, 26).cuboid(0.0F, 0.0F, -1.0F, 12.0F, 1.0F, 6.0F), ModelTransform.of(2.0F, 15.0F, -1.0F, 0.0F, 0.0F, -0.3F));
        root.addChild("wing_r", ModelPartBuilder.create().uv(0, 33).cuboid(-12.0F, 0.0F, -1.0F, 12.0F, 1.0F, 6.0F), ModelTransform.of(-2.0F, 15.0F, -1.0F, 0.0F, 0.0F, 0.3F));
        return TexturedModelData.of(data, 64, 64);
    }
    public static TexturedModelData cerberus() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -4.0F, -9.0F, 10.0F, 9.0F, 18.0F), ModelTransform.of(0.0F, 11.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addChild("mane", ModelPartBuilder.create().uv(56, 0).cuboid(-6.0F, -5.0F, -3.0F, 12.0F, 10.0F, 6.0F), ModelTransform.of(0.0F, 9.0F, -7.0F, 0.0F, 0.0F, 0.0F));
        root.addChild("head", ModelPartBuilder.create().uv(32, 27).cuboid(-3.0F, -3.0F, -6.0F, 6.0F, 6.0F, 6.0F).uv(0, 41).cuboid(-2.0F, 0.0F, -9.0F, 4.0F, 3.0F, 3.0F).uv(42, 41).cuboid(-3.0F, -5.0F, -3.0F, 2.0F, 2.0F, 1.0F).uv(48, 41).cuboid(1.0F, -5.0F, -3.0F, 2.0F, 2.0F, 1.0F).uv(78, 41).cuboid(-2.0F, -2.0F, -7.0F, 1.0F, 1.0F, 1.0F).uv(82, 41).cuboid(1.0F, -2.0F, -7.0F, 1.0F, 1.0F, 1.0F).uv(86, 41).cuboid(-2.0F, 3.0F, -9.0F, 1.0F, 1.0F, 1.0F).uv(90, 41).cuboid(1.0F, 3.0F, -9.0F, 1.0F, 1.0F, 1.0F), ModelTransform.of(0.0F, 8.0F, -10.0F, 0.0F, 0.0F, 0.0F));
        root.addChild("head_l", ModelPartBuilder.create().uv(56, 27).cuboid(-3.0F, -3.0F, -6.0F, 6.0F, 6.0F, 6.0F).uv(14, 41).cuboid(-2.0F, 0.0F, -9.0F, 4.0F, 3.0F, 3.0F).uv(54, 41).cuboid(-3.0F, -5.0F, -3.0F, 2.0F, 2.0F, 1.0F).uv(60, 41).cuboid(1.0F, -5.0F, -3.0F, 2.0F, 2.0F, 1.0F).uv(94, 41).cuboid(-2.0F, -2.0F, -7.0F, 1.0F, 1.0F, 1.0F).uv(98, 41).cuboid(1.0F, -2.0F, -7.0F, 1.0F, 1.0F, 1.0F).uv(102, 41).cuboid(-2.0F, 3.0F, -9.0F, 1.0F, 1.0F, 1.0F).uv(106, 41).cuboid(1.0F, 3.0F, -9.0F, 1.0F, 1.0F, 1.0F), ModelTransform.of(6.0F, 10.0F, -8.0F, 0.0F, -0.45F, 0.0F));
        root.addChild("head_r", ModelPartBuilder.create().uv(80, 27).cuboid(-3.0F, -3.0F, -6.0F, 6.0F, 6.0F, 6.0F).uv(28, 41).cuboid(-2.0F, 0.0F, -9.0F, 4.0F, 3.0F, 3.0F).uv(66, 41).cuboid(-3.0F, -5.0F, -3.0F, 2.0F, 2.0F, 1.0F).uv(72, 41).cuboid(1.0F, -5.0F, -3.0F, 2.0F, 2.0F, 1.0F).uv(110, 41).cuboid(-2.0F, -2.0F, -7.0F, 1.0F, 1.0F, 1.0F).uv(114, 41).cuboid(1.0F, -2.0F, -7.0F, 1.0F, 1.0F, 1.0F).uv(118, 41).cuboid(-2.0F, 3.0F, -9.0F, 1.0F, 1.0F, 1.0F).uv(122, 41).cuboid(1.0F, 3.0F, -9.0F, 1.0F, 1.0F, 1.0F), ModelTransform.of(-6.0F, 10.0F, -8.0F, 0.0F, 0.45F, 0.0F));
        root.addChild("leg_fl", ModelPartBuilder.create().uv(92, 0).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 4.0F), ModelTransform.of(3.5F, 14.0F, -6.0F, 0.0F, 0.0F, 0.0F));
        root.addChild("leg_fr", ModelPartBuilder.create().uv(108, 0).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 4.0F), ModelTransform.of(-3.5F, 14.0F, -6.0F, 0.0F, 0.0F, 0.0F));
        root.addChild("leg_bl", ModelPartBuilder.create().uv(0, 27).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 4.0F), ModelTransform.of(3.5F, 14.0F, 6.0F, 0.0F, 0.0F, 0.0F));
        root.addChild("leg_br", ModelPartBuilder.create().uv(16, 27).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 4.0F), ModelTransform.of(-3.5F, 14.0F, 6.0F, 0.0F, 0.0F, 0.0F));
        root.addChild("tail", ModelPartBuilder.create().uv(104, 27).cuboid(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 10.0F), ModelTransform.of(0.0F, 8.0F, 9.0F, 0.7F, 0.0F, 0.0F));
        return TexturedModelData.of(data, 128, 64);
    }
    public static TexturedModelData stone_golem() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("leg_l", ModelPartBuilder.create().uv(100, 0).cuboid(-3.0F, 0.0F, -3.0F, 6.0F, 12.0F, 6.0F), ModelTransform.of(4.5F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addChild("leg_r", ModelPartBuilder.create().uv(0, 28).cuboid(-3.0F, 0.0F, -3.0F, 6.0F, 12.0F, 6.0F), ModelTransform.of(-4.5F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addChild("torso", ModelPartBuilder.create().uv(48, 0).cuboid(-8.0F, -14.0F, -5.0F, 16.0F, 14.0F, 10.0F).uv(48, 46).cuboid(-2.0F, -11.0F, -6.0F, 4.0F, 4.0F, 1.0F).uv(56, 28).cuboid(-8.0F, -15.0F, -5.0F, 16.0F, 1.0F, 10.0F), ModelTransform.of(0.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addChild("head", ModelPartBuilder.create().uv(24, 28).cuboid(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F).uv(58, 46).cuboid(-3.0F, -5.0F, -6.0F, 2.0F, 1.0F, 1.0F).uv(64, 46).cuboid(1.0F, -5.0F, -6.0F, 2.0F, 1.0F, 1.0F), ModelTransform.of(0.0F, -2.0F, -1.0F, 0.0F, 0.0F, 0.0F));
        root.addChild("arm_r", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -2.0F, -3.0F, 6.0F, 22.0F, 6.0F).uv(0, 46).cuboid(-3.0F, -3.0F, -3.0F, 6.0F, 1.0F, 6.0F), ModelTransform.of(-11.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addChild("arm_l", ModelPartBuilder.create().uv(24, 0).cuboid(-3.0F, -2.0F, -3.0F, 6.0F, 22.0F, 6.0F).uv(24, 46).cuboid(-3.0F, -3.0F, -3.0F, 6.0F, 1.0F, 6.0F), ModelTransform.of(11.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        return TexturedModelData.of(data, 128, 64);
    }
    public static TexturedModelData blood_knight() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("leg_l", ModelPartBuilder.create().uv(18, 0).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), ModelTransform.of(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addChild("leg_r", ModelPartBuilder.create().uv(34, 0).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), ModelTransform.of(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addChild("body", ModelPartBuilder.create().uv(50, 0).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F).uv(106, 19).cuboid(-4.0F, 1.0F, -3.0F, 8.0F, 6.0F, 1.0F).uv(0, 35).cuboid(-4.0F, 10.0F, -3.0F, 8.0F, 1.0F, 1.0F), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addChild("head", ModelPartBuilder.create().uv(74, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F).uv(18, 35).cuboid(-3.0F, -5.0F, -5.0F, 6.0F, 1.0F, 1.0F).uv(44, 19).cuboid(-1.0F, -12.0F, -3.0F, 2.0F, 4.0F, 7.0F), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addChild("cape", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, 0.0F, 0.0F, 8.0F, 18.0F, 1.0F), ModelTransform.of(0.0F, 0.0F, 2.0F, 0.0F, 0.0F, 0.0F));
        root.addChild("arm_r", ModelPartBuilder.create().uv(106, 0).cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F).uv(62, 19).cuboid(-4.0F, -3.0F, -3.0F, 5.0F, 3.0F, 6.0F).uv(32, 35).cuboid(-3.0F, 9.0F, -4.0F, 4.0F, 1.0F, 1.0F).uv(16, 19).cuboid(-2.0F, 9.0F, -16.0F, 2.0F, 1.0F, 12.0F), ModelTransform.of(-5.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addChild("arm_l", ModelPartBuilder.create().uv(0, 19).cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F).uv(84, 19).cuboid(-1.0F, -3.0F, -3.0F, 5.0F, 3.0F, 6.0F), ModelTransform.of(5.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        return TexturedModelData.of(data, 128, 64);
    }

    public static void registerAll() {
        reg("ant", ModEntities.ANT, ModelDefs::ant, new String[]{"thorax", "abdomen", "head", "leg_l0", "leg_r0", "leg_l1", "leg_r1", "leg_l2", "leg_r2"}, new int[]{0, 0, 1, 3, 3, 3, 3, 3, 3}, new float[]{0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 3.1416F, 0.0F, 0.0F, 3.1416F}, 0.7F);
        reg("ant_king", ModEntities.ANT_KING, ModelDefs::ant_king, new String[]{"thorax", "abdomen", "head", "leg_l0", "leg_r0", "leg_l1", "leg_r1", "leg_l2", "leg_r2", "wing_l", "wing_r"}, new int[]{0, 0, 1, 3, 3, 3, 3, 3, 3, 4, 4}, new float[]{0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 3.1416F, 0.0F, 0.0F, 3.1416F, 0.0F, 3.1416F}, 1.2F);
        reg("cerberus", ModEntities.CERBERUS, ModelDefs::cerberus, new String[]{"body", "mane", "head", "head_l", "head_r", "leg_fl", "leg_fr", "leg_bl", "leg_br", "tail"}, new int[]{0, 0, 1, 1, 1, 2, 2, 2, 2, 5}, new float[]{0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 3.1416F, 0.0F, 0.0F}, 0.9F);
        reg("stone_golem", ModEntities.STONE_GOLEM, ModelDefs::stone_golem, new String[]{"leg_l", "leg_r", "torso", "head", "arm_r", "arm_l"}, new int[]{2, 2, 0, 1, 6, 6}, new float[]{0.0F, 3.1416F, 0.0F, 0.0F, 3.1416F, 0.0F}, 1.0F);
        reg("blood_knight", ModEntities.BLOOD_KNIGHT, ModelDefs::blood_knight, new String[]{"leg_l", "leg_r", "body", "head", "cape", "arm_r", "arm_l"}, new int[]{2, 2, 0, 1, 7, 8, 6}, new float[]{0.0F, 3.1416F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F}, 0.5F);
    }

    private static void reg(String id, EntityType<SlMob> type, EntityModelLayerRegistry.TexturedModelDataProvider prov,
                            String[] names, int[] roles, float[] phase, float shadow) {
        EntityModelLayer layer = new EntityModelLayer(Identifier.of(SoloLeveling.MOD, id), "main");
        EntityModelLayerRegistry.registerModelLayer(layer, prov);
        Identifier tex = Identifier.of(SoloLeveling.MOD, "textures/entity/" + id + ".png");
        EntityRendererRegistry.register(type, ctx -> new GenRenderer(ctx, new GenModel(ctx.getPart(layer), names, roles, phase), shadow, tex));
    }

    private ModelDefs() {}
}
