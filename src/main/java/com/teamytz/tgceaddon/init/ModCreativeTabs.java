package com.teamytz.tgceaddon.init;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.item.ItemBlueprint;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import techguns.TGItems;

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
            
            // 注意：
            // - 炮塔基座、基座支撑、通用复制机、机炮炮弹已通过 setCreativeTab 自动注册，无需手动添加
            // - 爆弹、弹夹等共享物品通过 Techguns 共享物品机制注册（enabled=false，科技枪物品栏不显示），需手动补充到本模组物品栏
            // 这里仅添加带 NBT 标签的预设蓝图变体（无法自动显示）
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
            // 爆弹弹夹（满）
            items.add(ModAmmoTypes.BOLT_MAGAZINE_FULL.copy());
            // 爆弹空弹夹
            items.add(ModAmmoTypes.BOLT_MAGAZINE_EMPTY.copy());
            // 爆弹（单发）
            items.add(ModAmmoTypes.BOLT_ITEM.copy());
            // 火箭弹蓝图（通用复制机可复制科技枪火箭弹，BMPT 导弹弹药）
            items.add(ItemBlueprint.createBlueprint(TGItems.ROCKET.copy()));
            // 机炮炮弹蓝图（通用复制机可复制机炮炮弹，BMPT 机炮弹药）
            items.add(ItemBlueprint.createBlueprint(new ItemStack(ModItems.cannonShell)));
            // 带 bmpt NBT 的炮塔卡片（放入炮塔控制器卡片槽生成 BMPT 炮塔实体）
            ItemStack bmptCard = new ItemStack(ModItems.turretCard);
            bmptCard.setTagCompound(new NBTTagCompound());
            bmptCard.getTagCompound().setString("turretType", "bmpt");
            items.add(bmptCard);
        }
    };
}
