package com.tcoded.hologramlib.hologram.meta;

import com.tcoded.hologramlib.types.math.Quaternion4F;
import com.tcoded.hologramlib.types.math.Vector3F;

public class DisplayMeta {
    
    private int interpolationDelay;
    private int transformationInterpolationDuration;
    private int positionRotationInterpolationDuration;
    private Vector3F translation;
    private Vector3F scale;
    private Quaternion4F leftRotation;
    private Quaternion4F rightRotation;
    private BillboardConstraints billboardConstraints;
    private int brightnessOverride;
    private float viewRange;
    private float shadowRadius;
    private float shadowStrength;
    private float width;
    private float height;
    private int glowColorOverride;

    public DisplayMeta() {
        this.interpolationDelay = 0;
        this.transformationInterpolationDuration = 10;
        this.positionRotationInterpolationDuration = 10;
        this.translation = Vector3F.zero();
        this.scale = new Vector3F(1.0F, 1.0F, 1.0F);
        this.leftRotation = Quaternion4F.defaults();
        this.rightRotation = Quaternion4F.defaults();
        this.billboardConstraints = BillboardConstraints.CENTER;
        this.brightnessOverride = -1;
        this.viewRange = 1.0F;
        this.shadowRadius = 0.0F;
        this.shadowStrength = 1.0F;
        this.width = 0.0F;
        this.height = 0.0F;
        this.glowColorOverride = -1;
    }

    public int getInterpolationDelay() {
        return interpolationDelay;
    }

    public void setInterpolationDelay(int value) {
        this.interpolationDelay = value;
    }

    public int getTransformationInterpolationDuration() {
        return transformationInterpolationDuration;
    }

    public void setTransformationInterpolationDuration(int value) {
        this.transformationInterpolationDuration = value;
    }

    public int getPositionRotationInterpolationDuration() {
        return positionRotationInterpolationDuration;
    }

    public void setPositionRotationInterpolationDuration(int value) {
        this.positionRotationInterpolationDuration = value;
    }

    public Vector3F getTranslation() {
        return translation;
    }

    public void setTranslation(Vector3F value) {
        this.translation = value;
    }

    public Vector3F getScale() {
        return scale;
    }

    public void setScale(Vector3F value) {
        this.scale = value;
    }

    public Quaternion4F getLeftRotation() {
        return leftRotation;
    }

    public void setLeftRotation(Quaternion4F value) {
        this.leftRotation = value;
    }

    public Quaternion4F getRightRotation() {
        return rightRotation;
    }

    public void setRightRotation(Quaternion4F value) {
        this.rightRotation = value;
    }

    public BillboardConstraints getBillboardConstraints() {
        return billboardConstraints;
    }

    public void setBillboardConstraints(BillboardConstraints value) {
        this.billboardConstraints = value;
    }

    public int getBrightnessOverride() {
        return brightnessOverride;
    }

    public void setBrightnessOverride(int value) {
        this.brightnessOverride = value;
    }

    public float getViewRange() {
        return viewRange;
    }

    public void setViewRange(float value) {
        this.viewRange = value;
    }

    public float getShadowRadius() {
        return shadowRadius;
    }

    public void setShadowRadius(float value) {
        this.shadowRadius = value;
    }

    public float getShadowStrength() {
        return shadowStrength;
    }

    public void setShadowStrength(float value) {
        this.shadowStrength = value;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float value) {
        this.width = value;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float value) {
        this.height = value;
    }

    public int getGlowColorOverride() {
        return glowColorOverride;
    }

    public void setGlowColorOverride(int value) {
        this.glowColorOverride = value;
    }

}
