package com.tcoded.hologramlib.hologram;

import com.google.common.collect.ImmutableList;
import com.tcoded.hologramlib.PlaceholderHandler;
import com.tcoded.hologramlib.tracker.HologramPlayerTracker;
import com.tcoded.hologramlib.types.LocationUpdateHook;
import com.tcoded.hologramlib.utils.SyncCatcher;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;


public class TextHologram <InternalIdType> implements Hologram<InternalIdType> {

    private final InternalIdType internalId;
    private final HologramPlayerTracker tracker;

    // The lines of the hologram. List needs to be rebuilt for any edit to structure.
    private ImmutableList<TextHologramLine> lines;
    private ReentrantLock linesLock;

    private boolean visible;
    private ReentrantLock visibleLock;

    private float trackingDistance;

    private Location location;
    private boolean locationSynced;
    private Location lastSyncedLocation;
    private LocationUpdateHook locationUpdateHook;

    public TextHologram(InternalIdType internalId) {
        this.internalId = internalId;
        this.tracker = new HologramPlayerTracker(this);

        this.lines = ImmutableList.of();
        this.linesLock = new ReentrantLock();

        this.visible = false; // initial state - no lock
        this.visibleLock = new ReentrantLock();

        this.trackingDistance = 40.0f;

        this.location = null;
        this.locationSynced = false;
        this.lastSyncedLocation = null;
        this.locationUpdateHook = null;
    }

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
                this.syncLocation(); // ignore result since spawn packets will be sent anyway
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

    public void updateMeta() {
        SyncCatcher.ensureAsync();

        List<Player> viewers = this.tracker.getAllViewingPlayers();
        sendMetaPackets(viewers);
    }

    public InternalIdType getInternalId() {
        return this.internalId;
    }

    public Location getLocation() {
        return location.clone();
    }

    public void setLocation(Location location) {
        boolean holoVisible; // thread safety: consistency

        this.visibleLock.lock();
        try {
            holoVisible = this.isVisible();
        } finally {
            this.visibleLock.unlock();
        }

        if (holoVisible) SyncCatcher.ensureAsync();

        this.location = location.clone();

        // If the hologram is visible, we need to sync the location
        // If the location was not previously synced, we need to teleport the hologram lines
        if (holoVisible && this.syncLocation()) {
            this.sendTeleportPackets(this.tracker.getAllViewingPlayers());
        }
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

    public List<TextHologramLine> getLines() {
        try {
            this.linesLock.lock();
            return this.lines;
        } finally {
            this.linesLock.unlock();
        }
    }

    public TextHologramLine getLine(int index) {
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

    public void addLine(TextHologramLine line) {
        boolean holoVisible; // thread safety: consistency

        this.visibleLock.lock();
        try {
            holoVisible = this.isVisible();
        } finally {
            this.visibleLock.unlock();
        }

        if (holoVisible) SyncCatcher.ensureAsync();

        ImmutableList.Builder<TextHologramLine> linesBuilder = ImmutableList.builder();
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
        boolean holoVisible; // thread safety: consistency

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

            ImmutableList.Builder<TextHologramLine> linesBuilder = ImmutableList.builder();
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

    /**
     * @return true if the location was updated, false if it was already synced
     */
    private boolean syncLocation() {
        if (this.location == null) {
            throw new IllegalStateException("Hologram location is null");
        }

        Location from = this.lastSyncedLocation;
        Location to = this.location;

        boolean updated = false;
        if (!Objects.equals(from, to)) {
            this.syncLineLocations();
            this.locationUpdateHook.handle(this, from, to);
            updated = true;
        }

        this.lastSyncedLocation = this.location;
        this.locationSynced = true;
        return updated;
    }

    private void syncLineLocations() {
        try {
            this.linesLock.lock();

            ImmutableList<TextHologramLine> linesRef = this.lines;

            Location currentLocation = this.location.clone();
            for (int i = linesRef.size() - 1; i >= 0; i--) {
                TextHologramLine line = linesRef.get(i);
                line.setLocation(currentLocation);

                currentLocation.add(0, line.getHeight().orElse(HologramLine.DEFAULT_LINE_HEIGHT), 0);
            }
        } finally {
            this.linesLock.unlock();
        }
    }

    private void sendSpawnPackets(List<Player> players) {
        for (TextHologramLine line : this.lines) {
            line.sendSpawnPacket(players);
        }
    }

    private void sendMetaPackets(List<Player> players) {
        for (TextHologramLine line : this.lines) {
            line.sendMetaPacket(players);
        }
    }

    private void sendTeleportPackets(List<Player> players) {
        for (TextHologramLine line : this.lines) {
            line.sendTeleportPackets(players);
        }
    }

    private void sendKillPackets(List<Player> players) {
        for (TextHologramLine line : this.lines) {
            line.sendKillPacket(players);
        }
    }

}
