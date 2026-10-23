package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.DamageScalingHelper;
import net.soulsweaponry.items.abilities.use.UmbralTrespass;
import net.soulsweaponry.items.scythe.DarkinScythePrime;
import net.soulsweaponry.items.scythe.ShadowAssassinScythe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = UmbralTrespass.class, remap = false)
public abstract class UmbralTrespassMixin {
    @Unique
    private static final ThreadLocal<Float> mswcompat$factor = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(
            method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/util/TypedActionResult;",
            at = @At("HEAD"),
            remap = true
    )
    private void mswcompat$cacheTrespassFactor(World world, PlayerEntity user, Hand hand, ItemStack stack, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        float baseline = 0.0F;

        Item item = stack.getItem();
        if (item instanceof DarkinScythePrime) {
            baseline = ConfigHelper.getBaselineValue("umbral_trespass.darkin_scythe_baseline", 12.0F);
        } else if (item instanceof ShadowAssassinScythe) {
            baseline = ConfigHelper.getBaselineValue("umbral_trespass.shadow_assassin_scythe_baseline", 13.0F);
        }

        mswcompat$factor.set(DamageScalingHelper.attackDamageOnly(
                user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE), baseline));
    }

    @ModifyArg(
            method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/util/TypedActionResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entitydata/UmbralTrespassData;setOtherStats(Lnet/minecraft/entity/LivingEntity;FIFD)V",
                    remap = true
            ),
            index = 1,
            remap = true
    )
    private float mswcompat$scaleTrespassDamage(float damage) {
        return damage * mswcompat$factor.get();
    }

    @Inject(
            method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/util/TypedActionResult;",
            at = @At("RETURN"),
            remap = true
    )
    private void mswcompat$clearTrespassFactor(World world, PlayerEntity user, Hand hand, ItemStack stack, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        mswcompat$factor.remove();
    }
}
