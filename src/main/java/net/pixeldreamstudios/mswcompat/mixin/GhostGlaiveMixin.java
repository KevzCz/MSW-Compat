package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.entity.projectile.noclip.DamagingNoClipEntity;
import net.soulsweaponry.entity.projectile.noclip.GhostGlaiveEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(value = DamagingNoClipEntity.class, remap = false)
public abstract class GhostGlaiveMixin {

    @Unique
    private float mswcompat$computeFactor() {
        if (!(((Object) this) instanceof GhostGlaiveEntity gg)) return 1.0F;

        Entity owner = gg.getOwner();
        if (!(owner instanceof LivingEntity living)) return 1.0F;

        float adBaseline = ConfigHelper.getBaselineValue("ghost_glaive.attack_damage_baseline", 10.0F);
        float frostBaseline = ConfigHelper.getBaselineValue("ghost_glaive.frost_baseline", 20.0F);
        float adWeight = ConfigHelper.getFloatValue("ghost_glaive.attack_damage_weight", 0.5F);
        float frostWeight = ConfigHelper.getFloatValue("ghost_glaive.frost_weight", 0.5F);

        float ad = (float) living.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        float frost = 0.0F;

        RegistryEntry.Reference<EntityAttribute> frostRef = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.FROST);
        if (frostRef != null) {
            frost = (float) living.getAttributeValue(frostRef);
        }

        float adPart = adBaseline > 0.0F ? ad / adBaseline : 1.0F;
        float frostPart = frostBaseline > 0.0F ? frost / frostBaseline : 0.0F;

        float factor = adWeight * adPart + frostWeight * frostPart;
        return Math.max(0.0F, factor);
    }

    @ModifyArg(
            method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    remap = true
            ),
            index = 1,
            require = 0
    )
    private float mswcompat$scaleGhostGlaiveDamage(float amount) {
        return amount * mswcompat$computeFactor();
    }
}