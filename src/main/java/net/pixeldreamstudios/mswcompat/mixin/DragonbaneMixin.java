package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.soulsweaponry.items.katana.Dragonbane;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;

@Pseudo
@Mixin(value = Dragonbane.class)
public abstract class DragonbaneMixin {
    @Unique private static final Identifier mswcompat$LIGHTNING_ID = Identifier.of("spell_power", "lightning");
    @Unique private static final ThreadLocal<Float> mswcompat$scale = ThreadLocal.withInitial(() -> 1.0F);

    @Unique
    private static RegistryEntry.Reference<EntityAttribute> mswcompat$getLightningAttrRef() {
        RegistryKey<EntityAttribute> key = RegistryKey.of(RegistryKeys.ATTRIBUTE, mswcompat$LIGHTNING_ID);
        RegistryEntry.Reference<EntityAttribute> entry = Registries.ATTRIBUTE.getEntry(key).orElse(null);
        if (entry == null) {
            EntityAttribute attr = Registries.ATTRIBUTE.get(mswcompat$LIGHTNING_ID);
            if (attr != null) {
                var optKey = Registries.ATTRIBUTE.getKey(attr);
                if (optKey.isPresent()) entry = Registries.ATTRIBUTE.getEntry(optKey.get()).orElse(null);
            }
        }
        return entry;
    }

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        float factor = 1.0F;
        if (attacker != null) {
            RegistryEntry.Reference<EntityAttribute> entry = mswcompat$getLightningAttrRef();
            if (entry != null) {
                double power = attacker.getAttributeValue(entry);
                factor = (float)(1.0 + power / 20.0);
            }
        }
        mswcompat$scale.set(factor);
    }

    @Redirect(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETSTATIC,
                    target = "Lnet/soulsweaponry/config/ConfigConstructor;dragonbane_chain_lightning_damage_per_level:F",
                    remap = false
            ),
            require = 0
    )
    private float mswcompat$scaleDamagePerLevel() {
        return net.soulsweaponry.config.ConfigConstructor.dragonbane_chain_lightning_damage_per_level * mswcompat$scale.get();
    }

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At("RETURN"),
            require = 0
    )
    private void mswcompat$clearScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        mswcompat$scale.remove();
    }
}
