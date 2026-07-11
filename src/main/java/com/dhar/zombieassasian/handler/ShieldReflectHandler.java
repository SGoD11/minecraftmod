package com.dhar.zombieassasian.handler;

import com.dhar.zombieassasian.entity.BurnedArrowEntity;
import com.dhar.zombieassasian.item.CookedShieldItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * FEATURE 2 — Defensive Equipment
 * --------------------------------
 * Requirement: an arrow hitting a Cooked Shield gets reflected instead of
 * behaving normally, and the reflected projectile is the burned-arrow
 * variant.
 *
 * ProjectileImpactEvent fires the instant a projectile is ABOUT to resolve
 * its hit (damage a target, stick into a block, etc.) — cancelling it here
 * stops all of vanilla's normal arrow-hit handling, so we can fully replace
 * it with our own.
 */
public class ShieldReflectHandler {

    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent event) {
        // Only care about arrows (covers vanilla Arrow, Spectral Arrow, and
        // our own BurnedArrowEntity since it extends Arrow).
        if (!(event.getEntity() instanceof AbstractArrow arrow)) {
            return;
        }
        // Guard against infinite reflect loops: a burned arrow that gets
        // reflected again would otherwise keep bouncing forever.
        if (arrow instanceof BurnedArrowEntity) {
            return;
        }
        // Only care about arrows that hit an entity (not blocks).
        if (!(event.getRayTraceResult() instanceof EntityHitResult entityHit)) {
            return;
        }
        if (!(entityHit.getEntity() instanceof Player player)) {
            return;
        }
        if (!player.isBlocking()) {
            return;
        }

        ItemStack blockingStack = player.getUseItem();
        if (!(blockingStack.getItem() instanceof CookedShieldItem)) {
            return; // blocking with a normal shield/other item — let vanilla handle it
        }

        // From here on: this IS an arrow hitting a player blocking with the
        // Cooked Shield. Cancel vanilla handling and do our own.
        event.setCanceled(true);

        Level level = player.level();
        if (level.isClientSide) {
            return; // spawn the replacement arrow only on the server
        }

        // Reflect = reverse the incoming velocity (bounces straight back
        // toward whoever/wherever it came from).
        Vec3 reflectedVelocity = arrow.getDeltaMovement().scale(-1.0D);

        BurnedArrowEntity burnedArrow = new BurnedArrowEntity(level, arrow.getX(), arrow.getY(), arrow.getZ());
        burnedArrow.setDeltaMovement(reflectedVelocity);
        burnedArrow.setOwner(player);
        level.addFreshEntity(burnedArrow);

        arrow.discard(); // remove the original arrow entity
    }
}
