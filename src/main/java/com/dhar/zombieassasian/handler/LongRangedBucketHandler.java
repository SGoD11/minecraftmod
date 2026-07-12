package com.dhar.zombieassasian.handler;

import com.dhar.zombieassasian.item.ILongRangedTool;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

/**
 * FEATURE 11 — Long-Ranged Bucket
 * ---------------------------------
 * Requirement: ~20 block reach for breaking blocks, attacking, collecting
 * water, and placing water, while holding this item.
 *
 * Uses Forge's own reach attributes (ForgeMod.BLOCK_REACH for block
 * interaction, ForgeMod.ENTITY_REACH for entities) instead of anything
 * homemade — these are the same attributes Forge itself uses to check
 * reach server-side, so this isn't a visual-only trick. (Older Forge/MC
 * versions called these REACH_DISTANCE / ATTACK_RANGE — Forge renamed the
 * Java fields in the 1.20.x branch; the underlying mechanism is the same.)
 *
 * IMPORTANT, flagging honestly: BLOCK_REACH is reliably wired up in
 * Forge 1.20.1 (confirmed — this is the same mechanism "extra reach"
 * armor/tool mods have used successfully for years, e.g. block breaking
 * distance). ENTITY_REACH (extending how far you can hit entities) has a
 * known, documented bug in some 1.20.1 Forge builds where the attribute is
 * applied correctly but doesn't actually extend the attack range in
 * practice (unrelated to our code — it's a Forge-side issue). Please test
 * both separately: block breaking/water at range vs. hitting mobs at
 * range, and tell me if the attack-range part doesn't visibly work.
 *
 * Technique: since reach attributes only apply automatically for
 * equipped armor, we manually add/remove a transient AttributeModifier
 * each tick based on whether the Long-Ranged Bucket is in the main hand.
 * Fixed UUIDs let us find & remove our own modifier reliably instead of
 * stacking duplicates.
 */
public class LongRangedBucketHandler {

    private static final double EXTRA_REACH = 15.5D; // ~4.5 base + this ≈ 20 total
    private static final UUID REACH_MODIFIER_ID = UUID.fromString("8f36c92e-2b1a-4f3e-9c4a-2f6f1a6a10a1");
    private static final UUID ATTACK_RANGE_MODIFIER_ID = UUID.fromString("8f36c92e-2b1a-4f3e-9c4a-2f6f1a6a10a2");

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        boolean holdingBucket = player.getMainHandItem().getItem() instanceof ILongRangedTool;

        applyOrRemove(player.getAttribute(ForgeMod.BLOCK_REACH.get()), REACH_MODIFIER_ID, holdingBucket);
        applyOrRemove(player.getAttribute(ForgeMod.ENTITY_REACH.get()), ATTACK_RANGE_MODIFIER_ID, holdingBucket);
    }

    private void applyOrRemove(AttributeInstance attribute, UUID modifierId, boolean shouldHaveModifier) {
        if (attribute == null) {
            return; // attribute not present on this entity type — nothing to do
        }
        boolean alreadyHasModifier = attribute.getModifier(modifierId) != null;

        if (shouldHaveModifier && !alreadyHasModifier) {
            attribute.addTransientModifier(new AttributeModifier(
                    modifierId, "long_ranged_bucket_reach", EXTRA_REACH, AttributeModifier.Operation.ADDITION));
        } else if (!shouldHaveModifier && alreadyHasModifier) {
            attribute.removeModifier(modifierId);
        }
    }
}
