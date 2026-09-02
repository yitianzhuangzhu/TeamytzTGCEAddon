package com.teamytz.tgceaddon.entities;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.entities.projectiles.RolandMissileProjectile;
import com.teamytz.tgceaddon.entities.projectiles.RolandMissileProjectileNuke;
import com.teamytz.tgceaddon.tileentities.TurretBaseTileEntMaster;
import com.teamytz.tgceaddon.util.AirborneTargetUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import techguns.TGSounds;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.factions.TGNpcFactions;

import java.util.List;
import java.util.UUID;

/**
 * 罗兰防空系统炮塔实体(NPC 化)
 *
 * 由炮塔控制器(卡片槽)放入 roland 卡片后生成,显示在炮塔底座上方:
 * - 索敌距离 300 格(防空远程索敌,每 5 tick 雷达扫描;前置量瞄准高速目标)
 * - 动画:radar 组持续顺时针旋转(供电+开机时),tr(追踪雷达)/l(左发射架)/r(右发射架)随目标俯仰
 * - 武器:发射定制指令线导弹(RolandMissileProjectile,快+长存活),
 *   左右发射架交替发射;同一时间仅一发在飞,导弹结算(命中/脱靶/失导)后目标仍存活才发射下一发
 * - 引导:发射时炮塔视线可达目标 → 导弹持续引导;视线被挡 → 导弹失导惯性飞行(大号火箭弹)
 * - 俯仰角限制:仰角 90°(可正对头顶)/ 俯角 20°(MC 语义 pitch:负=抬头、正=低头 → 范围 [-90, +20])
 */
public class EntityRolandTurret extends EntityCreature {

    private static final DataParameter<Integer> FACING =
            EntityDataManager.createKey(EntityRolandTurret.class, DataSerializers.VARINT);
    /** 俯仰角同步:living 实体 rotationPitch 依赖 HeadLook/Look 交替包同步,转向中几乎不更新,
     *  用 dataManager 每 tick 直接同步 float(无量化),渲染器读该值 */
    private static final DataParameter<Float> TURRET_PITCH =
            EntityDataManager.createKey(EntityRolandTurret.class, DataSerializers.FLOAT);
    /** 雷达旋转角同步(度):服务端供电+开机时递增,渲染器据此顺时针旋转 radar 组 */
    private static final DataParameter<Float> RADAR_ANGLE =
            EntityDataManager.createKey(EntityRolandTurret.class, DataSerializers.FLOAT);

    // ===== 火力/索敌参数 =====
    /** 索敌/开火距离(格):远程防空(300 格) */
    public static final double ATTACK_RANGE = 300.0D;
    /** 最大仰角(度;MC 语义 pitch 负=抬头)。90 = 可正对头顶目标(防空全向覆盖) */
    public static final float PITCH_MAX_UP = 90.0F;
    /** 最大俯角(度;pitch 正=低头) */
    public static final float PITCH_MAX_DOWN = 20.0F;
    /** 开火前水平对准容差(度):导弹飞行中自修正,放宽可更快打出第一发 */
    public static final float YAW_ALIGN_TOLERANCE = 10.0F;
    /** 开火前俯仰对准容差(度) */
    public static final float PITCH_ALIGN_TOLERANCE = 6.0F;
    /** 水平转向速度(度/tick) */
    public static final float YAW_TURN_SPEED = 6.0F;
    /** 俯仰转向速度(度/tick) */
    public static final float PITCH_TURN_SPEED = 8.0F;
    /** 在飞导弹最长等待(tick):超过视为脱靶/飞丢,允许发射下一发
     *  (导弹 TTL 800=40 秒,若一直等它结算,一发打空后炮塔会静默近 40 秒) */
    public static final int MAX_IN_FLIGHT_WAIT = 120;
    /** 雷达旋转速度(度/tick,200°/秒 ≈ 1.8 秒一圈,模拟真实雷达扫描) */
    public static final float RADAR_SPIN_SPEED = 10.0F;
    /** 每发导弹 RF 消耗 */
    public static final int MISSILE_POWER = 300;
    /** 导弹结算后的装填冷却(tick) */
    public static final int MISSILE_COOLDOWN = 30;
    /** 罗兰导弹参数:初速 3.0 格/tick(快于玩家武器 2.5)、TTL 800(40 秒,射程远超玩家) */
    public static final float MISSILE_SPEED = 3.0F;
    public static final int MISSILE_TTL = 800;
    /** 前置量瞄准最大预测时长(tick):目标速度×提前量,导弹飞行时间的一半,封顶 20 tick */
    private static final double MAX_LEAD_TICKS = 20.0D;
    /** 目标速度估计限幅(格/tick):防止传送/瞬移造成的前置量尖峰 */
    private static final double MAX_TRACK_VELOCITY = 6.0D;

