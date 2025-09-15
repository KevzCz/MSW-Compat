package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.mob.EvokerFangsEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EvokerFangsEntity.class)
public abstract class EvokerFangsSoulmassScalingMixin {
    @Unique private static final Identifier SOUL_ID = Identifier.of("spell_power", "soul");

    @Unique
    private static float mswcompat$factorFromOwner(EvokerFangsEntity self) {
        LivingEntity owner = self.getOwner();
        if (owner == null) return 1.0F;

        RegistryKey<EntityAttribute> key = RegistryKey.of(RegistryKeys.ATTRIBUTE, SOUL_ID);
        RegistryEntry<EntityAttribute> entry = Registries.ATTRIBUTE.getEntry(key).orElse(null);
        if (entry == null) {
            EntityAttribute attr = Registries.ATTRIBUTE.get(SOUL_ID);
            if (attr != null) entry = Registries.ATTRIBUTE.getEntry(attr);
        }
        if (entry == null) return 1.0F;

        double soul = owner.getAttributeValue(entry);
        float factor = (float)(soul / 10.0);
        return Math.max(0.0F, factor);
    }
    @Redirect(
            method = "damage(Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    ordinal = 0
            ),
            require = 0
    )
    private boolean mswcompat$scaleFangsDamageNoOwner(LivingEntity target,
                                                      net.minecraft.entity.damage.DamageSource src,
                                                      float amount) {
        EvokerFangsEntity self = (EvokerFangsEntity)(Object)this;
        return target.damage(src, amount * mswcompat$factorFromOwner(self));
    }

    @Redirect(
            method = "damage(Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    ordinal = 1
            ),
            require = 0
    )
    private boolean mswcompat$scaleFangsDamageWithOwner(LivingEntity target,
                                                        net.minecraft.entity.damage.DamageSource src,
                                                        float amount) {
        EvokerFangsEntity self = (EvokerFangsEntity)(Object)this;
        return target.damage(src, amount * mswcompat$factorFromOwner(self));
    }

}
