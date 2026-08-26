package com.teamytz.tgceaddon.client.render.tileentities;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.client.model.BlockbenchJsonModel;
import com.teamytz.tgceaddon.tileentities.TurretBaseTileEntMaster;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

/**
 * 炮塔基座渲染器
 * master 形成后，渲染用户建模的炮塔底座大模型（跨 3x1x3 区域）
 * 模型以 master 方块中心为原点，朝向随 multiblockDirection 旋转
 */
public class RenderTurretBase extends TileEntitySpecialRenderer<TurretBaseTileEntMaster> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TGCEAddon.MODID, "textures/blocks/turretbase_texture.png");
    private static final ResourceLocation MODEL =
            new ResourceLocation(TGCEAddon.MODID, "models/block/turretbase_model.json");

    private final BlockbenchJsonModel model = new BlockbenchJsonModel(MODEL);

    @Override
    public void render(TurretBaseTileEntMaster te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (!te.isFormed()) {
            return;
        }
        this.bindTexture(TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);

        // 朝向旋转（默认 SOUTH 为 0 度）
        EnumFacing facing = te.getMultiblockDirection();
        if (facing != null) {
            float rot = 0;
            switch (facing) {
                case WEST: rot = 90; break;
                case NORTH: rot = 180; break;
                case EAST: rot = 270; break;
                default: break;
            }
            GlStateManager.rotate(rot, 0, 1, 0);
        }

        model.render();
        GlStateManager.popMatrix();
    }
}
