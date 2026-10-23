package net.pixeldreamstudios.mswcompat.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public final class DamageScalingHelper {

    public enum Mode {
        ABOVE_BASELINE,
        FROM_ZERO,
        SYMMETRIC
    }

    private DamageScalingHelper() {}

    public static float ratio(double value, float baseline) {
        if (baseline <= 0.0F) {
            return 1.0F;
        }
        return (float) (value / baseline);
    }

    public static float contribution(double value, float baseline, float weight, Mode mode) {
        if (baseline <= 0.0F || weight == 0.0F) {
            return 0.0F;
        }
        float part = ratio(value, baseline);
        return switch (mode) {
            case ABOVE_BASELINE -> weight * Math.max(0.0F, part - 1.0F);
            case FROM_ZERO -> weight * part;
            case SYMMETRIC -> weight * (part - 1.0F);
        };
    }

    public static float factor(double value, float baseline, float weight, Mode mode) {
        return 1.0F + contribution(value, baseline, weight, mode);
    }

    public static float factor(
            double value1, float baseline1, float weight1, Mode mode1,
            double value2, float baseline2, float weight2, Mode mode2) {
        return 1.0F
                + contribution(value1, baseline1, weight1, mode1)
                + contribution(value2, baseline2, weight2, mode2);
    }

    public static float dualFromZero(
            double value1, float baseline1, float weight1,
            double value2, float baseline2, float weight2) {
        return factor(
                value1, baseline1, weight1, Mode.FROM_ZERO,
                value2, baseline2, weight2, Mode.FROM_ZERO);
    }

    public static float ratioMultiplier(double value, float baseline) {
        if (baseline <= 0.0F) {
            return 1.0F;
        }
        return ratio(value, baseline);
    }

    public static float attackDamageAndSecondary(
            double attackDamage, float attackDamageBaseline, float attackDamageWeight,
            double secondary, float secondaryBaseline, float secondaryWeight) {
        return factor(
                attackDamage, attackDamageBaseline, attackDamageWeight, Mode.ABOVE_BASELINE,
                secondary, secondaryBaseline, secondaryWeight, Mode.FROM_ZERO);
    }

    public static float attackDamageOnly(double attackDamage, float baseline, float weight) {
        return factor(attackDamage, baseline, weight, Mode.ABOVE_BASELINE);
    }

    public static float attackDamageOnly(double attackDamage, float baseline) {
        return attackDamageOnly(attackDamage, baseline, 1.0F);
    }

    public static float clamp(float factor, float min, float max) {
        return Math.max(min, Math.min(max, factor));
    }

    public static float spellPowerFactor(LivingEntity entity, Identifier schoolId, float baseline) {
        if (entity == null || baseline <= 0.0F) {
            return 1.0F;
        }
        return factor(SpellPowerHelper.getEffectiveSpellPower(entity, schoolId), baseline, 1.0F, Mode.FROM_ZERO);
    }

    public static float multiplicativeAboveBaselineAdAndRageFromZero(
            double attackDamage, float attackDamageBaseline,
            double rage, float rageBaseline) {
        float f = attackDamageOnly(attackDamage, attackDamageBaseline);
        if (rageBaseline > 0.0F && rage > 0.0) {
            f *= 1.0F + (float) (rage / rageBaseline);
        }
        return f;
    }

    public static float multiplicativeAdRatioAndRageFromZero(
            double attackDamage, float attackDamageBaseline,
            double rage, float rageBaseline) {
        float f = 1.0F;
        if (attackDamageBaseline > 0.0F && attackDamage > 0.0) {
            f = ratio(attackDamage, attackDamageBaseline);
        }
        if (rageBaseline > 0.0F && rage > 0.0) {
            f *= 1.0F + (float) (rage / rageBaseline);
        }
        return f;
    }
}
