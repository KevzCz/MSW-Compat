package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.entity.projectile.DragonStaffProjectile;
import net.soulsweaponry.items.abilities.abilitykeybind.ShootDragonProjectile;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = ShootDragonProjectile.class, remap = false )
public abstract class ShootDragonProjectileMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$auraAmp = ThreadLocal.withInitial(() -> 0.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheArcaneScaling(ServerWorld world, ItemStack stack, PlayerEntity user, @Nullable Hand hand, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isDragonStaff(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (!shouldScale) {
            mswcompat$auraAmp.set(0.0F);
            return;
        }

        float arcane = 0.0F;
        if (user != null) {
            RegistryEntry.Reference<EntityAttribute> entry = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.ARCANE);
            if (entry != null) {
                arcane = (float) user.getAttributeValue(entry);
            }
        }

        float auraPer10 = ConfigHelper.getBaselineValue("dragon_staff.aura_amplifier_per_10_arcane", 1.0F);
        mswcompat$auraAmp.set((arcane / 10.0F) * auraPer10);
    }

    @Redirect(
            method = "getDragonStaffProjectile(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;I)Lnet/soulsweaponry/entity/projectile/DragonStaffProjectile;",
            at = @At(value = "INVOKE", target = "Lnet/soulsweaponry/entity/projectile/DragonStaffProjectile;setEffectAmp(I)V"),
            require = 0
    )
    private void mswcompat$boostEffectAmp(DragonStaffProjectile projectile, int amp) {
        if (!mswcompat$shouldScale.get()) {
            projectile.setEffectAmp(amp);
            return;
        }
        projectile.setEffectAmp((int)(amp + mswcompat$auraAmp.get()));
    }

    @Inject(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearArcaneScaling(ServerWorld world, ItemStack stack, PlayerEntity user, @Nullable Hand hand, CallbackInfo ci) {
        mswcompat$auraAmp.remove();
        mswcompat$shouldScale.remove();
    }
}