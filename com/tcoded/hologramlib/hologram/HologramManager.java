package com.tcoded.hologramlib.hologram;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class HologramManager {

    private final Map<TextHologram, WrappedTask> hologramAnimations = new ConcurrentHashMap<>();
    private final Map<String, TextHologram> hologramsMap = new ConcurrentHashMap<>();

//    public HologramManager(Plugin plugin) {
//        this.plugin = plugin;
//    }


    public List<TextHologram> getHolograms() {
        return new ArrayList<>(this.hologramsMap.values());
    }

    public abstract TextHologram create(String id, RenderMode renderMode);

    public void spawn(TextHologram textHologram, Location location) {
        textHologram.spawn(location);
        this.hologramsMap.put(textHologram.getId(), textHologram);
    }

    public void attach(TextHologram textHologram, int entityID) {
        textHologram.attach(textHologram, entityID);
    }

    public void register(TextHologram textHologram) {
        this.hologramsMap.put(textHologram.getId(), textHologram);
    }

    public void remove(TextHologram textHologram) {
        remove(textHologram.getId());
    }

    public void remove(String id) {
        Optional.ofNullable(this.hologramsMap.remove(id)).ifPresent(TextHologram::kill);
    }

    public void removeAll() {
        this.hologramsMap.values().forEach(TextHologram::kill);
        this.hologramsMap.clear();
    }

//    public void applyAnimation(TextHologram hologram, TextAnimation textAnimation) {
//        cancelAnimation(hologram);
//        hologramAnimations.put(hologram, animateHologram(hologram, textAnimation));
//    }

    public void cancelAnimation(TextHologram hologram) {
        Optional.ofNullable(hologramAnimations.remove(hologram)).ifPresent(WrappedTask::cancel);
    }

//    private WrappedTask animateHologram(TextHologram hologram, TextAnimation textAnimation) {
//        AtomicInteger currentFrame = new AtomicInteger(0);
//
//        return hologram.getScheduler().runTimerAsync(() -> {
//                if (textAnimation.getTextFrames().isEmpty()) return;
//                hologram.setMiniMessageText(textAnimation.getTextFrames().get(currentFrame.get()));
//                hologram.update();
//                currentFrame.set((currentFrame.get() + 1) % textAnimation.getTextFrames().size());
//        }, textAnimation.getDelay(), textAnimation.getSpeed());
//    }

    public Map<String, TextHologram> getHologramsMap() {
        return hologramsMap;
    }

    public Map<TextHologram, WrappedTask> getHologramAnimations() {
        return hologramAnimations;
    }
}
