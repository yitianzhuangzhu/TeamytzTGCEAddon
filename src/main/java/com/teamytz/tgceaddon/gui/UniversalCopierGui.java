package com.teamytz.tgceaddon.gui;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.item.ItemBlueprint;
import com.teamytz.tgceaddon.tileentities.UniversalCopierTileEnt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.gui.FontRenderer;
import techguns.TGPackets;
import techguns.gui.PoweredTileEntGui;
import techguns.packets.PacketGuiButtonClick;

import java.io.IOException;

/**
 * 通用复制机 GUI
 *
 * UI 布局（用户自定义纹理 + 科技枪 RF/设定保留样式）：
 *   xSize=176 ySize=198
 *
 * 左半 (MODE 区域，注册蓝图后显示当前选中可制作的物品):
 *   [MODE标签框]        —— 纹理自带 (在顶部中央偏左)
 *   [产物预览框 16x16]  —— (45, 30)  用 itemRender 把物品直接绘制在 MODE 下方
 *   [◀  prev] [next  ▶] —— prev=(31,68) 20×13，next=(61,68) 16×13
 *
 * 右半 (LOAD 区域，蓝图注册 + 成品生成):
 *   [蓝图槽 16x16]      —— (120, 15)  Slot 渲染
 *   [LOAD按钮]          —— (139, 17)  29×9，点击注册蓝图(蓝图槽右边紧邻)
 *   [垂直进度条]        —— (126, 36)  宽度 4，高度动态，UV 从 ammo_press_gui (176,0) 取
 *   [输出槽 16x16]      —— (120, 60)  Slot 渲染
 *
 * 左侧科技枪自带：
 *   RF 能量条 (8, 17) 4×59  —— 完全匹配 AmmoPressGui 参数
 *   红石/安全开关按钮 —— 父类自动绘制
 *
 * 底部：玩家物品栏 (8, 116)
 */
public class UniversalCopierGui extends PoweredTileEntGui {

    public static final ResourceLocation texture = new ResourceLocation(
        TGCEAddon.MODID, "textures/gui/universal_copier_gui.png");

    protected UniversalCopierTileEnt tileent;

    public static final int GUI_X_SIZE = 176;
    public static final int GUI_Y_SIZE = 198;

    // ======== 右半：蓝图输入槽 (SLOT，Container 同位置) ========
    public static final int SLOT_BLUEPRINT_X = 120;
    public static final int SLOT_BLUEPRINT_Y = 15;

    // ======== 右半：LOAD按钮 (点击后调用注册蓝图逻辑) ========
    public static final int BUTTON_REGISTER_X = 139;
    public static final int BUTTON_REGISTER_Y = 17;
    public static final int BUTTON_REGISTER_WIDTH = 29;
    public static final int BUTTON_REGISTER_HEIGHT = 9;

    // ======== 左半：MODE下方产物预览框（16x16，直接绘制物品模型）========
    // 相对格子右移3px、下移7px 对齐
    public static final int PREVIEW_X = 48;
    public static final int PREVIEW_Y = 37;

    // ======== 左半：切换物品 (prev / next 按钮，纹理中有菱形图标) ========
    public static final int BUTTON_PREV_X = 31;
    public static final int BUTTON_PREV_Y = 68;
    public static final int BUTTON_PREV_WIDTH = 20;
    public static final int BUTTON_PREV_HEIGHT = 13;

    public static final int BUTTON_NEXT_X = 61;
    public static final int BUTTON_NEXT_Y = 68;
    public static final int BUTTON_NEXT_WIDTH = 16;
    public static final int BUTTON_NEXT_HEIGHT = 13;

    // ======== 选中计数（切换按钮上方文字）========
    public static final int COUNT_DISPLAY_X = 46;
    public static final int COUNT_DISPLAY_Y = 82;

    // ======== 右半：输出成品槽 (SLOT，Container 同位置) ========
    public static final int SLOT_OUTPUT_X = 120;
    public static final int SLOT_OUTPUT_Y = 60;

    // ======== 垂直进度条（纹理窄轨道 x=126-129, y=36-50）==========
    //   UV 从 ammo_press_gui.png (176,0) 取，宽度 4，高度动态填充
    public static final int PROGRESS_ARROW_X = 126;          // 窄轨道内部左边缘（纹理 x=126-129）
    public static final int PROGRESS_ARROW_Y = 36;           // 窄轨道内部顶部（纹理 y=36-50）
    public static final int PROGRESS_ARROW_WIDTH = 4;        // 窄轨道内部宽度
    public static final int PROGRESS_ARROW_TOTAL_H = 15;     // 窄轨道内部高度（y=36-50）
    public static final int PROGRESS_ARROW_U = 176;          // 纹理扩展 UV X
    public static final int PROGRESS_ARROW_V = 0;            // 纹理扩展 UV Y

