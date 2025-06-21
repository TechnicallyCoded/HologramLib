package com.tcoded.hologramlib.hologram;

import com.tcoded.hologramlib.PlaceholderHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

public abstract class HologramLine {

    public static final Double DEFAULT_LINE_HEIGHT = 0.3;

    private final int entityId;
    private final UUID uuid;
    private final PlaceholderHandler placeholderHandler;

    private Location location;
    private Double height;

    public HologramLine(int entityId, PlaceholderHandler placeholderHandler) {
        this.entityId = entityId;
        this.uuid = UUID.randomUUID();
        this.placeholderHandler = placeholderHandler;
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

    public void sendMetaPacket(Collection<Player> players) {
        Pattern pattern = placeholderHandler.getPattern();

        if (pattern == null) {
            sendMetaPacket(players, null);
            return;
        }

        this.sendMetaPacket(players, (p, b) -> {
            b.match(pattern).replacement((result, b2) ->
                    Component.text(
                            // Adventure does not understand regex groups, we use 0 instead of 1
                            placeholderHandler.setPlaceholders(p, result.group(0))
                    )
            );
        });
    }

    protected abstract void sendMetaPacket(Collection<Player> players, @Nullable BiConsumer<Player, TextReplacementConfig.Builder> textParser);

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
