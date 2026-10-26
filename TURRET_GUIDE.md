# 炮塔系统开发指南（TGCEAddon / MC 1.12.2 / Forge）

> 用途：新增炮塔（BMPT、罗兰之后的第三个、第四个…）时的唯一参照。
> 依据：`EntityBMPTTurret`(685 行)、`EntityRolandTurret`(764 行)、`TurretBaseTileEntMaster`(519 行)、
> `ModelBMPT`(176)、`ModelRoland`(270)、`RenderBMPTTurret`(140)、`RenderRolandTurret`(94)、
> `ItemTurretCard`、`ItemTurretUpgrade` 源码实测。
> 上游参考源码：`D:\download\Techguns2-CE-master (1)\Techguns2-CE-master`（mod_version 2.2.0.1，RFG 构建）。

---

## 0. 一句话架构

```
炮塔基座(共用)                     炮塔(=一种一个)
├ 方块多方块 3x1x3                 ├ 实体 EntityCreature + 2~4 个 AI
│  炮塔控制器(master) + 8 防护板    ├ 模型 ModelBase(Blockbench 导出的 Java 类)
├ TileEntity(能量/物品/红黑石/GUI)   ├ 渲染器 Render<T>(转向/俯仰/补间)
└ 卡片 NBT turretType ────────────►└ 贴图 textures/entities/<name>.png
```

**新炮塔 = 一套「实体 + 模型 + 渲染器 + 贴图」，接到共用的基座上**；基座基本不用改（除非要做新的弹药/升级门控）。

---

## 1. 基座侧（`TurretBaseTileEntMaster`）

- 继承上游 `techguns.tileentities.BasicPoweredTileEnt` → 白嫖 RF 能量存储（`MAX_POWER = 5000`）、红石控制、
  所有者/安全、`ItemStackHandlerPlus` 物品栏与 NBT 同步。
- 结构：master 在中心，周围 8 格同高度 slave（`SLAVE_OFFSETS`）。`checkAndForm()` 形成，
  `BlockTurretBase.FORMED` 方块属性标记；每 10 tick `validateMultiblock()` 自检，缺一格即 `onMultiBlockBreak()` 解散。
- **槽位布局（写新炮塔取弹药时必用）**：

| 槽位 | 常量 | 内容 |
|---|---|---|
| 0–8 | `SLOT_INPUT1` / `INPUTS_SIZE=9` | 弹药输入 |
| 9–17 | `SLOT_OUTPUT1` / `OUTPUTS_SIZE=9` | 弹药输出 |
| 18 | `SLOT_CARD` | 炮塔卡片（决定生成哪种炮塔） |
| 19 | `SLOT_UPGRADE` | 炮塔升级卡 |

- **炮塔实体生命周期**（`update()`，仅服务端）：
  `wantTurret = formed && !getCardType().isEmpty()` → 空则 `spawnTurretEntity()`；有则 `killTurretEntity()`。
  世界重载后按 `mountedTurretUUID` 在 `world.loadedEntityList` 里找回实体，避免重复生成。
- **弹药与能量接口**（炮塔实体调用）：
  - `hasCannonAmmo()/consumeCannonAmmo()` → `ModItems.cannonShell`
  - `hasRocketAmmo()/consumeRocketAmmo()` → `TGItems.ROCKET`
  - `hasNukeAmmo()/consumeNukeAmmo()` → `TGItems.ROCKET_NUKE`
  - `consumeTurretPower(int rf)`
  - ⚠ `InventoryUtil.canConsumeItem(...)` **返回 0 表示"弹药足够"**，所以判定写 `<= 0`（历史上写反过一次）。
- 索敌开关：`attackAnimals`（GUI 按钮）、`getPvpSetting()`（0=不打玩家 / 4=打其他玩家）、
  `isPlayerPlaced()`（false=自然结构/模组放置，视为敌对建筑打所有玩家）、`getOwner()`。
- 升级：`hasUpgrade()`（升级槽里有没有炮塔升级卡）。**升级卡不区分类型**（0.26.1 起为单一通用卡），
  插上后由各炮塔自己解锁专属强化，详见 §13。
- 基座渲染：`RenderTurretBase` + `models/block/turretbase_model.json` + `textures/blocks/turretbase_texture.png`
  —— 走 `BlockbenchJsonModel`（JSON 路线，与炮塔本体的 Java 模型路线不同）。

---

## 2. 两种炮塔参数对照（新炮塔照着填这张表）

| 项 | BMPT (`EntityBMPTTurret`) | 罗兰 (`EntityRolandTurret`) |
|---|---|---|
| 索敌/射程 | `ATTACK_RANGE = 64` | `ATTACK_RANGE = 300`（远程防空） |
| 转向速度 | yaw `6.0F`/tick (=120°/s)、pitch `8.0F`/tick (=160°/s) | `YAW_TURN_SPEED = 6.0F`、`PITCH_TURN_SPEED = 8.0F`（同值，只是提成了常量） |
| 俯仰限制 | 上 `PITCH_MAX_UP=60` / 下 `PITCH_MAX_DOWN=10` | 上 90 / 下 20 |
| 对准容差 | yaw 4° / pitch 3° | yaw 10° / pitch 6° |
| 火力 | 左机炮 20 发(`CANNON_DELAY=2`)→左巢 2 火箭(`ROCKET_DELAY=15`)→右机炮→右巢，循环换侧 | 单发导弹，`MISSILE_COOLDOWN=30`，最多 1 发在飞 |
| 弹药/消耗 | 炮弹 10 RF/发、火箭 200 RF/发 | `MISSILE_POWER=300` RF/发 + 1 火箭（升级卡+有核弹→核变体） |
| 发射物(实体 id) | `CannonShellProjectile`(3003)、`BMPTGuidedMissileProjectile`(3012) | `RolandMissileProjectile`(3008)、`RolandMissileProjectileNuke`(3009) |
| 实体锚点 | `master.y + 1.0625` | `master.y + 0.5625` |
| 渲染上移 | `translate(0, 25/16, 0)` | `translate(0, 33/16, 0)` |
| 模型俯仰组 | `gun` + `pitchL(rol)` + `pitchR(ror)` | `tr`（发射架） |
| 模型专属动画 | 无 | `radar` 匀速旋转（`RADAR_ANGLE` 同步，有电才转） |
| 注册实体 id | **3002** | **3007** |
| 血量/移动 | 100 血、移动速度 0 | 同左 |

> **锚点与渲染上移必须成对标定**：实体 y 与渲染 `translate` 是解耦的两个数，模型根节点 y 不同就得重新对。
> 对齐目标：模型底部正好坐在基座顶面。

---

## 3. 实体侧约定（每个新炮塔都要遵守）

1. `extends EntityCreature`，构造 `(World)` 与 `(World, BlockPos masterPos, EnumFacing facing)` 两个。
2. `applyEntityAttributes()`：`MAX_HEALTH`、`MOVEMENT_SPEED = 0`、`FOLLOW_RANGE = 射程`。
3. 同步用 `DataParameter`（`EntityInit` 里 `register`）：
   `FACING`(Integer, 结构朝向)、`TURRET_PITCH`(Float)、罗兰另加 `RADAR_ANGLE`(Float)。
4. `onUpdate()` 服务端分支：取 `getMasterTile()`，为 null 或未成形 → `setDead()`；
   否则每 tick `setPosition(master 中心 + 锚点高度)`、`rotationYaw = yawForFacing(dir)`、写回 `FACING`。
5. **转向自己累积**（`turretYaw`/`turretPitch` 字段），每 tick `approachAngle` 限速逼近，
   再写回 `rotationYawHead`/`rotationPitch`。**不要依赖 `lookHelper`**：它每 tick 把这两个值清零。
6. 索敌：
   - **索敌：一律用自研扫描 AI（`EntityBaseTurret.TurretTargetSearchAI`，`setMutexBits(0)`），
  不要用原版 / 科技枪的 `EntityAINearestAttackableTarget` 系列** —— 见 §13（整合库里会被拦截）
     （基类默认 16，1.12.2 没有 setter；不覆写会出现"射程外目标被锁定、炮塔空转抖动"）。
   - 远程/高速目标：自研扫描 AI（罗兰 `AIRolandTargetSearch` 每 5 tick 一扫，避免高速目标穿过防区漏检）。
   - 过滤条件链：红石允许 → 距离内 → `IMob` / 玩家(PVP 设置+所有者) / 动物(`attackAnimals`) → 俯仰角够得着。
7. 开火 AI：`extends EntityAIBase`，**`setMutexBits(0)`**（否则被其他 mod 注入的常驻任务互斥拦截，永远不开火）。
   `shouldExecute()` 检查"有目标 + 能量 > 0 + 红石允许"；开火前先做**炮口对准检查**（容差内才开火，否则扫射）。
