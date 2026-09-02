package com.teamytz.tgceaddon.client.render.entities;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.client.models.machines.ModelBMPT;
import com.teamytz.tgceaddon.entities.EntityBMPTTurret;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/**
 * BMPT 炮塔实体渲染器
 * - 使用 Blockbench 导出的 ModelBMPT 模型 + bmpt.png 纹理
 * - 朝向：yawForFacing 计算的 yaw 直接旋转（模型炮口默认朝 -Z，与 facing 一致）
 * - 模型 main 旋转点在 y=14(像素)，渲染后模型底部在实体上方 14/16 格，需下移对齐
 */
public class RenderBMPTTurret extends Render<EntityBMPTTurret> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TGCEAddon.MODID, "textures/entities/bmpt.png");

    private final ModelBMPT model = new ModelBMPT();

    /** 只记录一次渲染调用，确认渲染器工作 */
    private static boolean loggedOnce = false;

    // ===== 客户端帧级补间（丝滑转向）=====
    // MC 服务端转向是 tick 粒度的（rotationYawHead 每 0.05s 跳一次），
    // 即使渲染用 prev+partialTicks 插值，20tps 的阶梯感依然存在（用户反馈"一卡一卡"）。
    // 玩家转头丝滑是因为鼠标在客户端本地每帧连续驱动，不经网络阶梯。
    // 这里在渲染器里做同样的"本地连续驱动"：每帧把显示朝向以固定角速度向服务端
    // 同步值逼近，60fps 下每帧更新，彻底摆脱 tick 阶梯，达到玩家转头式的丝滑。
    // 补间状态存放在 EntityBMPTTurret 的字段上（渲染器实例按实体类共享，
    // 多座炮塔不能共用同一份状态）。
    /** 客户端补间速度（°/s）：略快于服务端转向（yaw 120°/s、pitch 160°/s），避免渲染明显滞后 */
    private static final float RENDER_TURN_SPEED = 180.0F;

    public RenderBMPTTurret(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBMPTTurret entity) {
        return TEXTURE;
    }

    /**
     * 以最大 maxDelta(°) 从 current 向 target 逼近（处理 ±180° 环绕）。
     * maxDelta<=0 或当前值已落在 target 的 maxDelta 范围内时直接返回 target。
     */
    private static float approachAngle(float current, float target, float maxDelta) {
        float diff = MathHelper.wrapDegrees(target - current);
        if (maxDelta <= 0.0F || Math.abs(diff) <= maxDelta) {
            return target;
        }
        return MathHelper.wrapDegrees(current + (diff > 0.0F ? maxDelta : -maxDelta));
    }

    @Override
    public void doRender(EntityBMPTTurret entity, double x, double y, double z,
                         float entityYaw, float partialTicks) {
        if (!loggedOnce) {
            loggedOnce = true;
            TGCEAddon.getLogger().info("[debug][BMPT渲染] doRender 被调用 pos=" + entity.getPosition()
                    + " facing=" + entity.getTurretFacing().getName() + " yaw=" + entity.rotationYaw);
        }
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();

        GlStateManager.translate((float) x, (float) y, (float) z);

        // 注意：translate 必须在 scale 之前调用，否则位移会被负缩放(1,-1,1)翻转成向下。
        // 实测：+24px 基本合适，再上移 1 像素 => +25px
        GlStateManager.translate(0.0F, 25.0F / 16.0F, 0.0F);

        // 翻转 Y 修正模型上下颠倒;再翻转 X 修正左右镜像(渲染级镜像,模型数据保持原始)
        // scale(-1,-1,1) 使 3D 变换行列式为 +1(纯旋转),渲染结果与 Blockbench 预览一致,
        // 且贴图保持原样——不再需要几何级镜像
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);

        // ===== 水平转向：整个实体（含底座/侧裙）朝目标方向旋转 =====
        // 模型零方向朝 -Z(北)，MC yaw 语义 0=南，且 scale(1,-1,1) 镜像反转了旋转方向，
        // 标准实体渲染公式 rotate(180 - yaw) 在镜像下正好正确（目标在 yaw 方向时模型指向目标）
        // 无目标时服务端会把 rotationYawHead 平滑回正到结构朝向
        //
        // 丝滑转向：服务端转向是 tick 粒度的（每 0.05s 跳一次），prev+partialTicks 插值
        // 只能线性连接相邻 tick 的采样值，20tps 的阶梯感仍在。玩家转头丝滑是因为鼠标在
        // 客户端本地每帧连续驱动；这里在渲染器里做同样的"本地连续驱动"：每帧把显示朝向
        // 以 RENDER_TURN_SPEED(°/s) 向服务端同步值逼近，帧率越高越丝滑。
        long now = Minecraft.getSystemTime();
        float dt = entity.renderLastTime < 0L ? 0.0F : (now - entity.renderLastTime) / 1000.0F;
        entity.renderLastTime = now;

        float yawHead;
        float turretPitch;
        // 俯仰目标值用 dataManager 同步的 TURRET_PITCH：
        // living 实体的 rotationPitch 由 HeadLook/Look 交替包同步，转向中几乎不更新（严重滞后），
        // 而 dataManager 每 tick 同步 float 无量化，客户端能拿到连续准确的俯仰角。
        float targetPitch = entity.getTurretPitch();
        if (Float.isNaN(entity.renderYawHead) || Float.isNaN(entity.renderPitch)) {
            // 首次渲染（或实体刚生成）：直接对准服务端同步值，不做补间
            yawHead = entity.rotationYawHead;
            turretPitch = targetPitch;
        } else {
            yawHead = approachAngle(entity.renderYawHead, entity.rotationYawHead, RENDER_TURN_SPEED * dt);
            turretPitch = approachAngle(entity.renderPitch, targetPitch, RENDER_TURN_SPEED * dt);
            // 服务端值跳变过大（实体重生成/传送）时直接瞬移，避免慢吞吞转大半圈
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

        // ===== 俯仰：只有炮塔上部（gun/rol/ror 中间机炮 + 左右火箭巢）单独俯仰 =====
        // 注意：getAttackTarget() 在 1.12.2 不同步到客户端（永远是 null），
        // 不能用它判断是否转向；rotationYawHead/rotationPitch 由实体朝向同步包更新

        // 低频日志：确认渲染器实际应用的转向值
        if (entity.ticksExisted % 100 == 0) {
            TGCEAddon.getLogger().info("[debug][BMPT渲染] tick=" + entity.ticksExisted
                    + " yawHead=" + entity.rotationYawHead + " pitch=" + entity.rotationPitch
                    + " renderYaw=" + yawHead + " renderPitch=" + turretPitch);
        }

        this.model.renderTurret(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, turretPitch);

        GlStateManager.enableCull();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}
