package net.pixeldreamstudios.mswcompat.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.soulsweaponry.items.axe.LeviathanAxe;
import net.soulsweaponry.items.bow.DarkmoonLongbow;
import net.soulsweaponry.items.bow.Galeforce;
import net.soulsweaponry.items.bow.KrakenSlayer;
import net.soulsweaponry.items.crossbow.KrakenSlayerCrossbow;
import net.soulsweaponry.items.hammer.Mjolnir;
import net.soulsweaponry.items.hammer.Nightfall;
import net.soulsweaponry.items.hammer.Supernova;
import net.soulsweaponry.items.hammer.Tonitrus;
import net.soulsweaponry.items.katana.Bloodlust;
import net.soulsweaponry.items.katana.Dragonbane;
import net.soulsweaponry.items.katana.Moonveil;
import net.soulsweaponry.items.scythe.DarkinScythePrime;
import net.soulsweaponry.items.scythe.ShadowAssassinScythe;
import net.soulsweaponry.items.spear.DragonslayerSwordspear;
import net.soulsweaponry.items.spear.DraupnirSpear;
import net.soulsweaponry.items.spear.GlaiveOfHodir;
import net.soulsweaponry.items.staff.DragonStaff;
import net.soulsweaponry.items.sword.*;

public final class ItemMatcher {

    private ItemMatcher() {}

    public static boolean isLeviathanAxe(ItemStack stack) {
        return stack.getItem() instanceof LeviathanAxe;
    }

    public static boolean isFrostmourne(ItemStack stack) {
        return stack.getItem() instanceof Frostmourne;
    }

    public static boolean isDarkMoonGreatsword(ItemStack stack) {
        return stack.getItem() instanceof DarkMoonGreatsword;
    }

    public static boolean isMasterSword(ItemStack stack) {
        return stack.getItem() instanceof MasterSword;
    }

    public static boolean isMoonlightGreatsword(ItemStack stack) {
        return stack.getItem() instanceof MoonlightGreatsword;
    }

    public static boolean isHolyMoonlightGreatsword(ItemStack stack) {
        return stack.getItem() instanceof HolyMoonlightGreatsword;
    }

    public static boolean isMoonveil(ItemStack stack) {
        return stack.getItem() instanceof Moonveil;
    }

    public static boolean isBloodlust(ItemStack stack) {
        return stack.getItem() instanceof Bloodlust;
    }

    public static boolean isDarkinBlade(ItemStack stack) {
        return stack.getItem() instanceof DarkinBlade;
    }

    public static boolean isDarkinScythePrime(ItemStack stack) {
        return stack.getItem() instanceof DarkinScythePrime;
    }

    public static boolean isShadowAssassinScythe(ItemStack stack) {
        return stack.getItem() instanceof ShadowAssassinScythe;
    }

    public static boolean isNightsEdge(ItemStack stack) {
        return stack.getItem() instanceof NightsEdgeItem;
    }

    public static boolean isExcalibur(ItemStack stack) {
        return stack.getItem() instanceof Excalibur;
    }

    public static boolean isLichBane(ItemStack stack) {
        return stack.getItem() instanceof LichBane;
    }

    public static boolean isDawnbreaker(ItemStack stack) {
        return stack.getItem() instanceof Dawnbreaker;
    }

    public static boolean isEmpoweredDawnbreaker(ItemStack stack) {
        return stack.getItem() instanceof EmpoweredDawnbreaker;
    }
    public static boolean isNightlordsSword(ItemStack stack) {
        return stack.getItem() instanceof NightlordsSword;
    }
    public static boolean isWhirligigSawblade(ItemStack stack) {
        return stack.getItem() instanceof WhirligigSawblade;
    }

    public static boolean isHeapOfRawIron(ItemStack stack) {
        return stack.getItem() instanceof HeapOfRawIron;
    }

    public static boolean isMjolnir(ItemStack stack) {
        return stack.getItem() instanceof Mjolnir;
    }
    public static boolean isDragonbane(ItemStack stack) {
        return stack.getItem() instanceof Dragonbane;
    }

    public static boolean isNightfall(ItemStack stack) {
        return stack.getItem() instanceof Nightfall;
    }

    public static boolean isSupernova(ItemStack stack) {
        return stack.getItem() instanceof Supernova;
    }

    public static boolean isTonitrus(ItemStack stack) {
        return stack.getItem() instanceof Tonitrus;
    }

    public static boolean isDragonslayerSwordspear(ItemStack stack) {
        return stack.getItem() instanceof DragonslayerSwordspear;
    }

    public static boolean isGlaiveOfHodir(ItemStack stack) {
        return stack.getItem() instanceof GlaiveOfHodir;
    }

    public static boolean isDraupnirSpear(ItemStack stack) {
        return stack.getItem() instanceof DraupnirSpear;
    }

    public static boolean isDragonStaff(ItemStack stack) {
        return stack.getItem() instanceof DragonStaff;
    }

    public static boolean isDarkmoonLongbow(ItemStack stack) {
        return stack.getItem() instanceof DarkmoonLongbow;
    }
    public static boolean isBloodthirster(ItemStack stack) {
        return stack.getItem() instanceof Bloodthirster;
    }
    public static boolean isGaleforce(ItemStack stack) {
        return stack.getItem() instanceof Galeforce;
    }

    public static boolean isKrakenSlayerBow(ItemStack stack) {
        return stack.getItem() instanceof KrakenSlayer;
    }

    public static boolean isKrakenSlayerCrossbow(ItemStack stack) {
        return stack.getItem() instanceof KrakenSlayerCrossbow;
    }
    public static boolean isMoonlightShortsword(ItemStack stack) {
        return stack.getItem() instanceof MoonlightShortsword;
    }

    public static boolean isBluemoonShortsword(ItemStack stack) {
        return stack.getItem() instanceof BluemoonShortsword;
    }
    public static boolean isBluemoonGreatsword(ItemStack stack) {
        return stack.getItem() instanceof BluemoonGreatsword;
    }

    public static boolean isPureMoonlightGreatsword(ItemStack stack) {
        return stack.getItem() instanceof PureMoonlightGreatsword;
    }
    public static boolean isAnyKrakenSlayer(ItemStack stack) {
        return isKrakenSlayerBow(stack) || isKrakenSlayerCrossbow(stack);
    }

    public static boolean isAnyDarkinScythe(ItemStack stack) {
        return isDarkinScythePrime(stack) || isShadowAssassinScythe(stack);
    }

    public static boolean isAnyOmnivamp(ItemStack stack) {
        return isDarkinBlade(stack) || isDarkinScythePrime(stack);
    }

    public static boolean isAnyBleedWeapon(ItemStack stack) {
        return isMoonveil(stack) || isBloodlust(stack);
    }

    public static boolean isAnyPermafrost(ItemStack stack) {
        return isFrostmourne(stack) || isLeviathanAxe(stack) || isDarkMoonGreatsword(stack);
    }

    public static boolean isAnyRainBoost(ItemStack stack) {
        return isDragonslayerSwordspear(stack) || isMjolnir(stack);
    }

    public static boolean isAnyShootMoonlight(ItemStack stack) {
        return isMasterSword(stack) || isMoonlightGreatsword(stack);
    }

    public static Item.Settings getSettings(ItemStack stack) {
        return null;
    }
}