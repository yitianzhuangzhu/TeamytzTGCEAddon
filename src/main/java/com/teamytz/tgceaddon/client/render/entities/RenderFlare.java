package com.teamytz.tgceaddon.client.render.entities;

import com.teamytz.tgceaddon.entities.EntityFlare;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

/**
 * 热诱弹渲染器:面向相机的火焰贴图十字四边形(明亮橙红,诱饵视觉效果)。
 */
public class RenderFlare extends Render<EntityFlare> {

    private static final ResourceLocation FLARE_TEXTURE =
            new ResourceLocation("minecraft", "textures/particle/flame.png");

    public RenderFlare(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityFlare entity, double x, double y, double z,
                         float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        this.bindEntityTexture(entity);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        float size = 0.35F;
        Tessellator tessellator = Tessellator.getInstance();
        // 两个交叉的面向相机四边形,任何角度都可见
        for (int face = 0; face < 2; face++) {
            GlStateManager.pushMatrix();
            if (face == 1) {
                GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            }
            GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(-size, -size, 0.0D).tex(0.0D, 0.0D).endVertex();
            buffer.pos(-size, size, 0.0D).tex(0.0D, 1.0D).endVertex();
            buffer.pos(size, size, 0.0D).tex(1.0D, 1.0D).endVertex();
            buffer.pos(size, -size, 0.0D).tex(1.0D, 0.0D).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
        }

        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityFlare entity) {
        return FLARE_TEXTURE;
    }
}
