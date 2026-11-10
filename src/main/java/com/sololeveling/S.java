package com.sololeveling;

/** Índices del array que se sincroniza con el cliente. */
public final class S {
    public static final int LEVEL = 0, XP = 1, NEED = 2, PTS = 3,
            STR = 4, VIT = 5, AGI = 6, PER = 7, INT = 8,
            MANA = 9, MMAX = 10, RANK = 11,
            QDIST = 12, QKILL = 13, QJUMP = 14, QDONE = 15,
            SHAD = 16, SHADMAX = 17, CD = 18, // CD .. CD+6
            HP = 25, SIZE = 26;

    // Objetivos de la misión diaria
    public static final int Q_DIST = 1000, Q_KILL = 20, Q_JUMP = 100;

    // Habilidades: nombre, nivel, maná, enfriamiento (s), color
    public static final String[] SKILL_NAMES = {"Carrera", "Intención Asesina", "Sigilo", "¡Levántate!",
            "Autoridad del Gobernante", "Intercambio de Sombras", "Dominio del Monarca"};
    public static final int[] SKILL_LEVEL = {3, 8, 12, 20, 30, 45, 65};
    public static final int[] SKILL_MANA = {15, 30, 40, 50, 40, 50, 120};
    public static final int[] SKILL_CD = {3, 10, 25, 2, 8, 5, 45};

    public static final String[] RANKS = {"E", "D", "C", "B", "A", "S", "MONARCA"};
    public static final int[] RANK_COLORS = {0xAAAAAA, 0x55FF55, 0x55FFFF, 0x5555FF, 0xFF55FF, 0xFFAA00, 0xAA00AA};

    public static int rankOf(int level) {
        if (level >= 100) return 6;
        if (level >= 80) return 5;
        if (level >= 60) return 4;
        if (level >= 40) return 3;
        if (level >= 25) return 2;
        if (level >= 10) return 1;
        return 0;
    }

    private S() {}
}
