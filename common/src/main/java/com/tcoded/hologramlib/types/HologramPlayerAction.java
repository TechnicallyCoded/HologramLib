package com.tcoded.hologramlib.types;

import com.tcoded.hologramlib.hologram.TextHologram;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.BiConsumer;

public interface HologramPlayerAction extends BiConsumer<TextHologram<?>, List<Player>> {
}
