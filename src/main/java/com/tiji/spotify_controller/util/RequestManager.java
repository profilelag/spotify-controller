package com.tiji.spotify_controller.util;

import net.minecraft.Util;

import java.util.ArrayList;

public class RequestManager {
    private static final ArrayList<Long> requestQueue = new ArrayList<>();

    public static void putRequest() {
        putRequest(300);
    }

    public static void putRequest(int delayMs) {
        requestQueue.add(Util.getMillis() + delayMs);
    }

    public static boolean pollRequest() {
        if (requestQueue.isEmpty()) return false;
        if (requestQueue.getFirst() < Util.getMillis()) {
            requestQueue.removeIf(time -> time < Util.getMillis());
            return true;
        }
        return false;
    }
}
