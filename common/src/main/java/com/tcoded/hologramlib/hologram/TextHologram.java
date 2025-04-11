package com.tcoded.hologramlib.hologram;

import com.tcoded.hologramlib.hologram.meta.TextDisplayMeta;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import com.tcoded.hologramlib.tracker.HologramPlayerTracker;
import com.tcoded.hologramlib.types.LocationUpdateHook;
import com.tcoded.hologramlib.utils.SyncCatcher;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.*;


public abstract class TextHologram <InternalIdType> {

    private final int id;
    private final InternalIdType internalId;
    private final UUID uuid;
    private final TextDisplayMeta meta;
    private final HologramPlayerTracker tracker;

    private boolean visible;
    private float trackingDistance;
    private Location location;

    private WrappedTask task;
    private LocationUpdateHook locationUpdateHook;

    public TextHologram(InternalIdType internalId, int id) {
        this.internalId = internalId;
        this.id = id;
        this.uuid = UUID.randomUUID();
        this.meta = new TextDisplayMeta();
        this.tracker = new HologramPlayerTracker(this);
        this.trackingDistance = 40.0f;
        this.visible = false;
    }

    public void show() {
        SyncCatcher.ensureAsync();
        this.visible = true;

        List<Player> viewers = this.tracker.getAllViewingPlayers();
        sendSpawnPacket(viewers);
        sendMetaPacket(viewers);
    }

    public void hide() {
        SyncCatcher.ensureAsync();

        List<Player> viewers = this.tracker.getAllViewingPlayers();
        sendKillPacket(viewers);
    }

    public void spawn(List<Player> players) {
        SyncCatcher.ensureAsync();

        this.sendSpawnPacket(players);
        this.sendMetaPacket(players);
    }

    public void updateMeta() {
        SyncCatcher.ensureAsync();

        List<Player> viewers = this.tracker.getAllViewingPlayers();
        sendMetaPacket(viewers);
    }

    public void kill(List<Player> players) {
        this.sendKillPacket(players);
    }

    /**
     * Use {@link TextHologram#spawn(List)} instead!
     */
    public abstract void sendSpawnPacket(Collection<Player> players);

    public abstract void sendSetPassengerPacket(Collection<Player> players, int baseEntityId);

    public abstract void sendMetaPacket(Collection<Player> players);

    public TextDisplayMeta getMeta() {
        return this.meta;
    }

    private void setAlignment(TextDisplay.TextAlignment alignment) {
        switch (alignment) {
            case LEFT -> getMeta().setAlignLeft(true);
            case RIGHT -> getMeta().setAlignRight(true);
        }
    }

    /**
     * Use HologramManager#remove(TextHologram.class); instead!
     * Only if you want to manage the holograms yourself and don't want to use the animation system use this
     */
    public abstract void sendKillPacket(Collection<Player> players);

    protected abstract void sendTeleportPacket(Collection<Player> players, Location location);

    public int getId() {
        return this.id;
    }

    public Location getLocation() {
        return location;
    }

    public WrappedTask getTask() {
        return task;
    }

    protected UUID getUuid() {
        return this.uuid;
    }

    public InternalIdType getInternalId() {
        return this.internalId;
    }

    public void setLocation(Location location) {
        SyncCatcher.ensureAsync();
        Location from = this.location;
        this.location = location;
        if (this.locationUpdateHook != null) this.locationUpdateHook.handle(this, from, location);
    }

    public void hookLocationUpdate(LocationUpdateHook hook) {
        this.locationUpdateHook = hook;
    }

    public float getTrackingDistance() {
        return trackingDistance;
    }

    public void setTrackingDistance(float trackingDistance) {
        this.trackingDistance = trackingDistance;
    }

    public HologramPlayerTracker getTracker() {
        return this.tracker;
    }

    // "Never prefer not using non-negatives"
    // Negating code only serves to confuse
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isVisible() {
        return this.visible;
    }

}
