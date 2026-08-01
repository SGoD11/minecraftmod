package com.dhar.zombieassasian.blockentity;

import com.dhar.zombieassasian.register.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
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
 * FEATURE 8 — Trap Room Mechanism (10x10 Structure Required)
 * -------------------------------------------------------------
 * Requirements:
 * 1. The trap ONLY activates when ALL 36 blocks of the 10x10 boundary are placed.
 * 2. Anvils MUST ONLY fall inside the 10x10 inner room boundary.
 * 3. Any living entity (players, mobs, creatures) inside the trap room receives damage.
 */
public class TrapRoomCoreBlockEntity extends BlockEntity {

    private static final int COOLDOWN_TICKS = 40; // 2 seconds between anvil drops per entity
    private static final int EXACT_PERIMETER_BLOCKS = 36; // Strict: ALL 36 boundary blocks of the 10x10 square are required
    private static final float ANVIL_TRAP_DAMAGE = 6.0F; // 3 hearts damage per drop

    private final Map<UUID, Integer> entityCooldowns = new HashMap<>();

    public TrapRoomCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.TRAP_ROOM_CORE_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TrapRoomCoreBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }

        // Search for a 100% complete 10x10 perimeter structure containing this block
        RoomData roomData = findComplete10x10Room(level, pos);
        if (roomData == null) {
            // Incomplete boundary or single block — DO NOTHING!
            return;
        }

        // Decrement active cooldowns
        blockEntity.entityCooldowns.replaceAll((uuid, ticksLeft) -> ticksLeft - 1);
        blockEntity.entityCooldowns.entrySet().removeIf(entry -> entry.getValue() <= 0);

        // Search only inside the strictly inner 8x8 room bounds (excluding boundary blocks)
        List<LivingEntity> insideEntities = level.getEntitiesOfClass(LivingEntity.class, roomData.interiorBox);

        for (LivingEntity entity : insideEntities) {
            // STRICT INTERIOR CHECK: Entity's position MUST be strictly inside the inner room boundary
            if (!roomData.interiorBox.contains(entity.position()) && !roomData.interiorBox.intersects(entity.getBoundingBox())) {
                continue;
            }

            UUID id = entity.getUUID();
            if (blockEntity.entityCooldowns.containsKey(id)) {
                continue; // Still on cooldown
            }

            spawnFallingAnvilAndDamage(level, roomData, entity);
            blockEntity.entityCooldowns.put(id, COOLDOWN_TICKS);
        }
    }

    private static class RoomData {
        final int originX;
        final int originY;
        final int originZ;
        final AABB interiorBox;

        RoomData(int ox, int oy, int oz) {
            this.originX = ox;
            this.originY = oy;
            this.originZ = oz;
            // Interior is x: [ox + 1, ox + 9], z: [oz + 1, oz + 9] (strictly inside the 10x10 border)
            this.interiorBox = new AABB(
                    ox + 1.0D, oy, oz + 1.0D,
                    ox + 9.0D, oy + 8.0D, oz + 9.0D
            );
        }
    }

    /**
     * Checks if this block is part of a 100% complete 10x10 room boundary (36/36 blocks).
     */
    private static RoomData findComplete10x10Room(Level level, BlockPos pos) {
        int posY = pos.getY();

        for (int ox = pos.getX() - 9; ox <= pos.getX(); ox++) {
            for (int oz = pos.getZ() - 9; oz <= pos.getZ(); oz++) {

                int matchingPerimeterCount = 0;

                // Check all 36 border positions of the 10x10 square
                for (int i = 0; i < 10; i++) {
                    // Top & Bottom edges (10 + 10)
                    if (isTrapBlock(level, ox + i, posY, oz)) matchingPerimeterCount++;
                    if (isTrapBlock(level, ox + i, posY, oz + 9)) matchingPerimeterCount++;

                    // Left & Right inner edges (8 + 8)
                    if (i > 0 && i < 9) {
                        if (isTrapBlock(level, ox, posY, oz + i)) matchingPerimeterCount++;
                        if (isTrapBlock(level, ox + 9, posY, oz + i)) matchingPerimeterCount++;
                    }
                }

                // STRICT: Require ALL 36 boundary blocks to be present
                if (matchingPerimeterCount >= EXACT_PERIMETER_BLOCKS) {
                    return new RoomData(ox, posY, oz);
                }
            }
        }
        return null;
    }

    private static boolean isTrapBlock(Level level, int x, int y, int z) {
        BlockPos checkPos = new BlockPos(x, y, z);
        return level.getBlockState(checkPos).is(ModRegistries.TRAP_ROOM_CORE.get());
    }

    private static void spawnFallingAnvilAndDamage(Level level, RoomData room, LivingEntity entity) {
        // Clamp anvil spawn coordinates strictly to the inner 8x8 area (ox+1 to ox+8, oz+1 to oz+8)
        int clampedX = Math.max(room.originX + 1, Math.min(room.originX + 8, entity.getBlockX()));
        int clampedZ = Math.max(room.originZ + 1, Math.min(room.originZ + 8, entity.getBlockZ()));
        int spawnY = Math.min((int) Math.floor(entity.getY()) + 5, room.originY + 7);

        BlockPos spawnPos = new BlockPos(clampedX, spawnY, clampedZ);

        // Spawn falling anvil entity configured to hurt entities
        FallingBlockEntity fallingBlock = FallingBlockEntity.fall(level, spawnPos, Blocks.ANVIL.defaultBlockState());
        fallingBlock.setHurtsEntities(2.0F, 40);

        // Instantly deal anvil damage to any player, mob, or creature inside the trap room
        entity.hurt(level.damageSources().anvil(fallingBlock), ANVIL_TRAP_DAMAGE);

        // Sound effect
        level.playSound(null, spawnPos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1.0F, 0.9F);
    }
}
