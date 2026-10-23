package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.pixeldreamstudios.mswcompat.util.DamageScalingHelper;
import net.pixeldreamstudios.mswcompat.util.SpellPowerHelper;
import net.soulsweaponry.items.abilities.posthit.BonusMagicDamage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = BonusMagicDamage.class, remap = false )
public abstract class BonusMagicDamageMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$damageScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("HEAD"),
            remap = true
    )
    private void mswcompat$cacheScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isLichBane(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (!shouldScale) {
            mswcompat$damageScale.set(1.0F);
            return;
        }

        float factor = 1.0F;
        if (attacker != null) {
            factor = DamageScalingHelper.spellPowerFactor(
                    attacker, MSWCompatIdentifiers.SpellPower.FIRE,
                    ConfigHelper.getBaselineValue("lich_bane.fire_baseline", 20.0F));
        }

        mswcompat$damageScale.set(factor);
    }

    @ModifyVariable(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(value = "STORE"),
            ordinal = 0,
            remap = true
    )
    private float mswcompat$scaleMagicDamage(float dmg) {
        if (!mswcompat$shouldScale.get()) {
            return dmg;
        }
        return dmg * mswcompat$damageScale.get();
    }

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("TAIL"),
            remap = true
    )
    private void mswcompat$clearScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        mswcompat$damageScale.remove();
        mswcompat$shouldScale.remove();
    }
}
