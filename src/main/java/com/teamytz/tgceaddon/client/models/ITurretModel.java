package com.teamytz.tgceaddon.client.models;

import net.minecraft.entity.Entity;

/**
 * 带俯仰动画的炮塔模型接口（0.23.0 新增）。
 *
 * 由 {@code .tools/bbmodel2java.js --pitch <组名>} 生成的模型类实现，
 * {@link com.teamytz.tgceaddon.client.render.entities.RenderBaseTurret} 检测到该接口后
 * 会调用 {@link #renderTurret} 而不是普通的 {@code render}，从而让指定分组绕自身旋转点俯仰。
 *
 * 俯仰角语义与 BMPT 一致（MC 语义：正 = 低头、负 = 抬头）；
 * 实现方设置 rotateAngleX 后必须复位，避免污染下一帧。
 */
public interface ITurretModel {

    /**
     * 渲染炮塔（带动画）。
     *
     * @param f1..f5  与 ModelBase.render 同义的常规参数（本模组均传 0）
     * @param pitchDeg 俯仰角（度，MC 语义：正=低头、负=抬头）
     */
    void renderTurret(Entity entity, float f, float f1, float f2, float f3, float f4, float f5, float pitchDeg);
}
