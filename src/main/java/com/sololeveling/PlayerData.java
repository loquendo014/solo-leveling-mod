package com.sololeveling;

public class PlayerData {
    public int level = 1, xp = 0, points = 0;
    public int str = 10, vit = 10, agi = 10, per = 10, intel = 10;
    public int mana = 100;
    public int questDay = -1, questDone = 0, qMoveBase = 0, qJumpBase = 0, qKills = 0, qPlay = 0;
    public int awakened = 0;
    public int gold = 0, stage = 0, stageProg = 0;

    public int need() {
        return 30 + level * 20 + level * level / 2;
    }

    public int rank() {
        return S.rankOf(level);
    }

    public int manaMax() {
        return 100 + (intel - 10) * 10;
    }

    public int shadowMax() {
        return 1 + level / 10 + Math.max(0, (intel - 10) / 25);
    }

    public int[] toArray() {
        return new int[]{level, xp, points, str, vit, agi, per, intel, mana,
                questDay, questDone, qMoveBase, qJumpBase, qKills, qPlay, awakened, gold, stage, stageProg};
    }

    public static PlayerData fromArray(int[] a) {
        PlayerData d = new PlayerData();
        if (a.length < 16) return d;
        d.level = a[0]; d.xp = a[1]; d.points = a[2];
        d.str = a[3]; d.vit = a[4]; d.agi = a[5]; d.per = a[6]; d.intel = a[7];
        d.mana = a[8]; d.questDay = a[9]; d.questDone = a[10]; d.qMoveBase = a[11];
        d.qJumpBase = a[12]; d.qKills = a[13]; d.qPlay = a[14]; d.awakened = a[15];
        if (a.length >= 19) { d.gold = a[16]; d.stage = a[17]; d.stageProg = a[18]; }
        return d;
    }
}
