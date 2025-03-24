package com.tcoded.hologramlib.types.chunk;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.UUID;

public record ChunkKey(WeakReference<World> world, int chunkX, int chunkZ) {

    public ChunkKey(World world, int chunkX, int chunkZ) {
        this(new WeakReference<>(world), chunkX, chunkZ);
    }

    public ChunkKey(Chunk chunk) {
        this(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    public ChunkKey(Location location) {
        this(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    @Override
    public int hashCode() {
        World w = world.get();
        int wHash = w == null ? 0 : w.getUID().hashCode();
        return Objects.hash(wHash, chunkX, chunkZ);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChunkKey(WeakReference<World> otherWorldRef, int otherX, int otherZ)) {
            World selfWorld = this.world.get();
            UUID selfWorldUID = selfWorld == null ? null : selfWorld.getUID();

            World otherWorld = otherWorldRef.get();
            UUID otherWorldUID = otherWorld == null ? null : otherWorld.getUID();

            return Objects.equals(selfWorldUID, otherWorldUID) &&
                    this.chunkX == otherX &&
                    this.chunkZ == otherZ;
        }

        return false;
    }
}
