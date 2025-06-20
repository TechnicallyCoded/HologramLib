package com.tcoded.hologramlib.nms.v1_21_4;

import com.google.common.collect.ImmutableList;
import com.mojang.math.Transformation;
import com.tcoded.hologramlib.HologramLib;
import com.tcoded.hologramlib.PlaceholderHandler;
import com.tcoded.hologramlib.hologram.TextHologramLine;
import com.tcoded.hologramlib.hologram.meta.BillboardConstraints;
import com.tcoded.hologramlib.hologram.meta.TextDisplayMeta;
import com.tcoded.hologramlib.types.math.Quaternion4F;
import com.tcoded.hologramlib.types.math.Vector3F;
import io.papermc.paper.adventure.PaperAdventure;
import it.unimi.dsi.fastutil.ints.IntList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Level;

public class NmsTextHologramLine extends TextHologramLine {

    private final Display.TextDisplay parent;

    public static NmsTextHologramLine create(PlaceholderHandler placeholderHandler) {
        // Accepts null values
        // noinspection DataFlowIssue
        Display.TextDisplay parent = new Display.TextDisplay(EntityType.TEXT_DISPLAY, null);
        return new NmsTextHologramLine(parent, placeholderHandler);
    }

    public NmsTextHologramLine(Display.TextDisplay parent, PlaceholderHandler placeholderHandler) {
        super(parent.getId(), placeholderHandler);
        this.parent = parent;
    }

