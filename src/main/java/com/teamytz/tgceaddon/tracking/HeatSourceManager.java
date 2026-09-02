package com.teamytz.tgceaddon.tracking;

import com.teamytz.tgceaddon.TGCEAddon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import techguns.entities.projectiles.RocketProjectile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 热信号(红外热源)管理器
 *
 * 预设功能:当玩家使用鞘翅喷气背包加速飞行时,会被标记为"热源",
 * 产生红外热信号。停止加速后热信号强度逐 tick 衰减直至消失。
 *
 * 方块热源:热源测试方块(带 TileEntity)会持续注册自身坐标,
 * 红外导弹可锁定这些坐标(与热源玩家同等优先级,取最近者)。
 *
 * 未来的红外热追踪导弹通过本类的查询接口锁定热源:
 *   - isHeatSource(Entity)   : 判断实体是否为热源
 *   - getHeatLevel(player)   : 获取玩家热信号强度(0~1)
 *   - getHeatSources(World)  : 获取某世界的所有热源玩家
 *   - getHeatBlocks(World)   : 获取某世界的所有方块热源坐标(存活期内)
 *
 * 仅在服务端维护(热追踪导弹在服务端工作)。
 */
@Mod.EventBusSubscriber(modid = TGCEAddon.MODID)
public class HeatSourceManager {

    /** 热信号满强度 */
    public static final float MAX_HEAT = 1.0f;
    /** 停止加速后每 tick 衰减量:0.01/tick → 满信号残留 100 tick = 5 秒
     *  (原 0.02/tick 只有 2.5 秒,高速目标容易错过锁定窗口) */
    public static final float DECAY_PER_TICK = 0.01f;
    /** 方块热源条目有效时长(tick):超过则视为失效(方块被卸载/破坏后自动过期) */
    public static final long BLOCK_HEAT_TIMEOUT = 100;

    // ===== 热值模型(导弹/发射器观感) =====
    /** 玩家满热源热值(喷气背包加速后) */
    public static final float PLAYER_MAX_HEAT = 100.0f;
    /** 末影水晶基础热值(比玩家更热,可锁定距离更远) */
    public static final float END_CRYSTAL_HEAT = 120.0f;
    /** 科技枪导弹/火箭基础热值(可被红外锁定与拦截) */
    public static final float MISSILE_HEAT = 70.0f;
    /** 热诱弹热值:远高于玩家(满热 100)/导弹(70),红外导弹会优先追踪而被骗走 */
    public static final float FLARE_HEAT = 200.0f;
    /** 每格距离热值衰减:距离 100 格时热值降到 50 */
    public static final float HEAT_DROP_PER_BLOCK = 0.5f;
    /** 可锁定最低热值:超过此值才能被锁定((100-25)/0.5 = 150 格为玩家最大锁定距离) */
    public static final float MIN_LOCK_HEAT = 25.0f;
    /** 方块热源恒定热值(热源测试方块,不随距离衰减) */
    public static final float BLOCK_HEAT = 100.0f;

    /** 玩家 -> 热信号强度(弱引用,玩家对象消失自动清理) */
    private static final WeakHashMap<EntityPlayer, Float> HEAT_LEVELS = new WeakHashMap<>();

    /** 方块热源:World(弱引用) -> (方块坐标 -> 最后刷新 tick) */
    private static final WeakHashMap<World, Map<BlockPos, Long>> HEAT_BLOCKS = new WeakHashMap<>();

    private HeatSourceManager() {
    }

    /** 标记玩家为热源(强度拉满),由鞘翅喷气背包加速时调用(服务端) */
    public static void markHeatSource(EntityPlayer player) {
        HEAT_LEVELS.put(player, MAX_HEAT);
    }

    /** 清除玩家的热信号 */
    public static void clearHeatSource(EntityPlayer player) {
        HEAT_LEVELS.remove(player);
    }

    /** 判断实体是否当前是热源 */
    public static boolean isHeatSource(Entity entity) {
        return entity instanceof EntityPlayer && HEAT_LEVELS.containsKey((EntityPlayer) entity);
    }

    /** 获取玩家剩余热信号强度(0~1,衰减前剩余量),非热源返回 0 */
    public static float getHeatLevel(EntityPlayer player) {
        Float level = HEAT_LEVELS.get(player);
        return level != null ? level : 0.0f;
    }

