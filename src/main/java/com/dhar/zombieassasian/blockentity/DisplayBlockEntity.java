package com.dhar.zombieassasian.blockentity;

import com.dhar.zombieassasian.register.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * FEATURE 6 — Interactive Display
 * ---------------------------------
 * Holds exactly one ItemStack. All the actual "place on right-click /
 * retrieve on right-click again" logic lives in DisplayBlock.use() — this
 * class is just storage + save/load + the client sync packet that keeps
 * the floating item visible for everyone nearby.
 *
 * Method signatures here target Minecraft 1.20.1's BlockEntity API
 * specifically (no HolderLookup.Provider parameter — that was added in a
 * later version and doesn't exist here).
 */
public class DisplayBlockEntity extends BlockEntity {

    private ItemStack displayedItem = ItemStack.EMPTY;

    public DisplayBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.DISPLAY_BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStack getDisplayedItem() {
        return displayedItem;
    }

    public boolean hasDisplayedItem() {
        return !displayedItem.isEmpty();
    }

    public void setDisplayedItem(ItemStack stack) {
        this.displayedItem = stack;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("DisplayedItem", displayedItem.save(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("DisplayedItem")) {
            displayedItem = ItemStack.of(tag.getCompound("DisplayedItem"));
        }
    }

    // Standard vanilla pattern for syncing a block entity's extra data to
    // nearby clients (used by countless vanilla/modded block entities):
    // getUpdateTag() builds the sync payload, handleUpdateTag() reads it
    // back in on the client.
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        load(tag);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}