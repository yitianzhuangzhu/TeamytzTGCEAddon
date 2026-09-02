package com.teamytz.tgceaddon.entities.projectiles;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.network.MissileDeathMessage;
import com.teamytz.tgceaddon.network.PacketHandler;
import com.teamytz.tgceaddon.util.ProjectileUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import techguns.api.damagesystem.DamageType;
import techguns.capabilities.TGExtendedPlayer;
import techguns.client.ClientProxy;
import techguns.client.particle.TGFX;
import techguns.client.particle.TGParticleSystem;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.entities.projectiles.GuidedMissileProjectile;
import techguns.items.guns.GenericGun;
import techguns.items.guns.IChargedProjectileFactory;
import techguns.util.EntityCondition;
import techguns.util.MathUtil;

import java.util.List;

/**
 * 过载限制制导导弹
 *
 * 与科技枪原版制导导弹(固定 9°/tick ≈ 180°/秒 转向率)不同,本导弹的机动受
 * "过载"(最大向心加速度)限制:
 *
 *   最大角速度 ω_max = a_max / v(弧度/秒),a_max 为最大过载(格/秒²)
 *
 * 特性:
 * - 过载限制:速度越快,允许的转向角速度越小(转弯半径 r = v²/a_max 越大),
 *   高速导弹无法急转,玩家/生物侧向跑动或急转即可甩开
 * - 机动减速:转向角度越大损失速度越多,轻微修正不掉速;连续急转后导弹变慢
 * - 动力段:发射后 BOOST_TICKS 内持续加速,期间带科技枪尾烟;之后滑行无尾烟
 * - 重力:始终受重力影响,下落加速、上升减速(参考鞘翅飞行)
 * - 近炸引信:距锁定目标 1 格内直接爆炸;击中方块/实体直接爆炸(命中后立即
 *   消失,客户端与服务端同步,避免爆炸后残留渲染)
 * - 目标死亡后失去制导,沿当前方向直线飞行
 */
public class OverloadGuidedMissileProjectile extends GuidedMissileProjectile {

    /** Debug 日志开关:测试期间开启,定位爆炸/同步问题,测试完关闭 */
    public static final boolean DEBUG = true;

    /** 返回当前端标识,用于日志区分客户端/服务端 */
    private String side() {
        return this.world != null && this.world.isRemote ? "CLIENT" : "SERVER";
    }

    /** 最大过载:向心加速度上限,单位 格/秒²(约 2g 的 MC 尺度;初速提到 1.5 后同步调高保持机动性) */
    public static final double MAX_OVERLOAD = 30.0;
    /** 机动减速阈值:转角占比低于该值时不掉速(轻微修正不掉速) */
    public static final double TURN_LOSS_THRESHOLD = 0.2;
    /** 机动减速系数:极限机动(转角占比 1.0)时每 tick 损失该比例的速度 */
    public static final double TURN_DAMPING_K = 0.04;
    /** 速度下限:不低于初始速度的该比例,防止导弹过慢悬停 */
    public static final double MIN_SPEED_FACTOR = 0.5;
    /** 近炸引信距离(格),距锁定目标该距离内直接爆炸(服务端权威判定) */
    public static final double PROXIMITY_FUSE_DIST = 1.0;
    /**
     * 客户端近炸距离(格),略大于服务端:保证客户端实体在服务端爆炸前先消失,
     * 避免"爆炸动画已出现但导弹仍残留渲染"的问题
     */
    public static final double CLIENT_FUSE_DIST = 1.2;
    /** 动力段时长(tick),发射后该时间内持续加速并带尾烟 */
    public static final int BOOST_TICKS = 40; // 2 秒
    /**
     * 动力段线性加速度(格/tick²):每 tick 速度增加该值(方向不变)。
     * 初速 0.5,40 tick 后约 2.5 格/tick,提速 5 倍,加速感明显
     */
    public static final double BOOST_ACCELERATION = 0.05;
    /**
     * 失速速度阈值(格/tick):速度低于该值时导弹失去升力,
     * 受强重力下坠(模拟现实导弹失速后头坠地),转向能力大降。
     * 注意必须低于初始速度 0.5,否则发射即失速
     */
    public static final double STALL_SPEED = 0.35;
    /** 失速时的重力加速度(格/tick²),约为正常重力的 4 倍(MC 标准坠落重力) */
    public static final double STALL_GRAVITY = 0.08;
    /** 失速时的转向能力系数:最大转角乘以该值(几乎无法机动,只能微弱修正) */
    public static final double STALL_TURN_FACTOR = 0.3;
    /** 重力加速度(格/tick²):下落加速、上升减速(参考鞘翅飞行) */
    public static final double GRAVITY = 0.02;
    /**
     * 动力段重力系数:动力段(发动机点火)推力抵消大部分重力,导弹平飞加速;
     * 滑行段恢复完整重力。解决"动力段下坠夸张"问题
     */
    public static final double BOOST_GRAVITY_FACTOR = 0.15;

