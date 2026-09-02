package com.teamytz.tgceaddon.entities.projectiles;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.tracking.HeatSourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import techguns.api.damagesystem.DamageType;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.entities.projectiles.GuidedMissileProjectile;
import techguns.entities.projectiles.RocketProjectile;
import techguns.items.guns.GenericGun;
import techguns.items.guns.IProjectileFactory;
import techguns.util.MathUtil;

/**
 * 红外热追踪导弹(自主导引头)
 *
 * 导弹自带红外寻的导引头,独立于发射器工作:
 * - 视场:弹体前方 30° 锥体(半角 15°),只看得见锥体内的目标
 * - 热值:玩家 = 剩余信号 × (100 − 距离 × 0.5);末影水晶 120、科技枪导弹 70(均随距离衰减);
 *   热源方块恒定 100;只锁定热值 > 25 的目标
 * - 追踪:每 tick 最多转 2°,转向使目标保持在视场中央
 * - 脱锁:目标离开视场 / 热值 ≤ 25 / 死亡 → 脱锁,在视场内重新索敌,锁定最热目标并切换
 * - 拦截:锁定科技枪导弹(含其它红外导弹)后,接近到 3 格内即摧毁目标并自爆;
 *   自身爆炸的冲击波也会摧毁爆炸半径内的所有科技枪导弹(弹药拦截)
 * - 太阳锁定(发射器已锁太阳):直飞太阳,不可脱锁
 *
 * 注意:父类 GuidedMissileProjectile 自带 9°/tick 转向,与本导引头冲突,
 * 因此在调用父类 onUpdate 前临时置空 target 以禁用其转向。
 */
public class IRMissileProjectile extends GuidedMissileProjectile {

    /** 转向上限:每 tick 2°(低机动,可被机动甩脱) */
    public static final double MAX_TURN_ANGLE = 2.0 * MathUtil.D2R;

    /** 导弹视场:弹体前方 30° 锥体(半角 15°) */
    public static final double FOV_HALF_ANGLE = 15.0;
    public static final double FOV_COS = Math.cos(Math.toRadians(FOV_HALF_ANGLE));

    /** 拦截距离(格):锁定目标为导弹且接近到该距离内 → 摧毁目标并自爆 */
    public static final double INTERCEPT_DISTANCE = 3.0D;
    /** 爆炸冲击波摧毁导弹的半径(与武器爆炸半径一致) */
    public static final double BLAST_MISSILE_RADIUS = 4.0D;

    /** 锁定太阳标志:true 时朝太阳方向飞(无实体目标,不可脱锁) */
    private boolean lockedSun = false;
    /** 锁定方块热源坐标标志 */
    private boolean posTarget = false;
    /** 方块热源锁定坐标 */
    private double targetX = 0.0D;
    private double targetY = 0.0D;
    private double targetZ = 0.0D;
    /** 接近锁定坐标多少格内引爆 */
    private static final double POS_HIT_DISTANCE = 1.5D;

    public IRMissileProjectile(World worldIn) {
        super(worldIn);
    }

    public IRMissileProjectile(World par2World, EntityLivingBase p, float damage, float speed, int TTL,
            float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration, boolean blockdamage,
            EnumBulletFirePos leftGun, float radius, double gravity, Entity target, boolean sun) {
        super(par2World, p, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, penetration, blockdamage, leftGun,
                radius, target);
        this.lockedSun = sun;
    }

    /** 锁定方块热源坐标的构造 */
    public IRMissileProjectile(World par2World, EntityLivingBase p, float damage, float speed, int TTL,
            float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration, boolean blockdamage,
            EnumBulletFirePos leftGun, float radius, double tx, double ty, double tz) {
        super(par2World, p, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, penetration, blockdamage, leftGun,
                radius, 0.0f);
        this.posTarget = true;
        this.targetX = tx;
        this.targetY = ty;
        this.targetZ = tz;
    }

