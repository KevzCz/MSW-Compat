package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.items.katana.Dragonbane;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = Dragonbane.class)
public abstract class DragonbaneMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$scale = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        float factor = 1.0F;
        if (attacker != null) {
            float lightningBaseline = ConfigHelper.getBaselineValue("dragonbane.lightning_baseline", 20.0F);

            RegistryEntry.Reference<EntityAttribute> entry = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.LIGHTNING);
            if (entry != null && lightningBaseline > 0.0F) {
                double power = attacker.getAttributeValue(entry);
                factor = 1.0F + (float)(power / lightningBaseline);
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