package com.dhar.zombieassasian.handler;

import com.dhar.zombieassasian.ZombieAssasianMod;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
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
import net.minecraft.world.entity.LivingEntity;

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

            // Add a fresh goal: attack the nearest hostile mob (Monster),
            // excluding itself and other Zombies (so zombies don't fight
            // each other — remove the "!= Zombie" check below if you want
            // zombie-on-zombie violence instead).
            targetSelector.addGoal(1, new NearestAttackableTargetGoal<Monster>(
                    zombie,
                    Monster.class,
                    10,     // how often it re-checks for a target
                    true,   // must see target (line of sight)
                    false,  // don't require target to already be provoked
                    (LivingEntity target) -> target != zombie && !(target instanceof Zombie)
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
     * fire and any other fire source (lava, flint & steel, fire charges,
     * a burning weapon, etc.) through the exact same mechanism, so there is
     * no clean built-in way to tell them apart without Mixins. This handler
     * uses a heuristic — only clears fire when it's daytime, the zombie is
     * NOT in water/rain, and it has no helmet (all 3 are exactly vanilla's
     * own conditions for sun-burning). Edge case: if some other feature we
     * build later also sets a Zombie on fire during a sunny, dry moment,
     * this will incorrectly extinguish it too.
     *
     * When we build the Cooked Diamond Axe (Feature 3, "set living entity
     * on fire on hit"), we'll set a short-lived NBT flag on the target via
     * `entity.getPersistentData()` right when the axe hits, and check for
     * that flag here before clearing — so intentional fire is protected.
     * That flag doesn't exist yet, so it's not referenced below yet.
     */
    @SubscribeEvent
    public void onZombieTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().getClass() != Zombie.class) {
            return;
        }
        Zombie zombie = (Zombie) event.getEntity();
        if (!zombie.isOnFire()) {
            return;
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