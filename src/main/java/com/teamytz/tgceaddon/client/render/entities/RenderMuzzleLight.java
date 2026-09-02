package com.teamytz.tgceaddon.client.render.entities;

import com.teamytz.tgceaddon.entities.EntityMuzzleLight;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

/**
 * 动态光源实体渲染器:什么都不画(实体已 setInvisible,这里兜底确保不渲染)。
 * 光源实体只作为 OptiFine 动态光源的"移动光源载体",本身不可见。
 */
public class RenderMuzzleLight extends Render<EntityMuzzleLight> {

    public RenderMuzzleLight(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityMuzzleLight entity, double x, double y, double z,
                         float entityYaw, float partialTicks) {
        // 不渲染任何东西
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityMuzzleLight entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
