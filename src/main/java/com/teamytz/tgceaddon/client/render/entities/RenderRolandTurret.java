package com.teamytz.tgceaddon.client.render.entities;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.client.models.machines.ModelRoland;
import com.teamytz.tgceaddon.entities.EntityRolandTurret;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/**
 * 罗兰防空炮塔实体渲染器
 * - Blockbench 导出的 ModelRoland + roland.png 纹理
 * - 水平转向:整个实体绕 Y 轴旋转(rotate(180 - yawHead),与 BMPT 一致)
 * - 动画:radar 组顺时针旋转(角度来自服务端 dataManager 同步);tr/l/r 随目标俯仰
 * - 模型根空间底部 y=24(像素),渲染器 translate 35px 使模型底部位于实体上方 11/16 格
 */
public class RenderRolandTurret extends Render<EntityRolandTurret> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TGCEAddon.MODID, "textures/entities/roland.png");

    private final ModelRoland model = new ModelRoland();

    /** 客户端补间速度(°/s) */
    private static final float RENDER_TURN_SPEED = 180.0F;

    public RenderRolandTurret(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityRolandTurret entity) {
        return TEXTURE;
    }

    private static float approachAngle(float current, float target, float maxDelta) {
        float diff = MathHelper.wrapDegrees(target - current);
        if (maxDelta <= 0.0F || Math.abs(diff) <= maxDelta) {
            return target;
        }
        return MathHelper.wrapDegrees(current + (diff > 0.0F ? maxDelta : -maxDelta));
    }

    @Override
    public void doRender(EntityRolandTurret entity, double x, double y, double z,
                         float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();

        GlStateManager.translate((float) x, (float) y, (float) z);
        // 模型底部(根 y=24)上移到实体上方 11/16 格,对齐炮塔底座
        // 模型底部(根 y=24):实体在 master+0.5625,translate 33px → 底部 = master+1 + 2px
        GlStateManager.translate(0.0F, 33.0F / 16.0F, 0.0F);
        // 翻转 Y 修正模型上下颠倒;再翻转 X 修正左右镜像(渲染级镜像,模型数据保持原始)
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);

        // ===== 水平转向(整个实体)+ 客户端帧级补间(丝滑转向,同 BMPT)=====
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

        this.model.renderTurret(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F,
                turretPitch, entity.getRadarAngle());

        GlStateManager.enableCull();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}
