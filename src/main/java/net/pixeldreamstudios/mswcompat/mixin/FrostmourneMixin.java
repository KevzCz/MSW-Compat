package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.items.sword.Frostmourne;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(Frostmourne.class)
public abstract class FrostmourneMixin {
    @Unique private static final ThreadLocal<Integer> mswcompat$ampBonus = ThreadLocal.withInitial(() -> 0);

    @Inject(method = "postHit", at = @At("HEAD"))
    private void mswcompat$cacheFrostAmp(ItemStack stack, LivingEntity target, LivingEntity attacker,
                                         CallbackInfoReturnable<Boolean> cir) {
        int bonus = 0;
        if (attacker != null) {
            float frostPerAmplifier = ConfigHelper.getBaselineValue("frostmourne.frost_per_amplifier", 10.0F);

            RegistryEntry.Reference<EntityAttribute> entry = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.FROST);

            if (entry != null && frostPerAmplifier > 0.0F) {
                double frost = attacker.getAttributeValue(entry);
                bonus = (int)Math.floor(frost / frostPerAmplifier);
            }
        }
        mswcompat$ampBonus.set(bonus);
    }

    @Inject(method = "postHit", at = @At("TAIL"))
    private void mswcompat$clearFrostAmp(ItemStack stack, LivingEntity target, LivingEntity attacker,
                                         CallbackInfoReturnable<Boolean> cir) {
        mswcompat$ampBonus.remove();
    }

    @Redirect(
            method = "postHit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z"
            )
    )
    private boolean mswcompat$addStatusEffectWithFrostBonus(LivingEntity target, StatusEffectInstance original,
                                                            ItemStack stack, LivingEntity tgt, LivingEntity attacker) {
        var type = original.getEffectType();
        int duration = original.getDuration();
        int amp = Math.max(0, original.getAmplifier() + mswcompat$ampBonus.get());
        return target.addStatusEffect(new StatusEffectInstance(type, duration, amp));
    }
}