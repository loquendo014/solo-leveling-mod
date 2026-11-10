package com.sololeveling;

/** Índices del array que se sincroniza con el cliente. */
public final class S {
    public static final int LEVEL = 0, XP = 1, NEED = 2, PTS = 3,
            STR = 4, VIT = 5, AGI = 6, PER = 7, INT = 8,
            MANA = 9, MMAX = 10, RANK = 11,
            QDIST = 12, QKILL = 13, QJUMP = 14, QDONE = 15,
            SHAD = 16, SHADMAX = 17, CD = 18, // CD .. CD+6
            HP = 25, GOLD = 26, STAGE = 27, SPROG = 28, EM_LEFT = 29, EM_PROG = 30, EM_TARGET = 31, SIZE = 32;

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

    // Misiones principales. Tipos: 0 bajas, 1 nivel, 2 puertas, 3 sombras, 4 jefes
    public static final String[] STORY_TEXT = {"Derrota a 10 monstruos", "Alcanza el nivel 5", "Conquista una Puerta",
            "Alcanza el nivel 20", "Extrae tu primera sombra", "Derrota a 100 monstruos", "Conquista 3 Puertas",
            "Extrae 5 sombras", "Derrota a 5 jefes", "Alcanza el nivel 60", "Conquista 10 Puertas", "Alcanza el nivel 100"};
    public static final int[] STORY_TYPE = {0, 1, 2, 1, 3, 0, 2, 3, 4, 1, 2, 1};
    public static final int[] STORY_TARGET = {10, 5, 1, 20, 1, 100, 3, 5, 5, 60, 10, 100};

    // Tienda del Sistema
    public static final String[] SHOP_ID = {"health_potion", "mana_potion", "elixir", "return_stone", "gate_key", "random_box",
            "power_fragment", "kasaka_fang", "knight_killer", "baruka_dagger", "demon_king_dagger", "kamish_wrath"};
    public static final String[] SHOP_NAME = {"Poción de Curación", "Poción de Maná", "Elixir de la Vida", "Piedra de Retorno",
            "Llave de Mazmorra", "Caja Aleatoria", "Fragmento de Poder", "Colmillo Venenoso de Kasaka", "Asesina de Caballeros",
            "Daga de Baruka", "Daga del Rey Demonio", "Ira de Kamish"};
    public static final int[] SHOP_PRICE = {40, 40, 400, 120, 250, 150, 1500, 600, 1800, 4500, 12000, 40000};
    public static final int[] SHOP_LEVEL = {1, 1, 10, 1, 5, 1, 10, 5, 20, 40, 60, 90};

    private S() {}
}
