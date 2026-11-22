package net.pixeldreamstudios.mswcompat.mixin.living;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.living.LivingAbilities;
import net.soulsweaponry.items.abilities.stoppedusing.ChaosStorm;
import net.soulsweaponry.items.abilities.stoppedusing.ShootMoonlight;
import net.soulsweaponry.items.abilities.stoppedusing.ThrowCometSpear;
import net.soulsweaponry.items.abilities.stoppedusing.ThrowDragonslayerSwordspear;
import net.soulsweaponry.items.abilities.stoppedusing.ThrowDraupnirSpear;
import net.soulsweaponry.items.abilities.stoppedusing.ThrowGhostGlaive;
import net.soulsweaponry.items.abilities.stoppedusing.ThrowLeviathanAxe;
import net.soulsweaponry.items.abilities.stoppedusing.ThrowMjolnir;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = {ChaosStorm.class, ShootMoonlight.class, ThrowCometSpear.class, ThrowDragonslayerSwordspear.class, ThrowDraupnirSpear.class,
        ThrowGhostGlaive.class, ThrowLeviathanAxe.class, ThrowMjolnir.class}, remap = false)
public abstract class LivingReleaseMixin {

    @Inject(method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V", at = @At("HEAD"),
            cancellable = true, remap = true)
    private void mswcompat$living(ItemStack stack, World world, LivingEntity user, int ticksUsed, CallbackInfo ci) {
        if (user instanceof PlayerEntity || world.isClient()) {
            return;
        }
        LivingAbilities.release(this, stack, world, user, ticksUsed);
        ci.cancel();
    }
}
