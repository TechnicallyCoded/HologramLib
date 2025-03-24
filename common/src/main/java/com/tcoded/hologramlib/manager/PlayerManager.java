package com.tcoded.hologramlib.manager;

import com.tcoded.hologramlib.hologram.TextHologram;
import com.tcoded.hologramlib.tracker.PlayerHologramTracker;
import com.tcoded.hologramlib.types.PlayerListSupplier;
import com.tcoded.hologramlib.types.desync.DesyncAction;
import com.tcoded.hologramlib.types.desync.ManualDesyncAction;
import com.tcoded.hologramlib.types.desync.SpecificDesyncAction;
import com.tcoded.hologramlib.utils.HologramLookupCache;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {

    private final HologramLookupCache hologramLookupCache;
    private final PlayerListSupplier playerListSupplier;
    private final Map<UUID, PlayerHologramTracker> trackers;

    private final Queue<Deque<DesyncAction>> freeQueues;

    private final Object mutex = new Object();
    private Deque<DesyncAction> desyncs;

    public PlayerManager(HologramLookupCache hologramLookupCache, PlayerListSupplier playerListSupplier) {
        this.hologramLookupCache = hologramLookupCache;
        this.playerListSupplier = playerListSupplier;
        this.trackers = new ConcurrentHashMap<>();
        this.freeQueues = new LinkedList<>();

        getAndSwitchDesyncQueue();
    }

    public void recordDesync(DesyncAction action) {
        this.desyncs.add(action);
    }

    public void recordDesyncs(Collection<? extends Player> players) {
        players.forEach(p -> recordDesync(new ManualDesyncAction(p)));
    }

    public Deque<DesyncAction> getAndSwitchDesyncQueue() {
        int playerCount = getPlayerCount();

        Deque<DesyncAction> previous;
        synchronized (mutex) {
            previous = desyncs;
        }

        Deque<DesyncAction> polled;

        // Check if we can reuse
        while ((polled = freeQueues.poll()) != null) {
            // Require minimum of 2x player count
            if (polled.size() < calcMinSize(playerCount)) continue;
            synchronized (mutex) {
                this.desyncs = polled;
            }
            return previous;
        }

        // Init with 3x player count to allow reuse as much as possible
        synchronized (mutex) {
            this.desyncs = new ArrayDeque<>(calcDefaultSize(playerCount));
        }
        return previous;
    }

    private int getPlayerCount() {
        return playerListSupplier.get().size();
    }

    public void recycleDesyncQueue(Deque<DesyncAction> queue) {
        queue.clear();
        this.freeQueues.add(queue);
    }

    private static int calcMinSize(int playerCount) {
        return playerCount * 2;
    }

    private static int calcDefaultSize(int playerCount) {
        return playerCount * 3;
    }

    // Use by projects using the library, ignore
    @SuppressWarnings("UnusedReturnValue")
    public PlayerHologramTracker createTracker(Player player) {
        PlayerHologramTracker tracker = new PlayerHologramTracker(this.hologramLookupCache, player);
        this.trackers.put(player.getUniqueId(), tracker);
        return tracker;
    }

    public PlayerHologramTracker getTracker(Player player) {
        return this.trackers.get(player.getUniqueId());
    }

    public void removeTracker(Player player) {
        PlayerHologramTracker tracker = this.trackers.remove(player.getUniqueId());
        tracker.getTrackedArea().forEach(chunk -> {
            // Stop tracking player for each hologram in the chunk
            Collection<TextHologram<?>> holograms = this.hologramLookupCache.getHolograms(chunk);
            if (holograms == null) return;
            holograms.forEach(holo -> holo.getTracker().removeViewer(tracker));
        });
    }

    public void updateTrackersNear(TextHologram<?> hologram, Location to) {
        // Build set of players to update
        Set<PlayerHologramTracker> playerHoloTrackers = new HashSet<>();

        // Update all previously tracked players
        // Also readability > perfection here
        // noinspection CollectionAddAllCanBeReplacedWithConstructor
        playerHoloTrackers.addAll(hologram.getTracker().getAllViewers());

        // Update all players near the new location
        addAllNear(to, playerHoloTrackers);

        // Mark all players as desynced
        markDesynced(playerHoloTrackers, hologram);
    }

    private void addAllNear(Location to, Set<PlayerHologramTracker> trackers) {
        for (Player player : this.getOnlinePlayers()) {
            PlayerHologramTracker tracker = this.getTracker(player);
            if (tracker == null) continue;

            Location pLoc = player.getLocation();
            if (pLoc.getWorld() != to.getWorld()) continue;

            int viewDistance = player.getViewDistance();
            int viewDistSquared = viewDistance * viewDistance;

            if (pLoc.distanceSquared(to) <= viewDistSquared) {
                trackers.add(tracker);
            }
        }
    }

    private Collection<? extends Player> getOnlinePlayers() {
        return this.playerListSupplier.get();
    }

    private void markDesynced(Set<PlayerHologramTracker> playerHoloTrackers, TextHologram<?> hologram) {
        for (PlayerHologramTracker playerHoloTracker : playerHoloTrackers) {
            // Sanity check
            Player player = playerHoloTracker.getPlayer();
            if (player == null) continue;

            SpecificDesyncAction action = new SpecificDesyncAction(player, List.of(hologram));
            this.recordDesync(action);
        }
    }

}
