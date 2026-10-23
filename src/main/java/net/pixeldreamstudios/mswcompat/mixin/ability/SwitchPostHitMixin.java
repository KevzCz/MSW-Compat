package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MswCompatBleedContext;
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
    @Unique
    private static final ThreadLocal<Integer> mswcompat$ampBonus = ThreadLocal.withInitial(() -> 0);
    @Unique
    private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);
    @Unique
    private static final ThreadLocal<Float> mswcompat$amountScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique
    private static final ThreadLocal<LivingEntity> mswcompat$attacker = new ThreadLocal<>();

    @Inject(
            method = "onMainHandEquip(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V",
            at = @At("HEAD"),
            remap = true
    )
    private void mswcompat$cacheEquipBonus(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isNightlordsSword(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (!shouldScale) {
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
                    bonus = (int) Math.floor((ad - adBaseline) / ampPerAD);
                }
            }
        }
        mswcompat$ampBonus.set(bonus);
    }

    @Redirect(
            method = "onMainHandEquip(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z",
                    remap = true
            ),
            remap = true
    )
    private boolean mswcompat$scalePotencyOnEquip(PlayerEntity player, StatusEffectInstance original) {
        if (!mswcompat$shouldScale.get() || !original.getEffectType().equals(EffectRegistry.POTENCY)) {
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
            remap = true
    )
    private void mswcompat$clearEquipBonus(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        mswcompat$ampBonus.remove();
        mswcompat$shouldScale.remove();
    }

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("HEAD"),
            remap = true
    )
    private void mswcompat$cachePostHitBonus(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isNightlordsSword(stack);
        mswcompat$shouldScale.set(shouldScale);
        mswcompat$attacker.set(attacker);

        if (!shouldScale) {
            mswcompat$ampBonus.set(0);
            mswcompat$amountScale.set(1.0F);
            return;
        }

        int bonus = 0;
        if (attacker != null) {
            float adBaseline = ConfigHelper.getBaselineValue("nightlords_sword.attack_damage_baseline", 10.0F);
            float ampPerAD = ConfigHelper.getFloatValue("nightlords_sword.amp_per_attack_damage", 5.0F);

            if (adBaseline > 0.0F && ampPerAD > 0.0F) {
                double ad = attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (ad > adBaseline) {
                    bonus = (int) Math.floor((ad - adBaseline) / ampPerAD);
                }
            }
        }
        mswcompat$ampBonus.set(bonus);
        mswcompat$amountScale.set(1.0F + (bonus * 0.2F));
    }

    @ModifyArg(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entitydata/BleedData;addBleed(Lnet/minecraft/entity/LivingEntity;I)V",
                    remap = true
            ),
            index = 1,
            remap = true
    )
    private int mswcompat$scaleBleedAmount(LivingEntity target, int amount) {
        MswCompatBleedContext.begin("Nightlord's Sword", mswcompat$attacker.get(), target);
        if (!mswcompat$shouldScale.get()) {
            return amount;
        }
        int scaled = (int) (amount * mswcompat$amountScale.get());
        MswCompatBleedContext.setBuildup(amount, scaled, mswcompat$amountScale.get());
        return scaled;
    }

    @ModifyArg(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entitydata/FrostData;addFrost(Lnet/minecraft/entity/LivingEntity;I)V",
                    remap = true
            ),
            index = 1,
            remap = true
    )
    private int mswcompat$scaleFrostAmount(int amount) {
        if (!mswcompat$shouldScale.get()) {
            return amount;
        }
        return (int) (amount * mswcompat$amountScale.get());
    }

    @ModifyArg(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/items/abilities/ChainLightning;trigger(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;FD)V",
                    remap = true
            ),
            index = 3,
            remap = true
    )
    private float mswcompat$scaleChainLightningDamage(float damage) {
        if (!mswcompat$shouldScale.get()) {
            return damage;
        }
        return damage * mswcompat$amountScale.get();
    }

    @Redirect(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z",
                    remap = true
            ),
            remap = true
    )
    private boolean mswcompat$scaleStatusEffect(LivingEntity target, StatusEffectInstance original) {
        if (!mswcompat$shouldScale.get()) {
            return target.addStatusEffect(original);
        }

        int newAmp = Math.max(0, original.getAmplifier() + mswcompat$ampBonus.get());
        StatusEffectInstance scaled = new StatusEffectInstance(
                original.getEffectType(),
                original.getDuration(),
                newAmp
        );
        return target.addStatusEffect(scaled);
    }

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("TAIL"),
            remap = true
    )
    private void mswcompat$clearPostHitBonus(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        mswcompat$amountScale.remove();
        mswcompat$ampBonus.remove();
        mswcompat$shouldScale.remove();
        mswcompat$attacker.remove();
    }
}
