package net.pixeldreamstudios.mswcompat.config;

public class ConfigHelper {

    public static float getBaselineValue(String key, float defaultValue) {
        MSWCompatConfig config = MSWCompatConfig.getInstance();
        switch (key) {
            case "mjolnir.projectile.attack_damage_baseline":
                return config.mjolnir.projectile.attackDamageBaseline;
            case "mjolnir.lightning_baseline":
                return config.mjolnir.lightningBaseline;
            case "mjolnir.water_baseline":
                return config.mjolnir.waterBaseline;
            case "nightfall.attack_damage_baseline":
                return config.nightfall.attackDamageBaseline;
            case "nightfall.soul_baseline":
                return config.nightfall.soulBaseline;
            case "leviathan_axe.attack_damage_baseline":
                return config.leviathan_axe.attackDamageBaseline;
            case "leviathan_axe.frost_baseline":
                return config.leviathan_axe.frostBaseline;
            case "leviathan_axe.frost_per_amplifier":
                return config.leviathan_axe.frostPerAmplifier;
            case "leviathan_axe.ice_explosion.frost_baseline":
                return config.leviathan_axe.ice_explosion.frostBaseline;
            case "lich_bane.fire_baseline":
                return config.lich_bane.fireBaseline;
            case "master_sword.attack_damage_baseline":
                return config.master_sword.attackDamageBaseline;
            case "master_sword.max_health_baseline":
                return config.master_sword.maxHealthBaseline;
            case "dark_moon_greatsword.attack_damage_baseline":
                return config.dark_moon_greatsword.attackDamageBaseline;
            case "dark_moon_greatsword.frost_baseline":
                return config.dark_moon_greatsword.frostBaseline;
            case "dark_moon_greatsword.frost_per_amplifier":
                return config.dark_moon_greatsword.frostPerAmplifier;
            case "dark_moon_greatsword.attack_damage_weight":
                return config.dark_moon_greatsword.attackDamageWeight;
            case "dark_moon_greatsword.frost_weight":
                return config.dark_moon_greatsword.frostWeight;
            case "moonlight_greatsword.attack_damage_baseline":
                return config.moonlight_greatsword.attackDamageBaseline;
            case "bluemoon_greatsword.attack_damage_baseline":
                return config.bluemoon_greatsword.attackDamageBaseline;
            case "pure_moonlight_greatsword.attack_damage_baseline":
                return config.pure_moonlight_greatsword.attackDamageBaseline;
            case "moonlight_shortsword.attack_damage_baseline":
                return config.moonlight_shortsword.attackDamageBaseline;
            case "bluemoon_shortsword.attack_damage_baseline":
                return config.bluemoon_shortsword.attackDamageBaseline;
            case "comet_spear.attack_damage_baseline":
                return config.comet_spear.attackDamageBaseline;
            case "umbral_trespass.darkin_scythe_baseline":
                return config.umbral_trespass.darkinScytheBaseline;
            case "umbral_trespass.shadow_assassin_scythe_baseline":
                return config.umbral_trespass.shadowAssassinScytheBaseline;
            case "wither_skull.soul_baseline":
                return config.wither_skull.soulBaseline;
            case "excalibur.arcane_baseline":
                return config.excalibur.arcaneBaseline;
            case "excalibur.soul_baseline":
                return config.excalibur.soulBaseline;
            case "frostmourne.frost_baseline":
                return config.frostmourne.frostBaseline;
            case "frostmourne.frost_per_amplifier":
                return config.frostmourne.frostPerAmplifier;
            case "nights_edge.attack_damage_baseline":
                return config.nights_edge.attackDamageBaseline;
            case "nights_edge.arcane_baseline":
                return config.nights_edge.arcaneBaseline;
            case "holy_moonlight_greatsword.attack_damage_baseline":
                return config.holy_moonlight_greatsword.attackDamageBaseline;
            case "rime_spectre.soul_baseline":
                return config.rime_spectre.soulBaseline;
            case "rime_spectre.frost_baseline":
                return config.rime_spectre.frostBaseline;
            case "soulmass.soul_baseline":
                return config.soulmass.soulBaseline;
            case "evoker_fangs.soul_baseline":
                return config.evoker_fangs.soulBaseline;
            case "supernova.fire_baseline":
                return config.supernova.fireBaseline;
            case "supernova.flame_pillar_scaling":
                return config.supernova.flamePillarScaling;
            case "supernova.molten_metal_scaling":
                return config.supernova.moltenMetalScaling;
            case "moonveil.attack_damage_baseline":
                return config.moonveil.attackDamageBaseline;
            case "moonveil.rage_baseline":
                return config.moonveil.rageBaseline;
            case "moonveil.bleed_buildup_min_scale":
                return config.moonveil.bleedBuildupMinScale;
            case "moonveil.bleed_buildup_max_scale":
                return config.moonveil.bleedBuildupMaxScale;
            case "dragonbane.lightning_baseline":
                return config.dragonbane.lightningBaseline;
            case "dragon_staff.arcane_baseline":
                return config.dragon_staff.arcaneBaseline;
            case "dragon_staff.heal_cap_multiplier":
                return config.dragon_staff.healCapMultiplier;
            case "dragon_staff.aura_amplifier_per_10_arcane":
                return config.dragon_staff.auraAmplifierPer10Arcane;
            case "darkin_blade.attack_damage_baseline":
                return config.darkin_blade.attackDamageBaseline;
            case "darkin_blade.heal_min_scale":
                return config.darkin_blade.healMinScale;
            case "darkin_blade.heal_max_scale":
                return config.darkin_blade.healMaxScale;
            case "darkin_scythe_prime.attack_damage_baseline":
                return config.darkin_scythe_prime.attackDamageBaseline;
            case "darkin_scythe_prime.heal_min_scale":
                return config.darkin_scythe_prime.healMinScale;
            case "darkin_scythe_prime.heal_max_scale":
                return config.darkin_scythe_prime.healMaxScale;
            case "whirligig_sawblade.attack_damage_baseline":
                return config.whirligig_sawblade.attackDamageBaseline;
            case "whirligig_sawblade.rage_baseline":
                return config.whirligig_sawblade.rageBaseline;
            case "whirligig_sawblade.damage_min_scale":
                return config.whirligig_sawblade.damageMinScale;
            case "whirligig_sawblade.damage_max_scale":
                return config.whirligig_sawblade.damageMaxScale;
            case "whirligig_sawblade.bleed_min_scale":
                return config.whirligig_sawblade.bleedMinScale;
            case "whirligig_sawblade.bleed_max_scale":
                return config.whirligig_sawblade.bleedMaxScale;
            case "tonitrus.lightning_baseline":
                return config.tonitrus.lightningBaseline;
            case "tonitrus.amplifier_per_lightning":
                return config.tonitrus.amplifierPerLightning;
            case "ghost_glaive.attack_damage_baseline":
                return config.ghost_glaive.attackDamageBaseline;
            case "ghost_glaive.arcane_baseline":
                return config.ghost_glaive.arcaneBaseline;
            case "freyr_sword.attack_damage_baseline":
                return config.freyr_sword.attackDamageBaseline;
            case "freyr_sword.soul_baseline":
                return config.freyr_sword.soulBaseline;
            case "dawnbreaker.fire_baseline":
                return config.dawnbreaker.fireBaseline;
            case "empowered_dawnbreaker.fire_baseline":
                return config.empowered_dawnbreaker.fireBaseline;
            case "draupnir_spear.attack_damage_baseline":
                return config.draupnir_spear.attackDamageBaseline;
            case "dragonslayer_swordspear.lightning_baseline":
                return config.dragonslayer_swordspear.lightningBaseline;
            case "dragonslayer_swordspear.water_baseline":
                return config.dragonslayer_swordspear.waterBaseline;
            case "bloodthirster.attack_damage_baseline":
                return config.bloodthirster.attackDamageBaseline;
            case "bloodthirster.rage_baseline":
                return config.bloodthirster.rageBaseline;
            case "bloodthirster.heal_min_scale":
                return config.bloodthirster.healMinScale;
            case "bloodthirster.heal_max_scale":
                return config.bloodthirster.healMaxScale;
            case "bloodthirster.overheal_min_scale":
                return config.bloodthirster.overhealMinScale;
            case "bloodthirster.overheal_max_scale":
                return config.bloodthirster.overhealMaxScale;
            case "blade_dance.attack_damage_baseline":
                return config.blade_dance.attackDamageBaseline;
            case "blade_dance.attack_speed_baseline":
                return config.blade_dance.attackSpeedBaseline;
            case "bloodlust.attack_damage_baseline":
                return config.bloodlust.attackDamageBaseline;
            case "bloodlust.rage_baseline":
                return config.bloodlust.rageBaseline;
            case "bloodlust.self_damage_cap_hearts":
                return config.bloodlust.selfDamageCapHearts;
            case "bloodlust.self_damage_cap_health_percent":
                return config.bloodlust.selfDamageCapHealthPercent;
            case "bloodlust.bleed_buildup_min_scale":
                return config.bloodlust.bleedBuildupMinScale;
            case "bloodlust.bleed_buildup_max_scale":
                return config.bloodlust.bleedBuildupMaxScale;
            case "darkmoon_longbow.ranged_damage_baseline":
                return config.darkmoon_longbow.rangedDamageBaseline;
            case "darkmoon_longbow.arcane_baseline":
                return config.darkmoon_longbow.arcaneBaseline;
            case "galeforce.ranged_damage_baseline":
                return config.galeforce.rangedDamageBaseline;
            case "galeforce.air_baseline":
                return config.galeforce.airBaseline;
            case "heap_of_raw_iron.attack_damage_baseline":
                return config.heap_of_raw_iron.attackDamageBaseline;
            case "heap_of_raw_iron.rage_baseline":
                return config.heap_of_raw_iron.rageBaseline;
            case "kraken_slayer.damage_per_true_damage_bonus":
                return config.kraken_slayer.damagePerTrueDamageBonus;
            case "kraken_slayer_bow.ranged_damage_baseline":
                return config.kraken_slayer_bow.rangedDamageBaseline;
            case "kraken_slayer_crossbow.ranged_damage_baseline":
                return config.kraken_slayer_crossbow.rangedDamageBaseline;
            case "blood_loss.attack_damage_baseline":
                return config.blood_loss.attackDamageBaseline;
            case "blood_loss.rage_baseline":
                return config.blood_loss.rageBaseline;
            case "blood_loss.min_scale":
                return config.blood_loss.minScale;
            case "blood_loss.max_scale":
                return config.blood_loss.maxScale;
            case "nightlords_sword.attack_damage_baseline":
                return config.nightlords_sword.attackDamageBaseline;
            default:
                return defaultValue;
        }
    }

