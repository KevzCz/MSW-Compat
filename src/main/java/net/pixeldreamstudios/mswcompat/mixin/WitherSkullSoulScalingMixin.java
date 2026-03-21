package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.pixeldreamstudios.mswcompat.util.SpellPowerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin( value = WitherSkullEntity.class)
public abstract class WitherSkullSoulScalingMixin {

    @Unique
    private static float mswcompat$factorFromSource(DamageSource src) {
        Entity attacker = src.getAttacker();
        if (!(attacker instanceof PlayerEntity player)) return 1.0F;

        float baseline = ConfigHelper.getBaselineValue("wither_skull.soul_baseline", 20.0F);
        return SpellPowerHelper.getScalingFactor(player, MSWCompatIdentifiers.SpellPower.SOUL, baseline);
    }

    @Redirect(
            method = "onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            )
    )
    private boolean mswcompat$scalePlayerWitherSkullDamage(Entity target, DamageSource source, float amount) {
        return target.damage(source, amount * mswcompat$factorFromSource(source));
    }
}