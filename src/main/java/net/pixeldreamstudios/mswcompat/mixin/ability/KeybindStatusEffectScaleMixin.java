package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.pixeldreamstudios.mswcompat.util.MswCompatScaleContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class KeybindStatusEffectScaleMixin {

    @ModifyArg(
            method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z",
                    remap = true
            ),
            index = 0,
            remap = true
    )
    private StatusEffectInstance mswcompat$scaleKeybindStatusEffect(StatusEffectInstance instance) {
        if (!MswCompatScaleContext.isKeybindActive()) {
            return instance;
        }

        float f = MswCompatScaleContext.keybindFactor();
        int scaledAmp = Math.max(0, (int) Math.floor((instance.getAmplifier() + 1) * f) - 1);
        return new StatusEffectInstance(
                instance.getEffectType(),
                instance.getDuration(),
                scaledAmp,
                instance.isAmbient(),
                instance.shouldShowParticles(),
                instance.shouldShowIcon()
        );
    }
}
