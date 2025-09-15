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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.soulsweaponry.entity.mobs.RimeSpectre$RimeSpectreGoal")
public abstract class RimeSpectreFrostBeamScalingMixin {
    @Unique private static final Identifier SOUL_ID  = Identifier.of("spell_power", "soul");
    @Unique private static final Identifier FROST_ID = Identifier.of("spell_power", "frost");
    @Unique private static final float BASELINE = 20.0F;

    @Unique
    private static float mswcompat$factorFromSource(DamageSource src) {
        Entity attacker = src.getAttacker();
        if (!(attacker instanceof LivingEntity living)) return 1.0F;

        float soul = 0.0F;
        float frost = 0.0F;

        RegistryKey<EntityAttribute> soulKey = RegistryKey.of(RegistryKeys.ATTRIBUTE, SOUL_ID);
        RegistryEntry<EntityAttribute> soulEntry = Registries.ATTRIBUTE.getEntry(soulKey).orElse(null);
        if (soulEntry == null) {
            EntityAttribute attr = Registries.ATTRIBUTE.get(SOUL_ID);
            if (attr != null) soulEntry = Registries.ATTRIBUTE.getEntry(attr);
        }
        if (soulEntry != null) soul = (float) living.getAttributeValue(soulEntry);

        RegistryKey<EntityAttribute> frostKey = RegistryKey.of(RegistryKeys.ATTRIBUTE, FROST_ID);
        RegistryEntry<EntityAttribute> frostEntry = Registries.ATTRIBUTE.getEntry(frostKey).orElse(null);
        if (frostEntry == null) {
            EntityAttribute attr = Registries.ATTRIBUTE.get(FROST_ID);
            if (attr != null) frostEntry = Registries.ATTRIBUTE.getEntry(attr);
        }
        if (frostEntry != null) frost = (float) living.getAttributeValue(frostEntry);

        float weighted = 0.25F * soul + 0.75F * frost;
        return 1.0F + (weighted / BASELINE);
    }

    @Redirect(
            method = "frostBeam(Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"),
            require = 0
    )
    private boolean mswcompat$scaleRimeSpectreBeamDamage(LivingEntity target, DamageSource source, float amount) {
        return target.damage(source, amount * mswcompat$factorFromSource(source));
    }
}
