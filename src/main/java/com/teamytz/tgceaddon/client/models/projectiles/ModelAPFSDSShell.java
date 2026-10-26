// Made with Blockbench (由 bbmodel2java.js 生成，规则与 Blockbench 1.12 导出一致)

package com.teamytz.tgceaddon.client.models.projectiles;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelAPFSDSShell extends ModelBase {
	private final ModelRenderer main;
	private final ModelRenderer cube_r1;
	private final ModelRenderer cube_r2;
	private final ModelRenderer cube_r3;
	private final ModelRenderer cube_r4;
	private final ModelRenderer cube_r5;
	private final ModelRenderer cube_r6;

	public ModelAPFSDSShell() {
		textureWidth = 32;
		textureHeight = 32;

		main = new ModelRenderer(this);
		main.setRotationPoint(0.0F, 24.0F, 0.0F);
		main.cubeList.add(new ModelBox(main, 0, 0, -5.0F, -1.0F, -0.5F, 13, 1, 1, 0.0F, false));

		cube_r1 = new ModelRenderer(this);
		cube_r1.setRotationPoint(-5.5F, -0.5F, 0.0F);
		main.addChild(cube_r1);
		setRotationAngle(cube_r1, -0.7854F, 0.0F, 0.0F);
		cube_r1.cubeList.add(new ModelBox(cube_r1, 0, 3, -2.5F, -0.5F, 0.0F, 3, 1, 0, 0.0F, false));

		cube_r2 = new ModelRenderer(this);
		cube_r2.setRotationPoint(-5.5F, -0.5F, 0.0F);
		main.addChild(cube_r2);
		setRotationAngle(cube_r2, 0.7854F, 0.0F, 0.0F);
		cube_r2.cubeList.add(new ModelBox(cube_r2, 0, 2, -2.5F, -0.5F, 0.0F, 3, 1, 0, 0.0F, false));

		cube_r3 = new ModelRenderer(this);
		cube_r3.setRotationPoint(2.5F, -1.0F, -0.5F);
		main.addChild(cube_r3);
		setRotationAngle(cube_r3, 0.7854F, 0.0F, 0.0F);
		cube_r3.cubeList.add(new ModelBox(cube_r3, 0, 6, 2.5F, -1.0F, 0.0F, 3, 1, 0, 0.0F, false));

		cube_r4 = new ModelRenderer(this);
		cube_r4.setRotationPoint(2.5F, 0.0F, -0.5F);
		main.addChild(cube_r4);
		setRotationAngle(cube_r4, 2.3562F, 0.0F, 0.0F);
		cube_r4.cubeList.add(new ModelBox(cube_r4, 0, 5, 2.5F, -1.0F, 0.0F, 3, 1, 0, 0.0F, false));

		cube_r5 = new ModelRenderer(this);
		cube_r5.setRotationPoint(2.5F, 0.0F, 0.5F);
		main.addChild(cube_r5);
		setRotationAngle(cube_r5, -2.3562F, 0.0F, 0.0F);
		cube_r5.cubeList.add(new ModelBox(cube_r5, 6, 2, 2.5F, -1.0F, 0.0F, 3, 1, 0, 0.0F, false));

		cube_r6 = new ModelRenderer(this);
		cube_r6.setRotationPoint(2.5F, -1.0F, 0.5F);
		main.addChild(cube_r6);
		setRotationAngle(cube_r6, -0.7854F, 0.0F, 0.0F);
		cube_r6.cubeList.add(new ModelBox(cube_r6, 0, 4, 2.5F, -1.0F, 0.0F, 3, 1, 0, 0.0F, false));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		main.render(f5);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
