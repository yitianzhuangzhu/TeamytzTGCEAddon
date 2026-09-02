package com.teamytz.tgceaddon.init;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.recipes.CommandLineAmmoSwitchRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * 合成配方注册
 */
@Mod.EventBusSubscriber(modid = TGCEAddon.MODID)
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

    /**
     * 自定义配方注册(1.12.2 自定义 IRecipe 走 RegistryEvent)
     */
    @SubscribeEvent
    public static void registerCustomRecipes(RegistryEvent.Register<IRecipe> event) {
        // 指令线导弹发射器弹药切换(合成台:发射器 + 火箭弹/核火箭 → 切换弹药变体)
        CommandLineAmmoSwitchRecipe recipe = new CommandLineAmmoSwitchRecipe();
        recipe.setRegistryName(new ResourceLocation(TGCEAddon.MODID, "command_line_ammo_switch"));
        event.getRegistry().register(recipe);
        TGCEAddon.getLogger().info("弹药切换配方注册完成");
    }
}
