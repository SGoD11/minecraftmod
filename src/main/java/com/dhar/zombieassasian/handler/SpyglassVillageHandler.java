package com.dhar.zombieassasian.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import com.mojang.datafixers.util.Pair;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * FEATURE 9 — Spyglass Interaction
 * ----------------------------------
 * Requirement: looking at a Pig through a Spyglass teleports the player to
 * a Village.
 *
 * "Using a spyglass" is detected the same way vanilla itself does — the
 * player is actively using an item (isUsingItem) and that item is a
 * Spyglass. A manual ray-trace (using AABB#clip against nearby Pigs, the
 * same technique vanilla uses internally for entity ray-tracing) finds
 * what the player is actually looking at.
 *
 * teleportedPlayers prevents re-triggering every single tick while the
 * player keeps looking through the spyglass at the same pig — it only
 * fires once per continuous "look" until they stop using the spyglass.
 */
public class SpyglassVillageHandler {

    private static final double MAX_LOOK_DISTANCE = 64.0D;
    private static final int STRUCTURE_SEARCH_RADIUS_CHUNKS = 100;

    private final Set<java.util.UUID> alreadyTeleported = new HashSet<>();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        boolean usingSpyglass = player.isUsingItem() && player.getUseItem().is(Items.SPYGLASS);
        if (!usingSpyglass) {
            alreadyTeleported.remove(player.getUUID());
            return;
        }
        if (alreadyTeleported.contains(player.getUUID())) {
            return;
        }

        Entity lookedAt = raytraceNearestEntity(player, MAX_LOOK_DISTANCE);
        if (lookedAt instanceof Pig) {
            teleportToNearestVillage(serverPlayer);
            alreadyTeleported.add(player.getUUID());
        }
    }

    /**
     * Ray-traces from the player's eyes along their look direction, finding
     * the closest entity matching the given distance whose bounding box the
     * ray actually intersects (not just "nearby" — must be in the crosshair
     * line).
     */
    private Entity raytraceNearestEntity(Player player, double maxDistance) {
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 endPos = eyePos.add(lookVec.scale(maxDistance));

        AABB searchArea = player.getBoundingBox().expandTowards(lookVec.scale(maxDistance)).inflate(1.0D);

        Entity closest = null;
        double closestDistanceSq = maxDistance * maxDistance;

        for (Entity entity : player.level().getEntities(player, searchArea)) {
            if (!(entity instanceof Pig)) {
                continue;
            }
            AABB box = entity.getBoundingBox().inflate(0.3D);
            Optional<Vec3> hit = box.clip(eyePos, endPos);
            if (hit.isPresent()) {
                double distSq = eyePos.distanceToSqr(hit.get());
                if (distSq < closestDistanceSq) {
                    closestDistanceSq = distSq;
                    closest = entity;
                }
            }
        }
        return closest;
    }

    private void teleportToNearestVillage(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Registry<Structure> structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Optional<HolderSet.Named<Structure>> villageTag = structureRegistry.getTag(StructureTags.VILLAGE);
        if (villageTag.isEmpty()) {
            return;
        }

        BlockPos searchOrigin = player.blockPosition();
        Pair<BlockPos, Holder<Structure>> result = level.getChunkSource().getGenerator()
                .findNearestMapStructure(level, villageTag.get(), searchOrigin, STRUCTURE_SEARCH_RADIUS_CHUNKS, false);

        if (result == null) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("No village found nearby."), true);
            return;
        }

        BlockPos structurePos = result.getFirst();
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, structurePos.getX(), structurePos.getZ());

        player.teleportTo(level,
                structurePos.getX() + 0.5D,
                surfaceY,
                structurePos.getZ() + 0.5D,
                Set.of(),
                player.getYRot(),
                player.getXRot());
    }
}
