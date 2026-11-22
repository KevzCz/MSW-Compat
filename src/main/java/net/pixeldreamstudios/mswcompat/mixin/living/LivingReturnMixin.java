package net.pixeldreamstudios.mswcompat.mixin.living;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.soulsweaponry.entity.projectile.ReturningProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = ReturningProjectile.class, remap = false)
public abstract class LivingReturnMixin {

    @Unique
    private static final double mswcompat$CATCH_REACH = 1.5D;

    @Inject(method = "tick()V", at = @At("TAIL"), remap = true)
    private void mswcompat$caughtByLiving(CallbackInfo ci) {
        ReturningProjectile self = (ReturningProjectile) (Object) this;
        Entity owner = self.getOwner();
        if (self.getWorld().isClient() || !self.shouldReturn() || !(owner instanceof LivingEntity user) || owner instanceof PlayerEntity
                || self.pickupType != PersistentProjectileEntity.PickupPermission.DISALLOWED) {
            return;
        }
        if (!user.isAlive() || self.squaredDistanceTo(user.getEyePos()) <= mswcompat$CATCH_REACH * mswcompat$CATCH_REACH) {
            self.discard();
        }
    }
}
