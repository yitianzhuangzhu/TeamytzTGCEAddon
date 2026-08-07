package com.teamytz.tgceaddon.item;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.init.ModCreativeTabs;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import techguns.items.armors.GenericShield;

/**
 * 风暴盾牌
 * 继承科技枪的GenericShield，行为参考原版盾牌
 * 渲染通过 ItemRenderHack.registerItemRenderer 注册自定义 IItemRenderer
 */
public class ItemStormShield extends GenericShield {
    public static ItemStormShield INSTANCE;

    public ItemStormShield(String name) {
        super(TGCEAddon.MODID, name, 2000, false, 1);
        this.setCreativeTab(null); // 从创造模式物品栏移除
        INSTANCE = this;
    }

    /**
     * 初始化模型（必须调用，否则会显示紫块）
     * 继承自 GenericShield，设置原版盾牌的 inventory 模型
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void initModel() {
        super.initModel();
    }
}
