package com.sololeveling.client;

import com.sololeveling.S;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class Hud {

    public static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden || mc.player == null || !ClientData.ready()) return;
        TextRenderer tr = mc.textRenderer;

        int x = 6, y = 6, w = 132;
        int rank = Math.min(6, Math.max(0, ClientData.get(S.RANK)));
        ctx.fill(x - 3, y - 3, x + w + 3, y + 39, 0x90000000);
        ctx.fill(x - 3, y - 3, x + w + 3, y - 2, 0xFF2AA8FF);

        ctx.drawTextWithShadow(tr, "Nv. " + ClientData.get(S.LEVEL), x, y, 0xFFFFFF);
        String rk = "Rango " + S.RANKS[rank];
        ctx.drawTextWithShadow(tr, rk, x + w - tr.getWidth(rk), y, S.RANK_COLORS[rank]);

        // XP
        int xp = ClientData.get(S.XP), need = Math.max(1, ClientData.get(S.NEED));
        bar(ctx, x, y + 12, w, 4, xp / (float) need, 0xFF3CD65A);
        // Maná
        int mana = ClientData.get(S.MANA), mmax = Math.max(1, ClientData.get(S.MMAX));
        bar(ctx, x, y + 20, w, 4, mana / (float) mmax, 0xFF3C8CFF);
        String ms = "✦ " + mana + "/" + mmax;
        ctx.drawTextWithShadow(tr, ms, x, y + 27, 0x7FB8FF);

        int pts = ClientData.get(S.PTS);
        if (pts > 0) {
            String ps = "▲ " + pts + " pts [K]";
            ctx.drawTextWithShadow(tr, ps, x + w - tr.getWidth(ps), y + 27, 0xFFFF55);
        } else if (ClientData.get(S.LEVEL) >= S.SKILL_LEVEL[3]) {
            String ss = "☗ " + ClientData.get(S.SHAD) + "/" + ClientData.get(S.SHADMAX);
            ctx.drawTextWithShadow(tr, ss, x + w - tr.getWidth(ss), y + 27, 0xC77DFF);
        }
    }

    static void bar(DrawContext ctx, int x, int y, int w, int h, float pct, int color) {
        pct = Math.max(0, Math.min(1, pct));
        ctx.fill(x, y, x + w, y + h, 0xFF1A1A1A);
        ctx.fill(x, y, x + (int) (w * pct), y + h, color);
    }

    private Hud() {}
}
