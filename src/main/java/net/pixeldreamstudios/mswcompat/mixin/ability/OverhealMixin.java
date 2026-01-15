package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.items.abilities.posthit.Overheal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = Overheal.class, remap = false )
public abstract class OverhealMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$ampScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Float> mswcompat$durationScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isBloodthirster(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (!shouldScale) {
            mswcompat$ampScale.set(1.0F);
            mswcompat$durationScale.set(1.0F);
            return;
        }

        float ampFactor = 1.0F;
        float durationFactor = 1.0F;

        if (attacker != null) {
            float adBaseline = ConfigHelper.getBaselineValue("bloodthirster.attack_damage_baseline", 8.0F);

            if (adBaseline > 0.0F) {
                double ad = attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (ad > 0.0) {
                    ampFactor = (float)(ad / adBaseline);
                    durationFactor = ampFactor;
                }
            }

            RegistryEntry.Reference<EntityAttribute> rageEntry = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.MoreRPGLibrary.RAGE);
            if (rageEntry != null) {
                double rage = attacker.getAttributeValue(rageEntry);
                float rageBaseline = ConfigHelper.getBaselineValue("bloodthirster.rage_baseline", 100.0F);
                if (rageBaseline > 0.0F && rage > 0.0) {
                    float rageMultiplier = (1.0F + (float)(rage / rageBaseline));
                    ampFactor *= rageMultiplier;
                    durationFactor *= rageMultiplier;
                }
            }
        }

        float minScale = ConfigHelper.getBaselineValue("bloodthirster.overheal_min_scale", 0.75F);
        float maxScale = ConfigHelper.getBaselineValue("bloodthirster.overheal_max_scale", 1.50F);
        ampFactor = Math.max(minScale, Math.min(maxScale, ampFactor));
        durationFactor = Math.max(minScale, Math.min(maxScale, durationFactor));

        mswcompat$ampScale.set(ampFactor);
        mswcompat$durationScale.set(durationFactor);
    }

    @ModifyVariable(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(value = "STORE"),
            ordinal = 0,
            require = 0
    )
    private int mswcompat$scaleDuration(int duration) {
        if (!mswcompat$shouldScale.get()) {
            return duration;
        }
        return (int)(duration * mswcompat$durationScale.get());
    }

    @ModifyVariable(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(value = "STORE"),
            ordinal = 1,
            require = 0
    )
    private int mswcompat$scaleAmplifier(int amp) {
        if (!mswcompat$shouldScale.get()) {
            return amp;
        }
        return (int)(amp * mswcompat$ampScale.get());
    }

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        mswcompat$ampScale.remove();
        mswcompat$durationScale.remove();
        mswcompat$shouldScale.remove();
    }
}