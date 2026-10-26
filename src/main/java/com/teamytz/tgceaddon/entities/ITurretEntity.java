package com.teamytz.tgceaddon.entities;

import com.teamytz.tgceaddon.tileentities.TurretBaseTileEntMaster;

/**
 * 炮塔实体统一接口。
 *
 * 基座（{@link TurretBaseTileEntMaster}）只通过这个接口认识炮塔，
 * 从而不必为每种炮塔写 instanceof / if-else —— 加新炮塔时不会漏改基座代码。
 *
 * 实现类必须与 {@link TurretRegistry} 注册的卡片类型一致：
 * {@link #getTurretType()} 的返回值要和卡片 NBT "turretType" 完全相同，
 * 否则基座会认为"卡片类型和已生成的炮塔不符"，把炮塔拆掉重建。
 */
public interface ITurretEntity {

    /** 炮塔所属的基座 master（不在有效结构上时返回 null） */
    TurretBaseTileEntMaster getMasterTile();

    /** 基座的红石需求是否满足（不满足时炮塔不索敌、不开火、原地回正） */
    boolean isRedstoneAllowed();

    /** 炮塔类型标识，对应 {@code ItemTurretCard} 的 NBT "turretType"（如 "bmpt" / "roland"） */
    String getTurretType();
}
