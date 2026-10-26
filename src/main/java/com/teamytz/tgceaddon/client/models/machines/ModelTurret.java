// Made with Blockbench (由 bbmodel2java.js 生成，规则与 Blockbench 1.12 导出一致)

package com.teamytz.tgceaddon.client.models.machines;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import com.teamytz.tgceaddon.client.models.ITurretModel;

public class ModelTurret extends ModelBase implements ITurretModel {
	private final ModelRenderer main;
	private final ModelRenderer base;
	private final ModelRenderer gun;
	private final ModelRenderer cube_r1;
	private final ModelRenderer cube_r2;

	public ModelTurret() {
		textureWidth = 256;
		textureHeight = 256;

		main = new ModelRenderer(this);
		main.setRotationPoint(0.0F, 24.0F, 0.0F);

		base = new ModelRenderer(this);
		base.setRotationPoint(0.0F, 0.0F, 0.0F);
		main.addChild(base);
		base.cubeList.add(new ModelBox(base, 88, 0, -8.0F, -2.0F, -8.0F, 16, 2, 16, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 84, 44, -9.0F, -14.0F, -9.0F, 18, 12, 18, 0.0F, false));

		gun = new ModelRenderer(this);
		gun.setRotationPoint(0.0F, -9.0F, 0.0F);
		main.addChild(gun);
		gun.cubeList.add(new ModelBox(gun, 98, 25, 1.0F, -3.0F, -54.0F, 1, 3, 1, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 98, 39, 1.0F, -3.0F, -58.0F, 1, 3, 1, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 102, 25, 1.0F, -2.0F, -56.0F, 1, 2, 1, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 102, 39, -2.0F, -2.0F, -54.0F, 1, 2, 1, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 84, 102, -2.0F, -2.0F, -58.0F, 1, 2, 1, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 88, 102, -2.0F, -2.0F, -56.0F, 1, 2, 1, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 88, 29, -2.0F, 0.0F, -57.0F, 1, 1, 3, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 88, 33, -2.0F, -3.0F, -57.0F, 1, 1, 3, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 88, 37, 1.0F, -3.0F, -57.0F, 1, 1, 3, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 96, 29, 1.0F, 0.0F, -57.0F, 1, 1, 3, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 96, 33, -2.0F, -3.0F, -54.0F, 3, 1, 1, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 96, 35, -2.0F, -3.0F, -58.0F, 3, 1, 1, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 96, 37, -2.0F, -3.0F, -56.0F, 3, 1, 1, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 88, 25, -2.0F, 0.0F, -54.0F, 4, 1, 1, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 88, 27, -2.0F, 0.0F, -58.0F, 4, 1, 1, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 88, 41, -2.0F, 0.0F, -56.0F, 4, 1, 1, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 0, 0, -2.0F, -3.0F, -53.0F, 4, 4, 40, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 88, 18, -4.0F, -4.0F, -16.0F, 8, 6, 1, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 84, 74, -9.0F, -8.0F, 7.0F, 18, 11, 17, 0.0F, false));

		cube_r1 = new ModelRenderer(this);
		cube_r1.setRotationPoint(0.0F, 7.0F, 3.0F);
		gun.addChild(cube_r1);
		setRotationAngle(cube_r1, 0.3102F, 0.0F, 0.0F);
		cube_r1.cubeList.add(new ModelBox(cube_r1, 0, 79, -8.0F, -16.0F, -14.0F, 16, 8, 26, 0.0F, false));

		cube_r2 = new ModelRenderer(this);
		cube_r2.setRotationPoint(0.0F, 7.0F, 0.0F);
		gun.addChild(cube_r2);
		setRotationAngle(cube_r2, 0.1106F, 0.0F, 0.0F);
		cube_r2.cubeList.add(new ModelBox(cube_r2, 0, 44, -8.0F, -13.0F, -14.0F, 16, 9, 26, 0.0F, false));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		main.render(f5);
	}

	/**
	 * 渲染炮塔(带动画):俯仰组 gun 绕自身旋转点绕 X 轴旋转,其余分组不动。
	 * 俯仰角语义与 BMPT 一致(MC 语义:正=低头、负=抬头);渲完必须复位,否则污染下一帧。
	 */
	@Override
	public void renderTurret(Entity entity, float f, float f1, float f2, float f3, float f4, float f5,
	                         float turretPitchDeg) {
		float pitchRad = (float) Math.toRadians(turretPitchDeg);
		gun.rotateAngleX = pitchRad;
		main.render(f5);
		gun.rotateAngleX = 0.0F;
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
