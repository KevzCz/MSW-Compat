package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.hit.EntityHitResult;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.DamageScalingHelper;
import net.soulsweaponry.entity.projectile.DraupnirSpearEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin( value = DraupnirSpearEntity.class, remap = false )
public abstract class DraupnirSpearEntityMixin {

    @Redirect(
            method = "onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    remap = true
            ),
            remap = true
    )
    private boolean mswcompat$scaleProjectileDamage(Entity target, DamageSource source, float amount, EntityHitResult hit) {
        DraupnirSpearEntity self = (DraupnirSpearEntity)(Object)this;
        float factor = 1.0F;
        Entity owner = self.getOwner();
        if (owner instanceof LivingEntity living) {
            factor = DamageScalingHelper.attackDamageOnly(
                    living.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
                    ConfigHelper.getBaselineValue("draupnir_spear.attack_damage_baseline", 8.0F));
        }

        return target.damage(source, amount * factor);
    }
}
