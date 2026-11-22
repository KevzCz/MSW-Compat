package net.pixeldreamstudios.mswcompat.mixin.living;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.living.LivingAbilities;
import net.soulsweaponry.items.abilities.inventorytick.FlipEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FlipEffects.class, remap = false)
public abstract class LivingFlipEffectsMixin {

    @Inject(method = "inventoryTick(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;IZ)V", at = @At("HEAD"), remap = true)
    private void mswcompat$living(ItemStack stack, World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity user) || user instanceof PlayerEntity || world.isClient()) {
            return;
        }
        LivingAbilities.flipEffects((FlipEffects) (Object) this, stack, user);
    }
}
