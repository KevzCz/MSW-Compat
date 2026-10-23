package net.pixeldreamstudios.mswcompat.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;
import org.jetbrains.annotations.Nullable;

public class SpellPowerHelper {

    public static double getEffectiveSpellPower(LivingEntity entity, Identifier schoolId) {
        if (entity == null) return 0.0;

        SpellSchool school = resolveSchool(schoolId);
        if (school != null) {
            return SpellPower.getSpellPower(school, entity).nonCriticalValue();
        }

        return getRawAttributeValue(entity, schoolId);
    }

    public static float getScalingFactor(LivingEntity entity, Identifier schoolId, float baseline) {
        return DamageScalingHelper.spellPowerFactor(entity, schoolId, baseline);
    }

    @Nullable
    private static SpellSchool resolveSchool(Identifier schoolId) {
        return SpellSchools.getSchool(schoolId.toString());
    }

    private static double getRawAttributeValue(LivingEntity entity, Identifier attributeId) {
        RegistryEntry.Reference<EntityAttribute> attr = AttributeHelper.getAttributeEntry(attributeId);
        if (attr == null) return 0.0;
        return entity.getAttributeValue(attr);
    }
}
