package com.dhar.zombieassasian.blockentity;

import com.dhar.zombieassasian.register.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * FEATURE 8 — Trap Room
 * -----------------------
 * Requirement: whenever a player occupies any position inside a 10x10 room,
 * an anvil falls directly above them; this must never happen outside the
 * designated room.
 *
 * Design: placing this block defines the room. Its footprint is a strict
 * 10x10 XZ square centered on this block (5 blocks each direction), and 5
 * blocks tall starting at this block's Y level. A player is only ever
 * checked against THIS exact bounding box — nothing outside it can trigger
 * an anvil, satisfying "must never occur outside the designated room."
 *
 * A per-player cooldown (COOLDOWN_TICKS) stops it from raining anvils every
 * single tick while someone just stands there — one anvil, then a breather,
 * as long as they're still inside.
 */
public class TrapRoomCoreBlockEntity extends BlockEntity {

    private static final int ROOM_HALF_WIDTH = 5; // 5 + 5 = 10 blocks wide on each horizontal axis
    private static final int ROOM_HEIGHT = 5;
    private static final int COOLDOWN_TICKS = 100; // 5 seconds between anvil drops per player
    private static final int ANVIL_SPAWN_HEIGHT_ABOVE_PLAYER = 6;

    private final Map<UUID, Integer> playerCooldowns = new HashMap<>();

    public TrapRoomCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.TRAP_ROOM_CORE_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TrapRoomCoreBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }

        AABB roomBounds = new AABB(
                pos.getX() - ROOM_HALF_WIDTH, pos.getY(), pos.getZ() - ROOM_HALF_WIDTH,
                pos.getX() + ROOM_HALF_WIDTH + 1, pos.getY() + ROOM_HEIGHT, pos.getZ() + ROOM_HALF_WIDTH + 1
        );

        List<Player> playersInRoom = level.getEntitiesOfClass(Player.class, roomBounds);

        // Tick down every active cooldown by 1, dropping any that hit zero.
        blockEntity.playerCooldowns.replaceAll((uuid, ticksLeft) -> ticksLeft - 1);
        blockEntity.playerCooldowns.entrySet().removeIf(entry -> entry.getValue() <= 0);

        for (Player player : playersInRoom) {
            UUID id = player.getUUID();
            if (blockEntity.playerCooldowns.containsKey(id)) {
                continue; // still on cooldown from a recent drop
            }
            spawnFallingAnvil(level, player);
            blockEntity.playerCooldowns.put(id, COOLDOWN_TICKS);
        }
    }

    private static void spawnFallingAnvil(Level level, Player player) {
        BlockPos spawnPos = player.blockPosition().above(ANVIL_SPAWN_HEIGHT_ABOVE_PLAYER);
        FallingBlockEntity.fall(level, spawnPos, Blocks.ANVIL.defaultBlockState());
    }
}
