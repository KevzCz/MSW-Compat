package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.soulsweaponry.items.sword.Excalibur;
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
import net.minecraft.util.Identifier;

@Pseudo
@Mixin(value = Excalibur.class)
public abstract class ExcaliburMixin {
    @Unique private static final Identifier ARCANE_ID = Identifier.of("spell_power", "arcane");
    @Unique private static final Identifier SOUL_ID   = Identifier.of("spell_power", "soul");
    @Unique private static final ThreadLocal<Float> SCALE = ThreadLocal.withInitial(() -> 1.0F);

    @Unique
    private static RegistryEntry.Reference<EntityAttribute> getAttrRef(Identifier id) {
        RegistryKey<EntityAttribute> key = RegistryKey.of(RegistryKeys.ATTRIBUTE, id);
        RegistryEntry.Reference<EntityAttribute> entry = Registries.ATTRIBUTE.getEntry(key).orElse(null);
        if (entry == null) {
            EntityAttribute attr = Registries.ATTRIBUTE.get(id);
            if (attr != null) {
                var optKey = Registries.ATTRIBUTE.getKey(attr);
                if (optKey.isPresent()) entry = Registries.ATTRIBUTE.getEntry(optKey.get()).orElse(null);
            }
        }
        return entry;
    }

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("HEAD"),
            require = 0
    )
    private void cacheScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        float factor = 1.0F;
        if (user != null) {
            RegistryEntry.Reference<EntityAttribute> arcRef = getAttrRef(ARCANE_ID);
            RegistryEntry.Reference<EntityAttribute> soulRef = getAttrRef(SOUL_ID);
            double arc = arcRef != null ? user.getAttributeValue(arcRef) : 0.0;
            double soul = soulRef != null ? user.getAttributeValue(soulRef) : 0.0;
            factor = (float)(1.0 + (0.5 * arc) / 20.0 + (0.5 * soul) / 20.0);
        }
        SCALE.set(factor);
    }

    @ModifyArg(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            ),
            index = 1,
            require = 0
    )
    private float scaleSonicBoomDamage(float amount) {
        return amount * SCALE.get();
    }

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("TAIL"),
            require = 0
    )
    private void clearScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        SCALE.remove();
    }
}
