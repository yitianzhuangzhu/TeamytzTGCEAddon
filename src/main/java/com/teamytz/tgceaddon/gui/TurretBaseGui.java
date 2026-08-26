package com.teamytz.tgceaddon.gui;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.gui.widgets.GuiButtonPvPSetting;
import com.teamytz.tgceaddon.gui.widgets.GuiButtonTargetAnimals;
import com.teamytz.tgceaddon.item.ItemTurretCard;
import com.teamytz.tgceaddon.tileentities.TurretBaseTileEntMaster;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import techguns.gui.OwnedTileEntGui;
import techguns.gui.PoweredTileEntGui;
import techguns.util.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 炮塔基座 GUI（套用科技枪 TurretGui 布局）
 * 布局：3x3 弹药输入(左) + 3x3 弹药输出(右) + 卡片槽(中央，原武器槽位置)
 * 左侧栏：RF 能量条 + 红石/安全按钮（手动复刻父类绘制，因为 jar 中绘制方法为混淆名）
 * 炮塔实体生成逻辑后续补充，当前仅基础 UI
 */
public class TurretBaseGui extends PoweredTileEntGui {

    public static final ResourceLocation texture = new ResourceLocation(
            TGCEAddon.MODID, "textures/gui/turret_base_gui.png");

    // 卡片槽屏幕坐标（与 Container 中 Slot 位置一致）
    public static final int SLOT_CARD_X = 85;
    public static final int SLOT_CARD_Y = 19;

    protected final TurretBaseTileEntMaster tileent;

    public TurretBaseGui(InventoryPlayer player, TurretBaseTileEntMaster tile) {
        super(new TurretBaseContainer(player, tile), tile);
        this.tileent = tile;
        this.tex = texture;
        this.showUpgradeSlot = false;
        this.showRedstone = true;
    }

