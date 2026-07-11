package com.dhar.zombieassasian.client;

import com.dhar.zombieassasian.entity.BurnedArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * ArrowRenderer is abstract (it declares getTextureLocation() without
 * implementing it), so it can't be used directly as `ArrowRenderer::new` —
 * we need this small concrete subclass instead.
 *
 * Reusing vanilla's own arrow texture as a placeholder for now (same as the
 * Cooked Shield's placeholder icon). Swap TEXTURE below once a real
 * "burned arrow" texture exists.
 */
public class BurnedArrowRenderer extends ArrowRenderer<BurnedArrowEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/projectiles/arrow.png");

    public BurnedArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(BurnedArrowEntity entity) {
        return TEXTURE;
    }
}