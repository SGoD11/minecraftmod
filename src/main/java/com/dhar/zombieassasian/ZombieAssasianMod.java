package com.dhar.zombieassasian;

import com.dhar.zombieassasian.client.BurnedArrowRenderer;
import com.dhar.zombieassasian.client.ShieldRenderHandler;
import com.dhar.zombieassasian.client.renderer.CutePuppyRenderer;
import com.dhar.zombieassasian.client.renderer.DisplayBlockEntityRenderer;
import com.dhar.zombieassasian.entity.CutePuppyEntity;
import com.dhar.zombieassasian.handler.DispenserRegistry;
import com.dhar.zombieassasian.handler.LaserTrapHandler;
import com.dhar.zombieassasian.handler.LongRangedBucketHandler;
import com.dhar.zombieassasian.handler.MultiToolHandler;
import com.dhar.zombieassasian.handler.PuppyLoyaltyHandler;
import com.dhar.zombieassasian.handler.ShieldReflectHandler;
import com.dhar.zombieassasian.handler.SpyglassVillageHandler;
import com.dhar.zombieassasian.handler.ZombieBehaviorHandler;
import com.dhar.zombieassasian.register.ModCreativeTabs;
import com.dhar.zombieassasian.register.ModRegistries;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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
        ModCreativeTabs.register(modEventBus);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerAttributes);

        // MinecraftForge.EVENT_BUS is the "game event" bus — entity ticks,
        // damage, targeting, right-click, etc. all fire here.
        MinecraftForge.EVENT_BUS.register(new ZombieBehaviorHandler());
        MinecraftForge.EVENT_BUS.register(new ShieldReflectHandler());
        MinecraftForge.EVENT_BUS.register(new MultiToolHandler());
        MinecraftForge.EVENT_BUS.register(new PuppyLoyaltyHandler());
        MinecraftForge.EVENT_BUS.register(new SpyglassVillageHandler());
        MinecraftForge.EVENT_BUS.register(new LongRangedBucketHandler());
        MinecraftForge.EVENT_BUS.register(new DispenserRegistry());
        MinecraftForge.EVENT_BUS.register(new LaserTrapHandler());

        LOGGER.info("{} initialized", MODID);
    }

    /**
     * Common setup (runs on both client and server). GeckoLib REQUIRES this
     * initialize() call once at startup — without it, GeckoLib's internal
     * systems (animation registry, etc.) never start, and any GeoEntity
     * (like Cute Puppy) will misbehave or fail to animate.
     */
    private void commonSetup(FMLCommonSetupEvent event) {
        software.bernie.geckolib.GeckoLib.initialize();
    }

    /**
     * Every living entity MUST have its attributes (max health, movement
     * speed, etc.) registered here, or the game crashes the moment one is
     * spawned with "No attribute registered" — this was missing before,
     * which is why Cute Puppy wasn't safe to enable yet.
     */
    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModRegistries.CUTE_PUPPY.get(), CutePuppyEntity.createAttributes().build());
    }

    /**
     * Client-only setup — tells the game HOW to draw entities/block entities
     * we register. Without this, anything with no renderer registered
     * crashes the game the moment one exists in a loaded chunk.
     */
    private void clientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(ModRegistries.BURNED_ARROW.get(), BurnedArrowRenderer::new);
        EntityRenderers.register(ModRegistries.CUTE_PUPPY.get(), CutePuppyRenderer::new);
        BlockEntityRenderers.register(ModRegistries.DISPLAY_BLOCK_ENTITY.get(), DisplayBlockEntityRenderer::new);
        MinecraftForge.EVENT_BUS.register(new ShieldRenderHandler());
    }
}
