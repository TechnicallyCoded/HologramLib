package com.tcoded.hologramlib.types.desync;

import com.tcoded.hologramlib.hologram.TextHologram;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface DesyncAction {

    @NotNull
    Player player();

    @Nullable
    Location from();

    @Nullable
    Location to();

    @Nullable
    Collection<TextHologram<?>> manualHolos();

}
