package com.dhar.zombieassasian;

import com.dhar.zombieassasian.client.BurnedArrowRenderer;
import com.dhar.zombieassasian.client.ShieldRenderHandler;
import com.dhar.zombieassasian.handler.ShieldReflectHandler;
import com.dhar.zombieassasian.handler.ZombieBehaviorHandler;
import com.dhar.zombieassasian.register.ModRegistries;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
        // The "mod event" bus handles registry/setup events (registering
        // items, entities, blocks, etc.) — different from the bus below.
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModRegistries.register(modEventBus);
        modEventBus.addListener(this::clientSetup);

        // MinecraftForge.EVENT_BUS is the "game event" bus — entity ticks,
        // damage, targeting, right-click, etc. all fire here.
        MinecraftForge.EVENT_BUS.register(new ZombieBehaviorHandler());
        MinecraftForge.EVENT_BUS.register(new ShieldReflectHandler());

        LOGGER.info("{} initialized", MODID);
    }

    /**
     * Client-only setup — tells the game HOW to draw entities we register.
     * Without this, any entity type with no renderer registered crashes
     * the game the moment one exists in a loaded chunk.
     */
    private void clientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(ModRegistries.BURNED_ARROW.get(), BurnedArrowRenderer::new);
        MinecraftForge.EVENT_BUS.register(new ShieldRenderHandler());
    }
}