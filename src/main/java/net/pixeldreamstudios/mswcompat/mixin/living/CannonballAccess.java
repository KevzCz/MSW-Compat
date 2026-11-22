package net.pixeldreamstudios.mswcompat.mixin.living;

import net.soulsweaponry.items.abilities.use.ShootCannonball;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(value = ShootCannonball.class, remap = false)
public interface CannonballAccess {

    @Accessor(value = "launchPower", remap = false)
    float mswcompat$launchPower();
}
