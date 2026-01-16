package com.tcoded.hologramlib.types;

import com.tcoded.hologramlib.hologram.Hologram;
import org.bukkit.Location;

@FunctionalInterface
public interface LocationUpdateHook {

    void handle(Hologram<?> hologram, Location from, Location to);

}
