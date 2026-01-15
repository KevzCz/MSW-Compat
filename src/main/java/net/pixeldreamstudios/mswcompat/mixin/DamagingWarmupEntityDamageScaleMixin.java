package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.entity.projectile.noclip.DamagingWarmupEntity;
import net.soulsweaponry.entity.projectile.noclip.FlamePillar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin( value = DamagingWarmupEntity.class, remap = false)
public abstract class DamagingWarmupEntityDamageScaleMixin {

    @Unique
    private float mswcompat$computeScale() {
        DamagingWarmupEntity self = (DamagingWarmupEntity)(Object)this;
        Entity owner = self.getOwner();

        if (!(owner instanceof LivingEntity living)) return 1.0F;

        if (self instanceof FlamePillar pillar) {
            if (pillar.getEventId() == -1) {
                return 1.0F;
            }
        }

        float fireBaseline = ConfigHelper.getBaselineValue("supernova.fire_baseline", 20.0F);
        boolean isFlamePillar = self instanceof FlamePillar;
        float scaling = isFlamePillar
                ? ConfigHelper.getBaselineValue("supernova.flame_pillar_scaling", 0.75F)
                : ConfigHelper.getBaselineValue("supernova.molten_metal_scaling", 0.25F);

        RegistryEntry.Reference<EntityAttribute> fireRef = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.FIRE);
        double fire = (fireRef != null) ? living.getAttributeValue(fireRef) : 0.0;

        float firePart = fireBaseline > 0.0F ? (float)(fire / fireBaseline) : 0.0F;
        float s = 1.0F + scaling * firePart;
        return s < 0.0F ? 0.0F : s;
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
    private float mswcompat$scaleWarmupDamage(float amount) {
        return amount * mswcompat$computeScale();
    }
}