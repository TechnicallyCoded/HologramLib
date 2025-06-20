package com.tcoded.hologramlib;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class FallbackPlaceholderHandler extends PlaceholderHandler {

    @Override
    public String setPlaceholders(OfflinePlayer player, String input) {
        return input;
    }

    @Override
    @Nullable
    public Pattern getPattern() {
        return null;
    }

}
