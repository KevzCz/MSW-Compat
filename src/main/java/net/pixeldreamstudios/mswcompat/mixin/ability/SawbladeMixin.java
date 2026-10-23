package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.DamageScalingHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.pixeldreamstudios.mswcompat.util.MswCompatBleedContext;
import net.soulsweaponry.items.abilities.usagetick.Sawblade;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = Sawblade.class, remap = false )
public abstract class SawbladeMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$damageScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Float> mswcompat$bleedScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);
    @Unique private static final ThreadLocal<LivingEntity> mswcompat$user = new ThreadLocal<>();

    @Inject(
            method = "usageTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;I)V",
            at = @At("HEAD"),
            remap = true
)
    private void mswcompat$cacheScale(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
        mswcompat$user.set(user);
        boolean shouldScale = ItemMatcher.isWhirligigSawblade(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (! shouldScale) {
            mswcompat$damageScale.set(1.0F);
            mswcompat$bleedScale.set(1.0F);
            return;
        }

        float damageFactor = 1.0F;
        float bleedFactor = 1.0F;

        if (user != null) {
            float adBaseline = ConfigHelper.getBaselineValue("whirligig_sawblade.attack_damage_baseline", 11.0F);
            float rageBaseline = ConfigHelper.getBaselineValue("whirligig_sawblade.rage_baseline", 100.0F);
            float adWeight = ConfigHelper.getFloatValue("whirligig_sawblade.attack_damage_weight", 0.7F);
            float rageWeight = ConfigHelper.getFloatValue("whirligig_sawblade.rage_weight", 0.3F);

            double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            double rage = 0.0;

            RegistryEntry.Reference<EntityAttribute> rageEntry = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.MoreRPGLibrary.RAGE);
            if (rageEntry != null) {
                rage = user.getAttributeValue(rageEntry);
            }

            damageFactor = DamageScalingHelper.attackDamageAndSecondary(ad, adBaseline, adWeight, rage, rageBaseline, rageWeight);

            float bleedRageWeight = ConfigHelper.getFloatValue("whirligig_sawblade.bleed_rage_weight", 0.5F);
            bleedFactor = DamageScalingHelper.factor(rage, rageBaseline, bleedRageWeight, DamageScalingHelper.Mode.FROM_ZERO);

            float minDamageScale = ConfigHelper.getBaselineValue("whirligig_sawblade.damage_min_scale", 0.5F);
            float maxDamageScale = ConfigHelper.getBaselineValue("whirligig_sawblade.damage_max_scale", 10.0F);
            damageFactor = DamageScalingHelper.clamp(damageFactor, minDamageScale, maxDamageScale);

            float minBleedScale = ConfigHelper.getBaselineValue("whirligig_sawblade.bleed_min_scale", 0.5F);
            float maxBleedScale = ConfigHelper.getBaselineValue("whirligig_sawblade.bleed_max_scale", 12.5F);
            bleedFactor = DamageScalingHelper.clamp(bleedFactor, minBleedScale, maxBleedScale);
        }
        mswcompat$damageScale.set(damageFactor);
        mswcompat$bleedScale.set(bleedFactor);
    }

    @ModifyArg(
            method = "usageTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entitydata/BleedData;addBleed(Lnet/minecraft/entity/LivingEntity;I)V",
                    remap = true
            ),
            index = 1,
            remap = true
    )
    private int mswcompat$scaleBleedBuildup(LivingEntity target, int amount) {
        MswCompatBleedContext.begin("Whirligig Sawblade (ability)", mswcompat$user.get(), target);
        if (!mswcompat$shouldScale.get()) {
            return amount;
        }
        int scaled = (int) (amount * mswcompat$bleedScale.get());
        MswCompatBleedContext.setBuildup(amount, scaled, mswcompat$bleedScale.get());
        return scaled;
    }

    @ModifyVariable(
            method = "usageTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;I)V",
            at = @At(value = "STORE"),
            ordinal = 0,
            remap = true
)
    private float mswcompat$scaleDamage(float damage) {
        if (! mswcompat$shouldScale.get()) {
            return damage;
        }
        return damage * mswcompat$damageScale.get();
    }

    @Inject(
            method = "usageTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;I)V",
            at = @At("TAIL"),
            remap = true
)
    private void mswcompat$clearScale(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
        mswcompat$damageScale.remove();
        mswcompat$bleedScale.remove();
        mswcompat$shouldScale.remove();
        mswcompat$user.remove();
    }
}