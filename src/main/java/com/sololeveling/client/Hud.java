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

        int x = 6, y = 6, w = 176;
        int rank = Math.min(6, Math.max(0, ClientData.get(S.RANK)));
        int stage = ClientData.get(S.STAGE);
        boolean story = stage >= 0 && stage < S.STORY_TEXT.length;
        boolean em = ClientData.get(S.EM_TARGET) > 0;
        int h = 39 + (story ? 11 : 0) + (em ? 11 : 0);
        ctx.fill(x - 3, y - 3, x + w + 3, y + h, 0x90000000);
        ctx.fill(x - 3, y - 3, x + w + 3, y - 2, 0xFF2AA8FF);

        ctx.drawTextWithShadow(tr, "Nv. " + ClientData.get(S.LEVEL) + "  ⛃ " + ClientData.get(S.GOLD), x, y, 0xFFFFFF);
        String rk = "Rango " + S.RANKS[rank];
        ctx.drawTextWithShadow(tr, rk, x + w - tr.getWidth(rk), y, S.RANK_COLORS[rank]);

        int xp = ClientData.get(S.XP), need = Math.max(1, ClientData.get(S.NEED));
        bar(ctx, x, y + 12, w, 4, xp / (float) need, 0xFF3CD65A);
        int mana = ClientData.get(S.MANA), mmax = Math.max(1, ClientData.get(S.MMAX));
        bar(ctx, x, y + 20, w, 4, mana / (float) mmax, 0xFF3C8CFF);
        ctx.drawTextWithShadow(tr, "✦ " + mana + "/" + mmax, x, y + 27, 0x7FB8FF);

        int pts = ClientData.get(S.PTS);
        if (pts > 0) {
            String ps = "▲ " + pts + " pts [K]";
            ctx.drawTextWithShadow(tr, ps, x + w - tr.getWidth(ps), y + 27, 0xFFFF55);
        } else if (ClientData.get(S.LEVEL) >= S.SKILL_LEVEL[3]) {
            String ss = "☗ " + ClientData.get(S.SHAD) + "/" + ClientData.get(S.SHADMAX);
            ctx.drawTextWithShadow(tr, ss, x + w - tr.getWidth(ss), y + 27, 0xC77DFF);
        }
        int ly = y + 38;
        if (story) {
            int target = S.STORY_TARGET[stage];
            String s = "◆ " + S.STORY_TEXT[stage] + " (" + Math.min(ClientData.get(S.SPROG), target) + "/" + target + ")";
            ctx.drawTextWithShadow(tr, tr.trimToWidth(s, w), x, ly, 0xFFD27F);
            ly += 11;
        }
        if (em) {
            String s = "⚠ Emergencia " + ClientData.get(S.EM_PROG) + "/" + ClientData.get(S.EM_TARGET) + " · " + ClientData.get(S.EM_LEFT) + " s";
            ctx.drawTextWithShadow(tr, s, x, ly, 0xFF5555);
        }
    }

    static void bar(DrawContext ctx, int x, int y, int w, int h, float pct, int color) {
        pct = Math.max(0, Math.min(1, pct));
        ctx.fill(x, y, x + w, y + h, 0xFF1A1A1A);
        ctx.fill(x, y, x + (int) (w * pct), y + h, color);
    }

    private Hud() {}
}
