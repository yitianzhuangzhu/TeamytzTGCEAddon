package com.teamytz.tgceaddon.compat;

import com.teamytz.tgceaddon.TGCEAddon;

/**
 * 科技枪兼容性处理
 */
public class TechgunsCompat
{
    public static void init()
    {
        TGCEAddon.getLogger().info("初始化科技枪兼容性...");

        // 添加科技枪相关的配方和物品转换
        registerRecipes();
    }

    private static void registerRecipes()
    {
        // TODO: 添加与科技枪的兼容配方
        // 例如：使用科技枪的材料来制作新武器
        // 与科技枪的弹药系统兼容
    }
}