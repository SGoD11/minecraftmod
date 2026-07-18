package com.dhar.zombieassasian.client.renderer;

import com.dhar.zombieassasian.entity.CutePuppyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CutePuppyRenderer extends GeoEntityRenderer<CutePuppyEntity> {
    public CutePuppyRenderer(EntityRendererProvider.Context context) {
        super(context, new CutePuppyModel());
    }
}
