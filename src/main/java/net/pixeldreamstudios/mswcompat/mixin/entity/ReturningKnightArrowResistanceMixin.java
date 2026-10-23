package net.pixeldreamstudios.mswcompat.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.pixeldreamstudios.mswcompat.config.MSWCompatConfig;
import net.soulsweaponry.entity.mobs.ReturningKnight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ReturningKnight.class)
public abstract class ReturningKnightArrowResistanceMixin {

    @Unique
    private static final ThreadLocal<Float> mswcompat$arrowResistanceScale = ThreadLocal.withInitial(() -> -1.0F);

    @Inject(
            method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At("HEAD")
    )
    private void mswcompat$captureArrowResistance(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        MSWCompatConfig.ReturningKnightConfig config = MSWCompatConfig.getInstance().returning_knight;
        if (!config.replaceArrowImmunityWithResistance) {
            mswcompat$arrowResistanceScale.set(-1.0F);
            return;
        }

        Entity attacker = source.getAttacker();
        if (!(attacker instanceof PersistentProjectileEntity)) {
            mswcompat$arrowResistanceScale.set(-1.0F);
            return;
        }

        ReturningKnight self = (ReturningKnight) (Object) this;
        if (self.isProjectileWhitelisted(attacker)) {
            mswcompat$arrowResistanceScale.set(-1.0F);
            return;
        }

        float resistance = Math.max(0.0F, Math.min(1.0F, config.arrowDamageResistanceMultiplier));
        mswcompat$arrowResistanceScale.set(resistance);
    }

    @Redirect(
            method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/mobs/ReturningKnight;isProjectileWhitelisted(Lnet/minecraft/entity/Entity;)Z"
            )
    )
    private boolean mswcompat$skipArrowImmunity(ReturningKnight self, Entity projectile) {
        if (mswcompat$arrowResistanceScale.get() >= 0.0F) {
            return true;
        }
        return self.isProjectileWhitelisted(projectile);
    }

    @ModifyArg(
            method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/mobs/BossEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            ),
            index = 1
    )
    private float mswcompat$scaleArrowDamage(float amount) {
        float scale = mswcompat$arrowResistanceScale.get();
        if (scale >= 0.0F) {
            mswcompat$arrowResistanceScale.remove();
            return amount * scale;
        }
        return amount;
    }

    /**
     * In mobTick, ReturningKnight loops over nearby ProjectileEntities and negates their velocity
     * (physically deflecting them) for any that aren't whitelisted. We redirect the same
     * isProjectileWhitelisted check here: for PersistentProjectileEntity (arrows) when resistance
     * mode is enabled, return true so they are NOT deflected.
     */
    @Redirect(
            method = "mobTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/mobs/ReturningKnight;isProjectileWhitelisted(Lnet/minecraft/entity/Entity;)Z"
            )
    )
    private boolean mswcompat$skipArrowDeflection(ReturningKnight self, Entity projectile) {
        MSWCompatConfig.ReturningKnightConfig config = MSWCompatConfig.getInstance().returning_knight;
        if (config.replaceArrowImmunityWithResistance && projectile instanceof PersistentProjectileEntity) {
            return true;
        }
        return self.isProjectileWhitelisted(projectile);
    }
}
