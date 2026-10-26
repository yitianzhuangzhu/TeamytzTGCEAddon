// Made with Blockbench (由 bbmodel2java.js 生成，规则与 Blockbench 1.12 导出一致)

package com.teamytz.tgceaddon.client.models.projectiles;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelHEShell extends ModelBase {
	private final ModelRenderer main;

	public ModelHEShell() {
		textureWidth = 32;
		textureHeight = 32;

		main = new ModelRenderer(this);
		main.setRotationPoint(0.0F, 24.0F, 0.0F);
		main.cubeList.add(new ModelBox(main, 0, 6, -6.0F, -2.0F, -1.0F, 3, 2, 2, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 0, 10, 7.0F, -2.0F, -1.0F, 1, 2, 2, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 6, 10, -7.0F, -1.5F, -0.5F, 1, 1, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 0, 0, -3.0F, -2.5F, -1.5F, 10, 3, 3, 0.0F, false));
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
