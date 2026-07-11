package com.dhar.zombieassasian.entity;

import com.dhar.zombieassasian.register.ModRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * The "burned arrow variant" mentioned in the brief. Behaves exactly like a
 * normal arrow, except:
 *   - sets whatever it hits on fire for 5 seconds
 *   - spawns flame particles while flying so it visibly reads as "on fire"
 *     (a plain Entity like this doesn't get the automatic fire-overlay
 *     render that LivingEntities get, so we fake it with particles instead
 *     of writing a custom renderer)
 */
public class BurnedArrowEntity extends Arrow {

    // Required constructor shape for EntityType.Builder factories.
    public BurnedArrowEntity(EntityType<? extends Arrow> type, Level level) {
        super(type, level);
    }

    // Convenience constructor used by our reflect handler to spawn one at a
    // specific position (mirrors AbstractArrow's own "position" constructor).
    public BurnedArrowEntity(Level level, double x, double y, double z) {
        super(ModRegistries.BURNED_ARROW.get(), level);
        this.setPos(x, y, z);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        result.getEntity().setSecondsOnFire(5);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.FLAME,
                    this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }
}
