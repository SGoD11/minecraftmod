package com.dhar.zombieassasian.handler;

import com.dhar.zombieassasian.item.CookedDiamondAxeItem;
import com.dhar.zombieassasian.ZombieAssasianMod;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Field;

/**
 * FEATURE 1 — Creature Behaviour
 * -------------------------------
 * Requirement:
 *   - Standard Zombies no longer treat players as valid targets.
 *   - Their aggression is redirected toward other hostile entities.
 *   - Daylight burning is disabled for them.
 *
 * Why reflection is used here:
 *   Vanilla's Mob class keeps its "targetSelector" (the AI goal list that
 *   decides who to attack) as a protected field with no public getter.
 *   Since 1.17, Forge runs on Mojang's official (deobfuscated) mappings even
 *   in the compiled jar, so a plain Java reflection field lookup by name
 *   ("targetSelector") works identically in both the dev environment and the
 *   exported mod — no obfuscation-mapping helper needed.
 *
 * Scope note:
 *   We deliberately check `entity.getClass() == Zombie.class` (not
 *   `instanceof Zombie`) everywhere, so this ONLY affects plain Zombies —
 *   Husks, Drowned, and Zombie Villagers are untouched, matching
 *   "Standard Zombies" in the brief.
 */
public class ZombieBehaviorHandler {

    /**
     * Fires once when any entity is added to the world (spawn, chunk load,
     * dimension change, etc.). This is where we edit the zombie's AI goals,
     * once, right after it exists.
     */
    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return; // AI only needs to be edited on the server; client is just rendering.
        }
        if (event.getEntity().getClass() == Zombie.class) {
            redirectZombieTargeting((Zombie) event.getEntity());
        }
    }

    private void redirectZombieTargeting(Zombie zombie) {
        try {
            Field targetSelectorField = Mob.class.getDeclaredField("targetSelector");
            targetSelectorField.setAccessible(true);
            GoalSelector targetSelector = (GoalSelector) targetSelectorField.get(zombie);

            // Wipe every existing target goal (this removes vanilla's
            // "attack nearest player" goal along with everything else).
            targetSelector.removeAllGoals(goal -> true);

            // Add a fresh goal: attack the nearest hostile-or-aggressive mob.
            // Target type is Mob.class (broad) because NeutralMob-implementing
            // animals (Polar Bear, Wolf, Bee, etc.) are Animal subclasses, NOT
            // Monster subclasses — Monster.class alone would miss them
            // entirely, which was the bug. The predicate below narrows it back
            // down to only Monster + NeutralMob instances, excluding Players,
            // Zombies (no cannibalism), and plain passive animals (cows,
            // sheep, pigs, chickens, etc. are Animal but NOT NeutralMob, so
            // they're correctly excluded).
            targetSelector.addGoal(1, new NearestAttackableTargetGoal<Mob>(
                    zombie,
                    Mob.class,
                    10,     // how often it re-checks for a target
                    true,   // check line of sight
                    false,  // don't require target to already be provoked
                    (LivingEntity target) -> target != zombie
                            && !(target instanceof Zombie)
                            && !(target instanceof Player)
                            && (target instanceof Monster || target instanceof NeutralMob)
            ));
        } catch (ReflectiveOperationException e) {
            // If this ever fails (e.g. a Forge/Minecraft update renames the
            // field), log it loudly instead of silently doing nothing —
            // easy to spot in the log if zombies start acting vanilla again.
            ZombieAssasianMod.LOGGER.error("Could not access Mob#targetSelector via reflection", e);
        }
    }

    /**
     * Belt-and-braces safety net: if ANYTHING else in the game (a splash
     * potion, another mod, a command) tries to set a Zombie's target to a
     * Player, this cancels it. LivingChangeTargetEvent fires right before
     * the new target is applied, so cancelling it fully prevents the
     * assignment.
     */
    @SubscribeEvent
    public void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity().getClass() == Zombie.class
                && event.getNewTarget() instanceof Player) {
            event.setCanceled(true);
        }
    }

    /**
     * Disables sun-burning for plain Zombies.
     *
     * Limitation (flagging honestly, not hiding it): vanilla applies sun
     * fire and any other fire source through the same mechanism, so there's
     * no built-in way to tell them apart without Mixins. This uses a
     * heuristic — only clears fire when it's daytime, the zombie is NOT in
     * water/rain, and has no helmet (vanilla's own sun-burn conditions).
     *
     * Feature 3 (Cooked Diamond Axe) intentionally sets zombies on fire on
     * hit, which would otherwise collide with this same heuristic during
     * daytime. To prevent that, CookedDiamondAxeItem tags its target with
     * CookedDiamondAxeItem.INTENTIONAL_FIRE_TAG in persistent data — we
     * check for that tag here and skip clearing if it's present. The tag is
     * removed once the zombie stops being on fire, so it doesn't linger and
     * protect unrelated future fires.
     */
    @SubscribeEvent
    public void onZombieTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().getClass() != Zombie.class) {
            return;
        }
        Zombie zombie = (Zombie) event.getEntity();

        if (!zombie.isOnFire()) {
            // Not on fire — clean up the tag if it's still lingering from a
            // previous intentional fire that has since burned out.
            if (zombie.getPersistentData().contains(CookedDiamondAxeItem.INTENTIONAL_FIRE_TAG)) {
                zombie.getPersistentData().remove(CookedDiamondAxeItem.INTENTIONAL_FIRE_TAG);
            }
            return;
        }

        if (zombie.getPersistentData().getBoolean(CookedDiamondAxeItem.INTENTIONAL_FIRE_TAG)) {
            return; // Cooked Diamond Axe fire — protected, let it burn out naturally.
        }

        Level level = zombie.level();
        boolean matchesSunBurnConditions = level.isDay()
                && !zombie.isInWaterRainOrBubble()
                && zombie.getItemBySlot(EquipmentSlot.HEAD).isEmpty();

        if (matchesSunBurnConditions) {
            zombie.clearFire();
        }
    }
}