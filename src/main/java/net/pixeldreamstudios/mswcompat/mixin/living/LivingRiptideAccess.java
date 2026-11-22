package net.pixeldreamstudios.mswcompat.mixin.living;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingRiptideAccess {

    @Accessor("riptideTicks")
    void mswcompat$setRiptideTicks(int ticks);

    @Accessor("riptideAttackDamage")
    void mswcompat$setRiptideDamage(float damage);

    @Accessor("riptideStack")
    void mswcompat$setRiptideStack(ItemStack stack);

    @Invoker("setLivingFlag")
    void mswcompat$setLivingFlag(int flag, boolean value);

    @Accessor("USING_RIPTIDE_FLAG")
    static int mswcompat$riptideFlag() {
        throw new AssertionError();
    }
}
