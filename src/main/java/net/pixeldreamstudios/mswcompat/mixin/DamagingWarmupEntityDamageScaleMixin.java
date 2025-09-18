package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.soulsweaponry.entity.projectile.noclip.DamagingWarmupEntity;
import net.soulsweaponry.entity.projectile.noclip.FlamePillar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(value = DamagingWarmupEntity.class, remap = false)
public abstract class DamagingWarmupEntityDamageScaleMixin {
    @Unique private static final Identifier FIRE_ID = Identifier.of("spell_power", "fire");

    @Unique
    private static RegistryEntry.Reference<EntityAttribute> fireAttr() {
        RegistryKey<EntityAttribute> key = RegistryKey.of(RegistryKeys.ATTRIBUTE, FIRE_ID);
        return Registries.ATTRIBUTE.getEntry(key).orElse(null);
    }

    @Unique
    private float mswcompat$computeScale() {
        Entity owner = ((DamagingWarmupEntity)(Object)this).getOwner();
        if (!(owner instanceof LivingEntity living)) return 1.0F;
        var fireRef = fireAttr();
        double fire = (fireRef != null) ? living.getAttributeValue(fireRef) : 0.0;
        float base = (Object)this instanceof FlamePillar ? 0.75F : 0.25F;
        float s = (float)(1.0 + base * (fire / 20.0));
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
