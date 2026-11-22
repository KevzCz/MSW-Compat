package net.pixeldreamstudios.mswcompat.mixin.living;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.pixeldreamstudios.mswcompat.living.LivingLightning;
import net.soulsweaponry.entity.projectile.DragonslayerSwordspearEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = DragonslayerSwordspearEntity.class, remap = false)
public abstract class LivingSwordspearLightningMixin {

    @ModifyArg(method = "onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z", remap = true), index = 0, remap = true)
    private Entity mswcompat$markCaster(Entity spawned) {
        return LivingLightning.mark(spawned, ((ProjectileEntity) (Object) this).getOwner());
    }
}
