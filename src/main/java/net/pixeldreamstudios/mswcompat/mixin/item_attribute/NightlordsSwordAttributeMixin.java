package net.pixeldreamstudios.mswcompat.mixin.item_attribute;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.items.sword.NightlordsSword;
import net.soulsweaponry.registry.ComponentRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(value = ItemStack.class)
public abstract class NightlordsSwordAttributeMixin {

    @Shadow
    public abstract Item getItem();

    @Unique
    private void mswcompat$applyPotencyAttributes(BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> consumer) {
        ItemStack self = (ItemStack)(Object)this;

        if (!(self.getItem() instanceof NightlordsSword)) {
            return;
        }

        Integer effectId = self.get(ComponentRegistry.POST_HIT_EFFECT_ID);
        if (effectId == null || effectId == 0) {
            return;
        }

        double bonus = 0.0;
        RegistryEntry.Reference<EntityAttribute> attr = null;
        Identifier modifierId = null;
        EntityAttributeModifier.Operation operation = EntityAttributeModifier.Operation.ADD_VALUE;

        switch (effectId) {
            case 1: // BLEED
                attr = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.MoreRPGLibrary.RAGE);
                bonus = 0.20;
                modifierId = MSWCompatIdentifiers.ModifierIds.NIGHTLORDS_SWORD_RAGE;
                operation = EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                break;
            case 2: // POISON
                attr = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.ARCANE);
                bonus = 4.0;
                modifierId = MSWCompatIdentifiers.ModifierIds.NIGHTLORDS_SWORD_ARCANE;
                operation = EntityAttributeModifier.Operation.ADD_VALUE;
                break;
            case 3: // CHAIN_LIGHTNING
                attr = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.LIGHTNING);
                bonus = 4.0;
                modifierId = MSWCompatIdentifiers.ModifierIds.NIGHTLORDS_SWORD_LIGHTNING;
                operation = EntityAttributeModifier.Operation.ADD_VALUE;
                break;
            case 4: // WITHER
                attr = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.SOUL);
                bonus = 4.0;
                modifierId = MSWCompatIdentifiers.ModifierIds.NIGHTLORDS_SWORD_SOUL;
                operation = EntityAttributeModifier.Operation.ADD_VALUE;
                break;
            case 5: // FREEZE
                RegistryEntry.Reference<EntityAttribute> waterAttr = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.MoreRPGLibrary.WATER);
                RegistryEntry.Reference<EntityAttribute> frostAttr = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.FROST);

                if (waterAttr != null) {
                    consumer.accept(waterAttr, new EntityAttributeModifier(
                            MSWCompatIdentifiers.ModifierIds.NIGHTLORDS_SWORD_WATER,
                            2.0,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ));
                }
                if (frostAttr != null) {
                    consumer.accept(frostAttr, new EntityAttributeModifier(
                            MSWCompatIdentifiers.ModifierIds.NIGHTLORDS_SWORD_FROST,
                            2.0,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ));
                }
                return;
            case 6: // FIRE
                attr = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.FIRE);
                bonus = 4.0;
                modifierId = MSWCompatIdentifiers.ModifierIds.NIGHTLORDS_SWORD_FIRE;
                operation = EntityAttributeModifier.Operation.ADD_VALUE;
                break;
            case 7: // CRIPPLE
                consumer.accept(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(
                        MSWCompatIdentifiers.ModifierIds.NIGHTLORDS_SWORD_ATTACK_SPEED,
                        0.20,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
                return;
            case 8: // DECAY
                attr = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellEngine.HEALING_TAKEN);
                bonus = 0.20;
                modifierId = MSWCompatIdentifiers.ModifierIds.NIGHTLORDS_SWORD_HEALING;
                operation = EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                break;
        }

        if (attr != null && modifierId != null) {
            consumer.accept(attr, new EntityAttributeModifier(
                    modifierId,
                    bonus,
                    operation
            ));
        }
    }

    @Inject(
            method = "applyAttributeModifier(Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("RETURN")
    )
    private void mswcompat$addNightlordsSwordAttributesSlot(AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> consumer, CallbackInfo ci) {
        if (slot != AttributeModifierSlot.MAINHAND) {
            return;
        }
        mswcompat$applyPotencyAttributes(consumer);
    }

    @Inject(
            method = "applyAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("RETURN")
    )
    private void mswcompat$addNightlordsSwordAttributesEquipment(EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> consumer, CallbackInfo ci) {
        if (slot != EquipmentSlot.MAINHAND) {
            return;
        }
        mswcompat$applyPotencyAttributes(consumer);
    }
}