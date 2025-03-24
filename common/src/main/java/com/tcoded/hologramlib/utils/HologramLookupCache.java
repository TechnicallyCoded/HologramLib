package com.tcoded.hologramlib.utils;

import com.tcoded.hologramlib.hologram.TextHologram;
import com.tcoded.hologramlib.types.chunk.ChunkKey;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HologramLookupCache {

    private final Map<ChunkKey, Set<TextHologram<?>>> chunkCache;

    public HologramLookupCache() {
        chunkCache = new ConcurrentHashMap<>();
    }

    public void onHoloPositionUpdate(TextHologram<?> hologram, Location from, Location to) {
        // Remove the hologram from the previous chunk
        if (from != null) {
            ChunkKey fromChunk = new ChunkKey(from);

            Set<TextHologram<?>> holosRemove = chunkCache.get(fromChunk);
            holosRemove.remove(hologram);
        }

        // Add the hologram to the new chunk
        ChunkKey toChunk = new ChunkKey(to);

        Set<TextHologram<?>> holosAdd = chunkCache.computeIfAbsent(toChunk, this::createHoloSet);
        holosAdd.add(hologram);
    }

    private Set<TextHologram<?>> createHoloSet(Object ignore) {
        return this.createHoloSet();
    }

    private Set<TextHologram<?>> createHoloSet() {
        return Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    }

    @Nullable
    public Collection<TextHologram<?>> getHolograms(ChunkKey key) {
        return chunkCache.get(key);
    }

}
