package com.teamytz.tgceaddon.client.render.entities;

import com.teamytz.tgceaddon.client.models.ITurretModel;
import com.teamytz.tgceaddon.entities.EntityBaseTurret;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/**
 * 新炮塔通用渲染器（0.21.0 新增）。
 *
 * 变换顺序与 ModelBMPT/ModelRoland 的渲染器完全一致（这套是标定过的，勿改）：
 *   1. translate(x, y, z)
 *   2. translate(0, translatePx / 16, 0)   ← 必须在 scale 之前，否则位移被负缩放翻成向下
 *   3. scale(-1, -1, 1)                    ← 修 Blockbench 的上下颠倒 + 左右镜像；
 *                                            行列式 = +1（纯旋转），模型数据保持 Blockbench 原样
 *   4. rotate(yawHead - 180, 0, 1, 0)      ← 模型零向朝 -Z，镜像后 (yaw-180) 正好指向目标方向
 *   5. model.render(...)
 *
 * 锚点配对（TURRET_GUIDE.md 第 2 节）：实体 y = master.y + 17/16 时，translatePx = 模型最高点 + 1，
 * 使模型底部落在 master 上方 1.125 格（基座上表面）。四条新模型的最高点都是 24 → translatePx = 25。
 *
 * 当前阶段不做俯仰/雷达等动画：需要时按炮塔各自的模型分组写 model.renderTurret(...)，
 * 再在这里改成调用它（参考 RenderBMPTTurret / RenderRolandTurret）。
 * 客户端帧级补间状态（renderYawHead / renderPitch）已按既有约定放在实体上。
 */
public class RenderBaseTurret<T extends EntityBaseTurret> extends Render<T> {

    private final ModelBase model;
    private final ResourceLocation texture;
    /** 模型上移像素数（16 像素 = 1 格） */
    private final float translatePx;

    /** 客户端补间速度（°/s）：略快于服务端转向，避免渲染明显滞后 */
    private static final float RENDER_TURN_SPEED = 180.0F;

    public RenderBaseTurret(RenderManager renderManager, ModelBase model,
                            ResourceLocation texture, float translatePx) {
        super(renderManager);
        this.model = model;
        this.texture = texture;
        this.translatePx = translatePx;
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        return this.texture;
    }

    /** 以最大 maxDelta(°) 从 current 向 target 逼近（处理 ±180° 环绕） */
    private static float approachAngle(float current, float target, float maxDelta) {
        float diff = MathHelper.wrapDegrees(target - current);
        if (maxDelta <= 0.0F || Math.abs(diff) <= maxDelta) {
            return target;
        }
        return MathHelper.wrapDegrees(current + (diff > 0.0F ? maxDelta : -maxDelta));
    }

    @Override
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();

        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.translate(0.0F, this.translatePx / 16.0F, 0.0F);
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);

        // ===== 朝向 + 客户端帧级补间（服务端是 20tps 台阶，这里每帧连续逼近）=====
        long now = Minecraft.getSystemTime();
        float dt = entity.renderLastTime < 0L ? 0.0F : (now - entity.renderLastTime) / 1000.0F;
        entity.renderLastTime = now;

        float yawHead;
        float turretPitch;
        float targetPitch = entity.getTurretPitch();
        if (Float.isNaN(entity.renderYawHead) || Float.isNaN(entity.renderPitch)) {
            yawHead = entity.rotationYawHead;
            turretPitch = targetPitch;
        } else {
            yawHead = approachAngle(entity.renderYawHead, entity.rotationYawHead, RENDER_TURN_SPEED * dt);
            turretPitch = approachAngle(entity.renderPitch, targetPitch, RENDER_TURN_SPEED * dt);
            if (Math.abs(MathHelper.wrapDegrees(entity.rotationYawHead - entity.renderYawHead)) > 90.0F
                    || Math.abs(MathHelper.wrapDegrees(targetPitch - entity.renderPitch)) > 90.0F) {
                yawHead = entity.rotationYawHead;
                turretPitch = targetPitch;
            }
        }
        entity.renderYawHead = yawHead;
        entity.renderPitch = turretPitch;

        GlStateManager.rotate(yawHead - 180.0F, 0.0F, 1.0F, 0.0F);

        this.bindEntityTexture(entity);
        // 实现了 ITurretModel 的模型(由 bbmodel2java.js --pitch 生成)支持俯仰动画：
        // 俯仰角用帧级补间后的 renderPitch(已随目标连续变化)，而不是 20tps 的同步值
        if (this.model instanceof ITurretModel) {
            ((ITurretModel) this.model).renderTurret(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F,
                    turretPitch * entity.getModelPitchSign());
        } else {
            this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
        }

        GlStateManager.enableCull();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}
