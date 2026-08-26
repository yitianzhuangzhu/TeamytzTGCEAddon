package com.teamytz.tgceaddon.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * 服务端代理基类
 */
public class CommonProxy
{
    public void preInit(FMLPreInitializationEvent event)
    {
        // 服务端预初始化逻辑
        registerEntityRenderers();
        registerCapabilities();
    }

    protected void registerEntityRenderers()
    {
        // 在客户端覆盖此方法注册实体渲染器
    }

    protected void registerCapabilities()
    {
        // 在客户端覆盖此方法注册能力
    }

    public void init(FMLInitializationEvent event)
    {
        // 服务端初始化逻辑
        registerItemRenderers();
    }

    protected void registerItemRenderers()
    {
        // 在客户端覆盖此方法注册物品渲染器
    }

    public void postInit(FMLPostInitializationEvent event)
    {
        // 服务端后初始化逻辑
    }
}