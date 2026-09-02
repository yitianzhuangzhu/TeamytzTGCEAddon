package com.teamytz.tgceaddon.gui;

import com.teamytz.tgceaddon.item.ItemTurretUpgrade;
import net.minecraft.item.ItemStack;
import techguns.gui.widgets.SlotMachineInput;
import techguns.gui.widgets.SlotTG;
import techguns.tileentities.operation.ItemStackHandlerPlus;

/**
 * 炮塔升级卡槽:只允许放入炮塔升级卡(ItemTurretUpgrade)。
 * 对应科技枪炮台 GUI 中"防护板槽位"的位置(85, 42)。
 */
public class SlotTurretUpgrade extends SlotMachineInput {

    public SlotTurretUpgrade(ItemStackHandlerPlus itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return stack.getItem() instanceof ItemTurretUpgrade;
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }

    @Override
    public String getSlotTexture() {
        // 复用科技枪炮台防护板槽位的空槽纹理(注意科技枪字段拼写为 TURTETARMORSLOT_TEX)
        return SlotTG.TURTETARMORSLOT_TEX.toString();
    }
}
