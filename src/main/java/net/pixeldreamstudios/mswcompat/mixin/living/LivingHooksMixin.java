package net.pixeldreamstudios.mswcompat.mixin.living;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.living.LivingAbilities;
import net.soulsweaponry.items.abilities.IHasAbilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.soulsweaponry.items.abilities.HasAbilitiesHooks", remap = false)
public abstract class LivingHooksMixin {

    @Inject(method = "onStoppedUsing(Lnet/soulsweaponry/items/abilities/IHasAbilities;Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("HEAD"), cancellable = true, remap = true)
    private static void mswcompat$livingRelease(IHasAbilities self, ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (user instanceof PlayerEntity || world.isClient()) {
            return;
        }
        LivingAbilities.releaseAny(self, stack, world, user, remainingUseTicks);
        ci.cancel();
    }

    @Inject(method = "finishUsing(Lnet/soulsweaponry/items/abilities/IHasAbilities;Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;",
            at = @At("HEAD"), remap = true)
    private static void mswcompat$livingFinish(IHasAbilities self, ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (user instanceof PlayerEntity || world.isClient() || self.isDisabled(stack)) {
            return;
        }
        LivingAbilities.finishChannels(self, stack, user);
    }
}
