package com.tcoded.hologramlib.hologram;

import com.tcoded.hologramlib.PlaceholderHandler;
import com.tcoded.hologramlib.hologram.meta.TextDisplayMeta;
import org.bukkit.entity.TextDisplay;

public abstract class TextHologramLine extends HologramLine {

    private final TextDisplayMeta meta;

    public TextHologramLine(int entityId, PlaceholderHandler placeholderHandler, PacketPreprocessor packetPreprocessor) {
        super(entityId, placeholderHandler, packetPreprocessor);
        this.meta = new TextDisplayMeta();
    }

    public TextDisplayMeta getMeta() {
        return this.meta;
    }

    private void setAlignment(TextDisplay.TextAlignment alignment) {
        switch (alignment) {
            case LEFT -> getMeta().setAlignLeft(true);
            case RIGHT -> getMeta().setAlignRight(true);
        }
    }

}