    /** 客户端:目标实体 ID(用于延迟解析——目标实体可能晚于导弹在客户端加载) */
    private int targetEntityId = -1;

    /** 客户端:动力段尾烟(RocketLauncherExhaust 火箭弹尾烟)粒子系统引用 */
    private List<TGParticleSystem> boostSystems;
    /** 客户端:滑行段尾烟(GuidedMissileExhaust 制导导弹尾烟)粒子系统引用 */
    private List<TGParticleSystem> slideSystems;

    public OverloadGuidedMissileProjectile(World par2World, EntityLivingBase p, float damage, float speed, int TTL,
            float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration, boolean blockdamage,
            EnumBulletFirePos leftGun, float radius, double gravity) {
        super(par2World, p, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, penetration, blockdamage, leftGun,
                radius, 0.0f);
        // 重力由本类 onUpdate 自行处理:构造已传 0 给父类(gravity 字段包私有,无法直接赋值)
        if (DEBUG) {
            TGCEAddon.getLogger().info("[Missile] 构造(无target) side={} speed={} TTL={} pos=({},{},{})", side(), speed, TTL, posX, posY, posZ);
        }
    }

    public OverloadGuidedMissileProjectile(World par2World, EntityLivingBase p, float damage, float speed, int TTL,
            float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration, boolean blockdamage,
            EnumBulletFirePos leftGun, float radius, Entity target) {
        super(par2World, p, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, penetration, blockdamage, leftGun,
                radius, target);
        // 重力由本类 onUpdate 自行处理:父类有 target 构造已传 0 给父类
        if (DEBUG) {
            TGCEAddon.getLogger().info("[Missile] 构造(带target) side={} target={} speed={} TTL={}", side(), target != null ? target.getName() : "null", speed, TTL);
        }
    }

    public OverloadGuidedMissileProjectile(World worldIn) {
        super(worldIn);
        if (DEBUG) {
            TGCEAddon.getLogger().info("[Missile] 构造(网络spawn) side={}", side());
        }
    }

    /**
     * 覆写 spawn 数据:额外记录目标实体 ID。
     * 科技枪父类的 readSpawnData 在 spawn 早期用 getEntityByID 解析目标,
     * 若目标实体尚未在客户端加载会得到 null 且无法重试,导致客户端导弹不追踪
     * (轨迹与服务端分叉,表现为"不渲染但正常爆炸")。这里保存 ID 供 onUpdate 延迟重试。
     */
    @Override
    public void writeSpawnData(ByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeInt(this.target != null ? this.target.getEntityId() : -1);
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        super.readSpawnData(additionalData);
        this.targetEntityId = additionalData.readInt();
        this.tryResolveTarget();
    }

    /** 客户端:延迟解析目标(目标实体可能晚于导弹加载,每 tick 重试直到成功) */
    private void tryResolveTarget() {
        if (this.targetEntityId != -1 && this.target == null && this.world != null) {
            this.target = this.world.getEntityByID(this.targetEntityId);
            if (DEBUG && this.target != null) {
                TGCEAddon.getLogger().info("[Missile] 客户端目标延迟解析成功 target={}", this.target.getName());
            }
        }
    }

