package com.tcoded.hologramlib.types.chunk;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public record ChunkArea(ChunkKey center, int radius) implements Iterable<ChunkKey> {

    @Override
    public @NotNull Iterator<ChunkKey> iterator() {
        return new ChunkAreaIterator(this);
    }

    public static class ChunkAreaIterator implements Iterator<ChunkKey> {

        private final World world;
        private final int minX;
        private final int minZ;
        private final int len;

        private int x;
        private int z;

        public ChunkAreaIterator(ChunkArea area) {
            this(
                    area.center().world().get(),
                    area.center().chunkX() - area.radius(),
                    area.center().chunkZ() - area.radius(),
                    (area.radius() * 2) + 1
            );
        }

        public ChunkAreaIterator(World world, int minX, int minZ, int len) {
            this.world = world;
            this.minX = minX;
            this.minZ = minZ;
            this.len = len;
        }

        @Override
        public boolean hasNext() {
            return x < len && z < len;
        }

        @Override
        public ChunkKey next() {
            ChunkKey key = new ChunkKey(world, x + minX, z + minZ);

            x++;
            if (x >= len) {
                x = 0;
                z++;
            }

            return key;
        }
    }

}
