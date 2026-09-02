package com.teamytz.tgceaddon.recipes;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.init.ModAmmoTypes;
import com.teamytz.tgceaddon.init.ModItems;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import techguns.TGItems;

/**
 * 指令线导弹发射器弹药切换配方(仿照科技枪 AmmoSwitchRecipe)
 *
 * 科技枪的弹药种类存在武器 NBT 的 ammovariant 标签里,通过合成台配方切换:
 *   合成台:指令线导弹发射器 + 火箭弹/核火箭 → 发射器(ammovariant 改为对应变体)
 * - 放核火箭 → ammovariant=nuke → 发射核导弹变体
 * - 放普通火箭 → ammovariant=default → 发射普通导弹
 * 切换后弹药计数重置为 1,玩家重新装填对应变体的弹药。
 */
public class CommandLineAmmoSwitchRecipe extends ShapelessOreRecipe {

    public CommandLineAmmoSwitchRecipe() {
        super(new ResourceLocation(TGCEAddon.MODID, "command_line_ammo_switch"),
                new ItemStack(ModItems.commandLineMissileLauncher),
                new ItemStack(ModItems.commandLineMissileLauncher),
                // 任一种火箭弹药均可触发切换
                // 注意:运行时 Forge 14.23.5.2864 的 ShapelessOreRecipe 构造器把材料交给
                // CraftingHelper.getIngredient(Object),已不再接受 ItemStack[] 数组,
                // 必须用 Ingredient.fromStacks 表达"任一匹配"(meta 敏感,ROCKET=@7 / ROCKET_NUKE=@117)
                Ingredient.fromStacks(TGItems.ROCKET, TGItems.ROCKET_NUKE));
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack gun = ItemStack.EMPTY;
        ItemStack ammo = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack s = inv.getStackInSlot(i);
            if (s.isEmpty()) {
                continue;
            }
            if (s.getItem() == ModItems.commandLineMissileLauncher) {
                gun = s;
            } else {
                ammo = s;
            }
        }
        if (gun.isEmpty() || ammo.isEmpty()) {
            return super.getCraftingResult(inv);
        }
        ItemStack out = gun.copy();
        NBTTagCompound tags = out.getTagCompound();
        if (tags == null) {
            tags = new NBTTagCompound();
            out.setTagCompound(tags);
        }
        // 按合成台上的弹药确定变体:普通火箭 → default,核火箭 → nuke
        String variant = ModAmmoTypes.ROCKETS_WITH_NUKE.getAmmoVariantKeyfor(ammo, 0);
        tags.setString("ammovariant", variant);
        // 切换后弹药计数重置(与科技枪一致,重新装填新变体弹药)
        tags.setShort("ammo", (short) 1);
        return out;
    }
}
