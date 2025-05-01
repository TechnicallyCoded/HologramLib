package com.tcoded.hologramlib.hologram;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class HologramLine {

    public static final Double DEFAULT_LINE_HEIGHT = 0.3;

    private final int entityId;
    private final UUID uuid;

    private Location location;
    private Double height;

    public HologramLine(int entityId) {
        this.entityId = entityId;
        this.uuid = UUID.randomUUID();
        this.location = null;
        this.height = null;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public UUID getEntityUuid() {
        return this.uuid;
    }

    public Location getLocation() {
        return this.location.clone();
    }

    public void setLocation(Location location) {
        this.location = location.clone();
    }

    public Optional<Double> getHeight() {
        return Optional.ofNullable(this.height);
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    /**
     * Use {@link TextHologram#show()} instead!
     */
    public abstract void sendSpawnPacket(Collection<Player> players);

    public abstract void sendSetPassengerPacket(Collection<Player> players, int baseEntityId);

    public abstract void sendMetaPacket(Collection<Player> players);

    /**
     * Use HologramManager#remove(TextHologram.class); instead!
     * Only if you want to manage the holograms yourself and don't want to use the animation system use this
     */
    public abstract void sendKillPacket(Collection<Player> players);

    protected abstract void sendTeleportPacket(Collection<Player> players, Location location);

    public void sendTeleportPackets(List<Player> viewers) {
        this.sendTeleportPacket(viewers, this.location);
    }

}
