package com.teamytz.tgceaddon.init;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import techguns.TGItems;
import techguns.items.guns.ammo.AmmoType;
import techguns.init.ITGInitializer;
import techguns.api.tginventory.TGSlotType;

/**
 * 爆弹弹药类型注册
 */
public class ModAmmoTypes implements ITGInitializer {

    public static AmmoType BOLTS;
    public static AmmoType BOLT_MAGAZINE;
    
    public static ItemStack BOLT_ITEM;
    public static ItemStack BOLT_MAGAZINE_FULL;
    public static ItemStack BOLT_MAGAZINE_EMPTY;

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        // 创建爆弹弹药类型
        // BOLTS - 单个爆弹（用于合成弹夹）
        BOLT_ITEM = TGItems.SHARED_ITEM.addsharedVariant("bolts", TGSlotType.AMMOSLOT);
        BOLTS = new AmmoType(BOLT_ITEM);
        
        // BOLT_MAGAZINE - 爆弹弹夹（满的和空的）
        // 容量20发
        BOLT_MAGAZINE_FULL = TGItems.SHARED_ITEM.addsharedVariant("boltermagazine", TGSlotType.AMMOSLOT);
        BOLT_MAGAZINE_EMPTY = TGItems.SHARED_ITEM.addsharedVariant("boltermagazineempty", TGSlotType.AMMOSLOT);
        BOLT_MAGAZINE = new AmmoType(
            BOLT_MAGAZINE_FULL,
            BOLT_MAGAZINE_EMPTY,
            BOLT_ITEM,  // 使用爆弹作为弹药
            20  // 容量20发
        );
    }

    @Override
    public void init(FMLInitializationEvent event) {
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
    }
}
