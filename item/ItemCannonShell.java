package com.teamytz.tgceaddon.item;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.init.ModCreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

/**
 * 机炮炮弹
 * BMPT 炮塔机炮的弹药，放入炮塔基座弹药输入槽被消耗。
 * 材质暂为占位，后续由美术提供。
 */
public class ItemCannonShell extends Item {

    public ItemCannonShell(String name) {
        this.setRegistryName(new ResourceLocation(TGCEAddon.MODID, name));
        this.setUnlocalizedName(TGCEAddon.MODID + "." + name);
        this.setCreativeTab(ModCreativeTabs.TAB_TGCEADDON);
        this.setMaxStackSize(64);
    }
}
