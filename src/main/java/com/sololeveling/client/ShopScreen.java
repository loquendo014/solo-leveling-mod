package com.sololeveling.client;

import com.sololeveling.Net;
import com.sololeveling.S;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ShopScreen extends Screen {
    private static final int PW = 330, ROW = 15;
    private int left, top, ph;

    public ShopScreen() {
        super(Text.literal("Tienda del Sistema"));
    }

    private static void send(int action) {
        if (ClientPlayNetworking.canSend(Net.ActionPayload.ID)) ClientPlayNetworking.send(new Net.ActionPayload(action));
    }

    @Override
    protected void init() {
        ph = 28 + S.SHOP_ID.length * ROW + 28;
        left = (width - PW) / 2;
        top = (height - ph) / 2;
        for (int i = 0; i < S.SHOP_ID.length; i++) {
            final int idx = i;
            addDrawableChild(ButtonWidget.builder(Text.literal("Comprar"), b -> send(30 + idx))
                    .dimensions(left + PW - 62, top + 26 + i * ROW, 52, 13).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Vender piedras de maná y esencia"), b -> send(60))
                .dimensions(left + 10, top + ph - 22, 190, 16).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Estado"), b -> {
            if (client != null) client.setScreen(new StatusScreen());
        }).dimensions(left + PW - 70, top + ph - 22, 60, 16).build());
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);
        ctx.fill(left, top, left + PW, top + ph, 0xE00A0F1E);
        int c = 0xFFFFC83C;
        ctx.fill(left, top, left + PW, top + 1, c);
        ctx.fill(left, top + ph - 1, left + PW, top + ph, c);
        ctx.fill(left, top, left + 1, top + ph, c);
        ctx.fill(left + PW - 1, top, left + PW, top + ph, c);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        int gold = ClientData.get(S.GOLD), lvl = ClientData.get(S.LEVEL);
        ctx.drawTextWithShadow(textRenderer, "⟦ TIENDA DEL SISTEMA ⟧", left + 10, top + 9, 0xFFC83C);
        String g = "Oro: " + gold;
        ctx.drawTextWithShadow(textRenderer, g, left + PW - 10 - textRenderer.getWidth(g), top + 9, 0xFFD700);
        for (int i = 0; i < S.SHOP_ID.length; i++) {
            int y = top + 29 + i * ROW;
            boolean ok = lvl >= S.SHOP_LEVEL[i];
            ctx.drawTextWithShadow(textRenderer, S.SHOP_NAME[i], left + 10, y, ok ? 0xFFFFFF : 0x666666);
            ctx.drawTextWithShadow(textRenderer, "Nv." + S.SHOP_LEVEL[i], left + 178, y, ok ? 0x888888 : 0xFF5555);
            ctx.drawTextWithShadow(textRenderer, S.SHOP_PRICE[i] + " oro", left + 210, y, gold >= S.SHOP_PRICE[i] ? 0xFFD700 : 0xAA5555);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
