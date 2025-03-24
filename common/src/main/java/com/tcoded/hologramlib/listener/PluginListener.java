package com.tcoded.hologramlib.listener;

import com.tcoded.hologramlib.types.HologramLibShutdownExecutor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

public class PluginListener implements Listener {

    private final Plugin target;
    private final HologramLibShutdownExecutor shutdownExecutor;

    public PluginListener(Plugin target, HologramLibShutdownExecutor shutdownExecutor) {
        this.target = target;
        this.shutdownExecutor = shutdownExecutor;
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin() != this.target) return;
        this.shutdownExecutor.shutdown(); // HologramLib.shutdown()
    }

}
