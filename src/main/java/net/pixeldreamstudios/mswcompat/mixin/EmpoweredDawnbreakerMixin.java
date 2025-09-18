package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.soulsweaponry.items.sword.EmpoweredDawnbreaker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;

@Pseudo
@Mixin(value = EmpoweredDawnbreaker.class)
public abstract class EmpoweredDawnbreakerMixin {
    @Unique private static final Identifier FIRE_ID = Identifier.of("spell_power", "fire");
    @Unique private static final ThreadLocal<Float> SCALE = ThreadLocal.withInitial(() -> 1.0F);

    @Unique
    private static RegistryEntry.Reference<EntityAttribute> getFireAttrRef() {
        RegistryKey<EntityAttribute> key = RegistryKey.of(RegistryKeys.ATTRIBUTE, FIRE_ID);
        RegistryEntry.Reference<EntityAttribute> entry = Registries.ATTRIBUTE.getEntry(key).orElse(null);
        if (entry == null) {
            EntityAttribute attr = Registries.ATTRIBUTE.get(FIRE_ID);
            if (attr != null) {
                var optKey = Registries.ATTRIBUTE.getKey(attr);
                if (optKey.isPresent()) entry = Registries.ATTRIBUTE.getEntry(optKey.get()).orElse(null);
            }
        }
        return entry;
    }

    @Inject(
            method = "summonFlamePillars(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void cacheFireScale(World world, ItemStack stack, LivingEntity user, CallbackInfo ci) {
        float factor = 1.0F;
        if (user != null) {
            RegistryEntry.Reference<EntityAttribute> ref = getFireAttrRef();
            double fire = (ref != null) ? user.getAttributeValue(ref) : 0.0;
            factor = (float)(1.0 + fire / 20.0);
        }
        SCALE.set(factor);
    }

    @ModifyArg(
            method = "summonFlamePillars(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/noclip/FlamePillar;setDamage(D)V",
                    remap = false
            ),
            index = 0,
            require = 0
    )
    private double scaleFlamePillarDamage(double baseDamage) {
        return baseDamage * SCALE.get();
    }

    @Inject(
            method = "summonFlamePillars(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void clearFireScale(World world, ItemStack stack, LivingEntity user, CallbackInfo ci) {
        SCALE.remove();
    }
}
