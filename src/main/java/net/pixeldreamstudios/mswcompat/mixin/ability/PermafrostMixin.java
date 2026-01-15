package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.items.abilities.posthit.Permafrost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = Permafrost.class, remap = false )
public abstract class PermafrostMixin {
    @Unique private static final ThreadLocal<Integer> mswcompat$ampBonus = ThreadLocal.withInitial(() -> 0);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheFrostAmp(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isAnyPermafrost(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (!shouldScale) {
            mswcompat$ampBonus.set(0);
            return;
        }

        int bonus = 0;
        if (attacker != null) {
            String configKey;
            if (ItemMatcher.isFrostmourne(stack)) {
                configKey = "frostmourne.frost_per_amplifier";
            } else if (ItemMatcher.isLeviathanAxe(stack)) {
                configKey = "leviathan_axe.frost_per_amplifier";
            } else {
                configKey = "dark_moon_greatsword.frost_per_amplifier";
            }

            float frostPerAmplifier = ConfigHelper.getBaselineValue(configKey, 10.0F);
            RegistryEntry.Reference<EntityAttribute> entry = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.FROST);

            if (entry != null && frostPerAmplifier > 0.0F) {
                double frost = attacker.getAttributeValue(entry);
                bonus = (int)Math.floor(frost / frostPerAmplifier);
            }
        }
        mswcompat$ampBonus.set(bonus);
    }

    @Redirect(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z"
            ),
            require = 0
    )
    private boolean mswcompat$addStatusEffectWithFrostBonus(LivingEntity target, StatusEffectInstance original) {
        if (!mswcompat$shouldScale.get()) {
            return target.addStatusEffect(original);
        }

        var type = original.getEffectType();
        int duration = original.getDuration();
        int amp = Math.max(0, original.getAmplifier() + mswcompat$ampBonus.get());
        return target.addStatusEffect(new StatusEffectInstance(type, duration, amp));
    }

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearFrostAmp(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        mswcompat$ampBonus.remove();
        mswcompat$shouldScale.remove();
    }
}