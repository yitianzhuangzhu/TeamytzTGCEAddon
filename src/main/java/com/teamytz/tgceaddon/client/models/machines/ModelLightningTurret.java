// Made with Blockbench (由 bbmodel2java.js 生成，规则与 Blockbench 1.12 导出一致)

package com.teamytz.tgceaddon.client.models.machines;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import com.teamytz.tgceaddon.client.models.ITurretModel;

public class ModelLightningTurret extends ModelBase implements ITurretModel {
	private final ModelRenderer BASE;
	private final ModelRenderer main;

	public ModelLightningTurret() {
		textureWidth = 128;
		textureHeight = 128;

		BASE = new ModelRenderer(this);
		BASE.setRotationPoint(0.0F, 24.0F, 0.0F);
		BASE.cubeList.add(new ModelBox(BASE, 0, 0, -8.0F, -2.0F, -8.0F, 16, 2, 16, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 20, 65, 5.0F, -4.0F, -5.0F, 1, 2, 2, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 90, 13, 5.0F, -4.0F, 3.0F, 1, 2, 2, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 16, 79, 5.0F, -4.0F, -2.0F, 1, 2, 1, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 84, 67, -2.0F, -3.0F, 1.0F, 8, 1, 1, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 28, 51, 5.0F, -4.0F, -1.0F, 1, 1, 3, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 62, 36, 3.0F, -4.0F, -6.0F, 2, 2, 1, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 0, 39, -5.0F, -4.0F, -5.0F, 10, 2, 10, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 64, 0, -4.0F, -5.0F, -4.0F, 8, 1, 8, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 94, 17, 1.0F, -3.0F, -6.0F, 1, 1, 1, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 16, 91, -1.0F, -4.0F, -6.0F, 3, 1, 1, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 30, 91, -2.0F, -4.0F, -6.0F, 1, 2, 1, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 58, 91, -5.0F, -4.0F, -6.0F, 2, 2, 1, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 24, 91, 3.0F, -4.0F, 5.0F, 2, 2, 1, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 36, 51, 1.0F, -4.0F, 5.0F, 1, 2, 1, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 94, 21, -2.0F, -3.0F, 5.0F, 1, 1, 1, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 76, 91, -1.0F, -4.0F, 5.0F, 2, 1, 1, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 94, 19, -2.0F, -4.0F, 5.0F, 1, 1, 1, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 52, 91, -5.0F, -4.0F, 5.0F, 2, 2, 1, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 90, 9, -6.0F, -4.0F, -5.0F, 1, 2, 2, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 76, 93, -6.0F, -3.0F, -2.0F, 1, 1, 1, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 94, 23, -6.0F, -3.0F, 1.0F, 1, 1, 1, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 88, 89, -6.0F, -4.0F, -2.0F, 1, 1, 4, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 62, 32, -6.0F, -4.0F, 3.0F, 1, 2, 2, 0.0F, false));
		BASE.cubeList.add(new ModelBox(BASE, 72, 25, -2.0F, -8.0F, -2.0F, 4, 3, 4, 0.0F, false));
		main = new ModelRenderer(this);
		main.setRotationPoint(0.0F, 17.0F, 0.0F);
		main.cubeList.add(new ModelBox(main, 68, 32, -3.0F, -1.0F, -5.0F, 6, 1, 9, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 88, 75, -2.0F, -1.0F, -8.0F, 4, 1, 3, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 84, 60, -3.0F, -7.0F, -10.0F, 6, 6, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 70, 91, -1.0F, -5.0F, -23.0F, 2, 2, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 40, 77, -4.0F, -6.0F, -30.0F, 1, 5, 7, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 34, 83, -4.0F, -8.0F, -14.0F, 1, 8, 2, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 88, 79, -4.0F, -8.0F, -20.0F, 1, 8, 2, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 56, 77, 3.0F, -6.0F, -30.0F, 1, 5, 7, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 66, 69, -2.0F, -8.0F, -30.0F, 4, 1, 7, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 68, 52, -3.0F, -8.0F, -20.0F, 6, 1, 2, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 88, 69, -3.0F, -8.0F, -14.0F, 6, 1, 2, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 72, 88, -3.0F, -1.0F, -14.0F, 6, 1, 2, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 88, 72, -3.0F, -1.0F, -20.0F, 6, 1, 2, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 72, 17, -2.0F, -1.0F, -30.0F, 4, 1, 7, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 44, 18, 2.0F, -7.0F, -23.0F, 1, 1, 13, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 0, 51, -3.0F, -7.0F, -23.0F, 1, 1, 13, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 28, 55, -3.0F, -2.0F, -23.0F, 1, 1, 13, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 56, 55, 2.0F, -2.0F, -23.0F, 1, 1, 13, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 44, 32, -4.0F, -7.0F, -9.0F, 8, 6, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 52, 89, -3.0F, -8.0F, -9.0F, 6, 1, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 0, 18, -4.0F, -8.0F, -8.0F, 8, 7, 14, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 16, 83, -4.0F, -8.0F, 6.0F, 8, 7, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 88, 25, -3.0F, -8.0F, 7.0F, 6, 6, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 68, 42, -3.0F, -9.0F, -4.0F, 6, 1, 9, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 66, 89, -3.0F, -16.0F, 5.0F, 1, 8, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 82, 91, -4.0F, -10.0F, -5.0F, 1, 2, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 20, 93, -4.0F, -10.0F, 5.0F, 1, 2, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 46, 89, 3.0F, -8.0F, -14.0F, 1, 8, 2, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 40, 89, 3.0F, -8.0F, -20.0F, 1, 8, 2, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 64, 9, -3.0F, -2.0F, -30.0F, 6, 1, 7, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 72, 77, 2.0F, -6.0F, -30.0F, 1, 4, 7, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 84, 52, -2.0F, -6.0F, -27.0F, 4, 4, 4, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 0, 79, -3.0F, -6.0F, -30.0F, 1, 4, 7, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 40, 69, -3.0F, -7.0F, -30.0F, 6, 1, 7, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 20, 69, -6.0F, -8.0F, -1.0F, 2, 6, 8, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 40, 39, -1.0F, -6.0F, -22.0F, 2, 4, 12, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 8, 90, -5.0F, -7.0F, -4.0F, 1, 4, 3, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 0, 65, 4.0F, -8.0F, -1.0F, 2, 6, 8, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 0, 90, 4.0F, -7.0F, -4.0F, 1, 4, 3, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 16, 93, 3.0F, -10.0F, 5.0F, 1, 2, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 34, 93, 3.0F, -10.0F, -5.0F, 1, 2, 1, 0.0F, false));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		BASE.render(f5);
		main.render(f5);
	}

	/**
	 * 渲染炮塔(带动画):俯仰组 main 绕自身旋转点绕 X 轴旋转,其余分组不动。
	 * 俯仰角语义与 BMPT 一致(MC 语义:正=低头、负=抬头);渲完必须复位,否则污染下一帧。
	 */
	@Override
	public void renderTurret(Entity entity, float f, float f1, float f2, float f3, float f4, float f5,
	                         float turretPitchDeg) {
		float pitchRad = (float) Math.toRadians(turretPitchDeg);
		main.rotateAngleX = pitchRad;
		BASE.render(f5);
		main.render(f5);
		main.rotateAngleX = 0.0F;
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
