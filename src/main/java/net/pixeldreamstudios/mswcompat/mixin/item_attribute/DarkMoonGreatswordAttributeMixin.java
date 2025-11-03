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
import net.soulsweaponry.items.sword.DarkMoonGreatsword;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(ItemStack.class)
public abstract class DarkMoonGreatswordAttributeMixin {

    @Shadow
    public abstract net.minecraft.item.Item getItem();

    @Inject(
            method = "applyAttributeModifier(Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("RETURN")
    )
    private void mswcompat$addDarkMoonGreatswordAttributesSlot(AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (!(this.getItem() instanceof DarkMoonGreatsword) || slot != AttributeModifierSlot.MAINHAND) {
            return;
        }

        double frostBonus = ConfigHelper.getDoubleValue("dark_moon_greatsword.frost_spell_power_bonus", 3.0);

        if (frostBonus != 0.0) {
            RegistryEntry.Reference<EntityAttribute> frostAttr = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.FROST);

            if (frostAttr != null) {
                EntityAttributeModifier frostModifier = new EntityAttributeModifier(
                        MSWCompatIdentifiers.ModifierIds.DARK_MOON_GREATSWORD_FROST,
                        frostBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                attributeModifierConsumer.accept(frostAttr, frostModifier);
            }
        }
    }

    @Inject(
            method = "applyAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("RETURN")
    )
    private void mswcompat$addDarkMoonGreatswordAttributesEquipment(EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (!(this.getItem() instanceof DarkMoonGreatsword) || slot != EquipmentSlot.MAINHAND) {
            return;
        }

        double frostBonus = ConfigHelper.getDoubleValue("dark_moon_greatsword.frost_spell_power_bonus", 3.0);

        if (frostBonus != 0.0) {
            RegistryEntry.Reference<EntityAttribute> frostAttr = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.FROST);

            if (frostAttr != null) {
                EntityAttributeModifier frostModifier = new EntityAttributeModifier(
                        MSWCompatIdentifiers.ModifierIds.DARK_MOON_GREATSWORD_FROST,
                        frostBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                attributeModifierConsumer.accept(frostAttr, frostModifier);
            }
        }
    }
}