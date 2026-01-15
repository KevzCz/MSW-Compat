package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.soulsweaponry.items.abilities.posthit.SwitchPostHit;
import net.soulsweaponry.registry.EffectRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = SwitchPostHit.class, remap = false)
public abstract class SwitchPostHitMixin {
    @Unique private static final ThreadLocal<Integer> mswcompat$ampBonus = ThreadLocal.withInitial(() -> 0);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "onMainHandEquip(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheEquipBonus(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isNightlordsSword(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (! shouldScale) {
            mswcompat$ampBonus.set(0);
            return;
        }

        int bonus = 0;
        if (player != null) {
            float adBaseline = ConfigHelper.getBaselineValue("nightlords_sword.attack_damage_baseline", 10.0F);
            float ampPerAD = ConfigHelper.getFloatValue("nightlords_sword.amp_per_attack_damage", 5.0F);

            if (adBaseline > 0.0F && ampPerAD > 0.0F) {
                double ad = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (ad > adBaseline) {
                    bonus = (int)Math.floor((ad - adBaseline) / ampPerAD);
                }
            }
        }
        mswcompat$ampBonus.set(bonus);
    }

    @Redirect(
            method = "onMainHandEquip(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z"),
            require = 0
    )
    private boolean mswcompat$scalePotencyOnEquip(PlayerEntity player, StatusEffectInstance original) {
        if (! mswcompat$shouldScale.get() || ! original.getEffectType().equals(EffectRegistry.POTENCY)) {
            return player.addStatusEffect(original);
        }

        int newAmp = Math.max(0, original.getAmplifier() + mswcompat$ampBonus.get());
        StatusEffectInstance scaled = new StatusEffectInstance(
                original.getEffectType(),
                original.getDuration(),
                newAmp
        );
        return player.addStatusEffect(scaled);
    }

    @Inject(
            method = "onMainHandEquip(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearEquipBonus(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        mswcompat$ampBonus.remove();
        mswcompat$shouldScale.remove();
    }

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cachePostHitBonus(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isNightlordsSword(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (!shouldScale) {
            mswcompat$ampBonus.set(0);
            return;
        }

        int bonus = 0;
        if (attacker != null) {
            float adBaseline = ConfigHelper.getBaselineValue("nightlords_sword.attack_damage_baseline", 10.0F);
            float ampPerAD = ConfigHelper.getFloatValue("nightlords_sword.amp_per_attack_damage", 5.0F);

            if (adBaseline > 0.0F && ampPerAD > 0.0F) {
                double ad = attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (ad > adBaseline) {
                    bonus = (int)Math.floor((ad - adBaseline) / ampPerAD);
                }
            }
        }
        mswcompat$ampBonus.set(bonus);
    }

    @Redirect(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z"),
            require = 0
    )
    private boolean mswcompat$scaleStatusEffect(LivingEntity target, StatusEffectInstance original) {
        if (!mswcompat$shouldScale.get()) {
            return target.addStatusEffect(original);
        }

        int bonus = mswcompat$ampBonus.get();
        int newAmp = Math.max(0, original.getAmplifier() + bonus);

        StatusEffectInstance scaled = new StatusEffectInstance(
                original.getEffectType(),
                original.getDuration(),
                newAmp
        );
        return target.addStatusEffect(scaled);
    }

    @ModifyArg(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(value = "INVOKE", target = "Lnet/soulsweaponry/entitydata/BleedData;addBleed(Lnet/minecraft/entity/LivingEntity;I)V"),
            index = 1,
            require = 0
    )
    private int mswcompat$scaleBleed(int bleed) {
        if (!mswcompat$shouldScale.get()) {
            return bleed;
        }

        float scale = 1.0F + (mswcompat$ampBonus.get() * 0.2F);
        return (int)(bleed * scale);
    }

    @ModifyArg(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(value = "INVOKE", target = "Lnet/soulsweaponry/entitydata/FrostData;addFrost(Lnet/minecraft/entity/LivingEntity;I)V"),
            index = 1,
            require = 0
    )
    private int mswcompat$scaleFrost(int frost) {
        if (!mswcompat$shouldScale.get()) {
            return frost;
        }

        float scale = 1.0F + (mswcompat$ampBonus.get() * 0.2F);
        return (int)(frost * scale);
    }

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearPostHitBonus(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        mswcompat$ampBonus.remove();
        mswcompat$shouldScale.remove();
    }
}