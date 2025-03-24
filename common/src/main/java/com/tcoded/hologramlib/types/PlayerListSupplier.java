package com.tcoded.hologramlib.types;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.function.Supplier;

public interface PlayerListSupplier extends Supplier<Collection<? extends Player>> {
}
