package net.pixeldreamstudios.mswcompat.mixin.living;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.soulsweaponry.items.abilities.use.ShootRandomProjectile;
import net.soulsweaponry.util.LuckChosenObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Pseudo
@Mixin(value = ShootRandomProjectile.class, remap = false)
public interface WabbajackAccess {

    @Invoker(value = "calculateProjectile", remap = false)
    EntityType<?> mswcompat$pickProjectile(LivingEntity user);

    @Accessor(value = "PROJECTILES", remap = false)
    static List<LuckChosenObject<EntityType<?>>> mswcompat$projectiles() {
        throw new AssertionError();
    }
}
