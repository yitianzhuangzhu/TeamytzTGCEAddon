package com.teamytz.tgceaddon.entities;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.entities.projectiles.CannonShellProjectile;
import com.teamytz.tgceaddon.init.ModSounds;
import com.teamytz.tgceaddon.tileentities.TurretBaseTileEntMaster;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import techguns.TGSounds;
import techguns.entities.ai.TurretEntityAINearestAttackableTarget;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.entities.projectiles.GuidedMissileProjectile;
import techguns.factions.TGNpcFactions;

import java.util.UUID;

/**
 * BMPT 炮塔实体（NPC 化）
 * 由炮塔控制器(卡片槽)放入 bmpt 卡片后生成，显示在炮塔底座上方：
 * - 本质与科技枪炮塔 NPC 相同：EntityCreature + 索敌 AI + 自定义开火 AI
 * - 服务端每 tick 跟随 master 的位置，朝向由结构方向决定（模型朝向）
 * - 索敌仿照 NPCTurret：敌对生物(IMob)、动物(按 attackAnimals)、玩家(按 PVP 设置)
 * - 开火仿照直升机 AIHelicopterAttack：左机炮 20 发 -> 左导弹巢 2 火箭 -> 右机炮 20 发 -> 右导弹巢 2 火箭 -> 两侧循环
 * - 弹药从模型对应发射位射出（左右炮管口、左右导弹巢口），随 yawHead 旋转
 * - 开火消耗：RF（master 能量）、火箭弹（TGItems.ROCKET，master 弹药输入槽）；机炮弹药暂不消耗
 */
public class EntityBMPTTurret extends EntityCreature {

    private static final DataParameter<Integer> FACING =
            EntityDataManager.createKey(EntityBMPTTurret.class, DataSerializers.VARINT);
    /**
     * 俯仰角同步：living 实体的 rotationPitch 依赖 SPacketEntityLook 同步，
     * 而 EntityTrackerEntry 对 living 是"HeadLook(仅yawHead)/Look(含pitch)交替"发送——
     * 转向中 yawHead 每 2 tick 都在变，永远走 HeadLook 分支，pitch 包几乎不发，
     * 导致客户端 pitch 严重滞后（日志里只能看到 -8.4/0/1.4 等离散旧值）。
     * 这里用 dataManager 每 tick 直接同步 pitch（float 无量化），渲染器改读该值。
     */
    private static final DataParameter<Float> TURRET_PITCH =
            EntityDataManager.createKey(EntityBMPTTurret.class, DataSerializers.FLOAT);

    // ===== 火力参数 =====
    /** 每轮机炮连射发数 */
    public static final int CANNON_SHOTS = 20;
    /** 机炮连射间隔（tick）。2 tick = 0.1s，与机炮开火音效 loop 时长（0.1s）严格同步，循环无缝无断层 */
    public static final int CANNON_DELAY = 2;
    /** 每轮火箭发数 */
    public static final int ROCKET_SHOTS = 2;
    /** 火箭发射间隔（tick） */
    public static final int ROCKET_DELAY = 15;
    /** 机炮每发 RF 消耗 */
    public static final int CANNON_POWER = 10;
    /** 火箭每发 RF 消耗 */
    public static final int ROCKET_POWER = 200;
    /** 开火索敌距离（格） */
    public static final double ATTACK_RANGE = 64.0D;
    /** 开火前水平对准容差（度）：炮塔转向是限速的，转动过程中不开火，避免扫射 */
    public static final float YAW_ALIGN_TOLERANCE = 4.0F;
    /** 开火前俯仰对准容差（度） */
    public static final float PITCH_ALIGN_TOLERANCE = 3.0F;
    /** 最大仰角（度；MC 语义 pitch 正=低头、负=抬头） */
    public static final float PITCH_MAX_UP = 60.0F;
    /** 最大俯角（度） */
    public static final float PITCH_MAX_DOWN = 10.0F;
    /** 机炮弹速（格/tick,CannonShellProjectile 的 speed 参数）——用于提前量计算 */
    public static final double CANNON_SPEED = 1.0D;

    /** 所属炮塔控制器(master)的位置 */
    private BlockPos masterPos = null;
    /** 炮塔类型（对应卡片 NBT turretType） */
    private String turretType = "bmpt";
    /** 机炮是否处于连射中（服务端，用于停止连射时播放结尾音） */
    private boolean cannonFiring = false;

