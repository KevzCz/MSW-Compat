package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.entity.ai.goal.FreyrSwordGoal;
import net.soulsweaponry.entity.mobs.FreyrSwordEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = FreyrSwordGoal.class)
public abstract class FreyrSwordGoalMixin {
    @Shadow(remap = false) private FreyrSwordEntity entity;

    @Inject(method = "getAttackDamage", at = @At("RETURN"), cancellable = true, require = 0)
    private void mswcompat$addEntityAttackAttribute(LivingEntity target, CallbackInfoReturnable<Float> cir) {
        if (this.entity == null) {
            return;
        }

        double attackDamageAttr = this.entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

        double soulSpellPowerAttr = 0.0D;
        var soulAttr = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.SOUL);
        if (soulAttr != null && this.entity.getAttributes().hasAttribute(soulAttr)) {
            soulSpellPowerAttr = this.entity.getAttributeValue(soulAttr);
        }

        float attackDamageBaseline = ConfigHelper.getBaselineValue("freyr_sword.attack_damage_baseline", 7.0F);
        float soulBaseline = ConfigHelper.getBaselineValue("freyr_sword.soul_baseline", 20.0F);
        float attackDamageWeight = ConfigHelper.getFloatValue("freyr_sword.attack_damage_weight", 0.75F);
        float soulWeight = ConfigHelper.getFloatValue("freyr_sword.soul_weight", 1.0F);

        float excessAttackDamage = (float) Math.max(0.0D, attackDamageAttr - attackDamageBaseline);

        float excessSoulPower = (float) Math.max(0.0D, soulSpellPowerAttr - soulBaseline);

        float soulMultiplier = 1.0F + (soulWeight * excessSoulPower / soulBaseline);
        float totalDamage = (attackDamageBaseline + excessAttackDamage * attackDamageWeight) * soulMultiplier;

        float bonusDamage = totalDamage - cir.getReturnValueF();

        cir.setReturnValue(cir.getReturnValueF() + bonusDamage);
    }
}