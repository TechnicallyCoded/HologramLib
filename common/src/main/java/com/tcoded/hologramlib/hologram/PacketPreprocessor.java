package com.tcoded.hologramlib.hologram;

import org.bukkit.entity.Player;

public interface PacketPreprocessor {

    <T> T preprocess(T packet, Player viewer);

}
