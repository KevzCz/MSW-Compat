package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.soulsweaponry.entity.projectile.DragonslayerSwordspearEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = DragonslayerSwordspearEntity.class, remap = false)
public abstract class DragonslayerSwordspearEntityMixin {
    @Unique
    private static final ThreadLocal<Entity> mswcompat$projectileOwner = ThreadLocal.withInitial(() -> null);

    @Inject(
            method = "onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$storeProjectileOwner(net.minecraft.util.hit.EntityHitResult entityHitResult, CallbackInfo ci) {
        DragonslayerSwordspearEntity self = (DragonslayerSwordspearEntity) (Object) this;
        mswcompat$projectileOwner.set(self.getOwner());
    }

    @Inject(
            method = "onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearProjectileOwner(net.minecraft.util.hit.EntityHitResult entityHitResult, CallbackInfo ci) {
        mswcompat$projectileOwner.remove();
    }

    @Redirect(
            method = "onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    remap = true
            ),
            require = 0
    )
    private boolean mswcompat$damageWithADAndCredit(Entity target, DamageSource source, float amount) {
        Entity owner = mswcompat$projectileOwner.get();

        if (owner instanceof LivingEntity living) {
            double ad = living.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (ad > 0.0) {
                amount = (float) ad;
            }
        }

        if (owner instanceof PlayerEntity player) {
            DragonslayerSwordspearEntity self = (DragonslayerSwordspearEntity) (Object) this;
            source = self.getWorld().getDamageSources().playerAttack(player);
        }

        return target.damage(source, amount);
    }

    @ModifyArg(
            method = "onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"),
            index = 0,
            require = 0
    )
    private Entity mswcompat$attributeProjectileLightning(Entity e) {
        Entity owner = mswcompat$projectileOwner.get();
        if (e instanceof LightningEntity && owner instanceof ServerPlayerEntity sp) {
            ((LightningEntity) e).setChanneler(sp);
        }
        return e;
    }
}