    /**
     * 计算玩家热源在观察者(发射器/导弹)眼中的可见热值。
     *
     * 热值 = 剩余信号强度 × (100 − 距离 × 0.5):
     * - 满热源玩家(刚加速)= 100 热值
     * - 距离每 +1 格,热值 −0.5;距离 100 格时热值剩 50
     * - 热值 ≤ 0 或玩家已不是热源 → 0
     */
    public static float getHeatLevel(EntityPlayer player, double obsX, double obsY, double obsZ) {
        Float residual = HEAT_LEVELS.get(player);
        if (residual == null || residual <= 0.0f) {
            return 0.0f;
        }
        double dx = player.posX - obsX;
        double dy = player.posY - obsY;
        double dz = player.posZ - obsZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        float base = PLAYER_MAX_HEAT - (float) dist * HEAT_DROP_PER_BLOCK;
        if (base <= 0.0f) {
            return 0.0f;
        }
        return base * residual;
    }

    /** 方块热源可见热值(恒定满值,测试用) */
    public static float getBlockHeat() {
        return BLOCK_HEAT;
    }

    // ===== 实体热源(末影水晶/科技枪导弹/配置实体) =====

    /** 配置热值表:实体 ID(小写,可省略命名空间) -> 红外热值 */
    private static final Map<String, Float> CONFIG_ENTITY_HEAT = new HashMap<>();

    /**
     * 从配置文件加载实体红外热值表(启动时由 ConfigHandler 调用;
     * 配置文件改动需重启生效)。格式:modid:entityname:热值 或 entityname:热值
     */
    public static void loadConfigEntityHeat() {
        CONFIG_ENTITY_HEAT.clear();
        for (String entry : com.teamytz.tgceaddon.config.ConfigHandler.entityHeatValues) {
            if (entry == null) {
                continue;
            }
            String[] parts = entry.trim().split(":");
            String id = null;
            float heat = 0.0f;
            try {
                if (parts.length >= 3) {
                    id = (parts[0] + ":" + parts[1]).toLowerCase();
                    heat = Float.parseFloat(parts[2]);
                } else if (parts.length == 2) {
                    id = parts[0].toLowerCase();
                    heat = Float.parseFloat(parts[1]);
                }
            } catch (NumberFormatException ignored) {
                continue;
            }
            if (id != null && !id.isEmpty()) {
                CONFIG_ENTITY_HEAT.put(id, heat);
            }
        }
        if (TGCEAddon.DEBUG_IR) {
            TGCEAddon.getLogger().info("[IR] 配置实体热值 " + CONFIG_ENTITY_HEAT.size() + " 条"
                    + " 默认热值=" + com.teamytz.tgceaddon.config.ConfigHandler.defaultEntityHeat);
        }
    }

    /** 实体基础热值(未随距离衰减):配置表优先,其次热诱弹 200 / 末影水晶 120 / 科技枪导弹 70,最后配置默认值 */
    public static float getEntityBaseHeat(Entity entity) {
        Float cfgHeat = matchConfigHeat(getEntityId(entity));
        if (cfgHeat != null) {
            return cfgHeat;
        }
        if (entity instanceof com.teamytz.tgceaddon.entities.EntityFlare) {
            return FLARE_HEAT;
        }
        if (entity instanceof EntityEnderCrystal) {
            return END_CRYSTAL_HEAT;
        }
        if (entity instanceof RocketProjectile) {
            return MISSILE_HEAT;
        }
        return com.teamytz.tgceaddon.config.ConfigHandler.defaultEntityHeat;
    }

    /** 实体注册 ID(modid:entityname);未注册返回空字符串 */
    private static String getEntityId(Entity entity) {
        net.minecraft.util.ResourceLocation key = net.minecraft.entity.EntityList.getKey(entity.getClass());
        return key != null ? key.toString() : "";
    }

    /** 配置热值查找(允许省略命名空间:blaze 可匹配 minecraft:blaze) */
    private static Float matchConfigHeat(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        String lower = id.toLowerCase();
        Float direct = CONFIG_ENTITY_HEAT.get(lower);
        if (direct != null) {
            return direct;
        }
        int idx = lower.indexOf(':');
        if (idx >= 0) {
            return CONFIG_ENTITY_HEAT.get(lower.substring(idx + 1));
        }
        return null;
    }

