package net.pixeldreamstudios.mswcompat.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;

public final class ProjectileScalingHelper {

    private ProjectileScalingHelper() {}

    public static float moonlightDamageFactor(LivingEntity owner) {
        if (owner == null) {
            return 1.0F;
        }
        float main = factorForMoonlightStack(owner, owner.getMainHandStack());
        float off = factorForMoonlightStack(owner, owner.getOffHandStack());
        return Math.max(main, off);
    }

    private static float factorForMoonlightStack(LivingEntity owner, ItemStack stack) {
        if (stack.isEmpty()) {
            return 1.0F;
        }

        double ad = owner.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

        if (ItemMatcher.isDarkMoonGreatsword(stack)) {
            return DamageScalingHelper.attackDamageAndSecondary(
                    ad,
                    ConfigHelper.getBaselineValue("dark_moon_greatsword.attack_damage_baseline", 10.0F),
                    ConfigHelper.getFloatValue("dark_moon_greatsword.attack_damage_weight", 0.5F),
                    SpellPowerHelper.getEffectiveSpellPower(owner, MSWCompatIdentifiers.SpellPower.FROST),
                    ConfigHelper.getBaselineValue("dark_moon_greatsword.frost_baseline", 20.0F),
                    ConfigHelper.getFloatValue("dark_moon_greatsword.frost_weight", 0.5F));
        }
        if (ItemMatcher.isMasterSword(stack)) {
            return DamageScalingHelper.factor(
                    ad,
                    ConfigHelper.getBaselineValue("master_sword.attack_damage_baseline", 8.0F),
                    ConfigHelper.getFloatValue("master_sword.attack_damage_weight", 0.5F),
                    DamageScalingHelper.Mode.ABOVE_BASELINE,
                    owner.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH),
                    ConfigHelper.getBaselineValue("master_sword.max_health_baseline", 40.0F),
                    ConfigHelper.getFloatValue("master_sword.max_health_weight", 0.5F),
                    DamageScalingHelper.Mode.ABOVE_BASELINE);
        }
        if (ItemMatcher.isPureMoonlightGreatsword(stack)) {
            return DamageScalingHelper.attackDamageOnly(
                    ad, ConfigHelper.getBaselineValue("pure_moonlight_greatsword.attack_damage_baseline", 9.0F));
        }
        if (ItemMatcher.isBluemoonGreatsword(stack)) {
            return DamageScalingHelper.attackDamageOnly(
                    ad, ConfigHelper.getBaselineValue("bluemoon_greatsword.attack_damage_baseline", 9.0F));
        }
        if (ItemMatcher.isMoonlightGreatsword(stack)) {
            return DamageScalingHelper.attackDamageOnly(
                    ad, ConfigHelper.getBaselineValue("moonlight_greatsword.attack_damage_baseline", 9.0F));
        }
        if (ItemMatcher.isMoonlightShortsword(stack)) {
            return DamageScalingHelper.attackDamageOnly(
                    ad, ConfigHelper.getBaselineValue("moonlight_shortsword.attack_damage_baseline", 8.0F));
        }
        if (ItemMatcher.isBluemoonShortsword(stack)) {
            return DamageScalingHelper.attackDamageOnly(
                    ad, ConfigHelper.getBaselineValue("bluemoon_shortsword.attack_damage_baseline", 8.0F));
        }
        return 1.0F;
    }

    public static float arrowStormDamageFactor(LivingEntity owner) {
        if (!(owner instanceof net.minecraft.entity.player.PlayerEntity player)) {
            return 1.0F;
        }

        float rangedBaseline = ConfigHelper.getBaselineValue("darkmoon_longbow.ranged_damage_baseline", 9.0F);
        float arcaneBaseline = ConfigHelper.getBaselineValue("darkmoon_longbow.arcane_baseline", 20.0F);
        float rangedWeight = ConfigHelper.getFloatValue("darkmoon_longbow.ranged_damage_weight", 0.5F);
        float arcaneWeight = ConfigHelper.getFloatValue("darkmoon_longbow.arcane_weight", 0.5F);

        double ranged = 0.0;
        double arcane = SpellPowerHelper.getEffectiveSpellPower(player, MSWCompatIdentifiers.SpellPower.ARCANE);

        RegistryEntry.Reference<EntityAttribute> rangedAttr =
                AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.RangedWeapon.DAMAGE);
        if (rangedAttr != null) {
            ranged = player.getAttributeValue(rangedAttr);
        }

        return DamageScalingHelper.attackDamageAndSecondary(
                ranged, rangedBaseline, rangedWeight, arcane, arcaneBaseline, arcaneWeight);
    }

    public static float leviathanThrownFactor(LivingEntity owner) {
        if (owner == null) {
            return 1.0F;
        }
        return DamageScalingHelper.attackDamageAndSecondary(
                owner.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
                ConfigHelper.getBaselineValue("leviathan_axe.attack_damage_baseline", 10.0F),
                ConfigHelper.getFloatValue("leviathan_axe.attack_damage_weight", 0.5F),
                SpellPowerHelper.getEffectiveSpellPower(owner, MSWCompatIdentifiers.SpellPower.FROST),
                ConfigHelper.getBaselineValue("leviathan_axe.frost_baseline", 20.0F),
                ConfigHelper.getFloatValue("leviathan_axe.frost_weight", 0.5F));
    }
}
