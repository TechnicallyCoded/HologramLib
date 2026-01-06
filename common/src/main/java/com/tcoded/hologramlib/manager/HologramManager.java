package com.tcoded.hologramlib.manager;

import com.tcoded.hologramlib.HologramLib;
import com.tcoded.hologramlib.PlaceholderHandler;
import com.tcoded.hologramlib.hologram.PacketPreprocessor;
import com.tcoded.hologramlib.hologram.TextHologram;
import com.tcoded.hologramlib.hologram.TextHologramLine;
import com.tcoded.hologramlib.utils.HologramLookupCache;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Plugins use these methods, ignore unused warnings
@SuppressWarnings("unused")
public abstract class HologramManager <InternalIdType> {

    private final HologramLib<?> lib;
    private final Map<InternalIdType, TextHologram<InternalIdType>> hologramsMap;

    private HologramLookupCache cache;
    private PlayerManager playerManager;

    protected HologramManager(HologramLib<?> lib) {
        this.lib = lib;
        this.hologramsMap = new ConcurrentHashMap<>();
    }

    public void withCache(HologramLookupCache cache) {
        this.cache = cache;
    }

    protected abstract int nextEntityId(World world);

    protected abstract TextHologramLine createNmsLine(PlaceholderHandler placeholderHandler, PacketPreprocessor packetPreprocessor);

    protected abstract Location getPosUnsafe(Player player);

    public List<TextHologram<InternalIdType>> getHolograms() {
        return new ArrayList<>(this.hologramsMap.values());
    }


    /**
     * @param id Hologram ID
     * @throws IllegalStateException If a hologram with this ID already exits
     * @return TextHologram
     */
    public TextHologram<InternalIdType> create(InternalIdType id) {
        return this.create(id, false);
    }

    /**
     * @param id Hologram ID
     * @return TextHologram or null
     */
    public TextHologram<InternalIdType> createIfNotExists(InternalIdType id) {
        return this.create(id, true);
    }

    /**
     * Creates a new line
     * @return TextHologramLine
     */
    public TextHologramLine createLine() {
        return createNmsLine(this.lib.getPlaceholderHandler(), this.lib.getPacketPreprocessor());
    }

    /**
     * @param id Hologram ID
     * @param silentFail Handle duplicate IDs gracefully
     * @throws IllegalStateException If a hologram with this ID already exits && silentFail is false
     * @return TextHologram or null
     */
    private TextHologram<InternalIdType> create(InternalIdType id, boolean silentFail) {
        TextHologram<InternalIdType> hologram = new TextHologram<>(id);
        TextHologramLine defaultLine = createLine();
        hologram.addLine(defaultLine);

        TextHologram<InternalIdType> result = this.hologramsMap.compute(id, (id2, prevValue) -> prevValue == null ? hologram : prevValue);
        if (result != hologram) {
            if (silentFail) return null;
            else throw new IllegalStateException("Hologram with that ID already exists");
        }

        hologram.hookLocationUpdate(this::onHoloPositionUpdate);
        return hologram;
    }

    private void onHoloPositionUpdate(TextHologram<?> hologram, Location from, Location to) {
        this.cache.onHoloPositionUpdate(hologram, from, to);
        this.playerManager.updateTrackersNear(hologram, to);
    }

    public TextHologram<InternalIdType> getHologram(InternalIdType id) {
        return this.hologramsMap.get(id);
    }

    public void killAndRemove(InternalIdType id) {
        Optional.ofNullable(this.hologramsMap.remove(id))
                .ifPresent(this::tryHide); // despawn for all viewers
    }

    public void killAndRemoveAll() {
        this.hologramsMap.values().forEach(this::tryHide); // despawn for all viewers
        this.hologramsMap.clear();
    }

    public HologramLookupCache getLookupCache() {
        return this.cache;
    }

    public void withPlayerManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    private void tryHide(@NotNull TextHologram<InternalIdType> hologram) {
        if (hologram.isVisible()) hologram.hide();
    }

}