    public static final int PLAYER_INV_Y = 116;

    public UniversalCopierGui(InventoryPlayer ply, UniversalCopierTileEnt tileent) {
        super(new UniversalCopierContainer(ply, tileent), tileent);
        this.tileent = tileent;
        this.tex = texture;
        this.xSize = GUI_X_SIZE;
        this.ySize = GUI_Y_SIZE;
        this.showUpgradeSlot = false;
        this.showRedstone = true;
    }

    // ===== 前景层：绘制预览物品、文字、Tooltip（在背景之上）=====
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        int mx = mouseX - (this.width - this.xSize) / 2;
        int my = mouseY - (this.height - this.ySize) / 2;

        ItemStack output = tileent.getCurrentOutput();
        int unlocked = tileent.getUnlockedCount();

        // ========== 左半：MODE下方预览框文字显示（物品已在 BackgroundLayer 用标准模式绘制）==========
        if (unlocked > 0 && output != null && !output.isEmpty()) {
            // 物品名称：预览框下面
            String name = output.getDisplayName();
            this.mc.fontRenderer.drawString(TextFormatting.GOLD + name,
                (PREVIEW_X + 8) - this.mc.fontRenderer.getStringWidth(name) / 2,
                PREVIEW_Y + 20,
                0xFFFFFF);

            // 选中计数 (已解锁总数)
            if (unlocked > 1) {
                String idx = (tileent.selectedIndex + 1) + " / " + unlocked;
                this.mc.fontRenderer.drawString(TextFormatting.GRAY + idx,
                    COUNT_DISPLAY_X - this.mc.fontRenderer.getStringWidth(idx) / 2,
                    COUNT_DISPLAY_Y, 0xFFFFFF);
            }
        } else if (unlocked == 0) {
            this.mc.fontRenderer.drawString(TextFormatting.GRAY + "先注册蓝图",
                PREVIEW_X - 20, PREVIEW_Y + 2, 0xFFFFFF);
        }

        // 状态 (就绪/生产中)
        if (tileent.isWorking()) {
            int scaledPct = tileent.getProgressScaled(100);
            String s = "生产中 " + scaledPct + "%";
            this.mc.fontRenderer.drawString(TextFormatting.YELLOW + s,
                COUNT_DISPLAY_X - this.mc.fontRenderer.getStringWidth(s) / 2,
                84, 0xFFFFFF);
        } else if (unlocked > 0) {
            int totalRF = UniversalCopierTileEnt.POWER_PER_TICK * UniversalCopierTileEnt.CRAFT_TIME;
            String s = "就绪: " + totalRF + " FE";
            this.mc.fontRenderer.drawString(TextFormatting.GREEN + s,
                COUNT_DISPLAY_X - this.mc.fontRenderer.getStringWidth(s) / 2,
                84, 0xFFFFFF);
        }

        // 蓝图槽 & 输出槽 —— Slot 自行渲染物品图标，这里只拿引用供 Tooltip 使用
        ItemStack blueprint = tileent.getCurrentBlueprint();
        ItemStack result = tileent.inventory.getStackInSlot(UniversalCopierTileEnt.SLOT_OUTPUT);

        // =========== Tooltip 区域 ===========

        // LOAD 按钮提示
        if (isInRect(mx, my, BUTTON_REGISTER_X, BUTTON_REGISTER_Y,
            BUTTON_REGISTER_WIDTH, BUTTON_REGISTER_HEIGHT)) {
            if (tileent.canRegisterBlueprint()) {
                this.drawHoveringText("§a点击 LOAD 注册蓝图", mx, my);
            } else {
                this.drawHoveringText("§7左侧放入蓝图后点 LOAD", mx, my);
            }
        }

        // 蓝图槽 Tooltip
        if (isInRect(mx, my, SLOT_BLUEPRINT_X, SLOT_BLUEPRINT_Y,
            16, 16)) {
            if (!blueprint.isEmpty()) {
                this.drawHoveringText(blueprint.getTooltip(this.mc.player,
                    this.mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL), mx, my);
            } else {
                this.drawHoveringText("§7放入蓝图", mx, my);
            }
        }

