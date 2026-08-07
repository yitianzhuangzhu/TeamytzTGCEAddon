package com.teamytz.tgceaddon.init;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.entities.projectiles.BoltProjectile;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class ModEntities
{
    public static void init()
    {
        TGCEAddon.getLogger().info("注册实体...");
        
        // 在 preInit 阶段注册实体
        // registerModEntity(ResourceLocation, Class, String, int, Object, int, int, boolean)
        // 注意：第5个参数是mod ID字符串，不是mod实例
        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "bolt_projectile"),
            BoltProjectile.class,
            "bolt_projectile",
            1,  // ID
            TGCEAddon.MODID,  // mod ID 字符串
            128,  // tracking range (与科技枪一致)
            10,  // update frequency
            true // sends velocity updates
        );
        
        TGCEAddon.getLogger().info("bolt_projectile 实体注册完成");
    }
}
