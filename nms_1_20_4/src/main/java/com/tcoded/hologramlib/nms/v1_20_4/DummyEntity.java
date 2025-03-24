package com.tcoded.hologramlib.nms.v1_20_4;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class DummyEntity extends Entity {

    public DummyEntity(int id) {
        // Null is allowed
        // noinspection DataFlowIssue
        super(EntityType.MARKER, null);
        this.setId(id);
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {

    }

}