    /** 发射架模型旋转点/炮口(根模型空间,像素;模型保持 Blockbench 原始数据):
     *  l(左发射架)旋转点 (12,11.5,-2),炮口 (12,15.5,-23.5)
     *  r(右发射架)旋转点 (-12,11.5,-2),炮口 (-12,15.5,-23.5) */
    private static final float[] LAUNCH_L_CENTER = {12.0F, 11.5F, -2.0F};
    private static final float[] LAUNCH_L_MUZZLE = {12.0F, 15.5F, -23.5F};
    private static final float[] LAUNCH_R_CENTER = {-12.0F, 11.5F, -2.0F};
    private static final float[] LAUNCH_R_MUZZLE = {-12.0F, 15.5F, -23.5F};
    /** 模型根空间底部 y=24(格) → 渲染器 translate 33px:底部贴底座上方 2 像素 */
    private static final float RENDER_TRANSLATE = 33.0F;

    /** 所属炮塔控制器(master)的位置 */
    private BlockPos masterPos = null;
    /** 炮塔类型(卡片 NBT turretType) */
    private String turretType = "roland";
    /** 服务端累积俯仰角(平滑瞄准,绕开 lookHelper 清零问题,同 BMPT) */
    private float turretPitch = 0.0F;
    /** 服务端累积水平角 */
    private float turretYaw = 0.0F;
    /** 服务端雷达旋转角(度,累计) */
    private float radarAngle = 0.0F;
    /** 当前在飞导弹(服务端:同一时间仅一发) */
    private Entity activeMissile = null;
    /** 下一发可用 tick 计数(服务端) */
    private int missileCooldown = 0;
    /** 下一发使用左( true )/右( false )发射架 */
    private boolean sideLeft = true;
    /** 目标速度估计(格/tick,位置差分;用于前置量瞄准) */
    private Vec3d targetVelocity = Vec3d.ZERO;
    /** 上一次记录的目标位置(速度差分用) */
    private Vec3d lastTargetPos = null;
    /** 速度估计对应的目标(换目标时清零) */
    private EntityLivingBase velocityTrackTarget = null;

    // ===== 客户端帧级补间状态(仅客户端渲染使用,同 BMPT)=====
    public float renderYawHead = Float.NaN;
    public float renderPitch = Float.NaN;
    public long renderLastTime = -1L;

    public EntityRolandTurret(World world) {
        super(world);
        this.setSize(2.0F, 2.6F);
        this.noClip = true;
    }

