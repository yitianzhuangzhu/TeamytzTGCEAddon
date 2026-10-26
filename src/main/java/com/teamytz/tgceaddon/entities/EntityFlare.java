package com.teamytz.tgceaddon.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

/**
 * 热诱弹(红外诱饵)
 *
 * - 红外热值 200,远高于玩家(满热 100)与科技枪导弹(70),
 *   基于热值追踪的红外导弹会优先追踪它,从而被"骗走"脱离玩家
 * - 存在 5 秒(100 tick)后消失
 * - 生成时小幅上冲,随后缓慢下落;落地后停驻直到寿命结束
 * - 外观(模型)由**原版岩浆大粒子**构成:EnumParticleTypes.LAVA,见 spawnLavaParticle 注释
 */
public class EntityFlare extends Entity {

    /** 存在时长(tick):5 秒 */
    public static final int LIFETIME = 100;
    /** 红外热值:远高于玩家(100)/导弹(70),成为红外导弹首选目标 */
    public static final float HEAT = 200.0f;

    private int lifetime = LIFETIME;

    public EntityFlare(World worldIn) {
        super(worldIn);
        this.setSize(0.25F, 0.25F);
    }

    @Override
    protected void entityInit() {
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.lifetime = compound.getInteger("FlareLifetime");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("FlareLifetime", this.lifetime);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        this.lifetime--;
        if (this.lifetime <= 0) {
            this.setDead();
            return;
        }

        // 缓慢下落:轻重力 + 终速限制(约正常重力的 1/4)
        this.motionY -= 0.018D;
        if (this.motionY < -0.12D) {
            this.motionY = -0.12D;
        }
        this.motionX *= 0.98D;
        this.motionZ *= 0.98D;
        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        if (this.onGround) {
            this.motionX *= 0.6D;
            this.motionZ *= 0.6D;
            this.motionY = 0.0D;
        }

        // 客户端:外观 = 原版岩浆大粒子(热诱弹本体就是这团岩浆泡)
        if (this.world.isRemote) {
            // 每 2 tick 一颗,并随机偏出本体半径。
            // 注意:岩浆粒子贴图(particles.png 49 号,4x4)中心是近白色 255,245,198 的芯、
            // 外圈才是橙黄 255,106,0 / 255,216,0,且 alpha 全 255 硬边。
            // 若把大量粒子堆在同一点,浅色芯会叠满整个可见区,看上去就是一团白 —— 所以必须拉开间距。
            if (this.ticksExisted % 2 == 0) {
                double offX = (this.rand.nextDouble() - 0.5D) * 0.5D;
                double offZ = (this.rand.nextDouble() - 0.5D) * 0.5D;
                double offY = (this.rand.nextDouble() - 0.5D) * 0.3D;
                this.spawnLavaParticle(offX, 0.05D + offY, offZ, offX * 0.08D, 0.02D, offZ * 0.08D);
            }
            // 烟雾拖尾:保留原有的诱饵下落轨迹
            if (this.ticksExisted % 5 == 0) {
                this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                        this.posX + (this.rand.nextDouble() - 0.5D) * 0.3D,
                        this.posY + 0.15D,
                        this.posZ + (this.rand.nextDouble() - 0.5D) * 0.3D,
                        0.0D, 0.02D, 0.0D);
            }
        }
    }

    /**
     * 生成一颗原版岩浆大粒子。
     *
     * 1.12.2 实测依据(MCP snapshot 20171003,取自 mapped jar 的 javap,不是凭记忆):
     * - EnumParticleTypes.LAVA:粒子 id = 27;小滴岩浆是 DRIP_LAVA(id 19),不要用错
     * - 工厂类 net.minecraft.client.particle.ParticleLava,贴图集索引 49,
     *   图集 textures/particle/particles.png(16x16 格,即 u=49%16=1,v=49/16=3);
     *   该精灵是 4x4 的橙黄岩浆泡:外圈 255,106,0、中层 255,216,0、中心 255,245,198(近白)
     * - ParticleLava 构造:particleScale *= rand*2+0.2;初速 motionY = rand*0.4+0.05;
     *   寿命 particleMaxAge = 16/(rand*0.8+0.2) tick(约 16~80);颜色恒为 1,1,1(不着色,
     *   颜色全靠贴图);getBrightnessForRender 强制天空亮度 240,所以永远全亮
     * - 渲染尺寸 = 0.1 * particleScale 的四边形(基础 particleScale 为 1~2),
     *   故实测直径约 0.04~0.88 格 —— 这正是"大粒子"的量级,够撑起热诱弹本体外观
     * - 该粒子由 ParticleLava.renderParticle 按 lavaParticleScale*(1-f*f) 随寿命收缩(f=age/maxAge)
     */
    private void spawnLavaParticle(double offX, double offY, double offZ,
                                   double velX, double velY, double velZ) {
        this.world.spawnParticle(EnumParticleTypes.LAVA,
                this.posX + offX, this.posY + offY, this.posZ + offZ,
                velX, velY, velZ);
    }
}
