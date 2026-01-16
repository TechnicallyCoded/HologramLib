package com.tcoded.hologramlib.manager;

import com.tcoded.hologramlib.HologramLib;
import com.tcoded.hologramlib.PlaceholderHandler;
import com.tcoded.hologramlib.hologram.*;
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
    private final Map<InternalIdType, ItemHologram<InternalIdType>> itemHologramsMap;

    private HologramLookupCache cache;
    private PlayerManager playerManager;

    protected HologramManager(HologramLib<?> lib) {
        this.lib = lib;
        this.hologramsMap = new ConcurrentHashMap<>();
        this.itemHologramsMap = new ConcurrentHashMap<>();
    }

    public void withCache(HologramLookupCache cache) {
        this.cache = cache;
    }

    protected abstract int nextEntityId(World world);

    protected abstract TextHologramLine createNmsLine(PlaceholderHandler placeholderHandler, PacketPreprocessor packetPreprocessor);

    protected abstract ItemHologramLine createNmsItemLine(PlaceholderHandler placeholderHandler);

    protected abstract Location getPosUnsafe(Player player);

    public List<TextHologram<InternalIdType>> getHolograms() {
        return new ArrayList<>(this.hologramsMap.values());
    }

    public List<ItemHologram<InternalIdType>> getItemHolograms() {
        return new ArrayList<>(this.itemHologramsMap.values());
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
     * Creates a new text line
     * @return TextHologramLine
     */
    public TextHologramLine createLine() {
        return createNmsLine(this.lib.getPlaceholderHandler(), this.lib.getPacketPreprocessor());
    }

    /**
     * Creates a new item line
     * @return ItemHologramLine
     */
    public ItemHologramLine createItemLine() {
        return createNmsItemLine(this.lib.getPlaceholderHandler());
    }

    /**
     * @param id Hologram ID
     * @throws IllegalStateException If an item hologram with this ID already exits
     * @return ItemHologram
     */
    public ItemHologram<InternalIdType> createItem(InternalIdType id) {
        return this.createItem(id, false);
    }

    /**
     * @param id Hologram ID
     * @return ItemHologram or null
     */
    public ItemHologram<InternalIdType> createItemIfNotExists(InternalIdType id) {
        return this.createItem(id, true);
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

    /**
     * @param id Hologram ID
     * @param silentFail Handle duplicate IDs gracefully
     * @throws IllegalStateException If an item hologram with this ID already exits && silentFail is false
     * @return ItemHologram or null
     */
    private ItemHologram<InternalIdType> createItem(InternalIdType id, boolean silentFail) {
        ItemHologram<InternalIdType> hologram = new ItemHologram<>(id);

        ItemHologram<InternalIdType> result = this.itemHologramsMap.compute(id, (id2, prevValue) -> prevValue == null ? hologram : prevValue);
        if (result != hologram) {
            if (silentFail) return null;
            else throw new IllegalStateException("Item hologram with that ID already exists");
        }

        hologram.hookLocationUpdate(this::onItemHoloPositionUpdate);
        return hologram;
    }

    private void onHoloPositionUpdate(Hologram<?> hologram, Location from, Location to) {
        this.cache.onHoloPositionUpdate(hologram, from, to);
        this.playerManager.updateTrackersNear(hologram, to);
    }

    private void onItemHoloPositionUpdate(Hologram<?> hologram, Location from, Location to) {
        this.cache.onHoloPositionUpdate(hologram, from, to);
        this.playerManager.updateTrackersNear(hologram, to);
    }

    public TextHologram<InternalIdType> getHologram(InternalIdType id) {
        return this.hologramsMap.get(id);
    }

    public ItemHologram<InternalIdType> getItemHologram(InternalIdType id) {
        return this.itemHologramsMap.get(id);
    }

    public void killAndRemove(InternalIdType id) {
        Optional.ofNullable(this.hologramsMap.remove(id))
                .ifPresent(this::tryHide);
        Optional.ofNullable(this.itemHologramsMap.remove(id))
                .ifPresent(this::tryHideItem);
    }

    public void killAndRemoveAll() {
        this.hologramsMap.values().forEach(this::tryHide);
        this.hologramsMap.clear();
        this.itemHologramsMap.values().forEach(this::tryHideItem);
        this.itemHologramsMap.clear();
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

    private void tryHideItem(@NotNull ItemHologram<InternalIdType> hologram) {
        if (hologram.isVisible()) hologram.hide();
    }

}
