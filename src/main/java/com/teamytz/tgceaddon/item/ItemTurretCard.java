package com.teamytz.tgceaddon.item;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.init.ModCreativeTabs;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.List;

/**
 * 炮塔卡片
 * 放入炮塔控制器 GUI 的卡片槽，可生成对应类型的炮塔实体。
 * 炮塔类型通过 NBT "turretType" 指定（实体生成细节后续补充）。
 */
public class ItemTurretCard extends Item {

    public static final String TAG_TURRET_TYPE = "turretType";

    public ItemTurretCard(String name) {
        this.setRegistryName(new ResourceLocation(TGCEAddon.MODID, name));
        this.setUnlocalizedName(TGCEAddon.MODID + "." + name);
        this.setCreativeTab(ModCreativeTabs.TAB_TGCEADDON);
        this.setMaxStackSize(1);
    }

    /**
     * 读取卡片指定的炮塔类型
     */
    public static String getTurretType(ItemStack stack) {
        if (!stack.isEmpty() && stack.hasTagCompound() && stack.getTagCompound().hasKey(TAG_TURRET_TYPE)) {
            return stack.getTagCompound().getString(TAG_TURRET_TYPE);
        }
        return "";
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        String type = getTurretType(stack);
        if (!type.isEmpty()) {
            tooltip.add(net.minecraft.client.resources.I18n.format("item.tgceaddon.turret_card.type") + ": " + type);
        } else {
            tooltip.add(net.minecraft.client.resources.I18n.format("item.tgceaddon.turret_card.type") + ": "
                    + net.minecraft.client.resources.I18n.format("item.tgceaddon.turret_card.unknown"));
        }
    }
}
