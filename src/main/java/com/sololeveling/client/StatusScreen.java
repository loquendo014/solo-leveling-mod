package com.sololeveling.client;

import com.sololeveling.Net;
import com.sololeveling.S;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class StatusScreen extends Screen {

    private static final String[] STAT_NAMES = {"FUERZA", "VITALIDAD", "AGILIDAD", "PERCEPCIÓN", "INTELIGENCIA"};
    private static final String[] STAT_INFO = {"daño", "vida máx.", "velocidad", "armadura/suerte", "maná"};
    private static final int[] STAT_IDX = {S.STR, S.VIT, S.AGI, S.PER, S.INT};

    private static final int PW = 360, PH = 236;
    private int left, top;

    public StatusScreen() {
        super(Text.literal("Ventana de Estado"));
    }

    @Override
    protected void init() {
        left = (width - PW) / 2;
        top = (height - PH) / 2;
        for (int i = 0; i < 5; i++) {
            final int stat = i;
            int y = top + 96 + i * 15;
            addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> send(10 + stat))
                    .dimensions(left + 136, y - 2, 16, 12).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("+5"), b -> send(15 + stat))
                    .dimensions(left + 154, y - 2, 20, 12).build());
        }
    }

    private static void send(int action) {
        if (ClientPlayNetworking.canSend(Net.ActionPayload.ID)) ClientPlayNetworking.send(new Net.ActionPayload(action));
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);
        ctx.fill(left, top, left + PW, top + PH, 0xE00A0F1E);
        int c = 0xFF2AA8FF;
        ctx.fill(left, top, left + PW, top + 1, c);
        ctx.fill(left, top + PH - 1, left + PW, top + PH, c);
        ctx.fill(left, top, left + 1, top + PH, c);
        ctx.fill(left + PW - 1, top, left + PW, top + PH, c);
        ctx.fill(left + 186, top + 30, left + 187, top + PH - 10, 0x552AA8FF);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        if (!ClientData.ready()) {
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("El Sistema no responde..."), width / 2, height / 2, 0xAAAAAA);
            return;
        }
        int lvl = ClientData.get(S.LEVEL);
        int rank = Math.min(6, Math.max(0, ClientData.get(S.RANK)));
        int x = left + 12;

        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("⟦ ESTADO ⟧"), width / 2, top + 9, 0x55D8FF);
        String name = client != null && client.player != null ? client.player.getName().getString() : "Jugador";
        ctx.drawTextWithShadow(textRenderer, "Nombre: " + name, x, top + 30, 0xFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "Nivel: " + lvl, x, top + 42, 0xFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "Rango: ", x + 70, top + 42, 0xAAAAAA);
        ctx.drawTextWithShadow(textRenderer, S.RANKS[rank], x + 70 + textRenderer.getWidth("Rango: "), top + 42, S.RANK_COLORS[rank]);
        String job = lvl >= 100 ? "Monarca de las Sombras" : lvl >= 20 ? "Nigromante" : "Ninguna";
        ctx.drawTextWithShadow(textRenderer, "Clase: " + job, x, top + 54, lvl >= 20 ? 0xC77DFF : 0xAAAAAA);

        ctx.drawTextWithShadow(textRenderer, "XP " + ClientData.get(S.XP) + "/" + ClientData.get(S.NEED), x, top + 66, 0x55FF55);
        Hud.bar(ctx, x, top + 76, 160, 3, ClientData.get(S.XP) / (float) Math.max(1, ClientData.get(S.NEED)), 0xFF3CD65A);
        ctx.drawTextWithShadow(textRenderer, "PV " + ClientData.get(S.HP) + "   ✦ Maná " + ClientData.get(S.MANA) + "/" + ClientData.get(S.MMAX),
                x, top + 82, 0x7FB8FF);

        for (int i = 0; i < 5; i++) {
            int y = top + 96 + i * 15;
            ctx.drawTextWithShadow(textRenderer, STAT_NAMES[i], x, y, 0xDDDDDD);
            ctx.drawTextWithShadow(textRenderer, String.valueOf(ClientData.get(STAT_IDX[i])), x + 82, y, 0xFFFFFF);
            if (mouseX >= x && mouseX <= x + 120 && mouseY >= y - 2 && mouseY <= y + 10) {
                ctx.drawTooltip(textRenderer, Text.literal("Aumenta: " + STAT_INFO[i]), mouseX, mouseY);
            }
        }
        int pts = ClientData.get(S.PTS);
        ctx.drawTextWithShadow(textRenderer, "Puntos disponibles: " + pts, x, top + 175, pts > 0 ? 0xFFFF55 : 0x888888);

        // Misión diaria
        int qy = top + 192;
        boolean done = ClientData.get(S.QDONE) == 1;
        ctx.drawTextWithShadow(textRenderer, "MISIÓN DIARIA" + (done ? "  ✔" : ""), x, qy, done ? 0x55FF55 : 0xFFAA00);
        ctx.drawTextWithShadow(textRenderer, "Recorrer " + Math.min(ClientData.get(S.QDIST), S.Q_DIST) + "/" + S.Q_DIST
                + "  Monstruos " + Math.min(ClientData.get(S.QKILL), S.Q_KILL) + "/" + S.Q_KILL, x, qy + 11, 0xBBBBBB);
        ctx.drawTextWithShadow(textRenderer, "Saltos " + Math.min(ClientData.get(S.QJUMP), S.Q_JUMP) + "/" + S.Q_JUMP
                + (done ? "" : "  · ¡antes del amanecer!"), x, qy + 22, 0xBBBBBB);

        // Habilidades
        int sx = left + 196;
        ctx.drawTextWithShadow(textRenderer, "HABILIDADES", sx, top + 30, 0xC77DFF);
        for (int i = 0; i < 7; i++) {
            int y = top + 44 + i * 22;
            boolean unlocked = lvl >= S.SKILL_LEVEL[i];
            String key = SoloLevelingClient.SKILLS[i].getBoundKeyLocalizedText().getString();
            ctx.drawTextWithShadow(textRenderer, "[" + key + "] " + S.SKILL_NAMES[i], sx, y, unlocked ? 0xFFFFFF : 0x666666);
            String sub;
            int cd = ClientData.get(S.CD + i);
            if (!unlocked) sub = "Se desbloquea en el nivel " + S.SKILL_LEVEL[i];
            else if (cd > 0) sub = "Enfriamiento: " + cd + " s";
            else sub = S.SKILL_MANA[i] + " maná · " + S.SKILL_CD[i] + " s";
            ctx.drawTextWithShadow(textRenderer, sub, sx + 8, y + 10, unlocked ? (cd > 0 ? 0xFF7777 : 0x7FB8FF) : 0x555555);
        }
        if (lvl >= S.SKILL_LEVEL[3]) {
            ctx.drawTextWithShadow(textRenderer, "Ejército de sombras: " + ClientData.get(S.SHAD) + "/" + ClientData.get(S.SHADMAX),
                    sx, top + 204, 0xC77DFF);
        }
        ctx.drawTextWithShadow(textRenderer, "Agáchate + [R] para reunir a tu ejército", sx, top + 216, 0x777777);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (SoloLevelingClient.STATUS.matchesKey(keyCode, scanCode)) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
