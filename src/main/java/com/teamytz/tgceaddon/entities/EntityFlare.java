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
 * - 客户端附带火焰/烟雾粒子(诱饵视觉效果)
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

        // 客户端:火焰/烟雾粒子
        if (this.world.isRemote) {
            if (this.ticksExisted % 2 == 0) {
                this.world.spawnParticle(EnumParticleTypes.FLAME,
                        this.posX + (this.rand.nextDouble() - 0.5D) * 0.2D,
                        this.posY + 0.1D,
                        this.posZ + (this.rand.nextDouble() - 0.5D) * 0.2D,
                        0.0D, 0.01D, 0.0D);
            }
            if (this.ticksExisted % 5 == 0) {
                this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                        this.posX + (this.rand.nextDouble() - 0.5D) * 0.3D,
                        this.posY + 0.15D,
                        this.posZ + (this.rand.nextDouble() - 0.5D) * 0.3D,
                        0.0D, 0.02D, 0.0D);
            }
        }
    }
}
