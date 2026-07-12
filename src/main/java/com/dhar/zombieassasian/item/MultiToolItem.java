package com.dhar.zombieassasian.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * FEATURE 12 — Multi-Tool
 * -------------------------
 * The AoE-mining behavior itself lives in handler/MultiToolHandler.java
 * (listens for BlockEvent.BreakEvent) — this class only exists so that
 * handler can identify "is the player holding THIS item" via instanceof,
 * plus gives it a flat, usable mining speed (brief didn't specify tool
 * tier, so treating it like a generic all-purpose diamond-level tool).
 */
public class MultiToolItem extends Item {
    public MultiToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 8.0F;
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        return true;
    }
}
