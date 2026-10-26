package com.teamytz.tgceaddon.item;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.init.ModCreativeTabs;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

/**
 * 炮塔升级卡（0.26.1 起为**单一通用卡片**）
 *
 * 放入炮塔基座的升级槽（槽位 19）即生效：**卡片本身不区分类型**，
 * 由当场那座炮塔的 AI 自行"解锁"它的专属强化：
 * - 激光炮塔        → 一次射出多道光束（MULTI_BEAM_COUNT）
 * - 闪电炮塔        → 闪电连锁目标数增加（CHAIN_TARGETS_UPGRADED）
 * - 激光炮塔 Mk.1   → 射击间隔增大、爆炸更强并破坏徒手可挖的方块（过载弹）
 * - BMPT / 罗兰     → 计算移动目标提前量；罗兰另有"有核弹→核弹变体"
 *
 * 判定接口：{@code TurretBaseTileEntMaster.hasUpgrade()} / {@code EntityBaseTurret.hasUpgrade()}，
 * 每发实时查询 → 插卡/拔卡立即生效，无需拆塔重建。
 *
 * 兼容性：0.26.0 的旧卡片（NBT {@code upgradeType=...}）仍然有效——
 * 现在任何一张炮塔升级卡都满足判定，旧的类型标签被忽略。
 */
public class ItemTurretUpgrade extends Item {

    public static ItemTurretUpgrade INSTANCE;

    /** 悬浮提示最多尝试读取的行数（语言键 tip / tip2 / tip3 ...） */
    private static final int TIP_LINES = 8;

    public ItemTurretUpgrade(String name) {
        setRegistryName(name);
        setUnlocalizedName(TGCEAddon.MODID + "." + name);
        setCreativeTab(ModCreativeTabs.TAB_TGCEADDON);
        setMaxStackSize(1);
        INSTANCE = this;
    }

    /** 该物品堆是否是炮塔升级卡（槽位校验与炮塔判定共用） */
    public static boolean isUpgrade(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemTurretUpgrade;
    }

    /**
     * 悬浮提示：语言键 item.tgceaddon.turret_upgrade.tip / tip2 / tip3 ...
     * 从上往下读，遇到缺失的键跳过（不中断），便于后续增删说明行。
     */
    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        for (int i = 1; i <= TIP_LINES; i++) {
            String key = "item.tgceaddon.turret_upgrade.tip" + (i == 1 ? "" : String.valueOf(i));
            String line = I18n.format(key);
            if (!line.equals(key)) {
                tooltip.add(line);
            }
        }
    }
}
