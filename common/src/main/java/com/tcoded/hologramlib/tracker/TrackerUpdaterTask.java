package com.tcoded.hologramlib.tracker;

import com.tcoded.folialib.impl.PlatformScheduler;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import com.tcoded.hologramlib.manager.PlayerManager;
import com.tcoded.hologramlib.types.desync.DesyncAction;

import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class TrackerUpdaterTask implements Runnable {

    private final PlatformScheduler scheduler;
    private final PlayerManager playerManager;

    private WrappedTask task;

    public TrackerUpdaterTask(PlatformScheduler scheduler, PlayerManager playerManager) {
        this.scheduler = scheduler;
        this.playerManager = playerManager;
    }

    public void start() {
        task = scheduler.runTimerAsync(this, 1, 1);
    }

    public void cancel() {
        task.cancel();
    }

    @Override
    public void run() {
        Deque<DesyncAction> queue = playerManager.getAndSwitchDesyncQueue();
        Set<PlayerHologramTracker> trackers = new HashSet<>(queue.size());

        for (DesyncAction action : queue) {
            PlayerHologramTracker tracker = playerManager.getTracker(action.player());
            trackers.add(tracker);

            if (action.to() != null) {
                tracker.move(action.from(), action.to());
            }

            if (action.manualHolos() != null) {
                tracker.markDesynced(action.manualHolos());
            }

        }

        for (PlayerHologramTracker tracker : trackers) {
            tracker.checkAll();
        }

        queue.clear();
        playerManager.recycleDesyncQueue(queue);
    }

}
