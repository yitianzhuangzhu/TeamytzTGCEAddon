package com.teamytz.tgceaddon.item;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.init.ModCreativeTabs;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * 普通炮塔（turret）的炮弹弹药物品 —— HE（高爆弹）与 APFSDS（尾翼稳定脱壳穿甲弹）。
 *
 * 与 {@link ItemCannonShell}（BMPT 机炮炮弹）同样是最朴素的物品：只作为炮塔基座
 * 弹药槽里的消耗品被识别，具体的飞行/命中行为在对应的弹丸实体里
 * （{@code HEShellProjectile} / {@code APFSDSShellProjectile}）。
 *
 * 一个类带两种弹药（靠注册名区分），方便炮塔侧用 {@code instanceof ItemTurretShell}
 * 一次判断"这是不是普通炮塔的弹药"。
 */
public class ItemTurretShell extends Item {

    /** 悬浮提示最多读取的行数（语言键 <unlocalized>.tip / .tip2 / .tip3 ...） */
    private static final int TIP_LINES = 4;

    public ItemTurretShell(String name) {
        this.setRegistryName(TGCEAddon.MODID, name);
        this.setUnlocalizedName(TGCEAddon.MODID + "." + name);
        this.setCreativeTab(ModCreativeTabs.TAB_TGCEADDON);
        this.setMaxStackSize(64);
    }

    /** 悬浮提示：说明这种弹药的飞行与命中特性（读取本物品自己的语言键，便于两种弹药各写各的） */
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        String base = this.getUnlocalizedName();
        for (int i = 1; i <= TIP_LINES; i++) {
            String key = base + ".tip" + (i == 1 ? "" : String.valueOf(i));
            String line = I18n.format(key);
            if (!line.equals(key)) {
                tooltip.add(line);
            }
        }
    }
}
