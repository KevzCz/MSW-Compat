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
import net.pixeldreamstudios.mswcompat.util.SpellPowerHelper;
import net.soulsweaponry.items.abilities.customarrows.GaleArrows;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin( value = GaleArrows.class, remap = false )
public abstract class GaleArrowsMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$damageScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "getModifiedProjectile(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/projectile/PersistentProjectileEntity;)Lnet/minecraft/entity/projectile/PersistentProjectileEntity;",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheScale(World world, ItemStack bowStack, ItemStack arrowStack, LivingEntity shooter, PersistentProjectileEntity originalArrow, CallbackInfoReturnable<PersistentProjectileEntity> cir) {
        boolean shouldScale = ItemMatcher.isGaleforce(bowStack);
        mswcompat$shouldScale.set(shouldScale);

        if (! shouldScale) {
            mswcompat$damageScale.set(1.0F);
            return;
        }

        float factor = 1.0F;
        if (shooter != null) {
            float rangedBaseline = ConfigHelper.getBaselineValue("galeforce.ranged_damage_baseline", 9.0F);
            float airBaseline = ConfigHelper.getBaselineValue("galeforce.air_baseline", 20.0F);
            float rangedWeight = ConfigHelper.getFloatValue("galeforce.ranged_weight", 0.7F);
            float airWeight = ConfigHelper.getFloatValue("galeforce.air_weight", 0.3F);

            double ranged = 0.0;
            double air = SpellPowerHelper.getEffectiveSpellPower(shooter, MSWCompatIdentifiers.MoreRPGLibrary.AIR);

            RegistryEntry.Reference<EntityAttribute> rangedAttr = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.RangedWeapon.DAMAGE);
            if (rangedAttr != null) {
                ranged = shooter.getAttributeValue(rangedAttr);
            }

            float rangedPart = rangedBaseline > 0.0F ? (float)(ranged / rangedBaseline) : 1.0F;
            float airPart = airBaseline > 0.0F ? (float)(air / airBaseline) : 0.0F;

            factor = 1.0F + rangedWeight * (rangedPart - 1.0F) + airWeight * airPart;
        }

        mswcompat$damageScale.set(factor);
    }

    @Inject(
            method = "getModifiedProjectile(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/projectile/PersistentProjectileEntity;)Lnet/minecraft/entity/projectile/PersistentProjectileEntity;",
            at = @At("RETURN"),
            require = 0
    )
    private void mswcompat$scaleArrowDamage(World world, ItemStack bowStack, ItemStack arrowStack, LivingEntity shooter, PersistentProjectileEntity originalArrow, CallbackInfoReturnable<PersistentProjectileEntity> cir) {
        if (!mswcompat$shouldScale.get()) {
            return;
        }

        PersistentProjectileEntity arrow = cir.getReturnValue();
        if (arrow != null) {
            double baseDamage = arrow.getDamage();
            arrow.setDamage(baseDamage * mswcompat$damageScale.get());
        }
    }

    @Inject(
            method = "getModifiedProjectile(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/projectile/PersistentProjectileEntity;)Lnet/minecraft/entity/projectile/PersistentProjectileEntity;",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearScale(World world, ItemStack bowStack, ItemStack arrowStack, LivingEntity shooter, PersistentProjectileEntity originalArrow, CallbackInfoReturnable<PersistentProjectileEntity> cir) {
        mswcompat$damageScale.remove();
        mswcompat$shouldScale.remove();
    }
}