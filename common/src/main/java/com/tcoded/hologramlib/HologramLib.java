package com.tcoded.hologramlib;

import com.tcoded.hologramlib.listener.PlayerListener;
import com.tcoded.hologramlib.listener.PluginListener;
import com.tcoded.hologramlib.manager.HologramManager;
import com.tcoded.hologramlib.hologram.TextHologram;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import com.tcoded.hologramlib.manager.PlayerManager;
import com.tcoded.hologramlib.tracker.TrackerUpdaterTask;
import com.tcoded.hologramlib.types.HologramLibShutdownExecutor;
import com.tcoded.hologramlib.utils.HologramLookupCache;
import com.tcoded.hologramlib.utils.SyncCatcher;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class HologramLib <InternalIdType> implements HologramLibShutdownExecutor {

    protected static final String CREDITS = "HologramLib by TechnicallyCoded. https://github.com/TechnicallyCoded/HologramLib";
    private static Logger LOGGER = null;

    public static Logger logger() {
        if (LOGGER == null) throw new IllegalStateException("Logger not initialized");
        return LOGGER;
    }

    public static HologramLib<String> stringIdentified(Plugin plugin) {
        return new HologramLib<>(plugin);
    }

    public static <U> HologramLib<U> customIdentified(Class<U> clazz, Plugin plugin) {
        return new HologramLib<>(plugin);
    }

    private final Plugin plugin;
    private FoliaLib foliaLib;
    private final PlatformScheduler scheduler;
    private PlaceholderHandler placeholderHandler;

    private HologramManager<InternalIdType> hologramManager;
    private PlayerManager playerManager;

    private PlayerListener playerListener;
    private PluginListener pluginListener;

    private TrackerUpdaterTask trackerUpdaterTask;

    public HologramLib(Plugin plugin) {
        LOGGER = Logger.getLogger(plugin.getName() + "/HologramLib");
        this.plugin = plugin;

        this.foliaLib = new FoliaLib(plugin);
        this.scheduler = foliaLib.getScheduler();
        this.placeholderHandler = PlaceholderHandler.createHandler();

        // Cache
        HologramLookupCache hologramLookupCache = new HologramLookupCache();

        // Player manager
        this.playerManager = new PlayerManager(hologramLookupCache, () -> plugin.getServer().getOnlinePlayers());

        // Setup hologram manager
        String mcVersion = this.getPlugin().getServer().getMinecraftVersion();
        String parsedVersion = mcVersion.replace('.', '_');

        try {
            String rawClassName = HologramLib.class.getPackageName() + ".nms.v{VERSION}.NmsHologramManager";
            String versionedClassName = rawClassName.replace("{VERSION}", parsedVersion);

            Class<?> clazz = Class.forName(versionedClassName);
            Constructor<?> constructor = clazz.getConstructor(this.getClass());

            // noinspection unchecked
            hologramManager = (HologramManager<InternalIdType>) constructor.newInstance(this);
        } catch (Exception e) {
            // noinspection CallToPrintStackTrace
            e.printStackTrace();
        }

        this.hologramManager.withCache(hologramLookupCache);
        this.hologramManager.withPlayerManager(this.playerManager);

        // Listeners
        this.playerListener = new PlayerListener(this.getScheduler(), this.getPlayerManager());
        this.pluginListener = new PluginListener(this.plugin, this);
        this.plugin.getServer().getPluginManager().registerEvents(this.playerListener, this.plugin);

        // Tasks
        this.trackerUpdaterTask = new TrackerUpdaterTask(
                this.getFoliaLib().getScheduler(),
                this.getPlayerManager()
        );
        this.trackerUpdaterTask.start();

        // Init online players
        this.plugin.getServer().getOnlinePlayers().forEach(this.playerManager::createTracker);

    }

    @Override
    public void shutdown() {
        SyncCatcher.shutdownMode();

        // Shutdown listeners
        if (this.playerListener != null) HandlerList.unregisterAll(this.playerListener);
        this.playerListener = null;

        if (this.pluginListener != null) HandlerList.unregisterAll(this.pluginListener);
        this.pluginListener = null;

        // Shutdown tasks
        if (this.foliaLib != null) this.foliaLib.getScheduler().cancelAllTasks();
        this.foliaLib = null;

        // Shutdown managers
        if (this.hologramManager != null) this.hologramManager.killAndRemoveAll();
        this.hologramManager = null;

        // Shutdown player manager - Force desync all players
        if (this.playerManager != null) this.playerManager.recordDesyncs(this.plugin.getServer().getOnlinePlayers());
        this.playerManager = null;

        // Final update to trackers in order to remove all holograms client-side
        if (this.trackerUpdaterTask != null) {
            this.trackerUpdaterTask.run();
            this.trackerUpdaterTask.cancel(); // Should already be cancelled but let's make sure
        }
        this.trackerUpdaterTask = null;
    }

    public FoliaLib getFoliaLib() {
        return foliaLib;
    }

    public HologramManager<InternalIdType> getHologramManager() {
        return hologramManager;
    }

    public PlatformScheduler getScheduler() {
        return scheduler;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public TextHologram<InternalIdType> create(InternalIdType id) {
        return getHologramManager().create(id);
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public PlaceholderHandler getPlaceholderHandler() {
        return placeholderHandler;
    }

}
