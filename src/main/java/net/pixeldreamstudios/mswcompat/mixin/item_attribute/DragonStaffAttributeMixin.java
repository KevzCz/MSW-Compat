package net.pixeldreamstudios.mswcompat.mixin.item_attribute;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.items.staff.DragonStaff;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(ItemStack.class)
public abstract class DragonStaffAttributeMixin {

    @Shadow
    public abstract net.minecraft.item.Item getItem();

    @Inject(
            method = "applyAttributeModifier(Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("RETURN")
    )
    private void mswcompat$addDragonStaffAttributesSlot(AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (!(this.getItem() instanceof DragonStaff) || slot != AttributeModifierSlot.MAINHAND) {
            return;
        }

        double arcaneBonus = ConfigHelper.getDoubleValue("dragon_staff.arcane_spell_power_bonus", 3.0);

        if (arcaneBonus != 0.0) {
            RegistryEntry.Reference<EntityAttribute> arcaneAttr = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.ARCANE);
            if (arcaneAttr != null) {
                EntityAttributeModifier arcaneModifier = new EntityAttributeModifier(
                        MSWCompatIdentifiers.ModifierIds.DRAGON_STAFF_ARCANE,
                        arcaneBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                attributeModifierConsumer.accept(arcaneAttr, arcaneModifier);
            }
        }
    }

    @Inject(
            method = "applyAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("RETURN")
    )
    private void mswcompat$addDragonStaffAttributesEquipment(EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (!(this.getItem() instanceof DragonStaff) || slot != EquipmentSlot.MAINHAND) {
            return;
        }

        double arcaneBonus = ConfigHelper.getDoubleValue("dragon_staff.arcane_spell_power_bonus", 3.0);

        if (arcaneBonus != 0.0) {
            RegistryEntry.Reference<EntityAttribute> arcaneAttr = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.ARCANE);
            if (arcaneAttr != null) {
                EntityAttributeModifier arcaneModifier = new EntityAttributeModifier(
                        MSWCompatIdentifiers.ModifierIds.DRAGON_STAFF_ARCANE,
                        arcaneBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                attributeModifierConsumer.accept(arcaneAttr, arcaneModifier);
            }
        }
    }
}