package net.pixeldreamstudios.mswcompat.mixin.living;

import net.soulsweaponry.items.abilities.use.WitherSoulRelease;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(value = WitherSoulRelease.class, remap = false)
public interface WitherReleaseAccess {

    @Accessor(value = "baseSkullExplosionPower", remap = false)
    float mswcompat$basePower();

    @Accessor(value = "criticalSkullExplosionPower", remap = false)
    float mswcompat$criticalPower();
}
