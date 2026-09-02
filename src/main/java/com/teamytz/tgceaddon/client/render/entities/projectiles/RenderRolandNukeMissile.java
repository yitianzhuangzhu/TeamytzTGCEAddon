package com.teamytz.tgceaddon.client.render.entities.projectiles;

import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * 罗兰核导弹渲染器:复用科技枪火箭渲染,但使用核火箭贴图(rocket_nuke.png)
 */
public class RenderRolandNukeMissile extends techguns.client.render.entities.projectiles.RenderRocketProjectile {

    private static final ResourceLocation NUKE_TEXTURE =
            new ResourceLocation("techguns", "textures/guns/rocket_nuke.png");

    public RenderRolandNukeMissile(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return NUKE_TEXTURE;
    }
}
