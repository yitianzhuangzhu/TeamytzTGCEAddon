package com.teamytz.tgceaddon.item;

import com.teamytz.tgceaddon.TGCEAddon;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * 蓝图物品：存储一个或多个目标物品信息，用于通用复制机解锁制作。
 * - 蓝图通过 NBT 标签记录目标物品，预设蓝图由创造物品栏直接提供
 */
public class ItemBlueprint extends Item {

    // ===== NBT 标签键名：目标物品列表 / 名称列表 / 主目标索引 =====
    private static final String TAG_TARGETS = "TargetItems";
    private static final String TAG_NAMES = "TargetNames";
    private static final String TAG_PRIMARY = "PrimaryTarget";

    // ===== 构造：单格堆叠、带子类型 =====
    public ItemBlueprint(String name) {
        this.setRegistryName(TGCEAddon.MODID, name);
        this.setUnlocalizedName(TGCEAddon.MODID + "." + name);
        this.setCreativeTab(com.teamytz.tgceaddon.init.ModCreativeTabs.TAB_TGCEADDON);
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
    }

    // ===== 静态工具方法：创建/查询蓝图目标物品 =====
    /**
     * 创建只包含一个目标物品的蓝图
     */
    public static ItemStack createBlueprint(ItemStack target) {
        ItemStack blueprint = new ItemStack(com.teamytz.tgceaddon.init.ModItems.blueprint);
        NBTTagCompound nbt = new NBTTagCompound();

        NBTTagList targets = new NBTTagList();
        targets.appendTag(target.writeToNBT(new NBTTagCompound()));
        nbt.setTag(TAG_TARGETS, targets);

        NBTTagList names = new NBTTagList();
        names.appendTag(new NBTTagString(target.getDisplayName()));
        nbt.setTag(TAG_NAMES, names);

        nbt.setInteger(TAG_PRIMARY, 0);
        blueprint.setTagCompound(nbt);
        return blueprint;
    }

    /**
     * 获取蓝图中存储的目标物品数量
     */
    public static int getTargetCount(ItemStack blueprint) {
        if (blueprint.isEmpty() || !blueprint.hasTagCompound()) return 0;
        NBTTagCompound nbt = blueprint.getTagCompound();
        if (nbt.hasKey(TAG_TARGETS)) {
            return nbt.getTagList(TAG_TARGETS, 10).tagCount();
        }
        return 0;
    }

    /**
     * 获取指定索引的目标物品
     */
    @Nullable
    public static ItemStack getTargetAt(ItemStack blueprint, int index) {
        if (blueprint.isEmpty() || !blueprint.hasTagCompound()) return null;
        NBTTagCompound nbt = blueprint.getTagCompound();
        if (!nbt.hasKey(TAG_TARGETS)) return null;
        NBTTagList targets = nbt.getTagList(TAG_TARGETS, 10);
        if (index < 0 || index >= targets.tagCount()) return null;
        return new ItemStack(targets.getCompoundTagAt(index));
    }

    /**
     * 获取所有目标物品（仅用于兼容或调试）
     */
    public static List<ItemStack> getAllTargets(ItemStack blueprint) {
        List<ItemStack> list = new ArrayList<>();
        if (blueprint.isEmpty() || !blueprint.hasTagCompound()) return list;
        NBTTagCompound nbt = blueprint.getTagCompound();
        if (!nbt.hasKey(TAG_TARGETS)) return list;
        NBTTagList targets = nbt.getTagList(TAG_TARGETS, 10);
        for (int i = 0; i < targets.tagCount(); i++) {
            list.add(new ItemStack(targets.getCompoundTagAt(i)));
        }
        return list;
    }

    /**
     * 获取指定索引的目标名称
     */
    @Nullable
    public static String getTargetNameAt(ItemStack blueprint, int index) {
        if (blueprint.isEmpty() || !blueprint.hasTagCompound()) return null;
        NBTTagCompound nbt = blueprint.getTagCompound();
        if (!nbt.hasKey(TAG_NAMES)) return null;
        NBTTagList names = nbt.getTagList(TAG_NAMES, 8);
        if (index < 0 || index >= names.tagCount()) return null;
        return names.getStringTagAt(index);
    }

    /**
     * 兼容旧版：获取蓝图存储的单个目标物品
     */
    @Nullable
    public static ItemStack getTargetItem(ItemStack blueprint) {
        return getTargetAt(blueprint, 0);
    }

    /**
     * 兼容旧版：获取蓝图显示名称
     */
    @Nullable
    public static String getTargetName(ItemStack blueprint) {
        return getTargetNameAt(blueprint, 0);
    }

    // ===== 客户端：悬浮提示显示蓝图目标列表 =====
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        int count = getTargetCount(stack);
        if (count > 0) {
            tooltip.add("§b蓝图目标 (" + count + "):");
            for (int i = 0; i < Math.min(count, 5); i++) {
                String name = getTargetNameAt(stack, i);
                if (name != null) {
                    tooltip.add("§7  - " + name);
                }
            }
            if (count > 5) {
                tooltip.add("§7  ... 以及其他 " + (count - 5) + " 项");
            }
        } else {
            tooltip.add("§7蓝图用于通用复制机");
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return this.getUnlocalizedName();
    }
}
