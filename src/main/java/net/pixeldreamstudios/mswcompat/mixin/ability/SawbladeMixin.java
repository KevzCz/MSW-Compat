package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.items.abilities.usagetick.Sawblade;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = Sawblade.class, remap = false )
public abstract class SawbladeMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$damageScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Float> mswcompat$bleedScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "usageTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;I)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheScale(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
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

            float adPart = adBaseline > 0.0F ? (float)(ad / adBaseline) : 1.0F;
            float ragePart = rageBaseline > 0.0F ? (float)(rage / rageBaseline) : 0.0F;

            damageFactor = 1.0F + adWeight * (adPart - 1.0F) + rageWeight * ragePart;

            float bleedRageWeight = ConfigHelper.getFloatValue("whirligig_sawblade.bleed_rage_weight", 0.5F);
            bleedFactor = 1.0F + bleedRageWeight * ragePart;

            float minDamageScale = ConfigHelper.getBaselineValue("whirligig_sawblade.damage_min_scale", 0.5F);
            float maxDamageScale = ConfigHelper.getBaselineValue("whirligig_sawblade.damage_max_scale", 10.0F);
            damageFactor = Math.max(minDamageScale, Math.min(maxDamageScale, damageFactor));

            float minBleedScale = ConfigHelper.getBaselineValue("whirligig_sawblade.bleed_min_scale", 0.5F);
            float maxBleedScale = ConfigHelper.getBaselineValue("whirligig_sawblade.bleed_max_scale", 12.5F);
            bleedFactor = Math.max(minBleedScale, Math.min(maxBleedScale, bleedFactor));
        }
        mswcompat$damageScale.set(damageFactor);
        mswcompat$bleedScale.set(bleedFactor);
    }

    @ModifyVariable(
            method = "usageTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;I)V",
            at = @At(value = "STORE"),
            ordinal = 0,
            require = 0
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
            require = 0
    )
    private void mswcompat$clearScale(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
        mswcompat$damageScale.remove();
        mswcompat$bleedScale.remove();
        mswcompat$shouldScale.remove();
    }
}