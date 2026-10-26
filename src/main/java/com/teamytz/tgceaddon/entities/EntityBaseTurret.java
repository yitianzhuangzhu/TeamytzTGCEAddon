package com.teamytz.tgceaddon.entities;

import com.teamytz.tgceaddon.TGCEAddon;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import techguns.factions.TGNpcFactions;

import java.util.List;
import java.util.UUID;

/**
 * 炮塔实体抽象基类（0.21.0 新增，供「一批新炮塔」共用；0.22.0 加入战斗框架）。
 *
 * 基类负责与基座耦合的公共部分 + 通用战斗框架，**具体武器由子类实现**：
 * - 每 tick 跟随所属基座 master 的位置与结构朝向，master 失效即自毁
 * - 卡片类型标识（{@link ITurretEntity#getTurretType()}）与基座派发对接
 * - 索敌（沿科技枪 {@code TurretEntityAINearestAttackableTarget} 的范式，过滤链与 BMPT 一致）
 * - 水平转向（限速逼近目标；无目标回正到结构方向）
 * - 开火节流、瞄准对准检查、视线检查、弹药按发数扣除、能量/红石门控
 * - 炮口世界坐标换算（复刻渲染器变换，含镜像；见 {@link #muzzlePos}）
 *
 * 子类只需实现：{@link #getAttackRange()}、{@link #getFireCooldown()}、{@link #tryFire}，
 * 以及各自的炮口常量。
 *
 * 与既有炮塔的关系：BMPT / 罗兰是各自独立实现的（行为已标定），本类只服务新炮塔。
 */
public abstract class EntityBaseTurret extends EntityCreature implements ITurretEntity {

    /**
     * 渲染器上移像素数。**必须与 ClientProxy 注册的渲染器传入值一致**。
     * 25 = 模型最高点(24) + 1：配合实体锚点 +17/16，模型底部落在 master 上方 1.125 格。
     */
    public static final float RENDER_TRANSLATE_PX = 25.0F;

    private static final DataParameter<Integer> FACING =
            EntityDataManager.createKey(EntityBaseTurret.class, DataSerializers.VARINT);

    /**
     * 俯仰角同步参数。living 实体的 rotationPitch 由 SPacketEntityLook 同步，而 EntityTrackerEntry
     * 对 living 是「HeadLook / Look 交替」发送，转向中 head 每 2 tick 都在变 → pitch 包几乎不发。
     * 用 dataManager 每 tick 同步 float，渲染器读该值。
     */
    private static final DataParameter<Float> TURRET_PITCH =
            EntityDataManager.createKey(EntityBaseTurret.class, DataSerializers.FLOAT);

    /** 开火前水平对准容差（度）：转向是限速的，转动过程中不开火，避免光束扫出去 */
    public static final float YAW_ALIGN_TOLERANCE = 5.0F;

    /** 开火前俯仰对准容差（度） */
    public static final float PITCH_ALIGN_TOLERANCE = 5.0F;

    /**
     * 炮塔调试日志开关：true = 每 {@link #HEARTBEAT_INTERVAL} tick 打一行状态
     * （AI 是否注册 / 目标 / 附近生物 / 能量 / 红石 / canFire / 弹药）。
     * 排查"炮塔不索敌/不开火"时保持 true，稳定后可改 false。
     */
    public static final boolean DEBUG_TURRET = true;

    /** 心跳日志间隔（tick）：100 = 5 秒一行（以前是 400 tick/20 秒，短时间试炮等不到日志） */
    public static final int HEARTBEAT_INTERVAL = 100;

    /** 所属炮塔控制器(master)的位置 */
    protected BlockPos masterPos = null;
    /** 炮塔类型（必须与 TurretRegistry 注册的卡片类型、ItemTurretCard 的 NBT 一致） */
    protected final String turretType;

    /** 服务端累积俯仰角（度，正=低头）。LookHelper 每 tick 清零 rotationPitch，故必须独立累积 */
    protected float turretPitch = 0.0F;
    /** 服务端累积水平角（度）。同理不能直接用 rotationYawHead 做跨 tick 累积 */
    protected float turretYaw = 0.0F;

    /** 距上次"扣除一个弹药"以来已射击的发数（配合子类的每 N 发消耗一个） */
    protected int ammoShotCounter = 0;

    /** 索敌扫描间隔（tick）：与罗兰一致，5 tick 一扫，快速捕捉高速目标 */
    public static final int SCAN_INTERVAL = 5;

    /**
     * 目标被遮挡多久后放弃（tick）。与原版 {@code EntityAITarget.unseenMemoryTicks} 默认值一致（3 秒）。
     *
     * BMPT 走原版目标 AI：新目标必须**看得见**（checkSight=true），已有目标看不见超过这个时间就丢弃、
     * 转而搜索能打的目标。本基类的自研扫描必须自己实现这两条，否则会死抱一个打不到的目标，
     * 新敌人出现也不切换（实测症状）。
     */
    public static final int UNSEEN_MEMORY_TICKS = 60;