    // ===== 背景层：主纹理 + 左侧红石/安全按钮背景 + RF 能量条 =====
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1f, 1f, 1f, 1f);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        // ===== 左侧面板背景（复刻 OwnedTileEntGui/RedstoneTileEntGui BRONZISH 绘制）=====
        this.mc.getTextureManager().bindTexture(OwnedTileEntGui.security_texture);
        // 安全按钮背景 (OwnedTileEntGui: k-27, l+35, UV 195,0, 27x30)
        this.drawTexturedModalRect(k - 27, l + 35, 195, 0, 27, 30);
        // 红石按钮背景 (RedstoneTileEntGui: k-27, l+5, UV 195,0, 27x30)
        this.drawTexturedModalRect(k - 27, l + 5, 195, 0, 27, 30);
        // 红石状态图标 (5x5 两枚，BRONZISH 外观 UV 199,202 区域)
        if (redstoneTileEnt.isRedstoneEnabled()) {
            this.drawTexturedModalRect(k + 7, l + 5, 199, 202, 5, 5);
            this.drawTexturedModalRect(k + 7, l + 10, 204, 207, 5, 5);
        } else {
            this.drawTexturedModalRect(k + 7, l + 5, 199, 207, 5, 5);
            this.drawTexturedModalRect(k + 7, l + 10, 204, 202, 5, 5);
        }

        // ===== 主纹理 =====
        this.mc.getTextureManager().bindTexture(tex);
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);

        // ===== 动物瞄准 / PVP 设置按钮背景 =====
        // 注意：turret_base_gui.png 的 (195,0,27,30) 区域是彩色渐变条（不是按钮槽），
        // 与安全/红石按钮背景保持一致，改用 security_texture(BRONZISH) 的纯色槽 (195,0,27,30)
        this.mc.getTextureManager().bindTexture(OwnedTileEntGui.security_texture);
        this.drawTexturedModalRect(k - 27, l + 65, 195, 0, 27, 30);
        this.drawTexturedModalRect(k - 27, l + 95, 195, 0, 27, 30);

        // ===== RF 能量条 =====
        drawDefaultEnergyBar();
    }

    // ===== 添加动物瞄准 / PVP 设置按钮 =====
    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.add(new GuiButtonTargetAnimals(
                TurretBaseTileEntMaster.BUTTON_ID_TARGET_ANIMALS, this.guiLeft - 22, this.guiTop + 70, 20, 20, this.tileent));
        this.buttonList.add(new GuiButtonPvPSetting(
                TurretBaseTileEntMaster.BUTTON_ID_PVP_SETTING, this.guiLeft - 22, this.guiTop + 100, 20, 20, this.tileent));
    }

    // ===== 前景层：机器名 + 红石 tooltip + 卡片类型 + 能量 tooltip =====
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        int mx = mouseX - (this.width - this.xSize) / 2;
        int my = mouseY - (this.height - this.ySize) / 2;

        // 机器名 + "物品栏" 文字（复刻 OwnedTileEntGui 行为）
        this.drawInventoryContainerName(tileent);

        // ===== 红石按钮 tooltip（复刻 RedstoneTileEntGui 行为）=====
        if (showRedstone) {
            if (isInRect(mx, my, 7, 5, 5, 10) || isInRect(mx, my, -22, 10, 20, 20)) {
                List<String> tooltip = new ArrayList<>();
                tooltip.add(TextUtil.trans("techguns.container.redstone"));
                tooltip.add(TextUtil.trans("techguns.container.redstone.mode") + ": " + TextUtil.trans(redstoneTileEnt.getRedstoneModeText()));
                tooltip.add(TextUtil.trans("techguns.container.redstone.signal") + ": " + TextUtil.trans(redstoneTileEnt.getSignalStateText()));
                this.drawHoveringText(tooltip, mx, my);
            }
        }

        // ===== 安全按钮 tooltip（复刻 OwnedTileEntGui 行为）=====
        if (isInRect(mx, my, -22, 40, 20, 20)) {
            List<String> tooltip = new ArrayList<>();
            String color = "";
            byte type = tileent.getSecurity();
            switch (type) {
                case 0:
                    color += TextFormatting.GREEN;
                    break;
                case 1:
                    color += TextFormatting.YELLOW;
                    break;
                case 2:
                    color += TextFormatting.GOLD;
                    break;
                case 3:
                    color += TextFormatting.RED;
                    break;
            }
            String own = TextUtil.trans("techguns.container.security.tooltip.unowned");
            UUID owner = tileent.getOwner();
            if (owner != null) {
                own = TextUtil.trans("techguns.container.security.tooltip.owner") + ": " + owner.toString();
            }
            tooltip.add(TextUtil.trans("techguns.container.security.owner") + ": " + own);
            tooltip.add(TextUtil.trans("techguns.container.security.tooltip") + ": " + color + TextUtil.trans("techguns.container.security.tooltip." + type));
            tooltip.add(TextUtil.trans("techguns.container.security.tooltip.description." + type));
            tooltip.add(TextUtil.trans("techguns.container.security.tooltip.descr2"));
            this.drawHoveringText(tooltip, mx, my);
        }

        // ===== 动物瞄准按钮 tooltip =====
        if (isInRect(mx, my, -22, 70, 20, 20)) {
            this.drawHoveringText(TextUtil.trans("techguns.turret.targetAnimals") + ": "
                    + (this.tileent.attackAnimals
                    ? net.minecraft.util.text.TextFormatting.RED + TextUtil.trans("techguns.yes")
                    : net.minecraft.util.text.TextFormatting.GREEN + TextUtil.trans("techguns.no")), mx, my);
        } else if (isInRect(mx, my, -22, 100, 20, 20)) {
            // ===== PVP 设置按钮 tooltip =====
            List<String> tooltip = new ArrayList<>(2);
            tooltip.add(TextUtil.trans("techguns.turret.pvpsetting") + ": "
                    + TextUtil.trans("techguns.turret.pvpsetting." + tileent.getPvpSetting()));
            tooltip.add(TextUtil.trans("techguns.container.security.tooltip.descr2"));
            this.drawHoveringText(tooltip, mx, my);
        }

        // ===== 卡片槽下方显示当前卡片类型 =====
        String cardType = tileent.getCardType();
        if (cardType.isEmpty()) {
            String s = I18n.format("item.tgceaddon.turret_card.unknown");
            this.mc.fontRenderer.drawString(TextFormatting.GRAY + s,
                    SLOT_CARD_X + 8 - this.mc.fontRenderer.getStringWidth(s) / 2,
                    SLOT_CARD_Y + 20, 0x404040);
        } else {
            this.mc.fontRenderer.drawString(TextFormatting.GOLD + cardType,
                    SLOT_CARD_X + 8 - this.mc.fontRenderer.getStringWidth(cardType) / 2,
                    SLOT_CARD_Y + 20, 0x404040);
        }

        // ===== 卡片槽 Tooltip =====
        if (isInRect(mx, my, SLOT_CARD_X, SLOT_CARD_Y, 16, 16)) {
            ItemStack card = tileent.getCard();
            if (!card.isEmpty()) {
                this.drawHoveringText(card.getTooltip(this.mc.player,
                        this.mc.gameSettings.advancedItemTooltips ? net.minecraft.client.util.ITooltipFlag.TooltipFlags.ADVANCED : net.minecraft.client.util.ITooltipFlag.TooltipFlags.NORMAL), mx, my);
            } else {
                this.drawHoveringText(I18n.format("item.tgceaddon.turret_card.name"), mx, my);
            }
        }

        // ===== RF 能量条 Tooltip =====
        drawDefaultEnergyTooltip(mx, my);
    }
}
