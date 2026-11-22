package net.pixeldreamstudios.mswcompat.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.soulsweaponry.items.scythe.DarkinScythePrime;
import net.soulsweaponry.items.scythe.ShadowAssassinScythe;

public final class AbilityScales {

    private static final float NEUTRAL = 1.0F;
    private static final float NO_BONUS = 0.0F;
    private static final double NO_RAGE = 0.0D;
    private static final float ARCANE_STEP = 10.0F;

    private AbilityScales() {
    }

    private static double rage(LivingEntity user) {
        RegistryEntry.Reference<EntityAttribute> entry = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.MoreRPGLibrary.RAGE);
        return entry == null ? NO_RAGE : user.getAttributeValue(entry);
    }

    public static float bloodlust(LivingEntity user) {
        return DamageScalingHelper.multiplicativeAboveBaselineAdAndRageFromZero(
                user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
                ConfigHelper.getBaselineValue("bloodlust.attack_damage_baseline", 7.0F),
                rage(user),
                ConfigHelper.getBaselineValue("bloodlust.rage_baseline", 100.0F));
    }

    public static float bloodlustSelfDamage(LivingEntity user, float baseAmount, float factor) {
        float capHearts = ConfigHelper.getBaselineValue("bloodlust.self_damage_cap_hearts", 12.0F);
        float capHealthPercent = ConfigHelper.getBaselineValue("bloodlust.self_damage_cap_health_percent", 0.5F);
        return Math.min(baseAmount * factor, Math.min(capHearts, user.getMaxHealth() * capHealthPercent));
    }

    public static float heapOfRawIron(LivingEntity user) {
        return DamageScalingHelper.multiplicativeAboveBaselineAdAndRageFromZero(
                user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
                ConfigHelper.getBaselineValue("heap_of_raw_iron.attack_damage_baseline", 12.0F),
                rage(user),
                ConfigHelper.getBaselineValue("heap_of_raw_iron.rage_baseline", 20.0F));
    }

    public static float galeforce(LivingEntity user) {
        double ranged = NO_RAGE;
        RegistryEntry.Reference<EntityAttribute> rangedAttr = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.RangedWeapon.DAMAGE);
        if (rangedAttr != null) {
            ranged = user.getAttributeValue(rangedAttr);
        }
        return DamageScalingHelper.attackDamageAndSecondary(
                ranged, ConfigHelper.getBaselineValue("galeforce.ranged_damage_baseline", 9.0F), ConfigHelper.getFloatValue("galeforce.ranged_weight", 0.7F),
                SpellPowerHelper.getEffectiveSpellPower(user, MSWCompatIdentifiers.MoreRPGLibrary.AIR),
                ConfigHelper.getBaselineValue("galeforce.air_baseline", 20.0F), ConfigHelper.getFloatValue("galeforce.air_weight", 0.3F));
    }

    public static boolean scalesSmallMoonlight(ItemStack stack) {
        return ItemMatcher.isMoonlightShortsword(stack) || ItemMatcher.isBluemoonShortsword(stack);
    }

    public static float smallMoonlight(LivingEntity user, ItemStack stack) {
        String key = ItemMatcher.isMoonlightShortsword(stack) ? "moonlight_shortsword.attack_damage_baseline" : "bluemoon_shortsword.attack_damage_baseline";
        return DamageScalingHelper.attackDamageOnly(user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE), ConfigHelper.getBaselineValue(key, 8.0F));
    }

    public static float draupnir(LivingEntity user) {
        return DamageScalingHelper.attackDamageOnly(user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
                ConfigHelper.getBaselineValue("draupnir_spear.attack_damage_baseline", 8.0F));
    }

    public static float draupnirExplosion(float factor) {
        return Math.min(factor, ConfigHelper.getFloatValue("draupnir_spear.explosion_size_max_multiplier", 5.0F));
    }

    public static float dragonslayerStomp(LivingEntity user) {
        float lightningBaseline = ConfigHelper.getBaselineValue("dragonslayer_swordspear.lightning_baseline", 20.0F);
        float waterBaseline = ConfigHelper.getBaselineValue("dragonslayer_swordspear.water_baseline", 20.0F);
        float lightningWeight = ConfigHelper.getFloatValue("dragonslayer_swordspear.lightning_weight", 0.5F);
        float waterWeight = ConfigHelper.getFloatValue("dragonslayer_swordspear.water_weight", 0.5F);
        double lightning = SpellPowerHelper.getEffectiveSpellPower(user, MSWCompatIdentifiers.SpellPower.LIGHTNING);
        double water = SpellPowerHelper.getEffectiveSpellPower(user, MSWCompatIdentifiers.MoreRPGLibrary.WATER);
        float lightningPart = lightningBaseline > NO_BONUS ? (float) (lightning / lightningBaseline) : NO_BONUS;
        float waterPart = waterBaseline > NO_BONUS ? (float) (water / waterBaseline) : NO_BONUS;
        return NEUTRAL + lightningWeight * lightningPart + waterWeight * waterPart;
    }

    public static int dragonslayerLightning(LivingEntity user) {
        double lightning = SpellPowerHelper.getEffectiveSpellPower(user, MSWCompatIdentifiers.SpellPower.LIGHTNING);
        return (int) Math.floor(lightning * ConfigHelper.getFloatValue("dragonslayer_swordspear.lightning_per_spell_power", 0.1F));
    }

    public static float nightsEdge(LivingEntity user) {
        return DamageScalingHelper.attackDamageAndSecondary(
                user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
                ConfigHelper.getBaselineValue("nights_edge.attack_damage_baseline", 10.0F),
                ConfigHelper.getFloatValue("nights_edge.attack_damage_weight", 0.5F),
                SpellPowerHelper.getEffectiveSpellPower(user, MSWCompatIdentifiers.SpellPower.ARCANE),
                ConfigHelper.getBaselineValue("nights_edge.arcane_baseline", 20.0F),
                ConfigHelper.getFloatValue("nights_edge.arcane_weight", 0.5F));
    }

    public static float dragonStaffAura(LivingEntity user) {
        float arcane = (float) SpellPowerHelper.getEffectiveSpellPower(user, MSWCompatIdentifiers.SpellPower.ARCANE);
        return arcane / ARCANE_STEP * ConfigHelper.getBaselineValue("dragon_staff.aura_amplifier_per_10_arcane", 1.0F);
    }

    public static int tonitrusAmpBonus(LivingEntity user) {
        float ampPer = ConfigHelper.getBaselineValue("tonitrus.amplifier_per_lightning", 20.0F);
        if (ampPer <= NO_BONUS) {
            return 0;
        }
        return (int) (SpellPowerHelper.getEffectiveSpellPower(user, MSWCompatIdentifiers.SpellPower.LIGHTNING) / ampPer);
    }

    public static float umbralTrespass(ItemStack stack, LivingEntity user) {
        float baseline = NO_BONUS;
        Item item = stack.getItem();
        if (item instanceof DarkinScythePrime) {
            baseline = ConfigHelper.getBaselineValue("umbral_trespass.darkin_scythe_baseline", 12.0F);
        } else if (item instanceof ShadowAssassinScythe) {
            baseline = ConfigHelper.getBaselineValue("umbral_trespass.shadow_assassin_scythe_baseline", 13.0F);
        }
        return DamageScalingHelper.attackDamageOnly(user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE), baseline);
    }
}
