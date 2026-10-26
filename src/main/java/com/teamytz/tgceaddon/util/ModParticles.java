package com.teamytz.tgceaddon.util;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/**
 * 原版粒子发射小工具（服务端调一次 → 附近所有玩家都看得到）。
 *
 * 用途：HE 高爆弹的爆炸"可见性兜底"。爆炸外观主体是科技枪的火箭弹特效包
 * （{@code PacketSpawnParticle("RocketExplosion")}，与 Roland 导弹同一行调用），
 * 但那个包在本整合包里不保证渲染，所以再叠一层**原版粒子**，保证任何情况下都看得见。
 *
 * 关键点：原版粒子在服务端由 {@link WorldServer} 广播，
 * 而 WorldServer 的重载比 {@link World} 多一个 {@code longDistance} 参数——
 * 默认只发给 **32 格**内玩家，true 才扩到 **512 格**。
 * 炮塔射程 64 格，玩家站在炮塔边上打远处目标时离爆点几十格，
 * 用 32 格版本就什么都看不见，所以这里一律走 longDistance。
 * 客户端调用则退化为本地循环生成（单机/自己看都一致）。
 */
public final class ModParticles {

    private ModParticles() {
    }

    /**
     * 在 (x,y,z) 撒一团 count 颗粒子：位置抖动 ±spread 格，速度 speed（原版"随机方向"语义）。
     */
    public static void burst(World world, EnumParticleTypes type, double x, double y, double z,
                             int count, double spread, double speed) {
        if (world == null || type == null || count <= 0) {
            return;
        }
        if (world instanceof WorldServer) {
            ((WorldServer) world).spawnParticle(type, true, x, y, z, count, spread, spread, spread, speed);
            return;
        }
        for (int i = 0; i < count; i++) {
            double vx = (world.rand.nextDouble() - 0.5D) * 2.0D * speed;
            double vy = (world.rand.nextDouble() - 0.5D) * 2.0D * speed;
            double vz = (world.rand.nextDouble() - 0.5D) * 2.0D * speed;
            world.spawnParticle(type,
                    x + (world.rand.nextDouble() - 0.5D) * 2.0D * spread,
                    y + (world.rand.nextDouble() - 0.5D) * 2.0D * spread,
                    z + (world.rand.nextDouble() - 0.5D) * 2.0D * spread,
                    vx, vy, vz);
        }
    }
}
