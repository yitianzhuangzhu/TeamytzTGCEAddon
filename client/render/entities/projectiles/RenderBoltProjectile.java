package com.teamytz.tgceaddon.client.render.entities.projectiles;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.entities.projectiles.BoltProjectile;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import techguns.client.render.entities.projectiles.RenderGenericProjectile;

/**
 * 爆弹投射物渲染器 - 继承科技枪的GenericProjectile渲染器
 */
@SideOnly(Side.CLIENT)
public class RenderBoltProjectile extends RenderGenericProjectile<BoltProjectile> {
    
    private static boolean hasLogged = false;

    public RenderBoltProjectile(RenderManager renderManager) {
        super(renderManager);
        if (!hasLogged) {
            TGCEAddon.getLogger().info("RenderBoltProjectile 已创建");
            hasLogged = true;
        }
    }
}