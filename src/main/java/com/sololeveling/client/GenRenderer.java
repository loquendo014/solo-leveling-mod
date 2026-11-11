package com.sololeveling.client;

import com.sololeveling.SlMob;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class GenRenderer extends MobEntityRenderer<SlMob, GenModel> {
    private final Identifier tex;

    public GenRenderer(EntityRendererFactory.Context ctx, GenModel model, float shadow, Identifier tex) {
        super(ctx, model, shadow);
        this.tex = tex;
    }

    @Override
    public Identifier getTexture(SlMob e) {
        return tex;
    }
}