    /** 炮口/视线被方块持续遮挡多少 tick 后放弃该目标（换更近的可打目标） */
    public static final int BLOCKED_GIVEUP_TICKS = 40;

    /**
     * 炮塔自己的目标引用（服务端，**权威来源**）。
     *
     * 为什么不直接用 {@code getAttackTarget()}：部分整合包 mod（实测 DynamicStealth）会在实体入世界时
     * 注入自己的最高优先级 AI，并在其逻辑里调用 {@code clearAttackTargetAndCancelBadTasks()}，
     * 把原版攻击目标清空（实测每 5~6 tick 清一次）——于是炮塔"锁定→丢失"反复横跳，
     * 开火 AI 永远拿不到目标（现象就是完全不索敌不开火）。
     *
     * 炮塔是机器，不受那套潜行/威胁系统管辖，所以以本字段为准。
     * {@code setAttackTarget(...)} 仍会同步调用，保证其它系统（HUD/仇恨/击杀归属）正常工作。
     * 另一层保险：DynamicStealth 的配置里有 "Entity-Specific Full Bypass" 名单
     * （BMPT/罗兰早已在名单内），新炮塔也应加入，见 TURRET_GUIDE.md §13。
     */
    protected EntityLivingBase turretTarget = null;

    /**
     * initEntityAI() 是否已执行（诊断用：确认 AI 真的注册到了这个实例上）。
     * **不要给本字段写初始值**：initEntityAI() 是在 super() 期间（EntityLiving 构造函数末尾）被调用的，
     * 而实例字段初始化器在 super() 之后才执行 —— 写 `= false` 会把 initEntityAI 里的赋值冲掉
     * （0.25.0 实测日志里一直显示 AI已注册=false 就是这个原因）。
     */
    protected boolean aiReady;

    // ===== 客户端帧级补间状态（状态放实体上，渲染器实例按实体类共享，多座炮塔会串味）=====
    public float renderYawHead = Float.NaN;
    public float renderPitch = Float.NaN;
    public long renderLastTime = -1L;

    protected EntityBaseTurret(World world, String turretType) {
        super(world);
        this.turretType = turretType;
        this.setSize(1.5F, 2.0F);
        this.noClip = true;
    }

    protected EntityBaseTurret(World world, BlockPos masterPos, EnumFacing facing, String turretType) {
        this(world, turretType);
        this.masterPos = masterPos;
        this.dataManager.set(FACING, facing.getIndex());
        // 必须在 spawn 前定位：World.spawnEntity 会按坐标判断 chunk 是否加载，
        // 若还在默认 (0,0,0) 而 chunk(0,0) 未加载，spawnEntity 会返回 false，实体进不了世界。
        this.setPosition(masterPos.getX() + 0.5D, masterPos.getY() + getAnchorHeight(), masterPos.getZ() + 0.5D);
        this.rotationYaw = yawForFacing(facing);
        this.rotationYawHead = this.rotationYaw;
        this.turretYaw = this.rotationYaw;
    }

    // ==================================================================
    // 子类必须/可覆写
    // ==================================================================

    /** 索敌与开火距离（格） */
    protected abstract double getAttackRange();

    /** 开火间隔（tick） */
    protected abstract int getFireCooldown();

    /**
     * 实际开火。返回 true 表示本次射击成功（会进入冷却）。
     * 子类在此检查/扣除弹药并生成弹丸，不要在这里做转向或目标判定。
     */
    protected abstract boolean tryFire(EntityLivingBase target);

    /** 实体在 master 上方的高度偏移（格）。1.0625 = 17/16，与 BMPT 相同 */
    protected double getAnchorHeight() {
        return 1.0625D;
    }

    /** 水平转向速度（度/tick） */
    protected float getYawTurnSpeed() {
        return 6.0F;
    }

    /** 俯仰转向速度（度/tick） */
    protected float getPitchTurnSpeed() {
        return 8.0F;
    }

    /**
     * 最大仰角（度，正值；MC 语义 pitch 为负表示抬头，故实际限制区间是 [-俯仰上限, +俯角上限]）。
     * 由用户按模型给出的俯仰数据设定。想"不设限制"就返回 90。
     */
    protected float getPitchMaxUp() {
        return 90.0F;
    }

    /** 最大俯角（度，正值） */
    protected float getPitchMaxDown() {
        return 90.0F;
    }

    /**
     * 索敌俯仰筛选用的上限/下限（度）。
     *
     * <p>默认与炮塔俯仰限幅相同。**多武器炮塔**（例如 M1 的机枪仰角 50°、主炮只有 15°）
     * 应覆写成副武器的范围，否则主炮打不到的高空目标根本不会被锁定，副武器等于没有。</p>
     */
    protected float getTargetPitchMaxUp() {
        return getPitchMaxUp();
    }

    protected float getTargetPitchMaxDown() {
        return getPitchMaxDown();
    }

