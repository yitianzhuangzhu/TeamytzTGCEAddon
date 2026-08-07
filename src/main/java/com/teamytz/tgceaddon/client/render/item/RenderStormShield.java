package com.teamytz.tgceaddon.client.render.item;

import com.teamytz.tgceaddon.client.models.armor.ModelStormShield;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import techguns.api.render.IItemRenderer;

/**
 * 风暴盾牌渲染器
 * 使用 IItemRenderer 接口，可以获取实体信息来检测举盾状态
 * forcefield 仅在举盾时显示
 */
public class RenderStormShield implements IItemRenderer {
    private final ModelStormShield model;
    private final ResourceLocation texture;

    public RenderStormShield(ModelStormShield model, ResourceLocation texture) {
        this.model = model;
        this.texture = texture;
    }

    @Override
    public void renderItem(ItemCameraTransforms.TransformType transform, ItemStack stack, EntityLivingBase entity, boolean leftHand) {
        // 绑定纹理
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

        GlStateManager.pushMatrix();

        // 盾牌模型需要翻转才能正确显示
        GlStateManager.scale(1.0F, -1.0F, -1.0F);

        // 检测是否正在举盾
        boolean isBlocking = false;
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            // 检测玩家是否正在使用盾牌（举盾中）
            ItemStack activeItem = player.getActiveItemStack();
            if (activeItem != null && activeItem.getItem() == stack.getItem()) {
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
}
