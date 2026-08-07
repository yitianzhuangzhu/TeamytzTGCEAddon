package com.teamytz.tgceaddon.compat;

import com.teamytz.tgceaddon.TGCEAddon;

import net.minecraftforge.fml.common.Loader;

/**
 * 兼容性处理类
 * 处理与其他Mod的兼容性
 */
public class ModCompat
{
    public static boolean ic2Loaded = false;
    public static boolean thermalExpansionLoaded = false;
    public static boolean techgunsLoaded = false;

    public static void init()
    {
        TGCEAddon.getLogger().info("检查Mod兼容性...");

        ic2Loaded = Loader.isModLoaded("ic2");
        thermalExpansionLoaded = Loader.isModLoaded("thermalexpansion");
        techgunsLoaded = Loader.isModLoaded("techguns");

        if (ic2Loaded)
        {
            TGCEAddon.getLogger().info("检测到工业时代2，启用兼容性支持");
            IC2Compat.init();
        }

        if (thermalExpansionLoaded)
        {
            TGCEAddon.getLogger().info("检测到热力膨胀，启用兼容性支持");
            ThermalCompat.init();
        }

        if (techgunsLoaded)
        {
            TGCEAddon.getLogger().info("检测到科技枪，启用兼容性支持");
            TechgunsCompat.init();
        }
    }
}