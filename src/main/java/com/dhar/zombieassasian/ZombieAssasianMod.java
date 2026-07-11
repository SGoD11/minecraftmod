package com.dhar.zombieassasian;

import com.dhar.zombieassasian.handler.ZombieBehaviorHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main entry point for the mod. Forge finds this class via the @Mod
 * annotation and the modId string must exactly match "modId" in mods.toml.
 *
 * Keep this class thin — it should only wire up registration/event handlers.
 * Actual feature logic lives in its own class under the relevant package
 * (handler/, item/, entity/, worldgen/, etc.) so bugs stay isolated and easy
 * to find.
 */
@Mod(ZombieAssasianMod.MODID)
public class ZombieAssasianMod {

    // Must match mod_id in gradle.properties / mods.toml exactly.
    public static final String MODID = "zombieassasian";

    // Shared logger — use this instead of System.out so log lines are
    // tagged with the mod name in the console/log file.
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public ZombieAssasianMod() {
        // MinecraftForge.EVENT_BUS is the "game event" bus — entity ticks,
        // damage, targeting, right-click, etc. all fire here.
        // (There is a separate "mod event" bus for setup/registry events,
        // which we are not using yet in this phase.)
        MinecraftForge.EVENT_BUS.register(new ZombieBehaviorHandler());

        LOGGER.info("{} initialized", MODID);
    }
}
