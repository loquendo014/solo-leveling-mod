package com.sololeveling.client;

import com.sololeveling.S;

public final class ClientData {
    public static int[] data = null;

    public static int get(int i) {
        return data == null || i >= data.length ? 0 : data[i];
    }

    public static boolean ready() {
        return data != null && data.length >= S.SIZE;
    }

    private ClientData() {}
}
