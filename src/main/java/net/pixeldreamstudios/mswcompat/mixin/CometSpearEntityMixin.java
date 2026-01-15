package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.hit.EntityHitResult;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.soulsweaponry.entity.projectile.CometSpearEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = CometSpearEntity.class, remap = false )
public abstract class CometSpearEntityMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$scale = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(
            method = "onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheScale(EntityHitResult entityHitResult, CallbackInfo ci) {
        CometSpearEntity self = (CometSpearEntity)(Object)this;
        float factor = 1.0F;
        Entity owner = self.getOwner();
        float baseline = ConfigHelper.getBaselineValue("comet_spear.attack_damage_baseline", 8.0F);

        if (owner instanceof LivingEntity living && baseline > 0.0F) {
            double ad = living.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (ad > 0.0) factor = (float)(ad / baseline);
        }

        mswcompat$scale.set(factor);
    }

    @ModifyVariable(
            method = "onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V",
            at = @At(value = "STORE"),
            ordinal = 0,
            require = 0
    )
    private float mswcompat$scaleBaseDamage(float f) {
        return f * mswcompat$scale.get();
    }

    @Inject(
            method = "onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearScale(EntityHitResult entityHitResult, CallbackInfo ci) {
        mswcompat$scale.remove();
    }
}