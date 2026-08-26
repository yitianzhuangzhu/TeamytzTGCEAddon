package com.teamytz.tgceaddon.client.render.entities.projectiles;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.entities.projectiles.CannonShellProjectile;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import techguns.client.render.entities.projectiles.RenderGenericProjectile;

/**
 * 机炮炮弹投射物渲染器 - 继承科技枪的 GenericProjectile 渲染器（与爆弹相同）
 */
@SideOnly(Side.CLIENT)
public class RenderCannonShellProjectile extends RenderGenericProjectile<CannonShellProjectile> {

    private static boolean hasLogged = false;

    public RenderCannonShellProjectile(RenderManager renderManager) {
        super(renderManager);
        if (!hasLogged) {
            TGCEAddon.getLogger().info("RenderCannonShellProjectile 已创建");
            hasLogged = true;
        }
    }
}
