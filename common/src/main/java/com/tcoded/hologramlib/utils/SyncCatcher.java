package com.tcoded.hologramlib.utils;

import org.bukkit.Bukkit;

public class SyncCatcher {

    private static boolean shutdown = false;

    public static void shutdownMode() {
        shutdown = true;
    }

    public static void ensureAsync() {
        if (shutdown) return;
        if (Bukkit.isPrimaryThread()) throw new IllegalStateException("[SyncCatcher] Can't perform HologramLib updates on a tick thread. Must run asynchronously.");
    }

}