        // 预览框 Tooltip
        if (isInRect(mx, my, PREVIEW_X, PREVIEW_Y,
            16, 16)) {
            if (unlocked > 0 && output != null && !output.isEmpty()) {
                this.drawHoveringText(output.getTooltip(this.mc.player,
                    this.mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL), mx, my);
            } else {
                this.drawHoveringText("§7产物预览", mx, my);
            }
        }

        // 输出槽 Tooltip
        if (isInRect(mx, my, SLOT_OUTPUT_X, SLOT_OUTPUT_Y,
            16, 16)) {
            if (!result.isEmpty()) {
                this.drawHoveringText(result.getTooltip(this.mc.player,
                    this.mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL), mx, my);
            } else {
                this.drawHoveringText("§7输出槽", mx, my);
            }
        }

        // 切换按钮提示
        if (unlocked > 0) {
            if (isInRect(mx, my, BUTTON_PREV_X, BUTTON_PREV_Y,
                BUTTON_PREV_WIDTH, BUTTON_PREV_HEIGHT)) {
                this.drawHoveringText("上一个", mx, my);
            }
            if (isInRect(mx, my, BUTTON_NEXT_X, BUTTON_NEXT_Y,
                BUTTON_NEXT_WIDTH, BUTTON_NEXT_HEIGHT)) {
                this.drawHoveringText("下一个", mx, my);
            }
        }

