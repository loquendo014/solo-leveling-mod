package com.sololeveling;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Misiones principales (cadena) y misiones de emergencia. Tipos: 0 bajas, 1 nivel, 2 puertas, 3 sombras, 4 jefes. */
public final class Story {
    private static final String[] REWARD = {null, "health_potion", "kasaka_fang", "mana_potion", "knight_killer", "gate_key",
            "elixir", "baruka_dagger", "power_fragment", "demon_king_dagger", "power_fragment", "kamish_wrath"};
    /** jugador -> {segundos restantes, progreso, objetivo} */
    private static final Map<UUID, int[]> EM = new HashMap<>();

    public static int[] emergency(ServerPlayerEntity p) {
        return EM.get(p.getUuid());
    }

    public static void progress(ServerPlayerEntity p, int type, int n) {
        PlayerData d = DataState.of(p);
        if (type == 0) {
            int[] e = EM.get(p.getUuid());
            if (e != null && (e[1] += n) >= e[2]) {
                EM.remove(p.getUuid());
                int gold = 100 + d.rank() * 100;
                d.gold += gold;
                Market.give(p, "mana_stone", 2);
                Fx.title(p, Text.literal("EMERGENCIA SUPERADA").formatted(Formatting.GOLD, Formatting.BOLD),
                        Text.literal("+" + gold + " de oro").formatted(Formatting.YELLOW), 5, 40, 10);
                Fx.sound(p, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.3f);
                Leveling.addXp(p, 100 + d.level * 8);
            }
        }
        if (d.stage >= S.STORY_TEXT.length || S.STORY_TYPE[d.stage] != type) return;
        d.stageProg += n;
        check(p, d);
    }

    private static void check(ServerPlayerEntity p, PlayerData d) {
        while (d.stage < S.STORY_TEXT.length) {
            int cur = S.STORY_TYPE[d.stage] == 1 ? d.level : d.stageProg;
            if (cur < S.STORY_TARGET[d.stage]) return;
            int i = d.stage++;
            d.stageProg = 0;
            int gold = 150 * (i + 1);
            d.gold += gold;
            if (REWARD[i] != null) Market.give(p, REWARD[i], 1);
            Fx.title(p, Text.literal("MISIÓN PRINCIPAL COMPLETADA").formatted(Formatting.GOLD, Formatting.BOLD),
                    Text.literal(S.STORY_TEXT[i]).formatted(Formatting.GRAY), 10, 50, 15);
            Fx.sound(p, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.1f);
            Fx.part(p, ParticleTypes.END_ROD, 1, 40, 0.5, 0.1);
            Fx.msg(p, Text.literal("Misión principal completada: +" + gold + " de oro"
                    + (REWARD[i] != null ? " y un objeto de recompensa." : ".")).formatted(Formatting.YELLOW));
            if (d.stage < S.STORY_TEXT.length)
                Fx.msg(p, Text.literal("Nueva misión: ").formatted(Formatting.WHITE)
                        .append(Text.literal(S.STORY_TEXT[d.stage]).formatted(Formatting.GOLD)));
            Leveling.addXp(p, 80 * (i + 1));
        }
    }

    public static void second(ServerPlayerEntity p) {
        PlayerData d = DataState.of(p);
        if (d.awakened == 0) return;
        check(p, d);
        int[] e = EM.get(p.getUuid());
        if (e != null) {
            if (--e[0] <= 0) {
                EM.remove(p.getUuid());
                int loss = Math.min(d.gold, 50);
                d.gold -= loss;
                Fx.msg(p, Text.literal("Has fallado la misión de emergencia. Penalización: -" + loss + " de oro.").formatted(Formatting.RED));
                Fx.sound(p, SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, 0.5f, 1f);
            }
        } else if (d.level >= 5 && !p.isSpectator() && p.getRandom().nextInt(1500) == 0) {
            int target = 8 + d.rank() * 3;
            EM.put(p.getUuid(), new int[]{300, 0, target});
            Fx.title(p, Text.literal("⚠ MISIÓN DE EMERGENCIA ⚠").formatted(Formatting.RED, Formatting.BOLD),
                    Text.literal("Derrota a " + target + " monstruos en 5 minutos").formatted(Formatting.GRAY), 5, 60, 15);
            Fx.msg(p, Text.literal("Misión de emergencia: derrota a " + target + " monstruos en 5 minutos.").formatted(Formatting.RED));
            Fx.sound(p, SoundEvents.BLOCK_BELL_USE, 1f, 0.6f);
        }
    }

    private Story() {}
}
