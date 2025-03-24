package com.tcoded.hologramlib.types.math;

public class Quaternion4F {
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
