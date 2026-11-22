package net.pixeldreamstudios.mswcompat.mixin.living;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.living.LivingActives;
import net.soulsweaponry.items.abilities.usagetick.GatlingSilverBullets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GatlingSilverBullets.class, remap = false)
public abstract class LivingGatlingMixin {

    @Inject(method = "usageTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;I)V", at = @At("TAIL"), remap = true)
    private void mswcompat$living(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
        if (user instanceof PlayerEntity || !(world instanceof ServerWorld server)) {
            return;
        }
        LivingActives.gatlingTick((GatlingSilverBullets) (Object) this, server, user, stack, remainingUseTicks);
    }
}
