package com.teamytz.tgceaddon.item;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.init.ModCreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * 炮塔升级卡
 *
 * 放入大型炮塔(炮塔基座)的升级槽(原科技枪炮台防护板槽位位置)生效。
 * 通过 NBT upgradeType 区分升级类型:
 * - lead_computing: 运算卡片——炮塔计算移动目标的提前量再开火
 */
public class ItemTurretUpgrade extends Item {

    /** 运算卡片:计算移动目标提前量 */
    public static final String TYPE_LEAD_COMPUTING = "lead_computing";

    public static ItemTurretUpgrade INSTANCE;

    public ItemTurretUpgrade(String name) {
        setRegistryName(name);
        setUnlocalizedName(TGCEAddon.MODID + "." + name);
        setCreativeTab(ModCreativeTabs.TAB_TGCEADDON);
        setMaxStackSize(1);
        INSTANCE = this;
    }

    /** 读取升级卡的升级类型(NBT upgradeType) */
    public static String getUpgradeType(ItemStack stack) {
        if (stack.isEmpty()) {
            return "";
        }
        NBTTagCompound tags = stack.getTagCompound();
        return tags != null && tags.hasKey("upgradeType") ? tags.getString("upgradeType") : "";
    }

    /** 创建指定类型的升级卡(带 NBT 标签,创造栏中提供预设) */
    public static ItemStack createUpgrade(String type) {
        ItemStack stack = new ItemStack(INSTANCE);
        stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setString("upgradeType", type);
        return stack;
    }
}
