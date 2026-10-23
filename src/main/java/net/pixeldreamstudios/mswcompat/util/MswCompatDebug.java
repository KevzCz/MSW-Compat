package net.pixeldreamstudios.mswcompat.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;

public final class MswCompatDebug {

    private MswCompatDebug() {}

    public static void chat(LivingEntity entity, String message) {
        if (!ConfigHelper.isDebugLogging() || entity == null) {
            return;
        }
        if (entity instanceof ServerPlayerEntity player) {
            player.sendMessage(Text.literal("[MSW-Compat] " + message), false);
        }
    }

    public static void logBleedBuildup(MswCompatBleedContext.Snapshot snapshot, int appliedAmount) {
        if (!ConfigHelper.isDebugLogging()) {
            return;
        }

        LivingEntity notify = snapshot.attacker;
        if (notify == null && snapshot.target instanceof ServerPlayerEntity targetPlayer) {
            notify = targetPlayer;
        }
        if (notify == null) {
            return;
        }

        String targetName = snapshot.target != null ? snapshot.target.getName().getString() : "?";
        String detail;
        if (snapshot.baseAmount >= 0 && snapshot.scaledAmount >= 0) {
            detail = String.format(
                    "[%s] bleed %d -> %d (x%.2f) on %s",
                    snapshot.source, snapshot.baseAmount, appliedAmount, snapshot.factor, targetName);
        } else {
            detail = String.format("[%s] +%d bleed on %s", snapshot.source, appliedAmount, targetName);
        }
        chat(notify, detail);
    }

    public static void logBloodLoss(LivingEntity entity, float baseDamage, float scaledDamage, float factor) {
        if (!ConfigHelper.isDebugLogging()) {
            return;
        }
        chat(
                entity,
                String.format("Blood loss: %.1f -> %.1f (x%.2f)", baseDamage, scaledDamage, factor));
    }
}

