package com.tcoded.hologramlib;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public abstract class PlaceholderHandler {

    public static PlaceholderHandler createHandler() {
        PlaceholderHandler fallbackHandler = new FallbackPlaceholderHandler();

        Plugin plugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");

        // PlaceholderAPI not found, return input unchanged
        if (!(plugin instanceof JavaPlugin)) {
            return fallbackHandler;
        }

        return new PlaceholderAPIPlaceholderHandler(plugin, fallbackHandler);
    }

    public abstract String setPlaceholders(OfflinePlayer player, String input);

    @Nullable
    public abstract Pattern getPattern();

    public boolean isEnabled() {
        return getPattern() != null;
    }

}
