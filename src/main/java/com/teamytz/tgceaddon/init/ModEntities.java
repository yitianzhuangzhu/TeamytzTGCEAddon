package com.teamytz.tgceaddon.init;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.entities.EntityBMPTTurret;
import com.teamytz.tgceaddon.entities.projectiles.BoltProjectile;
import com.teamytz.tgceaddon.entities.projectiles.CannonShellProjectile;
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
        // 注意：实体 id 是全局 EntityList id，techguns 用 0..N 自增（modEntityID 从 -1 起），
        // 若用 1/2 之类的小 id 会与 techguns 的实体冲突，导致客户端 spawn 时反查到错误实体。
        // 因此使用 3001+ 的大号唯一 id。
        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "bolt_projectile"),
            BoltProjectile.class,
            "bolt_projectile",
            3001,  // ID
            TGCEAddon.MODID,  // mod ID 字符串
            128,  // tracking range (与科技枪一致)
            10,  // update frequency
            true // sends velocity updates
        );

        TGCEAddon.getLogger().info("bolt_projectile 实体注册完成");

        // BMPT 炮塔实体（由炮塔控制器卡片槽生成，显示在炮塔底座上方）
        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "bmpt_turret"),
            EntityBMPTTurret.class,
            "bmpt_turret",
            3002,  // ID
            TGCEAddon.MODID,  // mod ID 字符串
            128,  // tracking range
            5,  // update frequency（静止实体，5 tick 足够）
            false  // 不发速度更新
        );

        TGCEAddon.getLogger().info("bmpt_turret 实体注册完成");

        // BMPT 机炮炮弹弹丸（撞击爆炸、不破坏方块）
        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "cannon_shell_projectile"),
            CannonShellProjectile.class,
            "cannon_shell_projectile",
            3003,  // ID
            TGCEAddon.MODID,  // mod ID 字符串
            128,  // tracking range
            10,  // update frequency
            true  // sends velocity updates
        );

        TGCEAddon.getLogger().info("cannon_shell_projectile 实体注册完成");
    }
}
