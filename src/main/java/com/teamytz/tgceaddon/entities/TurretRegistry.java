package com.teamytz.tgceaddon.entities;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 炮塔类型注册表：卡片 NBT "turretType" → 炮塔实体工厂。
 *
 * 背景：原先 {@code TurretBaseTileEntMaster} 里是硬编码 if-else 判断类型
 * （"roland" → 罗兰，其余 → BMPT），而且"世界重载后按 UUID 找回炮塔"那处还写着
 * {@code instanceof EntityBMPTTurret || instanceof EntityRolandTurret}——
 * 加新炮塔时漏改第二处，会导致新炮塔在世界重载后重复生成或被丢弃。
 *
 * 现在加一座炮塔只需在本类的静态块里加一行 {@code register(...)}，
 * 再按 {@code TURRET_GUIDE.md} 的清单补模型/贴图/渲染器/卡片/语言。
 *
 * 注意：注册的是"卡片类型 → 实体"，与 {@code ModEntities} 里的实体 id 注册是两个独立步骤，
 * 两边都要做（id 从 3014 起顺延）。
 */
public final class TurretRegistry {

    /** 炮塔实体工厂 */
    public interface Factory {
        Entity create(World world, BlockPos masterPos, EnumFacing facing);
    }

    /** type → 工厂（LinkedHashMap 保证顺序稳定，便于 GUI/调试输出） */
    private static final Map<String, Factory> FACTORIES = new LinkedHashMap<>();

    /**
     * 全部炮塔类型在此登记。
     * ===== 新增炮塔：在这里加一行即可（顺序无所谓，但保持 BMPT 在最前，它是默认类型）=====
     */
    static {
        register("bmpt", EntityBMPTTurret::new);
        register("roland", EntityRolandTurret::new);
        // 0.21.0 新炮塔（模型/贴图/卡片已接入，武器与俯仰待实现）
        register("turret", EntityTurret::new);
        register("laser_turret", EntityLaserTurret::new);
        register("laser_turret_mk1", EntityLaserTurretMark1::new);
        register("lightning_turret", EntityLightningTurret::new);
        // 0.30.0 M1 主战坦克炮塔（模型 models/Turret/M1，贴图 m1_abrams.png）
        register("m1", EntityM1Turret::new);
    }

    private TurretRegistry() {
    }

    /**
     * 注册一种炮塔类型。
     *
     * @param type    卡片 NBT "turretType" 的值（小写，与语言键 item.tgceaddon.turret_card.&lt;type&gt; 一致）
     * @param factory 实体工厂，签名 (World, BlockPos masterPos, EnumFacing facing)
     */
    public static void register(String type, Factory factory) {
        if (type == null || type.isEmpty() || factory == null) {
            throw new IllegalArgumentException("TurretRegistry.register: type/factory 不能为空");
        }
        Factory old = FACTORIES.put(type, factory);
        if (old != null) {
            throw new IllegalStateException("TurretRegistry: 炮塔类型重复注册 " + type);
        }
    }

    /** 是否为已登记的类型 */
    public static boolean isKnown(String type) {
        return type != null && FACTORIES.containsKey(type);
    }

    /**
     * 按类型创建炮塔实体。
     *
     * @return 新实体；类型未登记时返回 null（调用方应记日志并放弃生成，不要回退到别的类型，
     *         否则卡片与炮塔不符会让人以为"卡片没生效"）
     */
    public static Entity create(String type, World world, BlockPos masterPos, EnumFacing facing) {
        Factory factory = FACTORIES.get(type);
        return factory == null ? null : factory.create(world, masterPos, facing);
    }

    /** 已登记的全部类型（只读） */
    public static Set<String> types() {
        return Collections.unmodifiableSet(FACTORIES.keySet());
    }
}
