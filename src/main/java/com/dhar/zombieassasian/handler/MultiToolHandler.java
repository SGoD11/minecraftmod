package com.dhar.zombieassasian.handler;

import com.dhar.zombieassasian.item.MultiToolItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * FEATURE 12 — Multi-Tool
 * -------------------------
 * BlockEvent.BreakEvent fires for the ONE block the player directly broke;
 * vanilla still handles that single block normally. This handler just adds
 * the other 124 blocks in the surrounding 5x5x5 cuboid (radius 2 in every
 * direction from the block actually broken).
 *
 * Sneaking (shift) bypasses the AoE and mines only the single block —
 * standard convention for AoE tools, so players aren't forced into it.
 */
public class MultiToolHandler {

    private static final int RADIUS = 2; // 2 blocks each side of center = 5 total per axis

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.isShiftKeyDown()) {
            return;
        }

        ItemStack tool = player.getMainHandItem();
        if (!(tool.getItem() instanceof MultiToolItem)) {
            return;
        }

        Level level = player.level();
        if (level.isClientSide) {
            return;
        }

        BlockPos center = event.getPos();

        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue; // the original block — vanilla already handles this one
                    }
                    BlockPos pos = center.offset(dx, dy, dz).immutable();
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) {
                        continue;
                    }
                    if (state.getDestroySpeed(level, pos) < 0) {
                        continue; // unbreakable (bedrock, barrier, etc.)
                    }
                    level.destroyBlock(pos, true, player);
                }
            }
        }
    }
}
