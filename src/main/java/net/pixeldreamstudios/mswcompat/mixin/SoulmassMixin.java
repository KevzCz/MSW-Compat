package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.soulsweaponry.entity.mobs.Soulmass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.soulsweaponry.entity.mobs.Soulmass$SoulmassGoal")
public abstract class SoulmassMixin {

    @Unique private static final Identifier SOUL_ID = Identifier.of("spell_power", "soul");

    @Unique
    private static float mswcompat$factorFromSource(DamageSource src) {
        Entity attacker = src.getAttacker();
        if (!(attacker instanceof Soulmass sm)) return 1.0F;

        RegistryKey<EntityAttribute> key = RegistryKey.of(RegistryKeys.ATTRIBUTE, SOUL_ID);
        RegistryEntry.Reference<EntityAttribute> entry = Registries.ATTRIBUTE.getEntry(key).orElse(null);
        if (entry == null) {
            EntityAttribute attr = Registries.ATTRIBUTE.get(SOUL_ID);
            if (attr != null) {
                var optKey = Registries.ATTRIBUTE.getKey(attr);
                if (optKey.isPresent()) entry = Registries.ATTRIBUTE.getEntry(optKey.get()).orElse(null);
            }
        }
        if (entry == null) return 1.0F;

        double soul = sm.getAttributeValue(entry);
        // 1.0x at 0 soul; +50% per 10 soul
        return 1.0F + (float)(soul / 20.0);
    }

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"),
            remap = true
    )
    private boolean mswcompat$scaleBeam(LivingEntity target, DamageSource source, float amount) {
        return target.damage(source, amount * mswcompat$factorFromSource(source));
    }
}
