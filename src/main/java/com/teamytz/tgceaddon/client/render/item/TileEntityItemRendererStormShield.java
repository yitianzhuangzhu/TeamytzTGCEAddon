package com.teamytz.tgceaddon.client.render.item;

import com.teamytz.tgceaddon.client.models.armor.ModelStormShield;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 风暴盾牌渲染器
 * forcefield 仅在举盾时显示
 */
@SideOnly(Side.CLIENT)
public class TileEntityItemRendererStormShield extends TileEntityItemStackRenderer {
    private final ModelStormShield model;
    private final ResourceLocation texture;

    public TileEntityItemRendererStormShield(ModelStormShield model, ResourceLocation texture) {
        this.model = model;
        this.texture = texture;
    }

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        // 绑定纹理
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

        GlStateManager.pushMatrix();
        
        // 盾牌模型需要翻转才能正确显示
        GlStateManager.scale(1.0F, -1.0F, -1.0F);

        // 检测是否正在举盾
        // 通过检测玩家是否正在使用物品来判断
        boolean isBlocking = false;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null && mc.player.isUser()) {
            ItemStack activeItem = mc.player.getActiveItemStack();
            if (activeItem != null && activeItem == stack) {
                // 玩家正在使用这个盾牌（举盾中）
                isBlocking = true;
            }
        }

        if (isBlocking) {
            // 举盾时渲染所有内容（包括 forcefield）
            model.render(null, 0, 0, 0, 0, 0, 0.0625F);
        } else {
            // 未举盾时只渲染主体（不包括 forcefield）
            model.renderWithoutForcefield(0.0625F);
        }

        GlStateManager.popMatrix();
    }

    /**
     * 外部调用方法，用于检测举盾状态
     * 可以在第一人称渲染时使用
     */
    @SideOnly(Side.CLIENT)
    public static boolean isPlayerBlockingWithShield(ItemStack stack) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null) {
            ItemStack activeItem = mc.player.getActiveItemStack();
            return activeItem != null && activeItem == stack;
        }
        return false;
    }
}
