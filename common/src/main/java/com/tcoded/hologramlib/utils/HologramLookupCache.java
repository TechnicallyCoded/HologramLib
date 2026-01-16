package com.tcoded.hologramlib.utils;

import com.tcoded.hologramlib.hologram.Hologram;
import com.tcoded.hologramlib.types.chunk.ChunkKey;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HologramLookupCache {

    private final Map<ChunkKey, Set<Hologram<?>>> chunkCache;

    public HologramLookupCache() {
        chunkCache = new ConcurrentHashMap<>();
    }

    public void onHoloPositionUpdate(Hologram<?> hologram, Location from, Location to) {
        // Remove the hologram from the previous chunk
        if (from != null) {
            ChunkKey fromChunk = new ChunkKey(from);

            Set<Hologram<?>> holosRemove = chunkCache.get(fromChunk);
            if (holosRemove != null) {
                holosRemove.remove(hologram);
            }
        }

        // Add the hologram to the new chunk
        ChunkKey toChunk = new ChunkKey(to);

        Set<Hologram<?>> holosAdd = chunkCache.computeIfAbsent(toChunk, this::createHoloSet);
        holosAdd.add(hologram);
    }

    private Set<Hologram<?>> createHoloSet(Object ignore) {
        return this.createHoloSet();
    }

    private Set<Hologram<?>> createHoloSet() {
        return Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    }

    @Nullable
    public Collection<Hologram<?>> getHolograms(ChunkKey key) {
        return chunkCache.get(key);
    }

}
