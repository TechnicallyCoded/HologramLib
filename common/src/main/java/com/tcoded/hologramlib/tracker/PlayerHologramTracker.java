package com.tcoded.hologramlib.tracker;

import com.tcoded.hologramlib.hologram.TextHologram;
import com.tcoded.hologramlib.types.chunk.ChunkArea;
import com.tcoded.hologramlib.types.chunk.ChunkKey;
import com.tcoded.hologramlib.utils.HologramLookupCache;
import com.tcoded.hologramlib.utils.SyncCatcher;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;

public class PlayerHologramTracker {

    private final HologramLookupCache hologramCache;
    private final WeakReference<Player> playerRef;

    // Chunks which were desynced due to a player action
    // For example: a player moved, a player teleported
    private final Set<ChunkKey> desyncedChunks;

    // Holograms which were specifically desynced due to an action outside the player's control
    // For example: a hologram was moved
    private final Set<TextHologram<?>> desyncedHolograms;

    private ChunkArea trackedArea;

    public PlayerHologramTracker(HologramLookupCache hologramCache, Player player) {
        this.hologramCache = hologramCache;
        this.playerRef = new WeakReference<>(player);

        updateTrackedArea(new ChunkKey(player.getLocation()), player);

        this.desyncedChunks = createSyncronizedWeakSet(calcInitialChuckCapacity());
        this.desyncedHolograms = createSyncronizedWeakSet();
    }

    public void move(Location from, Location to) {
        SyncCatcher.ensureAsync();

        Player player = this.playerRef.get();
        Objects.requireNonNull(player);

        Set<ChunkKey> toCheck = new HashSet<>(calcInitialChuckCapacity());

        // If the center has changed, consider updates for the previous area
        ChunkKey updatedCenter = new ChunkKey(to);
        if (!this.trackedArea.center().equals(updatedCenter)) {
            ChunkArea previousArea = this.trackedArea;
            updateTrackedArea(updatedCenter, player);

            previousArea.iterator().forEachRemaining(toCheck::add);
        }

        // Add the new area to check
        this.trackedArea.iterator().forEachRemaining(toCheck::add);

        // Schedule the chunks for updates
        this.desyncedChunks.addAll(toCheck);
    }

    public void checkAll() {
        this.checkDesyncedChunks();
        this.checkDesyncedHolograms();
    }

    public void checkDesyncedChunks() {
        this.checkChunkHolograms(this.desyncedChunks);
    }

    // Check the manually specified desynced holograms
    public void checkDesyncedHolograms() {
        if (this.desyncedHolograms.isEmpty()) return;

        Set<TextHologram<?>> toCheck;

        synchronized (this.desyncedHolograms) {
            toCheck = new HashSet<>(this.desyncedHolograms);
            this.desyncedHolograms.clear();
        }

        toCheck.forEach(this::checkHologram);
    }

    private void checkChunkHolograms(Set<ChunkKey> chunks) {
        for (ChunkKey chunk : chunks) {
            Collection<TextHologram<?>> holograms = this.hologramCache.getHolograms(chunk);
            if (holograms == null) continue;
            holograms.forEach(this::checkHologram);
        }
    }

    private void checkHologram(TextHologram<?> hologram) {
        Player player = this.playerRef.get();
        if (player == null) return;

        int viewDistBlocks = player.getViewDistance() * 16;
        int viewDistSquaredBlocks = viewDistBlocks * viewDistBlocks;

        Location pLoc = player.getLocation();
        Location holoLoc = hologram.getLocation();

        HologramPlayerTracker holoTracker = hologram.getTracker();

        boolean shouldTrack = holoLoc.getWorld().equals(pLoc.getWorld()) && holoLoc.distanceSquared(pLoc) <= viewDistSquaredBlocks;
        boolean isTracked = holoTracker.isViewing(this);

        if (shouldTrack && !isTracked) {
            holoTracker.addViewer(this);
        } else if (!shouldTrack && isTracked) {
            holoTracker.removeViewer(this);
        }
    }

    private void updateTrackedArea(ChunkKey updatedCenter, Player player) {
        this.trackedArea = new ChunkArea(updatedCenter, player.getViewDistance());
    }

    public ChunkArea getTrackedArea() {
        return this.trackedArea;
    }

    public void markDesynced(TextHologram<?> hologram) {
        this.desyncedHolograms.add(hologram);
    }

    public void markDesynced(Collection<TextHologram<?>> holograms) {
        this.desyncedHolograms.addAll(holograms);
    }

    @Nullable
    public Player getPlayer() {
        return this.playerRef.get();
    }

    private int calcInitialChuckCapacity() {
        int radius = this.trackedArea.radius();
        int side = radius * 2 + 1;
        // 2x for a load factor of 50% or less
        //   2x for the potential 2 areas to check
        int calc = 2 * 2 * (side * side);

        // Must return a power of 2
        if (calc < 16) return 16; // 2 ^ 4
        else if (calc < 64) return 64; // 2 ^ 6
        else if (calc < 128) return 128; // 2 ^ 7
        else if (calc < 256) return 256; // 2 ^ 8
        else if (calc < 512) return 512; // 2 ^ 9
        else if (calc < 1024) return 1024; // 2 ^ 10
        else if (calc < 2048) return 2048; // 2 ^ 11
        else if (calc < 4096) return 4096; // 2 ^ 12
        else if (calc < 8192) return 8192; // 2 ^ 13
        else if (calc < 16384) return 16384; // 2 ^ 14
        else if (calc < 32768) return 32768; // 2 ^ 15
        else if (calc < 65536) return 65536; // 2 ^ 16
        else throw new IllegalStateException("Too large area to track");
    }

    private static <T> @NotNull Set<T> createSyncronizedWeakSet() {
        return createSyncronizedWeakSet(-1);
    }

    private static <T> @NotNull Set<T> createSyncronizedWeakSet(int initialCapacity) {
        final int MIN_INITIAL_CAPACITY = 16;

        if (initialCapacity < MIN_INITIAL_CAPACITY) {
            initialCapacity = MIN_INITIAL_CAPACITY;
        }

        return Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>(initialCapacity)));
    }

}
