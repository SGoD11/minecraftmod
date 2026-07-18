package com.dhar.zombieassasian.handler;

import com.dhar.zombieassasian.entity.CutePuppyEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * FEATURE 4 (continued) — Cute Puppy loyalty/defense
 * -----------------------------------------------------
 * Requirement: the puppy is loyal — if anyone attacks the player, the
 * puppy attacks that enemy back.
 *
 * This is NOT owner-specific (the puppy isn't tamed to one player) — ANY
 * nearby Cute Puppy defends ANY player who gets hurt by a living attacker,
 * matching what was asked for ("anyone attacks the user, it shall attack
 * that enemy") in the simplest, most direct way.
 *
 * LivingHurtEvent fires whenever ANY living entity takes damage, before the
 * damage is actually applied — perfect moment to react to "player got
 * attacked."
 */
public class PuppyLoyaltyHandler {

    private static final double DEFENSE_RADIUS = 16.0D;

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return; // only care about players being attacked
        }

        DamageSource source = event.getSource();
        Entity attackerEntity = source.getEntity();
        if (!(attackerEntity instanceof LivingEntity attacker)) {
            return; // not attacked by a living thing (e.g. fall damage, fire) — nothing to fight
        }
        if (attacker instanceof CutePuppyEntity) {
            return; // don't sic puppies on each other if one somehow hurts a player accidentally
        }

        AABB searchArea = player.getBoundingBox().inflate(DEFENSE_RADIUS);
        List<CutePuppyEntity> nearbyPuppies = player.level().getEntitiesOfClass(CutePuppyEntity.class, searchArea);

        for (CutePuppyEntity puppy : nearbyPuppies) {
            puppy.setTarget(attacker);
        }
    }
}
