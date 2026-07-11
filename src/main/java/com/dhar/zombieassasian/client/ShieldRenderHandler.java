package com.dhar.zombieassasian.client;

import com.dhar.zombieassasian.item.CookedShieldItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Adds extra lift to the Cooked Shield specifically while the player is
 * actively blocking with it.
 *
 * RenderHandEvent fires right before vanilla draws the held item in your
 * hand, carrying the same PoseStack vanilla is about to use. Pushing a
 * translation onto it here (without cancelling the event) means vanilla's
 * own blocking-pose transform still applies underneath — this just adds
 * on top, instead of fighting or replacing it. That's why it's safer than
 * trying to override the pose entirely.
 *
 * IMPORTANT: this class is CLIENT-ONLY (uses Minecraft/LocalPlayer, which
 * don't exist on a dedicated server). It must only ever be registered from
 * inside FMLClientSetupEvent — never from common/shared code — otherwise
 * a dedicated server would crash trying to load this class. See
 * ZombieAssasianMod.clientSetup().
 */
public class ShieldRenderHandler {

    // How much extra height to add while blocking. Positive Y = up.
    // Tweak this single number to raise it more/less.
    private static final double EXTRA_LIFT = 0.18D;

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof CookedShieldItem)) {
            return; // not our shield, do nothing — let vanilla render as normal
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !player.isUsingItem()) {
            return; // not currently blocking (button not held)
        }
        if (player.getUsedItemHand() != event.getHand()) {
            return; // blocking with the OTHER hand, this event is for the idle one
        }

        event.getPoseStack().translate(0.0D, EXTRA_LIFT, 0.0D);
    }
}