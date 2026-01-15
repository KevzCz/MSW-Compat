package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.entity.projectile.MoonlightProjectile;
import net.soulsweaponry.items.abilities.stoppedusing.ShootMoonlight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin( value = ShootMoonlight.class, remap = false )
public abstract class ShootMoonlightMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$damageScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Integer> mswcompat$ampBonus = ThreadLocal.withInitial(() -> 0);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScaleAmp = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "shootMoonlightProjectiles(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheScale(World world, LivingEntity user, ItemStack stack, CallbackInfo ci) {
        boolean isDarkMoon = ItemMatcher.isDarkMoonGreatsword(stack);
        boolean isMasterSword = ItemMatcher.isMasterSword(stack);
        boolean isMoonlight = ItemMatcher.isMoonlightGreatsword(stack) || ItemMatcher.isPureMoonlightGreatsword(stack) || ItemMatcher.isBluemoonGreatsword(stack);
        boolean shouldScale = isDarkMoon || isMasterSword || isMoonlight;

        mswcompat$shouldScale.set(shouldScale);
        mswcompat$shouldScaleAmp.set(isDarkMoon);

        if (! shouldScale) {
            mswcompat$damageScale.set(1.0F);
            mswcompat$ampBonus.set(0);
            return;
        }

        float damageFactor = 1.0F;
        int bonusAmp = 0;

        if (user != null) {
            if (isDarkMoon) {
                float adBaseline = ConfigHelper.getBaselineValue("dark_moon_greatsword.attack_damage_baseline", 10.0F);
                float frostBaseline = ConfigHelper.getBaselineValue("dark_moon_greatsword.frost_baseline", 20.0F);
                float adWeight = ConfigHelper.getFloatValue("dark_moon_greatsword.attack_damage_weight", 0.5F);
                float frostWeight = ConfigHelper.getFloatValue("dark_moon_greatsword.frost_weight", 0.5F);

                double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                double frost = 0.0;

                RegistryEntry.Reference<EntityAttribute> frostEntry = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.FROST);
                if (frostEntry != null) {
                    frost = user.getAttributeValue(frostEntry);
                }

                float adPart = adBaseline > 0.0F ? (float)(ad / adBaseline) : 1.0F;
                float frostPart = frostBaseline > 0.0F ? (float)(frost / frostBaseline) : 0.0F;

                damageFactor = 1.0F + adWeight * (adPart - 1.0F) + frostWeight * frostPart;

                float frostPerAmp = ConfigHelper.getBaselineValue("dark_moon_greatsword.frost_per_amplifier", 10.0F);
                if (frostPerAmp > 0.0F) {
                    bonusAmp = (int)Math.floor(frost / frostPerAmp);
                }
            } else if (isMasterSword) {
                float adBaseline = ConfigHelper.getBaselineValue("master_sword.attack_damage_baseline", 8.0F);
                float hpBaseline = ConfigHelper.getBaselineValue("master_sword.max_health_baseline", 40.0F);
                float adWeight = ConfigHelper.getFloatValue("master_sword.attack_damage_weight", 0.5F);
                float hpWeight = ConfigHelper.getFloatValue("master_sword.max_health_weight", 0.5F);

                double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                double hp = user.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);

                float adPart = adBaseline > 0.0F ? (float)(ad / adBaseline) : 1.0F;
                float hpPart = hpBaseline > 0.0F ? (float)(hp / hpBaseline) : 1.0F;

                damageFactor = adWeight * adPart + hpWeight * hpPart;
            } else if (isMoonlight) {
                float adBaseline = ConfigHelper.getBaselineValue("moonlight_greatsword.attack_damage_baseline", 9.0F);

                if (adBaseline > 0.0F) {
                    double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                    if (ad > 0.0) {
                        damageFactor = (float)(ad / adBaseline);
                    }
                }
            }
        }

        mswcompat$damageScale.set(Math.max(0.0F, damageFactor));
        mswcompat$ampBonus.set(bonusAmp);
    }

    @Inject(
            method = "createMoonlightProjectile(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;II)Lnet/soulsweaponry/entity/projectile/MoonlightProjectile;",
            at = @At("RETURN"),
            require = 0
    )
    private void mswcompat$scaleProjectile(World world, LivingEntity user, ItemStack stack, int projectileNr, int lvl, CallbackInfoReturnable<MoonlightProjectile> cir) {
        if (! mswcompat$shouldScale.get()) {
            return;
        }

        MoonlightProjectile projectile = cir.getReturnValue();
        if (projectile != null) {
            double baseDamage = projectile.getDamage();
            projectile.setDamage(baseDamage * mswcompat$damageScale.get());

            if (mswcompat$shouldScaleAmp.get()) {
                int currentAmp = projectile.getEffectAmplifier();
                projectile.setEffectAmplifier(currentAmp + mswcompat$ampBonus.get());
            }
        }
    }

    @Inject(
            method = "shootMoonlightProjectiles(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearScale(World world, LivingEntity user, ItemStack stack, CallbackInfo ci) {
        mswcompat$damageScale.remove();
        mswcompat$ampBonus.remove();
        mswcompat$shouldScale.remove();
        mswcompat$shouldScaleAmp.remove();
    }
}