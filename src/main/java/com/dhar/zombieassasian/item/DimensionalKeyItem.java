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
 * A full custom portal-frame system (like the Nether's obsidian+flint&steel
 * frame detection) is a significantly larger, higher-risk undertaking on
 * its own. Given the priority on correctness here, this item is a
 * deliberately simple, low-risk way to enter/exit the dimension instead:
 * right-click to toggle between the Overworld and the Diamond Realm.
 *
 * DIAMOND_REALM is built the exact same way vanilla itself builds
 * Level.OVERWORLD / Level.NETHER (ResourceKey.create with Registries.DIMENSION)
 * — copying that proven pattern instead of inventing a new one.
 *
 * Landing spot (0, 57, 0) is calculated from the dimension's flat layers:
 * 1 bedrock + 50 diamond_block + 5 netherite_block = top of the terrain at
 * Y=56, so Y=57 stands the player on solid ground right on the netherite
 * layer.
 */
public class DimensionalKeyItem extends Item {

    private static final ResourceKey<Level> DIAMOND_REALM = ResourceKey.create(
            Registries.DIMENSION, new ResourceLocation("zombieassasian", "diamond_realm"));

    private static final BlockPos LANDING_POS = new BlockPos(0, 57, 0);

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