    /**
     * 客户端创建动力段尾烟:使用科技枪"非制导火箭弹"的尾烟(RocketLauncherExhaust)。
     * 动力段结束后会切换为制导导弹专属尾烟(GuidedMissileExhaust)。
     * 该回调在客户端实体构造时由父类调用。
     *
     * 注意:必须与科技枪 ClientProxy.createFXOnEntity 一致——把粒子系统加入
     * particleManager(否则系统从不更新,不会生成粒子),并设置 condition。
     */
    @Override
    protected void createTrailFX() {
        if (this.world != null && this.world.isRemote) {
            List<TGParticleSystem> systems = TGFX.createFXOnEntity(this, "RocketLauncherExhaust");
            if (DEBUG) {
                TGCEAddon.getLogger().info("[Missile] createTrailFX(动力段-火箭尾烟) side={} systems={}", side(), systems != null ? systems.size() : 0);
            }
            if (systems != null) {
                this.boostSystems = systems;
                for (TGParticleSystem system : systems) {
                    system.condition = EntityCondition.ENTITY_ALIVE;
                    ClientProxy.get().particleManager.addEffect(system);
                }
            }
        }
    }

    /** 客户端:动力段结束,移除火箭尾烟并切换为制导导弹专属尾烟(滑行段持续) */
    private void switchToSlideTrail() {
        if (this.boostSystems != null) {
            for (TGParticleSystem system : this.boostSystems) {
                system.setExpired();
            }
            this.boostSystems = null;
        }
        if (this.slideSystems == null) {
            List<TGParticleSystem> systems = TGFX.createFXOnEntity(this, "GuidedMissileExhaust");
            if (DEBUG) {
                TGCEAddon.getLogger().info("[Missile] 动力段结束,切换滑行段尾烟(制导导弹) side={} systems={}", side(), systems != null ? systems.size() : 0);
            }
            if (systems != null) {
                this.slideSystems = systems;
                for (TGParticleSystem system : systems) {
                    system.condition = EntityCondition.ENTITY_ALIVE;
                    ClientProxy.get().particleManager.addEffect(system);
                }
            }
        }
    }

    /**
     * 爆炸处理:
     * - 服务端:发"导弹死亡同步包"给追踪玩家(客户端实体立即消失),然后走父类
     *   逻辑(结算伤害 + 发爆炸粒子包给客户端)
     * - 客户端:只消失,不自行播放爆炸动画——爆炸动画与伤害统一由服务端爆炸
     *   驱动,避免"动画先出现、伤害后到"的时序错位
     */
    @Override
    protected void explodeRocket() {
        if (DEBUG) {
            TGCEAddon.getLogger().info("[Missile] explodeRocket 调用! side={} t={} pos=({},{},{}) targetAlive={}", side(), ticksExisted, posX, posY, posZ, target != null && !target.isDead);
        }
        if (this.world.isRemote) {
            this.setDead();
            return;
        }
        // 通知客户端实体立即消失,解决 1.12.2 destroy 同步不可靠导致的残留渲染
        PacketHandler.sendToAllAround(new MissileDeathMessage(this.getEntityId()),
                new TargetPoint(this.world.provider.getDimension(), this.posX, this.posY, this.posZ, 128.0));
        super.explodeRocket();
    }

    /** 直接命中(射线碰撞到实体/方块) */
    @Override
    protected void onHit(RayTraceResult raytraceResultIn) {
        if (DEBUG) {
            TGCEAddon.getLogger().info("[Missile] onHit 命中! side={} type={} entity={}", side(), raytraceResultIn.typeOfHit, raytraceResultIn.entityHit != null ? raytraceResultIn.entityHit.getName() : "null");
        }
        super.onHit(raytraceResultIn);
        // 客户端:命中后立即消失(伤害与爆炸动画由服务端驱动,客户端只需消失)
        if (this.world.isRemote) {
            this.setDead();
        }
    }

    /** 实体被标记死亡(记录消失时机) */
    @Override
    public void setDead() {
        if (DEBUG) {
            TGCEAddon.getLogger().info("[Missile] setDead side={} t={}", side(), ticksExisted);
        }
        super.setDead();
    }

