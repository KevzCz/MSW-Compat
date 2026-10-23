package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.minecraft.entity.damage.DamageSource;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.pixeldreamstudios.mswcompat.util.MswCompatDebug;
import net.pixeldreamstudios.mswcompat.util.ProjectileScalingHelper;
import net.pixeldreamstudios.mswcompat.util.SpellPowerHelper;
import net.soulsweaponry.entity.projectile.LeviathanAxeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin( value = LeviathanAxeEntity.class)
public abstract class LeviathanAxeEntityMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$damageFactor = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(method = "getDamage", at = @At("HEAD"))
    private void mswcompat$cacheFactor(Entity target, CallbackInfoReturnable<Float> cir) {
        float factor = 1.0F;
        LeviathanAxeEntity self = (LeviathanAxeEntity) (Object) this;
        Entity owner = self.getOwner();
        if (owner instanceof LivingEntity living) {
            factor = ProjectileScalingHelper.leviathanThrownFactor(living);
        }
        mswcompat$damageFactor.set(factor);
    }

    @Inject(method = "getDamage", at = @At("RETURN"), cancellable = true)
    private void mswcompat$scaleThrownDamage(Entity target, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValueF() * mswcompat$damageFactor.get());
        mswcompat$damageFactor.remove();
    }

    @Redirect(
            method = "collide(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z",
                    remap = true
            ),
            remap = true
    )
    private boolean mswcompat$addStatusEffectWithFrostBonus(LivingEntity target, StatusEffectInstance original) {
        LeviathanAxeEntity self = (LeviathanAxeEntity)(Object)this;
        Entity owner = self.getOwner();

        int bonus = 0;
        if (owner instanceof LivingEntity living) {
            float frostPerAmp = ConfigHelper.getBaselineValue("leviathan_axe.frost_per_amplifier", 10.0F);
            double frost = SpellPowerHelper.getEffectiveSpellPower(living, MSWCompatIdentifiers.SpellPower.FROST);
            bonus = (int)Math.floor(frost / frostPerAmp);
        }

        RegistryEntry<StatusEffect> type = original.getEffectType();
        int duration = original.getDuration();
        int amp = Math.max(0, original.getAmplifier() + bonus);
        return target.addStatusEffect(new StatusEffectInstance(type, duration, amp));
    }

    @Redirect(
            method = "collide(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    remap = true
            ),
            remap = true
    )
    private boolean mswcompat$scaleSplashDamage(Entity target, DamageSource source, float amount) {
        LeviathanAxeEntity self = (LeviathanAxeEntity) (Object) this;
        Entity owner = self.getOwner();
        float factor = 1.0F;
        if (owner instanceof LivingEntity living) {
            factor = ProjectileScalingHelper.leviathanThrownFactor(living);
            MswCompatDebug.chat(
                    living,
                    String.format(
                            "Leviathan collide damage: %.1f -> %.1f (x%.2f)",
                            amount, amount * factor, factor));
        }
        return target.damage(source, amount * factor);
    }
}