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
            // 普通炮塔两种弹药的蓝图（通用复制机生产 HE 高爆弹 / APFSDS 穿甲弹）
            items.add(ItemBlueprint.createBlueprint(new ItemStack(ModItems.heShell)));
            items.add(ItemBlueprint.createBlueprint(new ItemStack(ModItems.apfsdsShell)));
            // 热诱弹蓝图（通用复制机可复制热诱弹,1000RF 产出 16 个,热诱弹发射器弹药）
            items.add(ItemBlueprint.createBlueprint(new ItemStack(ModItems.flare)));
            // 带 bmpt NBT 的炮塔卡片（放入炮塔控制器卡片槽生成 BMPT 炮塔实体）
            ItemStack bmptCard = new ItemStack(ModItems.turretCard);
            bmptCard.setTagCompound(new NBTTagCompound());
            bmptCard.getTagCompound().setString("turretType", "bmpt");
            items.add(bmptCard);
            // 带 roland NBT 的炮塔卡片（生成罗兰防空炮塔实体）
            ItemStack rolandCard = new ItemStack(ModItems.turretCard);
            rolandCard.setTagCompound(new NBTTagCompound());
            rolandCard.getTagCompound().setString("turretType", "roland");
            items.add(rolandCard);
            // 炮塔升级卡(turretUpgrade)是单一通用卡,已由 setCreativeTab 自动进入本物品栏,无需手动添加
            // ===== 0.21.0 新炮塔卡片（模型/贴图已接入；武器与俯仰待实现）=====
            items.add(createTurretCard("turret"));
            items.add(createTurretCard("laser_turret"));
            items.add(createTurretCard("laser_turret_mk1"));
            items.add(createTurretCard("lightning_turret"));
            // 0.30.0 M1 主战坦克炮塔（HE/APFSDS 主炮，72 格射程）
            items.add(createTurretCard("m1"));
        }

        /** 生成带 turretType NBT 的炮塔卡片（卡片槽放入后生成对应炮塔实体）
         *  注意：Java 8 不允许匿名类内定义 static 方法，故此处为实例方法 */
        private ItemStack createTurretCard(String type) {
            ItemStack card = new ItemStack(ModItems.turretCard);
            card.setTagCompound(new NBTTagCompound());
            card.getTagCompound().setString("turretType", type);
            return card;
        }
    };
}
