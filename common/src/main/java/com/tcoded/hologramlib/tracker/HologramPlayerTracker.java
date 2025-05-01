package com.tcoded.hologramlib.tracker;

import com.tcoded.hologramlib.hologram.TextHologram;
import com.tcoded.hologramlib.types.HologramPlayerAction;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

public class HologramPlayerTracker {

    private final WeakReference<TextHologram<?>> hologramRef;
    private final Set<PlayerHologramTracker> viewers;

    public HologramPlayerTracker(TextHologram<?> hologram) {
        this.hologramRef = new WeakReference<>(hologram);
        this.viewers = createTrackedSet();
    }

    public Set<PlayerHologramTracker> getAllViewers() {
        return Set.copyOf(this.viewers);
    }

    public List<Player> getAllViewingPlayers() {
        return this.viewers.stream().map(PlayerHologramTracker::getPlayer).toList();
    }

    public void addViewer(PlayerHologramTracker holoTracker) {
        this.viewers.add(holoTracker);
        updatePlayer(holoTracker, TextHologram::show);
    }

    public void removeViewer(PlayerHologramTracker holoTracker) {
        this.viewers.remove(holoTracker);
        updatePlayer(holoTracker, TextHologram::hide);
    }

    public boolean isViewing(PlayerHologramTracker playerHoloTracker) {
        return this.viewers.contains(playerHoloTracker);
    }

    private void updatePlayer(PlayerHologramTracker holoTracker, HologramPlayerAction consumer) {
        TextHologram<?> holo = hologramRef.get();
        if (holo == null) return;
        if (!holo.isVisible()) return;

        Player player = holoTracker.getPlayer();
        if (player == null) return;

        List<Player> players = List.of(player);
        consumer.accept(holo, players);
    }

    private static @NotNull Set<PlayerHologramTracker> createTrackedSet() {
        return Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    }

}
