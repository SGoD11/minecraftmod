package com.dhar.zombieassasian.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * FEATURE 7 — Laser Trap (part 2: beam + damage)
 * -------------------------------------------------
 * Requirement: two dispensers form a visible red beam between them; any
 * player intersecting it takes damage immediately.
 *
 * Runs every CHECK_INTERVAL_TICKS (not every single tick) and only checks
 * positions from DispenserRegistry (dispensers actually placed in the
 * world) — never scans the whole world. For each pair of tracked
 * dispensers on the same axis, facing directly at each other, with a clear
 * (air) line between them, it's treated as an active beam: red dust
 * particles are spawned along the line (visible to all nearby players,
 * server-driven so no custom renderer needed) and any player whose
 * bounding box intersects the line takes damage.
 */
public class LaserTrapHandler {

    private static final int CHECK_INTERVAL_TICKS = 5; // runs 4x per second
    private static final int MAX_BEAM_LENGTH = 32;
    private static final float DAMAGE_PER_HIT = 4.0F;

    private int tickCounter = 0;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        tickCounter++;
        if (tickCounter < CHECK_INTERVAL_TICKS) {
            return;
        }
        tickCounter = 0;

        MinecraftServer server = event.getServer();
        for (ServerLevel level : server.getAllLevels()) {
            checkLevel(level);
        }
    }

    private void checkLevel(ServerLevel level) {
        Set<BlockPos> dispensers = DispenserRegistry.getDispensers(level);
        if (dispensers.size() < 2) {
            return;
        }

        BlockPos[] positions = dispensers.toArray(new BlockPos[0]);
        for (int i = 0; i < positions.length; i++) {
            for (int j = i + 1; j < positions.length; j++) {
                Optional<Direction> beamAxis = findBeamBetween(level, positions[i], positions[j]);
                if (beamAxis.isPresent()) {
                    fireBeam(level, positions[i], positions[j]);
                }
            }
        }
    }

    /**
     * Returns the direction from posA to posB if: they share two coordinates
     * (same axis), are within MAX_BEAM_LENGTH, both dispensers actually face
     * toward each other, and every block strictly between them is air.
     */
    private Optional<Direction> findBeamBetween(Level level, BlockPos posA, BlockPos posB) {
        Direction axisDirection = getAxisDirection(posA, posB);
        if (axisDirection == null) {
            return Optional.empty();
        }

        int distance = manhattanDistanceAlongAxis(posA, posB);
        if (distance < 2 || distance > MAX_BEAM_LENGTH) {
            return Optional.empty(); // too close (overlapping) or too far
        }

        BlockState stateA = level.getBlockState(posA);
        BlockState stateB = level.getBlockState(posB);
        if (!(stateA.getBlock() instanceof DispenserBlock) || !(stateB.getBlock() instanceof DispenserBlock)) {
            return Optional.empty();
        }

        Direction facingA = stateA.getValue(DispenserBlock.FACING);
        Direction facingB = stateB.getValue(DispenserBlock.FACING);
        if (facingA != axisDirection || facingB != axisDirection.getOpposite()) {
            return Optional.empty(); // not actually facing each other
        }

        // Clear line check: every block strictly between the two dispensers must be air.
        BlockPos cursor = posA.relative(axisDirection);
        while (!cursor.equals(posB)) {
            if (!level.getBlockState(cursor).isAir()) {
                return Optional.empty();
            }
            cursor = cursor.relative(axisDirection);
        }

        return Optional.of(axisDirection);
    }

    @javax.annotation.Nullable
    private Direction getAxisDirection(BlockPos a, BlockPos b) {
        if (a.getY() == b.getY() && a.getZ() == b.getZ() && a.getX() != b.getX()) {
            return a.getX() < b.getX() ? Direction.EAST : Direction.WEST;
        }
        if (a.getX() == b.getX() && a.getZ() == b.getZ() && a.getY() != b.getY()) {
            return a.getY() < b.getY() ? Direction.UP : Direction.DOWN;
        }
        if (a.getX() == b.getX() && a.getY() == b.getY() && a.getZ() != b.getZ()) {
            return a.getZ() < b.getZ() ? Direction.SOUTH : Direction.NORTH;
        }
        return null; // not aligned on a single axis
    }

    private int manhattanDistanceAlongAxis(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()) + Math.abs(a.getZ() - b.getZ());
    }

    private void fireBeam(ServerLevel level, BlockPos posA, BlockPos posB) {
        Vec3 start = Vec3.atCenterOf(posA);
        Vec3 end = Vec3.atCenterOf(posB);
        Vec3 direction = end.subtract(start);
        double length = direction.length();
        Vec3 step = direction.normalize().scale(0.5D); // particle every half-block

        DustParticleOptions redDust = new DustParticleOptions(new org.joml.Vector3f(1.0F, 0.0F, 0.0F), 2.5F);

        Vec3 point = start;
        double traveled = 0.0D;
        while (traveled < length) {
            // 3 particles per point instead of 1 — noticeably denser/brighter,
            // easier to spot even if particle video settings are turned down.
            level.sendParticles(redDust, point.x, point.y, point.z, 3, 0.02, 0.02, 0.02, 0.0);
            point = point.add(step);
            traveled += 0.5D;
        }

        // Damage any player whose bounding box the beam line intersects.
        AABB beamBounds = new AABB(start, end).inflate(0.5D);
        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, beamBounds);
        for (Player player : nearbyPlayers) {
            Optional<Vec3> hit = player.getBoundingBox().inflate(0.2D).clip(start, end);
            if (hit.isPresent()) {
                player.hurt(level.damageSources().generic(), DAMAGE_PER_HIT);
            }
        }
    }
}
