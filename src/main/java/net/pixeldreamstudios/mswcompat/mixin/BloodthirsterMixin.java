package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.soulsweaponry.items.sword.Bloodthirster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(Bloodthirster.class)
public abstract class BloodthirsterMixin {

    @Unique private static final ThreadLocal<Float> mswcompat$scale = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(method = "postHit", at = @At("HEAD"))
    private void mswcompat$cacheScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        float factor = 1.0F;
        if (attacker != null) {
            float adBaseline = ConfigHelper.getBaselineValue("bloodthirster.attack_damage_baseline", 8.0F);

            if (adBaseline > 0.0F) {
                double ad = attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (ad > 0.0) {
                    factor = (float)(ad / adBaseline);
                }
            }
        }

        float minScale = ConfigHelper.getBaselineValue("bloodthirster.heal_min_scale", 0.75F);
        float maxScale = ConfigHelper.getBaselineValue("bloodthirster.heal_max_scale", 1.50F);
        float clamped = Math.max(minScale, Math.min(maxScale, factor));
        mswcompat$scale.set(clamped);
    }

    @Inject(method = "postHit", at = @At("RETURN"))
    private void mswcompat$clearScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        mswcompat$scale.remove();
    }

    @ModifyVariable(method = "postHit", at = @At("STORE"), ordinal = 0)
    private float mswcompat$scaleHealing(float healing) {
        return healing * mswcompat$scale.get();
    }
}