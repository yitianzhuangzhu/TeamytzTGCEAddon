package com.teamytz.tgceaddon.client.render.entities;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.client.models.machines.ModelM1Abrams;
import com.teamytz.tgceaddon.entities.EntityBaseTurret;
import com.teamytz.tgceaddon.entities.EntityM1Turret;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/**
 * M1 主战坦克炮塔渲染器（0.30.0）。
 *
 * <p>变换顺序照抄 {@link RenderBaseTurret} / {@code RenderRolandTurret}（这套是标定过的，勿改）：</p>
 * <pre>
 *   1. translate(x, y, z)
 *   2. translate(0, RENDER_TRANSLATE_PX/16, 0)   ← 必须在 scale 之前
 *   3. scale(-1, -1, 1)                          ← 修 Blockbench 的上下颠倒 + 左右镜像
 *   4. rotate(yawHead - 180, 0, 1, 0)            ← 模型零向朝 -Z
 *   5. model.renderM1(...)
 * </pre>
 *
 * <p>四个动画通道：主炮俯仰（{@code mainweapon}）、机枪方向机（{@code machinegun}）、
 * 机枪高低（{@code gun_main}）、主炮后坐力（{@code cannon_barrel}）。
 * 机枪方向机是**相对炮塔**的角度，渲染时减去当前（补间后的）炮塔偏航。</p>
 *
 * <p>补间状态全部放在实体上（{@code renderYawHead/renderPitch/renderMgYaw/renderMgPitch/renderRecoil}），
 * 渲染器本身是共享实例，不能存状态。</p>
 */
public class RenderM1Turret extends Render<EntityM1Turret> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TGCEAddon.MODID, "textures/entities/m1_abrams.png");

    private final ModelM1Abrams model = new ModelM1Abrams();

    /** 客户端补间速度（°/s） */
    private static final float RENDER_TURN_SPEED = 210.0F;

    /** 后坐力补间速度（单位/秒）：0→1 在开火后约 0.12 秒内到位 */
    private static final float RENDER_RECOIL_SPEED = 8.0F;

    public RenderM1Turret(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityM1Turret entity) {
        return TEXTURE;
    }

    private static float approachAngle(float current, float target, float maxDelta) {
        float diff = MathHelper.wrapDegrees(target - current);
        if (maxDelta <= 0.0F || Math.abs(diff) <= maxDelta) {
            return target;
        }
        return MathHelper.wrapDegrees(current + (diff > 0.0F ? maxDelta : -maxDelta));
    }

    private static float approachFloat(float current, float target, float maxDelta) {
        float diff = target - current;
        if (maxDelta <= 0.0F || Math.abs(diff) <= maxDelta) {
            return target;
        }
        return current + (diff > 0.0F ? maxDelta : -maxDelta);
    }

    @Override
    public void doRender(EntityM1Turret entity, double x, double y, double z,
                         float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();

        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.translate(0.0F, EntityBaseTurret.RENDER_TRANSLATE_PX / 16.0F, 0.0F);
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);

        // ===== 客户端帧级补间（服务端 20tps 有台阶，这里每帧连续逼近）=====
        long now = Minecraft.getSystemTime();
        float dt = entity.renderLastTime < 0L ? 0.0F : (now - entity.renderLastTime) / 1000.0F;
        entity.renderLastTime = now;

        float targetYaw = entity.rotationYawHead;
        float targetMainPitch = entity.getTurretPitch();
        float targetMgYaw = entity.getMgYaw();
        float targetMgPitch = entity.getMgPitch();
        float targetRecoil = entity.getRecoil();

        float yawHead, mainPitch, mgYaw, mgPitch, recoil;
        if (Float.isNaN(entity.renderYawHead) || Float.isNaN(entity.renderPitch)
                || Float.isNaN(entity.renderMgYaw) || Float.isNaN(entity.renderMgPitch)
                || Float.isNaN(entity.renderRecoil)) {
            yawHead = targetYaw;
            mainPitch = targetMainPitch;
            mgYaw = targetMgYaw;
            mgPitch = targetMgPitch;
            recoil = targetRecoil;
        } else {
            yawHead = approachAngle(entity.renderYawHead, targetYaw, RENDER_TURN_SPEED * dt);
            mainPitch = approachAngle(entity.renderPitch, targetMainPitch, RENDER_TURN_SPEED * dt);
            mgYaw = approachAngle(entity.renderMgYaw, targetMgYaw, RENDER_TURN_SPEED * dt * 1.5F);
            mgPitch = approachAngle(entity.renderMgPitch, targetMgPitch, RENDER_TURN_SPEED * dt * 1.5F);
            // 后坐力是"开火瞬间跳到 1 再衰减"，补间只做平滑，不做 >90° 瞬移那套
            recoil = approachFloat(entity.renderRecoil, targetRecoil, RENDER_RECOIL_SPEED * dt);

            // 差值过大（刚入镜/传送/目标切换）直接瞬移，免得镜头里"甩"一大圈
            if (Math.abs(MathHelper.wrapDegrees(targetYaw - entity.renderYawHead)) > 90.0F) {
                yawHead = targetYaw;
            }
            if (Math.abs(MathHelper.wrapDegrees(targetMgYaw - entity.renderMgYaw)) > 90.0F) {
                mgYaw = targetMgYaw;
            }
        }
        entity.renderYawHead = yawHead;
        entity.renderPitch = mainPitch;
        entity.renderMgYaw = mgYaw;
        entity.renderMgPitch = mgPitch;
        entity.renderRecoil = recoil;

        // ===== 水平转向：整个模型绕 Y 轴（车体 + 炮塔一起转）=====
        GlStateManager.rotate(yawHead - 180.0F, 0.0F, 1.0F, 0.0F);

        this.bindEntityTexture(entity);

        // 机枪方向机是相对炮塔的角度
        float mgYawRelative = MathHelper.wrapDegrees(mgYaw - yawHead);
        this.model.renderM1(0.0625F, mainPitch, mgYawRelative, mgPitch, recoil);

        GlStateManager.enableCull();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}
