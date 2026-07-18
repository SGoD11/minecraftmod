package com.dhar.zombieassasian.client.renderer;

import com.dhar.zombieassasian.ZombieAssasianMod;
import com.dhar.zombieassasian.entity.CutePuppyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CutePuppyModel extends GeoModel<CutePuppyEntity> {

    @Override
    public ResourceLocation getModelResource(CutePuppyEntity animatable) {
        return new ResourceLocation(ZombieAssasianMod.MODID, "geo/cute_puppy.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CutePuppyEntity animatable) {
        return new ResourceLocation(ZombieAssasianMod.MODID, "textures/entity/cute_puppy.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CutePuppyEntity animatable) {
        return new ResourceLocation(ZombieAssasianMod.MODID, "animations/cute_puppy.animation.json");
    }
}
