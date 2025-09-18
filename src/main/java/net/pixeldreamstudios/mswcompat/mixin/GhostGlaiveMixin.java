package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
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
    @Unique private static final Identifier FROST_ID = Identifier.of("spell_power", "frost");

    @Unique
    private static RegistryEntry.Reference<EntityAttribute> frostAttrRef() {
        RegistryKey<EntityAttribute> key = RegistryKey.of(RegistryKeys.ATTRIBUTE, FROST_ID);
        return Registries.ATTRIBUTE.getEntry(key).orElse(null);
    }

    @Unique
    private float mswcompat$computeFactor() {
        // Only scale Ghost Glaive; leave other DamagingNoClipEntity projectiles untouched.
        if (!(((Object) this) instanceof GhostGlaiveEntity gg)) return 1.0F;

        Entity owner = gg.getOwner();
        if (!(owner instanceof LivingEntity living)) return 1.0F;

        float ad = (float) living.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        float frost = 0.0F;
        var frostRef = frostAttrRef();
        if (frostRef != null) {
            frost = (float) living.getAttributeValue(frostRef);
        }

        // Same spirit as your Leviathan scaling: blend AD and Frost power.
        float factor = 0.5F * (ad / 10.0F) + 0.5F * (frost / 20.0F);
        if (factor < 0.0F) factor = 0.0F;
        return factor;
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