    /**
     * 实体热源在观察者眼中的可见热值:
     * - 玩家:剩余信号 × (100 − 距离 × 0.5)
     * - 末影水晶/科技枪导弹:基础热值 − 距离 × 0.5(恒定,不随时间衰减)
     * 非热源返回 0。
     */
    public static float getEntityHeat(Entity entity, double obsX, double obsY, double obsZ) {
        if (entity instanceof EntityPlayer) {
            return getHeatLevel((EntityPlayer) entity, obsX, obsY, obsZ);
        }
        float base = getEntityBaseHeat(entity);
        if (base <= 0.0f) {
            return 0.0f;
        }
        double dx = entity.posX - obsX;
        double dy = entity.posY - obsY;
        double dz = entity.posZ - obsZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        float heat = base - (float) dist * HEAT_DROP_PER_BLOCK;
        return Math.max(0.0f, heat);
    }

    // ===== 统一锥体扫描 =====

    /** 热源扫描结果:实体(玩家/末影水晶/导弹)或热源方块(二者其一为 null) */
    public static class HeatTarget {
        public final Entity entity;
        public final BlockPos block;
        public final double heat;
        public final double dist;

        public HeatTarget(Entity entity, BlockPos block, double heat, double dist) {
            this.entity = entity;
            this.block = block;
            this.heat = heat;
            this.dist = dist;
        }

        public boolean isBlock() {
            return this.block != null;
        }
    }

    /**
     * 在观察者前方锥体内扫描热值最高的热源(玩家/末影水晶/科技枪导弹/热源方块)。
     * 热值必须"超过" MIN_LOCK_HEAT;同热值取更近者。
     *
     * @param obsX/obsY/obsZ 观察者位置(发射器持枪者眼睛 / 导弹弹体)
     * @param coneDir        锥体轴线(已归一化)
     * @param cosLimit       锥体半角余弦
     * @param maxRange       距离预筛上限(热值检查为最终判定)
     * @param ignoreEntity   忽略的实体(如导弹自身)
     * @param excludeShooter 排除的玩家(如射手自己,可为 null)
     * @param requireLOS     实体目标是否需要视线可达(发射器 true / 导弹寻的 false)
     */
    public static HeatTarget findHottestInCone(World world, double obsX, double obsY, double obsZ,
            Vec3d coneDir, double cosLimit, double maxRange, Entity ignoreEntity, EntityPlayer excludeShooter,
            boolean requireLOS) {
        HeatTarget best = null;

        // 1. 热源玩家
        for (EntityPlayer p : getHeatSources(world)) {
            if (p == excludeShooter || p == ignoreEntity || p.isDead) {
                continue;
            }
            Vec3d to = new Vec3d(p.posX - obsX, p.posY + p.getEyeHeight() - obsY, p.posZ - obsZ);
            double dist = to.lengthVector();
            if (dist > maxRange) {
                continue;
            }
            to = to.normalize();
            if (coneDir.dotProduct(to) < cosLimit) {
                continue;
            }
            if (requireLOS && !isLineOfSight(world, obsX, obsY, obsZ, p.posX, p.posY + p.getEyeHeight(), p.posZ)) {
                continue;
            }
            double heat = getHeatLevel(p, obsX, obsY, obsZ);
            if (heat > MIN_LOCK_HEAT && isBetter(best, heat, dist)) {
                best = new HeatTarget(p, null, heat, dist);
            }
        }

        // 2. 末影水晶 / 科技枪导弹
        for (Entity e : world.loadedEntityList) {
            if (e == ignoreEntity || e == excludeShooter || e.isDead) {
                continue;
            }
            float base = getEntityBaseHeat(e);
            if (base <= 0.0f) {
                continue;
            }
            double tx = e.posX;
            double ty = e.posY + e.height * 0.5D;
            double tz = e.posZ;
            Vec3d to = new Vec3d(tx - obsX, ty - obsY, tz - obsZ);
            double dist = to.lengthVector();
            if (dist > maxRange) {
                continue;
            }
            to = to.normalize();
            if (coneDir.dotProduct(to) < cosLimit) {
                continue;
            }
            if (requireLOS && !isLineOfSight(world, obsX, obsY, obsZ, tx, ty, tz)) {
                continue;
            }
            double heat = base - dist * HEAT_DROP_PER_BLOCK;
            if (heat > MIN_LOCK_HEAT && isBetter(best, heat, dist)) {
                best = new HeatTarget(e, null, heat, dist);
            }
        }

        // 3. 热源方块(恒定热值,不要求视线)
        for (BlockPos bp : getHeatBlocks(world)) {
            double tx = bp.getX() + 0.5D;
            double ty = bp.getY() + 0.5D;
            double tz = bp.getZ() + 0.5D;
            Vec3d to = new Vec3d(tx - obsX, ty - obsY, tz - obsZ);
            double dist = to.lengthVector();
            if (dist > maxRange) {
                continue;
            }
            to = to.normalize();
            if (coneDir.dotProduct(to) < cosLimit) {
                continue;
            }
            double heat = BLOCK_HEAT;
            if (heat > MIN_LOCK_HEAT && isBetter(best, heat, dist)) {
                best = new HeatTarget(null, bp, heat, dist);
            }
        }

        return best;
    }

