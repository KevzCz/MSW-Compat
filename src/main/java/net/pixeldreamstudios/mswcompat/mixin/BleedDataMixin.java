package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.pixeldreamstudios.mswcompat.util.MswCompatBleedContext;
import net.pixeldreamstudios.mswcompat.util.MswCompatDebug;
import net.soulsweaponry.entitydata.BleedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = BleedData.class, remap = false)
public abstract class BleedDataMixin {

    @Inject(
            method = "addBleed(Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("HEAD"),
            remap = true
    )
    private static void mswcompat$debugAddBleed(LivingEntity entity, int amount, CallbackInfo ci) {
        MswCompatBleedContext.Snapshot snapshot = MswCompatBleedContext.consume();
        if (snapshot == null) {
            snapshot = new MswCompatBleedContext.Snapshot();
            snapshot.target = entity;
            snapshot.source = "BleedData (unknown source)";
        } else if (snapshot.target == null) {
            snapshot.target = entity;
        }
        snapshot.scaledAmount = amount;
        MswCompatDebug.logBleedBuildup(snapshot, amount);
    }
}
