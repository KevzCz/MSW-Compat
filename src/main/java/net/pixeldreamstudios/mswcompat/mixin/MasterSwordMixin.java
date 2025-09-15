package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import net.soulsweaponry.items.sword.MasterSword;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(MasterSword.class)
public abstract class MasterSwordMixin {
    @Unique private static final float mswcompat$AD_BASELINE  = 8.0F;   // requested baseline
    @Unique private static final float mswcompat$HP_BASELINE  = 40.0F;  // 20 hearts = 40 HP
    @Unique private static final ThreadLocal<Float> mswcompat$factor = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("HEAD")
    )
    private void mswcompat$cacheScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        float factor = 1.0F;
        if (user != null) {
            // These are already RegistryEntry<EntityAttribute> in 1.21.1
            RegistryEntry<EntityAttribute> adEntry = EntityAttributes.GENERIC_ATTACK_DAMAGE;
            RegistryEntry<EntityAttribute> hpEntry = EntityAttributes.GENERIC_MAX_HEALTH;

            double ad = user.getAttributeValue(adEntry);
            double hp = user.getAttributeValue(hpEntry);

            float adPart = mswcompat$AD_BASELINE > 0.0F ? (float)(ad / mswcompat$AD_BASELINE) : 1.0F;
            float hpPart = mswcompat$HP_BASELINE > 0.0F ? (float)(hp / mswcompat$HP_BASELINE) : 1.0F;

            factor = 0.5F * adPart + 0.5F * hpPart;
            if (factor < 0.0F) factor = 0.0F;
        }
        mswcompat$factor.set(factor);
    }

    @ModifyArg(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/MoonlightProjectile;setDamage(D)V"
            ),
            index = 0
    )
    private double mswcompat$scaleProjectileDamage(double baseDamage) {
        return baseDamage * mswcompat$factor.get();
    }

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("TAIL")
    )
    private void mswcompat$clearScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        mswcompat$factor.remove();
    }
}