8. 停火但不断头：能量/弹药不足时只重置开火计数，**转向逻辑放在 `onUpdate` 而不是 AI 里**
   （AI 的 `shouldExecute` 返回 false 会让整个 AI 不跑，转向放里面炮塔就僵住）。
9. 其他：`canDespawn()=false`、`isEntityInvulnerable()=true`、`setPositionAndRotationDirect()` 只同步位置与
   yaw/pitch（客户端不要用它改 `rotationYawHead`）、`readEntityFromNBT/writeEntityToNBT` 存 `turretType`。
10. 炮口位置：模型像素坐标 → 世界坐标，必须**复刻渲染器的变换**（俯仰绕发射架中心 → 水平旋转取负 →
    垂直 `(RENDER_TRANSLATE - y3)/16`），见 `EntityRolandTurret.getMuzzlePos()`。

---

## 4. 渲染侧约定

固定顺序（照抄，顺序错就翻车）：

```java
GlStateManager.pushMatrix();
GlStateManager.disableCull();
GlStateManager.translate(x, y, z);
GlStateManager.translate(0.0F, N / 16.0F, 0.0F);   // ① 必须在 scale 之前，否则位移被负缩放翻成向下
GlStateManager.scale(-1.0F, -1.0F, 1.0F);          // ② 行列式 +1 的纯旋转：修 Blockbench 的上下颠倒+左右镜像
GlStateManager.rotate(yawHead - 180.0F, 0, 1, 0);  // ③ 模型零向朝 -Z，镜像后 180-yaw 正好指向目标
this.bindEntityTexture(entity);
this.model.renderTurret(entity, ..., turretPitch /*, radarAngle*/);
```

- **客户端帧级补间**：实体上放 `public float renderYawHead = Float.NaN; public float renderPitch = Float.NaN;`，
  渲染器每帧按 `RENDER_TURN_SPEED`(180°/s) 逼近服务端同步值；差值 > 90° 直接瞬移。
  状态存**实体**上而不是渲染器上（渲染器实例按实体类共享，多座炮塔会串味）。
- 俯仰客户端读 `entity.getTurretPitch()`（dataManager 每 tick 无量化同步），
  **不要读 `rotationPitch`**（被 HeadLook/Look 包交替同步，严重滞后）。
- `getAttackTarget()` 在客户端永远是 null（1.12.2 不同步）→ 渲染判断不要用它。

---

## 5. 模型与贴图约定

- **模型路线 A（炮塔本体用这个）**：Blockbench 5.x → 导出 `Minecraft 1.7-1.12` 的 Java 模型 →
  落到 `com.teamytz.tgceaddon.client.models.machines.ModelXxx extends ModelBase`，
  `textureWidth = textureHeight = 256`。分组就是动画单元。
  水平转向由渲染器整体旋转，**只有俯仰组**在 `renderTurret(...)` 里设 `rotateAngleX`，渲完立刻复位（否则污染下一帧）。
- **模型路线 B（基座方块用）**：`BlockbenchJsonModel` 直接读 `assets/tgceaddon/models/**/*.json`
  （支持 element from/to、rotation、逐面 UV）。要"改贴图不用重编译"可以走这条。
- 贴图：`assets/tgceaddon/textures/entities/<name>.png`，当前 `bmpt.png` / `roland.png` 均为 **256×256**，一个炮塔一张。
- 工作区根 `models\` 里是你的 Blockbench 源文件（含 `.bbmodel` 与导出 `.java`/`.json`），导入 `src` 前注意
  贴图必须放到上面的资源路径、模型类必须放进 `client.models.machines` 包。

---

## 6. 新增一个炮塔的改动清单

> **实体 id 占用表（`ModEntities`，新增顺延到 3014+）**：
> 3001 BoltProjectile · 3002 BMPT 炮塔 · 3003 CannonShellProjectile · 3004 OverloadGuidedMissileProjectile ·
> 3005 CommandLineMissileProjectile · 3006 IRMissileProjectile · 3007 罗兰炮塔 · 3008 RolandMissileProjectile ·
> 3009 RolandMissileProjectileNuke · 3010 CommandLineMissileProjectileNuke · 3011 EntityMuzzleLight ·
> 3012 BMPTGuidedMissileProjectile · 3013 EntityFlare

| # | 文件 | 改什么 |
|---|---|---|
| 1 | `entities/EntityXxxTurret.java` | 新建（可复制罗兰或 BMPT 改参数），**`implements ITurretEntity`** |
| 2 | `client/models/machines/ModelXxx.java` | Blockbench 导出 |
| 3 | `client/render/entities/RenderXxxTurret.java` | 新建（贴图路径 + 锚点标定） |
| 4 | `assets/.../textures/entities/xxx.png` | 贴图 |
| 5 | `init/ModEntities.java` | `registerModEntity`，id 从 3014 起顺延（勿用小数 id） |
| 6 | `proxy/ClientProxy.java` | `registerEntityRenderingHandler` |
| 7 | **`entities/TurretRegistry.java`** | 静态块里加一行 `register("<type>", EntityXxxTurret::new);` ← **唯一的类型登记点** |
| 8 | `init/ModCreativeTabs.java` | 创造栏加入带 `turretType` NBT 的卡片 |
| 9 | `lang/en_us.lang` + `zh_cn.lang` | `item.tgceaddon.turret_card.<type>` |
| 10 | `init/ModRecipes.java` / 蓝图 | 若需要合成 |

**基座（`TurretBaseTileEntMaster`）不再需要为每种炮塔改动** —— 类型派发已抽成注册表（0.20.30 完成）：

```java
// entities/ITurretEntity.java —— 基座只认接口，不认具体类
public interface ITurretEntity {
    TurretBaseTileEntMaster getMasterTile();
    boolean isRedstoneAllowed();
    String getTurretType();        // 必须与卡片 NBT "turretType" 完全一致
}