    @Override
    public void onUpdate() {
        // 客户端:延迟解析目标(修复 spawn 早期目标未加载导致客户端导弹不追踪)
        if (this.world.isRemote) {
            this.tryResolveTarget();
            // 动力段动态光源:BOOST_TICKS(发动机点火)内携带持续尾焰光,离开动力段即熄灭
            if (this.ticksExisted < BOOST_TICKS) {
                updateMotorLight();
            } else {
                killMotorLight();
            }
        }

        // 动力段结束(仅客户端):火箭尾烟 → 制导导弹尾烟
        if (this.world.isRemote && this.ticksExisted >= BOOST_TICKS
                && (this.boostSystems != null || this.slideSystems == null)) {
            this.switchToSlideTrail();
        }

        // Debug:周期性输出飞行状态(每 10 tick),观察轨迹/速度/target 同步
        if (DEBUG && this.ticksExisted % 10 == 0) {
            double dist = this.target != null ? Math.sqrt(this.getDistanceSq(this.target)) : -1.0;
            double speed = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
            TGCEAddon.getLogger().info("[Missile] 状态 side={} t={} pos=({},{},{}) speed={} target={} dist={}",
                    side(), ticksExisted, String.format("%.1f", posX), String.format("%.1f", posY), String.format("%.1f", posZ),
                    String.format("%.2f", speed), target != null ? target.getName() : "null", String.format("%.2f", dist));
        }

        // 动力段线性加速(两端):每 tick 速度 +BOOST_ACCELERATION(方向不变);
        // 滑行段无推力,由父类空气阻力(每 tick ×0.99)自然减速
        if (this.ticksExisted < BOOST_TICKS) {
            double speed = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
            if (speed > 0.0001) {
                double factor = (speed + BOOST_ACCELERATION) / speed;
                this.motionX *= factor;
                this.motionY *= factor;
                this.motionZ *= factor;
            }
        }

        // 当前速度与失速判定:速度过低 = 失去升力,开始头坠地
        double speedNow = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        boolean stalled = speedNow < STALL_SPEED;

        // 重力(两端):动力段发动机推力抵消大部分重力(平飞加速,解决下坠夸张);
        // 滑行段恢复重力——失速时强重力下坠(无升力头坠地),正常时轻微重力(鞘翅感)
        double gravity = this.ticksExisted < BOOST_TICKS
                ? GRAVITY * BOOST_GRAVITY_FACTOR
                : (stalled ? STALL_GRAVITY : GRAVITY);
        this.motionY -= gravity;

        // 近炸引信 + 过载限制转向
        if (this.target != null && !this.target.isDead) {
            // 近炸:距锁定目标该距离内直接爆炸(客户端距离略大,保证先消失)
            double fuseDist = this.world.isRemote ? CLIENT_FUSE_DIST : PROXIMITY_FUSE_DIST;
            double distSq = this.getDistanceSq(this.target);
            if (distSq <= fuseDist * fuseDist) {
                if (DEBUG) {
                    TGCEAddon.getLogger().info("[Missile] 触发近炸! side={} t={} dist={} (阈值={})", side(), ticksExisted, String.format("%.2f", Math.sqrt(distSq)), String.format("%.2f", fuseDist));
                }
                this.explodeRocket();
                return;
            }

            Vec3d motion = new Vec3d(motionX, motionY, motionZ);
            double speed = motion.lengthVector();

            if (speed > 0.0001) {
                Vec3d v2 = new Vec3d(target.posX, target.posY + target.height * 0.5f, target.posZ)
                        .subtract(new Vec3d(this.posX, this.posY, this.posZ)).normalize();
                Vec3d v1 = motion.normalize();

                double dot = MathUtil.clamp(v1.dotProduct(v2), -1.0, 1.0);
                double angle = Math.acos(dot);

                // 过载限制:ω_max = a_max / v_s(弧度/秒),每 tick 最大转角 = ω_max / 20
                // speed 单位 格/tick → v_s = speed * 20(格/秒)
                // maxTurn = a_max / (speed * 20) / 20 = a_max / (speed * 400)(弧度)
                double maxTurn = MAX_OVERLOAD / (speed * 400.0);
                if (stalled) {
                    // 失速:转向能力大降,只能微弱修正(配合强重力头坠地)
                    maxTurn *= STALL_TURN_FACTOR;
                }
                if (maxTurn <= 0.0) {
                    maxTurn = 0.001;
                }

                // 实际转角及其占最大转角的比例(决定机动减速损失)
                double actualTurn = Math.min(angle, maxTurn);
                double turnRatio = Math.min(1.0, actualTurn / maxTurn);

                // 转向方向:夹角在允许范围内直接对准,否则按过载上限旋转
                Vec3d newDir;
                if (angle <= maxTurn) {
                    newDir = v2;
                } else {
                    Vec3d axis = v1.crossProduct(v2);
                    double axisLen = axis.lengthVector();
                    if (axisLen > 1.0E-6) {
                        axis = axis.scale(1.0 / axisLen);
                        newDir = MathUtil.rotateVector(v1, axis, maxTurn).normalize();
                    } else {
                        newDir = v2;
                    }
                }

                // 机动减速:转角占比越大损失越多;低于阈值的轻微修正不掉速
                double newSpeed = speed;
                if (turnRatio >= TURN_LOSS_THRESHOLD) {
                    double loss = TURN_DAMPING_K * turnRatio * turnRatio;
                    newSpeed = speed * (1.0 - loss);
                }
                // 速度下限
                double minSpeed = this.speed * MIN_SPEED_FACTOR;
                if (newSpeed < minSpeed) {
                    newSpeed = minSpeed;
                }

                motion = newDir.scale(newSpeed);
                this.motionX = motion.x;
                this.motionY = motion.y;
                this.motionZ = motion.z;
            }
        }
        super.onUpdate();
        if (this.world.isRemote && this.isDead) {
            killMotorLight();
        }
    }

