package net.pixeldreamstudios.mswcompat.mixin. item_attribute;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft. entity. EquipmentSlot;
import net.minecraft.entity.attribute. EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft. item.ItemStack;
import net.minecraft. registry.entry.RegistryEntry;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat. util.AttributeHelper;
import net.pixeldreamstudios.mswcompat. util.MSWCompatIdentifiers;
import net.soulsweaponry.items.spear.DragonslayerSwordspear;
import org.spongepowered. asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered. asm.mixin.injection. Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin( value = ItemStack.class)
public abstract class DragonslayerSwordspearAttributeMixin {

    @Shadow
    public abstract net.minecraft.item.Item getItem();

    @Inject(
            method = "applyAttributeModifier(Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("RETURN")
    )
    private void mswcompat$addDragonslayerSwordspearAttributesSlot(AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (!(this.getItem() instanceof DragonslayerSwordspear) || slot != AttributeModifierSlot.MAINHAND) {
            return;
        }

        double lightningBonus = ConfigHelper.getDoubleValue("dragonslayer_swordspear. lightning_spell_power_bonus", 4.0);

        if (lightningBonus != 0.0) {
            RegistryEntry. Reference<EntityAttribute> lightningAttr = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.LIGHTNING);
            if (lightningAttr != null) {
                EntityAttributeModifier lightningModifier = new EntityAttributeModifier(
                        MSWCompatIdentifiers.ModifierIds.DRAGONSLAYER_SWORDSPEAR_LIGHTNING,
                        lightningBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                attributeModifierConsumer.accept(lightningAttr, lightningModifier);
            }
        }
    }

    @Inject(
            method = "applyAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("RETURN")
    )
    private void mswcompat$addDragonslayerSwordspearAttributesEquipment(EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (!(this.getItem() instanceof DragonslayerSwordspear) || slot != EquipmentSlot.MAINHAND) {
            return;
        }

        double lightningBonus = ConfigHelper.getDoubleValue("dragonslayer_swordspear. lightning_spell_power_bonus", 4.0);

        if (lightningBonus != 0.0) {
            RegistryEntry.Reference<EntityAttribute> lightningAttr = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.LIGHTNING);
            if (lightningAttr != null) {
                EntityAttributeModifier lightningModifier = new EntityAttributeModifier(
                        MSWCompatIdentifiers.ModifierIds. DRAGONSLAYER_SWORDSPEAR_LIGHTNING,
                        lightningBonus,
                        EntityAttributeModifier. Operation.ADD_VALUE
                );
                attributeModifierConsumer.accept(lightningAttr, lightningModifier);
            }
        }
    }
}