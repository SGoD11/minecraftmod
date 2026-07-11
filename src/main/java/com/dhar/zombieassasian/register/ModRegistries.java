package com.dhar.zombieassasian.register;

import com.dhar.zombieassasian.ZombieAssasianMod;
import com.dhar.zombieassasian.entity.BurnedArrowEntity;
import com.dhar.zombieassasian.item.CookedShieldItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Single place where every new item / entity / block etc. gets registered.
 * Keeping ALL registries in one file (instead of scattering DeferredRegisters
 * across many classes) makes it easy to see everything the mod adds, and
 * avoids "did I register this on the right bus?" mistakes later.
 *
 * DeferredRegister is Forge's standard, safe way to register things — it
 * defers actual registration until the right point in Minecraft's startup,
 * instead of doing it immediately (which crashes).
 */
public class ModRegistries {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ZombieAssasianMod.MODID);

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ZombieAssasianMod.MODID);

    // --- Feature 2: Cooked Shield ---
    // durability(336) matches vanilla shield's durability exactly.
    public static final RegistryObject<Item> COOKED_SHIELD = ITEMS.register("cooked_shield",
            () -> new CookedShieldItem(new Item.Properties().durability(336)));

    public static final RegistryObject<EntityType<BurnedArrowEntity>> BURNED_ARROW =
            ENTITY_TYPES.register("burned_arrow",
                    () -> EntityType.Builder.<BurnedArrowEntity>of(BurnedArrowEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .build("burned_arrow"));

    /**
     * Call this once from the main mod class constructor. Hooks every
     * DeferredRegister above onto Forge's mod event bus.
     */
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
    }
}
