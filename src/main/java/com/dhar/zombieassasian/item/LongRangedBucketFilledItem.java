package com.dhar.zombieassasian.item;

import com.dhar.zombieassasian.register.ModRegistries;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * FEATURE 11 — Long-Ranged Bucket (filled/holding-water state)
 * ----------------------------------------------------------------
 * IMPORTANT: vanilla BucketItem#getEmptySuccessItem is a STATIC method.
 * Static methods can't be polymorphically overridden in Java — even if a
 * subclass declares one with the same signature, code inside BucketItem's
 * own use() method still calls BucketItem's own static version, not ours
 * (static calls resolve at compile time based on the declaring class, not
 * the runtime type). So instead of trying to override that static method,
 * this class overrides use() entirely, copying vanilla's own placement
 * logic (verified against BucketItem's real 1.20.1 source) but hardcoding
 * the result to be our own empty Long-Ranged Bucket at the very end.
 */
public class LongRangedBucketFilledItem extends BucketItem implements ILongRangedTool {

    public LongRangedBucketFilledItem(Properties properties) {
        super(Fluids.WATER, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);

        HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemInHand);
        }

        BlockHitResult blockHit = (BlockHitResult) hitResult;
        BlockPos targetPos = blockHit.getBlockPos();
        Direction face = blockHit.getDirection();
        BlockPos adjacentPos = targetPos.relative(face);

        if (!level.mayInteract(player, targetPos) || !player.mayUseItemAt(adjacentPos, face, itemInHand)) {
            return InteractionResultHolder.fail(itemInHand);
        }

        BlockState targetState = level.getBlockState(targetPos);
        // If the targeted block can directly accept water poured into it
        // (e.g. a cauldron), place there; otherwise place in the adjacent
        // empty space, exactly matching vanilla's own logic.
        BlockPos placePos = targetState.getBlock() instanceof LiquidBlockContainer ? targetPos : adjacentPos;

        if (!this.emptyContents(player, level, placePos, blockHit)) {
            return InteractionResultHolder.fail(itemInHand);
        }

        this.checkExtraContent(player, level, itemInHand, placePos);
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, placePos, itemInHand);
        }
        player.awardStat(Stats.ITEM_USED.get(this));

        ItemStack emptyBucket = new ItemStack(ModRegistries.LONG_RANGED_BUCKET.get());
        ItemStack resultStack = ItemUtils.createFilledResult(itemInHand, player, emptyBucket);
        return InteractionResultHolder.sidedSuccess(resultStack, level.isClientSide());
    }
}