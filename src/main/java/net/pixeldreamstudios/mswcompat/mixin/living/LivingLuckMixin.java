package net.pixeldreamstudios.mswcompat.mixin.living;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;
import net.soulsweaponry.util.WeaponUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = WeaponUtil.class, remap = false)
public abstract class LivingLuckMixin {

    @WrapOperation(method = "getLuckFactor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getAttributeValue(Lnet/minecraft/registry/entry/RegistryEntry;)D", remap = true))
    private static double mswcompat$luckless(LivingEntity entity, RegistryEntry<EntityAttribute> attribute, Operation<Double> original) {
        return entity.getAttributes().hasAttribute(attribute) ? original.call(entity, attribute) : attribute.value().getDefaultValue();
    }
}
