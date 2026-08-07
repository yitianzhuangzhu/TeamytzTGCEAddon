package com.teamytz.tgceaddon.gui;

import com.teamytz.tgceaddon.item.ItemBlueprint;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * 通用复制机的蓝图输入槽：只允许放入 ItemBlueprint 类物品
 */
public class SlotBlueprintInput extends SlotItemHandler {

    public SlotBlueprintInput(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    // ===== 限制只有蓝图物品才能放入此槽 =====
    @Override
    public boolean isItemValid(ItemStack stack) {
        return stack.getItem() instanceof ItemBlueprint;
    }
}
