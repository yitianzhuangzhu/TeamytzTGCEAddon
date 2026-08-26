package com.teamytz.tgceaddon.compat;

import com.teamytz.tgceaddon.TGCEAddon;

/**
 * 热力膨胀兼容性处理
 */
public class ThermalCompat
{
    public static void init()
    {
        TGCEAddon.getLogger().info("初始化热力膨胀兼容性...");

        // 添加热力膨胀相关的配方和物品转换
        registerRecipes();
    }

    private static void registerRecipes()
    {
        // TODO: 添加与热力膨胀的兼容配方
        // 例如：使用热力膨胀的机器来处理新材料
        // 使用热力膨胀的能量系统
    }
}