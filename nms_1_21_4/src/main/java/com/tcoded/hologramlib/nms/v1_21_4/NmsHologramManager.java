package com.tcoded.hologramlib.nms.v1_21_4;

import com.tcoded.hologramlib.HologramLib;
import com.tcoded.hologramlib.manager.HologramManager;
import com.tcoded.hologramlib.hologram.TextHologram;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

// This class is instantiated using reflection
@SuppressWarnings("unused")
public class NmsHologramManager <T> extends HologramManager<T> {

    public NmsHologramManager(HologramLib<T> lib) {
        super();
    }

    @Override
    protected int nextEntityId(World world) {
        return Entity.nextEntityId();
    }

    @Override
    protected TextHologram<T> createNms(T id) {
        return NmsTextHologram.create(id);
    }

    // Might become Folia incompatible at some point, so we build
    // a structure which allows for modification later
    @Override
    protected Location getPosUnsafe(Player player) {
        return player.getLocation().clone();
    }

}