        // 能量条 tooltip (父类处理，按科技枪默认 RF 区域)
        drawDefaultEnergyTooltip(mx, my);
    }

    // ===== 背景层：绘制主纹理 + 进度箭头 + 能量条 =====
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1f, 1f, 1f, 1f);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        // ===== 左侧面板背景（沿用 Techguns 风格）=====
        this.mc.getTextureManager().bindTexture(techguns.gui.PoweredTileEntGui.power_texture);
        // 安全按钮背景 (OwnedTileEntGui: k-22-5, l+40-5, UV 195,60, 27x30)
        this.drawTexturedModalRect(k - 27, l + 35, 195, 60, 27, 30);
        // 红石按钮背景 (RedstoneTileEntGui: k-22-5, l+10-5, UV 195,0, 27x30)
        this.drawTexturedModalRect(k - 27, l + 5, 195, 0, 27, 30);
        // 红石按钮图标
        if (redstoneTileEnt.isRedstoneEnabled()) {
            this.drawTexturedModalRect(k + 7, l + 5, 199, 0, 5, 5);
            this.drawTexturedModalRect(k + 7, l + 10, 204, 5, 5, 5);
        } else {
            this.drawTexturedModalRect(k + 7, l + 5, 199, 5, 5, 5);
            this.drawTexturedModalRect(k + 7, l + 10, 204, 0, 5, 5);
        }

        // ===== 绘制主纹理 =====
        this.mc.getTextureManager().bindTexture(tex);
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);

        // ===== RF 能量条上方状态标记 =====
        // 纹理右下区上面板(y=202-211)，红X在左(x=200-204)，绿G在右(x=205-209)，5×10
        int iconX = 8;
        int iconY = 5;
        if (redstoneTileEnt.isRedstoneEnabled()) {
            this.drawTexturedModalRect(k + iconX, l + iconY, 204, 202, 5, 10);
        } else {
            this.drawTexturedModalRect(k + iconX, l + iconY, 199, 202, 5, 10);
        }

        // ===== 绘制 Techguns 风格按钮框（覆盖用户纹理中的按钮区域）=====
        // 使用 ammo_press_gui.png 中 UV(227, 132) 的按钮框纹理
        this.mc.getTextureManager().bindTexture(techguns.gui.PoweredTileEntGui.power_texture);
        drawTechgunsButtonFrame(k + BUTTON_PREV_X, l + BUTTON_PREV_Y, BUTTON_PREV_WIDTH, BUTTON_PREV_HEIGHT, true);
        drawTechgunsButtonFrame(k + BUTTON_NEXT_X, l + BUTTON_NEXT_Y, BUTTON_NEXT_WIDTH, BUTTON_NEXT_HEIGHT, false);
        // 切回自己的纹理
        this.mc.getTextureManager().bindTexture(tex);

        // ===== 垂直进度箭头（模仿 AmmoPress）：从上往下填充 =====
        if (tileent.isWorking()) {
            int scaled = tileent.getProgressScaled(PROGRESS_ARROW_TOTAL_H);
            if (scaled > 0) {
                this.mc.getTextureManager().bindTexture(techguns.gui.PoweredTileEntGui.power_texture);
                this.drawTexturedModalRect(
                    k + PROGRESS_ARROW_X,
                    l + PROGRESS_ARROW_Y,
                    PROGRESS_ARROW_U,
                    PROGRESS_ARROW_V,
                    PROGRESS_ARROW_WIDTH,
                    scaled + 1);
                this.mc.getTextureManager().bindTexture(tex);
            }
        }

        // ===== 左半：MODE下方预览框物品渲染 =====
        ItemStack outputForPreview = tileent.getCurrentOutput();
        int unlocked = tileent.getUnlockedCount();
        if (unlocked > 0 && outputForPreview != null && !outputForPreview.isEmpty()) {
            ItemStack display = outputForPreview.copy();
            display.setCount(1);

            GlStateManager.pushMatrix();
            GlStateManager.color(1F, 1F, 1F, 1F);
            RenderHelper.enableGUIStandardItemLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
            GlStateManager.enableRescaleNormal();

            this.itemRender.zLevel = 10.0F;
            this.itemRender.renderItemAndEffectIntoGUI(display, k + PREVIEW_X, l + PREVIEW_Y);
            this.itemRender.zLevel = 0.0F;

            GlStateManager.disableRescaleNormal();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.enableAlpha();
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.popMatrix();
        }

        // ===== RF 能量条（使用自定义参数）=====
        drawDefaultEnergyBar();
    }

    // ===== 绘制 Techguns 风格按钮框（复用 power_texture 中的按钮纹理）=====
    private void drawTechgunsButtonFrame(int x, int y, int w, int h, boolean isPrev) {
        // AmmoPressGui 的 Prev/Next 按钮是直接绘制在主背景纹理中的
        // Prev 按钮 UV: (31, 68)，尺寸 21x12
        // Next 按钮 UV: (61, 68)，尺寸 21x12
        int u = isPrev ? 31 : 61;
        int v = 68;
        this.drawTexturedModalRect(x, y, u, v, w, h);
    }

    // ===== 能量条：完全匹配 AmmoPressGui 参数 =====
    @Override
    protected void drawDefaultEnergyBar() {
        drawEnergyBar(guiLeft, guiTop, 8, 17, 251, 1, 4, 59);
    }

    @Override
    protected void drawDefaultEnergyTooltip(int mouseX, int mouseY) {
        drawEnergyTooltip(mouseX, mouseY, 8, 17, 4, 59);
    }

    // ===== 鼠标点击：LOAD/prev/next 三个按钮发网络包给服务端 =====
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton != 0) return;

        int mx = mouseX - (this.width - this.xSize) / 2;
        int my = mouseY - (this.height - this.ySize) / 2;

        // LOAD 注册蓝图按钮
        if (isInRect(mx, my, BUTTON_REGISTER_X, BUTTON_REGISTER_Y,
            BUTTON_REGISTER_WIDTH, BUTTON_REGISTER_HEIGHT)) {
            if (tileent.canRegisterBlueprint()) {
                playVanillaButtonSound();
                TGPackets.wrapper.sendToServer(
                    new PacketGuiButtonClick(this.tileent, UniversalCopierTileEnt.BUTTON_ID_REGISTER));
            }
        }
        // prev
        else if (isInRect(mx, my, BUTTON_PREV_X, BUTTON_PREV_Y,
            BUTTON_PREV_WIDTH, BUTTON_PREV_HEIGHT)) {
            if (tileent.getUnlockedCount() > 0) {
                playVanillaButtonSound();
                TGPackets.wrapper.sendToServer(
                    new PacketGuiButtonClick(this.tileent, UniversalCopierTileEnt.BUTTON_ID_PREV));
            }
        }
        // next
        else if (isInRect(mx, my, BUTTON_NEXT_X, BUTTON_NEXT_Y,
            BUTTON_NEXT_WIDTH, BUTTON_NEXT_HEIGHT)) {
            if (tileent.getUnlockedCount() > 0) {
                playVanillaButtonSound();
                TGPackets.wrapper.sendToServer(
                    new PacketGuiButtonClick(this.tileent, UniversalCopierTileEnt.BUTTON_ID_NEXT));
            }
        }
    }
}
