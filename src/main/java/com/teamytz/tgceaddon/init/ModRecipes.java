package com.teamytz.tgceaddon.init;

import com.teamytz.tgceaddon.TGCEAddon;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * 合成配方注册
 */
public class ModRecipes {

    public static void init() {
        TGCEAddon.getLogger().info("注册合成配方...");

        // 爆弹弹夹：1空弹夹 + 3爆弹 → 1爆弹弹夹（2x2 合成，空弹夹在左下角）
        //   B B
        //   E B
        GameRegistry.addShapedRecipe(
            new ResourceLocation(TGCEAddon.MODID, "boltermagazine"),
            null,
            ModAmmoTypes.BOLT_MAGAZINE_FULL.copy(),
            "BB",
            "EB",
            'B', ModAmmoTypes.BOLT_ITEM,
            'E', ModAmmoTypes.BOLT_MAGAZINE_EMPTY);

        TGCEAddon.getLogger().info("配方注册完成");
    }
}
