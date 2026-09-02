package com.teamytz.tgceaddon.item;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.init.ModCreativeTabs;
import net.minecraft.item.Item;

/**
 * 热诱弹(消耗品)
 *
 * 由热诱弹发射器消耗:右键发射器在玩家位置生成一个热诱弹实体(200 红外热值,
 * 诱骗红外热追踪导弹)。可通过通用复制机 + 蓝图生产(1000 RF 产出 16 个)。
 */
public class ItemFlare extends Item {

    public ItemFlare(String name) {
        this.setRegistryName(TGCEAddon.MODID, name);
        this.setUnlocalizedName(TGCEAddon.MODID + "." + name);
        this.setCreativeTab(ModCreativeTabs.TAB_TGCEADDON);
        this.setMaxStackSize(64);
    }
}
