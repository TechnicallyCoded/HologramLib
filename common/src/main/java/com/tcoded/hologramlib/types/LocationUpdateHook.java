package com.tcoded.hologramlib.types;

import com.tcoded.hologramlib.hologram.TextHologram;
import org.bukkit.Location;

@FunctionalInterface
public interface LocationUpdateHook {

    void handle(TextHologram<?> hologram, Location from, Location to);

}
