package com.dhar.zombieassasian.item;

import net.minecraft.world.item.ShieldItem;

/**
 * Cooked Shield — functionally a normal shield (blocking, durability, etc.
 * all inherited from vanilla ShieldItem). The special "reflect arrows"
 * behavior is NOT here — it's handled globally in
 * handler/ShieldReflectHandler.java, which checks "is the player holding
 * THIS specific item" before reflecting. Keeping the reflect logic outside
 * the item class means we can later reuse the same handler for other
 * shields if needed, without touching this file.
 */
public class CookedShieldItem extends ShieldItem {
    public CookedShieldItem(Properties properties) {
        super(properties);
    }
}
