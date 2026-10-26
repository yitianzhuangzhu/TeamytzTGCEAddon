package com.teamytz.tgceaddon.init;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.entities.EntityBMPTTurret;
import com.teamytz.tgceaddon.entities.EntityFlare;
import com.teamytz.tgceaddon.entities.EntityLaserTurret;
import com.teamytz.tgceaddon.entities.EntityLaserTurretMark1;
import com.teamytz.tgceaddon.entities.EntityLightningTurret;
import com.teamytz.tgceaddon.entities.EntityM1Turret;
import com.teamytz.tgceaddon.entities.EntityMuzzleLight;
import com.teamytz.tgceaddon.entities.EntityRolandTurret;
import com.teamytz.tgceaddon.entities.EntityTurret;
import com.teamytz.tgceaddon.entities.projectiles.BMPTGuidedMissileProjectile;
import com.teamytz.tgceaddon.entities.projectiles.BoltProjectile;
import com.teamytz.tgceaddon.entities.projectiles.CannonShellProjectile;
import com.teamytz.tgceaddon.entities.projectiles.APFSDSShellProjectile;
import com.teamytz.tgceaddon.entities.projectiles.HEShellProjectile;
import com.teamytz.tgceaddon.entities.projectiles.OverloadGuidedMissileProjectile;
import com.teamytz.tgceaddon.entities.projectiles.CommandLineMissileProjectile;
import com.teamytz.tgceaddon.entities.projectiles.IRMissileProjectile;
import com.teamytz.tgceaddon.entities.projectiles.ModeBeamProjectile;
import com.teamytz.tgceaddon.entities.projectiles.RolandMissileProjectile;
import com.teamytz.tgceaddon.entities.projectiles.RolandMissileProjectileNuke;
import com.teamytz.tgceaddon.entities.projectiles.TurretLaserBeamProjectile;
import com.teamytz.tgceaddon.entities.projectiles.TurretOverloadLaserBeamProjectile;
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

        // ===== 0.21.0 新炮塔（模型/贴图/卡片接入；武器与俯仰待实现）=====
        // 说明：炮塔实体都是静止实体，位置由基座 master 每 tick 驱动，
        // 不需要速度同步，故 update frequency = 5、不发速度更新（与 BMPT/罗兰一致）。
        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "turret"),
            EntityTurret.class,
            "turret",
            3014,  // ID
            TGCEAddon.MODID,
            128,  // tracking range
            5,  // update frequency（静止实体）
            false  // 不发速度更新
        );

        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "laser_turret"),
            EntityLaserTurret.class,
            "laser_turret",
            3015,  // ID
            TGCEAddon.MODID,
            128,
            5,
            false
        );

        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "laser_turret_mk1"),
            EntityLaserTurretMark1.class,
            "laser_turret_mk1",
            3016,  // ID
            TGCEAddon.MODID,
            128,
            5,
            false
        );

        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "lightning_turret"),
            EntityLightningTurret.class,
            "lightning_turret",
            3017,  // ID
            TGCEAddon.MODID,
            128,
            5,
            false
        );

        TGCEAddon.getLogger().info("新炮塔实体注册完成 (turret / laser_turret / laser_turret_mk1 / lightning_turret)");

        // 激光炮塔 Mk.1 的光束弹丸（科技枪 LaserProjectile 子类：命中方块/实体时产生小型爆炸）
        // 子类必须单独注册实体 id，否则生成时会因"实体未注册"报错
        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "turret_laser_beam"),
            TurretLaserBeamProjectile.class,
            "turret_laser_beam",
            3018,  // ID
            TGCEAddon.MODID,
            128,  // tracking range
            1,  // update frequency（光束存活极短，需及时同步）
            true  // 发送速度更新
        );

        TGCEAddon.getLogger().info("turret_laser_beam 实体注册完成");

        // 过载激光光束（Mk.1 装炮塔升级卡后使用：爆炸更强 + 破坏徒手可挖的软方块）
        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "turret_overload_laser_beam"),
            TurretOverloadLaserBeamProjectile.class,
            "turret_overload_laser_beam",
            3019,  // ID
            TGCEAddon.MODID,
            128,
            1,
            true
        );

        TGCEAddon.getLogger().info("turret_overload_laser_beam 实体注册完成");

        // 普通炮塔的两种炮弹（HE 高爆弹 / APFSDS 穿甲弹）
        // 参考科技枪注册弹丸实体的方式：独立 id + 独立渲染器（科技枪自己的弹丸也是这么注册的）
        // update frequency 用 1：炮弹速度快（4~6 格/tick），低频同步会在视觉上"跳格"
        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "he_shell_projectile"),
            HEShellProjectile.class,
            "he_shell_projectile",
            3020,  // ID
            TGCEAddon.MODID,
            160,  // tracking range（普通炮塔射程较远）
            1,  // update frequency
            true  // 发送速度更新
        );

        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "apfsds_shell_projectile"),
            APFSDSShellProjectile.class,
            "apfsds_shell_projectile",
            3021,  // ID
            TGCEAddon.MODID,
            160,
            1,
            true
        );

        TGCEAddon.getLogger().info("he_shell_projectile / apfsds_shell_projectile 实体注册完成");

        // 测试光束枪的弹丸（0.29.0）：NDRProjectile 子类，每 tick 重新对准射手并重新射线。
        // update frequency 用 1：光束每 tick 都要重算终点，低频同步会看到"光束跟不上枪口"
        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "mode_beam"),
            ModeBeamProjectile.class,
            "mode_beam",
            3022,  // ID
            TGCEAddon.MODID,
            160,  // tracking range（光束射线长度 100 格）
            1,  // update frequency
            true  // 发送速度更新
        );

        TGCEAddon.getLogger().info("mode_beam 实体注册完成");

        // M1 主战坦克炮塔（0.30.0）
        EntityRegistry.registerModEntity(
            new ResourceLocation(TGCEAddon.MODID, "m1_turret"),
            EntityM1Turret.class,
            "m1_turret",
            3023,  // ID
            TGCEAddon.MODID,
            128,   // tracking range
            1,     // update frequency（炮塔每 tick 都要同步朝向/俯仰）
            false  // 炮塔不移动，不需要速度更新
        );

        TGCEAddon.getLogger().info("m1_turret 实体注册完成");
    }
}
