package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.entity.effect.StatusEffectInstance;
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
    @Unique private static final Identifier FROST_ID = Identifier.of("spell_power", "frost");
    @Unique private static final ThreadLocal<Integer> AMP_BONUS = ThreadLocal.withInitial(() -> 0);
    @Inject(method = "postHit", at = @At("HEAD"))
    private void mswcompat$cacheFrostAmp(ItemStack stack, LivingEntity target, LivingEntity attacker,
                                         CallbackInfoReturnable<Boolean> cir) {
        int bonus = 0;
        if (attacker != null) {
            RegistryKey<EntityAttribute> key = RegistryKey.of(RegistryKeys.ATTRIBUTE, FROST_ID);
            RegistryEntry<EntityAttribute> entry = Registries.ATTRIBUTE.getEntry(key).orElse(null);
            if (entry == null) {
                EntityAttribute attr = Registries.ATTRIBUTE.get(FROST_ID);
                if (attr != null) entry = Registries.ATTRIBUTE.getEntry(attr);
            }
            if (entry != null) {
                double frost = attacker.getAttributeValue(entry);
                bonus = (int)Math.floor(frost / 10.0);
            }
        }
        AMP_BONUS.set(bonus);
    }

    @Inject(method = "postHit", at = @At("TAIL"))
    private void mswcompat$clearFrostAmp(ItemStack stack, LivingEntity target, LivingEntity attacker,
                                         CallbackInfoReturnable<Boolean> cir) {
        AMP_BONUS.remove();
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
        int amp = Math.max(0, original.getAmplifier() + AMP_BONUS.get());
        return target.addStatusEffect(new StatusEffectInstance(type, duration, amp));
    }
}
