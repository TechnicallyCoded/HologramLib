package com.tcoded.hologramlib.types.desync;

import com.tcoded.hologramlib.hologram.Hologram;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public record MoveDesyncAction(Player player, Location from, Location to) implements DesyncAction {

    @Override
    public @Nullable Collection<Hologram<?>> manualHolos() {
        return null;
    }

}
