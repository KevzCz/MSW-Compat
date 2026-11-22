package net.pixeldreamstudios.mswcompat.mixin.living;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.soulsweaponry.entitydata.UmbralTrespassData;
import net.soulsweaponry.registry.EffectRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class LivingTrespassMixin {

    @Unique
    private static final int mswcompat$NO_AMPLIFIER = 0;

    @Inject(method = "tickRiding", at = @At("TAIL"))
    private void mswcompat$trespassRiding(CallbackInfo ci) {
        if (!((Object) this instanceof LivingEntity user) || user instanceof PlayerEntity || user.getWorld().isClient()
                || !UmbralTrespassData.shouldDamageRiding(user)) {
            return;
        }
        user.addStatusEffect(new StatusEffectInstance(EffectRegistry.COOLDOWN, UmbralTrespassData.getAbilityCooldown(user), mswcompat$NO_AMPLIFIER));
        if (!user.hasStatusEffect(EffectRegistry.GHOSTLY)) {
            user.stopRiding();
        }
    }
}
