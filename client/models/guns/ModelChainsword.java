package com.teamytz.tgceaddon.client.models.guns;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.Entity;
import techguns.client.models.ModelMultipart;

public class ModelChainsword extends ModelMultipart {
    private final ModelRenderer chainsword;
    private final ModelRenderer blade1;
    private final ModelRenderer blade1_r1;
    private final ModelRenderer blade2;
    private final ModelRenderer blade2_r1;
    private final ModelRenderer main;
    private final ModelRenderer d_r1;
    private final ModelRenderer heat;

    public ModelChainsword() {
        textureWidth = 128;
        textureHeight = 128;

        chainsword = new ModelRenderer(this);
        chainsword.setRotationPoint(2.0F, 24.0F, 0.0F);
        setRotationAngle(chainsword, -0.25F, 0.0F, -0.1F);

        main = new ModelRenderer(this);
        main.setRotationPoint(1.0F, 0.0F, 0.0F);
        chainsword.addChild(main);

        blade1 = new ModelRenderer(this);
        blade1.setRotationPoint(2.0F, 24.0F, 0.0F);
        setRotationAngle(blade1, -0.25F, 0.0F, -0.1F);

        ModelRenderer blade1_base = new ModelRenderer(this);
        blade1_base.setRotationPoint(13.0F, -40.5F, -3.0F);
        blade1.addChild(blade1_base);
        setRotationAngle(blade1_base, 0.0F, 1.5708F, 0.0F);

        blade1_r1 = new ModelRenderer(this);
        blade1_r1.setRotationPoint(0.0F, 20.5F, -31.0F);
        blade1_base.addChild(blade1_r1);
        setRotationAngle(blade1_r1, -1.5708F, 0.0F, -1.5708F);
        blade1_r1.addBox(12.0F, -27.5F, -4.0F, 36, 8, 1, 0.0F);

        blade2 = new ModelRenderer(this);
        blade2.setRotationPoint(2.0F, 24.0F, 0.0F);
        setRotationAngle(blade2, -0.25F, 0.0F, -0.1F);

        ModelRenderer blade2_base = new ModelRenderer(this);
        blade2_base.setRotationPoint(13.0F, -40.5F, -3.0F);
        blade2.addChild(blade2_base);
        setRotationAngle(blade2_base, 0.0F, 1.5708F, 0.0F);

        blade2_r1 = new ModelRenderer(this);
        blade2_r1.setRotationPoint(0.0F, 20.5F, -31.0F);
        blade2_base.addChild(blade2_r1);
        setRotationAngle(blade2_r1, -1.5708F, 0.0F, -1.5708F);
        blade2_r1.cubeList.add(new net.minecraft.client.model.ModelBox(blade2_r1, 0, 9, 12.0F, -27.5F, -4.0F, 36, 8, 1, 0.0F, false));
        setRotationAngle(main, 0.0F, 3.1416F, 0.0F);
        
        ModelRenderer main1 = new ModelRenderer(this, 40, 41);
        main1.addBox(-6.0F, -30.5F, -4.5F, 2, 2, 8, 0.0F);
        main.addChild(main1);
        
        ModelRenderer main2 = new ModelRenderer(this, 68, 69);
        main2.addBox(-4.0F, -28.0F, -2.5F, 1, 4, 4, 0.0F);
        main.addChild(main2);
        
        ModelRenderer main3 = new ModelRenderer(this, 83, 19);
        main3.addBox(-6.0F, -22.0F, -3.0F, 3, 1, 5, 0.0F);
        main.addChild(main3);
        
        ModelRenderer main4 = new ModelRenderer(this, 28, 37);
        main4.addBox(-2.0F, -18.0F, -2.0F, 3, 17, 3, 0.0F);
        main.addChild(main4);
        
        ModelRenderer main5 = new ModelRenderer(this, 40, 51);
        main5.addBox(-3.0F, -3.0F, -3.0F, 5, 3, 5, 0.0F);
        main.addChild(main5);
        
        ModelRenderer main6 = new ModelRenderer(this, 0, 18);
        main6.addBox(-6.4F, -68.0F, -1.5F, 8, 36, 2, 0.0F);
        main.addChild(main6);
        
        ModelRenderer main7 = new ModelRenderer(this, 20, 18);
        main7.addBox(0.83F, -66.0F, -1.5F, 2, 33, 2, 0.0F);
        main.addChild(main7);
        
        ModelRenderer main8 = new ModelRenderer(this, 39, 36);
        main8.addBox(-8.0F, -33.0F, -2.0F, 11, 2, 3, 0.0F);
        main.addChild(main8);
        
        ModelRenderer main9 = new ModelRenderer(this, 20, 57);
        main9.addBox(-7.0F, -18.0F, -2.5F, 1, 10, 4, 0.0F);
        main.addChild(main9);
        
        ModelRenderer main10 = new ModelRenderer(this, 28, 18);
        main10.addBox(-3.0F, -31.0F, -3.5F, 5, 13, 6, 0.0F);
        main.addChild(main10);
        
        ModelRenderer main11 = new ModelRenderer(this, 50, 18);
        main11.addBox(-7.0F, -31.0F, -3.5F, 4, 3, 6, 0.0F);
        main.addChild(main11);
        
        ModelRenderer main12 = new ModelRenderer(this, 50, 27);
        main12.addBox(-7.0F, -21.0F, -3.5F, 4, 3, 6, 0.0F);
        main.addChild(main12);
        
        ModelRenderer main13 = new ModelRenderer(this, 54, 69);
        main13.addBox(-2.0F, -28.0F, -5.5F, 3, 7, 2, 0.0F);
        main.addChild(main13);

        d_r1 = new ModelRenderer(this);
        d_r1.setRotationPoint(0.5F, -41.6F, -1.0F);
        main.addChild(d_r1);
        setRotationAngle(d_r1, 0.0F, 0.0F, -0.3508F);
        d_r1.cubeList.add(new net.minecraft.client.model.ModelBox(d_r1, 0, 56, 2.5F, -26.9F, -0.5F, 8, 5, 2, 0.0F, false));

        heat = new ModelRenderer(this);
        heat.setRotationPoint(-1.0F, -20.0F, 10.0F);
        main.addChild(heat);
        setRotationAngle(heat, 0.0F, 1.5708F, 0.0F);
        
        ModelRenderer heat1 = new ModelRenderer(this, 62, 43);
        heat1.addBox(7.0F, -1.0F, -1.5F, 1, 1, 4, 0.0F);
        heat.addChild(heat1);
        
        ModelRenderer heat2 = new ModelRenderer(this, 2, 65);
        heat2.addBox(7.0F, -5.0F, -1.5F, 1, 1, 4, 0.0F);
        heat.addChild(heat2);
        
        ModelRenderer heat3 = new ModelRenderer(this, 48, 61);
        heat3.addBox(7.0F, -7.0F, -1.5F, 1, 1, 4, 0.0F);
        heat.addChild(heat3);
        
        ModelRenderer heat4 = new ModelRenderer(this, 14, 63);
        heat4.addBox(7.0F, -9.0F, -1.5F, 1, 9, 1, 0.0F);
        heat.addChild(heat4);
        
        ModelRenderer heat5 = new ModelRenderer(this, 14, 63);
        heat5.addBox(7.0F, -9.0F, 1.5F, 1, 9, 1, 0.0F);
        heat.addChild(heat5);
        
        ModelRenderer heat6 = new ModelRenderer(this, 32, 67);
        heat6.addBox(7.0F, -9.0F, -1.5F, 1, 1, 4, 0.0F);
        heat.addChild(heat6);
        
        ModelRenderer heat7 = new ModelRenderer(this, 62, 64);
        heat7.addBox(7.0F, -3.0F, -1.5F, 1, 1, 4, 0.0F);
        heat.addChild(heat7);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, int ammoLeft, float reloadProgress, ItemCameraTransforms.TransformType transformType, int part, float fireProgress, float chargeProgress) {
        if (part == 0) {
            chainsword.render(scale);
        } else if (part == 1) {
            if (fireProgress > 0 && ammoLeft > 0) {
                int frame = Math.round(fireProgress * 100.0f) % 2;
                if (frame == 0) {
                    blade1.render(scale);
                } else {
                    blade2.render(scale);
                }
            } else {
                blade1.render(scale);
            }
        }
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
