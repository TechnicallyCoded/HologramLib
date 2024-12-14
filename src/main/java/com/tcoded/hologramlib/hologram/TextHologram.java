package com.tcoded.hologramlib.hologram;

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Quaternion4f;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.tcoded.hologramlib.HologramLib;
import com.tcoded.hologramlib.utils.Vector3F;
import com.tcoded.folialib.impl.PlatformScheduler;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.tofaa.entitylib.meta.EntityMeta;
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;


public class TextHologram {

    private final HologramLib lib;

    private long updateTaskPeriod = 20L * 3;
    private double nearbyEntityScanningDistance = 40.0;
    private final String id;

    private int entityID;

    protected Component text = Component.text("Hologram API");
    protected Vector3f scale = new Vector3f(1, 1, 1);
    protected Vector3f translation = new Vector3f(0, 0F, 0);

    protected Quaternion4f rightRotation = new Quaternion4f(0, 0, 0, 1);
    protected Quaternion4f leftRotation = new Quaternion4f(0, 0, 0, 1);

    private Display.Billboard billboard = Display.Billboard.CENTER;
    private int interpolationDurationRotation = 10;
    private int interpolationDurationTransformation = 10;
    private double viewRange = 1.0;
    private boolean shadow = true;
    private int maxLineWidth = 200;
    private int backgroundColor;
    private boolean seeThroughBlocks = false;
    private TextDisplay.TextAlignment alignment = TextDisplay.TextAlignment.CENTER;
    private byte textOpacity = (byte) -1;

    private final RenderMode renderMode;

    private Location location;

    private final List<Player> viewers = new CopyOnWriteArrayList<>();

    private boolean dead = false;

    private WrappedTask task;

    public TextHologram(HologramLib lib, String id, RenderMode renderMode) {
        this.lib = lib;
        this.renderMode = renderMode;
        validateId(id);
        this.id = id.toLowerCase();
        startRunnable();
    }

    private void validateId(String id) {
        if (id.contains(" ")) {
            throw new IllegalArgumentException("The hologram ID cannot contain spaces! (" + id + ")");
        }
    }

    private void startRunnable() {
        if (task != null) return;
        task = getScheduler().runTimerAsync(this::updateAffectedPlayers, 20L, updateTaskPeriod);
    }

    /**
     * Use HologramManager#spawn(TextHologram.class, Location.class); instead!
     * Only if you want to manage the holograms yourself and don't want to use the animation system use this
     */
    public void spawn(Location location) {
        this.location = location;
        entityID = ThreadLocalRandom.current().nextInt(4000, Integer.MAX_VALUE);
        WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(
                entityID, Optional.of(UUID.randomUUID()), EntityTypes.TEXT_DISPLAY,
                new Vector3d(location.getX(), location.getY() + 1, location.getZ()), 0f, 0f, 0f, 0, Optional.empty()
        );
        getScheduler().runNextTick(wt -> {
            updateAffectedPlayers();
            sendPacket(packet);
            this.dead = false;
        });
        update();
    }

    public void attach(TextHologram textHologram, int entityID) {
        int[] hologramToArray = { textHologram.getEntityID() };
        WrapperPlayServerSetPassengers attachPacket = new WrapperPlayServerSetPassengers(entityID, hologramToArray);
        getScheduler().runNextTick(wt -> {
            sendPacket(attachPacket);
        });
    }

    public TextHologram update() {
        getScheduler().runNextTick(wt -> {
            updateAffectedPlayers();
            TextDisplayMeta meta = createMeta();
            sendPacket(meta.createPacket());
        });
        return this;
    }

    private TextDisplayMeta createMeta() {
        TextDisplayMeta meta = (TextDisplayMeta) EntityMeta.createMeta(this.entityID, EntityTypes.TEXT_DISPLAY);
        meta.setText(getTextAsComponent());
        meta.setInterpolationDelay(-1);
        meta.setTransformationInterpolationDuration(this.interpolationDurationTransformation);
        meta.setPositionRotationInterpolationDuration(this.interpolationDurationRotation);
        meta.setTranslation(toVector3f(this.translation));
        meta.setScale(toVector3f(this.scale));
        meta.setBillboardConstraints(AbstractDisplayMeta.BillboardConstraints.valueOf(this.billboard.name()));
        meta.setLineWidth(this.maxLineWidth);
        meta.setViewRange((float) this.viewRange);
        meta.setBackgroundColor(this.backgroundColor);
        meta.setTextOpacity(this.textOpacity);
        meta.setShadow(this.shadow);
        meta.setSeeThrough(this.seeThroughBlocks);
        setAlignment(meta);
        return meta;
    }

    private TextHologram setAlignment(TextDisplayMeta meta) {
        switch (this.alignment) {
            case LEFT -> meta.setAlignLeft(true);
            case RIGHT -> meta.setAlignRight(true);
        }
        return this;
    }

    private com.github.retrooper.packetevents.util.Vector3f toVector3f(Vector3f vector) {
        return new com.github.retrooper.packetevents.util.Vector3f(vector.x, vector.y, vector.z);
    }

