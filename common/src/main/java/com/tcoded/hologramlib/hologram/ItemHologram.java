package com.tcoded.hologramlib.hologram;

import com.google.common.collect.ImmutableList;
import com.tcoded.hologramlib.tracker.HologramPlayerTracker;
import com.tcoded.hologramlib.types.LocationUpdateHook;
import com.tcoded.hologramlib.utils.SyncCatcher;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class ItemHologram<InternalIdType> implements Hologram<InternalIdType> {

    private final InternalIdType internalId;
    private final HologramPlayerTracker tracker;

    private ImmutableList<ItemHologramLine> lines;
    private ReentrantLock linesLock;

    private boolean visible;
    private ReentrantLock visibleLock;

    private float trackingDistance;

    private Location location;
    private boolean locationSynced;
    private Location lastSyncedLocation;
    private LocationUpdateHook locationUpdateHook;

    public ItemHologram(InternalIdType internalId) {
        this.internalId = internalId;
        this.tracker = new HologramPlayerTracker(this);

        this.lines = ImmutableList.of();
        this.linesLock = new ReentrantLock();

        this.visible = false;
        this.visibleLock = new ReentrantLock();

        this.trackingDistance = 40.0f;

        this.location = null;
        this.locationSynced = false;
        this.lastSyncedLocation = null;
        this.locationUpdateHook = null;
    }

    @Override
    public void show() {
        SyncCatcher.ensureAsync();

        this.visibleLock.lock();
        try {
            if (this.isVisible()) {
                throw new IllegalStateException("Hologram already visible");
            }

            if (this.location == null) {
                throw new IllegalStateException("Hologram location is null");
            }

            if (!this.locationSynced) {
                this.syncLocation();
            }

            this.setVisible(true);

            List<Player> viewers = this.tracker.getAllViewingPlayers();
            this.show(viewers);
        } finally {
            this.visibleLock.unlock();
        }
    }

    @ApiStatus.Internal
    public void show(List<Player> players) {
        SyncCatcher.ensureAsync();

        this.sendSpawnPackets(players);
        this.sendMetaPackets(players);
    }

    @Override
    public void hide() {
        SyncCatcher.ensureAsync();

        this.visibleLock.lock();
        try {
            if (!this.isVisible()) {
                throw new IllegalStateException("Hologram already hidden");
            }

            this.setVisible(false);

            List<Player> viewers = this.tracker.getAllViewingPlayers();
            this.hide(viewers);
        } finally {
            this.visibleLock.unlock();
        }
    }

    @ApiStatus.Internal
    public void hide(List<Player> players) {
        this.sendKillPackets(players);
    }

    @Override
    public void updateMeta() {
        SyncCatcher.ensureAsync();

        List<Player> viewers = this.tracker.getAllViewingPlayers();
        sendMetaPackets(viewers);
    }

    @Override
    public InternalIdType getInternalId() {
        return this.internalId;
    }

    @Override
    public Location getLocation() {
        return location.clone();
    }

    @Override
    public void setLocation(Location location) {
        boolean holoVisible;

        this.visibleLock.lock();
        try {
            holoVisible = this.isVisible();
        } finally {
            this.visibleLock.unlock();
        }

        if (holoVisible) SyncCatcher.ensureAsync();

        this.location = location.clone();

        if (holoVisible && this.syncLocation()) {
            this.sendTeleportPackets(this.tracker.getAllViewingPlayers());
        }
    }

    @Override
    public void hookLocationUpdate(LocationUpdateHook hook) {
        this.locationUpdateHook = hook;
    }

    @Override
    public float getTrackingDistance() {
        return trackingDistance;
    }

    @Override
    public void setTrackingDistance(float trackingDistance) {
        this.trackingDistance = trackingDistance;
    }

    @Override
    public HologramPlayerTracker getTracker() {
        return this.tracker;
    }

    @Override
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isVisible() {
        this.visibleLock.lock();
        try {
            return this.visible;
        } finally {
            this.visibleLock.unlock();
        }
    }

    private void setVisible(boolean state) {
        this.visibleLock.lock();
        try {
            this.visible = state;
        } finally {
            this.visibleLock.unlock();
        }
    }

    public List<ItemHologramLine> getLines() {
        try {
            this.linesLock.lock();
            return this.lines;
        } finally {
            this.linesLock.unlock();
        }
    }

    public ItemHologramLine getLine(int index) {
        try {
            this.linesLock.lock();

            if (index < 0 || index >= this.lines.size()) {
                throw new IndexOutOfBoundsException("Index out of bounds");
            }

            return this.lines.get(index);
        } finally {
            this.linesLock.unlock();
        }
    }

    public void addLine(ItemHologramLine line) {
        boolean holoVisible;

        this.visibleLock.lock();
        try {
            holoVisible = this.isVisible();
        } finally {
            this.visibleLock.unlock();
        }

        if (holoVisible) SyncCatcher.ensureAsync();

        ImmutableList.Builder<ItemHologramLine> linesBuilder = ImmutableList.builder();
        linesBuilder.addAll(this.lines);
        linesBuilder.add(line);

        try {
            this.linesLock.lock();

            this.lines = linesBuilder.build();

            if (holoVisible) {
                this.syncLineLocations();
                this.sendTeleportPackets(this.tracker.getAllViewingPlayers());
            }
        } finally {
            this.linesLock.unlock();
        }
    }

    public void removeLine(int index) {
        boolean holoVisible;

        this.visibleLock.lock();
        try {
            holoVisible = this.isVisible();
        } finally {
            this.visibleLock.unlock();
        }

        if (holoVisible) SyncCatcher.ensureAsync();

        try {
            this.linesLock.lock();

            if (index < 0 || index >= this.lines.size()) {
                throw new IndexOutOfBoundsException("Index out of bounds");
            }

            ImmutableList.Builder<ItemHologramLine> linesBuilder = ImmutableList.builder();
            for (int i = 0; i < this.lines.size(); i++) {
                if (i != index) {
                    linesBuilder.add(this.lines.get(i));
                }
            }

            this.lines = linesBuilder.build();

            if (holoVisible) {
                this.sendTeleportPackets(this.tracker.getAllViewingPlayers());
                this.syncLineLocations();
            }
        } finally {
            this.linesLock.unlock();
        }

        if (holoVisible) this.sendTeleportPackets(this.tracker.getAllViewingPlayers());
    }

    public void updateLocations() {
        this.syncLineLocations();
    }

    private boolean syncLocation() {
        if (this.location == null) {
            throw new IllegalStateException("Hologram location is null");
        }

        Location from = this.lastSyncedLocation;
        Location to = this.location;

        boolean updated = false;
        if (!Objects.equals(from, to)) {
            this.syncLineLocations();
            if (this.locationUpdateHook != null) {
                this.locationUpdateHook.handle(this, from, to);
            }
            updated = true;
        }

        this.lastSyncedLocation = this.location;
        this.locationSynced = true;
        return updated;
    }

    private void syncLineLocations() {
        try {
            this.linesLock.lock();

            ImmutableList<ItemHologramLine> linesRef = this.lines;

            Location currentLocation = this.location.clone();
            for (int i = linesRef.size() - 1; i >= 0; i--) {
                ItemHologramLine line = linesRef.get(i);
                line.setLocation(currentLocation);

                currentLocation.add(0, line.getHeight().orElse(HologramLine.DEFAULT_LINE_HEIGHT), 0);
            }
        } finally {
            this.linesLock.unlock();
        }
    }

    private void sendSpawnPackets(List<Player> players) {
        for (ItemHologramLine line : this.lines) {
            line.sendSpawnPacket(players);
        }
    }

    private void sendMetaPackets(List<Player> players) {
        for (ItemHologramLine line : this.lines) {
            line.sendMetaPacket(players);
        }
    }

    private void sendTeleportPackets(List<Player> players) {
        for (ItemHologramLine line : this.lines) {
            line.sendTeleportPackets(players);
        }
    }

    private void sendKillPackets(List<Player> players) {
        for (ItemHologramLine line : this.lines) {
            line.sendKillPacket(players);
        }
    }

}
