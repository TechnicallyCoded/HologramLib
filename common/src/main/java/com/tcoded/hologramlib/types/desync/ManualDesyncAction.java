package com.tcoded.hologramlib.types.desync;

import com.tcoded.hologramlib.hologram.Hologram;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public record ManualDesyncAction(Player player, Location loc) implements DesyncAction {

    public ManualDesyncAction(Player player) {
        this(player, player.getLocation());
    }

    @Override
    public @Nullable Location from() {
        return loc;
    }

    @Override
    public @Nullable Location to() {
        return loc;
    }

    @Override
    public @Nullable Collection<Hologram<?>> manualHolos() {
        return null;
    }

}
