package com.dhar.zombieassasian.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

/**
 * FEATURE 4 — Morph Support (mob part)
 * -------------------------------------
 * A real, spawnable/killable mob so Identity 2's generic
 * kill -> unlock -> morph system can pick it up automatically (confirmed
 * behavior — Identity 2 doesn't need per-entity registration for that part).
 *
 * The sit/idle/walk animations come from your Blockbench GeckoLib export
 * (cute_puppy.geo.json / cute_puppy.animation.json). Sitting is toggled by
 * SITTING (synced so it renders correctly for every nearby player), and
 * driven by a dedicated keybind — see ToggleSitPacket + KeybindHandler.
 *
 * NOTE: the "while morphed" part of this requirement (triggering sit
 * specifically when a player is morphed via Identity 2) is intentionally
 * NOT wired here yet, per your request to set that aside for now. Right
 * now, the keybind toggles sit on the nearest Cute Puppy entity in the
 * world — it does not yet check/require an active Identity 2 morph.
 */
public class CutePuppyEntity extends Animal implements GeoEntity {

    private static final EntityDataAccessor<Boolean> SITTING =
            SynchedEntityData.defineId(CutePuppyEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public CutePuppyEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Priority 1 (checked before wandering) — once something sets this
        // puppy's target (see handler/PuppyLoyaltyHandler.java, triggered
        // when a nearby player gets attacked), this goal makes it actually
        // chase and fight that target instead of just standing there.
        this.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SITTING, false);
    }

    public boolean isSitting() {
        return this.entityData.get(SITTING);
    }

    public void setSitting(boolean sitting) {
        this.entityData.set(SITTING, sitting);
    }

    @Nullable
    @Override
    public CutePuppyEntity getBreedOffspring(net.minecraft.server.level.ServerLevel level, AgeableMob otherParent) {
        return null; // breeding not implemented — not part of the brief
    }

    // --- GeckoLib wiring ---

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, this::animationPredicate));
    }

    private PlayState animationPredicate(software.bernie.geckolib.core.animation.AnimationState<CutePuppyEntity> state) {
        if (this.isSitting()) {
            state.getController().setAnimation(RawAnimation.begin().thenLoop("sit"));
        } else if (state.isMoving()) {
            state.getController().setAnimation(RawAnimation.begin().thenLoop("walk"));
        } else {
            state.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