    public EntityRolandTurret(World world, BlockPos masterPos, EnumFacing facing) {
        this(world);
        this.masterPos = masterPos;
        this.dataManager.set(FACING, facing.getIndex());
        this.setPosition(masterPos.getX() + 0.5, masterPos.getY() + 0.5625, masterPos.getZ() + 0.5);
        TGCEAddon.getLogger().info("[debug][罗兰] 构造 side=" + (world.isRemote ? "client" : "server")
                + " master=" + masterPos + " facing=" + facing.getName()
                + " pos=(" + this.posX + "," + this.posY + "," + this.posZ + ")");
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FACING, EnumFacing.SOUTH.getIndex());
        this.dataManager.register(TURRET_PITCH, 0.0F);
        this.dataManager.register(RADAR_ANGLE, 0.0F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(200.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(ATTACK_RANGE);
    }

    @Override
    protected void initEntityAI() {
        // ===== 索敌(300 格远程防空):自研雷达扫描 AI,每 5 tick 一次,
        // 快速捕捉高速通过的空中目标(科技枪原目标 AI 重扫间隔 20~60 tick,
        // 高速目标可能在两次扫描之间穿过防区而漏检)=====
        this.targetTasks.addTask(2, new AIRolandTargetSearch(this));

        // ===== 开火 AI =====
        this.tasks.addTask(1, new AIRolandAttack(this));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.world.isRemote) {
            return;
        }
        // ===== 服务端:跟随 master =====
        TurretBaseTileEntMaster master = getMasterTile();
        if (master == null || !master.isFormed()) {
            this.setDead();
            return;
        }
        BlockPos mp = master.getPos();
        EnumFacing dir = master.getMultiblockDirection();
        this.setPosition(mp.getX() + 0.5, mp.getY() + 0.5625, mp.getZ() + 0.5);
        this.rotationYaw = yawForFacing(dir);
        if (this.dataManager.get(FACING) != dir.getIndex()) {
            this.dataManager.set(FACING, dir.getIndex());
        }

        boolean hasPower = master.getEnergyStorage().getEnergyStored() > 0;
        boolean redstoneOk = isRedstoneAllowed();

        // ===== 雷达旋转:有电且开机 → 顺时针旋转 =====
        if (hasPower && redstoneOk) {
            this.radarAngle = (this.radarAngle + RADAR_SPIN_SPEED) % 360.0F;
        }
        this.dataManager.set(RADAR_ANGLE, this.radarAngle);

        // ===== 转向/回正(同 BMPT:独立字段累积,绕开 lookHelper 清零)=====
        EntityLivingBase tgt = this.getAttackTarget();
        if (tgt != null && hasPower && redstoneOk) {
            // 目标速度估计(位置差分,换目标时清零):用于前置量瞄准,提前量 = 速度 × 飞行时间一半
            updateTargetVelocity(tgt);
            Vec3d aim = getPredictedAim(tgt);
            float aimYaw = (float) (MathHelper.atan2(aim.z - this.posZ, aim.x - this.posX) * 180.0D / Math.PI) - 90.0F;
            this.turretYaw = approachAngle(this.turretYaw, aimYaw, YAW_TURN_SPEED);
            this.rotationYawHead = this.turretYaw;
            this.turretPitch = MathHelper.clamp(
                    approachAngle(this.turretPitch, getRequiredPitch(aim), PITCH_TURN_SPEED),
                    -PITCH_MAX_UP, PITCH_MAX_DOWN);
            this.rotationPitch = this.turretPitch;
        } else {
            this.targetVelocity = Vec3d.ZERO;
            this.lastTargetPos = null;
            this.velocityTrackTarget = null;
            this.turretYaw = lerpAngle(this.turretYaw, this.rotationYaw, 0.15F);
            this.rotationYawHead = this.turretYaw;
            this.turretPitch = lerpFloat(this.turretPitch, 0.0F, 0.15F);
            this.rotationPitch = this.turretPitch;
        }
        this.dataManager.set(TURRET_PITCH, this.turretPitch);

        if (this.ticksExisted % 40 == 0) {
            EntityLivingBase t2 = this.getAttackTarget();
            float aimYawLog = 0.0F;
            if (t2 != null) {
                aimYawLog = (float) (MathHelper.atan2(t2.posZ - this.posZ, t2.posX - this.posX) * 180.0D / Math.PI) - 90.0F;
            }
            TGCEAddon.getLogger().info("[debug][罗兰] server tick=" + this.ticksExisted
                    + " target=" + (t2 != null ? t2.getName() : "null")
                    + " dist=" + (t2 != null ? MathHelper.sqrt(t2.getDistanceSq(this)) : 0)
                    + " yaw=" + this.turretYaw + " aimYaw=" + aimYawLog
                    + " pitch=" + this.turretPitch
                    + " radar=" + String.format("%.0f", this.radarAngle)
                    + " missileInFlight=" + (this.activeMissile != null && !this.activeMissile.isDead)
                    + " energy=" + master.getEnergyStorage().getEnergyStored()
                    + " redstone=" + redstoneOk);
        }
    }