    /**
     * 模型俯仰符号（默认 +1）。
     *
     * 当俯仰分组自身带 180° 组旋转时（例如模型方向建反了、作者把整组绕 Y 转 180° 修正），
     * MC 的 `Rz*Ry*Rx` 复合会让该分组的俯仰方向反掉：此时必须返回 **-1**。
     * `bbmodel2java.js` 会在生成的模型类头部提示是否需要返回 -1
     * （判据：俯仰分组的组旋转把局部 X 轴翻向 -X）。
     *
     * 该符号同时用于模型动画（RenderBaseTurret 传入的俯仰角）与炮口坐标换算（muzzlePos），
     * 两者必须一致，否则炮口会与模型倾斜方向脱节。
     * 渲染器在另一个包，故为 public。
     */
    public float getModelPitchSign() {
        return 1.0F;
    }

    @Override
    protected void initEntityAI() {
        // ===== 索敌：自研雷达扫描（与罗兰同一范式，mutex=0）=====
        // **不要改回原版 EntityAINearestAttackableTarget / 科技枪 TurretEntityAINearestAttackableTarget**：
        // 那条路走 EntityPredicate 且互斥位=1，在装了 DynamicStealth 这类"往实体 tasks 注入最高
        // 优先级常驻任务"的整合包里会被永久拦截 —— 现象就是炮塔完全不索敌。
        // 罗兰一直是自研扫描（mutex=0）所以正常；本基类原先照 BMPT 用原版 AI，现已统一为自研扫描。
        this.targetTasks.addTask(2, new TurretTargetSearchAI());

        // ===== 开火（同样 mutex=0）=====
        this.tasks.addTask(1, new TurretAttackAI());

        this.aiReady = true;
    }

    /**
     * 雷达扫描索敌 AI（与罗兰 AIRolandTargetSearch 同范式）：
     * 每 {@link #SCAN_INTERVAL} tick 扫描一次周围的 EntityLivingBase，取最近的有效目标。
     * 自研扫描的好处：不依赖原版 EntityPredicate 与互斥位，整合包里不会被其它 mod 的常驻任务挡住。
     */
    protected class TurretTargetSearchAI extends EntityAIBase {

        private int scanTimer = 0;
        /** 当前目标连续看不见的 tick 数（超过 UNSEEN_MEMORY_TICKS 就放弃换目标） */
        private int unseenTicks = 0;

        public TurretTargetSearchAI() {
            // mutex=0：见 initEntityAI 注释
            this.setMutexBits(0);
        }

        @Override
        public boolean shouldExecute() {
            return true;
        }

        @Override
        public void updateTask() {
            // 没电/红石不允许时：不索敌、不开火，直接清空目标（转向会在 onUpdate 里回正）
            if (!canFire()) {
                if (EntityBaseTurret.this.turretTarget != null) {
                    EntityBaseTurret.this.turretTarget = null;
                    setAttackTarget(null);
                }
                this.unseenTicks = 0;
                return;
            }
            if (this.scanTimer > 0) {
                --this.scanTimer;
                return;
            }
            this.scanTimer = SCAN_INTERVAL;

            // 已有目标：先看是否仍然合格（射程/俯仰/目标类型/红石），再看能不能看见。
            // 与原版 EntityAITarget 一致：短暂遮挡先留着（绕墙后会恢复），超过记忆窗口就放弃、重新索敌。
            EntityLivingBase current = EntityBaseTurret.this.turretTarget;
            if (current != null && !current.isDead && isSuitableTarget(current)) {
                if (canEntityBeSeen(current)) {
                    this.unseenTicks = 0;
                    return;                       // 看得见 → 维持锁定
                }
                if (++this.unseenTicks <= UNSEEN_MEMORY_TICKS) {
                    return;                       // 短暂被挡：先留着
                }
                this.unseenTicks = 0;             // 长时间看不见 → 放弃，换能打的目标
            } else {
                this.unseenTicks = 0;
            }
            scanForTarget();
        }
    }

