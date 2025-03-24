//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.tcoded.hologramlib.utils;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import java.util.Objects;

public class Vector3F {
    
    public final float x;
    public final float y;
    public final float z;

    public Vector3F() {
        this.x = 0.0F;
        this.y = 0.0F;
        this.z = 0.0F;
    }

    public Vector3F(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3F(float[] array) {
        if (array.length > 0) {
            this.x = array[0];
            if (array.length > 1) {
                this.y = array[1];
                if (array.length > 2) {
                    this.z = array[2];
                } else {
                    this.z = 0.0F;
                }

            } else {
                this.y = 0.0F;
                this.z = 0.0F;
            }
        } else {
            this.x = 0.0F;
            this.y = 0.0F;
            this.z = 0.0F;
        }
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Vector3F) {
            Vector3F vec = (Vector3F)obj;
            return this.x == vec.x && this.y == vec.y && this.z == vec.z;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.x, this.y, this.z});
    }

    public Vector3F add(float x, float y, float z) {
        return new Vector3F(this.x + x, this.y + y, this.z + z);
    }

    public Vector3F add(Vector3F other) {
        return this.add(other.x, other.y, other.z);
    }

    public Vector3F offset(BlockFace face) {
        return this.add((float)face.getModX(), (float)face.getModY(), (float)face.getModZ());
    }

    public Vector3F subtract(float x, float y, float z) {
        return new Vector3F(this.x - x, this.y - y, this.z - z);
    }

    public Vector3F subtract(Vector3F other) {
        return this.subtract(other.x, other.y, other.z);
    }

    public Vector3F multiply(float x, float y, float z) {
        return new Vector3F(this.x * x, this.y * y, this.z * z);
    }

    public Vector3F multiply(Vector3F other) {
        return this.multiply(other.x, other.y, other.z);
    }

    public Vector3F multiply(float value) {
        return this.multiply(value, value, value);
    }

    public Vector3F crossProduct(Vector3F other) {
        float newX = this.y * other.z - other.y * this.z;
        float newY = this.z * other.x - other.z * this.x;
        float newZ = this.x * other.y - other.x * this.y;
        return new Vector3F(newX, newY, newZ);
    }

    public float dot(Vector3F other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public Vector3F with(Float x, Float y, Float z) {
        return new Vector3F(x == null ? this.x : x, y == null ? this.y : y, z == null ? this.z : z);
    }

    public Vector3F withX(float x) {
        return new Vector3F(x, this.y, this.z);
    }

    public Vector3F withY(float y) {
        return new Vector3F(this.x, y, this.z);
    }

    public Vector3F withZ(float z) {
        return new Vector3F(this.x, this.y, z);
    }

    public String toString() {
        return "X: " + this.x + ", Y: " + this.y + ", Z: " + this.z;
    }

    public static Vector3F zero() {
        return new Vector3F();
    }
    
}
