package com.teamytz.tgceaddon.client.models.guns;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.Entity;
import techguns.client.models.ModelMultipart;

public class ModelBolter extends ModelMultipart {
    private final ModelRenderer main;
    private final ModelRenderer scope;
    private final ModelRenderer grip;
    private final ModelRenderer magazine;
    private final ModelRenderer thePureSigil;

    public ModelBolter() {
        textureWidth = 64;
        textureHeight = 64;

        main = new ModelRenderer(this);
        main.setRotationPoint(0.0F, 9.0F, 0.0F);
        main.rotateAngleY = -1.5708F;
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, -4, 0, -22.0F, -19.0F, -2.0F, 18, 6, 4, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 28, 10, 0.0F, -19.0F, -2.0F, 4, 6, 4, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 20, 34, 4.0F, -19.0F, -1.5F, 4, 6, 3, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 48, 41, 8.0F, -18.0F, -1.5F, 1, 5, 3, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 44, 49, 9.0F, -17.0F, -1.5F, 1, 4, 3, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 36, 0, -4.0F, -18.0F, -1.5F, 4, 5, 3, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 0, 10, -8.0F, -13.0F, -2.0F, 10, 3, 4, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 10, 35, -12.0F, -13.0F, -2.0F, 1, 4, 4, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 44, 8, -10.0F, -13.0F, -2.0F, 1, 4, 4, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 0, 47, -9.0F, -13.0F, -2.0F, 1, 3, 4, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 50, 4, -8.0F, -10.0F, -1.5F, 2, 1, 3, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 44, 16, -14.0F, -13.0F, -2.0F, 1, 4, 4, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 0, 60, -29.0F, -18.5F, -1.0F, 4, 2, 2, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 56, 62, -25.0F, -18.0F, -0.5F, 1, 1, 1, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 3, 17, -32.0F, -15.5F, -1.5F, 7, 3, 3, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 36, 8, -24.0F, -9.0F, -0.5F, 2, 1, 1, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 44, 25, -26.0F, -8.0F, -1.0F, 3, 1, 2, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 27, 20, -28.0F, -11.0F, -0.5F, 4, 1, 1, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 11, 51, -25.0F, -15.0F, -1.0F, 3, 2, 2, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 22, 43, -24.0F, -19.0F, -2.0F, 2, 4, 4, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 10, 43, -21.0F, -13.0F, -2.0F, 1, 3, 4, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 10, 43, -22.0F, -13.0F, -2.0F, 1, 4, 4, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 10, 43, -24.0F, -13.0F, -2.0F, 2, 4, 4, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 32, 41, -20.0F, -13.0F, -2.0F, 5, 4, 4, 0.0F));
        main.cubeList.add(new net.minecraft.client.model.ModelBox(main, 34, 34, -15.0F, -13.0F, -1.5F, 5, 4, 3, 0.0F));

        scope = new ModelRenderer(this);
        scope.setRotationPoint(0.0F, 0.0F, 0.0F);
        main.addChild(scope);
        scope.cubeList.add(new net.minecraft.client.model.ModelBox(scope, 18, 51, -3.0F, -20.0F, -1.0F, 2, 2, 2, 0.0F));
        scope.cubeList.add(new net.minecraft.client.model.ModelBox(scope, 34, 49, -3.0F, -23.0F, -1.5F, 2, 3, 3, 0.0F));
        scope.cubeList.add(new net.minecraft.client.model.ModelBox(scope, 20, 28, -11.0F, -23.0F, -1.5F, 6, 3, 3, 0.0F));
        scope.cubeList.add(new net.minecraft.client.model.ModelBox(scope, 38, 28, 0.0F, -23.0F, -1.5F, 5, 3, 3, 0.0F));
        scope.cubeList.add(new net.minecraft.client.model.ModelBox(scope, 50, 37, -3.5F, -20.5F, -1.0F, 3, 1, 2, 0.0F));
        scope.cubeList.add(new net.minecraft.client.model.ModelBox(scope, 26, 51, -5.0F, -22.5F, -1.0F, 2, 2, 2, 0.0F));
        scope.cubeList.add(new net.minecraft.client.model.ModelBox(scope, 52, 49, -1.0F, -22.5F, -1.0F, 1, 2, 2, 0.0F));

        grip = new ModelRenderer(this);
        grip.setRotationPoint(0.0F, 0.0F, 0.0F);
        main.addChild(grip);
        grip.cubeList.add(new net.minecraft.client.model.ModelBox(grip, 0, 35, 5.0F, -13.0F, -1.0F, 3, 10, 2, 0.0F));
        grip.cubeList.add(new net.minecraft.client.model.ModelBox(grip, 50, 34, 2.0F, -11.0F, -1.0F, 3, 1, 2, 0.0F));

        magazine = new ModelRenderer(this);
        magazine.setRotationPoint(0.0F, -1.0F, 0.0F);
        main.addChild(magazine);
        magazine.cubeList.add(new net.minecraft.client.model.ModelBox(magazine, 20, 23, -6.5F, 0.0F, -2.0F, 8, 1, 4, 0.0F));
        magazine.cubeList.add(new net.minecraft.client.model.ModelBox(magazine, 0, 23, -6.0F, -9.0F, -1.5F, 7, 9, 3, 0.0F));

        thePureSigil = new ModelRenderer(this);
        thePureSigil.setRotationPoint(0.0F, 15.0F, 0.0F);
        main.addChild(thePureSigil);
        thePureSigil.cubeList.add(new net.minecraft.client.model.ModelBox(thePureSigil, 30, 59, -18.5F, -30.0F, -3.5F, 3, 3, 2, 0.0F));
        thePureSigil.cubeList.add(new net.minecraft.client.model.ModelBox(thePureSigil, 58, 47, -18.0F, -27.0F, -3.0F, 2, 9, 1, 0.0F));
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, int ammoLeft, float reloadProgress, ItemCameraTransforms.TransformType transformType, int part, float fireProgress, float chargeProgress) {
        if (part == 0) {
            main.render(scale);
        }
    }
}
