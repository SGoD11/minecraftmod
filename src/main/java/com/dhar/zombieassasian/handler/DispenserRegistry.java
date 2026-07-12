package com.dhar.zombieassasian.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * FEATURE 7 — Laser Trap (part 1: tracking)
 * --------------------------------------------
 * Keeps a lightweight set of "known dispenser positions" per level, updated
 * only when a dispenser is placed or removed — NOT by scanning the world
 * every tick, which would be expensive. LaserTrapHandler (part 2) checks
 * only these tracked positions periodically to find valid facing pairs.
 */
public class DispenserRegistry {

    private static final Map<Level, Set<BlockPos>> DISPENSERS_BY_LEVEL = new HashMap<>();

    public static Set<BlockPos> getDispensers(Level level) {
        return DISPENSERS_BY_LEVEL.getOrDefault(level, Set.of());
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getPlacedBlock().getBlock() instanceof DispenserBlock && event.getEntity() != null) {
            Level level = event.getEntity().level();
            DISPENSERS_BY_LEVEL
                    .computeIfAbsent(level, lvl -> new HashSet<>())
                    .add(event.getPos().immutable());
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState().getBlock() instanceof DispenserBlock && event.getPlayer() != null) {
            Level level = event.getPlayer().level();
            Set<BlockPos> set = DISPENSERS_BY_LEVEL.get(level);
            if (set != null) {
                set.remove(event.getPos());
            }
        }
    }
}
