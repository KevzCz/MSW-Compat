package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.DamageScalingHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MswCompatBleedContext;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.items.abilities.posthit.Bleed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = Bleed.class, remap = false)
public abstract class BleedMixin {
    @Unique
    private static final ThreadLocal<Float> mswcompat$scale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique
    private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);
    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("HEAD"),
            remap = true
    )
    private void mswcompat$cacheBleedScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isAnyBleedWeapon(stack);
        mswcompat$shouldScale.set(shouldScale);

        String source = stack.getItem().getName().getString();
        if (ItemMatcher.isMoonveil(stack)) {
            source = "Moonveil";
        } else if (ItemMatcher.isBloodlust(stack)) {
            source = "Bloodlust";
        } else {
            source = "Bleed (" + source + ")";
        }
        MswCompatBleedContext.begin(source, attacker, target);

        if (!shouldScale) {
            mswcompat$scale.set(1.0F);
            return;
        }

        float factor = 1.0F;
        if (attacker != null) {
            String configPrefix = ItemMatcher.isMoonveil(stack) ? "moonveil" : "bloodlust";

            float adBaseline = ConfigHelper.getBaselineValue(configPrefix + ".attack_damage_baseline", 7.0F);
            float rageBaseline = ConfigHelper.getBaselineValue(configPrefix + ".rage_baseline", 100.0F);
            float adWeight = ConfigHelper.getFloatValue(configPrefix + ".bleed_attack_damage_weight", 0.7F);
            float rageWeight = ConfigHelper.getFloatValue(configPrefix + ".bleed_rage_weight", 0.3F);

            double ad = attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            double rage = 0.0;

            RegistryEntry.Reference<EntityAttribute> rageEntry = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.MoreRPGLibrary.RAGE);
            if (rageEntry != null) {
                rage = attacker.getAttributeValue(rageEntry);
            }

            factor = DamageScalingHelper.attackDamageAndSecondary(ad, adBaseline, adWeight, rage, rageBaseline, rageWeight);

            float minScale = ConfigHelper.getBaselineValue(configPrefix + ".bleed_buildup_min_scale", 0.5F);
            float maxScale = ConfigHelper.getBaselineValue(configPrefix + ".bleed_buildup_max_scale", 2.0F);
            factor = DamageScalingHelper.clamp(factor, minScale, maxScale);
        }

        mswcompat$scale.set(factor);
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
    private int mswcompat$scaleBleedAmount(int amount) {
        if (!mswcompat$shouldScale.get()) {
            return amount;
        }
        int scaled = (int) (amount * mswcompat$scale.get());
        MswCompatBleedContext.setBuildup(amount, scaled, mswcompat$scale.get());
        return scaled;
    }

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("TAIL"),
            remap = true
    )
    private void mswcompat$clearBleedScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        mswcompat$scale.remove();
        mswcompat$shouldScale.remove();
    }
}
