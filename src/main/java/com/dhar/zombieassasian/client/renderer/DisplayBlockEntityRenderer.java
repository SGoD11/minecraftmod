package com.dhar.zombieassasian.client.renderer;

import com.dhar.zombieassasian.blockentity.DisplayBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Draws the stored item slowly spinning above the display block. Purely
 * cosmetic — DisplayBlockEntity holds the actual data.
 */
public class DisplayBlockEntityRenderer implements BlockEntityRenderer<DisplayBlockEntity> {

    private final ItemRenderer itemRenderer;

    public DisplayBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(DisplayBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                        MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack stack = blockEntity.getDisplayedItem();
        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.75D, 0.5D);

        // Slow constant spin, driven by real time so it's smooth regardless
        // of tick rate/lag.
        long time = System.currentTimeMillis() % 3600L;
        float rotationDegrees = (time / 10.0F);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotationDegrees));

        poseStack.scale(0.5F, 0.5F, 0.5F);

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                poseStack, bufferSource, Minecraft.getInstance().level, 0);

        poseStack.popPose();
    }
}
