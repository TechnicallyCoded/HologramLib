package com.tcoded.hologramlib.tracker;

import com.tcoded.hologramlib.hologram.TextHologram;
import com.tcoded.hologramlib.types.HologramPlayerAction;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;

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
        // Patch: todo: some concurrent modification is allowing stale player trackers to remain in the set
        List<PlayerHologramTracker> toRemove = new LinkedList<>();

        List<@Nullable Player> result = this.viewers.stream()
                // Patch: filter out stale trackers
                .filter(pTracker -> {
                    Player bukkitPlayer = pTracker.getPlayer();
                    if (bukkitPlayer == null || !bukkitPlayer.isOnline()) {
                        toRemove.add(pTracker);
                        return false;
                    }
                    return true;
                })
                .map(PlayerHologramTracker::getPlayer)
                .toList();

        // Patch: remove stale trackers
        toRemove.forEach(this::removeViewer);

        return result;
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