    /** 动力段尾焰光强(14=火把级) */
    private static final int LIGHT_MOTOR = 14;
    /** 客户端:跟随本导弹的持续光源(仅动力段存在) */
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

    public static class Factory implements IChargedProjectileFactory<OverloadGuidedMissileProjectile> {

        @Override
        public OverloadGuidedMissileProjectile createProjectile(GenericGun gun, World world, EntityLivingBase p, float damage,
                float speed, int TTL, float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration,
                boolean blockdamage, EnumBulletFirePos firePos, float radius, double gravity) {
            Entity target = null;
            if (p instanceof EntityPlayer) {
                TGExtendedPlayer epc = TGExtendedPlayer.get((EntityPlayer) p);
                if (epc.lockOnEntity != null && epc.lockOnTicks >= ((GenericGun) p.getActiveItemStack().getItem()).getLockOnTicks()) {
                    target = epc.lockOnEntity;
                }
            }
            OverloadGuidedMissileProjectile proj;
            if (target != null) {
                proj = new OverloadGuidedMissileProjectile(world, p, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd,
                        dmgMin, penetration, blockdamage, firePos, radius, target);
            } else {
                proj = new OverloadGuidedMissileProjectile(world, p, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd,
                        dmgMin, penetration, blockdamage, firePos, radius, 0.01f);
            }
            // 高速移动中发射(如鞘翅飞行):叠加射手速度,避免导弹比玩家还慢
            ProjectileUtil.applyShooterVelocity(proj, p);
            return proj;
        }

        @Override
        public DamageType getDamageType() {
            return DamageType.EXPLOSION;
        }

        @Override
        public OverloadGuidedMissileProjectile createChargedProjectile(World world, EntityLivingBase p, float damage,
                float speed, int TTL, float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration,
                boolean blockdamage, EnumBulletFirePos firePos, float radius, double gravity, float charge, int ammoConsumed) {
            // 与科技枪原版一致:制导导弹走 shootGunPrimary → spawnProjectile 路径,此处不生成
            return null;
        }
    }
}
