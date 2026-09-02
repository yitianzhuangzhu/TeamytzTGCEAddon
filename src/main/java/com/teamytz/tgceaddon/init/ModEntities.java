package com.teamytz.tgceaddon.init;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.entities.EntityBMPTTurret;
import com.teamytz.tgceaddon.entities.EntityFlare;
import com.teamytz.tgceaddon.entities.EntityMuzzleLight;
import com.teamytz.tgceaddon.entities.EntityRolandTurret;
import com.teamytz.tgceaddon.entities.projectiles.BMPTGuidedMissileProjectile;
import com.teamytz.tgceaddon.entities.projectiles.BoltProjectile;
import com.teamytz.tgceaddon.entities.projectiles.CannonShellProjectile;
import com.teamytz.tgceaddon.entities.projectiles.OverloadGuidedMissileProjectile;
import com.teamytz.tgceaddon.entities.projectiles.CommandLineMissileProjectile;
import com.teamytz.tgceaddon.entities.projectiles.IRMissileProjectile;
import com.teamytz.tgceaddon.entities.projectiles.RolandMissileProjectile;
import com.teamytz.tgceaddon.entities.projectiles.RolandMissileProjectileNuke;
import com.teamytz.tgceaddon.entities.projectiles.CommandLineMissileProjectileNuke;
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

        // 过载制导导弹弹丸(过载限制制导,可被躲避)
        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "overload_missile_projectile"),
            OverloadGuidedMissileProjectile.class,
            "overload_missile_projectile",
            3004,  // ID
            TGCEAddon.MODID,  // mod ID 字符串
            128,  // tracking range
            1,  // update frequency(每 tick 同步位置,两端一致,近炸/命中判定同步、爆炸后立即消失)
            true  // sends velocity updates
        );

        TGCEAddon.getLogger().info("overload_missile_projectile 实体注册完成");

        // 指令线制导导弹弹丸(跟随射手鼠标准星指向飞行)
        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "command_line_missile_projectile"),
            CommandLineMissileProjectile.class,
            "command_line_missile_projectile",
            3005,  // ID
            TGCEAddon.MODID,  // mod ID 字符串
            128,  // tracking range
            1,  // update frequency(每 tick 同步,保证引导判定一致)
            true  // sends velocity updates
        );

        TGCEAddon.getLogger().info("command_line_missile_projectile 实体注册完成");

        // 红外热追踪导弹弹丸(锁定热源/太阳方向)
        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "ir_missile_projectile"),
            IRMissileProjectile.class,
            "ir_missile_projectile",
            3006,  // ID
            TGCEAddon.MODID,  // mod ID 字符串
            128,  // tracking range
            1,  // update frequency
            true  // sends velocity updates
        );

        TGCEAddon.getLogger().info("ir_missile_projectile 实体注册完成");

        // 罗兰防空炮塔实体(由炮塔控制器卡片槽放入 roland 卡片生成)
        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "roland_turret"),
            EntityRolandTurret.class,
            "roland_turret",
            3007,  // ID
            TGCEAddon.MODID,  // mod ID 字符串
            128,  // tracking range
            5,  // update frequency(静止实体)
            false  // 不发速度更新
        );

        TGCEAddon.getLogger().info("roland_turret 实体注册完成");

        // 罗兰指令线导弹弹丸(炮塔专用:长存活+快速+可失导)
        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "roland_missile_projectile"),
            RolandMissileProjectile.class,
            "roland_missile_projectile",
            3008,  // ID
            TGCEAddon.MODID,  // mod ID 字符串
            128,  // tracking range
            1,  // update frequency
            true  // sends velocity updates
        );

        TGCEAddon.getLogger().info("roland_missile_projectile 实体注册完成");

        // 罗兰核导弹弹丸(核火箭变体:命中触发科技枪核爆)
        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "roland_missile_projectile_nuke"),
            RolandMissileProjectileNuke.class,
            "roland_missile_projectile_nuke",
            3009,  // ID
            TGCEAddon.MODID,  // mod ID 字符串
            128,  // tracking range
            1,  // update frequency
            true  // sends velocity updates
        );

        TGCEAddon.getLogger().info("roland_missile_projectile_nuke 实体注册完成");

        // 玩家指令线核导弹弹丸(核火箭变体:命中触发科技枪核爆)
        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "command_line_missile_projectile_nuke"),
            CommandLineMissileProjectileNuke.class,
            "command_line_missile_projectile_nuke",
            3010,  // ID
            TGCEAddon.MODID,  // mod ID 字符串
            128,  // tracking range
            1,  // update frequency
            true  // sends velocity updates
        );

        TGCEAddon.getLogger().info("command_line_missile_projectile_nuke 实体注册完成");

        // 动态光源实体(仅客户端生成;OptiFine 动态光源载体,服务端不生成实例)
        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "muzzle_light"),
            EntityMuzzleLight.class,
            "muzzle_light",
            3011,  // ID
            TGCEAddon.MODID,  // mod ID 字符串
            128,  // tracking range
            1,  // update frequency
            false  // 不发速度更新
        );

        TGCEAddon.getLogger().info("muzzle_light 实体注册完成");

        // BMPT 炮塔制导导弹弹丸(科技枪 GuidedMissileProjectile 定制子类,带动态光源)
        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "bmpt_guided_missile_projectile"),
            BMPTGuidedMissileProjectile.class,
            "bmpt_guided_missile_projectile",
            3012,  // ID
            TGCEAddon.MODID,  // mod ID 字符串
            128,  // tracking range
            1,  // update frequency
            true  // sends velocity updates
        );

        TGCEAddon.getLogger().info("bmpt_guided_missile_projectile 实体注册完成");

        // 热诱弹实体(200 红外热值,诱骗红外热追踪导弹;存在 5 秒缓慢下落)
        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "flare"),
            EntityFlare.class,
            "flare",
            3013,  // ID
            TGCEAddon.MODID,  // mod ID 字符串
            128,  // tracking range
            1,  // update frequency
            true  // sends velocity updates(客户端同步下落运动)
        );

        TGCEAddon.getLogger().info("flare 实体注册完成");
    }
}
