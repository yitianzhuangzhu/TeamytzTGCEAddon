package com.teamytz.tgceaddon.client.models.armor;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * 风暴盾牌模型
 * forcefield 组仅在举盾时显示
 */
public class ModelStormShield extends ModelBase {
    private final ModelRenderer main;
    private final ModelRenderer forcefield;
    private final ModelRenderer mainbody;
    private final ModelRenderer de;
    private final ModelRenderer handle;

    public ModelStormShield() {
        textureWidth = 128;
        textureHeight = 128;

        main = new ModelRenderer(this);
        main.setRotationPoint(0.0F, 24.0F, 0.0F);
        main.rotateAngleY = -1.5708F;

        forcefield = new ModelRenderer(this);
        forcefield.setRotationPoint(0.0F, 0.0F, 0.0F);
        main.addChild(forcefield);
        forcefield.setTextureOffset(0, 0).addBox(-12, -53, -17, 1, 53, 34);

        mainbody = new ModelRenderer(this);
        mainbody.setRotationPoint(0.0F, 0.0F, 0.0F);
        main.addChild(mainbody);
        mainbody.setTextureOffset(70, 45).addBox(-1, -20, -8, 2, 20, 16);
        mainbody.setTextureOffset(70, 0).addBox(-1, -43, -11, 2, 23, 22);
        mainbody.setTextureOffset(70, 89).addBox(-1, -48, -8, 2, 5, 16);
        mainbody.setTextureOffset(0, 94).addBox(-3, -39, 10, 5, 15, 3);
        mainbody.setTextureOffset(16, 94).addBox(-3, -39, -13, 5, 15, 3);
        mainbody.setTextureOffset(106, 93).addBox(-1, -23, 11, 2, 3, 3);
        mainbody.setTextureOffset(106, 87).addBox(-1, -23, -14, 2, 3, 3);
        mainbody.setTextureOffset(6, 115).addBox(-1, -45, -9, 2, 1, 1);
        mainbody.setTextureOffset(46, 115).addBox(-1, -40, -12, 2, 1, 1);
        mainbody.setTextureOffset(52, 115).addBox(-1, -24, -12, 2, 1, 1);
        mainbody.setTextureOffset(64, 115).addBox(-1, -40, 11, 2, 1, 1);
        mainbody.setTextureOffset(70, 115).addBox(-1, -24, 11, 2, 1, 1);
        mainbody.setTextureOffset(114, 61).addBox(-1, -45, 8, 2, 1, 1);
        mainbody.setTextureOffset(8, 112).addBox(-1, -44, 8, 2, 1, 2);
        mainbody.setTextureOffset(0, 112).addBox(-1, -44, -10, 2, 1, 2);
        mainbody.setTextureOffset(106, 81).addBox(-1, -43, -14, 2, 3, 3);
        mainbody.setTextureOffset(106, 75).addBox(-1, -43, 11, 2, 3, 3);

        de = new ModelRenderer(this);
        de.setRotationPoint(0.0F, 0.0F, 0.0F);
        mainbody.addChild(de);
        de.setTextureOffset(62, 94).addBox(-2, -41, -11, 1, 19, 1);
        de.setTextureOffset(124, 0).addBox(-2, -46, -4, 1, 5, 1);
        de.setTextureOffset(109, 0).addBox(-2, -46, 3, 1, 5, 1);
        de.setTextureOffset(16, 112).addBox(-2, -29, 8, 1, 3, 2);
        de.setTextureOffset(22, 112).addBox(-2, -37, -10, 1, 3, 2);
        de.setTextureOffset(40, 114).addBox(-2, -29, -10, 1, 3, 2);
        de.setTextureOffset(112, 111).addBox(-2, -37, 8, 1, 3, 2);
        de.setTextureOffset(114, 51).addBox(1, -37, 8, 1, 3, 2);
        de.setTextureOffset(114, 56).addBox(1, -37, -10, 1, 3, 2);
        de.setTextureOffset(58, 114).addBox(1, -29, 8, 1, 3, 2);
        de.setTextureOffset(0, 115).addBox(1, -29, -10, 1, 3, 2);
        de.setTextureOffset(32, 94).addBox(-2, -21, -7, 1, 20, 1);
        de.setTextureOffset(36, 94).addBox(-2, -21, 6, 1, 20, 1);
        de.setTextureOffset(40, 94).addBox(-2, -47, -4, 1, 1, 8);
        de.setTextureOffset(74, 120).addBox(-2, -42, -11, 1, 1, 7);
        de.setTextureOffset(62, 120).addBox(-2, -42, 4, 1, 1, 7);
        de.setTextureOffset(40, 109).addBox(-2, -2, 2, 1, 1, 4);
        de.setTextureOffset(66, 110).addBox(-2, -2, -6, 1, 1, 4);
        de.setTextureOffset(106, 63).addBox(-2, -22, 6, 1, 1, 5);
        de.setTextureOffset(106, 69).addBox(-2, -22, -11, 1, 1, 5);
        de.setTextureOffset(58, 94).addBox(-2, -41, 10, 1, 19, 1);
        de.setTextureOffset(50, 109).addBox(-2, -48, -8, 1, 3, 3);
        de.setTextureOffset(104, 111).addBox(-2, -4, -2, 1, 3, 3);
        de.setTextureOffset(96, 110).addBox(-2, -48, 5, 1, 3, 3);

        handle = new ModelRenderer(this);
        handle.setRotationPoint(0.0F, 0.0F, 0.0F);
        mainbody.addChild(handle);
        handle.setTextureOffset(106, 51).addBox(3, -35, -6, 2, 10, 2);
        handle.setTextureOffset(106, 105).addBox(1, -38, -6, 4, 1, 2);
        handle.setTextureOffset(106, 108).addBox(1, -23, -6, 4, 1, 2);
        handle.setTextureOffset(106, 99).addBox(1, -22, -6, 3, 4, 2);
        handle.setTextureOffset(40, 103).addBox(1, -25, -7, 5, 2, 4);
        handle.setTextureOffset(106, 45).addBox(1, -37, -7, 5, 2, 4);
    }

    /**
     * 渲染所有部件（包括 forcefield）
     */
    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        main.render(f5);
    }

    /**
     * 仅渲染主体（不包括 forcefield）
     * 用于未举盾时显示
     */
    public void renderWithoutForcefield(float scale) {
        // 只渲染 mainbody, de, handle（跳过 forcefield）
        mainbody.render(scale);
    }

    /**
     * 仅渲染 forcefield（能量护盾）
     * 用于举盾时显示
     */
    public void renderForcefield(float scale) {
        forcefield.render(scale);
    }

    /**
     * 获取 forcefield 引用，用于外部渲染
     */
    public ModelRenderer getForcefield() {
        return forcefield;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
