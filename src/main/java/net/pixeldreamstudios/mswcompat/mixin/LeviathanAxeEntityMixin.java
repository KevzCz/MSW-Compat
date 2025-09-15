package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;
import net.soulsweaponry.entity.projectile.LeviathanAxeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(LeviathanAxeEntity.class)
public abstract class LeviathanAxeEntityMixin {
    @Unique private static final Identifier FROST_ID = Identifier.of("spell_power", "frost");
    @Unique private static final ThreadLocal<Float> DAMAGE_FACTOR = ThreadLocal.withInitial(() -> 1.0F);

    /* Cache factor before getDamage() */
    @Inject(method = "getDamage", at = @At("HEAD"))
    private void mswcompat$cacheFactor(Entity target, CallbackInfoReturnable<Float> cir) {
        float ad = 0F, frost = 0F;

        LeviathanAxeEntity self = (LeviathanAxeEntity)(Object)this;
        Entity owner = self.getOwner();
        if (owner instanceof LivingEntity living) {
            ad = (float) living.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE);

            var key   = net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.ATTRIBUTE, FROST_ID);
            var entry = net.minecraft.registry.Registries.ATTRIBUTE.getEntry(key).orElse(null);
            if (entry != null) {
                frost = (float) living.getAttributeValue(entry);
            }
        }

        // factor = 0.5*(AD/10) + 0.5*(Frost/20)
        float factor = 0.5F * (ad / 10.0F) + 0.5F * (frost / 20.0F);
        if (factor < 0F) factor = 0F;
        DAMAGE_FACTOR.set(factor);
    }

    /* Apply factor to returned damage */
    @Inject(method = "getDamage", at = @At("RETURN"), cancellable = true)
    private void mswcompat$scaleThrownDamage(Entity target, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValueF() * DAMAGE_FACTOR.get());
        DAMAGE_FACTOR.remove();
    }

    /* Keep your collide() redirect from earlier if you want the FREEZING amp bonus there */
    @Redirect(
            method = "collide",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z")
    )
    private boolean mswcompat$addStatusEffectWithFrostBonus(LivingEntity target, StatusEffectInstance original) {
        LeviathanAxeEntity self = (LeviathanAxeEntity)(Object)this;
        Entity owner = self.getOwner();

        int bonus = 0;
        if (owner instanceof LivingEntity living) {
            var key   = net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.ATTRIBUTE, FROST_ID);
            var entry = net.minecraft.registry.Registries.ATTRIBUTE.getEntry(key).orElse(null);
            if (entry != null) bonus = (int)Math.floor(living.getAttributeValue(entry) / 10.0);
        }

        net.minecraft.registry.entry.RegistryEntry<StatusEffect> type = original.getEffectType();
        int duration = original.getDuration();
        int amp = Math.max(0, original.getAmplifier() + bonus);
        return target.addStatusEffect(new StatusEffectInstance(type, duration, amp));
    }
}
