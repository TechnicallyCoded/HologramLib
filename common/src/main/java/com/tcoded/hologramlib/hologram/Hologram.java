package com.tcoded.hologramlib.hologram;

import com.tcoded.hologramlib.tracker.HologramPlayerTracker;
import com.tcoded.hologramlib.types.LocationUpdateHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public interface Hologram<InternalIdType> {

    InternalIdType getInternalId();

    Location getLocation();

    void setLocation(Location location);

    void hookLocationUpdate(LocationUpdateHook hook);

    float getTrackingDistance();

    void setTrackingDistance(float trackingDistance);

    HologramPlayerTracker getTracker();

    boolean isVisible();

    void show();

    void show(List<Player> players);

    void hide();

    void hide(List<Player> players);

    void updateMeta();

}