    private static boolean isBetter(HeatTarget current, double heat, double dist) {
        if (current == null) {
            return true;
        }
        if (heat > current.heat + 0.001D) {
            return true;
        }
        return Math.abs(heat - current.heat) <= 0.001D && dist < current.dist;
    }

    private static boolean isLineOfSight(World world, double x1, double y1, double z1,
            double x2, double y2, double z2) {
        RayTraceResult rtr = world.rayTraceBlocks(new Vec3d(x1, y1, z1), new Vec3d(x2, y2, z2), false, true, false);
        return rtr == null || rtr.typeOfHit != RayTraceResult.Type.BLOCK;
    }

    /** 获取指定世界的所有热源玩家 */
    public static List<EntityPlayer> getHeatSources(World world) {
        List<EntityPlayer> result = new ArrayList<>();
        for (Map.Entry<EntityPlayer, Float> entry : HEAT_LEVELS.entrySet()) {
            EntityPlayer player = entry.getKey();
            if (player != null && !player.isDead && player.world == world) {
                result.add(player);
            }
        }
        return result;
    }

    // ===== 方块热源 =====

    /** 注册方块热源(由热源方块的 TileEntity 每 tick 刷新,服务端) */
    public static void registerHeatBlock(World world, BlockPos pos) {
        if (world == null || pos == null) {
            return;
        }
        Map<BlockPos, Long> map = HEAT_BLOCKS.get(world);
        if (map == null) {
            map = new HashMap<>();
            HEAT_BLOCKS.put(world, map);
        }
        boolean first = map.isEmpty();
        map.put(pos, world.getTotalWorldTime());
        if (TGCEAddon.DEBUG_IR && first) {
            TGCEAddon.getLogger().info("[IR] 热源方块注册 @ (" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")");
        }
    }

    /** 注销方块热源(方块破坏/卸载时,服务端) */
    public static void unregisterHeatBlock(World world, BlockPos pos) {
        if (world == null || pos == null) {
            return;
        }
        Map<BlockPos, Long> map = HEAT_BLOCKS.get(world);
        if (map != null) {
            boolean contained = map.remove(pos) != null;
            if (map.isEmpty()) {
                HEAT_BLOCKS.remove(world);
            }
            if (TGCEAddon.DEBUG_IR && contained) {
                TGCEAddon.getLogger().info("[IR] 热源方块注销 @ (" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")");
            }
        }
    }

    /** 获取指定世界的所有方块热源坐标(仅存活期内,即方块仍被加载刷新) */
    public static List<BlockPos> getHeatBlocks(World world) {
        Map<BlockPos, Long> map = HEAT_BLOCKS.get(world);
        if (map == null || map.isEmpty()) {
            return Collections.emptyList();
        }
        long now = world.getTotalWorldTime();
        List<BlockPos> result = new ArrayList<>();
        for (Map.Entry<BlockPos, Long> entry : map.entrySet()) {
            if (now - entry.getValue() < BLOCK_HEAT_TIMEOUT) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /** 服务端每 tick:热信号衰减 */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Iterator<Map.Entry<EntityPlayer, Float>> it = HEAT_LEVELS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<EntityPlayer, Float> entry = it.next();
            EntityPlayer player = entry.getKey();
            if (player == null || player.isDead) {
                it.remove();
                continue;
            }
            float level = entry.getValue() - DECAY_PER_TICK;
            if (level <= 0.0f) {
                it.remove();
            } else {
                entry.setValue(level);
            }
        }
    }
}
