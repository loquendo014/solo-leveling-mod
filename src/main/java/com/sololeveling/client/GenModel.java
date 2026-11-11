package com.sololeveling.client;

import com.sololeveling.SlMob;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;

/** Modelo genérico animado por roles (cabeza, patas, alas, cola, brazos, capa). */
public class GenModel extends SinglePartEntityModel<SlMob> {
    private final ModelPart root;
    private final ModelPart[] parts;
    private final int[] roles;
    private final float[] phase, bp, by, br;

    public GenModel(ModelPart root, String[] names, int[] roles, float[] phase) {
        this.root = root;
        this.roles = roles;
        this.phase = phase;
        int n = names.length;
        parts = new ModelPart[n];
        bp = new float[n];
        by = new float[n];
        br = new float[n];
        for (int i = 0; i < n; i++) {
            parts[i] = root.getChild(names[i]);
            bp[i] = parts[i].pitch;
            by[i] = parts[i].yaw;
            br[i] = parts[i].roll;
        }
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(SlMob e, float limb, float dist, float age, float headYaw, float headPitch) {
        float rad = (float) (Math.PI / 180);
        for (int i = 0; i < parts.length; i++) {
            ModelPart p = parts[i];
            p.pitch = bp[i];
            p.yaw = by[i];
            p.roll = br[i];
            float ph = phase[i];
            switch (roles[i]) {
                case 1 -> {
                    p.yaw += headYaw * rad;
                    p.pitch += headPitch * rad;
                }
                case 2 -> p.pitch += MathHelper.cos(limb * 0.6662f + ph) * 1.2f * dist;
                case 3 -> p.yaw += MathHelper.cos(limb * 1.3324f + ph) * 0.45f * dist;
                case 4 -> p.roll += MathHelper.cos(age * 1.8f + ph) * 0.45f;
                case 5 -> p.yaw += MathHelper.cos(age * 0.12f + ph) * 0.35f;
                case 6 -> p.pitch += MathHelper.cos(limb * 0.6662f + ph) * 0.9f * dist;
                case 7 -> p.pitch += 0.08f + dist * 0.7f;
                case 8 -> p.pitch += MathHelper.cos(limb * 0.6662f + ph) * 0.9f * dist
                        - MathHelper.sin(handSwingProgress * (float) Math.PI) * 1.8f;
                default -> {
                }
            }
        }
    }
}
