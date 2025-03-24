package com.tcoded.hologramlib.listener;

import com.tcoded.folialib.impl.PlatformScheduler;
import com.tcoded.hologramlib.manager.PlayerManager;
import com.tcoded.hologramlib.types.desync.MoveDesyncAction;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {

    private final PlatformScheduler scheduler;
    private final PlayerManager playerManager;

    public PlayerListener(PlatformScheduler scheduler, PlayerManager playerManager) {
        this.scheduler = scheduler;
        this.playerManager = playerManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        this.playerManager.createTracker(player);
        Location loc = player.getLocation();

        this.playerManager.recordDesync(new MoveDesyncAction(player, loc, loc));
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();

        Location from = event.getFrom();
        Location to = event.getTo();

        this.playerManager.recordDesync(new MoveDesyncAction(player, from, to));
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();

        Location from = event.getFrom();
        Location to = event.getTo();

        // Readability
        // noinspection CodeBlock2Expr
        this.scheduler.runAtEntityLater(player, wt -> {
            this.playerManager.recordDesync(new MoveDesyncAction(player, from, to));
        }, 1);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.playerManager.removeTracker(player);
    }

}