    @Override
    public void sendSpawnPacket(Collection<Player> players) {
        Location loc = getLocation();

        ClientboundAddEntityPacket packet = new ClientboundAddEntityPacket(this.getEntityId(),
                this.getEntityUuid(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getPitch(),
                loc.getYaw(),
                EntityType.TEXT_DISPLAY,
                0,
                Vec3.ZERO,
                loc.getYaw()
        );
        sendPacket(players, packet);
    }

    @Override
    public void sendSetPassengerPacket(Collection<Player> players, int baseEntityId) {
        DummyEntity entity = new DummyEntity(baseEntityId);
        entity.passengers = ImmutableList.of(this.parent);

        ClientboundSetPassengersPacket packet = new ClientboundSetPassengersPacket(entity);
        sendPacket(players, packet);
    }

    @Override
    public void sendMetaPacket(Collection<Player> players, @Nullable BiConsumer<Player, TextReplacementConfig.Builder> textParser) {
        TextDisplayMeta meta = this.getMeta();
        Component text = meta.getText();

        if (meta.getLineWidth() >= 0) this.setNmsLineWidth(meta.getLineWidth());
        if (meta.getBackgroundColor() >= 0) this.setNmsBackgroundColor(meta.getBackgroundColor());
        if (meta.getTextOpacity() >= 0) this.setNmsTextOpacity(meta.getTextOpacity());
        this.setNmsShadow(meta.isShadow() ? meta.getShadowStrength() : 0f);
        this.setNmsSeeThrough(meta.isSeeThrough());
        this.setNmsUseDefaultBackground(meta.isUseDefaultBackground());
        this.setNmsAlignLeft(meta.isAlignLeft());
        this.setNmsAlignRight(meta.isAlignRight());

        if (meta.getInterpolationDelay() >= 0) this.setNmsInterpolationDelay(meta.getInterpolationDelay());
        if (meta.getTransformationInterpolationDuration() >= 0) this.setNmsTransformationInterpolationDuration(meta.getTransformationInterpolationDuration());
        if (meta.getPositionRotationInterpolationDuration() >= 0) this.setNmsPositionRotationInterpolationDuration(meta.getPositionRotationInterpolationDuration());
        if (meta.getTranslation() != null && meta.getLeftRotation() != null && meta.getScale() != null && meta.getRightRotation() != null) {
            this.setNmsTransformation(meta.getTranslation(), meta.getLeftRotation(), meta.getScale(), meta.getRightRotation());
        } else {
            HologramLib.logger().log(Level.WARNING, "WARNING: Some transformation values are null!", new Exception());
        }
        if (meta.getBillboardConstraints() != null) this.setNmsBillboardConstraints(meta.getBillboardConstraints());
        if (meta.getBrightnessOverride() >= 0) this.setNmsBrightnessOverride(meta.getBrightnessOverride());
        if (meta.getViewRange() >= 0) this.setNmsViewRange(meta.getViewRange());
        if (meta.getShadowRadius() >= 0) this.setNmsShadowRadius(meta.getShadowRadius());
        if (meta.getWidth() >= 0) this.setNmsWidth(meta.getWidth());
        if (meta.getHeight() >= 0) this.setNmsHeight(meta.getHeight());
        if (meta.getGlowColorOverride() >= 0) this.setNmsGlowColorOverride(meta.getGlowColorOverride());

        // Send the same text to all players
        if (textParser == null) {
            if (text != null) this.setNmsText(PaperAdventure.asVanilla(text));

            List<SynchedEntityData.DataValue<?>> dataValues = buildDataValues();
            if (dataValues == null) return;

            ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(this.getEntityId(), dataValues);
            sendPacket(players, packet);
        }
        // If textParser is provided, it will be used to parse the text for each player individually
        else {
            sendIndividual(players, text, textParser);
        }
    }

    private void sendIndividual(Collection<Player> players, Component text, BiConsumer<Player, TextReplacementConfig.Builder> textParser) {
        for (Player player : players) {
            if (!player.isOnline()) {
                HologramLib.logger().warning("Player {NAME} is not online, cannot send packet".replace("{NAME}", player.getName()));
                continue;
            }

            Component parsedText = text.replaceText(b -> textParser.accept(player, b));
            this.setNmsText(PaperAdventure.asVanilla(parsedText));

            List<SynchedEntityData.DataValue<?>> dataValues = buildDataValues();
            if (dataValues == null) return;

            ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(this.getEntityId(), dataValues);
            sendPacket(player, packet);
        }
    }

    private @Nullable List<SynchedEntityData.DataValue<?>> buildDataValues() {
        List<SynchedEntityData.DataValue<?>> dataValues = this.parent.getEntityData().getNonDefaultValues();
        if (dataValues == null || dataValues.isEmpty()) {
            HologramLib.logger().warning("No data values to send");
            return null;
        }
        return dataValues;
    }

    @Override
    public void sendKillPacket(Collection<Player> players) {
        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(
                IntList.of(this.getEntityId())
        );
        sendPacket(players, packet);
    }

    @Override
    public void sendTeleportPacket(Collection<Player> players, Location location) {
        Vec3 prePos = this.parent.position();
        Vec3 newPos = new Vec3(location.getX(), location.getY(), location.getZ());
        Vec3 delta = newPos.subtract(prePos);
        PositionMoveRotation posMovRot = new PositionMoveRotation(newPos, delta, this.parent.getYRot(), this.parent.getXRot());

        this.parent.setPos(newPos);

        ClientboundTeleportEntityPacket packet = new ClientboundTeleportEntityPacket(this.getEntityId(), posMovRot, Relative.ALL, this.parent.onGround());
        sendPacket(players, packet);
    }

    private static void sendPacket(Collection<Player> players, Packet<ClientGamePacketListener> packet) {
        for (Player player : players) {
            sendPacket(player, packet);
        }
    }

    private static void sendPacket(Player player, Packet<ClientGamePacketListener> packet) {
        if (!player.isOnline()) {
            HologramLib.logger().warning("Player {NAME} is not online, cannot send packet".replace("{NAME}", player.getName()));
            return;
        }

        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    private void setNmsText(net.minecraft.network.chat.Component text) {
        this.parent.setText(text);
    }

    private void setNmsLineWidth(int lineWidth) {
        this.parent.getEntityData().set(Display.TextDisplay.DATA_LINE_WIDTH_ID, lineWidth);
    }

    private void setNmsBackgroundColor(int backgroundColor) {
        this.parent.getEntityData().set(Display.TextDisplay.DATA_BACKGROUND_COLOR_ID, backgroundColor);
    }

    private void setNmsTextOpacity(byte textOpacity) {
        this.parent.setTextOpacity(textOpacity);
    }

    private void setNmsShadow(float strength) {
        // noinspection UnnecessaryLocalVariable
        Display requiredWorkaround = this.parent;
        requiredWorkaround.setShadowStrength(strength);
    }

    private void setNmsSeeThrough(boolean seeThrough) {
        this.parent.setTextOpacity(seeThrough ? 0 : (byte) 255);
    }

    private void setNmsUseDefaultBackground(boolean useDefaultBackground) {
        final int defaultBgColor = 1073741824;
        this.parent.getEntityData().set(Display.TextDisplay.DATA_BACKGROUND_COLOR_ID, useDefaultBackground ? defaultBgColor : 0x00000000);
    }

    private void setNmsAlignLeft(boolean alignLeft) {
        byte flags = this.parent.getFlags();

        if (alignLeft) flags |= Display.TextDisplay.FLAG_ALIGN_LEFT;
        else flags &= ~Display.TextDisplay.FLAG_ALIGN_LEFT;

        this.parent.setFlags(flags);
    }

    private void setNmsAlignRight(boolean alignRight) {
        byte flags = this.parent.getFlags();

        if (alignRight) flags |= Display.TextDisplay.FLAG_ALIGN_RIGHT;
        else flags &= ~Display.TextDisplay.FLAG_ALIGN_RIGHT;

        this.parent.setFlags(flags);
    }

    private void setNmsInterpolationDelay(int interpolationDelay) {
        // noinspection UnnecessaryLocalVariable
        Display requiredWorkaround = this.parent;
        requiredWorkaround.setTransformationInterpolationDelay(interpolationDelay);
    }

    private void setNmsTransformationInterpolationDuration(int transformationInterpolationDuration) {
        // noinspection UnnecessaryLocalVariable
        Display requiredWorkaround = this.parent;
        requiredWorkaround.setTransformationInterpolationDuration(transformationInterpolationDuration);
    }

    private void setNmsPositionRotationInterpolationDuration(int positionRotationInterpolationDuration) {
        this.parent.getEntityData().set(Display.TextDisplay.DATA_POS_ROT_INTERPOLATION_DURATION_ID, positionRotationInterpolationDuration);
    }

    private void setNmsTransformation(Vector3F translation, Quaternion4F leftRotation, Vector3F scale, Quaternion4F rightRotation) {
        Vector3f mojTranslation = new Vector3f(translation.getX(), translation.getY(), translation.getZ());
        Quaternionf mojLeftRotation = new Quaternionf(leftRotation.getX(), leftRotation.getY(), leftRotation.getZ(), leftRotation.getW());
        Vector3f mojScale = new Vector3f(scale.getX(), scale.getY(), scale.getZ());
        Quaternionf mojRightRotation = new Quaternionf(rightRotation.getX(), rightRotation.getY(), rightRotation.getZ(), rightRotation.getW());

        Transformation transformation = new Transformation(mojTranslation, mojLeftRotation, mojScale, mojRightRotation);

        // noinspection UnnecessaryLocalVariable
        Display requiredWorkaround = this.parent;
        requiredWorkaround.setTransformation(transformation);
    }

    private void setNmsBillboardConstraints(BillboardConstraints billboardConstraints) {
        // noinspection UnnecessaryLocalVariable
        Display requiredWorkaround = this.parent;
        requiredWorkaround.setBillboardConstraints(Display.BillboardConstraints.valueOf(billboardConstraints.name()));
    }

    private void setNmsBrightnessOverride(int brightnessOverride) {
        // noinspection UnnecessaryLocalVariable
        Display requiredWorkaround = this.parent;
        requiredWorkaround.setBrightnessOverride(new Brightness(brightnessOverride, brightnessOverride));
    }

    private void setNmsViewRange(float viewRange) {
        // noinspection UnnecessaryLocalVariable
        Display requiredWorkaround = this.parent;
        requiredWorkaround.setViewRange(viewRange);
    }

    private void setNmsShadowRadius(float shadowRadius) {
        // noinspection UnnecessaryLocalVariable
        Display requiredWorkaround = this.parent;
        requiredWorkaround.setShadowRadius(shadowRadius);
    }

    private void setNmsWidth(float width) {
        // noinspection UnnecessaryLocalVariable
        Display requiredWorkaround = this.parent;
        requiredWorkaround.setWidth(width);
    }

    private void setNmsHeight(float height) {
        // noinspection UnnecessaryLocalVariable
        Display requiredWorkaround = this.parent;
        requiredWorkaround.setHeight(height);
    }

    private void setNmsGlowColorOverride(int glowColorOverride) {
        // noinspection UnnecessaryLocalVariable
        Display requiredWorkaround = this.parent;
        requiredWorkaround.setGlowColorOverride(glowColorOverride);
    }

}
