package com.dhar.zombieassasian.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;

/**
 * FEATURE 3 — Furnace Conversion
 * -------------------------------
 * Requirement: smelting a Diamond Axe produces a Cooked Diamond Axe; any
 * living entity struck by it is set on fire.
 *
 * Stats copied exactly from vanilla's own Diamond Axe registration
 * (Tiers.DIAMOND, 5.0F attack damage bonus, -3.0F attack speed bonus) so it
 * behaves identically as a weapon/tool aside from the fire effect.
 */
public class CookedDiamondAxeItem extends AxeItem {

    /**
     * Key used to mark a target's fire as "intentional" (caused by this
     * axe), read by ZombieBehaviorHandler (Feature 1) so its daylight
     * fire-clearing logic doesn't accidentally extinguish it.
     */
    public static final String INTENTIONAL_FIRE_TAG = "zombieassasian_intentional_fire";

    private static final int FIRE_SECONDS = 5;

    public CookedDiamondAxeItem(Properties properties) {
        super(Tiers.DIAMOND, 5.0F, -3.0F, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        target.setSecondsOnFire(FIRE_SECONDS);
        target.getPersistentData().putBoolean(INTENTIONAL_FIRE_TAG, true);
        return result;
    }
}