    public static float getScalingFactor(String key, float defaultValue) {
        return defaultValue;
    }

    public static int getIntValue(String key, int defaultValue) {
        return defaultValue;
    }

    public static double getDoubleValue(String key, double defaultValue) {
        MSWCompatConfig config = MSWCompatConfig.getInstance();
        switch (key) {
            case "mjolnir.lightning_spell_power_bonus":
                return config.mjolnir.lightningSpellPowerBonus;
            case "nightfall.soul_spell_power_bonus":
                return config.nightfall.soulSpellPowerBonus;
            case "nightfall.pet_inheritance_bonus":
                return config.nightfall.petInheritanceBonus;
            case "forlorn_scythe.soul_spell_power_bonus":
                return config.forlorn_scythe.soulSpellPowerBonus;
            case "leviathan_axe.frost_spell_power_bonus":
                return config.leviathan_axe.frostSpellPowerBonus;
            case "lich_bane.fire_spell_power_bonus":
                return config.lich_bane.fireSpellPowerBonus;
            case "master_sword.max_health_bonus_percentage":
                return config.master_sword.maxHealthBonusPercentage;
            case "dark_moon_greatsword.frost_spell_power_bonus":
                return config.dark_moon_greatsword.frostSpellPowerBonus;
            case "lightning.damage_per_spell_power":
                return config.lightning.damagePerSpellPower;
            case "excalibur.arcane_spell_power_bonus":
                return config.excalibur.arcaneSpellPowerBonus;
            case "excalibur.soul_spell_power_bonus":
                return config.excalibur.soulSpellPowerBonus;
            case "soul_reaper.soul_spell_power_bonus":
                return config.soul_reaper.soulSpellPowerBonus;
            case "soul_reaper.pet_inheritance_bonus":
                return config.soul_reaper.petInheritanceBonus;
            case "frostmourne.frost_spell_power_bonus":
                return config.frostmourne.frostSpellPowerBonus;
            case "frostmourne.pet_inheritance_bonus":
                return config.frostmourne.petInheritanceBonus;
            case "nights_edge.arcane_spell_power_bonus":
                return config.nights_edge.arcaneSpellPowerBonus;
            case "supernova.fire_spell_power_bonus":
                return config.supernova.fireSpellPowerBonus;
            case "dragonbane.lightning_spell_power_bonus":
                return config.dragonbane.lightningSpellPowerBonus;
            case "dragon_staff.arcane_spell_power_bonus":
                return config.dragon_staff.arcaneSpellPowerBonus;
            case "tonitrus.lightning_spell_power_bonus":
                return config.tonitrus.lightningSpellPowerBonus;
            case "ghost_glaive.arcane_spell_power_bonus":
                return config.ghost_glaive.arcaneSpellPowerBonus;
            case "dawnbreaker.fire_spell_power_bonus":
                return config.dawnbreaker.fireSpellPowerBonus;
            case "empowered_dawnbreaker.fire_spell_power_bonus":
                return config.empowered_dawnbreaker.fireSpellPowerBonus;
            case "darkmoon_longbow.arcane_spell_power_bonus":
                return config.darkmoon_longbow.arcaneSpellPowerBonus;
            case "freyr_sword.soul_spell_power_bonus":
                return config.freyr_sword.soulSpellPowerBonus;
            case "freyr_sword.pet_inheritance_bonus":
                return config.freyr_sword.petInheritanceBonus;
            case "dragonslayer_swordspear.lightning_spell_power_bonus":
                return config.dragonslayer_swordspear.lightningSpellPowerBonus;
            case "nightlords_sword.amp_per_attack_damage":
                return config.nightlords_sword.ampPerAttackDamage;
            default:
                return defaultValue;
        }
    }

