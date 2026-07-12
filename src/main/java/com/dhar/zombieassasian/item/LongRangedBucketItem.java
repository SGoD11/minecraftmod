package com.dhar.zombieassasian.item;

import com.dhar.zombieassasian.register.ModRegistries;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;

/**
 * FEATURE 11 — Long-Ranged Bucket (empty state)
 * -----------------------------------------------
 * This class deliberately does NOT extend vanilla BucketItem. Reason: when
 * ANY custom BucketItem subclass picks up a vanilla water source, vanilla's
 * own LiquidBlock#pickupBlock hard-codes the returned item as a plain
 * vanilla Water Bucket — it ignores which bucket item was actually used.
 * That's what caused the "turns into a normal water bucket" bug.
 *
 * This class reimplements just the pickup logic manually, forcing the
 * result to be LONG_RANGED_BUCKET_FILLED (our own item) instead of
 * whatever vanilla hands back. Placement is handled by the filled item's
 * class — see LongRangedBucketFilledItem.
 */
public class LongRangedBucketItem extends Item implements ILongRangedTool {

    public LongRangedBucketItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);

        HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemInHand);
        }

        BlockHitResult blockHit = (BlockHitResult) hitResult;
        BlockPos pos = blockHit.getBlockPos();
        if (!level.mayInteract(player, pos)) {
            return InteractionResultHolder.pass(itemInHand);
        }

        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof net.minecraft.world.level.block.BucketPickup bucketPickup)) {
            return InteractionResultHolder.pass(itemInHand);
        }

        // This call empties the source block. It ALSO returns a plain
        // vanilla Water Bucket ItemStack — we intentionally ignore that
        // returned stack's item and only care that it wasn't empty
        // (meaning the pickup succeeded).
        ItemStack vanillaResult = bucketPickup.pickupBlock(level, pos, state);
        if (vanillaResult.isEmpty()) {
            return InteractionResultHolder.fail(itemInHand);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        bucketPickup.getPickupSound(state).ifPresent(sound -> player.playSound(sound, 1.0F, 1.0F));
        level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);

        ItemStack filledBucket = new ItemStack(ModRegistries.LONG_RANGED_BUCKET_FILLED.get());
        ItemStack resultStack = ItemUtils.createFilledResult(itemInHand, player, filledBucket);
        return InteractionResultHolder.sidedSuccess(resultStack, level.isClientSide());
    }
}