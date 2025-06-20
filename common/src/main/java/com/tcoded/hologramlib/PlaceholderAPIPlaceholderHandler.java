package com.tcoded.hologramlib;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class PlaceholderAPIPlaceholderHandler extends PlaceholderHandler {

    private final Plugin papiPlugin;
    private final PlaceholderHandler fallbackHandler;

    public PlaceholderAPIPlaceholderHandler(Plugin papiPlugin, PlaceholderHandler fallbackHandler) {
        this.papiPlugin = papiPlugin;
        this.fallbackHandler = fallbackHandler;
    }

    @Override
    public String setPlaceholders(OfflinePlayer player, String input) {
        if (!papiPlugin.isEnabled()) {
            // Use fallback if PAPI is not enabled
            return fallbackHandler.setPlaceholders(player, input);
        }

        // Directly call PlaceholderAPI if available
        return PlaceholderAPI.setPlaceholders(player, input);
    }

    @Override
    @Nullable
    public Pattern getPattern() {
        return PlaceholderAPI.getPlaceholderPattern();
    }

}