    /**
     * 扫描并锁定最近的有效目标（无有效目标则清空）。
     * **新目标必须是看得见（视线可达）的**，与原版目标 AI 的 checkSight 行为一致 ——
     * 否则会锁定墙后的目标、既不攻击也不换目标。俯仰够不着的目标由 isSuitableTarget 排除。
     */
    protected void scanForTarget() {
        double range = getAttackRange();
        if (range <= 0.0D) {
            if (this.turretTarget != null) {
                this.turretTarget = null;
                setAttackTarget(null);
            }
            return;
        }
        AxisAlignedBB box = new AxisAlignedBB(
                this.posX - range, this.posY - range, this.posZ - range,
                this.posX + range, this.posY + range, this.posZ + range);
        List<EntityLivingBase> list = this.world.getEntitiesWithinAABB(EntityLivingBase.class, box,
                e -> e != null && e.isEntityAlive() && isSuitableTarget(e) && canEntityBeSeen(e));
        EntityLivingBase best = null;
        double bestDist = Double.MAX_VALUE;
        for (EntityLivingBase e : list) {
            double d = e.getDistanceSq(this);
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        this.turretTarget = best;
        // 同步给原版字段（HUD/仇恨/击杀归属等仍能看到目标）；变化时由 setAttackTarget 打日志
        setAttackTarget(best);
    }

    // ==================================================================
    // 生命周期
    // ==================================================================

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.world.isRemote) {
            return;
        }
        TurretBaseTileEntMaster master = getMasterTile();
        if (master == null || !master.isFormed()) {
            this.turretTarget = null;
            this.setDead();
            return;
        }
        BlockPos mp = master.getPos();
        EnumFacing dir = master.getMultiblockDirection();
        this.setPosition(mp.getX() + 0.5D, mp.getY() + getAnchorHeight(), mp.getZ() + 0.5D);
        if (this.dataManager.get(FACING) != dir.getIndex()) {
            this.dataManager.set(FACING, dir.getIndex());
        }

        // ===== 转向：有目标且可开火时限速逼近瞄准方向，否则回正到结构方向 =====
        // 放在 onUpdate 而不是 AI 里：AI 的 shouldExecute 在能量不足时会返回 false，
        // 若把转向放进去，炮塔会僵在原地（BMPT 踩过的坑）。
        EntityLivingBase target = this.turretTarget;
        // 转向：**有目标且可开火**（能量>0 + 红石允许）时才跟随；否则回正到结构方向。
        // 没电时炮塔完全静止、不索敌、不开火（用户要求：没电不执行攻击行为）。
        if (target != null && canFire()) {
            float aimYaw = aimYawTo(target);
            this.turretYaw = approachAngle(this.turretYaw, aimYaw, getYawTurnSpeed());
            float aimPitch = aimPitchTo(aimPointOf(target));
            // 俯仰限幅：模型只能转到用户给的角度范围（仰角 -> 负 pitch，俯角 -> 正 pitch）
            this.turretPitch = MathHelper.clamp(
                    approachAngle(this.turretPitch, aimPitch, getPitchTurnSpeed()),
                    -getPitchMaxUp(), getPitchMaxDown());
        } else {
            this.turretYaw = lerpAngle(this.turretYaw, yawForFacing(dir), 0.15F);
            this.turretPitch = lerpFloat(this.turretPitch, 0.0F, 0.15F);
        }
        this.rotationYaw = yawForFacing(dir);
        this.rotationYawHead = this.turretYaw;
        this.rotationPitch = this.turretPitch;
        this.dataManager.set(TURRET_PITCH, this.turretPitch);

