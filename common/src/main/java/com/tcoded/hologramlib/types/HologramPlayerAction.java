package com.tcoded.hologramlib.types;

import com.tcoded.hologramlib.hologram.Hologram;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.BiConsumer;

public interface HologramPlayerAction extends BiConsumer<Hologram<?>, List<Player>> {
}
