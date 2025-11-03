package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
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
        double attr = 0.0D;
        if (this.entity != null) {
            attr = this.entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        }

        float baseline = ConfigHelper.getBaselineValue("freyr_sword.attack_damage_baseline", 15.0F);
        float netBonus = (float) Math.max(0.0D, attr - baseline);
        cir.setReturnValue(cir.getReturnValueF() + netBonus);
    }
}