    /**
     * 服务端累积俯仰角（平滑瞄准用）。
     * 1.12.2 的 EntityLookHelper.onUpdateLook() 每 tick 先把 rotationPitch 清零
     * （super.onUpdate() 内部调用），rotationPitch 无法作为跨 tick 累积状态——
     * 若直接对它 approachAngle，每 tick 都从 0 走一步，会恒定卡在 maxDelta(8°)，
     * 与 0.10.23 的 LookHelper 卡死现象（pitch 恒 -8.0）一致。
     * 这里用独立字段累积，每 tick 计算后写回 rotationPitch（弹道/渲染读它）。
     */
    private float turretPitch = 0.0F;

    /**
     * 服务端累积水平角（平滑瞄准用）。
     * 与 turretPitch 同病：1.12.2 的 LookHelper 体系每 tick 把 rotationYawHead 也清零
     * （0.10.27 日志实证：aimYaw 在 -233°~+36° 大幅变化，而 yawHead 恒 ±6.0 =
     * approachAngle(0, aimYaw, 6) 每 tick 只走一步；仅当 |aimYaw|<6 时才跟随）。
     * 这里用独立字段累积，每 tick 计算后写回 rotationYawHead（弹道/渲染/同步读它）。
     */
    private float turretYaw = 0.0F;

    // ===== 客户端帧级补间状态（仅客户端渲染使用；服务端这些字段无意义，保持默认即可）=====
    // MC 服务端转向是 tick 粒度的（rotationYawHead 每 0.05s 跳一次），即使渲染用
    // prev+partialTicks 插值，20tps 的阶梯感依然存在（用户反馈"一卡一卡"）。
    // 玩家转头丝滑是因为鼠标在客户端本地每帧连续驱动，不经网络阶梯；渲染器每帧
    // 以固定角速度把这些显示朝向向服务端同步值逼近，60fps 下每帧都移动，达到玩家
    // 转头式的丝滑。状态放在实体上而不是渲染器上：渲染器实例按实体类共享，
    // 多座炮塔共用同一份状态会互相污染。
    /** 渲染用平滑 yawHead（Float.NaN=尚未初始化，渲染器首帧直接对准同步值） */
    public float renderYawHead = Float.NaN;
    /** 渲染用平滑 pitch（Float.NaN=尚未初始化） */
    public float renderPitch = Float.NaN;
    /** 上一次渲染帧的时间戳（Minecraft.getSystemTime() 毫秒，-1=尚无） */
    public long renderLastTime = -1L;

    public EntityBMPTTurret(World world) {
        super(world);
        this.setSize(1.5F, 2.0F);
        this.noClip = true;
    }

