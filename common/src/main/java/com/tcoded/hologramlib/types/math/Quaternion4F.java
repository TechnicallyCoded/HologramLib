package com.tcoded.hologramlib.types.math;

public class Quaternion4F {

    // Magic from Wikipedia
    // Note: Input angles are in DEGREES, converted to radians internally
    public static Quaternion4F fromEuler(float roll, float yaw, float pitch) {
        // Convert degrees to radians
        float rollRad = (float) Math.toRadians(roll);
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        float cosRoll = (float) Math.cos(rollRad * 0.5);
        float sinRoll = (float) Math.sin(rollRad * 0.5);
        float cosYaw = (float) Math.cos(yawRad * 0.5);
        float sinYaw = (float) Math.sin(yawRad * 0.5);
        float cosPitch = (float) Math.cos(pitchRad * 0.5);
        float sinPitch = (float) Math.sin(pitchRad * 0.5);

        return new Quaternion4F(
                sinRoll * cosPitch * cosYaw - cosRoll * sinPitch * sinYaw,
                cosRoll * cosPitch * sinYaw - sinRoll * sinPitch * cosYaw,
                cosRoll * sinPitch * cosYaw + sinRoll * cosPitch * sinYaw,
                cosRoll * cosPitch * cosYaw + sinRoll * sinPitch * sinYaw
        );
    }

    private float x;
    private float y;
    private float z;
    private float w;

    public Quaternion4F(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public static Quaternion4F defaults() {
        return new Quaternion4F(0.0F, 0.0F, 0.0F, 1.0F);
    }

    public float getX() {
        return this.x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return this.y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return this.z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getW() {
        return this.w;
    }

    public void setW(float w) {
        this.w = w;
    }

}
