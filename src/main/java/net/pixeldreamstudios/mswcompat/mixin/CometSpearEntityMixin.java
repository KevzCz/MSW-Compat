package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.hit.EntityHitResult;
import net.soulsweaponry.entity.projectile.CometSpearEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(value = CometSpearEntity.class)
public abstract class CometSpearEntityMixin {
    @Unique private static final float BASELINE_AD = 8.0F;


    @Redirect(
            method = "onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            ),
            require = 0
    )
    private boolean mswcompat$scaleCometDamage(Entity target, DamageSource source, float amount, EntityHitResult hit) {
        CometSpearEntity self = (CometSpearEntity)(Object)this;

        float factor = 1.0F;
        Entity owner = self.getOwner();
        if (owner instanceof LivingEntity living && BASELINE_AD > 0.0F) {
            double ad = living.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (ad > 0.0) factor = (float)(ad / BASELINE_AD);
        }

        return target.damage(source, amount * factor);
    }
}
