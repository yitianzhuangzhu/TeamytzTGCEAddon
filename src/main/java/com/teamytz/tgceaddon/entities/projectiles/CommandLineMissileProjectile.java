package com.teamytz.tgceaddon.entities.projectiles;

import com.teamytz.tgceaddon.util.ProjectileUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import techguns.api.damagesystem.DamageType;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.entities.projectiles.GenericProjectile;
import techguns.entities.projectiles.RocketProjectile;
import techguns.items.guns.GenericGun;
import techguns.items.guns.IProjectileFactory;
import techguns.util.MathUtil;

import java.util.List;

/**
 * 指令线制导导弹(Command Line Guidance / Wire-Guided Missile)
 *
 * 不依赖锁定,发射后每 tick 依据"射手玩家鼠标准星朝向"实时计算飞行目标:
 * - 玩家视线命中的实体 → 朝该实体当前位置飞
 * - 玩家视线命中的方块 → 朝该方块位置飞
 * - 玩家视线无命中 → 目标点 = 视线方向的远端(导弹矢量与玩家视线重合,直飞)
 *
 * 玩家转动准星,导弹即朝新指向的目标飞(人工指令引导)。
 * 参数套用科技枪原版制导导弹:匀速飞行(无动力段加速)、9°/tick 转向上限、火箭爆炸。
 */
public class CommandLineMissileProjectile extends RocketProjectile {

    /** 转向上限(与科技枪原版一致):每 tick 最大转角 9° ≈ 180°/秒 */
    public static final double MAX_TURN_ANGLE = 9.0 * MathUtil.D2R;
    /** 玩家视线扫描距离(格):视线无命中时,目标点取该距离的视线末端 */
    public static final double AIM_RANGE = 200.0;

    public CommandLineMissileProjectile(World worldIn) {
        super(worldIn);
    }

    public CommandLineMissileProjectile(World par2World, EntityLivingBase p, float damage, float speed, int TTL,
            float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration, boolean blockdamage,
            EnumBulletFirePos leftGun, float radius, double gravity) {
        super(par2World, p, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, penetration, blockdamage, leftGun,
                radius, gravity);
    }

    @Override
    public void onUpdate() {
        // 指令线制导:从射手玩家当前准星朝向计算目标点并转向
        if (this.shooter instanceof EntityPlayer && !this.shooter.isDead) {
            EntityPlayer player = (EntityPlayer) this.shooter;
            Vec3d aimPoint = getAimPoint(player);
            if (aimPoint != null) {
                Vec3d motion = new Vec3d(motionX, motionY, motionZ);
                double speed = motion.lengthVector();

                if (speed > 0.0001) {
                    Vec3d v2 = aimPoint.subtract(new Vec3d(this.posX, this.posY, this.posZ)).normalize();
                    Vec3d v1 = motion.normalize();

                    double dot = MathUtil.clamp(v1.dotProduct(v2), -1.0, 1.0);
                    double angle = Math.acos(dot);
                    Vec3d axis = v1.crossProduct(v2);

                    if (angle < MAX_TURN_ANGLE) {
                        motion = v2.scale(speed);
                    } else if (axis.lengthVector() > 1.0E-6) {
                        motion = MathUtil.rotateVector(v1, axis.normalize(), MAX_TURN_ANGLE).scale(speed);
                    } else {
                        motion = v2.scale(speed);
                    }

                    this.motionX = motion.x;
                    this.motionY = motion.y;
                    this.motionZ = motion.z;
                }
            }
        }
        if (this.world.isRemote && !this.isDead) {
            // 客户端:全程匀速飞行(无动力段/滑行段之分)→ 全程携带持续尾焰光源
            updateMotorLight();
        }
        super.onUpdate();
        if (this.world.isRemote && this.isDead) {
            killMotorLight();
        }
    }

    /** 持续尾焰光强(14=火把级) */
    private static final int LIGHT_MOTOR = 14;
    /** 客户端:跟随本导弹的持续光源 */
    private com.teamytz.tgceaddon.entities.EntityMuzzleLight motorLight = null;

    private void updateMotorLight() {
        if (this.motorLight == null || this.motorLight.isDead) {
            this.motorLight = com.teamytz.tgceaddon.entities.EntityMuzzleLight.spawnContinuous(
                    this.world, this.posX, this.posY, this.posZ, LIGHT_MOTOR);
        } else {
            this.motorLight.setPositionAndUpdate(this.posX, this.posY, this.posZ);
            this.motorLight.renew(40);
        }
    }

    private void killMotorLight() {
        if (this.motorLight != null) {
            this.motorLight.kill();
            this.motorLight = null;
        }
    }

    /**
     * 计算玩家准星指向的目标点(每 tick 实时):
     * 实体 > 方块 > 视线方向远端(矢量与视线重合)
     */
    protected Vec3d getAimPoint(EntityPlayer player) {
        Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        Vec3d look = player.getLookVec();
        Vec3d end = start.add(look.scale(AIM_RANGE));

        // 1. 视线命中的实体:朝实体当前位置飞
        Entity target = findEntityOnPath(player, start, end);
        if (target != null) {
            return new Vec3d(target.posX, target.posY + target.height * 0.5f, target.posZ);
        }

        // 2. 视线命中的方块:朝方块位置飞
        RayTraceResult rtr = player.world.rayTraceBlocks(start, end, false, true, false);
        if (rtr != null && rtr.typeOfHit == RayTraceResult.Type.BLOCK) {
            return rtr.hitVec;
        }

        // 3. 视线无命中:目标点 = 视线方向远端(导弹矢量与玩家视线重合,直飞)
        return end;
    }

    /** 沿玩家视线路径查找命中的实体(排除射手自己) */
    protected Entity findEntityOnPath(EntityPlayer player, Vec3d start, Vec3d end) {
        Entity hit = null;
        double closest = Double.MAX_VALUE;
        Vec3d look = player.getLookVec();
        List<Entity> list = player.world.getEntitiesInAABBexcluding(player,
                player.getEntityBoundingBox().expand(look.x * AIM_RANGE, look.y * AIM_RANGE, look.z * AIM_RANGE).grow(1.0D),
                GenericProjectile.BULLET_TARGETS::test);

        for (Entity entity : list) {
            if (entity == this || entity == this.shooter) {
                continue;
            }
            RayTraceResult rtr = entity.getEntityBoundingBox().grow(0.3D).calculateIntercept(start, end);
            if (rtr != null) {
                double dist = start.squareDistanceTo(rtr.hitVec);
                if (dist < closest) {
                    closest = dist;
                    hit = entity;
                }
            }
        }
        return hit;
    }

    public static class Factory implements IProjectileFactory<CommandLineMissileProjectile> {

        @Override
        public CommandLineMissileProjectile createProjectile(GenericGun gun, World world, EntityLivingBase p, float damage,
                float speed, int TTL, float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration,
                boolean blockdamage, EnumBulletFirePos firePos, float radius, double gravity) {
            CommandLineMissileProjectile proj = new CommandLineMissileProjectile(world, p, damage, speed, TTL, spread,
                    dmgDropStart, dmgDropEnd, dmgMin, penetration, blockdamage, firePos, radius, gravity);
            // 高速移动中发射(如鞘翅飞行):叠加射手速度,避免导弹比玩家还慢
            ProjectileUtil.applyShooterVelocity(proj, p);
            return proj;
        }

        @Override
        public DamageType getDamageType() {
            return DamageType.EXPLOSION;
        }
    }
}
