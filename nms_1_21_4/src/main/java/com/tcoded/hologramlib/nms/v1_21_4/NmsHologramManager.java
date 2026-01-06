package com.tcoded.hologramlib.nms.v1_21_4;

import com.tcoded.hologramlib.HologramLib;
import com.tcoded.hologramlib.PlaceholderHandler;
import com.tcoded.hologramlib.hologram.PacketPreprocessor;
import com.tcoded.hologramlib.hologram.TextHologramLine;
import com.tcoded.hologramlib.manager.HologramManager;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

// This class is instantiated using reflection
@SuppressWarnings("unused")
public class NmsHologramManager <T> extends HologramManager<T> {

    public NmsHologramManager(HologramLib<T> lib) {
        super(lib);
    }

    @Override
    protected int nextEntityId(World world) {
        return Entity.nextEntityId();
    }

    @Override
    protected TextHologramLine createNmsLine(PlaceholderHandler placeholderHandler, PacketPreprocessor packetPreprocessor) {
        return NmsTextHologramLine.create(placeholderHandler, packetPreprocessor);
    }

    // Might become Folia incompatible at some point, so we build
    // a structure which allows for modification later
    @Override
    protected Location getPosUnsafe(Player player) {
        return player.getLocation().clone();
    }

}