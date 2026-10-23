package net.pixeldreamstudios.mswcompat.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.pixeldreamstudios.mswcompat.config.MSWCompatConfig;
import net.soulsweaponry.entity.mobs.Moonknight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Moonknight.class)
public abstract class MoonknightArrowResistanceMixin {

    @Unique
    private static final ThreadLocal<Float> mswcompat$arrowResistanceScale = ThreadLocal.withInitial(() -> -1.0F);

    /**
     * At HEAD: if resistance mode is on and an unwhitelisted arrow hits, record the
     * resistance multiplier so the whitelist redirect and damage modifier can use it.
     */
    @Inject(
            method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At("HEAD")
    )
    private void mswcompat$captureArrowResistance(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        MSWCompatConfig.FallenIconConfig config = MSWCompatConfig.getInstance().fallen_icon;
        if (!config.replaceArrowReflectionWithResistance) {
            mswcompat$arrowResistanceScale.set(-1.0F);
            return;
        }

        Entity attacker = source.getAttacker();
        if (!(attacker instanceof PersistentProjectileEntity)) {
            mswcompat$arrowResistanceScale.set(-1.0F);
            return;
        }

        Moonknight self = (Moonknight) (Object) this;
        if (self.isProjectileWhitelisted(attacker)) {
            mswcompat$arrowResistanceScale.set(-1.0F);
            return;
        }

        float resistance = Math.max(0.0F, Math.min(1.0F, config.arrowDamageResistanceMultiplier));
        mswcompat$arrowResistanceScale.set(resistance);
    }

    /**
     * Redirect the isProjectileWhitelisted(Entity) call inside damage(): when resistance mode
     * is active, return true (pretend arrow is whitelisted) so the reflection/absorption block
     * is skipped and damage falls through to BossEntity.damage().
     */
    @Redirect(
            method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/mobs/Moonknight;isProjectileWhitelisted(Lnet/minecraft/entity/Entity;)Z"
            )
    )
    private boolean mswcompat$skipArrowReflection(Moonknight self, Entity projectile) {
        if (mswcompat$arrowResistanceScale.get() >= 0.0F) {
            return true;
        }
        return self.isProjectileWhitelisted(projectile);
    }

    /**
     * Scale the damage amount passed to BossEntity.damage() when resistance mode redirected us here.
     */
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
}
