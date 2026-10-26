package com.teamytz.tgceaddon.client.render.entities.projectiles;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.client.models.projectiles.ModelHEShell;
import com.teamytz.tgceaddon.entities.projectiles.HEShellProjectile;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** HE 高爆弹的弹丸渲染器（模型 ModelHE.bbmodel，贴图 32×32 UV 图集） */
@SideOnly(Side.CLIENT)
public class RenderHEShellProjectile extends RenderTurretShellProjectile<HEShellProjectile> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TGCEAddon.MODID, "textures/entities/he_shell.png");

    public RenderHEShellProjectile(RenderManager renderManager) {
        super(renderManager, new ModelHEShell(), TEXTURE);
    }
}