    @Override
    public void onUpdate() {
        if (!this.world.isRemote) {
            updateSeeker();
        } else if (!this.isDead) {
            // 客户端:全程匀速飞行(无动力段/滑行段之分)→ 全程携带持续尾焰光源
            updateMotorLight();
        }
        // 禁用父类 9°/tick 转向(其用 target 字段):本导引头以 2°/tick 自行控制
        Entity savedTarget = this.target;
        this.target = null;
        super.onUpdate();
        this.target = savedTarget;
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

    @Override
    protected void explodeRocket() {
        if (!this.world.isRemote) {
            // 弹药拦截:爆炸冲击波摧毁爆炸半径内的所有科技枪导弹(末影水晶由爆炸伤害自然处理)
            destroyMissilesInBlast();
        }
        super.explodeRocket();
    }

    /** 爆炸冲击波:摧毁爆炸半径内的所有科技枪导弹(含其它红外导弹/过载导弹/指令线导弹) */
    private void destroyMissilesInBlast() {
        double r2 = BLAST_MISSILE_RADIUS * BLAST_MISSILE_RADIUS;
        for (Entity e : this.world.loadedEntityList) {
            if (e == this || e.isDead || !(e instanceof RocketProjectile)) {
                continue;
            }
            double dx = e.posX - this.posX;
            double dy = e.posY - this.posY;
            double dz = e.posZ - this.posZ;
            if (dx * dx + dy * dy + dz * dz <= r2) {
                e.setDead();
                if (TGCEAddon.DEBUG_IR) {
                    TGCEAddon.getLogger().info("[IR] 爆炸冲击波摧毁导弹 @ ("
                            + (int) e.posX + "," + (int) e.posY + "," + (int) e.posZ + ")");
                }
            }
        }
    }

    /**
     * 自主导引头(服务端每 tick):
     * 1. 太阳锁定 → 直飞太阳
     * 2. 已有目标 → 视场 + 热值保持检查,满足则转向追踪
     * 3. 脱锁/无目标 → 视场内自行索敌,锁定最热目标(热值 > 25),必要时更换目标
     */
    private void updateSeeker() {
        Vec3d heading = new Vec3d(this.motionX, this.motionY, this.motionZ);
        double speed = heading.lengthVector();
        if (speed < 0.001D) {
            return; // 无速度时无法判断视场方向(正常不会发生,火箭有初速)
        }
        heading = heading.normalize();

        // 1. 太阳锁定:不可脱锁,直飞太阳
        if (this.lockedSun) {
            Vec3d sunDir = getSunDirection(this.world);
            Vec3d aim = new Vec3d(this.posX, this.posY, this.posZ).add(sunDir.scale(1000.0D));
            turnToward(aim);
            return;
        }

        // 2. 已有目标:保持条件(视场 + 热值 > 25);
        //    热诱弹诱骗:保持目标的同时,若锥体内出现热值明显更高的目标(如热诱弹 200)
        //    则切换目标(红外导弹始终追最热目标)
        if (this.posTarget) {
            // 方块目标(恒定 100 热值):出现热值明显更高的实体目标 → 切换
            HeatSourceManager.HeatTarget hotter = findHotterThan(HeatSourceManager.BLOCK_HEAT + 10.0);
            if (hotter != null) {
                switchTo(hotter);
                return;
            }
            if (isInFov(heading, this.targetX, this.targetY, this.targetZ)) {
                Vec3d to = new Vec3d(this.targetX - this.posX, this.targetY - this.posY, this.targetZ - this.posZ);
                if (to.lengthVector() < POS_HIT_DISTANCE) {
                    if (TGCEAddon.DEBUG_IR) {
                        TGCEAddon.getLogger().info("[IR] 导弹命中锁定坐标 @ ("
                                + (int) targetX + "," + (int) targetY + "," + (int) targetZ + ")");
                    }
                    this.explodeRocket();
                    return;
                }
                turnToward(new Vec3d(this.targetX, this.targetY, this.targetZ));
                return;
            }
            if (TGCEAddon.DEBUG_IR) {
                TGCEAddon.getLogger().info("[IR] 导弹丢失方块目标(脱离视场),脱锁重索敌");
            }
        } else if (this.target != null && !this.target.isDead) {
            Entity t = this.target;
            double tx = t.posX;
            double ty = t.posY + t.height * 0.5D;
            double tz = t.posZ;
            double heat = HeatSourceManager.getEntityHeat(t, this.posX, this.posY, this.posZ);
            if (isInFov(heading, tx, ty, tz) && heat > HeatSourceManager.MIN_LOCK_HEAT) {
                // 热诱弹诱骗:锥体内出现热值明显更高的目标(热诱弹 200 > 玩家 ≤100)→ 切换
                HeatSourceManager.HeatTarget hotter = findHotterThan(heat + 10.0);
                if (hotter != null) {
                    switchTo(hotter);
                    return;
                }
                // 拦截:锁定目标是科技枪导弹且已接近 → 摧毁目标并自爆
                if (t instanceof RocketProjectile) {
                    double d = new Vec3d(tx - this.posX, ty - this.posY, tz - this.posZ).lengthVector();
                    if (d <= INTERCEPT_DISTANCE) {
                        if (!t.isDead) {
                            t.setDead();
                            if (TGCEAddon.DEBUG_IR) {
                                TGCEAddon.getLogger().info("[IR] 拦截成功!摧毁敌方导弹 @ ("
                                        + (int) t.posX + "," + (int) t.posY + "," + (int) t.posZ + ")");
                            }
                        }
                        this.explodeRocket();
                        return;
                    }
                }
                turnToward(new Vec3d(tx, ty, tz));
                return;
            }
            if (TGCEAddon.DEBUG_IR) {
                TGCEAddon.getLogger().info("[IR] 导弹脱锁:目标 "
                        + (t.isDead ? "死亡" : (heat <= HeatSourceManager.MIN_LOCK_HEAT
                                ? "热值过低(" + String.format("%.1f", heat) + ")" : "脱离视场"))
                        + ",重新索敌");
            }
        }

        // 3. 脱锁/无目标:视场内自行索敌,锁定最热目标
        seekNewTarget(heading);
    }

    /**
     * 热诱弹诱骗检查:弹体前方 30° 锥体内热值超过 threshold 的最热实体目标
     * (热诱弹 200 / 导弹 70 / 玩家 ≤100)。方块目标返回 null(方块由各自分支处理)。
     */
    private HeatSourceManager.HeatTarget findHotterThan(double threshold) {
        Vec3d heading = new Vec3d(this.motionX, this.motionY, this.motionZ).normalize();
        EntityPlayer shooterPlayer = (this.shooter instanceof EntityPlayer) ? (EntityPlayer) this.shooter : null;
        HeatSourceManager.HeatTarget target = HeatSourceManager.findHottestInCone(this.world,
                this.posX, this.posY, this.posZ, heading, FOV_COS, 150.0D, this, shooterPlayer, false);
        if (target == null || target.isBlock()) {
            return null;
        }
        return target.heat > threshold ? target : null;
    }

    /** 切换到新目标(实体/方块),并输出切换日志 */
    private void switchTo(HeatSourceManager.HeatTarget target) {
        if (target.isBlock()) {
            boolean switched = this.target != null || !this.posTarget || this.lockedSun;
            this.target = null;
            this.posTarget = true;
            this.lockedSun = false;
            this.targetX = target.block.getX() + 0.5D;
            this.targetY = target.block.getY() + 0.5D;
            this.targetZ = target.block.getZ() + 0.5D;
            if (switched && TGCEAddon.DEBUG_IR) {
                TGCEAddon.getLogger().info("[IR] 导弹重新锁定热源方块 @ ("
                        + target.block.getX() + "," + target.block.getY() + "," + target.block.getZ()
                        + ")(热值 " + String.format("%.1f", target.heat) + ")");
            }
        } else {
            boolean switched = this.target != target.entity || this.posTarget || this.lockedSun;
            String prevType = this.target != null ? this.target.getName() : (this.posTarget ? "方块" : (this.lockedSun ? "太阳" : "无"));
            this.target = target.entity;
            this.posTarget = false;
            this.lockedSun = false;
            if (switched && TGCEAddon.DEBUG_IR) {
                String type = target.entity instanceof com.teamytz.tgceaddon.entities.EntityFlare ? "热诱弹"
                        : (target.entity instanceof net.minecraft.entity.item.EntityEnderCrystal ? "末影水晶"
                        : (target.entity instanceof RocketProjectile ? "导弹" : "玩家"));
                TGCEAddon.getLogger().info("[IR] 导弹切换目标(" + prevType + "→" + type + "): "
                        + (type.equals("玩家") ? ((EntityPlayer) target.entity).getName() : "")
                        + "(热值 " + String.format("%.1f", target.heat) + ",距离 "
                        + String.format("%.0f", target.dist) + " 格)");
            }
        }
    }

    /** 视场内自行索敌:锁定弹体前方 30° 锥体内热值最高的目标(玩家/末影水晶/热诱弹/导弹/方块,热值 > 25) */
    private void seekNewTarget(Vec3d heading) {
        EntityPlayer shooterPlayer = (this.shooter instanceof EntityPlayer) ? (EntityPlayer) this.shooter : null;
        HeatSourceManager.HeatTarget target = HeatSourceManager.findHottestInCone(this.world,
                this.posX, this.posY, this.posZ, heading, FOV_COS, 150.0D, this, shooterPlayer, false);

        if (target == null) {
            if (TGCEAddon.DEBUG_IR && this.ticksExisted % 20 == 0) {
                TGCEAddon.getLogger().info("[IR] 导弹视场内无热源,直飞(位置 "
                        + String.format("%.1f", this.posX) + "," + String.format("%.1f", this.posY) + ","
                        + String.format("%.1f", this.posZ) + ")");
            }
            return;
        }

        switchTo(target);
    }

    /** 判断点是否在弹体前方视场锥体内 */
    private boolean isInFov(Vec3d heading, double x, double y, double z) {
        Vec3d to = new Vec3d(x - this.posX, y - this.posY, z - this.posZ).normalize();
        return heading.dotProduct(to) >= FOV_COS;
    }

    /** 科技枪制导转向:每 tick 朝目标点最多转 2° */
    private void turnToward(Vec3d aim) {
        Vec3d motion = new Vec3d(motionX, motionY, motionZ);
        double speed = motion.lengthVector();
        if (speed > 0.0001) {
            Vec3d v2 = aim.subtract(new Vec3d(this.posX, this.posY, this.posZ)).normalize();
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

    /**
     * 太阳方向(1.12.2):与原版天空渲染一致。
     *
     * RenderGlobal.renderSky 的变换(已用 javap 反汇编 1.12.2 混淆 jar 验证):
     *   GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);                          // 先绕 Y 轴 -90°
     *   GlStateManager.rotate(getCelestialAngle(1.0F) * 360.0F, 1.0F, 0.0F, 0.0F); // 再绕 X 轴转 θ
     * 太阳四边顶点位于 (±30, 100, ±30),中心 (0, 100, 0)。
     * OpenGL 列向量约定下顶点先应用最后一次 rotate:
     *   世界坐标 = R_y(-90°) · R_x(θ) · (0,100,0) = (-100·sinθ, 100·cosθ, 0)
     * 因此太阳方向 = (-sin(a), cos(a), 0),a = 天顶角 × 2π。
     * 验证:正午(a=0)→ (0,1,0) 天顶;日落(a≈0.2155)→ 西;日出(a≈0.7845)→ 东。
     */
    public static Vec3d getSunDirection(World world) {
        float celestial = world.getCelestialAngle(1.0F);
        double a = celestial * 2.0D * Math.PI;
        return new Vec3d(-Math.sin(a), Math.cos(a), 0.0D).normalize();
    }

    public static class Factory implements IProjectileFactory<IRMissileProjectile> {

        @Override
        public IRMissileProjectile createProjectile(GenericGun gun, World world, EntityLivingBase p, float damage,
                float speed, int TTL, float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration,
                boolean blockdamage, EnumBulletFirePos firePos, float radius, double gravity) {
            // 由武器侧在锁定后手动创建(携带锁定目标),此处不应直接调用
            return null;
        }

        @Override
        public DamageType getDamageType() {
            return DamageType.EXPLOSION;
        }
    }
}
