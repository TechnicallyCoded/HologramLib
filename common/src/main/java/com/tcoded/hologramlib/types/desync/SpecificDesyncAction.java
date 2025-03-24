package com.tcoded.hologramlib.types.desync;

import com.tcoded.hologramlib.hologram.TextHologram;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public record SpecificDesyncAction(Player player, Collection<TextHologram<?>> manualHolos) implements DesyncAction {

    @Override
    public @Nullable Location from() {
        return null;
    }

    @Override
    public @Nullable Location to() {
        return null;
    }

}
