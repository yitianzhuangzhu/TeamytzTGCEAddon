package com.teamytz.tgceaddon.gui.widgets;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.tileentities.TurretBaseTileEntMaster;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiUtils;

/**
 * 炮塔 PVP 设置按钮（仿照科技枪 GuiButtonPvPSetting）
 * 图标取自 turret_base_gui.png：PVP 图标 UV (193, 6 + 16*mode)，16x16
 */
public class GuiButtonPvPSetting extends GuiButtonExt {

    public static final ResourceLocation texture =
            new ResourceLocation(TGCEAddon.MODID, "textures/gui/turret_base_gui.png");

    protected TurretBaseTileEntMaster tile;

    public GuiButtonPvPSetting(int id, int xPos, int yPos, int width, int height, TurretBaseTileEntMaster tile) {
        super(id, xPos, yPos, width, height, "");
        this.tile = tile;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partial) {
        if (this.visible) {
            this.hovered = mouseX >= this.x && mouseY >= this.y
                    && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int k = this.getHoverState(this.hovered);
            GuiUtils.drawContinuousTexturedBox(BUTTON_TEXTURES, this.x, this.y, 0, 46 + k * 20,
                    this.width, this.height, 200, 20, 2, 3, 2, 2, this.zLevel);
            this.mouseDragged(mc, mouseX, mouseY);

            Minecraft.getMinecraft().renderEngine.bindTexture(texture);
            byte mode = tile.getPvpSetting();
            this.drawTexturedModalRect(this.x + 2, this.y + 2, 193, 6 + (16 * mode), 16, 16);
        }
    }
}
