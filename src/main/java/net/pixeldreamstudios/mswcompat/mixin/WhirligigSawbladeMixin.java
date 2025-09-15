package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.items.sword.WhirligigSawblade;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(WhirligigSawblade.class)
public abstract class WhirligigSawbladeMixin {
    @Unique
    private static float mswcompat$scale(LivingEntity user) {
        if (user == null) return 1.0F;
        double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        float baseline = (float) ConfigConstructor.whirligig_sawblade_damage;
        if (ad <= 0.0 || baseline <= 0.0F) return 1.0F;
        return (float)(ad / baseline);
    }

    @Redirect(
            method = "usageTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            )
    )
    private boolean mswcompat$scaleAbilityDamageOnly(LivingEntity target,
                                                     DamageSource source,
                                                     float originalAmount,
                                                     World world,
                                                     LivingEntity user,
                                                     ItemStack stack,
                                                     int remainingUseTicks) {
        float factor = mswcompat$scale(user);
        float ability = (float) ConfigConstructor.whirligig_sawblade_ability_damage * factor;
        float ench = 0.0F;
        if (world instanceof ServerWorld serverWorld) {
            ench = EnchantmentHelper.getDamage(serverWorld, stack, target, source, 0.0F);
        }
        float newAmount = ability + ench;
        return target.damage(source, newAmount);
    }
}
