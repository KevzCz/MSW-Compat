package net.pixeldreamstudios.mswcompat.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class SpellPowerHelper {
    private static final Identifier GENERIC_SPELL_POWER = Identifier.of("spell_power", "generic");

    public static double getEffectiveSpellPower(LivingEntity entity, Identifier schoolId) {
        if (entity == null) return 0.0;

        RegistryEntry.Reference<EntityAttribute> schoolAttr = AttributeHelper.getAttributeEntry(schoolId);
        RegistryEntry.Reference<EntityAttribute> genericAttr = AttributeHelper.getAttributeEntry(GENERIC_SPELL_POWER);

        double schoolPower = (schoolAttr != null) ? entity.getAttributeValue(schoolAttr) : 0.0;
        double genericPower = (genericAttr != null) ? entity.getAttributeValue(genericAttr) : 1.0;

        double genericMultiplier = genericPower / 1.0;

        return schoolPower * genericMultiplier;
    }

    public static float getScalingFactor(LivingEntity entity, Identifier schoolId, float baseline) {
        if (baseline <= 0.0F) return 1.0F;

        double effectivePower = getEffectiveSpellPower(entity, schoolId);
        return 1.0F + (float)(effectivePower / baseline);
    }
}