    /** 客户端转向同步修正(同 BMPT):只更新位置与 yaw/pitch,不碰 rotationYawHead */
    @Override
    public void setPositionAndRotationDirect(double x, double y, double z,
                                             float yaw, float pitch,
                                             int posRotationIncrements, boolean teleport) {
        this.setPosition(x, y, z);
        this.setRotation(yaw, pitch);
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    /**
     * 更新目标速度估计(格/tick,位置差分;限幅防瞬移尖峰)。
     * 换目标时清零,避免用旧目标的速度推算新目标的前置量。
     */
    private void updateTargetVelocity(EntityLivingBase target) {
        Vec3d tp = new Vec3d(target.posX, target.posY + target.getEyeHeight(), target.posZ);
        if (this.velocityTrackTarget != target) {
            this.velocityTrackTarget = target;
            this.lastTargetPos = tp;
            this.targetVelocity = Vec3d.ZERO;
            return;
        }
        if (this.lastTargetPos != null) {
            Vec3d vel = tp.subtract(this.lastTargetPos);
            double len = vel.lengthVector();
            if (len > MAX_TRACK_VELOCITY) {
                vel = vel.normalize().scale(MAX_TRACK_VELOCITY);
            }
            this.targetVelocity = vel;
        }
        this.lastTargetPos = tp;
    }

    /**
     * 前置量瞄准点:目标当前位置 + 目标速度 × 提前量。
     * 提前量 = 导弹飞行时间的一半(封顶 MAX_LEAD_TICKS):导弹在飞行中仍会自修正,
     * 前置量只需让初始指向大体朝前,避免高速目标在导弹到达前已横向移出转弯半径。
     */
    private Vec3d getPredictedAim(EntityLivingBase target) {
        Vec3d cur = new Vec3d(target.posX, target.posY + target.getEyeHeight(), target.posZ);
        if (this.targetVelocity.lengthVector() <= 0.01D || this.velocityTrackTarget != target) {
            return cur;
        }
        double dist = MathHelper.sqrt(target.getDistanceSq(this));
        double leadTicks = MathHelper.clamp(dist / MISSILE_SPEED * 0.5, 3.0D, MAX_LEAD_TICKS);
        return cur.add(this.targetVelocity.scale(leadTicks));
    }

    /** 计算瞄准给定目标点所需的俯仰角(度,负=抬头、正=低头) */
    private float getRequiredPitch(Vec3d aim) {
        double dx = aim.x - this.posX;
        double dz = aim.z - this.posZ;
        double dy = aim.y - (this.posY + this.getEyeHeight());
        double dist = MathHelper.sqrt(dx * dx + dz * dz);
        return (float) (-(MathHelper.atan2(dy, dist) * 180.0D / Math.PI));
    }

    private float getRequiredPitch(EntityLivingBase target) {
        return getRequiredPitch(new Vec3d(target.posX, target.posY + target.getEyeHeight(), target.posZ));
    }

    /**
     * 计算发射架炮口的世界坐标(随 turretYaw 水平旋转 + turretPitch 绕发射架旋转点俯仰)。
     * 变换与渲染器完全一致:root(px) -> 绕旋转点 X 轴俯仰 -> yaw(180-yaw) -> translate(35px)/scale(1,-1,1)
     *
     * @param center 发射架旋转点(根模型空间,像素) {x,y,z}
     * @param muzzle 炮口(根模型空间,像素) {x,y,z}
     */
    private double[] getMuzzlePos(float[] center, float[] muzzle) {
        float lx = center[0], ly = center[1], lz = center[2];
        float mx = muzzle[0], my = muzzle[1], mz = muzzle[2];
        // 1) 绕发射架旋转点俯仰(X 轴旋转,与渲染器 rotateAngleX = radians(pitch) 一致)
        float p = (float) Math.toRadians(this.turretPitch);
        float cosP = MathHelper.cos(p);
        float sinP = MathHelper.sin(p);
        float dy = my - ly;
        float dz = mz - lz;
        float y3 = ly + (dy * cosP - dz * sinP);
        float z3 = lz + (dy * sinP + dz * cosP);
        // 2) 水平旋转(渲染器 scale(-1,-1,1) + rotate(yaw - 180):wx 取负,θ = yaw-180)
        float angle = (float) Math.toRadians(this.turretYaw - 180.0F);
        float cos = MathHelper.cos(angle);
        float sin = MathHelper.sin(angle);
        float wx = -((mx / 16.0F) * cos + (z3 / 16.0F) * sin);
        float wz = -(mx / 16.0F) * sin + (z3 / 16.0F) * cos;
        // 3) 垂直:translate(35px) 在 scale 之前 -> 世界偏移 = (35 - y3)/16 格
        float wy = (RENDER_TRANSLATE - y3) / 16.0F;
        return new double[]{this.posX + wx, this.posY + wy, this.posZ + wz};
    }

    /**
     * 发射一发罗兰指令线导弹,从指定侧发射架炮口射出。
     * 引导标志 = 发射瞬间炮塔视线是否可达目标(可达 → 持续引导;被挡 → 失导惯性飞行)。
     * 消耗 RF + 火箭弹(TGItems.ROCKET)。
     *
     * @param sideLeft true=左发射架,false=右发射架
     * @return 成功发射的导弹(失败返回 null)
     */
    public RolandMissileProjectile fireMissile(boolean sideLeft, EntityLivingBase target) {
        TurretBaseTileEntMaster master = getMasterTile();
        if (master == null || !master.isFormed()) {
            return null;
        }
        // 调试(每 60 tick):无论弹药是否充足都输出输入槽状态,区分"没弹药"与"AI 未到达发射"
        if (TGCEAddon.DEBUG_IR && this.ticksExisted % 60 == 0) {
            ItemStack nukeRef = techguns.TGItems.ROCKET_NUKE;
            boolean upgradeOk = master.hasUpgrade(com.teamytz.tgceaddon.item.ItemTurretUpgrade.TYPE_LEAD_COMPUTING);
            StringBuilder sb = new StringBuilder("[罗兰][调试] 升级卡=" + upgradeOk
                    + " ROCKET_NUKE参考="
                    + (nukeRef.isEmpty() ? "空" : nukeRef.getItem().getRegistryName() + "@" + nukeRef.getItemDamage())
                    + " | 槽位:");
            for (int i = TurretBaseTileEntMaster.SLOT_INPUT1;
                    i < TurretBaseTileEntMaster.SLOT_INPUT1 + TurretBaseTileEntMaster.INPUTS_SIZE; i++) {
                ItemStack s = master.getInputStack(i);
                if (s.isEmpty()) {
                    sb.append(" [空]");
                } else {
                    boolean matchNuke = !nukeRef.isEmpty() && s.getItem() == nukeRef.getItem()
                            && s.getItemDamage() == nukeRef.getItemDamage();
                    sb.append(" [").append(s.getItem().getRegistryName()).append("@")
                            .append(s.getItemDamage()).append("x").append(s.getCount())
                            .append(matchNuke ? "=核弹]" : "]");
                }
            }
            TGCEAddon.getLogger().info(sb.toString());
        }
        // 弹药选择:有核火箭且安装了炮塔升级卡 → 发射核导弹变体;否则普通火箭
        // (升级卡门控:没有升级卡即使装了核弹也只能发射普通导弹)
        boolean upgradeOk = master.hasUpgrade(com.teamytz.tgceaddon.item.ItemTurretUpgrade.TYPE_LEAD_COMPUTING);
        boolean nuke = upgradeOk && master.hasNukeAmmo();
        // 关键修复:先检查能量与弹药是否充足,不足则不消耗任何东西。
        // 旧逻辑先消耗 300 能量再检查弹药,无弹药时会每 tick 白耗能量直到耗尽,
        // 能量归零后 AI 的 shouldExecute 因 energy>0 不成立而停止 → 炮塔卡死无法再攻击
        if (master.getEnergyStorage().getEnergyStored() < MISSILE_POWER) {
            return null;
        }
        if (nuke) {
            if (!master.hasNukeAmmo()) {
                return null;
            }
        } else if (!master.hasRocketAmmo()) {
            return null;
        }
        // 弹药与能量都充足:先消耗弹药再消耗能量
        if (nuke) {
            master.consumeNukeAmmo();
        } else {
            master.consumeRocketAmmo();
        }
        master.consumeTurretPower(MISSILE_POWER);
        // GenericProjectile 构造读 shooter.rotationYawHead/rotationPitch 算弹道方向,先写回累积角
        this.rotationYawHead = this.turretYaw;
        this.rotationPitch = this.turretPitch;
        boolean guided = this.canEntityBeSeen(target);
        RolandMissileProjectile missile;
        if (nuke) {
            // 核弹变体:伤害/半径/射程 5 倍,可摧毁地形(模仿科技枪 RocketProjectileNuke 的 5x 修饰)
            missile = new RolandMissileProjectileNuke(this.world, this,
                    150.0f, MISSILE_SPEED, MISSILE_TTL, 0.05f, 10, 20, 100.0f, 0.25f, true,
                    EnumBulletFirePos.CENTER, 20.0f, 0.0f, target.getEntityId(), guided);
        } else {
            // 普通弹:威力 60(与玩家指令线导弹同级)。
            // 科技枪 damageDropStart/End 同时是"飞行距离衰减"与"爆炸半径"参数:
            // - 飞行衰减:150~300 格区间,保证 300 格交战距离内直击伤害不缩水
            // - 爆炸参数独立传入(主半径 6 / 次半径 12):6 格内满伤 60,
            //   6~12 格线性衰减到 30,12 格外无伤;不摧毁地形
            missile = new RolandMissileProjectile(this.world, this,
                    60.0f, MISSILE_SPEED, MISSILE_TTL, 0.05f, 150, 300, 40.0f, 0.25f, false,
                    EnumBulletFirePos.CENTER, 4.0f, 0.0f, target.getEntityId(), guided,
                    60.0f, 30.0f, 6.0f, 12.0f, false);
        }
        double[] muzzle = sideLeft
                ? getMuzzlePos(LAUNCH_L_CENTER, LAUNCH_L_MUZZLE)
                : getMuzzlePos(LAUNCH_R_CENTER, LAUNCH_R_MUZZLE);
        missile.setPosition(muzzle[0], muzzle[1], muzzle[2]);
        this.world.spawnEntity(missile);
        this.playRocketFireSound();
        TGCEAddon.getLogger().info("[debug][罗兰] 发射导弹 side=" + (sideLeft ? "L" : "R")
                + " nuke=" + nuke + " guided=" + guided + " target=" + target.getName());
        return missile;
    }

    /** 导弹发射音(科技枪追踪导弹音效) */
    public void playRocketFireSound() {
        this.world.playSound(null, this.posX, this.posY, this.posZ,
                TGSounds.GUIDEDMISSILE_FIRE, SoundCategory.BLOCKS, 3.0F, 1.0F);
    }

    public static float yawForFacing(EnumFacing facing) {
        switch (facing) {
            case SOUTH: return 0.0F;
            case WEST: return 90.0F;
            case EAST: return -90.0F;
            case NORTH: default: return 180.0F;
        }
    }

    public EnumFacing getTurretFacing() {
        return EnumFacing.getFront(this.dataManager.get(FACING));
    }

    /** 渲染用俯仰角(dataManager 每 tick 同步,无量化) */
    public float getTurretPitch() {
        return this.dataManager.get(TURRET_PITCH);
    }

    /** 渲染用雷达旋转角(度,dataManager 同步;未供电/关机时保持不变) */
    public float getRadarAngle() {
        return this.dataManager.get(RADAR_ANGLE);
    }

    private static float lerpAngle(float current, float target, float t) {
        float d = target - current;
        while (d > 180.0F) {
            d -= 360.0F;
        }
        while (d < -180.0F) {
            d += 360.0F;
        }
        return current + d * t;
    }

    private static float lerpFloat(float current, float target, float t) {
        return current + (target - current) * t;
    }

    private static float approachAngle(float current, float target, float maxDelta) {
        float diff = MathHelper.wrapDegrees(target - current);
        if (maxDelta <= 0.0F || Math.abs(diff) <= maxDelta) {
            return target;
        }
        return MathHelper.wrapDegrees(current + (diff > 0.0F ? maxDelta : -maxDelta));
    }

    private TurretBaseTileEntMaster getMasterTile() {
        if (this.masterPos == null) {
            return null;
        }
        TileEntity te = this.world.getTileEntity(this.masterPos);
        return te instanceof TurretBaseTileEntMaster ? (TurretBaseTileEntMaster) te : null;
    }

    private boolean isRedstoneAllowed() {
        TurretBaseTileEntMaster master = getMasterTile();
        return master != null && master.isRedstoneEnabled();
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return true;
    }

    // ===== NBT 持久化 =====
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (this.masterPos != null) {
            compound.setInteger("masterX", this.masterPos.getX());
            compound.setInteger("masterY", this.masterPos.getY());
            compound.setInteger("masterZ", this.masterPos.getZ());
        }
        compound.setString("turretType", this.turretType);
        compound.setInteger("facing", this.dataManager.get(FACING));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("masterX")) {
            this.masterPos = new BlockPos(compound.getInteger("masterX"),
                    compound.getInteger("masterY"), compound.getInteger("masterZ"));
        }
        if (compound.hasKey("turretType")) {
            this.turretType = compound.getString("turretType");
        }
        if (compound.hasKey("facing")) {
            this.dataManager.set(FACING, compound.getInteger("facing"));
        }
    }

    /**
     * 罗兰开火 AI:
     * - 同一时间仅一发导弹在飞;导弹死亡(命中/脱靶/失导炸地/TTL)后,
     *   若目标仍存活 → 装填冷却后发射下一发(左右发射架交替)
     * - 目标被墙挡住仍会发射(导弹失导惯性飞行),符合"视线被挡 → 大号火箭弹"设定
     */
    static class AIRolandAttack extends EntityAIBase {

        private final EntityRolandTurret turret;
        private int cooldown = 0;
        /** 当前在飞导弹已飞 tick 数(超 MAX_IN_FLIGHT_WAIT 视为脱靶,允许下一发) */
        private int missileInFlightTicks = 0;

        public AIRolandAttack(EntityRolandTurret turret) {
            this.turret = turret;
            // mutex=0:不与任何任务互斥。部分整合包(DynamicStealth 等)会在实体 tasks 里
            // 注入最高优先级常驻任务,若本 AI 带 mutex 位会被 canUse 永久拦截无法开火
            this.setMutexBits(0);
        }

        @Override
        public boolean shouldExecute() {
            if (this.turret.getAttackTarget() == null) {
                return false;
            }
            TurretBaseTileEntMaster master = this.turret.getMasterTile();
            return master != null && master.getEnergyStorage().getEnergyStored() > 0
                    && this.turret.isRedstoneAllowed();
        }

        @Override
        public void startExecuting() {
            this.cooldown = 0;
        }

        @Override
        public void resetTask() {
            this.cooldown = 0;
        }

        @Override
        public void updateTask() {
            EntityLivingBase target = this.turret.getAttackTarget();
            if (target == null) {
                return;
            }
            // 诊断(每 5 tick):开火 AI 是否真的看到目标、目标状态如何
            if (TGCEAddon.DEBUG_IR && this.turret.ticksExisted % 5 == 0) {
                TGCEAddon.getLogger().info("[罗兰][开火] 看到目标=" + target.getName()
                        + " airborne=" + AirborneTargetUtil.isAirborne(target)
                        + " 超距=" + (target.getDistanceSq(this.turret) > ATTACK_RANGE * ATTACK_RANGE));
            }
            // 诊断(每 20 tick):输出逐项检查结果,定位不开火的环节
            if (TGCEAddon.DEBUG_IR && this.turret.ticksExisted % 20 == 0) {
                Vec3d aimDbg = this.turret.getPredictedAim(target);
                float reqPitchDbg = this.turret.getRequiredPitch(aimDbg);
                float targetYawDbg = (float) (MathHelper.atan2(aimDbg.z - this.turret.posZ,
                        aimDbg.x - this.turret.posX) * 180.0D / Math.PI) - 90.0F;
                TGCEAddon.getLogger().info("[罗兰][AI] 检查: target=" + target.getName()
                        + " airborne=" + AirborneTargetUtil.isAirborne(target)
                        + " 范围=" + (target.getDistanceSq(this.turret) <= ATTACK_RANGE * ATTACK_RANGE)
                        + " reqPitch=" + String.format("%.1f", reqPitchDbg)
                        + " |yaw差|=" + String.format("%.1f", Math.abs(MathHelper.wrapDegrees(this.turret.turretYaw - targetYawDbg)))
                        + " |pitch差|=" + String.format("%.1f", Math.abs(MathHelper.wrapDegrees(this.turret.turretPitch - reqPitchDbg)))
                        + " 冷却=" + this.cooldown + " 在飞=" + (this.turret.activeMissile != null && !this.turret.activeMissile.isDead));
            }
            // 目标死亡/超距:放弃目标
            if (target.isDead || target.getDistanceSq(this.turret) > ATTACK_RANGE * ATTACK_RANGE) {
                if (TGCEAddon.DEBUG_IR) {
                    TGCEAddon.getLogger().info("[罗兰][开火] 放弃目标(死亡/超距) target=" + target.getName()
                            + " dist=" + String.format("%.1f", Math.sqrt(target.getDistanceSq(this.turret))));
                }
                this.turret.setAttackTarget(null);
                resetFireState();
                return;
            }
            // 目标落地(不再是空中目标):防空炮塔停止攻击并放弃目标
            if (!AirborneTargetUtil.isAirborne(target)) {
                if (TGCEAddon.DEBUG_IR) {
                    TGCEAddon.getLogger().info("[罗兰][开火] 放弃目标(不空中) target=" + target.getName()
                            + " onGround=" + target.onGround + " water=" + target.isInWater() + " riding=" + target.isRiding());
                }
                this.turret.setAttackTarget(null);
                resetFireState();
                return;
            }
            // 俯仰角限制导致无法命中:停火,继续转头(以目标当前位置判定,预测点可能越出俯角线)
            float requiredPitch = this.turret.getRequiredPitch(target);
            if (requiredPitch < -PITCH_MAX_UP || requiredPitch > PITCH_MAX_DOWN) {
                resetFireState();
                return;
            }
            // 炮口对准检查:限速转向过程中不开火(对准前置量瞄准点)
            Vec3d aim = this.turret.getPredictedAim(target);
            float targetYaw = (float) (MathHelper.atan2(aim.z - this.turret.posZ,
                    aim.x - this.turret.posX) * 180.0D / Math.PI) - 90.0F;
            float reqPitchAim = this.turret.getRequiredPitch(aim);
            if (Math.abs(MathHelper.wrapDegrees(this.turret.turretYaw - targetYaw)) > YAW_ALIGN_TOLERANCE
                    || Math.abs(MathHelper.wrapDegrees(this.turret.turretPitch - reqPitchAim)) > PITCH_ALIGN_TOLERANCE) {
                resetFireState();
                return;
            }

            // ===== 导弹结算:在飞导弹死亡或超时后,目标仍存活 → 装填冷却 → 下一发 =====
            if (this.turret.activeMissile != null) {
                this.missileInFlightTicks++;
                if (this.turret.activeMissile.isDead || this.missileInFlightTicks > MAX_IN_FLIGHT_WAIT) {
                    this.turret.activeMissile = null;
                    this.missileInFlightTicks = 0;
                    this.cooldown = MISSILE_COOLDOWN;
                }
            }
            if (this.cooldown > 0) {
                this.cooldown--;
                return;
            }
            if (this.turret.activeMissile != null) {
                return; // 一发在飞,等待其结算
            }

            // ===== 发射(左右发射架交替)=====
            RolandMissileProjectile m = this.turret.fireMissile(this.turret.sideLeft, target);
            if (m != null) {
                this.turret.activeMissile = m;
                this.missileInFlightTicks = 0;
                this.turret.sideLeft = !this.turret.sideLeft;
            }
        }

        private void resetFireState() {
            this.cooldown = 0;
        }
    }

    /**
     * 罗兰雷达扫描索敌 AI(替代科技枪 TurretEntityAINearestAttackableTarget):
     * - 每 5 tick 一次"雷达扫描",快速捕捉高速通过的空中目标(原 AI 重扫间隔 20~60 tick,
     *   高速目标可能在两次扫描之间穿过防区而漏检)
     * - 300 格立方体扫描,筛选条件与开火 AI 一致(红石/距离/敌对/PVP/动物/空中/俯仰角)
     */
    static class AIRolandTargetSearch extends EntityAIBase {

        private final EntityRolandTurret turret;
        private int scanTimer = 0;

        AIRolandTargetSearch(EntityRolandTurret turret) {
            this.turret = turret;
            // mutex=0:见 AIRolandAttack 注释,防止被其它 mod 注入的常驻任务互斥拦截
            this.setMutexBits(0);
        }

        @Override
        public boolean shouldExecute() {
            return true;
        }

        @Override
        public void updateTask() {
            if (this.scanTimer > 0) {
                this.scanTimer--;
                return;
            }
            this.scanTimer = 5;
            EntityLivingBase current = this.turret.getAttackTarget();
            if (current != null && !current.isDead) {
                return; // 已有目标,维持锁定(有效性由开火 AI 检查)
            }
            double range = ATTACK_RANGE;
            AxisAlignedBB box = new AxisAlignedBB(
                    this.turret.posX - range, this.turret.posY - range, this.turret.posZ - range,
                    this.turret.posX + range, this.turret.posY + range, this.turret.posZ + range);
            List<EntityLivingBase> list = this.turret.world.getEntitiesWithinAABB(EntityLivingBase.class, box,
                    e -> e != null && e.isEntityAlive() && isValidTarget(e));
            EntityLivingBase best = null;
            double bestDist = Double.MAX_VALUE;
            for (EntityLivingBase e : list) {
                double d = e.getDistanceSq(this.turret);
                if (d < bestDist) {
                    bestDist = d;
                    best = e;
                }
            }
            this.turret.setAttackTarget(best);
            // 索敌诊断(限频):锁定/无目标时输出,验证索敌 AI 是否运行、玩家是否在扫描范围内被看见
            if (TGCEAddon.DEBUG_IR) {
                if (best != null) {
                    TGCEAddon.getLogger().info("[罗兰][索敌] 锁定 target=" + best.getName()
                            + " dist=" + String.format("%.1f", Math.sqrt(bestDist)));
                } else if (this.turret.ticksExisted % 100 == 0) {
                    int players = 0;
                    for (net.minecraft.entity.player.EntityPlayer p : this.turret.world.playerEntities) {
                        if (p.getDistanceSq(this.turret) <= ATTACK_RANGE * ATTACK_RANGE) {
                            players++;
                        }
                    }
                    TGCEAddon.getLogger().info("[罗兰][索敌] 扫描无目标 候选=" + list.size() + " 范围内玩家=" + players);
                }
            }
        }

        /** 与开火 AI 相同的目标筛选(红石/距离/敌对/空中/俯仰角限制) */
        private boolean isValidTarget(EntityLivingBase entity) {
            if (!this.turret.isRedstoneAllowed()) {
                return false;
            }
            if (entity.getDistanceSq(this.turret) > ATTACK_RANGE * ATTACK_RANGE) {
                return false;
            }
            boolean isTarget = false;
            if (entity instanceof IMob) {
                isTarget = true;
            } else {
                TurretBaseTileEntMaster master = this.turret.getMasterTile();
                if (master != null) {
                    byte pvp = master.getPvpSetting();
                    UUID owner = master.getOwner();
                    if (entity instanceof EntityPlayer) {
                        UUID plyId = ((EntityPlayer) entity).getGameProfile().getId();
                        if (plyId != null) {
                            if (!master.isPlayerPlaced()) {
                                // 自然结构/模组生成的炮塔(未经玩家放置流程):视为敌对建筑,攻击一切玩家
                                isTarget = true;
                            } else if (owner == null) {
                                // 玩家放置但无主(异常状态):同样敌对一切玩家
                                isTarget = true;
                            } else if (pvp != 0) {
                                if (owner.equals(plyId)) {
                                    isTarget = false;
                                } else {
                                    isTarget = TGNpcFactions.shouldAttack(owner, plyId, pvp);
                                }
                            }
                        }
                    }
                    if (!isTarget && master.attackAnimals
                            && entity instanceof IAnimals && !(entity instanceof EntityRolandTurret)) {
                        if (entity instanceof EntityTameable) {
                            isTarget = !((EntityTameable) entity).isTamed();
                        } else if (entity instanceof EntityHorse) {
                            isTarget = !((EntityHorse) entity).isTame();
                        } else if (entity instanceof IEntityOwnable) {
                            isTarget = ((IEntityOwnable) entity).getOwner() == null;
                        } else {
                            isTarget = true;
                        }
                    }
                }
            }
            if (!isTarget) {
                return false;
            }
            // 防空炮塔只攻击空中目标:末影龙/凋零/科技枪直升机等飞行单位,
            // 地面单位(僵尸/玩家走地等)不锁定(通用判定 + 配置文件名单兜底)
            if (!AirborneTargetUtil.isAirborne(entity)) {
                return false;
            }
            // 俯仰角限制:目标在仰角>90° 或俯角>20° 处无法命中,不锁定
            Vec3d aim = this.turret.getPredictedAim(entity);
            float reqPitch = this.turret.getRequiredPitch(aim);
            return reqPitch >= -PITCH_MAX_UP && reqPitch <= PITCH_MAX_DOWN;
        }
    }
}
