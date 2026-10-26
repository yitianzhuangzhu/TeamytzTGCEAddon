package com.teamytz.tgceaddon.client.render.entities.projectiles;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 普通炮塔炮弹（HE / APFSDS）的通用渲染器 —— 用**实体模型**画弹丸本身。
 *
 * 参考的是科技枪画火箭弹的方式（{@code RenderRocketProjectile} + {@code ModelRocket}）：
 * 平移 → 按 yaw/pitch 旋转 → 渲染 ModelBase，而不是科技枪子弹用的公告板贴图
 * （子弹那套 {@code RenderGenericProjectile} 只会画一个永远朝向镜头的方块）。
 *
 * 三个必须注意的点：
 * 1. **yaw 用 +90 而不是科技枪的 −90**：模型是 Blockbench 的 modded_entity 导出，
 *    转换器（.tools/bbmodel2java.js）按 {@code A(x,y,z) = (-x, 24−y, z)} 把 **X 轴翻了过来**，
 *    所以炮弹头部落在 Java 空间的 **−X**；科技枪自己的模型头朝 +X，配 −90 才是"头朝飞行方向"。
 *    这里换成 +90 正好抵消这次镜像。改这一行前先想清楚。
 * 2. **模型中心要往下挪**：两种弹药模型都是在 Blockbench 地面上建的，
 *    转换后整体位于 y ≈ 21.5~24.5（中心 {@link #MODEL_CENTER_Y_PX}），
 *    而实体原点在弹丸中心，所以渲染前要把模型中心平移到原点，否则弹丸会飘在实体上方一格多。
 * 3. 旋转要绕**中心**做：先平移（含居中偏移）再旋转，弹头才会绕着弹体中心摆，而不是绕着尾部甩。
 */
@SideOnly(Side.CLIENT)
public abstract class RenderTurretShellProjectile<T extends Entity> extends Render<T> {

    /** 模型整体缩放（模型自身 1 单位 = 1/16 格）：两种弹药模型长 15~16 单位，0.9 时约 0.85 格长 */
    public static final float MODEL_SCALE = 0.9F;
    /** 模型在 Java 空间里的垂直中心（px）：HE 的 y 范围 21.5~24.5、APFSDS 是 22~24，中心都是 23 */
    public static final float MODEL_CENTER_Y_PX = 23.0F;

    private final ModelBase model;
    private final ResourceLocation texture;

    public RenderTurretShellProjectile(RenderManager renderManager, ModelBase model, ResourceLocation texture) {
        super(renderManager);
        this.model = model;
        this.texture = texture;
    }

    /** 模型 1 单位对应的格数（含整体缩放） */
    protected float unitScale() {
        return 0.0625F * MODEL_SCALE;
    }

    @Override
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        // 帧级补间：20tps 的同步值直接画会有明显卡顿
        float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
        float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;

        float unit = this.unitScale();

        this.bindEntityTexture(entity);
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(x, y - MODEL_CENTER_Y_PX * unit, z);   // ② 模型中心对齐实体原点
        GlStateManager.rotate(yaw + 90.0F, 0.0F, 1.0F, 0.0F);          // ① 见类注释：镜像补偿
        GlStateManager.rotate(pitch, 0.0F, 0.0F, 1.0F);                // ③ 俯仰绕已居中的弹体
        GlStateManager.enableRescaleNormal();
        this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, unit);
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        return this.texture;
    }
}