    /**
     * Use HologramManager#remove(TextHologram.class); instead!
     * Only if you want to manage the holograms yourself and don't want to use the animation system use this
     */
    public void kill() {
        WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(this.entityID);
        sendPacket(packet);
        this.dead = true;
    }

    public TextHologram teleport(Location location) {
        WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport(this.entityID, SpigotConversionUtil.fromBukkitLocation(location), false);
        this.location = location;
        sendPacket(packet);
        return this;
    }

    public TextHologram addAllViewers(List<Player> viewerList) {
        this.viewers.addAll(viewerList);
        return this;
    }

    public TextHologram addViewer(Player player) {
        this.viewers.add(player);
        return this;
    }

    public TextHologram removeViewer(Player player) {
        this.viewers.remove(player);
        return this;
    }

    public TextHologram removeAllViewers() {
        this.viewers.clear();
        return this;
    }

    public Vector3F getTranslation() {
        return new Vector3F(this.translation.x, this.translation.y, this.translation.z);
    }

    public TextHologram setLeftRotation(float x, float y, float z, float w) {
        this.leftRotation = new Quaternion4f(x, y, z, w);
        return this;
    }

    public TextHologram setRightRotation(float x, float y, float z, float w) {
        this.rightRotation = new Quaternion4f(x, y, z, w);
        return this;
    }

    public TextHologram setTranslation(float x, float y, float z) {
        this.translation = new Vector3f(x, y, z);
        return this;
    }

    public TextHologram setTranslation(Vector3F translation) {
        this.translation = new Vector3f(translation.x, translation.y, translation.z);
        return this;
    }

    public Vector3F getScale() {
        return new Vector3F(this.scale.x, this.scale.y, this.scale.z);
    }

    public TextHologram setScale(float x, float y, float z) {
        this.scale = new Vector3f(x, y, z);
        return this;
    }

    public TextHologram setScale(Vector3F scale) {
        this.scale = new Vector3f(scale.x, scale.y, scale.z);
        return this;
    }

    public Component getTextAsComponent() {
        return this.text;
    }

    public String getText() {
        return ((TextComponent) this.text).content();
    }

    public String getTextWithoutColor() {
        return ChatColor.stripColor(getText());
    }

    public TextHologram setText(String text) {
        this.text = Component.text(replaceFontImages(text));
        return this;
    }

    public TextHologram setText(Component component) {
        this.text = component;
        return this;
    }

    public TextHologram setMiniMessageText(String text) {
        this.text = MiniMessage.miniMessage().deserialize(replaceFontImages(text));
        return this;
    }

    private String replaceFontImages(String string) {
        return lib.getReplaceText().replace(string);
    }

    private void updateAffectedPlayers() {
        World world = this.location.getWorld();
        if (world == null) return;

        viewers.forEach(player -> {
            getScheduler().runAtEntity(player, wt -> {
                if (!player.isOnline()) return;
                if (player.getWorld() == world) return;
                if (player.getLocation().distance(this.location) <= 20) return;

                WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(this.entityID);
                lib.getPlayerManager().sendPacket(player, packet);
            });
        });

        if (this.renderMode == RenderMode.VIEWER_LIST) {
            return;
        }

        if (this.renderMode == RenderMode.ALL) {
            this.addAllViewers(new ArrayList<>(Bukkit.getOnlinePlayers()));
            return;
        }

        if (this.renderMode == RenderMode.NEARBY) {
            double radius = nearbyEntityScanningDistance;
            getScheduler().runAtLocation(this.location, wt -> {
                world.getNearbyEntities(this.location, radius, radius, radius)
                        .stream()
                        .filter(entity -> entity instanceof Player)
                        .forEach(entity -> this.viewers.add((Player) entity));
            });
        }
    }

    private void sendPacket(PacketWrapper<?> packet) {
        if (this.renderMode == RenderMode.NONE) return;
        viewers.forEach(player -> lib.getPlayerManager().sendPacket(player, packet));
    }

    public PlatformScheduler getScheduler() {
        return lib.getScheduler();
    }

    public Display.Billboard getBillboard() {
        return billboard;
    }

    public byte getTextOpacity() {
        return textOpacity;
    }

    public double getNearbyEntityScanningDistance() {
        return nearbyEntityScanningDistance;
    }

    public double getViewRange() {
        return viewRange;
    }

    public HologramLib getLib() {
        return lib;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getEntityID() {
        return entityID;
    }

    public int getInterpolationDurationRotation() {
        return interpolationDurationRotation;
    }

    public int getInterpolationDurationTransformation() {
        return interpolationDurationTransformation;
    }

    public int getMaxLineWidth() {
        return maxLineWidth;
    }

    public long getUpdateTaskPeriod() {
        return updateTaskPeriod;
    }

    public List<Player> getViewers() {
        return viewers;
    }

    public Location getLocation() {
        return location;
    }

    public Quaternion4f getLeftRotation() {
        return leftRotation;
    }

    public Quaternion4f getRightRotation() {
        return rightRotation;
    }

    public RenderMode getRenderMode() {
        return renderMode;
    }

    public String getId() {
        return id;
    }

    public TextDisplay.TextAlignment getAlignment() {
        return alignment;
    }

    public WrappedTask getTask() {
        return task;
    }

}
