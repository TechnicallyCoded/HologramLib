package com.tcoded.hologramlib.hologram;

import com.tcoded.hologramlib.PlaceholderHandler;
import com.tcoded.hologramlib.hologram.meta.ItemDisplayMeta;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.BiConsumer;

public abstract class ItemHologramLine extends HologramLine {

    private final ItemDisplayMeta meta;

    public ItemHologramLine(int entityId, PlaceholderHandler placeholderHandler) {
        super(entityId, placeholderHandler, null);
        this.meta = new ItemDisplayMeta();
    }

    public ItemDisplayMeta getMeta() {
        return this.meta;
    }

    public ItemStack getItemStack() {
        return this.meta.getItemStack();
    }

    public void setItemStack(ItemStack itemStack) {
        this.meta.setItemStack(itemStack);
    }

    public void setItemStack(Material material) {
        this.meta.setItemStack(new ItemStack(material));
    }

    @Override
    protected abstract void sendMetaPacket(Collection<Player> players, @Nullable BiConsumer<Player, TextReplacementConfig.Builder> textParser);

}
