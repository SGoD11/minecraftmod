package com.dhar.zombieassasian.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * FEATURE 7 — Laser Trap (part 1: tracking)
 * --------------------------------------------
 * Keeps a lightweight set of "known dispenser positions" per dimension.
 *
 * IMPORTANT FIX: the original version only tracked dispensers via
 * place/break events, which meant rejoining a world (fresh in-memory
 * state, nothing re-scanned) silently lost all tracked dispensers. Real
 * fix: also (re)populate from ChunkEvent.Load — whenever a chunk
 * containing dispensers loads (including right after rejoining, as chunks
 * load in around the player), we scan just THAT chunk's block entities
 * (cheap — only that chunk, not the world) and register any dispensers
 * found. ChunkEvent.Unload removes them again so memory doesn't grow
 * unbounded as the player travels.
 *
 * Keyed by ResourceKey<Level> (dimension identity) rather than the Level
 * object itself, since a new Level instance is created each time a world
 * is (re)loaded — keying by the object would silently orphan old entries.
 */
public class DispenserRegistry {

    private static final Map<ResourceKey<Level>, Set<BlockPos>> DISPENSERS_BY_DIMENSION = new HashMap<>();

    public static Set<BlockPos> getDispensers(Level level) {
        return DISPENSERS_BY_DIMENSION.getOrDefault(level.dimension(), Set.of());
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        if (event.getLevel().isClientSide()) {
            return;
        }
        ResourceKey<Level> dimension = chunk.getLevel().dimension();
        Set<BlockPos> known = DISPENSERS_BY_DIMENSION.computeIfAbsent(dimension, d -> new HashSet<>());

        for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
            if (entry.getValue() instanceof DispenserBlockEntity) {
                known.add(entry.getKey().immutable());
            }
        }
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (!(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        if (event.getLevel().isClientSide()) {
            return;
        }
        ResourceKey<Level> dimension = chunk.getLevel().dimension();
        Set<BlockPos> known = DISPENSERS_BY_DIMENSION.get(dimension);
        if (known == null) {
            return;
        }
        for (BlockPos pos : chunk.getBlockEntities().keySet()) {
            known.remove(pos);
        }
    }

    // Place/break events still handled directly too, so newly placed or
    // broken dispensers update immediately without waiting for a chunk
    // load/unload cycle.
    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getPlacedBlock().getBlock() instanceof DispenserBlock && event.getEntity() != null) {
            Level level = event.getEntity().level();
            DISPENSERS_BY_DIMENSION
                    .computeIfAbsent(level.dimension(), d -> new HashSet<>())
                    .add(event.getPos().immutable());
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState().getBlock() instanceof DispenserBlock && event.getPlayer() != null) {
            Level level = event.getPlayer().level();
            Set<BlockPos> set = DISPENSERS_BY_DIMENSION.get(level.dimension());
            if (set != null) {
                set.remove(event.getPos());
            }
        }
    }
}
