package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.pixeldreamstudios.mswcompat.util.DamageScalingHelper;
import net.pixeldreamstudios.mswcompat.util.SpellPowerHelper;
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
            remap = true
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
                double frost = SpellPowerHelper.getEffectiveSpellPower(user, MSWCompatIdentifiers.SpellPower.FROST);
                damageFactor = DamageScalingHelper.attackDamageAndSecondary(
                        user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
                        ConfigHelper.getBaselineValue("dark_moon_greatsword.attack_damage_baseline", 10.0F),
                        ConfigHelper.getFloatValue("dark_moon_greatsword.attack_damage_weight", 0.5F),
                        frost,
                        ConfigHelper.getBaselineValue("dark_moon_greatsword.frost_baseline", 20.0F),
                        ConfigHelper.getFloatValue("dark_moon_greatsword.frost_weight", 0.5F));

                float frostPerAmp = ConfigHelper.getBaselineValue("dark_moon_greatsword.frost_per_amplifier", 10.0F);
                if (frostPerAmp > 0.0F) {
                    bonusAmp = (int) Math.floor(frost / frostPerAmp);
                }
            } else if (isMasterSword) {
                damageFactor = DamageScalingHelper.factor(
                        user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
                        ConfigHelper.getBaselineValue("master_sword.attack_damage_baseline", 8.0F),
                        ConfigHelper.getFloatValue("master_sword.attack_damage_weight", 0.5F),
                        DamageScalingHelper.Mode.ABOVE_BASELINE,
                        user.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH),
                        ConfigHelper.getBaselineValue("master_sword.max_health_baseline", 40.0F),
                        ConfigHelper.getFloatValue("master_sword.max_health_weight", 0.5F),
                        DamageScalingHelper.Mode.ABOVE_BASELINE);
            } else if (ItemMatcher.isPureMoonlightGreatsword(stack)) {
                damageFactor = DamageScalingHelper.attackDamageOnly(
                        user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
                        ConfigHelper.getBaselineValue("pure_moonlight_greatsword.attack_damage_baseline", 9.0F));
            } else if (ItemMatcher.isBluemoonGreatsword(stack)) {
                damageFactor = DamageScalingHelper.attackDamageOnly(
                        user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
                        ConfigHelper.getBaselineValue("bluemoon_greatsword.attack_damage_baseline", 9.0F));
            } else if (isMoonlight) {
                damageFactor = DamageScalingHelper.attackDamageOnly(
                        user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
                        ConfigHelper.getBaselineValue("moonlight_greatsword.attack_damage_baseline", 9.0F));
            }
        }

        mswcompat$damageScale.set(damageFactor);
        mswcompat$ampBonus.set(bonusAmp);
    }

    @Inject(
            method = "createMoonlightProjectile(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;II)Lnet/soulsweaponry/entity/projectile/MoonlightProjectile;",
            at = @At("RETURN"),
            remap = true
)
    private void mswcompat$scaleProjectile(World world, LivingEntity user, ItemStack stack, int projectileNr, int lvl, CallbackInfoReturnable<MoonlightProjectile> cir) {
        if (!mswcompat$shouldScale.get()) {
            return;
        }

        MoonlightProjectile projectile = cir.getReturnValue();
        if (projectile == null) {
            return;
        }

        projectile.setDamage(projectile.getDamage() * mswcompat$damageScale.get());

        if (mswcompat$shouldScaleAmp.get()) {
            int currentAmp = projectile.getEffectAmplifier();
            projectile.setEffectAmplifier(currentAmp + mswcompat$ampBonus.get());
        }
    }

    @Inject(
            method = "shootMoonlightProjectiles(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V",
            at = @At("TAIL"),
            remap = true
)
    private void mswcompat$clearScale(World world, LivingEntity user, ItemStack stack, CallbackInfo ci) {
        mswcompat$damageScale.remove();
        mswcompat$ampBonus.remove();
        mswcompat$shouldScale.remove();
        mswcompat$shouldScaleAmp.remove();
    }
}