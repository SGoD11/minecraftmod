package com.dhar.zombieassasian.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Set;

/**
 * FEATURE 10 — New Dimension (entry point)
 * -------------------------------------------
 * Right-click to toggle between the Overworld and the Diamond Realm.
 *
 * The Diamond Realm uses a noise generator (same shape as the Overworld),
 * so terrain height varies. We spawn the player at X=0, Z=0 high enough
 * (Y=100) that they always land on top of the terrain, then fall naturally.
 * The surface rules replace stone/dirt/grass with diamond_block/netherite_block,
 * giving full Overworld-shaped terrain in diamond and netherite textures.
 *
 * DIAMOND_REALM is built the exact same way vanilla builds Level.OVERWORLD /
 * Level.NETHER (ResourceKey.create with Registries.DIMENSION).
 */
public class DimensionalKeyItem extends Item {

    private static final ResourceKey<Level> DIAMOND_REALM = ResourceKey.create(
            Registries.DIMENSION, new ResourceLocation("zombieassasian", "diamond_realm"));

    // Y=100 is safely above any noise-terrain surface (~64-80).
    // Player will fall onto the netherite-block surface on entry.
    private static final BlockPos LANDING_POS = new BlockPos(0, 100, 0);

    public DimensionalKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(itemInHand);
        }

        ServerLevel targetLevel;
        BlockPos targetPos;

        if (level.dimension().equals(DIAMOND_REALM)) {
            // Currently in the Diamond Realm — go back to the Overworld.
            targetLevel = serverPlayer.server.getLevel(Level.OVERWORLD);
            targetPos = targetLevel != null ? targetLevel.getSharedSpawnPos() : player.blockPosition();
        } else {
            targetLevel = serverPlayer.server.getLevel(DIAMOND_REALM);
            targetPos = LANDING_POS;
        }

        if (targetLevel == null) {
            return InteractionResultHolder.fail(itemInHand); // dimension not found — shouldn't happen if data files are in place
        }

        serverPlayer.teleportTo(targetLevel,
                targetPos.getX() + 0.5D,
                targetPos.getY(),
                targetPos.getZ() + 0.5D,
                Set.of(),
                player.getYRot(),
                player.getXRot());

        return InteractionResultHolder.sidedSuccess(itemInHand, false);
    }
}
