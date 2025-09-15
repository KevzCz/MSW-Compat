package net.pixeldreamstudios.mswcompat.mixin;

import net.soulsweaponry.items.sword.BluemoonShortsword;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = BluemoonShortsword.class, remap = false) // disable remapping for 3rd-party mod class
public abstract class BluemoonShortswordMixin {

    @Unique
    private static final float mswcompat$BASELINE_ATTACK_DAMAGE = 7.0F;

    // Explicit descriptor avoids surprises: ()F
    @Inject(method = "getProjectileDamage()F", at = @At("RETURN"), cancellable = true, require = 0)
    private void mswcompat$scaleProjectileDamage(CallbackInfoReturnable<Float> cir) {
        float base = cir.getReturnValueF();

        // getAttackDamage() is defined on the SoulsWeaponry sword hierarchy
        float weaponAd = ((BluemoonShortsword) (Object) this).getAttackDamage();

        if (weaponAd > 0.0F) {
            float factor = weaponAd / mswcompat$BASELINE_ATTACK_DAMAGE;
            cir.setReturnValue(base * factor);
        }
    }
}
