package com.dhar.zombieassasian.block;

import com.dhar.zombieassasian.blockentity.DisplayBlockEntity;
import com.dhar.zombieassasian.register.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

import javax.annotation.Nullable;

/**
 * FEATURE 6 — Interactive Display
 * ---------------------------------
 * Right-click with an item in hand while the display is empty -> places it.
 * Right-click again (display currently holding something) -> gives it back.
 */
public class DisplayBlock extends Block implements EntityBlock {

    // A short pedestal shape (not a full cube) so the displayed item floats
    // visibly above the block's top surface instead of being buried in it.
    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 8.0, 14.0);

    public DisplayBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                  InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof DisplayBlockEntity displayEntity)) {
            return InteractionResult.PASS;
        }

        if (displayEntity.hasDisplayedItem()) {
            // Retrieve: give the stored item back to the player.
            ItemStack stored = displayEntity.getDisplayedItem();
            displayEntity.setDisplayedItem(ItemStack.EMPTY);
            if (!player.getInventory().add(stored)) {
                player.drop(stored, false);
            }
            return InteractionResult.CONSUME;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.isEmpty()) {
            return InteractionResult.PASS; // nothing to place, nothing to retrieve
        }

        // Place: take exactly one item from the player's hand.
        ItemStack toDisplay = heldItem.copyWithCount(1);
        heldItem.shrink(1);
        displayEntity.setDisplayedItem(toDisplay);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof DisplayBlockEntity displayEntity
                    && displayEntity.hasDisplayedItem()) {
                // Don't delete an item into the void if the block is broken.
                net.minecraft.world.Containers.dropItemStack(level,
                        pos.getX(), pos.getY(), pos.getZ(), displayEntity.getDisplayedItem());
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL; // still uses a normal block model + our renderer draws the floating item on top
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DisplayBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null; // no per-tick logic needed — item is static once placed
    }
}
