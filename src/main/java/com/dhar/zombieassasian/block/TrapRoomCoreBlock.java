package com.dhar.zombieassasian.block;

import com.dhar.zombieassasian.blockentity.TrapRoomCoreBlockEntity;
import com.dhar.zombieassasian.register.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class TrapRoomCoreBlock extends Block implements EntityBlock {

    public TrapRoomCoreBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TrapRoomCoreBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModRegistries.TRAP_ROOM_CORE_ENTITY.get()) {
            return null; // wrong block entity type — not ours, don't tick it
        }
        // Manual ticker instead of relying on BaseEntityBlock's
        // createTickerHelper (that helper only exists on BaseEntityBlock,
        // and this class extends plain Block, so it isn't inherited here).
        return (lvl, pos, st, blockEntity) ->
                TrapRoomCoreBlockEntity.tick(lvl, pos, st, (TrapRoomCoreBlockEntity) blockEntity);
    }
}