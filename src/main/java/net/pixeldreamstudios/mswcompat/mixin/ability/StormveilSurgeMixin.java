package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.items.abilities.posthit.StormveilSurge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = StormveilSurge.class, remap = false )
public abstract class StormveilSurgeMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$damageScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<ServerPlayerEntity> mswcompat$channeler = new ThreadLocal<>();
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isTonitrus(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (! shouldScale) {
            mswcompat$damageScale.set(1.0F);
            mswcompat$channeler.set(null);
            return;
        }

        float scale = 1.0F;
        if (attacker != null) {
            float lightningBaseline = ConfigHelper.getBaselineValue("tonitrus.lightning_baseline", 40.0F);

            RegistryEntry.Reference<EntityAttribute> entry = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.LIGHTNING);
            if (entry != null && lightningBaseline > 0.0F) {
                double power = attacker.getAttributeValue(entry);
                scale = 1.0F + (float)(power / lightningBaseline);
            }

            if (attacker instanceof ServerPlayerEntity sp) {
                mswcompat$channeler.set(sp);
            }
        }

        mswcompat$damageScale.set(scale);
    }

    @ModifyArg(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/items/abilities/ChainLightning;trigger(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;FD)V",
                    remap = false
            ),
            index = 3,
            require = 0
    )
    private float mswcompat$scaleChainLightningDamage(float baseDamage) {
        if (! mswcompat$shouldScale.get()) {
            return baseDamage;
        }
        return baseDamage * mswcompat$damageScale.get();
    }

    @Redirect(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"
            ),
            require = 0
    )
    private boolean mswcompat$spawnWithChanneler(World world, Entity entity) {
        if (mswcompat$shouldScale.get() && entity instanceof net.minecraft.entity.LightningEntity lightning) {
            ServerPlayerEntity sp = mswcompat$channeler.get();
            if (sp != null) {
                lightning.setChanneler(sp);
            }
        }
        return world.spawnEntity(entity);
    }

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        mswcompat$damageScale.remove();
        mswcompat$channeler.remove();
        mswcompat$shouldScale.remove();
    }
}