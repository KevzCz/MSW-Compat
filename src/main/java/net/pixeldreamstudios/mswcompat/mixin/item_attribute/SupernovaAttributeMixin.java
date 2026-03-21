package net.pixeldreamstudios.mswcompat.mixin.item_attribute;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.items.hammer.Supernova;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin( value = ItemStack.class)
public abstract class SupernovaAttributeMixin {

    @Shadow
    public abstract Item getItem();

    @Inject(
            method = "applyAttributeModifier(Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("RETURN")
    )
    private void mswcompat$addSupernovaAttributesSlot(AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (!(this.getItem() instanceof Supernova) || slot != AttributeModifierSlot.MAINHAND) {
            return;
        }

        double fireBonus = ConfigHelper.getDoubleValue("supernova.fire_spell_power_bonus", 3.0);

        if (fireBonus != 0.0) {
            RegistryEntry.Reference<EntityAttribute> fireAttr = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.FIRE);
            if (fireAttr != null) {
                EntityAttributeModifier fireModifier = new EntityAttributeModifier(
                        MSWCompatIdentifiers.ModifierIds.SUPERNOVA_FIRE,
                        fireBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                attributeModifierConsumer.accept(fireAttr, fireModifier);
            }
        }
    }

    @Inject(
            method = "applyAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("RETURN")
    )
    private void mswcompat$addSupernovaAttributesEquipment(EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (!(this.getItem() instanceof Supernova) || slot != EquipmentSlot.MAINHAND) {
            return;
        }

        double fireBonus = ConfigHelper.getDoubleValue("supernova.fire_spell_power_bonus", 3.0);

        if (fireBonus != 0.0) {
            RegistryEntry.Reference<EntityAttribute> fireAttr = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.FIRE);
            if (fireAttr != null) {
                EntityAttributeModifier fireModifier = new EntityAttributeModifier(
                        MSWCompatIdentifiers.ModifierIds.SUPERNOVA_FIRE,
                        fireBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                attributeModifierConsumer.accept(fireAttr, fireModifier);
            }
        }
    }
}