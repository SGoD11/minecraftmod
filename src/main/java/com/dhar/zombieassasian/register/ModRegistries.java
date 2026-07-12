package com.dhar.zombieassasian.register;

import com.dhar.zombieassasian.ZombieAssasianMod;
import com.dhar.zombieassasian.block.DisplayBlock;
import com.dhar.zombieassasian.blockentity.DisplayBlockEntity;
import com.dhar.zombieassasian.entity.BurnedArrowEntity;
//import com.dhar.zombieassasian.entity.CutePuppyEntity;
import com.dhar.zombieassasian.item.CookedDiamondAxeItem;
import com.dhar.zombieassasian.item.CookedShieldItem;
import com.dhar.zombieassasian.item.MultiToolItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
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

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ZombieAssasianMod.MODID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ZombieAssasianMod.MODID);

    // --- Feature 2: Cooked Shield ---
    // durability(336) matches vanilla shield's durability exactly.
    public static final RegistryObject<Item> COOKED_SHIELD = ITEMS.register("cooked_shield",
            () -> new CookedShieldItem(new Item.Properties().durability(336)));

    // --- Feature 3: Cooked Diamond Axe ---
    // No explicit durability call needed — AxeItem/DiggerItem/TieredItem's
    // constructor chain sets durability from the tier (Tiers.DIAMOND)
    // automatically.
    public static final RegistryObject<Item> COOKED_DIAMOND_AXE = ITEMS.register("cooked_diamond_axe",
            () -> new CookedDiamondAxeItem(new Item.Properties()));

    public static final RegistryObject<EntityType<BurnedArrowEntity>> BURNED_ARROW =
            ENTITY_TYPES.register("burned_arrow",
                    () -> EntityType.Builder.<BurnedArrowEntity>of(BurnedArrowEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .build("burned_arrow"));

    // --- Feature 4: Cute Puppy — INTENTIONALLY NOT REGISTERED RIGHT NOW ---
    // Set aside per your request while we stabilize other features. The
    // entity/model/renderer code still exists under entity/ and
    // client/renderer/ — just not hooked into the game yet. Uncomment when
    // ready to resume:
    //
    // public static final RegistryObject<EntityType<CutePuppyEntity>> CUTE_PUPPY =
    //         ENTITY_TYPES.register("cute_puppy",
    //                 () -> EntityType.Builder.of(CutePuppyEntity::new, MobCategory.CREATURE)
    //                         .sized(0.6f, 0.7f)
    //                         .build("cute_puppy"));

    // --- Feature 12: Multi-Tool ---
    public static final RegistryObject<Item> WOODEN_MULTITOOL = ITEMS.register("wooden_multitool",
            () -> new MultiToolItem(new Item.Properties().durability(59))); // durability matches vanilla wooden tools

    // --- Feature 6: Interactive Display ---
    public static final RegistryObject<Block> DISPLAY_BLOCK = BLOCKS.register("display_block",
            () -> new DisplayBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .noOcclusion()));

    public static final RegistryObject<Item> DISPLAY_BLOCK_ITEM = ITEMS.register("display_block",
            () -> new BlockItem(DISPLAY_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<BlockEntityType<DisplayBlockEntity>> DISPLAY_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("display_block",
                    () -> BlockEntityType.Builder.of(DisplayBlockEntity::new, DISPLAY_BLOCK.get()).build(null));

    /**
     * Call this once from the main mod class constructor. Hooks every
     * DeferredRegister above onto Forge's mod event bus.
     */
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
