package com.tcoded.hologramlib.utils;

import org.bukkit.Location;

public class BoundingBoxUtil {

    // Easier to read if not 'simplified'
    @SuppressWarnings("RedundantIfStatement")
    public static boolean withinBounds(Location loc, Location min, Location max) {
        if (min.getWorld() != max.getWorld()) throw new IllegalArgumentException("min and max must be within the same world");

        if (loc.getWorld() != min.getWorld()) return false;
        if (loc.getX() < min.getX() || loc.getY() < min.getY() || loc.getZ() < min.getZ()) return false;
        if (loc.getX() > max.getX() || loc.getY() > max.getY() || loc.getZ() > max.getZ()) return false;

        return true;
    }

}
