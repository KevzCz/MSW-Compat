package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.soulsweaponry.items.BladeDanceItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(value = BladeDanceItem.class)
public abstract class BladeDanceAoEDamageScalingMixin {
    @Unique private static final float mswcompat$AD_BASE = 8.0F;
    @Unique private static final float mswcompat$AS_BASE = 1.3F;

    @Unique
    private static float mswcompat$factorFromSource(DamageSource src) {
        Entity atk = src.getAttacker();
        if (!(atk instanceof LivingEntity user)) return 1.0F;
        double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        double as = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED);
        float adPart = mswcompat$AD_BASE > 0.0F ? (float)(ad / mswcompat$AD_BASE) : 1.0F;
        float asPart = mswcompat$AS_BASE > 0.0F ? (float)(as / mswcompat$AS_BASE) : 1.0F;
        return 1.0F + 0.75F * (adPart - 1.0F) + 0.75F * (asPart - 1.0F);
    }

    @Redirect(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            ),
            require = 0
    )
    private boolean mswcompat$scaleAoESweep(LivingEntity target, DamageSource source, float amount) {
        return target.damage(source, amount * mswcompat$factorFromSource(source));
    }
}
