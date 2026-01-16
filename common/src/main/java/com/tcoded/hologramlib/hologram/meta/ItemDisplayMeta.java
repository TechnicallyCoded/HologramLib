package com.tcoded.hologramlib.hologram.meta;

import org.bukkit.inventory.ItemStack;

public class ItemDisplayMeta extends DisplayMeta {

    private ItemStack itemStack;

    public ItemDisplayMeta() {
        super();
        this.itemStack = null;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

}
