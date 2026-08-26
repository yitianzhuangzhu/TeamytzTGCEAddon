package com.teamytz.tgceaddon.client.models.guns;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.EntityLivingBase;

public class ModelChainsaw extends ModelBase {
    private final ModelRenderer main;
    private final ModelRenderer handle;
    private final ModelRenderer blade;

    public ModelChainsaw() {
        textureWidth = 32;
        textureHeight = 32;
        
        main = new ModelRenderer(this);
        main.setTextureOffset(0, 0);
        main.addBox(-2.0F, -1.0F, -4.0F, 4, 4, 8);
        
        handle = new ModelRenderer(this);
        handle.setTextureOffset(0, 12);
        handle.addBox(-1.0F, 2.0F, -2.0F, 2, 4, 4);
        
        blade = new ModelRenderer(this);
        blade.setTextureOffset(8, 12);
        blade.addBox(-0.5F, -1.0F, -6.0F, 1, 2, 4);
    }
    
    public void render(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, int ammoLeft, float reloadProgress, net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType transformType, int partIndex, float fireProgress, float chargeProgress) {
        main.render(scale);
        handle.render(scale);
        blade.render(scale);
    }
}
