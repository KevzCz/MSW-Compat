package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.soulsweaponry.items.sword.PureMoonlightGreatsword;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = PureMoonlightGreatsword.class)
public abstract class PureMoonlightGreatswordMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$scale = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        float baseline = ConfigHelper.getBaselineValue("pure_moonlight_greatsword.attack_damage_baseline", 11.0F);
        float factor = 1.0F;
        if (user != null && baseline > 0.0F) {
            double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (ad > 0.0) factor = (float)(ad / baseline);
        }
        mswcompat$scale.set(factor);
    }

    @Redirect(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETSTATIC,
                    target = "Lnet/soulsweaponry/config/ConfigConstructor;pure_moonlight_greatsword_projectile_damage:F",
                    remap = false
            ),
            require = 0
    )
    private float mswcompat$scaleProjectileConfig() {
        return net.soulsweaponry.config.ConfigConstructor.pure_moonlight_greatsword_projectile_damage * mswcompat$scale.get();
    }

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        mswcompat$scale.remove();
    }
}