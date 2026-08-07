package com.teamytz.tgceaddon.client.models.guns;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.Entity;
import techguns.client.models.ModelMultipart;

/**
 * 动力剑模型
 * 仿照雷霆锤的实现，包含两帧lightning动画
 */
public class ModelPowerSword extends ModelMultipart {
    private final ModelRenderer main;
    private final ModelRenderer lightning1;
    private final ModelRenderer lightning2;

    public ModelPowerSword() {
        textureWidth = 64;
        textureHeight = 64;

        // main 根节点
        main = new ModelRenderer(this);
        main.setRotationPoint(0.0F, 24.0F, 0.0F);
        setRotationAngle(main, 0.0F, 3.1416F, 0.0F);

        // blade 组
        ModelRenderer blade = new ModelRenderer(this);
        blade.setRotationPoint(0.0F, 0.0F, 0.0F);
        main.addChild(blade);
        blade.setTextureOffset(0, 0).addBox(-2.5F, -37.0F, -0.5F, 5, 18, 1);
        blade.setTextureOffset(20, 7).addBox(-2.0F, -42.0F, -0.5F, 4, 5, 1);
        blade.setTextureOffset(22, 24).addBox(-1.5F, -45.0F, -0.5F, 3, 3, 1);
        blade.setTextureOffset(30, 7).addBox(-1.0F, -47.0F, -0.5F, 2, 2, 1);
        blade.setTextureOffset(24, 22).addBox(-0.5F, -48.0F, -0.5F, 1, 1, 1);

        // handle 组
        ModelRenderer handle = new ModelRenderer(this);
        handle.setRotationPoint(0.0F, 0.0F, 0.0F);
        main.addChild(handle);
        handle.setTextureOffset(20, 3).addBox(-2.0F, -2.0F, -1.0F, 4, 2, 2);
        handle.setTextureOffset(12, 3).addBox(-1.0F, -13.0F, -1.0F, 2, 11, 2);
        handle.setTextureOffset(20, 13).addBox(-2.0F, -14.0F, -1.0F, 4, 1, 2);
        handle.setTextureOffset(12, 0).addBox(-4.0F, -15.0F, -1.0F, 8, 1, 2);
        handle.setTextureOffset(12, 24).addBox(-2.0F, -16.0F, 1.0F, 4, 2, 1);
        handle.setTextureOffset(24, 19).addBox(-2.0F, -16.0F, -1.0F, 4, 2, 1);
        handle.setTextureOffset(12, 27).addBox(-5.0F, -16.0F, -1.0F, 1, 3, 2);
        handle.setTextureOffset(0, 30).addBox(4.5F, -15.5F, -1.0F, 1, 2, 2);
        handle.setTextureOffset(24, 28).addBox(-5.5F, -15.5F, -1.0F, 1, 2, 2);
        handle.setTextureOffset(18, 28).addBox(4.0F, -16.0F, -1.0F, 1, 3, 2);
        handle.setTextureOffset(12, 19).addBox(-2.0F, -18.0F, -1.0F, 4, 3, 2);
        handle.setTextureOffset(30, 22).addBox(1.0F, -22.0F, -1.5F, 1, 3, 1);
        handle.setTextureOffset(30, 10).addBox(-1.0F, -20.0F, -1.5F, 1, 1, 1);
        handle.setTextureOffset(12, 16).addBox(-3.0F, -19.0F, -1.0F, 6, 1, 2);
        handle.setTextureOffset(28, 16).addBox(-3.0F, -20.0F, -1.0F, 1, 1, 2);
        handle.setTextureOffset(6, 30).addBox(2.0F, -20.0F, -1.0F, 1, 1, 2);

        // lightning1 - 独立节点，不添加到 main
        // 需要使用与 main 相同的旋转和平移
        lightning1 = new ModelRenderer(this);
        lightning1.setRotationPoint(0.0F, 24.0F, 0.0F);
        setRotationAngle(lightning1, 0.0F, 3.1416F, 0.0F);
        lightning1.setTextureOffset(27, 31).addBox(-3.0F, -49.0F, -1.5F, 6, 29, 3);

        // lightning2 - 独立节点，不添加到 main
        lightning2 = new ModelRenderer(this);
        lightning2.setRotationPoint(0.0F, 24.0F, 0.0F);
        setRotationAngle(lightning2, 0.0F, 3.1416F, 0.0F);
        lightning2.setTextureOffset(46, 31).addBox(-3.0F, -49.0F, -1.5F, 6, 29, 3);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, int ammoLeft, float reloadProgress, ItemCameraTransforms.TransformType transformType, int part, float fireProgress, float chargeProgress) {
        if (part == 0) {
            // 只渲染主体（blade + handle），不含lightning
            main.render(scale);
        } else if (part == 1) {
            // 独立渲染lightning动画
            if (ammoLeft > 0) {
                int frame = (entityIn != null ? entityIn.ticksExisted : (int)ageInTicks) / 5 % 2;
                if (frame == 0) {
                    lightning1.render(scale);
                } else {
                    lightning2.render(scale);
                }
            }
        }
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
