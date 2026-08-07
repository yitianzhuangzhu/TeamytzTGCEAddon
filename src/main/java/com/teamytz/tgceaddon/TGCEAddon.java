package com.teamytz.tgceaddon;

import com.teamytz.tgceaddon.compat.ModCompat;
import com.teamytz.tgceaddon.init.ModAmmoTypes;
import com.teamytz.tgceaddon.init.ModBlocks;
import com.teamytz.tgceaddon.init.ModEntities;
import com.teamytz.tgceaddon.init.ModGuiHandler;
import com.teamytz.tgceaddon.init.ModItems;
import com.teamytz.tgceaddon.init.ModRecipes;
import com.teamytz.tgceaddon.network.PacketHandler;
import com.teamytz.tgceaddon.proxy.CommonProxy;
import com.teamytz.tgceaddon.tileentities.UniversalCopierTileEnt;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;

/**
 * Mod主类
 * 提供新武器、材料和配方
 */
@Mod(modid = TGCEAddon.MODID,
     name = TGCEAddon.NAME,
     version = TGCEAddon.VERSION,
     dependencies = "required-after:techguns@[2.1.3,);"
                 + "after:ic2;"
                 + "after:thermalexpansion;"
                 + "after:thermalfoundation;"
                 + "after:baubles")
public class TGCEAddon
{
    public static final String MODID = "tgceaddon";
    public static final String NAME = "Tech Guns Community Edition Addon";
    public static final String VERSION = "0.6.1";

    @Mod.Instance(MODID)
    public static TGCEAddon instance;

    @SidedProxy(clientSide = "com.teamytz.tgceaddon.proxy.ClientProxy",
                serverSide = "com.teamytz.tgceaddon.proxy.CommonProxy")
    public static CommonProxy proxy;

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        logger.info("TGCEAddon 开始预初始化...");

        // 注册网络包处理器
        PacketHandler.init();

        // 初始化 GUI 处理器
        ModGuiHandler.init();

        // 初始化弹药类型
        new ModAmmoTypes().preInit(event);
        
        // 初始化物品
        ModItems.init();

        // 初始化方块
        ModBlocks.init();

        // 注册 TileEntity
        GameRegistry.registerTileEntity(UniversalCopierTileEnt.class,
            new ResourceLocation(MODID, "universal_copier"));

        // 初始化实体
        ModEntities.init();

        // 调用代理的 preInit - 注册实体渲染器
        proxy.preInit(event);

        logger.info("TGCEAddon 预初始化完成");
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        logger.info("TGCEAddon 开始初始化...");

        // 注册合成配方
        ModRecipes.init();
        
        // 客户端渲染注册
        proxy.init(event);

        logger.info("TGCEAddon 初始化完成");
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        logger.info("TGCEAddon 开始后初始化...");

        // 兼容性处理
        ModCompat.init();

        // 客户端后初始化
        proxy.postInit(event);

        logger.info("TGCEAddon 后初始化完成");
    }

    public static Logger getLogger()
    {
        return logger;
    }
}