    public static boolean getBooleanValue(String key, boolean defaultValue) {
        MSWCompatConfig config = MSWCompatConfig.getInstance();
        switch (key) {
            case "lightning.damage_passive_entities":
                return config.lightning.damagePassiveEntities;
            case "lightning.damage_tamed_entities":
                return config.lightning.damageTamedEntities;
            case "lightning.damage_channeler":
                return config.lightning.damageChanneler;
            case "soul_reaper.use_kevslibrary_pet_inheritance_attribute":
                return config.soul_reaper.useKevslibraryPetInheritanceAttribute;
            case "frostmourne.use_kevslibrary_pet_inheritance_attribute":
                return config.frostmourne.useKevslibraryPetInheritanceAttribute;
            case "nightfall.use_kevslibrary_pet_inheritance_attribute":
                return config.nightfall.useKevslibraryPetInheritanceAttribute;
            case "freyr_sword.use_kevslibrary_pet_inheritance_attribute":
                return config.freyr_sword.useKevslibraryPetInheritanceAttribute;
            default:
                return defaultValue;
        }
    }

    public static String getStringValue(String key, String defaultValue) {
        return defaultValue;
    }

    public static float getFloatValue(String key, float defaultValue) {
        MSWCompatConfig config = MSWCompatConfig.getInstance();
        switch (key) {
            case "lightning.player_damage_multiplier":
                return config.lightning.playerDamageMultiplier;
            case "master_sword.attack_damage_weight":
                return config.master_sword.attackDamageWeight;
            case "master_sword.max_health_weight":
                return config.master_sword.maxHealthWeight;
            case "excalibur.arcane_weight":
                return config.excalibur.arcaneWeight;
            case "excalibur.soul_weight":
                return config.excalibur.soulWeight;
            case "nights_edge.attack_damage_weight":
                return config.nights_edge.attackDamageWeight;
            case "nights_edge.arcane_weight":
                return config.nights_edge.arcaneWeight;
            case "rime_spectre.soul_weight":
                return config.rime_spectre.soulWeight;
            case "rime_spectre.frost_weight":
                return config.rime_spectre.frostWeight;
            case "leviathan_axe.attack_damage_weight":
                return config.leviathan_axe.attackDamageWeight;
            case "leviathan_axe.frost_weight":
                return config.leviathan_axe.frostWeight;
            case "nightfall.attack_damage_weight":
                return config.nightfall.attackDamageWeight;
            case "nightfall.soul_weight":
                return config.nightfall.soulWeight;
            case "ghost_glaive.attack_damage_weight":
                return config.ghost_glaive.attackDamageWeight;
            case "ghost_glaive.arcane_weight":
                return config.ghost_glaive.arcaneWeight;
            case "blade_dance.attack_damage_weight":
                return config.blade_dance.attackDamageWeight;
            case "blade_dance.attack_speed_weight":
                return config.blade_dance.attackSpeedWeight;
            case "darkmoon_longbow.ranged_damage_weight":
                return config.darkmoon_longbow.rangedDamageWeight;
            case "darkmoon_longbow.arcane_weight":
                return config.darkmoon_longbow.arcaneWeight;
            case "freyr_sword.attack_damage_weight":
                return config.freyr_sword.attackDamageWeight;
            case "freyr_sword.soul_weight":
                return config.freyr_sword.soulWeight;
            case "dragonslayer_swordspear.lightning_weight":
                return config.dragonslayer_swordspear.lightningWeight;
            case "dragonslayer_swordspear.water_weight":
                return config.dragonslayer_swordspear.waterWeight;
            case "dragonslayer_swordspear.rain_lightning_weight":
                return config.dragonslayer_swordspear.rainLightningWeight;
            case "dragonslayer_swordspear.rain_water_weight":
                return config.dragonslayer_swordspear.rainWaterWeight;
            case "dragonslayer_swordspear.lightning_per_spell_power":
                return config.dragonslayer_swordspear.lightningPerSpellPower;
            case "galeforce.ranged_weight":
                return config.galeforce.rangedWeight;
            case "galeforce.air_weight":
                return config.galeforce.airWeight;
            case "blood_loss.attack_damage_weight":
                return config.blood_loss.attackDamageWeight;
            case "blood_loss.rage_weight":
                return config.blood_loss.rageWeight;
            case "moonveil.attack_damage_weight":
                return config.moonveil.attackDamageWeight;
            case "moonveil.rage_weight":
                return config.moonveil.rageWeight;
            case "moonveil.bleed_attack_damage_weight":
                return config.moonveil.bleedAttackDamageWeight;
            case "moonveil.bleed_rage_weight":
                return config.moonveil.bleedRageWeight;
            case "bloodlust.bleed_attack_damage_weight":
                return config.bloodlust.bleedAttackDamageWeight;
            case "bloodlust.bleed_rage_weight":
                return config.bloodlust.bleedRageWeight;
            case "dark_moon_greatsword.attack_damage_weight":
                return config.dark_moon_greatsword.attackDamageWeight;
            case "dark_moon_greatsword.frost_weight":
                return config.dark_moon_greatsword.frostWeight;
            case "whirligig_sawblade.attack_damage_weight":
                return config.whirligig_sawblade.attackDamageWeight;
            case "whirligig_sawblade.rage_weight":
                return config.whirligig_sawblade.rageWeight;
            case "whirligig_sawblade.bleed_rage_weight":
                return config.whirligig_sawblade.bleedRageWeight;
            case "mjolnir.rain_lightning_weight":
                return config.mjolnir.rainLightningWeight;
            case "mjolnir.rain_water_weight":
                return config.mjolnir.rainWaterWeight;
            default:
                return defaultValue;
        }
    }
}