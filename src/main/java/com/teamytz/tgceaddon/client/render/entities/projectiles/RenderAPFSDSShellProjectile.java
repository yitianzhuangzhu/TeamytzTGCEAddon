package com.teamytz.tgceaddon.client.render.entities.projectiles;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.client.models.projectiles.ModelAPFSDSShell;
import com.teamytz.tgceaddon.entities.projectiles.APFSDSShellProjectile;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** APFSDS 穿甲弹的弹丸渲染器（模型 ModelAPFSDS.bbmodel，贴图 32×32 UV 图集） */
@SideOnly(Side.CLIENT)
public class RenderAPFSDSShellProjectile extends RenderTurretShellProjectile<APFSDSShellProjectile> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TGCEAddon.MODID, "textures/entities/apfsds_shell.png");

    public RenderAPFSDSShellProjectile(RenderManager renderManager) {
        super(renderManager, new ModelAPFSDSShell(), TEXTURE);
    }
}