// entities/TurretRegistry.java —— type → 工厂
static {
    register("bmpt", EntityBMPTTurret::new);
    register("roland", EntityRolandTurret::new);
    // 新炮塔加这里
}
```

基座侧现在：`spawnTurretEntity()` → `TurretRegistry.create(type, ...)`（类型未登记则**不生成并告警一次**，
不回退到默认类型）；UUID 找回 → `ent instanceof ITurretEntity` 且类型与卡片一致。

**顺带修掉一个真实 bug**：旧逻辑只判断"有没有已生成实体"，**换卡片（BMPT↔罗兰）后炮塔不会变、继续用旧炮塔**。
现在 `update()` 里会比对 `ITurretEntity.getTurretType()` 与卡片类型，不符则拆掉重建。

---

## 7. 已知坑（源码注释里踩出来的，必须沿用）

1. `lookHelper` 每 tick 清零 `rotationYawHead`/`rotationPitch` → 转向必须自建累积字段。
2. `getAttackTarget()` 不同步到客户端（永远 null）。
3. 服务端 20tps 转向有台阶感 → 渲染器帧级补间（状态放实体上）。
4. `translate` 必须在 `scale(-1,-1,1)` **之前**。
5. `InventoryUtil.canConsumeItem` 返回 0 = 足够（判定 `<= 0`）。
6. **先查能量与弹药再消耗**：旧罗兰先扣 300 RF 再查弹药，没弹药时每 tick 白耗能量，
   能量归零后 AI 的 `energy > 0` 不成立 → 炮塔永久卡死。
7. `registerModEntity` 的 id 必须 3001+（techguns 用低 id，撞了会反查到错实体）。
8. 方块被破坏时不要再发 TE 更新包（`invalid update packet for null tile entity`）。
9. `setMutexBits(0)`，否则其他 mod（如 DynamicStealth）注入的常驻任务会互斥掉开火 AI。
   **这就是历史问题"炮塔作为自然结构生成时 AI 像是消失"的真相**：不是我们的实体逻辑问题，
   是整合包里别的 mod 往 `tasks` 里注入最高优先级常驻任务，把带互斥位的开火 AI 永久拦掉了；
   三处 AI（BMPT 开火、罗兰开火、罗兰雷达扫描）都已置 mutex=0。**改动这三个 AI 时不要动 mutex 设置。**
10. **索敌用自研扫描 AI + `setMutexBits(0)`**（`EntityBaseTurret.TurretTargetSearchAI`）。
    原版 `EntityAINearestAttackableTarget`（含科技枪薄封装）走 `EntityPredicate` 且互斥位=1，
    在整合包里容易被别的 mod 的任务挡住，新炮塔一律用自研扫描。
    另见 §11 的「排查：炮塔不索敌 / 不开火」小节：DynamicStealth 会接管实体的攻击目标，**新炮塔的实体注册名必须加进它的
    "Entity-Specific Full Bypass" 名单**（BMPT/罗兰早已在名单内），代码侧有 `turretTarget` 兜底。
11. **抽象基类的实例字段初始化器会冲掉 `initEntityAI()` 里的赋值**：`initEntityAI()` 是在
    `super()` 期间（`EntityLiving` 构造函数末尾）被调用的，而字段初始化器在 `super()` 之后执行。
    所以基类里需要跨构造期保留的字段**不要写初始值**（0.25.0 的 `aiReady = false` 就因此永远是 false）。
11. **不要改渲染级的 `scale(-1,-1,1)` 镜像方案**：历史问题"导入模型时出现镜像"的最终解法就是
    "渲染级镜像 + 模型数据保持 Blockbench 原样"，并且炮口坐标计算（`wx` 取负）与罗兰雷达角度
    （正角补偿镜像反转的 Y 旋转）都依赖它。改镜像就要同步改这两处补偿。

---

## 8. 上游 techguns 可复用件（2.2.0.1 源码）

| 类 | 行数 | 用途 |
|---|---|---|
| `techguns.tileentities.TurretTileEnt` | 511 | 上游炮塔基座 TE（本模组另起了一套，未继承；可对照能量/弹药逻辑） |
| `techguns.entities.npcs.NPCTurret` | 384 | 上游炮塔实体（本模组 BMPT 的索敌范式来源） |
| `techguns.client.models.machines.ModelTurretBase` | 168 | 上游基座模型（Java 路线） |
| `techguns.client.models.npcs.ModelTurret` | 62 | 上游炮塔模型（简单版） |
| `techguns.client.render.tileentities.RenderTurret` | 107 | 上游基座渲染 |
| `techguns.gui.containers.TurretContainer` + `TurretGui` | 165 | 上游炮塔 GUI（含 `SlotTurretGun`/`SlotTurretArmor`） |
| `techguns.entities.ai.TurretEntityAINearestAttackableTarget` | 27 | ⚠ **不要用**（同原版目标 AI，整合包里会被拦截，见 §13） |
| `techguns.TGItems` | — | `ROCKET` / `ROCKET_NUKE` 弹药引用 |
| `techguns.gui.ButtonConstants` | — | GUI 按钮 ID 基准（`BUTTON_ID_REDSTONE` 等） |

---

## 9. 待办与线索

- `lang` 里已有 `tile.tgceaddon.wall_turret.name`（墙面炮塔 / Wall Turret），**代码中不存在** → 预留的炮塔位。
- `ItemTurretUpgrade` 是**单一通用升级卡**（无子类型）：装上即解锁当场炮塔的专属强化，详见 **§13**。
- 弹药目前写死三种（炮弹/火箭/核火箭）；若新炮塔要用自己的弹药，建议在 `TurretBaseTileEntMaster` 里加
  「按 ItemStack 模板匹配」的通用方法，而不是每种弹药再加一对 `has/consume`。
- **普通炮塔（`EntityTurret`）武器已实现（0.28.0）**：双弹种主炮，弹种由弹药栏里实际有的东西决定，
  4 秒一发，开火有炮口烟，详见 **§15**。
- 炮塔接弹药消耗时的现成接口：`TurretBaseTileEntMaster.hasAmmo(ItemStack)` /
  `consumeAmmo(ItemStack)`（已支持任意物品，不用再给每种弹药写一对方法）。

---

## 10. 已有炮塔清单（含 0.21.0 新增的四座）

| 卡片类型 | 实体类 | 模型类 | 贴图 | 尺寸 | 状态 |
|---|---|---|---|---|---|
| `bmpt` | EntityBMPTTurret | ModelBMPT | bmpt.png | 256² | 完整（机炮+火箭巢+俯仰） |
| `roland` | EntityRolandTurret | ModelRoland | roland.png | 256² | 完整（雷达+俯仰+导弹） |
| `turret` | EntityTurret | ModelTurret | turret.png | 256² | 完整（**HE/APFSDS 双弹种主炮** + 俯仰 + 炮口烟，0.28.0） |
| `laser_turret` | EntityLaserTurret | ModelLaserTurret | laser_turret.png | 128² | 仅模型/贴图/卡片 |
| `laser_turret_mk1` | EntityLaserTurretMark1 | ModelLaserTurretMark1 | laser_turret_mark1.png | 256² | 仅模型/贴图/卡片 |
| `lightning_turret` | EntityLightningTurret | ModelLightningTurret | lightning_turret.png | 128² | 仅模型/贴图/卡片 |
| `m1` | EntityM1Turret | ModelM1Abrams | m1_abrams.png | 512² | **完整（0.30.0）：主炮 HE/APFSDS + 机枪，双武器独立瞄准** |

`m1`（0.30.0）是本基类下第一座**双武器**炮塔，也是第一个**专用渲染器**的用例（其余四座用通用
`RenderBaseTurret`）。它给基类补了两个钩子，其它多武器炮塔可直接复用：

```java
// EntityBaseTurret
protected float getTargetPitchMaxUp()   { return getPitchMaxUp(); }
protected float getTargetPitchMaxDown() { return getPitchMaxDown(); }
```

索敌时的俯仰筛选改用这两个值。M1 主炮只有 ±15°，若沿用主炮限幅，机枪的 +50° 就永远用不上
（目标根本不会被锁定）；覆写成机枪范围后，云端/高空目标能被锁定，再由机枪去打。

**0.30.1 / 0.30.2 三条踩坑记录**：

1. **子弹材质**：机枪弹丸最初用科技枪的 `AdvancedBulletProjectile`，它的渲染器绑的是
   `textures/entity/bullet_blue.png` → 打出来是**蓝色**弹丸。BMPT 机炮用的是
   `GenericProjectile`（+ 本模组的 `RenderCannonShellProjectile extends RenderGenericProjectile`），
   贴图是 `textures/entity/bullet1.png`（**黄色曳光**）。要"像 BMPT 那样"的普通子弹，直接用
   `techguns.entities.projectiles.GenericProjectile`，**不要**用 `AdvancedBulletProjectile`。
2. **锚点随模型走**：实体锚点与渲染上移是一对，关系是
   `anchor + (RENDER_TRANSLATE_PX − 模型底部y)/16 = 1`
   （其它炮塔：底部 26 + 锚点 17/16 + 上移 25 = 1）。M1 最终模型底部 = **24** →
   锚点 **0.9375**。**每次重置模型都要重算这个数**，否则炮塔会陷进基座或浮空。
3. **算包围盒必须把旋转算进去**（0.30.2 的真凶）：M1 里 `cube_r17` 三轴都带旋转
   （`setRotationAngle(r, 0.9634, 0.8468, -0.7294)`），按**未旋转**的盒子它伸到 y=29，
   旋转后其实只到 24 以上。按 29 算锚点（1.25）会让整台车**浮空 5 像素（0.31 格）**。
   正确做法：对每个盒子的 8 个角点套上 `Rz*Ry*Rx` 再累加父链的旋转点与旋转，
   取变换后的 y 最大值。工作区里那次计算用的脚本思路可直接复用。

   > 顺带：`.bbmodel` 改了必须重新导出/转换 Java 模型 —— 0.30.1 时 `M1abrams.java`
   > 还是旧的时间戳（炮管 -106），而 bbmodel 已经是新的（-91）。用
   > `.tools/bbmodel2java.js` 直接转 bbmodel 最稳（本机无 Blockbench）。

M1 的四条动画通道（模型 {@code renderM1(scale, mainPitch, mgYawRel, mgPitch, recoil)}）：

| 通道 | 模型组 | 同步/驱动 |
|---|---|---|
| 炮塔水平 | 整体 `rotate(yawHead-180)` | 基类 `rotationYawHead` + 渲染器帧级补间 |
| 主炮俯仰（±15°） | `mainweapon` | 基类 `TURRET_PITCH` |
| 机枪方向机（相对炮塔） | `machinegun` 绕 Y | 实体自建 `MG_YAW` 数据参数（渲染时减去补间后的炮塔 yaw） |
| 机枪高低（仰 50°/俯 15°） | `gun_main` | `MG_PITCH` |
| 主炮后坐力 | `cannon_barrel.offsetZ` | `RECOIL`（开火置 1，6 tick 线性回零） |

机枪炮口坐标比基类 `muzzlePos` 多两步（先绕 `gun_main` 高低轴、再绕 `machinegun` 方向轴，
最后套用炮塔偏航 + 镜像），见 `EntityM1Turret.muzzlePosMachineGun()`。

后四座走**新路径**，与 BMPT/罗兰各自独立实现不同：

- `entities/EntityBaseTurret`（抽象基类）：跟随后 master、结构朝向、`ITurretEntity` 对接、
  NBT、不可摧毁/不自然消失。**不含任何武器逻辑**；子类只需 `applyEntityAttributes()` + `initEntityAI()`。
- `client/render/entities/RenderBaseTurret<T>`（通用渲染器）：构造参数 = 模型 + 贴图 + 上移像素。
  变换顺序与既有渲染器逐字一致（translate → 上移 → `scale(-1,-1,1)` → `rotate(yaw-180)` → render）。
- **BMPT / 罗兰未被改动**：它们的行为与视觉已标定，除非专门回归验证，不要顺手改成新基类。

### 新模型从哪来：bbmodel → Java 模型类

新炮塔只提供 `.bbmodel`（Blockbench 工程文件），本机跑不了 Blockbench，故用
`​.tools/bbmodel2java.js`（工作区根 `.tools/`）转换：

```powershell
$node = 'C:\Users\admin\AppData\Local\Programs\DeepSeek Harness\resources\runtime\win32-x64\node.exe'
& $node .tools\bbmodel2java.js models\Turret\xxx\xxx.bbmodel ModelXxxFoo com.teamytz.tgceaddon.client.models.machines
```

导出的规则（由 `models/blocks/` 下既有的 BMPT / 罗兰导出文件反推 + 回归验证，详见脚本头注释）：

| 项 | 规则 |
|---|---|
| 绝对变换 | `A(x,y,z) = (-x, 24-y, z)`（对应 bbmodel 的 `modded_entity_flip_y = true`） |
| 组渲染点 | `A(组origin) − A(父组origin)`，顶层父组视作原点 |
| 方块最小角 | `A(元素 to 的 x/y、from 的 z) − 参照点`，尺寸 = `|to−from|`；参照点：普通元素用组原点，旋转元素用 `A(元素origin)` |
| 旋转元素 | 单独成 `<元素名>_r<N>` 子渲染器（同名元素全局计数）；**rotation+origin 相同者合并**到一个渲染器；outliner 子节点**逆序**处理 |
| 旋转角符号 | `rx → -rx`、`ry → -ry`、`rz → +rz`（`D=diag(-1,-1,1)` 共轭） |
| 数字格式 | `toFixed(4)`（Blockbench 实测对 18.578949 输出 18.5789） |
| `render()` | 渲染**所有**顶层组（只渲染第一个会丢几何：罗兰有 5 个顶层组） |
| **组旋转** | **照抄**为组渲染器 `setRotationAngle`（与 Blockbench 一致），见下 |
| **俯仰** | `--pitch <组名>` → 实现 `ITurretModel` 并输出 `renderTurret(..., pitchDeg)` |

**组旋转绝不能"烘焙进元素坐标"（0.23.0 → 0.24.0 的教训）**：MC 的 `ModelBox` 用固定的 6 面 UV 布局、
只有一对 `(u,v)`；把盒子坐标镜像后几何翻转了但 UV 布局没跟着走 → **贴图会错位**（已实测）。
所以必须保留组旋转，让几何与贴图一起被旋转。

代价（必须处理）：MC 的 `ModelRenderer` 按 `Rz*Ry*Rx` 复合，俯仰分组自身的 `Ry`
会排在我们设置的俯仰 `Rx` **之前** → 该塔俯仰方向反掉。处理办法：实体覆写
`getModelPitchSign()` 返回 `-1`（默认 `+1`），该符号同时作用于**模型动画**与**炮口换算**。
脚本会自动检测并在生成文件头部提示是否需要 -1（判据：俯仰分组的组旋转把局部 X 轴翻向 -X）。

**回归基准（改了转换器必须重跑）**：BMPT 应零结构差异；罗兰只应有 1 处标识符差异
（Blockbench 生成非法 Java 名 `1`，本脚本重命名为 `_1`）+ 因 bbmodel 比旧导出文件新而产生的方块漂移。
实测基准值：BMPT 差异 8 行、罗兰 68 行。

### 锚点配对（新模型必须重算这一对常量）

```
渲染上移像素 N = 模型最高点(px) + 1        模型最高点 = 24 − 模型内最小 from_y
实体 y        = master.y + 17/16
=> 模型底部 = master.y + 1.125 格（基座上表面），与 BMPT 完全一致
```

实测：六座炮塔的模型最高点都是 24 → **N = 25、实体 +17/16**。
（罗兰历史上用的是 N=33 + 实体 +9/16，同样落在 1.125 格 —— 两者等价，说明这条不变式成立。）
改动模型尺寸/删减几何后必须重算 N，否则炮塔会悬空或陷进基座。

### 下一轮要做的事

> **0.22.0 完成了武器**（§11）、**0.23.0 完成了俯仰**（§12）。
> 剩下的待办：**普通炮塔的武器**（俯仰数据已有：俯角 25° / 仰角 50° / 分组 `gun`）。

对四座新炮塔，逐个确定后按既有模式补：

1. ~~`initEntityAI()`：索敌 + 开火 AI~~ → 0.22.0 已在 `EntityBaseTurret` 里通用化
2. ~~`onUpdate()` 转向~~ → 0.22.0 已在基类实现（水平转向已可用；俯仰的限幅待分组确定）
3. 俯仰：在各自模型类里加 `renderTurret(entity, ..., pitch)`，设模型分组（如 `gun`/`turret`）的
   `rotateAngleX`，渲完复位；然后 `RenderBaseTurret` 改为调用它
4. ~~炮口位置换算~~ → 0.22.0 已实现（`EntityBaseTurret.muzzlePos`，与罗兰同式）
5. ~~弹药/能量消耗~~ → 0.22.0 已实现（基座通用 `hasAmmo/consumeAmmo` + 基类 `consumeAmmoEvery`）

---

## 11. 武器实现（0.22.0）

### 三座炮塔的武器一览

| 炮塔 | 弹丸（科技枪） | 伤害/弹速/存活/冷却 | 发射 | 消耗（0.24.0 起） | 参数来源 |
|---|---|---|---|---|---|
| 激光炮塔 | `LaserProjectile` | 16 / 100 / 7 tick / 5 tick | 中间单束 | **RF 50/发** | `TGuns.lasergun` |
| 激光 Mk.1 | `LaserProjectile` | 16 / 100 / 7 tick / 5 tick | 左右各一束（共 2） | **RF 100/发** | `TGuns.lasergun` |
| 闪电炮塔 | `TeslaProjectile` | 9 / 80 / 10 tick / 8 tick | 中间单束 | **RF 60/发** | `TGuns.teslagun` |
| 普通炮塔 | —— | —— | —— | —— | 未实现（保持静止） |

**不再需要弹药**：0.24.0 按需求移除了"每 N 发一个能量单元"，
改为 `tryFire` 里先 `master.consumeTurretPower(cost)`，成功才生成弹丸（能量不足则不开火、不扣能量）。
RF 数值是本模组设定，均为 `public static final int POWER_PER_SHOT`，随时可调。
（基座的通用 `hasAmmo/consumeAmmo` 与基类 `consumeAmmoEvery` 都保留着，将来要弹药的塔直接用。）

**贴图 / 特效 / 渲染全部复用科技枪**：科技枪已注册 `LaserProjectile` / `TeslaProjectile` 两个实体
（`TGEntities` 第 90-91 行）并挂了渲染器（`techguns.client.ClientProxy` 第 1073-1074 行），
本模组只 `world.spawnEntity(...)`，**不需要注册实体、不需要写渲染器**。

**闪电的连续伤害是继承来的**：`TeslaProjectile.onHit` 命中后在其 `CHAIN_RANGE=8` 格内找下一个目标继续连锁，
最多 `CHAIN_TARGETS=4` 个目标，每跳伤害 ×`CHAIN_DAMAGE_FACTOR=0.75`，伤害源 `causeLightningDamage`
（DamageType.LIGHTNING）。我们只是原样生成它，机制没有被改写。

### 炮口坐标（bbmodel 实测）

| 炮塔 | bbmodel 炮口 | Java 模型空间 = A(x,y,z) |
|---|---|---|
| 激光炮塔 | (0, 11, −18)（2026-09-20 方向修正后重算，见 §12） | (0, 13, −18) |
| 激光 Mk.1 左/右 | (∓15.5, 14, −41)（两门炮前端面心，Y[7,21]） | (±15.5, 10, −41) |
| 闪电炮塔 | (0, 11, −30)（发射口环心，环面 X[-4,4] Y[7,15]） | (0, 13, −30) |

取法：找**发射分组**里 z 最小的那圈元素（发射口是环形的：上下左右各一块包围中间的洞），取环心。
`.tools/muzzle3.js` 可按分组打印"前端面范围 + 前端元素清单"，新炮塔直接用。

### 命中即判定弹丸的起始点问题（重要）

科技枪的激光/闪电弹**在构造器里就 `trace()` 完成 raycast 与伤害**（hitscan），之后只作为视觉光束存活。
所以起始位置**必须在构造前就摆对**，事后 `setPosition` 无效（那只会挪动视觉光束）。

`shooter` 字段是 `protected`，跨包无法直接写；而生成未注册的子类会因实体未注册而崩。因此采用：

```java
// 科技枪起始点 = (shooter.x, shooter.y + eyeHeight - 0.1, shooter.z)，故补偿 = -eyeHeight + 0.1
double ox = posX, oy = posY, oz = posZ;
alignToMuzzleForSpawn(muzzle, aimYaw, aimPitch);   // 临时把炮塔摆到炮口并写入瞄准角
LaserProjectile beam = new LaserProjectile(world, this, ...);   // 构造即命中，位置/朝向取自上面的临时状态
restoreFromMuzzle(ox, oy, oz);                     // 立刻复位（同 tick 内完成，不会产生位置同步包）
world.spawnEntity(beam);
```

- 弹丸方向取自 `shooter.rotationYawHead` / `rotationPitch`，所以临时状态下要一并写入瞄准角
- **已确认 `GenericProjectile.findEntityOnPath` 会跳过 `this.shooter`**，光束不会打到炮塔自己；
  方块射线从炮口向外，也不受影响
- `EnumBulletFirePos.CENTER` 不产生侧向偏移（只有 RIGHT/LEFT/ZOOMED 有）

### 弹药与门控（0.24.0 起：三塔只吃 RF，不需要弹药）

- **三座武器塔改为每发消耗 RF**：激光 50 / Mk.1 100 / 闪电 60（各塔 `POWER_PER_SHOT` 常量，可调）
- `tryFire` 里**先 `master.consumeTurretPower(cost)` 成功再生成弹丸**；失败直接返回 false（不开火、不扣能量）。
  顺序很关键：先扣再查会导致"没能量照样扣光 → 卡死"（罗兰早期踩过）
- 开火前置 `canFire()` = 已成形 + `energyStored > 0` + 红石允许
- 基础弹药接口**保留备用**（将来要弹药的塔直接用，不要再加一对 has/consume）：
  基座 `hasAmmo(ItemStack)` / `consumeAmmo(ItemStack)`（按 ItemStack 模板匹配）、
  基类 `consumeAmmoEvery(shotsPerItem, template)`（每 N 发扣一个，该扣而没有则不开火）
- 伤害衰减参数：`distance <= damageDropStart` 为满伤，超过后到 `damageDropEnd` 线性衰减到 `damageMin`；
  **两个值不能相等**（会除零产生 NaN），现有取值均远大于射程以保证射程内满伤

### 排查：炮塔不索敌 / 不开火

**0.25.1 的根因结论（实测定位，别再走弯路）**：整合包里的 **DynamicStealth** 会在
`EntityJoinWorldEvent` 里给**每个 EntityLiving** 注入自己的最高优先级 AI：

```java
tasks.addTask(-7777777, AIDynamicStealth.getInstance(living));   // DynamicStealth.addTasks()
```

并在其逻辑里调用 `Compat.clearAttackTargetAndCancelBadTasks()`（= `setAttackTarget(null)` +
取消"需要攻击目标"的 AI 任务）。于是炮塔"锁定 → 被清空 → 再锁定"每 5~6 tick 循环一次，
**开火 AI 永远拿不到目标**（日志实证：每座塔 387 次锁定 / 387 次丢失）。

**它自带豁免名单**，`config/dynamicstealth/<版本>.cfg`：

| 配置项 | 作用 |
|---|---|
| `S:"Entity-Specific Full Bypass" <` | **完全绕过所有 DS 系统**（BMPT / 罗兰早就在名单里，所以它们一直正常） |
| `S:"Threat Bypass" <` | 只绕过威胁系统（不注入威胁 AI，但保留其它 DS 功能） |

**新增炮塔必须把实体注册名加进 `Entity-Specific Full Bypass`**，例如：

```
tgceaddon:turret
tgceaddon:laser_turret
tgceaddon:laser_turret_mk1
tgceaddon:lightning_turret
```

（注册名 = `ModEntities` 里 `new ResourceLocation(MODID, "...")` 的字符串。）

**代码侧还有一层保险（0.25.1 起）**：`EntityBaseTurret.turretTarget` 是炮塔**自己的目标字段**，
索敌/开火/转向全部以它为准，`setAttackTarget(...)` 只作为同步镜像（给 HUD/仇恨/击杀归属用）。
因此即使换了服务器、没配豁免名单，外部 mod 清空原版攻击目标也不会让炮塔停摆。

**转向与开火的门控是分开的**：转向只要锁定目标就跟随（不受能量/红石影响）→ 没电也应该看得见转头；
开火才由 `canFire()` + 对准/视线/射程检查 + 每发 RF 门控。

`EntityBaseTurret.DEBUG_TURRET = true`（默认开）时每 **100 tick(≈5 秒)** 输出一行心跳
（0.28.1 起；以前是 400 tick/20 秒，挂上卡片试十几秒根本等不到一行），
另外 **tick=20 时固定打一行**，用于确认"实体确实起来了"。带弹药的炮塔（普通炮塔）末尾多一段 `弹药[HE=… APFSDS=…]`：

```
[debug][炮塔] <type> tick=… AI已注册=… 目标=… 附近生物=… 通过筛选=… 能量=… 已成形=… 红石允许=… canFire=… 射程=… 弹药[HE=… APFSDS=…]
```

| 现象 | 结论 |
|---|---|
| 完全没有心跳行 | 实体没在服务端 tick（master 失效被 setDead / 实体没生成） |
| `AI已注册=false` | `initEntityAI()` 没跑到这个实例上（只应在客户端实体上出现） |
| `附近生物=0` | 扫描范围内根本没有生物（射程/位置问题） |
| `附近生物>0 但 通过筛选=0` | 全被 `isSuitableTarget` 挡掉：红石、俯仰够不着、是炮塔、或 PVP/动物开关 |
| `通过筛选>0 但 目标=无` | 扫描 AI 没在跑（检查是否被其它 mod 的 AI 任务挤掉） |
| `红石允许=false` | 基座红石模式要求信号，给信号或在 GUI 切红石模式 |
| `canFire=false` | 基座 RF 为 0，需要供电 |
| `canFire=true 但不开火` | 多半是对准/视线没过：目标移动太快或视线被挡（开火 AI 要求水平 5°、俯仰 5° 容差 + 视线可见） |
| **`弹药[HE=0 APFSDS=0]`** | **普通炮塔没弹药**：把 HE / APFSDS 放进基座弹药槽（0–8）——它只吃这两种弹，且由弹种决定打什么 |
| 日志出现 `不开火：…` | 子类 `tryFire` 被挡（0.28.1 起每 5 秒最多一条）：写明是"没弹药 / 电量不足 / 扣弹药失败" |

> **改完代码记得确认实例里的 jar 是新的**：日志时间早于 `mods/` 里 jar 的落盘时间，
> 说明这一局跑的还是旧 jar（0.27.0 那局里普通炮塔本来就还没武器，日志自然什么都没有）。

目标锁定/丢失时还会各打一行（含同样的状态字段）。
稳定后把 `DEBUG_TURRET` 改回 `false` 关闭。

---

## 12. 俯仰角（0.23.0）

### 各塔俯仰数据（由用户按模型给出）

| 炮塔 | 俯角(向下) | 仰角(向上) | 俯仰分组 | 俯仰旋转中心（Java 模型空间） |
|---|---|---|---|---|
| 激光炮塔 | 22° | 17° | `connection` | (0, 17, −1) = 分组 origin |
| 激光 Mk.1 | 40° | 不限（代码取 90°） | `turret` | (0, 10, 0) |
| 闪电炮塔 | 25° | 60° | `main` | (0, 17, 0) |
| 普通炮塔 | 25° | 50° | `gun` | (0, 15, 0) |
| BMPT | 10° | 60° | `gun`+`pitchL/R` | 各自（历史实现，独立） |
| 罗兰 | 20° | 90° | `tr` | `LAUNCH_*_CENTER` |

**限制区间**：MC 语义 pitch 正=低头、负=抬头，故限制为 `[-仰角, +俯角]`
（`EntityBaseTurret.onUpdate` 里 `MathHelper.clamp(approachAngle(...), -getPitchMaxUp(), getPitchMaxDown())`）。

### 接线方式

1. `bbmodel2java.js --pitch <组名>` 生成的模型类 `implements ITurretModel`，
   并输出 `renderTurret(entity,...,pitchDeg)`：设该组 `rotateAngleX = radians(pitchDeg)` → 渲染所有顶层组 → **复位**
2. `RenderBaseTurret` 检测到 `ITurretModel` 就调 `renderTurret`，俯仰角用**帧级补间后的 renderPitch**
   （不是 20tps 的同步值）
3. `EntityBaseTurret`：`getPitchMaxUp()/getPitchMaxDown()` 子类覆写；
   开火 AI 增加俯仰对准容差 `PITCH_ALIGN_TOLERANCE = 5°`（未对准不开火，避免光束脱离炮口指向）；
   索敌过滤增加"俯仰够不着不锁定"（同 BMPT，避免空转抖动）
4. 炮口换算 `muzzlePos(muzzlePx, pivotPx)` 的俯仰项已与模型一致：
   绕 X 轴按 `Rx(pitch)` 旋转，旋转中心 = 俯仰分组的 origin（与模型 `rotateAngleX` 的作用点相同）

### 符号约定（推导过，别反了）

渲染器有 `scale(-1,-1,1)` 镜像 → **Java 模型空间的 +Y 对应世界 −Y（下）**。
四塔烘焙后炮管都指向 Java −Z，故 `rotateAngleX = +pitch`（正=低头）会让炮口在 Java 里抬高、
在世界里降低 ✓ 与 BMPT 一致。**若某台模型又出现"组旋转"，符号会反过来**（见 §10 组旋转烘焙）。

### 激光炮塔方向修正（0.23.0）

原模型炮管朝 +Z（建反了），作者把 `base`/`connection` 两组各绕 Y 转 −180°；
转换器把该旋转烘焙进元素坐标后：炮管朝 −Z（与其它塔一致），发射端面由 bb z=−12 移到 z=−18，
炮口随之由 Java (0, 14.5, −12) 改为 **(0, 13, −18)**。
若实测激光束出射端不对，改 `EntityLaserTurret.MUZZLE_PX` 一个常量即可。

---

## 13. 炮塔升级卡（0.26.1：单一通用卡）

### 设计：一张卡，谁装谁解锁

`turret_upgrade`（`ItemTurretUpgrade`）**只有一个物品、不分子类型**。
（0.26.0 曾用 NBT `upgradeType` 分四种卡，0.26.1 取消——不需要不同版本的卡。）
把卡放进炮塔基座的升级槽后，**由当场那座炮塔自己解锁它的专属强化**：

| 炮塔 | 装了升级卡后解锁 |
|---|---|
| 激光炮塔 | 一次射出 `MULTI_BEAM_COUNT = 3` 道光束（未装 = 1 道） |
| 闪电炮塔 | 连锁目标数 4 → `CHAIN_TARGETS_UPGRADED = 8` |
| 激光炮塔 Mk.1 | 改用过载弹：射击间隔 `FIRE_COOLDOWN_OVERLOAD = 20` tick、爆炸 3.0、破坏徒手可挖的方块 |
| BMPT | 瞄准点改算移动目标提前量（`getAimPoint`） |
| 罗兰 | 同样是"有核弹 → 核导弹变体"的门控（原有逻辑不变） |
| 普通炮塔 | 无升级效果（没有武器，留待实现） |

### 判定链路

1. GUI 放入 **槽位 19 = `TurretBaseTileEntMaster.SLOT_UPGRADE`**（`SlotTurretUpgrade` 只接受 `ItemTurretUpgrade`）；
2. `EntityBaseTurret.hasUpgrade()` → `TurretBaseTileEntMaster.hasUpgrade()` → `ItemTurretUpgrade.isUpgrade(stack)`
   —— 判定就是 `instanceof ItemTurretUpgrade`，**不看 NBT**；
3. **每发实时查询** → 插卡/拔卡立即生效，不需要拆塔重建、不需要重载；
4. 基座 / GUI / 容器都不认识"具体效果"，所以**给某座炮塔加升级效果只需要改那座炮塔实体**，基座侧零改动。

### 各炮塔的效果改动点（改数值就改这些常量）

| 炮塔 | 文件 | 常量 / 分支 |
|---|---|---|
| 激光炮塔（多光束） | `EntityLaserTurret` | `MULTI_BEAM_COUNT = 3`（束数）、`MULTI_BEAM_SPACING = 0.22`（相邻光束横向间距，格）；`fireWeapon` 里 `beams = hasUpgrade() ? MULTI_BEAM_COUNT : 1`，偏移沿**水平垂线**，**每束各自重新对准目标**（不是平行光束） |
| 闪电炮塔（连锁强化） | `EntityLightningTurret` | `CHAIN_TARGETS_UPGRADED = 8`；未升级时用 `CHAIN_TARGETS = TeslaProjectile.CHAIN_TARGETS`（上游 = 4），后续跳伤害沿用 techguns `CHAIN_DAMAGE_FACTOR = 0.75` |
| 激光 Mk.1（过载） | `EntityLaserTurretMark1` | `FIRE_COOLDOWN_OVERLOAD = 20`（tick，覆盖 `getFireCooldown()`）；`POWER_PER_SHOT = 100` RF 不变；`fireWeapon` 里按 `hasUpgrade()` 选弹种 |
| BMPT / 罗兰（提前量） | `EntityBMPTTurret.getAimPoint` / `EntityRolandTurret` | `master.hasUpgrade()` 为真才算提前量；罗兰另用它门控核弹变体 |

### 弹种差异（命中效果）

| 弹种（`entities/projectiles/`） | 爆炸强度 | 是否破坏方块 | 用在哪 |
|---|---|---|---|
| `TurretLaserBeamProjectile` | `HIT_EXPLOSION_STRENGTH = 1.5F` | 否（`isSmashing=false`、不点火） | 激光炮塔、激光 Mk.1 普通模式 |
| `TurretOverloadLaserBeamProjectile` | `OVERLOAD_EXPLOSION_STRENGTH = 3.0F` | **是**：命中原点 3×3 内硬度 ≤ `SMASH_MAX_HARDNESS = 1.0F` 的非流体方块（徒手可挖：泥土/沙子/树叶/玻璃…） | 激光 Mk.1 + 升级卡 |

- 爆炸一律用 `world.newExplosion(shooter, x, y, z, strength, /*flaming=*/false, /*smashing=*/false)`；
  方块的破坏**单独由 `smashSoftBlocks()` 做**（`destroyBlock(pos, false)`，逐个判定硬度），
  所以爆炸强度调大不会顺带炸掉石头/基岩，也不会点火。
- `getHitExplosionStrength()` / `shouldSmashBlocks()` 是**虚方法且都是常量返回**，
  可以安全地在 `LaserProjectile`（hitscan，构造函数里就 trace）**构造期间**被调用。
- `smashSoftBlocks()` 扫的是**命中方块周围 3×3×3**（`pos.add(-1..1)`），`hardness < 0`（基岩等）
  与流体直接跳过 —— 想扩大/缩小范围改那里面的循环半径。

### 给某座炮塔新增/修改升级效果

1. 在该炮塔实体里调 `hasUpgrade()` 分支（`fireWeapon()` / `getFireCooldown()` / `getAimPoint()` 等）；
2. 数值提成 `public static final` 常量，方便调参；
3. **物品、语言键、创造栏都不用动**：卡是通用的，`ItemTurretUpgrade` 已通过 `setCreativeTab`
   自动进入本模组物品栏（同一物品不会重复显示）；
4. 若效果需要新弹种：新增实体（`ModEntities` 下一个空闲 id ≥ 3020）+ `ClientProxy` 渲染器 + 弹药/图纸逻辑。

### 兼容性

- **0.26.0 的旧卡依旧有效**：判定是 `instanceof`，旧卡上的 `upgradeType` NBT 被忽略；
- 升级卡判定完全不涉及弹药：**弹药槽（0–17）四座新炮塔都不消耗**，只吃 RF
  （`POWER_PER_SHOT`：激光 50 / Mk.1 100 / 闪电 60），能量不足一律不开火。
  （例外：**普通炮塔**用的就是弹药槽里的 HE / APFSDS，见 §14。）

---

## 14. 普通炮塔的弹药：HE 与 APFSDS（0.27.0）

### 清单

| 项目 | HE 高爆弹 | APFSDS 穿甲弹 |
|---|---|---|
| 物品注册名 | `he_shell` | `apfsds_shell` |
| 物品类 | `ItemTurretShell`（一个类带两种弹药） | 同左 |
| 弹丸实体 | `HEShellProjectile`（id **3020**） | `APFSDSShellProjectile`（id **3021**） |
| 渲染器 | `RenderHEShellProjectile` | `RenderAPFSDSShellProjectile` |
| 模型类 | `client/models/projectiles/ModelHEShell` | `.../ModelAPFSDSShell` |
| 贴图 | `textures/entities/he_shell.png`（32²，模型 UV 图集） | `textures/entities/apfsds_shell.png` |
| 物品贴图 | `textures/items/he_shell.png`（16²，斜置炮弹图标） | `textures/items/apfsds_shell.png` |
| 蓝图 | 通用复制机可生产（创造栏已给蓝图） | 同左 |

模型源文件在工作区 `models/ammo/HE/`、`models/ammo/APFSDS/`（Blockbench `.bbmodel` + 32×32 贴图）。

### 行为差异（这就是"两种弹药"的全部意义）

| | HE | APFSDS |
|---|---|---|
| 命中 | **就地爆炸** | **不爆炸** |
| 对单体 | 直击伤害 + 主半径满伤 | 单体伤害很高 + **护甲穿透拉满** |
| 对群体 | **实体杀伤半径很大**（6 格内递减） | 沿弹道**穿透多个生物**（默认 4 个） |
| 方块 | 不破坏（`BLOCK_DAMAGE_FACTOR = 0`） | **击穿普通方块**（硬度 ≤ 3，默认最多 6 个，**不掉落**） |
| 弹道 | **有下坠**（抛物线） | 几乎平直 |

### HE 的关键参数（`HEShellProjectile`）

- `PRIMARY_RADIUS = 2.0`、`PRIMARY_DAMAGE = 40`：主半径内满伤
- `SECONDARY_RADIUS = 5.0`、`SECONDARY_DAMAGE = 6`：次级半径内**线性递减**——**5 格杀伤半径**（0.28.6 由 6 改成 5，用户指定）
- `BLOCK_DAMAGE_FACTOR = 0`：0 = 只炸生物不破方块；想炸出弹坑调到 0.05~0.15
- `GRAVITY = 0.01`：**下坠量 ≈ GRAVITY × t² / 2**（t = 飞行 tick）；0.01 时 20 tick（≈100 格）掉约 2 格
- 爆炸伤害走**科技枪的 `TGExplosion`**（不是原版 `Explosion`）：
  `new TGExplosion(world, shooter, this, x, y, z, 主伤, 次伤, 主半径, 次半径, 破方块系数)`，
  它对每个候选生物做**视线检测**（躲在墙后不受伤），并且正好表达"小半径满伤 + 大半径递减"。
- **爆炸外观（0.28.6 起 = 火箭弹那一套）**：
  1. 科技枪 `PacketSpawnParticle("RocketExplosion")`——冲击波 + 地面冲击波 + 大烟 + 火球 + 冲击环，
     与 Roland 导弹**同一行调用**；
  2. 原版粒子大爆团兜底（`EXPLOSION_HUGE ×2` + `EXPLOSION_LARGE ×8` + `FLAME ×12` +
     `SMOKE_LARGE ×16` + `SMOKE_NORMAL ×24`），走 `util/ModParticles` →
     WorldServer longDistance 广播（512 格内都看得见）。**科技枪 FX 包不保证渲染**（0.27 的教训），
     所以原版粒子必须有；
  3. 原版爆炸音效 `SoundEvents.ENTITY_GENERIC_EXPLODE`（音量 4）。
- 伤害**只由 `TGExplosion` 结算**（主 2 格满伤 → 5 格线性衰减 + 视线遮挡判定，`doExplosion(false)`）。
  注意**不要**再叠一个原版 `Explosion` 当视觉：那会变成两份伤害、两次音效
  ——0.28.3 曾经那样做过，0.28.6 改为上面这套火箭配方。

### APFSDS 的关键参数（`APFSDSShellProjectile`）

- `MAX_BLOCK_HARDNESS = 3.0`：石头 1.5 / 圆石 2.0 / 木板 2.0 穿得过；铁块 5.0 / 黑曜石 50 打不穿
- `MAX_BLOCKS_BROKEN = 6`、`MAX_ENTITIES_PIERCED = 4`：每发的穿透上限（防止一发打穿整座山/一条街的怪）
- `ARMOR_PENETRATION = 1.0`：完全无视护甲（`TGDamageSource.armorPenetration`）
- `DROP_BROKEN_BLOCKS = false`：击穿的方块**不掉落**（否则会被当成刷矿手段）
- `GRAVITY = 0.0`：直线弹道；想让它也下坠就改这个值

**两个"必须重写父类"的地方**（不能只覆盖小钩子）：

1. `onHit`：父类 **打中什么都会 `setDead()`**，这正是"穿透"的反面；本类重写为
   打中生物只结算伤害并继续飞、打中方块能穿就破坏继续飞。
2. `findEntityOnPath`：父类永远返回**最近**的生物，已穿透的目标会一直挡在弹道上；
   本类重写并**跳过 `piercedEntityIds` 里的实体 id**。

客户端在这两类里**什么都不做**（`onHit` 开头 `if (world.isRemote) return;`）：世界修改只由服务端做。

### 渲染：模型来自 Blockbench，有两个坑

`RenderTurretShellProjectile`（通用基类）+ 两个子类。要点：

1. **yaw 用 `+90`，不是科技枪的 `−90`**：`.bbmodel` 经 `bbmodel2java.js` 转换时
   `A(x,y,z) = (−x, 24−y, z)` 把 **X 轴翻了过来**，炮弹头部落在 Java 空间的 **−X**；
   科技枪自己的模型头朝 +X，所以它用 −90。改这一行前先想清楚。
2. **模型中心要平移到实体原点**：两种弹药都是在 Blockbench 地面上建模的，
   转换后整体位于 y ≈ 21.5~24.5（中心 `MODEL_CENTER_Y_PX = 23`），不平移的话弹丸会飘在实体上方一格多。
   顺序是**先平移（含居中）再旋转**，这样弹丸绕自己的中心摆头。
3. `MODEL_SCALE = 0.9`（模型 15~16 单位 ≈ 0.85 格长），改大小只动这一个常量。

> **工具修复（0.27.0）**：`bbmodel2java.js` 以前对**顶层裸元素**（模型没有 groups，
> outliner 直接列元素）只做 `boxLines`，**丢掉了元素自身的旋转** —— APFSDS 的尾翼在
> Blockbench 里是"零厚度平面 + 绕 X 转 ±45°"，结果被压成了没有角度的平板。
> 现在顶层裸元素统一收进一个隐式组（origin `[0,0,0]`）走 `emitRenderer`，旋转会照抄成
> `setRotationAngle`。已回归验证：带组的老模型（BMPT / 罗兰 / 普通炮塔）输出**零差异**。

### 通用复制机怎么生产（`UniversalCopierTileEnt`）

| 弹药 | RF/tick | 时长 | 每次产出 | 合计 |
|---|---|---|---|---|
| HE 高爆弹 | 120 | 40 t | **16 发** | 4800 RF |
| APFSDS 穿甲弹 | 150 | 60 t | **8 发** | 9000 RF |

- 三个参数在 `getNeededPower()` / `getOutputCount()` / `getCraftTime()` 里按物品分流，
  判定方法是 `isHEShell()` / `isAPFSDSShell()`（比较 `stack.getItem() == ModItems.heShell / apfsdsShell`）。
- 蓝图**不用做配方**：`ItemBlueprint.createBlueprint(new ItemStack(...))` 在创造栏里直接给，
  玩家把蓝图放进复制机的蓝图槽、点"注册"就解锁了。

### 再加一种弹药要动哪些地方（照抄这张表）

1. `ItemTurretShell("xxx_shell")` 注册进 `ModItems`（物品 + 模型分流已经按注册名自动处理）；
2. 弹丸类（继承 `GenericProjectile` 或复用上面两者之一）；
3. `ModEntities` 下一个空闲 id（**3022**）+ `ClientProxy` 注册渲染器；
4. 模型：`bbmodel2java.js → client/models/projectiles/`，贴图丢 `textures/entities/`，物品图标丢 `textures/items/`；
5. `models/item/<name>.json`（`item/generated` + `layer0`）；
6. `lang` 两种语言各写 `item.tgceaddon.<name>.name` + `.tip` ~ `.tip4`（tooltip 读的就是这几个键）；
7. `ModCreativeTabs` 加一行蓝图 + `UniversalCopierTileEnt` 加三个参数分流。

---

## 15. 普通炮塔的武器逻辑（0.28.0）

`EntityTurret` 从"只是模型"变成了一座真正的坦克炮塔：**两用主炮，打 HE 还是 APFSDS 由弹药栏决定**。

### 硬参数（都在 `EntityTurret` 里）

| 常量 | 值 | 说明 |
|---|---|---|
| `ATTACK_RANGE` | 64 格 | 索敌/开火射程 |
| `FIRE_COOLDOWN` | **80 tick = 4 秒** | 射击间隔（基类开火 AI 用它做冷却） |
| `AIM_SETTLE_TICKS` | **5 tick** | **开火前"瞄准保持"时间**（0.28.2）：对准+可见+通视连续满足 5 tick 才开炮，去掉"甩狙"观感。实现是基类的 `getAimSettleTicks()`（默认 0，只本塔覆写 → 不影响其它炮塔射速） |
| `POWER_PER_SHOT` | 10 RF | 每发耗电（与 BMPT 机炮同口径；基座没电不开火） |
| `MUZZLE_PX` | (0, 14, −58) | 炮口（Java 模型空间像素） |
| `PIVOT_PX` | (0, 15, 0) | 俯仰分组 `gun` 的旋转点 |
| 俯仰限幅 | 俯下 25° / 仰上 50° | 用户给定 |

### 弹种选择（关键行为）

```java
boolean hasApfsds = master.hasAmmo(new ItemStack(ModItems.apfsdsShell));
boolean hasHe     = master.hasAmmo(new ItemStack(ModItems.heShell));
if (!hasApfsds && !hasHe) return false;   // 弹药栏里没有本炮塔的弹药 → 不开火
boolean useApfsds = hasApfsds;            // 两种都有 → 优先穿甲弹
```

- **弹种完全由弹药栏（槽位 0–8）里实际有的东西决定**，不是卡片/配置选的；
- 两种都有时优先 APFSDS（单体伤害更高）；想改成"HE 优先"就改 `useApfsds = hasApfsds` 这一行；
- 顺序：**先查弹药 → 再扣电 → 再扣弹药**（历史坑：先扣电会造成"没弹药时每 tick 白耗电"，
  见 §7 的罗兰条目）。

### 开火流程（照抄代码顺序，别调换）

1. `muzzlePos(MUZZLE_PX, PIVOT_PX)` 求炮口世界坐标（含俯仰旋转）；
2. `aimPointOf(target)` 求瞄准点；**HE 还要弹道超升** `raiseAimForDrop(...)`：
   抬高 `0.5 × GRAVITY × (距离/弹速)²`（HE 有下坠，不补就整体打低，64 格约低 1.3 格）；
3. `aimAnglesFrom(炮口, 瞄准点)` 得到 **从炮口看目标**的 yaw/pitch
   （不逐炮口算就会"整根管子偏出目标"，见 §11 的命中率条目）；
4. **临时**把 `rotationYawHead`/`rotationPitch` 设成这对角度 → `new HEShellProjectile/APFSDSShellProjectile(...)`
   → 立刻复位：科技枪 `GenericProjectile` 的构造器读这两个字段决定初速方向，
   而渲染用的是补间角，不复位会让炮管方向闪一帧；
5. `shell.setPosition(炮口)`（构造器给的起点是"射手眼睛"，必须改成炮口）+ `world.spawnEntity`；
6. 炮口烟 + 炮响。

### 炮口烟 / 爆炸视觉：**统一按 BMPT 机炮炮弹的做法**（0.28.3）

> **为什么统一**：0.27~0.28.0 的炮口烟与爆炸都走科技枪 FX 包
> （`PacketSpawnParticle("HandCannonMuzzleSmoke" / "MiningChargeBlockExplosion")`），
> 名字在 techguns 的 `fxlist.txt` 里确实存在，但实测在本整合包里**发了看不到**；
> 0.28.2 又改成服务端广播原版粒子（WorldServer 的 longDistance 重载，512 格），仍然与 BMPT 不是一套。
> 0.28.3 起**统一到 BMPT 机炮炮弹的实现**（`CannonShellProjectile`）——那套是实测可见的。

| 效果 | 做法（与 `CannonShellProjectile` 一致） |
|---|---|
| 炮口烟 | **弹丸实体自己在客户端生成**（`world.isRemote` + 首个 tick 一次），原版粒子；再配 `EntityMuzzleLight.spawnFlash` 一发 OptiFine 动态光源 |
| 弹道尾迹 | 客户端每 tick 补烟（高速弹丸在移动线段上插值，见下） |
| 爆炸（HE） | **另走火箭弹配方**（0.28.6）：科技枪 `RocketExplosion` 特效包 + 原版粒子大爆团兜底 + 原版爆炸音效；伤害只由 `TGExplosion` 结算（5 格杀伤半径）。详见 §14 |

实现在两处：

- `TurretShellProjectile`（HE/APFSDS 的公共基类）：炮口烟 + 尾迹 + 下坠（`getShellGravity()`）。
- `HEShellProjectile.explodeAt(...)`：原版 Explosion + TGExplosion 分工如上。

**普通炮塔炮口烟的调参常量（`TurretShellProjectile`）**——按"寿命更短、数量更多、范围更大、前冲更快"调过：

| 常量 | 值 | 说明 |
|---|---|---|
| `MUZZLE_PUFF_COUNT` | 26 | `EXPLOSION_NORMAL` 短命白爆烟（主力；BMPT 只有 4） |
| `MUZZLE_SMOKE_LARGE` | 10 | `SMOKE_LARGE` 大烟团 |
| `MUZZLE_SMOKE_NORMAL` | 8 | `SMOKE_NORMAL` 小烟点（寿命最长，所以最少） |
| `MUZZLE_FORWARD` | 1.6 | 沿炮管方向的前冲速度（格/tick）；BMPT 是 0.5 |
| `MUZZLE_SPREAD` | 0.9 | 横向扩散速度（格/tick）；BMPT 是 0.3 |
| `MUZZLE_JITTER` | 0.18 | 起点在炮口附近的抖动（格）：粒子多了要一起放开，否则糊成一坨 |
| `TRAIL_COUNT` | 3 | 每 tick 在"上一 tick → 本 tick"线段上插几颗尾迹烟 |

一发炮口烟 = 26 + 10 + 8 = **44 颗**（0.28.3 是 24 颗，BMPT 机炮是 5 颗）；
4 秒才一发，客户端开销可忽略。想更浓就继续加 `MUZZLE_PUFF_COUNT`。

> **"烟寿命更短"怎么实现的**：原版粒子寿命写死在粒子类里，`spawnParticle` 传不了。
> 三类烟的平均寿命：`EXPLOSION_NORMAL`（约 4~40 tick）< `SMOKE_LARGE` ≈ `SMOKE_NORMAL`（约 8~40 tick）。
> 所以"更短"是靠**比例**：以 EXPLOSION_NORMAL 为主、长寿命的 SMOKE_NORMAL 只留 4 颗。
> 想更短就继续调小 `MUZZLE_SMOKE_NORMAL`、调大 `MUZZLE_PUFF_COUNT`。
>
> **前冲速度注意**：方向取的是出膛方向的**单位向量**，不能乘整个速度——
> 本模组炮弹 4~6 格/tick，乘上去烟会瞬间飞出视野（BMPT 机炮只有 1 格/tick，所以它敢写 `motion * 0.5`）。
>
> **尾迹为什么要插值**：BMPT 机炮 1 格/tick，每 tick 一颗烟点就够；本模组炮弹 4~6 格/tick，
> 单点会画成断断续续的虚线，所以在移动线段上插 3 颗。

- 声：`ModSounds.CANNON_FIRE`（BMPT 机炮同款，音量 5、音调 0.9~1.1 随机）。

### 想调手感改哪里

| 想要的效果 | 改什么 |
|---|---|
| 射速快/慢 | `EntityTurret.FIRE_COOLDOWN`（tick） |
| **开炮前"停稳"多久** | `EntityTurret.AIM_SETTLE_TICKS`（默认 5 tick；0 = 回到"进容差就打"） |
| 打得准不准 | `APFSDS_SPREAD` / `HE_SPREAD`（0 = 无散布） |
| 伤害/弹速/存活 | `HEShellProjectile.RECOMMENDED_*`、`APFSDSShellProjectile.RECOMMENDED_*` |
| 下坠补偿开关 | `EntityTurret.COMPENSATE_HE_DROP` |
| 炮口烟大小/数量/前冲 | `TurretShellProjectile`: `MUZZLE_PUFF_COUNT` / `MUZZLE_SMOKE_LARGE` / `MUZZLE_SMOKE_NORMAL` / `MUZZLE_FORWARD` / `MUZZLE_SPREAD` / `TRAIL_COUNT` |
| HE 爆炸观感 | `HEShellProjectile`: `FX_HUGE` / `FX_LARGE` / `FX_FLAME` / `FX_SMOKE_LARGE` / `FX_SMOKE_NORMAL` / `FX_SPREAD`（原版爆团兜底）+ 切换科技枪 `RocketExplosion` 特效包 |
| HE 杀伤半径 | `HEShellProjectile.SECONDARY_RADIUS`（当前 5 格） |
| 每发耗电 | `POWER_PER_SHOT`（设 0 就是"只要有电就能一直打"，因为基类 `canFire()` 要求能量 > 0） |

> **升级卡**：普通炮塔目前**没有专属升级效果**（升级卡对它无作用）。
> 若以后要给它加（例如像 BMPT 那样用 `hasUpgrade()` 开提前量计算），
> 只需在 `tryFire` 里加一个 `hasUpgrade()` 分支，并补 §13 的说明与语言键。


