package com.teamytz.tgceaddon.entities.projectiles;

import com.teamytz.tgceaddon.TGCEAddon;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import techguns.TGPackets;
import techguns.Techguns;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.packets.PacketSpawnParticle;

/**
 * 机炮炮弹弹丸
 * 撞击时爆炸，但不破坏方块、不引火（机制类似爆弹 BoltProjectile，爆炸半径更大）。
 * 继承 BoltProjectile 复用其爆炸伤害/击退逻辑，仅重写 explode 调整爆炸表现。
 * 客户端自带烟雾特效：首 tick 在炮口生成一簇短命快散的炮口烟，飞行中每 tick 生成
 * 弹道尾迹烟（纯客户端本地粒子，弹丸实体本身已被同步到客户端，无需额外网络包）。
 * 粒子使用 MC 原生 EXPLOSION_NORMAL（爆开即散的白烟，天然"散得快"）与 SMOKE_NORMAL
 * 尾迹——原生粒子渲染路径最可靠。注意：只有机炮炮弹有这套烟雾，导弹不做。
 */
public class CannonShellProjectile extends BoltProjectile {

    /** 爆炸半径（格） */
    private static final float EXPLOSION_RADIUS = 2.5F;

    /** 客户端：本发弹丸是否已生成过炮口烟雾（每发只生成一次） */
    private boolean muzzleSmokeSpawned = false;

    public CannonShellProjectile(World worldIn) {
        super(worldIn);
    }

    public CannonShellProjectile(World par2World, EntityLivingBase p, float damage, float speed, int TTL,
            float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration, boolean blockdamage,
            EnumBulletFirePos firePos) {
        super(par2World, p, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, penetration, blockdamage, firePos);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.world.isRemote && !this.isDead) {
            if (!this.muzzleSmokeSpawned) {
                this.muzzleSmokeSpawned = true;
                spawnMuzzleSmoke();
                // 开火动态光源:炮口短促高亮闪光(OptiFine 动态光源;未装则无光效)
                com.teamytz.tgceaddon.entities.EntityMuzzleLight.spawnFlash(
                        this.world, this.posX, this.posY, this.posZ, 15, 4);
            }
            spawnTrailSmoke();
        }
    }

    /**
     * 炮口烟雾：EXPLOSION_NORMAL 原生粒子，一次性爆开的白色烟，快速扩散消失。
     * 弹丸客户端首个 tick 时位置还在炮口附近（speed=1 格/tick，同步延迟仅 0~1 tick），
     * 在此生成即可近似炮口位置。
     */
    private void spawnMuzzleSmoke() {
        double mx = this.motionX;
        double my = this.motionY;
        double mz = this.motionZ;
        // 沿发射方向冲出 + 向四周扩散，4 个爆开烟 + 1 个大烟
        for (int i = 0; i < 4; i++) {
            double vx = mx * 0.5D + (this.rand.nextDouble() - 0.5D) * 0.6D;
            double vy = my * 0.5D + 0.1D + (this.rand.nextDouble() - 0.5D) * 0.3D;
            double vz = mz * 0.5D + (this.rand.nextDouble() - 0.5D) * 0.6D;
            this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL,
                    this.posX, this.posY, this.posZ, vx, vy, vz);
        }
        this.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE,
                this.posX, this.posY, this.posZ, mx * 0.3D, my * 0.3D + 0.05D, mz * 0.3D);
    }

    /**
     * 弹道尾迹烟：每 tick 在弹丸位置向后飘散的小烟点，短命快速消失，不堆积挡视线。
     */
    private void spawnTrailSmoke() {
        double vx = -this.motionX * 0.2D + (this.rand.nextDouble() - 0.5D) * 0.08D;
        double vy = -this.motionY * 0.2D + 0.02D + (this.rand.nextDouble() - 0.5D) * 0.05D;
        double vz = -this.motionZ * 0.2D + (this.rand.nextDouble() - 0.5D) * 0.08D;
        this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                this.posX, this.posY, this.posZ, vx, vy, vz);
    }

    @Override
    protected void explode(double x, double y, double z) {
        if (!this.world.isRemote) {
            // 爆炸粒子效果
            TGPackets.wrapper.sendToAllAround(new PacketSpawnParticle("MiningChargeBlockExplosion", x, y, z),
                    TGPackets.targetPointAroundEnt(this, 100.0f));
            // 小型爆炸：不破坏方块、不引火（后两个 false），仅对生物造成伤害
            Explosion exp = new Explosion(world, this, x, y, z, EXPLOSION_RADIUS, false, false);
            exp.doExplosionA();
            exp.doExplosionB(false);
        } else {
            // 客户端闪光效果
            Techguns.proxy.createLightPulse(x, y, z, 5, 15, 3.0f, 0.5f, 1f, 0.9f, 0.5f);
        }
        this.setDead();
    }
}
