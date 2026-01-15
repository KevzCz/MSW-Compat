package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.items.abilities.customarrows.ThirdShotTrue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin( value = ThirdShotTrue.class, remap = false )
public abstract class ThirdShotTrueMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$trueDamageBonus = ThreadLocal.withInitial(() -> 0.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "getModifiedProjectile(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/projectile/PersistentProjectileEntity;)Lnet/minecraft/entity/projectile/PersistentProjectileEntity;",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheBonus(World world, ItemStack bowStack, ItemStack arrowStack, LivingEntity shooter, PersistentProjectileEntity originalArrow, CallbackInfoReturnable<PersistentProjectileEntity> cir) {
        boolean shouldScale = ItemMatcher.isAnyKrakenSlayer(bowStack);
        mswcompat$shouldScale.set(shouldScale);

        if (! shouldScale) {
            mswcompat$trueDamageBonus.set(0.0F);
            return;
        }

        float bonus = 0.0F;
        if (shooter != null) {
            RegistryEntry.Reference<EntityAttribute> rangedAttr = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.RangedWeapon.DAMAGE);
            if (rangedAttr != null) {
                double rangedDamage = shooter.getAttributeValue(rangedAttr);
                float baseline = ItemMatcher.isKrakenSlayerCrossbow(bowStack) ?
                        ConfigHelper.getBaselineValue("kraken_slayer_crossbow.ranged_damage_baseline", 9.0F) :
                        ConfigHelper.getBaselineValue("kraken_slayer_bow.ranged_damage_baseline", 7.0F);
                float damagePerBonus = ConfigHelper.getFloatValue("kraken_slayer.damage_per_true_damage_bonus", 5.0F);

                if (rangedDamage > baseline && damagePerBonus > 0.0F) {
                    bonus = (float)Math.floor((rangedDamage - baseline) / damagePerBonus);
                }
            }
        }

        mswcompat$trueDamageBonus.set(bonus);
    }

    @ModifyVariable(
            method = "getModifiedProjectile(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/projectile/PersistentProjectileEntity;)Lnet/minecraft/entity/projectile/PersistentProjectileEntity;",
            at = @At(value = "INVOKE", target = "Lnet/soulsweaponry/entity/projectile/arrow/TrueDamageArrow;setTrueDamage(F)V"),
            ordinal = 0,
            require = 0
    )
    private float mswcompat$addTrueDamageBonus(float trueDamage) {
        if (! mswcompat$shouldScale.get()) {
            return trueDamage;
        }
        return trueDamage + mswcompat$trueDamageBonus.get();
    }

    @Inject(
            method = "getModifiedProjectile(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/projectile/PersistentProjectileEntity;)Lnet/minecraft/entity/projectile/PersistentProjectileEntity;",
            at = @At("RETURN"),
            require = 0
    )
    private void mswcompat$clearBonus(World world, ItemStack bowStack, ItemStack arrowStack, LivingEntity shooter, PersistentProjectileEntity originalArrow, CallbackInfoReturnable<PersistentProjectileEntity> cir) {
        mswcompat$trueDamageBonus.remove();
        mswcompat$shouldScale.remove();
    }
}