package net.pixeldreamstudios.mswcompat.mixin.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.pixeldreamstudios.mswcompat.util.SpellPowerHelper;
import net.soulsweaponry.entity.ai.goal.FreyrSwordGoal;
import net.soulsweaponry.entity.mobs.FreyrSwordEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin( value = FreyrSwordGoal.class, remap = false)
public abstract class FreyrSwordGoalMixin {
    @Shadow @Final private FreyrSwordEntity entity;

    @Inject(
            method = "getAttackDamage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;)F",
            at = @At("RETURN"),
            cancellable = true,
            remap = false,
            require = 0
    )
    private void mswcompat$addEntityAttackAttribute(ServerWorld world, LivingEntity target, CallbackInfoReturnable<Float> cir) {
        if (this.entity == null) {
            return;
        }

        float originalDamage = cir.getReturnValueF();

        double attackDamageAttr = this.entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

        double soulSpellPowerAttr = SpellPowerHelper.getEffectiveSpellPower(this.entity, MSWCompatIdentifiers.SpellPower.SOUL);

        float attackDamageBaseline = ConfigHelper.getBaselineValue("freyr_sword.attack_damage_baseline", 7.0F);
        float soulBaseline = ConfigHelper.getBaselineValue("freyr_sword.soul_baseline", 20.0F);
        float attackDamageWeight = ConfigHelper.getFloatValue("freyr_sword.attack_damage_weight", 0.75F);
        float soulWeight = ConfigHelper.getFloatValue("freyr_sword.soul_weight", 2.5F);

        float excessAttackDamage = (float) Math.max(0.0D, attackDamageAttr - attackDamageBaseline);
        float excessSoulPower = (float) Math.max(0.0D, soulSpellPowerAttr - soulBaseline);

        float soulMultiplier = 2.5F + (soulWeight * excessSoulPower / soulBaseline);

        float attackDamageBonus = (excessAttackDamage * attackDamageWeight) * soulMultiplier;
        float soulDamageBonus = (attackDamageBaseline * (soulMultiplier - 1.0F));

        cir.setReturnValue(originalDamage + attackDamageBonus + soulDamageBonus);
    }
}