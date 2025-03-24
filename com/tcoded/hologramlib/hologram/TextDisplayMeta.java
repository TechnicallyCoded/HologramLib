package com.tcoded.hologramlib.hologram;

import net.kyori.adventure.text.Component;

public class TextDisplayMeta extends DisplayMeta {

    private Component text;
    private int lineWidth;
    private int backgroundColor;
    private byte textOpacity;
    private boolean shadow;
    private boolean seeThrough;
    private boolean useDefaultBackground;
    private boolean alignLeft;
    private boolean alignRight;

    public TextDisplayMeta() {
        this.text = Component.empty();
        this.lineWidth = 0;
        this.backgroundColor = 0;
        this.textOpacity = (byte) 255;
        this.shadow = false;
        this.seeThrough = false;
        this.useDefaultBackground = false;
        this.alignLeft = false;
        this.alignRight = false;
    }

    public Component getText() {
        return text;
    }

    public void setText(Component component) {
        this.text = component;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int value) {
        this.lineWidth = value;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int value) {
        this.backgroundColor = value;
    }

    public byte getTextOpacity() {
        return textOpacity;
    }

    public void setTextOpacity(byte value) {
        this.textOpacity = value;
    }

    public boolean isShadow() {
        return shadow;
    }

    public void setShadow(boolean value) {
        this.shadow = value;
    }

    public boolean isSeeThrough() {
        return seeThrough;
    }

    public void setSeeThrough(boolean value) {
        this.seeThrough = value;
    }

    public boolean isUseDefaultBackground() {
        return useDefaultBackground;
    }

    public void setUseDefaultBackground(boolean value) {
        this.useDefaultBackground = value;
    }

    public boolean isAlignLeft() {
        return alignLeft;
    }

    public void setAlignLeft(boolean value) {
        this.alignLeft = value;
    }

    public boolean isAlignRight() {
        return alignRight;
    }

    public void setAlignRight(boolean value) {
        this.alignRight = value;
    }

}