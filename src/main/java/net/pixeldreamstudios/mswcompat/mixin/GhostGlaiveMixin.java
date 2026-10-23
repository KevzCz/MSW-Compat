package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.pixeldreamstudios.mswcompat.util.DamageScalingHelper;
import net.pixeldreamstudios.mswcompat.util.SpellPowerHelper;
import net.soulsweaponry.entity.projectile.noclip.DamagingNoClipEntity;
import net.soulsweaponry.entity.projectile.noclip.GhostGlaiveEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin( value = DamagingNoClipEntity.class, remap = false)
public abstract class GhostGlaiveMixin {

    @Unique
    private float mswcompat$computeFactor() {
        if (!(((Object) this) instanceof GhostGlaiveEntity gg)) return 1.0F;

        Entity owner = gg.getOwner();
        if (!(owner instanceof LivingEntity living)) return 1.0F;

        return DamageScalingHelper.attackDamageAndSecondary(
                living.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
                ConfigHelper.getBaselineValue("ghost_glaive.attack_damage_baseline", 10.0F),
                ConfigHelper.getFloatValue("ghost_glaive.attack_damage_weight", 0.5F),
                SpellPowerHelper.getEffectiveSpellPower(living, MSWCompatIdentifiers.SpellPower.ARCANE),
                ConfigHelper.getBaselineValue("ghost_glaive.arcane_baseline", 20.0F),
                ConfigHelper.getFloatValue("ghost_glaive.arcane_weight", 0.5F));
    }

    @ModifyArg(
            method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    remap = true
            ),
            index = 1
    )
    private float mswcompat$scaleGhostGlaiveDamage(float amount) {
        return amount * mswcompat$computeFactor();
    }
}