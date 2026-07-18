package com.dhar.zombieassasian.register;

import com.dhar.zombieassasian.ZombieAssasianMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * A single custom creative tab holding every item this mod adds, so
 * testing doesn't require /give commands — just open Creative inventory
 * and find the tab (it appears near the end of the tab row, with the
 * Cooked Shield as its icon).
 */
public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ZombieAssasianMod.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.zombieassasian.main"))
                    .icon(() -> new ItemStack(ModRegistries.COOKED_SHIELD.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModRegistries.COOKED_SHIELD.get());
                        output.accept(ModRegistries.COOKED_DIAMOND_AXE.get());
                        output.accept(ModRegistries.WOODEN_MULTITOOL.get());
                        output.accept(ModRegistries.LONG_RANGED_BUCKET.get());
                        output.accept(ModRegistries.DISPLAY_BLOCK_ITEM.get());
                        output.accept(ModRegistries.TRAP_ROOM_CORE_ITEM.get());
                        output.accept(ModRegistries.DIMENSIONAL_KEY.get());
                        output.accept(ModRegistries.CUTE_PUPPY_SPAWN_EGG.get());
                        // Intentionally NOT adding LONG_RANGED_BUCKET_FILLED or
                        // BURNED_ARROW — those are only meant to appear as a
                        // result of gameplay (filling the bucket, reflecting an
                        // arrow), not picked directly from the creative menu.
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