        // 心跳日志：确认实体在跑、AI 是否注册、有没有索敌，以及是否被能量/红石/弹药门控挡住。
        // 间隔 100 tick（5 秒）——以前是 400 tick（20 秒），短时间试炮（挂上卡片看两眼）根本等不到一行，
        // 排查时日志里什么都没有；现在挂上 5 秒内必有输出。开服 1 秒（tick=20）额外打一行，
        // 便于确认"实体确实起来了"。
        if (DEBUG_TURRET && (this.ticksExisted == 20 || this.ticksExisted % HEARTBEAT_INTERVAL == 0)) {
            double range = getAttackRange();
            int near = 0, pass = 0;
            if (range > 0.0D) {
                AxisAlignedBB box = new AxisAlignedBB(
                        this.posX - range, this.posY - range, this.posZ - range,
                        this.posX + range, this.posY + range, this.posZ + range);
                List<EntityLivingBase> all = this.world.getEntitiesWithinAABB(EntityLivingBase.class, box);
                near = all.size();
                for (EntityLivingBase e : all) {
                    if (isSuitableTarget(e)) {
                        pass++;
                    }
                }
            }
            TGCEAddon.getLogger().info("[debug][炮塔] " + this.turretType
                    + " tick=" + this.ticksExisted
                    + " AI已注册=" + this.aiReady
                    + " 目标=" + (target != null ? target.getName() : "无")
                    + " 附近生物=" + near + " 通过筛选=" + pass
                    + " 能量=" + master.getEnergyStorage().getEnergyStored()
                    + " 已成形=" + master.isFormed()
                    + " 红石允许=" + isRedstoneAllowed()
                    + " canFire=" + canFire()
                    + " 射程=" + range
                    + describeAmmo());
        }
    }

    /**
     * 心跳日志里的弹药描述（默认空）。带弹药的炮塔覆写它，
     * 让"为什么不开火"一眼可见——没有弹药是**堵在子类 tryFire 里的**，
     * 基类的 canFire() 看不到这一层。
     */
    protected String describeAmmo() {
        return "";
    }

    /**
     * 是否满足开火前置条件：红石需求满足 + 基座有能量（与 BMPT/罗兰 同口径）。
     * 具体每次射击扣多少 RF 由子类的 tryFire 用 {@code master.consumeTurretPower(n)} 负责。
     */
    protected boolean canFire() {
        TurretBaseTileEntMaster master = getMasterTile();
        return master != null && master.isFormed()
                && master.getEnergyStorage().getEnergyStored() > 0
                && isRedstoneAllowed();
    }

    /**
     * 索敌状态变化时打一条日志（便于确认"炮塔到底有没有在索敌"）。
     * 只在目标变化时输出，不刷屏；附带开火前置条件的状态，一次测试即可定位是
     * 没索敌、还是被能量/红石门控挡住。
     */
    @Override
    public void setAttackTarget(EntityLivingBase target) {
        EntityLivingBase old = this.getAttackTarget();
        super.setAttackTarget(target);
        if (!this.world.isRemote && old != target) {
            TurretBaseTileEntMaster master = getMasterTile();
            if (target == null) {
                TGCEAddon.getLogger().info("[debug][炮塔] " + this.turretType + " 失去目标 (master@"
                        + this.masterPos + ")");
            } else {
                TGCEAddon.getLogger().info("[debug][炮塔] " + this.turretType + " 锁定目标 "
                        + target.getName() + " 距离="
                        + String.format("%.1f", MathHelper.sqrt(target.getDistanceSq(this)))
                        + " 能量=" + (master != null ? master.getEnergyStorage().getEnergyStored() : -1)
                        + " 已成形=" + (master != null && master.isFormed())
                        + " 红石允许=" + isRedstoneAllowed()
                        + " canFire=" + canFire());
            }
        }
    }

    // ==================================================================
    // 索敌过滤（与 BMPT 同口径）
    // ==================================================================

    protected boolean isSuitableTarget(EntityLivingBase entity) {
        if (entity == null || !entity.isEntityAlive()) {
            return false;
        }
        // 不攻击其他炮塔
        if (entity instanceof ITurretEntity) {
            return false;
        }
        if (!isRedstoneAllowed()) {
            return false;
        }
        double range = getAttackRange();
        if (entity.getDistanceSq(this) > range * range) {
            return false;
        }
        // 俯仰够不着的目标不锁定：锁定只会让炮塔空转抖动（同 BMPT）
        float reqPitch = aimPitchTo(aimPointOf(entity));
        if (reqPitch < -getTargetPitchMaxUp() || reqPitch > getTargetPitchMaxDown()) {
            return false;
        }
        if (entity instanceof IMob) {
            return true;
        }
        TurretBaseTileEntMaster master = getMasterTile();
        if (master == null) {
            return false;
        }
        // 玩家：自然结构/模组生成的炮塔视为敌对建筑打一切玩家；玩家放置的按 PVP 设置与所有者
        if (entity instanceof EntityPlayer) {
            UUID plyId = ((EntityPlayer) entity).getGameProfile().getId();
            if (plyId == null) {
                return false;
            }
            if (!master.isPlayerPlaced()) {
                return true;
            }
            UUID owner = master.getOwner();
            if (owner == null) {
                return true;
            }
            byte pvp = master.getPvpSetting();
            if (pvp == 0) {
                return false;
            }
            return !owner.equals(plyId) && TGNpcFactions.shouldAttack(owner, plyId, pvp);
        }
        // 动物：按 GUI 的"攻击动物"开关
        if (master.attackAnimals && entity instanceof IAnimals) {
            if (entity instanceof EntityTameable) {
                return !((EntityTameable) entity).isTamed();
            }
            if (entity instanceof EntityHorse) {
                return !((EntityHorse) entity).isTame();
            }
            if (entity instanceof IEntityOwnable) {
                return ((IEntityOwnable) entity).getOwner() == null;
            }
            return true;
        }
        return false;
    }

    // ==================================================================
    // 瞄准与炮口
    // ==================================================================

    /** 目标瞄准点（中心 + 半眼高，与科技枪弹道取向一致） */
    protected Vec3d aimPointOf(EntityLivingBase target) {
        return new Vec3d(target.posX, target.posY + target.getEyeHeight() * 0.5D, target.posZ);
    }

    /** 瞄准给定点所需的水平角（度，MC yaw 语义） */
    protected float aimYawTo(Vec3d aim) {
        return (float) (MathHelper.atan2(aim.z - this.posZ, aim.x - this.posX) * 180.0D / Math.PI) - 90.0F;
    }

    protected float aimYawTo(EntityLivingBase target) {
        return aimYawTo(aimPointOf(target));
    }

    /** 瞄准给定点所需的俯仰角（度，负=抬头、正=低头；与 EntityLookHelper.updateLook 同式） */
    protected float aimPitchTo(Vec3d aim) {
        double dx = aim.x - this.posX;
        double dz = aim.z - this.posZ;
        double dy = aim.y - (this.posY + this.getEyeHeight());
        double dist = MathHelper.sqrt(dx * dx + dz * dz);
        return (float) (-(MathHelper.atan2(dy, dist) * 180.0D / Math.PI));
    }

    /**
     * 从**任意世界点**瞄准目标点所需的 {yaw, pitch}（度）。
     *
     * 开火必须用它逐炮口计算，不能用炮塔当前朝向（turretYaw/turretPitch）：
     * 朝向允许 {@link #YAW_ALIGN_TOLERANCE} 度的对准误差，且多管炮塔的炮口相距 1~2 格，
     * 若各管平行射出（都用同一个朝向），每根管子都会整体偏出目标 —— 表现为"命中率低"。
     */
    protected static float[] aimAnglesFrom(double fromX, double fromY, double fromZ, Vec3d aim) {
        double dx = aim.x - fromX;
        double dy = aim.y - fromY;
        double dz = aim.z - fromZ;
        float yaw = (float) (MathHelper.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(MathHelper.atan2(dy, MathHelper.sqrt(dx * dx + dz * dz)) * 180.0D / Math.PI));
        return new float[]{yaw, pitch};
    }

    /** 开火诊断（受 DEBUG_TURRET 控制、每 10 tick 最多一行）：炮口→目标距离 vs 实际弹道距离，用于判断是否命中 */
    protected void logShot(EntityLivingBase target, Vec3d aim, double[] muzzle, double beamDistance) {
        if (!DEBUG_TURRET || this.ticksExisted % 10 != 0) {
            return;
        }
        double dx = aim.x - muzzle[0];
        double dy = aim.y - muzzle[1];
        double dz = aim.z - muzzle[2];
        double muzzleToTarget = Math.sqrt(dx * dx + dy * dy + dz * dz);
        boolean hit = beamDistance >= muzzleToTarget - 0.75D;   // 弹道长度接近"到目标"即为命中（否则是打在方块上）
        TGCEAddon.getLogger().info("[debug][炮塔] " + this.turretType + " 开火 目标=" + target.getName()
                + " 目标距离=" + String.format("%.1f", Math.sqrt(target.getDistanceSq(this)))
                + " 炮口→目标=" + String.format("%.2f", muzzleToTarget)
                + " 弹道=" + String.format("%.2f", beamDistance)
                + " 命中=" + (hit ? "是" : "否"));
    }

    /**
     * 模型像素坐标 → 世界坐标。**必须复刻渲染器的变换**，否则光束不会从模型炮口出来：
     * 渲染器 = translate(实体) → translate(0, RENDER_TRANSLATE_PX/16, 0) → scale(-1,-1,1) → rotate(yaw-180)
     * 与 EntityRolandTurret.getMuzzlePos 同式（该实现是权威参照）。
     *
     * @param muzzlePx 炮口在 **Java 模型空间**的像素坐标（= bbmodel 坐标经 A(x,y,z)=(-x,24-y,z) 变换）
     * @param pivotPx  俯仰旋转中心（同为 Java 模型空间像素；俯仰角为 0 时不影响结果）
     */
    protected double[] muzzlePos(float[] muzzlePx, float[] pivotPx) {
        float p = (float) Math.toRadians(this.turretPitch * getModelPitchSign());
        float cosP = MathHelper.cos(p);
        float sinP = MathHelper.sin(p);
        float lx = pivotPx[0], ly = pivotPx[1], lz = pivotPx[2];
        float mx = muzzlePx[0], my = muzzlePx[1], mz = muzzlePx[2];
        float dy = my - ly;
        float dz = mz - lz;
        float y3 = ly + (dy * cosP - dz * sinP);
        float z3 = lz + (dy * sinP + dz * cosP);

        float angle = (float) Math.toRadians(this.turretYaw - 180.0F);
        float cos = MathHelper.cos(angle);
        float sin = MathHelper.sin(angle);
        double wx = -((mx / 16.0D) * cos + (z3 / 16.0D) * sin);
        double wz = -((mx / 16.0D) * sin - (z3 / 16.0D) * cos);
        double wy = (RENDER_TRANSLATE_PX - y3) / 16.0D;
        return new double[]{this.posX + wx, this.posY + wy, this.posZ + wz};
    }

    /**
     * 把实体临时摆到指定世界点并同步朝向，供"以射手为基准"的科技枪弹丸构造器使用
     * （科技枪激光/特斯拉弹**在构造时就 raycast 完伤害**，起始点事后无法修正）。
     *
     * 科技枪起始点 = (shooter.x, shooter.y + eyeHeight - 0.1, shooter.z)，故补偿量 = -eyeHeight + 0.1。
     * 用完必须调用 {@link #restoreFromMuzzle} 复位，否则实体本 tick 的位置会错。
     */
    protected void alignToMuzzleForSpawn(double[] muzzle, float aimYaw, float aimPitch) {
        this.setPosition(muzzle[0], muzzle[1] - this.getEyeHeight() + 0.1D, muzzle[2]);
        this.rotationYawHead = aimYaw;
        this.rotationPitch = aimPitch;
    }

    /** 与 {@link #alignToMuzzleForSpawn} 配对的位置复位 */
    protected void restoreFromMuzzle(double x, double y, double z) {
        this.setPosition(x, y, z);
    }

    // ==================================================================
    // 弹药（按发数扣除：每 N 发消耗一个）
    // ==================================================================

    /**
     * 每 shotsPerItem 发消耗一个指定弹药（当前无炮塔使用；新炮塔若要弹药可直接调用）。
     *
     * @return false 表示"该扣弹药但槽里没有" → 本次不开火；true 表示可以开火
     */
    protected boolean consumeAmmoEvery(int shotsPerItem, ItemStack template) {
        TurretBaseTileEntMaster master = getMasterTile();
        if (master == null) {
            return false;
        }
        if (this.ammoShotCounter + 1 >= shotsPerItem) {
            if (!master.hasAmmo(template)) {
                return false;
            }
            if (!master.consumeAmmo(template)) {
                return false;
            }
            this.ammoShotCounter = 0;
            return true;
        }
        this.ammoShotCounter++;
        return true;
    }

    // ==================================================================
    // 开火 AI
    // ==================================================================

    /** 通用开火 AI：节流 + 对准检查 + 视线检查，实际弹丸生成交给 {@link #tryFire} */
    protected class TurretAttackAI extends EntityAIBase {

        private int cooldown = 0;
        /** 当前目标弹道被方块持续遮挡的 tick 数（超过阈值就放弃该目标） */
        private int blockedTicks = 0;
        /** 已经"对准并可见"连续保持的 tick 数：满 {@link #getAimSettleTicks()} 才开火 */
        private int aimSettleTicks = 0;

        public TurretAttackAI() {
            // mutex=0：整合包里其他 mod 会往 tasks 注入最高优先级常驻任务，
            // 带互斥位的 AI 会被 canUse 永久拦截、永远不开火（历史问题，勿改）
            this.setMutexBits(0);
        }

        @Override
        public boolean shouldExecute() {
            // 用炮塔自己的目标字段（见 turretTarget 注释）：外部 mod 清空原版目标不影响开火
            EntityLivingBase target = turretTarget;
            if (target == null || !canFire()) {
                return false;
            }
            double range = getAttackRange();
            return target.getDistanceSq(EntityBaseTurret.this) <= range * range;
        }

        @Override
        public void resetTask() {
            this.cooldown = 0;
            this.aimSettleTicks = 0;
        }

        @Override
        public void updateTask() {
            EntityLivingBase target = turretTarget;
            if (target == null) {
                this.blockedTicks = 0;
                this.aimSettleTicks = 0;
                return;
            }
            if (this.cooldown > 0) {
                --this.cooldown;
                this.aimSettleTicks = 0;
                return;
            }
            // 水平对准检查：转动过程中不开火（否则光束随炮塔扫出去）
            float aimYaw = aimYawTo(target);
            if (Math.abs(MathHelper.wrapDegrees(turretYaw - aimYaw)) > YAW_ALIGN_TOLERANCE) {
                this.aimSettleTicks = 0;
                return;
            }
            // 俯仰对准检查：俯仰也在限速转动，未对准就开火会让光束脱离炮口指向
            float aimPitch = aimPitchTo(aimPointOf(target));
            float wantPitch = MathHelper.clamp(aimPitch, -getPitchMaxUp(), getPitchMaxDown());
            if (Math.abs(MathHelper.wrapDegrees(turretPitch - wantPitch)) > PITCH_ALIGN_TOLERANCE) {
                this.aimSettleTicks = 0;
                return;
            }
            if (!canEntityBeSeen(target)) {
                this.aimSettleTicks = 0;
                return;
            }
            // 弹道通视检查：炮口到瞄准点的直线上若有方块，光速弹丸会打在方块上（实测日志里"弹道 < 炮口→目标"）。
            // 挡住就停火；持续被挡 BLOCKED_GIVEUP_TICKS 则**放弃该目标**，让扫描去找看得见打得着的目标。
            if (isShotBlocked(target)) {
                this.aimSettleTicks = 0;
                if (++this.blockedTicks > BLOCKED_GIVEUP_TICKS) {
                    this.blockedTicks = 0;
                    EntityBaseTurret.this.turretTarget = null;
                    setAttackTarget(null);
                }
                return;
            }
            this.blockedTicks = 0;
            // 目标跑出射程就丢弃（换更近的目标），避免空转跟随
            double range = getAttackRange();
            if (target.getDistanceSq(EntityBaseTurret.this) > range * range) {
                this.aimSettleTicks = 0;
                EntityBaseTurret.this.turretTarget = null;
                setAttackTarget(null);
                return;
            }
            // 对准并保持：要求连续这么多 tick 都满足对准/可见/通视才开火。
            // 目的：转向是限速的，刚进容差时炮口其实还在动，立刻开火看起来像"甩狙"；
            // 蓄够 tick 再打，视觉上就是"稳稳瞄住了再开炮"。0 = 老行为（进容差即开火）。
            int settle = getAimSettleTicks();
            if (settle > 0 && ++this.aimSettleTicks < settle) {
                return;
            }
            if (tryFire(target)) {
                this.aimSettleTicks = 0;
                this.cooldown = getFireCooldown();
            }
        }
    }

    /**
     * 开火前要求"瞄准保持"多少 tick（默认 0 = 进对准容差即可开火）。
     * 普通炮塔覆写为 5：炮口停稳再打，避免看起来像甩狙。
     * 注意这会把实际射速拉长（冷却 + 保持时间），短冷却炮塔不要随手打开。
     */
    protected int getAimSettleTicks() {
        return 0;
    }

    /**
     * 炮口（用眼睛位置近似）到目标瞄准点的直线是否被方块挡住。
     * 比 {@code canEntityBeSeen}（眼→眼）更贴近真实弹道：激光/闪电是命中即判定的光束，
     * 打中了中间的方块就完全打不到目标。
     */
    protected boolean isShotBlocked(EntityLivingBase target) {
        Vec3d from = new Vec3d(this.posX, this.posY + this.getEyeHeight(), this.posZ);
        RayTraceResult hit = this.world.rayTraceBlocks(from, aimPointOf(target), false, true, false);
        return hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK;
    }

    // ==================================================================
    // 基础覆写
    // ==================================================================

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FACING, EnumFacing.SOUTH.getIndex());
        this.dataManager.register(TURRET_PITCH, 0.0F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0D);
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return true;
    }

    /** 客户端位置同步：只更新位置与朝向，不碰 rotationYawHead（那是服务端瞄准值） */
    @Override
    public void setPositionAndRotationDirect(double x, double y, double z,
                                             float yaw, float pitch,
                                             int posRotationIncrements, boolean teleport) {
        this.setPosition(x, y, z);
        this.setRotation(yaw, pitch);
    }

    // ==================================================================
    // ITurretEntity
    // ==================================================================

    @Override
    public TurretBaseTileEntMaster getMasterTile() {
        if (this.masterPos == null) {
            return null;
        }
        TileEntity te = this.world.getTileEntity(this.masterPos);
        return te instanceof TurretBaseTileEntMaster ? (TurretBaseTileEntMaster) te : null;
    }

    @Override
    public boolean isRedstoneAllowed() {
        TurretBaseTileEntMaster master = getMasterTile();
        return master != null && master.isRedstoneEnabled();
    }

    @Override
    public String getTurretType() {
        return this.turretType;
    }

    /**
     * 升级槽里是否装了炮塔升级卡。
     *
     * 升级卡是**单一通用卡**（0.26.1 起）：装了卡就由本实体自行解锁它的专属强化，
     * 具体解锁什么由各子类的 {@code fireWeapon()/getFireCooldown()/getAimPoint()} 决定。
     */
    protected boolean hasUpgrade() {
        TurretBaseTileEntMaster master = getMasterTile();
        return master != null && master.hasUpgrade();
    }

    // ==================================================================
    // 工具方法
    // ==================================================================

    public EnumFacing getTurretFacing() {
        return EnumFacing.getFront(this.dataManager.get(FACING));
    }

    /** 渲染用俯仰角：读 dataManager 同步值，不要读 rotationPitch（同步滞后） */
    public float getTurretPitch() {
        return this.dataManager.get(TURRET_PITCH);
    }

    /**
     * 结构方向对应的 MC 朝向角（yaw）
     * MC yaw 语义：0=南(+Z)、90=西(-X)、180/-180=北(-Z)、-90=东(+X)
     */
    public static float yawForFacing(EnumFacing facing) {
        switch (facing) {
            case SOUTH: return 0.0F;
            case WEST: return 90.0F;
            case EAST: return -90.0F;
            case NORTH: default: return 180.0F;
        }
    }

    /** 角度插值（处理 -180/180 环绕） */
    protected static float lerpAngle(float current, float target, float t) {
        float d = MathHelper.wrapDegrees(target - current);
        return MathHelper.wrapDegrees(current + d * t);
    }

    /** 普通线性插值 */
    protected static float lerpFloat(float current, float target, float t) {
        return current + (target - current) * t;
    }

    /** 角度限速逼近（处理 -180/180 环绕）：每 tick 最多转 maxDelta 度 */
    protected static float approachAngle(float current, float target, float maxDelta) {
        float diff = MathHelper.wrapDegrees(target - current);
        if (maxDelta <= 0.0F || Math.abs(diff) <= maxDelta) {
            return target;
        }
        return MathHelper.wrapDegrees(current + (diff > 0.0F ? maxDelta : -maxDelta));
    }

    // ==================================================================
    // NBT 持久化
    // ==================================================================

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
        if (compound.hasKey("facing")) {
            this.dataManager.set(FACING, compound.getInteger("facing"));
        }
        // turretType 是 final 且由构造器给定（写进 NBT 只为排障），不再从 NBT 覆盖
    }
}
