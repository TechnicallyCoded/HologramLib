package com.tcoded.hologramlib;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.tcoded.hologramlib.hologram.HologramManager;
import com.tcoded.hologramlib.hologram.RenderMode;
import com.tcoded.hologramlib.hologram.TextHologram;
import com.tcoded.hologramlib.utils.ReplaceText;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class HologramLib {

    private final Plugin plugin;
    private final FoliaLib foliaLib;
    private final PlatformScheduler scheduler;

    private HologramManager hologramManager;
    private ReplaceText replaceText;
    private PlayerManager playerManager;

    public HologramLib(Plugin plugin) {
        this.plugin = plugin;
        this.foliaLib = new FoliaLib((JavaPlugin) plugin);
        this.scheduler = foliaLib.getScheduler();

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this.plugin));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false)
                .checkForUpdates(false)
                .bStats(false);
        PacketEvents.getAPI().load();

        PacketEvents.getAPI().init();

//        SpigotEntityLibPlatform platform = new SpigotEntityLibPlatform((JavaPlugin) this.getPlugin());
//        APIConfig settings = new APIConfig(PacketEvents.getAPI())
//                .usePlatformLogger();
//
//        EntityLib.init(platform, settings);

        playerManager = PacketEvents.getAPI().getPlayerManager();

        hologramManager = new HologramManager(this.plugin);

//        try {
//            replaceText = new ItemsAdderHolder();
//        } catch (ClassNotFoundException exception) {
//            replaceText = s -> s;
//        }
        replaceText = s -> s;

    }

    public void disable() {
        PacketEvents.getAPI().terminate();
    }

    public FoliaLib getFoliaLib() {
        return foliaLib;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public PlatformScheduler getScheduler() {
        return scheduler;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public ReplaceText getReplaceText() {
        return replaceText;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public TextHologram createNearby(String id) {
        return create(id, RenderMode.NEARBY);
    }

    public TextHologram createVirtual(String id) {
        return create(id, RenderMode.VIEWER_LIST);
    }

    public TextHologram createAll(String id) {
        return create(id, RenderMode.ALL);
    }

    public TextHologram createNone(String id) {
        return create(id, RenderMode.NONE);
    }

}
