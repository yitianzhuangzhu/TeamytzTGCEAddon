package com.teamytz.tgceaddon.init;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.item.ItemBlueprint;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/**
 * 创造模式物品栏 - 测试枪
 */
public class ModCreativeTabs
{
    public static final CreativeTabs TAB_TGCEADDON = new CreativeTabs(TGCEAddon.MODID)
    {
        @Override
        public ItemStack getTabIconItem()
        {
            return new ItemStack(ModItems.testPistol);
        }
        
        @Override
        public void displayAllRelevantItems(NonNullList<ItemStack> items)
        {
            super.displayAllRelevantItems(items);
            
            // 添加爆弹枪弹药到创造模式物品栏
            // 注意：爆弹、弹夹等物品没有设置creativeTab，所以需要手动添加
            // 爆弹
            items.add(ModAmmoTypes.BOLT_ITEM.copy());
            // 满弹夹
            items.add(ModAmmoTypes.BOLT_MAGAZINE_FULL.copy());
            // 空弹夹
            items.add(ModAmmoTypes.BOLT_MAGAZINE_EMPTY.copy());
            // 注意：纯洁圣印已经通过 setCreativeTab 自动注册，不需要手动添加
            // 蓝图本体已通过 setCreativeTab 自动显示，这里仅添加带 NBT 标签的预设蓝图变体
            // 链锯剑蓝图
            items.add(ItemBlueprint.createBlueprint(new ItemStack(ModItems.chainsword)));
            // 动力剑蓝图
            items.add(ItemBlueprint.createBlueprint(new ItemStack(ModItems.powersword)));
            // 雷霆锤蓝图
            items.add(ItemBlueprint.createBlueprint(new ItemStack(ModItems.thunderhammer)));
            // 爆弹枪蓝图
            items.add(ItemBlueprint.createBlueprint(new ItemStack(ModItems.bolter)));
            // 爆弹蓝图
            items.add(ItemBlueprint.createBlueprint(ModAmmoTypes.BOLT_ITEM.copy()));
            // 添加通用复制机
            items.add(new ItemStack(ModBlocks.UNIVERSAL_COPIER));
        }
    };
}
