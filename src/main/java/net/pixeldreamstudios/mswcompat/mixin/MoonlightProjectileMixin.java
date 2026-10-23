package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.pixeldreamstudios.mswcompat.util.MswCompatScaleContext;
import net.pixeldreamstudios.mswcompat.util.ProjectileScalingHelper;
import net.soulsweaponry.entity.projectile.MoonlightProjectile;
import net.soulsweaponry.entity.projectile.arrow.MoonlightArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PersistentProjectileEntity.class)
public abstract class MoonlightProjectileMixin {

    @ModifyVariable(
            method = "setDamage(D)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private double mswcompat$scaleProjectileDamage(double damage) {
        Object self = this;
        if (self instanceof MoonlightProjectile) {
            return MswCompatScaleContext.applyMoonlightShortswordDamage(damage);
        }
        if (self instanceof MoonlightArrow arrow) {
            Entity owner = arrow.getOwner();
            if (owner instanceof LivingEntity living) {
                float factor = ProjectileScalingHelper.arrowStormDamageFactor(living);
                return factor == 1.0F ? damage : damage * factor;
            }
        }
        return damage;
    }
}