    public EntityBMPTTurret(World world, BlockPos masterPos, EnumFacing facing) {
        this(world);
        this.masterPos = masterPos;
        this.dataManager.set(FACING, facing.getIndex());
        // 关键：必须在 spawn 前设置实体位置。
        // World.spawnEntity 会按实体坐标判断所在 chunk 是否加载，
        // 若位置仍是默认 (0,0,0)，chunk (0,0) 未加载时 spawnEntity 返回 false，实体不会进入世界。
        this.setPosition(masterPos.getX() + 0.5, masterPos.getY() + 1.0625, masterPos.getZ() + 0.5);
        TGCEAddon.getLogger().info("[debug][BMPT实体] 构造 side=" + (world.isRemote ? "client" : "server")
                + " master=" + masterPos + " facing=" + facing.getName()
                + " pos=(" + this.posX + "," + this.posY + "," + this.posZ + ")");
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FACING, EnumFacing.SOUTH.getIndex());
        this.dataManager.register(TURRET_PITCH, 0.0F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100.0D);
        // 炮塔不动：速度 0
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0D);
        // 索敌范围与开火射程匹配
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(ATTACK_RANGE);
    }

    @Override
    protected void initEntityAI() {
        // ===== 索敌（仿照科技枪 NPCTurret）=====
        // 注意：TurretEntityAINearestAttackableTarget 最后一个参数是 yOffset（垂直扩展），
        // 不是索敌半径！索敌半径来自基类 getTargetDistance()（默认 16，且 1.12.2 无 setter），
        // 因此匿名子类覆写 getTargetDistance() 返回开火射程 64，让索敌 AABB 与
        // isSuitableTarget 的距离检查都匹配射程——否则射程外/头顶远目标会被锁定、炮塔空转晃动
        TurretEntityAINearestAttackableTarget<EntityLivingBase> targetAI =
                new TurretEntityAINearestAttackableTarget<EntityLivingBase>(
                this, EntityLivingBase.class, 0, true, false,
                (entity) -> {
                    // 红石信号需求未满足时不索敌（炮塔原地待机）
                    if (!isRedstoneAllowed()) {
                        return false;
                    }
                    // 超过开火射程的目标不锁定（射程外目标打不到，锁定只会导致炮塔空转晃动）
                    if (((EntityLivingBase) entity).getDistanceSq(this) > ATTACK_RANGE * ATTACK_RANGE) {
                        return false;
                    }
                    boolean isTarget = false;
                    // 敌对生物直接打
                    if (entity instanceof IMob) {
                        isTarget = true;
                    } else {
                        TurretBaseTileEntMaster master = getMasterTile();
                        if (master != null) {
                            byte pvp = master.getPvpSetting();
                            UUID owner = master.getOwner();
                            if (entity instanceof EntityPlayer) {
                                UUID plyId = ((EntityPlayer) entity).getGameProfile().getId();
                                if (plyId != null) {
                                    if (!master.isPlayerPlaced()) {
                                        // 自然结构/模组生成的炮塔:视为敌对建筑,攻击一切玩家
                                        isTarget = true;
                                    } else if (owner == null) {
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
                            // 动物按 attackAnimals 设置判断
                            if (!isTarget && master.attackAnimals
                                    && entity instanceof IAnimals && !(entity instanceof EntityBMPTTurret)) {
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
                    // 俯仰角限制：目标在仰角>60° 或俯角>10° 处无法命中，不锁定该目标，
                    // 让炮塔自动转移到近处可打的目标
                    float reqPitch = getRequiredPitch((EntityLivingBase) entity);
                    return reqPitch >= -PITCH_MAX_UP && reqPitch <= PITCH_MAX_DOWN;
                }, 20.0D) {
            // 基类 getTargetDistance() 默认 16 格，远小于开火射程 64：
            // 覆写为 ATTACK_RANGE，索敌 AABB 与 isSuitableTarget 的距离检查都会用它
            @Override
            protected double getTargetDistance() {
                return ATTACK_RANGE;
            }
        };
        this.targetTasks.addTask(2, targetAI);

        // ===== 开火 AI（仿照直升机 AIHelicopterAttack）=====
        this.tasks.addTask(1, new AIBMPTAttack(this));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.world.isRemote) {
            // 客户端：低频存活日志，确认客户端实体存在 + 朝向数据是否同步
            if (this.ticksExisted % 100 == 0) {
                TGCEAddon.getLogger().info("[debug][BMPT实体] 客户端存活 tick=" + this.ticksExisted
                        + " pos=" + this.getPosition() + " facing=" + getTurretFacing().getName()
                        + " target=" + (this.getAttackTarget() != null)
                        + " yawHead=" + this.rotationYawHead + " pitch=" + this.rotationPitch
                        + " yaw=" + this.rotationYaw);
            }
            return;
        }
        // ===== 服务端：跟随 master =====
        TurretBaseTileEntMaster master = getMasterTile();
        if (master == null || !master.isFormed()) {
            TGCEAddon.getLogger().info("[debug][BMPT实体] 服务端移除实体 (master失效) master=" + this.masterPos
                    + " masterTile=" + (master != null));
            this.setDead();
            return;
        }
        BlockPos mp = master.getPos();
        EnumFacing dir = master.getMultiblockDirection();
        // 模型渲染在 master 上方一格中心偏上 1 像素（炮塔底座为 3x3 结构，master 即中心）
        this.setPosition(mp.getX() + 0.5, mp.getY() + 1.0625, mp.getZ() + 0.5);
        // 身体朝向 = 结构朝向（模型渲染用 FACING）；开火朝向(rotationYawHead)由 lookHelper 控制
        this.rotationYaw = yawForFacing(dir);
        if (this.dataManager.get(FACING) != dir.getIndex()) {
            this.dataManager.set(FACING, dir.getIndex());
        }
        // 无目标、能量不足或红石信号需求未满足时：炮塔上部平滑回正（对齐结构朝向、俯仰归零），原地不动
        // 注意：getAttackTarget() 只存在于服务端，客户端不同步，回正也必须在服务端做
        boolean hasPower = master.getEnergyStorage().getEnergyStored() > 0;
        boolean redstoneOk = isRedstoneAllowed();
        EntityLivingBase tgt = this.getAttackTarget();
        if (tgt != null && hasPower && redstoneOk) {
            // 有目标：手动转向（限速逼近目标方向）。转向放这里而不是 AI updateTask，
            // 因为 AI 的 shouldExecute 在能量不足时返回 false 会导致整个 AI 不执行、
            // 炮塔僵在原地；而"只要有目标就持续转头"是预期行为，与开火条件分离。
            // 弃用 LookHelper：实测 1.12.2 的 LookHelper 在 living 实体上 yaw 正常
            // 但 pitch 卡住不动（reqPitch=-34 时 rotationPitch 一直 -8）。
            // 注意 pitch/yawHead 都不能直接用 rotationPitch/rotationYawHead 累积：
            // 1.12.2 的 LookHelper 体系每 tick 清零它们（rotationPitch 由
            // EntityLookHelper.onUpdateLook() 清零，yawHead 同样被外部清零），
            // approachAngle 永远只走一步；用独立字段 turretPitch/turretYaw 累积，
            // 计算完写回 rotationPitch/rotationYawHead（弹道 getMuzzlePos / 渲染 / 同步读它们）。
            float aimYaw = (float) (MathHelper.atan2(tgt.posZ - this.posZ,
                    tgt.posX - this.posX) * 180.0D / Math.PI) - 90.0F;
            Vec3d aim = getAimPoint(tgt);
            aimYaw = (float) (MathHelper.atan2(aim.z - this.posZ, aim.x - this.posX) * 180.0D / Math.PI) - 90.0F;
            this.turretYaw = approachAngle(this.turretYaw, aimYaw, 6.0F);
            this.rotationYawHead = this.turretYaw;
            this.turretPitch = MathHelper.clamp(
                    approachAngle(this.turretPitch, getRequiredPitch(aim), 8.0F),
                    -PITCH_MAX_UP, PITCH_MAX_DOWN);
            this.rotationPitch = this.turretPitch;
        } else {
            this.turretYaw = lerpAngle(this.turretYaw, this.rotationYaw, 0.15F);
            this.rotationYawHead = this.turretYaw;
            this.turretPitch = lerpFloat(this.turretPitch, 0.0F, 0.15F);
            this.rotationPitch = this.turretPitch;
        }
        // 每 tick 同步俯仰角到 dataManager（绕开 HeadLook/Look 交替发送导致的 pitch 严重滞后）
        this.dataManager.set(TURRET_PITCH, this.turretPitch);
        // 服务端日志（每 20 tick≈1 秒）：确认索敌/朝向/能量是否在工作。
        // aimYaw=目标相对方向（转向目标值）。若 aimYaw 大幅变化而 yawHead 恒 6.0/0.0，
        // 说明 yawHead 每 tick 被外部清零（approachAngle 只走一步），需查清零源；
        // 若 aimYaw 与 yawHead 同步变化，说明转向正常。
        if (this.ticksExisted % 20 == 0) {
            EntityLivingBase t2 = this.getAttackTarget();
            float aimYawLog = 0.0F;
            if (t2 != null) {
                aimYawLog = (float) (MathHelper.atan2(t2.posZ - this.posZ,
                        t2.posX - this.posX) * 180.0D / Math.PI) - 90.0F;
            }
            TGCEAddon.getLogger().info("[debug][BMPT实体] server tick=" + this.ticksExisted
                    + " target=" + (t2 != null ? t2.getName() : "null")
                    + " dist=" + (t2 != null ? MathHelper.sqrt(t2.getDistanceSq(this)) : 0)
                    + " aimYaw=" + aimYawLog
                    + " reqPitch=" + (t2 != null ? getRequiredPitch(t2) : 0.0F)
                    + " energy=" + master.getEnergyStorage().getEnergyStored()
                    + " redstone=" + redstoneOk
                    + " yawHead=" + this.rotationYawHead + " pitch=" + this.rotationPitch
                    + " turretYaw=" + this.turretYaw
                    + " yaw=" + this.rotationYaw);
        }
    }

    /**
     * 客户端转向同步修正：
     * MC 的 EntityLivingBase.setPositionAndRotationDirect 收到 SPacketEntityLook 时，
     * 会把 rotationYawHead 覆盖为服务端 rotationYaw（本炮塔的固定结构朝向），
     * 而 rotationYawHead 本应由 SPacketEntityHeadLook 单独同步（指向目标），
     * 两者来回覆盖导致客户端 yawHead 在"目标方向"和"结构朝向"之间跳动（一卡一卡）。
     * 这里复刻 Entity 的实现：只更新位置与 rotationYaw/rotationPitch，不碰 rotationYawHead。
     * 仅客户端会被网络包驱动调用，服务端不受影响。
     */
    @Override
    public void setPositionAndRotationDirect(double x, double y, double z,
                                             float yaw, float pitch,
                                             int posRotationIncrements, boolean teleport) {
        // 位置固定（炮塔服务端位置不变），直接定位即可，无需插值计数
        this.setPosition(x, y, z);
        this.setRotation(yaw, pitch);
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    /**
     * 计算瞄准目标所需的俯仰角（度，负=抬头、正=低头）
     * 公式与 EntityLookHelper.updateLook 完全一致，保证与实际瞄准方向相同
     */
    private float getRequiredPitch(EntityLivingBase target) {
        return getRequiredPitch(new Vec3d(target.posX, target.posY + target.getEyeHeight(), target.posZ));
    }

    /** 计算瞄准给定目标点所需的俯仰角（度，负=抬头、正=低头） */
    private float getRequiredPitch(Vec3d aim) {
        double dx = aim.x - this.posX;
        double dz = aim.z - this.posZ;
        double dy = aim.y - (this.posY + this.getEyeHeight());
        double dist = MathHelper.sqrt(dx * dx + dz * dz);
        return (float) (-(MathHelper.atan2(dy, dist) * 180.0D / Math.PI));
    }

    /**
     * 计算实际瞄准点:
     * - 未安装运算卡片:目标当前位置(中心+眼高)
     * - 安装运算卡片(lead_computing):计算移动目标提前量——
     *   按炮弹飞行时间预测目标未来位置(迭代两次收敛),朝提前量位置开火,
     *   解决机炮对空中移动目标"永远打不中"的问题
     */
    private Vec3d getAimPoint(EntityLivingBase target) {
        TurretBaseTileEntMaster master = getMasterTile();
        if (master == null || !master.hasUpgrade(com.teamytz.tgceaddon.item.ItemTurretUpgrade.TYPE_LEAD_COMPUTING)) {
            return new Vec3d(target.posX, target.posY + target.getEyeHeight(), target.posZ);
        }
        // 预测:目标当前位置 + 目标速度 × 炮弹飞行时间(距离/弹速)
        double t = this.getDistance(target) / CANNON_SPEED;
        Vec3d predicted = new Vec3d(
                target.posX + target.motionX * t,
                target.posY + target.motionY * t + target.getEyeHeight(),
                target.posZ + target.motionZ * t);
        // 二次迭代:用预测点的距离重新计算飞行时间,提高精度
        double t2 = Math.sqrt(predicted.squareDistanceTo(this.posX, this.posY, this.posZ)) / CANNON_SPEED;
        predicted = new Vec3d(
                target.posX + target.motionX * t2,
                target.posY + target.motionY * t2 + target.getEyeHeight(),
                target.posZ + target.motionZ * t2);
        return predicted;
    }

    // ===== 开火 =====
    /**
     * 播放机炮开火音（每发发射时播放一次）
     */
    public void playCannonFireSound() {
        this.world.playSound(null, this.posX, this.posY, this.posZ,
                ModSounds.CANNON_FIRE, SoundCategory.BLOCKS, 5.0F, 0.9F + this.world.rand.nextFloat() * 0.2F);
    }

    /**
     * 播放机炮停止开火结尾音（连射停止时播放一次）
     */
    public void playCannonEndSound() {
        this.world.playSound(null, this.posX, this.posY, this.posZ,
                ModSounds.CANNON_FIRE_END, SoundCategory.BLOCKS, 5.0F, 1.0F);
    }

    /**
     * 播放导弹发射音（直接复用科技枪追踪导弹发射音效）
     */
    public void playRocketFireSound() {
        this.world.playSound(null, this.posX, this.posY, this.posZ,
                TGSounds.GUIDEDMISSILE_FIRE, SoundCategory.BLOCKS, 3.0F, 1.0F);
    }

    /**
     * 计算模型发射点的世界坐标（相对实体位置，随 yawHead 水平旋转 + rotationPitch 俯仰旋转）
     * 模型空间坐标系：以 main 原点为参照，+X=模型右、-Z=模型前方、+Y=上（像素）
     * 变换与渲染器完全一致：
     * T(25px) -> S(1,-1,1) -> R(180-yawHead) -> T(main 14px) -> [T(gun 8px) -> Rx(pitch)] -> 发射点
     *
     * @param mx 模型空间 x（像素，相对 main 原点）
     * @param my 模型空间 y（像素，相对 main 原点）
     * @param mz 模型空间 z（像素，相对 main 原点，-Z=前方）
     * @return 世界坐标 {x, y, z}
     */
    private double[] getMuzzlePos(float mx, float my, float mz) {
        // 1) 俯仰旋转：机炮/左右火箭巢绕旋转中心 (0,8,0)（相对 main 原点）俯仰，
        //    与渲染器 gun/pitchL/pitchR 的 rotateAngleX = radians(rotationPitch) 一致（绕 X 轴，GL 右手系）
        //    用 turretPitch（服务端累积平滑值）；rotationPitch 每 tick 被 lookHelper 清零，不可用
        float pitch = (float) Math.toRadians(this.turretPitch);
        float cosP = MathHelper.cos(pitch);
        float sinP = MathHelper.sin(pitch);
        float dy = my - 8.0F;
        float my2 = dy * cosP - mz * sinP;   // 旋转后相对旋转中心的 y
        float mz2 = dy * sinP + mz * cosP;   // 旋转后相对旋转中心的 z
        // 2) 加回 gun(0,8,0) 与 main(0,14,0) 的平移 -> 相对实体原点的模型坐标
        float my3 = my2 + 22.0F;
        float mz3 = mz2;
        // 3) 水平旋转：渲染器 scale(-1,-1,1) + rotate(yaw - 180)：wx 取负，θ = yaw-180。
        //    用 turretYaw（服务端累积平滑值）；rotationYawHead 每 tick 被 lookHelper 体系清零，不可用
        float dx = mx / 16.0F;
        float dz = mz3 / 16.0F;
        float angle = (float) Math.toRadians(this.turretYaw - 180.0F);
        float cos = MathHelper.cos(angle);
        float sin = MathHelper.sin(angle);
        float wx = -(dx * cos + dz * sin);
        float wz = -dx * sin + dz * cos;
        // 4) 垂直：translate(25px) 在 scale 之前 -> 世界偏移 = (25 - my3)/16 格
        float wy = (25.0F - my3) / 16.0F;
        return new double[]{this.posX + wx, this.posY + wy, this.posZ + wz};
    }

    /**
     * 发射一发机炮（CannonShellProjectile，撞击爆炸不破坏方块），从对应侧的炮管口射出
     * 消耗 RF + 机炮炮弹成功才发射
     *
     * @param sideLeft true=左炮管，false=右炮管（模型坐标：左管 x=-2.5px，右管 x=+2.5px）
     */
    public boolean fireCannon(boolean sideLeft) {
        TurretBaseTileEntMaster master = getMasterTile();
        if (master == null || !master.isFormed()) {
            return false;
        }
        // 先检查弹药再扣能量/弹药(旧逻辑先扣电,无弹药时每 tick 白耗电,同罗兰卡死问题)
        if (!master.hasCannonAmmo()) {
            return false;
        }
        if (!master.consumeTurretPower(CANNON_POWER)) {
            return false;
        }
        if (!master.consumeCannonAmmo()) {
            return false;
        }
        // 弹道方向：GenericProjectile 构造读 shooter.rotationYawHead/rotationPitch 算方向，
        // 而 LookHelper 体系每 tick 已把两者清零（AI 阶段在 super.onUpdate() 内部执行），
        // 构造子弹前写回累积角，保证机炮弹道带正确的水平角/仰俯角
        this.rotationYawHead = this.turretYaw;
        this.rotationPitch = this.turretPitch;
        CannonShellProjectile bullet = new CannonShellProjectile(this.world, this,
                12.0f, 1.0f, 100, 0.05f, 30, 40, 8.0f, 0.25f, false, EnumBulletFirePos.CENTER);
        // 定位到对应炮管口（炮口模型坐标：左 x=-2.5、右 x=+2.5，y=-6，z=-44）
        double[] muzzle = getMuzzlePos(sideLeft ? -2.5f : 2.5f, -6.0f, -44.0f);
        bullet.setPosition(muzzle[0], muzzle[1], muzzle[2]);
        this.world.spawnEntity(bullet);
        // 机炮开火音
        this.playCannonFireSound();
        return true;
    }

    /**
     * 发射一发追踪火箭弹（GuidedMissileProjectile，科技枪追踪导弹），从对应侧的导弹巢口射出
     * 锁定当前攻击目标自动追踪（每 tick 最大转向 9°），消耗 RF + 火箭弹（TGItems.ROCKET）成功才发射
     *
     * @param sideLeft true=左导弹巢，false=右导弹巢（模型坐标：左巢 x≈+17.5px，右巢 x≈-17.75px）
     */
    public boolean fireRocket(boolean sideLeft) {
        TurretBaseTileEntMaster master = getMasterTile();
        if (master == null || !master.isFormed()) {
            return false;
        }
        // 先检查弹药再扣能量/弹药(同 fireCannon)
        if (!master.hasRocketAmmo()) {
            return false;
        }
        if (!master.consumeTurretPower(ROCKET_POWER)) {
            return false;
        }
        if (!master.consumeRocketAmmo()) {
            return false;
        }
        // 同 fireCannon：GenericProjectile 构造读 shooter.rotationYawHead/rotationPitch，写回累积角
        this.rotationYawHead = this.turretYaw;
        this.rotationPitch = this.turretPitch;
        GuidedMissileProjectile rocket = new com.teamytz.tgceaddon.entities.projectiles.BMPTGuidedMissileProjectile(
                this.world, this,
                12.0f, 1.0f, 100, 0.05f, 30, 40, 8.0f, 0.25f, false,
                EnumBulletFirePos.CENTER, 4.0f, this.getAttackTarget());
        // 定位到对应导弹巢口（巢口模型坐标：左巢 x=+17.5、右巢 x=-17.75，y=-5，z=-20.5）
        double[] muzzle = getMuzzlePos(sideLeft ? 17.5f : -17.75f, -5.0f, -20.5f);
        rocket.setPosition(muzzle[0], muzzle[1], muzzle[2]);
        this.world.spawnEntity(rocket);
        // 导弹发射音（科技枪追踪导弹音效）
        this.playRocketFireSound();
        return true;
    }

    /**
     * 结构方向对应的 MC 朝向角（yaw）
     * MC yaw 语义：0=南(+Z)、90=西(-X)、180/-180=北(-Z)、-90=东(+X)
     * 该值作为无目标时 rotationYawHead 的回正目标；渲染器整体旋转角 = yawHead + 180
     */
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

    /**
     * 渲染用俯仰角：dataManager 每 tick 同步的服务端 rotationPitch（连续、无量化）。
     * 不要用 rotationPitch——living 实体的它由 HeadLook/Look 交替包同步，转向中几乎不更新。
     */
    public float getTurretPitch() {
        return this.dataManager.get(TURRET_PITCH);
    }

    /** 角度插值（处理 -180/180 环绕） */
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

    /** 普通线性插值 */
    private static float lerpFloat(float current, float target, float t) {
        return current + (target - current) * t;
    }

    /**
     * 角度限速逼近（处理 -180/180 环绕）：
     * 每 tick 最多向 target 转动 maxDelta 度；与 LookHelper.updateRotation 逻辑一致。
     */
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

    /**
     * 红石信号需求是否满足（master 的红石模式：0=忽略总是工作，1=需要高信号，2=需要低信号）
     * 不满足时炮塔不索敌、不开火、原地回正
     */
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
     * BMPT 开火 AI（仿照科技枪直升机 AIHelicopterAttack）
     * 火力序列：左机炮 20 发 -> 左导弹巢 2 火箭 -> 右机炮 20 发 -> 右导弹巢 2 火箭 -> 回到左，两侧循环切换
     */
    static class AIBMPTAttack extends EntityAIBase {

        private final EntityBMPTTurret turret;
        private int attackTimer = 0;
        /** 当前侧机炮已发射数 */
        private int cannonFired = 0;
        /** 当前侧火箭已发射数 */
        private int rocketFired = 0;
        /** 当前侧（true=左，false=右；每完成一侧整轮后切换） */
        private boolean sideLeft = true;

        public AIBMPTAttack(EntityBMPTTurret turret) {
            this.turret = turret;
            // mutex=0:防止被其它 mod(DynamicStealth 等)注入的常驻任务互斥拦截无法开火
            this.setMutexBits(0);
        }

        @Override
        public boolean shouldExecute() {
            if (this.turret.getAttackTarget() == null) {
                return false;
            }
            // 能量不足或红石信号需求未满足时不开火（炮塔原地不动）
            TurretBaseTileEntMaster master = this.turret.getMasterTile();
            return master != null && master.getEnergyStorage().getEnergyStored() > 0
                    && this.turret.isRedstoneAllowed();
        }

        @Override
        public void startExecuting() {
            this.attackTimer = 0;
            this.cannonFired = 0;
            this.rocketFired = 0;
            this.sideLeft = true;
        }

        @Override
        public void resetTask() {
            // AI 停止（目标消失/能量不足等）：若正在机炮连射，播放结尾音
            if (this.turret.cannonFiring) {
                this.turret.playCannonEndSound();
                this.turret.cannonFiring = false;
            }
            this.attackTimer = 0;
        }

        @Override
        public void updateTask() {
            EntityLivingBase target = this.turret.getAttackTarget();
            if (target == null) {
                return;
            }
            // 转向（限速逼近目标方向）已统一放在 onUpdate 服务端分支执行，
            // 与 AI 生命周期解耦：AI 的 shouldExecute 在能量不足时返回 false
            // 会导致整个 AI 不执行，若转向放在 updateTask 炮塔就会僵在原地。
            // 这里只处理开火判定与火力输出。

            // 俯仰角限制导致无法命中目标（瞄准所需仰角>60° 或俯角>10°）：停火，但继续转头
            // 用实际瞄准点(含提前量)判断,与开火方向一致
            Vec3d aim = this.turret.getAimPoint(target);
            float requiredPitch = this.turret.getRequiredPitch(aim);
            if (requiredPitch < -PITCH_MAX_UP || requiredPitch > PITCH_MAX_DOWN) {
                resetFireState();
                return;
            }
            // 炮口对准检查：炮塔转向是限速的（水平 6°/tick），转动过程中不开火，
            // 否则弹道会随炮塔扫出去（扫射）。水平/俯仰都进入容差后才允许开火
            float targetYaw = (float) (MathHelper.atan2(aim.z - this.turret.posZ, aim.x - this.turret.posX) * 180.0D / Math.PI) - 90.0F;
            // 对准检查用 turret.turretPitch/turret.turretYaw（服务端累积值）；
            // rotationPitch/rotationYawHead 每 tick 被 lookHelper 体系清零
            if (Math.abs(MathHelper.wrapDegrees(this.turret.turretYaw - targetYaw)) > YAW_ALIGN_TOLERANCE
                    || Math.abs(MathHelper.wrapDegrees(this.turret.turretPitch - requiredPitch)) > PITCH_ALIGN_TOLERANCE) {
                resetFireState();
                return;
            }
            // 目标被锁定后跑出射程：直接放弃目标（重新索敌选更近的），
            // 避免炮塔空转跟随一个打不到的远方目标来回晃动。
            // 目标被墙挡住（不可见）：停火但保留目标继续转头，绕墙后恢复
            if (target.getDistanceSq(this.turret) > ATTACK_RANGE * ATTACK_RANGE) {
                this.turret.setAttackTarget(null);
                resetFireState();
                return;
            }
            if (!this.turret.canEntityBeSeen(target)) {
                resetFireState();
                return;
            }

            // 机炮连射状态边沿检测：机炮阶段（cannonFired<20）视为连射中，
            // 阶段切换/停火时若之前正在连射，播放机炮结尾音
            boolean nowCannonFiring = this.cannonFired < CANNON_SHOTS;
            if (this.turret.cannonFiring && !nowCannonFiring) {
                this.turret.playCannonEndSound();
            }
            this.turret.cannonFiring = nowCannonFiring;

            ++this.attackTimer;

            if (this.cannonFired < CANNON_SHOTS) {
                // ===== 当前侧机炮阶段 =====
                if (this.attackTimer % CANNON_DELAY == 0 && this.turret.fireCannon(this.sideLeft)) {
                    ++this.cannonFired;
                }
            } else if (this.rocketFired < ROCKET_SHOTS) {
                // ===== 当前侧导弹巢阶段 =====
                if (this.attackTimer % ROCKET_DELAY == 0 && this.turret.fireRocket(this.sideLeft)) {
                    ++this.rocketFired;
                }
            } else {
                // ===== 当前侧整轮完成：切换到另一侧 =====
                this.cannonFired = 0;
                this.rocketFired = 0;
                this.attackTimer = 0;
                this.sideLeft = !this.sideLeft;
            }
        }

        /** 重置当前侧火力计数（停火状态，转头不受影响）；若正在机炮连射，播放结尾音 */
        private void resetFireState() {
            if (this.turret.cannonFiring) {
                this.turret.playCannonEndSound();
                this.turret.cannonFiring = false;
            }
            this.attackTimer = 0;
            this.cannonFired = 0;
            this.rocketFired = 0;
        }
    }
}
