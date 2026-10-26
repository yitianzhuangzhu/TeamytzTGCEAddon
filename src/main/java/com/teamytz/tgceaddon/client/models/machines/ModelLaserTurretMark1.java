// Made with Blockbench (由 bbmodel2java.js 生成，规则与 Blockbench 1.12 导出一致)

package com.teamytz.tgceaddon.client.models.machines;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import com.teamytz.tgceaddon.client.models.ITurretModel;

public class ModelLaserTurretMark1 extends ModelBase implements ITurretModel {
	private final ModelRenderer main;
	private final ModelRenderer turret;
	private final ModelRenderer l;
	private final ModelRenderer cube_r1;
	private final ModelRenderer r;
	private final ModelRenderer cube_r2;
	private final ModelRenderer base;

	public ModelLaserTurretMark1() {
		textureWidth = 256;
		textureHeight = 256;

		main = new ModelRenderer(this);
		main.setRotationPoint(0.0F, 24.0F, 0.0F);

		turret = new ModelRenderer(this);
		turret.setRotationPoint(0.0F, -14.0F, 0.0F);
		main.addChild(turret);

		l = new ModelRenderer(this);
		l.setRotationPoint(26.0F, 16.0F, 0.0F);
		turret.addChild(l);
		l.cubeList.add(new ModelBox(l, 64, 190, -17.0F, -18.0F, -2.0F, 1, 4, 4, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 216, 159, -16.0F, -19.0F, -3.0F, 1, 6, 6, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 0, 122, -15.0F, -18.0F, -4.0F, 9, 4, 18, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 124, 175, -15.0F, -20.0F, -29.0F, 9, 8, 8, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 114, 209, -16.0F, -19.0F, -29.0F, 1, 6, 8, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 132, 209, -6.0F, -19.0F, -29.0F, 1, 6, 8, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 146, 160, -15.0F, -20.0F, 6.0F, 9, 2, 13, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 0, 144, -14.0F, -18.0F, -21.0F, 7, 4, 17, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 146, 144, -15.0F, -21.0F, -7.0F, 9, 3, 13, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 0, 165, -15.0F, -23.0F, -7.0F, 9, 2, 11, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 58, 104, -15.0F, -14.0F, -21.0F, 9, 3, 19, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 142, 20, -14.0F, -11.0F, -21.0F, 7, 1, 19, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 144, 58, -15.0F, -22.0F, -21.0F, 9, 4, 14, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 194, 26, -14.0F, -21.0F, -29.0F, 7, 1, 8, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 190, 205, -13.0F, -22.0F, -29.0F, 5, 1, 8, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 206, 75, -13.0F, -11.0F, -29.0F, 5, 1, 8, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 200, 118, -14.0F, -12.0F, -29.0F, 7, 1, 8, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 0, 178, -15.0F, -14.0F, -2.0F, 9, 2, 9, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 108, 191, -15.0F, -13.0F, -2.0F, 9, 3, 7, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 136, 201, -15.5F, -18.0F, 14.0F, 10, 4, 4, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 194, 35, -12.5F, -21.0F, 16.0F, 4, 1, 4, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 216, 194, -12.5F, -27.0F, 12.0F, 4, 4, 3, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 216, 207, -12.5F, -26.0F, 15.0F, 4, 3, 3, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 216, 62, -12.0F, -26.5F, 7.0F, 3, 3, 5, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 206, 84, -13.5F, -22.0F, 10.0F, 6, 2, 6, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 172, 214, -12.5F, -23.0F, 10.0F, 4, 1, 7, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 84, 53, -11.5F, -21.0F, 6.0F, 2, 1, 2, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 156, 76, -12.5F, -22.0F, 8.0F, 4, 2, 2, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 194, 12, -13.0F, -18.5F, -38.0F, 5, 5, 9, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 194, 171, -7.5F, -15.0F, -41.0F, 1, 4, 12, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 52, 217, -7.5F, -11.0F, -41.0F, 1, 2, 4, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 62, 217, -7.5F, -23.0F, -41.0F, 1, 2, 4, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 218, 40, -14.5F, -23.0F, -41.0F, 1, 2, 4, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 218, 46, -14.5F, -11.0F, -41.0F, 1, 2, 4, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 220, 171, -14.5F, -11.0F, -37.0F, 1, 1, 4, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 220, 176, -14.5F, -22.0F, -37.0F, 1, 1, 4, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 220, 181, -8.0F, -22.0F, -37.0F, 1, 1, 4, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 0, 222, -8.0F, -11.0F, -37.0F, 1, 1, 4, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 0, 201, -7.5F, -21.0F, -41.0F, 1, 4, 12, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 48, 152, -7.5F, -17.0F, -31.0F, 1, 2, 2, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 48, 156, -14.5F, -17.0F, -31.0F, 1, 2, 2, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 26, 201, -14.5F, -15.0F, -41.0F, 1, 4, 12, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 52, 201, -14.5F, -21.0F, -41.0F, 1, 4, 12, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 194, 214, -12.5F, -23.0F, -37.0F, 4, 1, 7, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 216, 213, -11.5F, -24.0F, -36.0F, 2, 1, 4, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 216, 54, -12.5F, -10.0F, -37.0F, 4, 1, 7, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 0, 217, -11.5F, -9.0F, -36.0F, 2, 1, 4, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 158, 175, -13.5F, -22.0F, -41.0F, 6, 2, 12, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 158, 189, -13.5F, -20.0F, -39.0F, 6, 2, 10, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 40, 176, -13.5F, -12.0F, -41.0F, 6, 2, 12, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 190, 0, -13.5F, -14.0F, -39.0F, 6, 2, 10, 0.0F, false));

		cube_r1 = new ModelRenderer(this);
		cube_r1.setRotationPoint(-10.5F, -14.0F, 18.0F);
		l.addChild(cube_r1);
		setRotationAngle(cube_r1, -2.7053F, 0.0F, 0.0F);
		cube_r1.cubeList.add(new ModelBox(cube_r1, 114, 104, -4.5F, -1.0F, -4.0F, 9, 6, 18, 0.0F, false));

		r = new ModelRenderer(this);
		r.setRotationPoint(-5.0F, 16.0F, 0.0F);
		turret.addChild(r);
		r.cubeList.add(new ModelBox(r, 76, 176, -5.0F, -18.0F, -2.0F, 1, 4, 4, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 88, 163, -6.0F, -19.0F, -3.0F, 1, 6, 6, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 116, 80, -15.0F, -18.0F, -4.0F, 9, 4, 18, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 168, 102, -15.0F, -20.0F, -29.0F, 9, 8, 8, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 78, 208, -16.0F, -19.0F, -29.0F, 1, 6, 8, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 96, 209, -6.0F, -19.0F, -29.0F, 1, 6, 8, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 102, 160, -15.0F, -20.0F, 6.0F, 9, 2, 13, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 54, 142, -14.0F, -18.0F, -21.0F, 7, 4, 17, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 102, 144, -15.0F, -21.0F, -7.0F, 9, 3, 13, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 48, 163, -15.0F, -23.0F, -7.0F, 9, 2, 11, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 82, 0, -15.0F, -14.0F, -21.0F, 9, 3, 19, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 138, 0, -14.0F, -11.0F, -21.0F, 7, 1, 19, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 144, 40, -15.0F, -22.0F, -21.0F, 9, 4, 14, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 82, 22, -14.0F, -21.0F, -29.0F, 7, 1, 8, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 164, 201, -13.0F, -22.0F, -29.0F, 5, 1, 8, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 202, 101, -13.0F, -11.0F, -29.0F, 5, 1, 8, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 112, 22, -14.0F, -12.0F, -29.0F, 7, 1, 8, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 170, 90, -15.0F, -14.0F, -2.0F, 9, 2, 9, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 168, 118, -15.0F, -13.0F, -2.0F, 9, 3, 7, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 108, 201, -15.5F, -18.0F, 14.0F, 10, 4, 4, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 166, 139, -12.5F, -21.0F, 16.0F, 4, 1, 4, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 216, 187, -12.5F, -27.0F, 12.0F, 4, 4, 3, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 216, 201, -12.5F, -26.0F, 15.0F, 4, 3, 3, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 140, 191, -12.0F, -26.5F, 7.0F, 3, 3, 5, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 202, 110, -13.5F, -22.0F, 10.0F, 6, 2, 6, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 206, 92, -12.5F, -23.0F, 10.0F, 4, 1, 7, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 76, 53, -11.5F, -21.0F, 6.0F, 2, 1, 2, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 144, 76, -12.5F, -22.0F, 8.0F, 4, 2, 2, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 190, 40, -13.0F, -18.5F, -38.0F, 5, 5, 9, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 190, 54, -7.5F, -15.0F, -41.0F, 1, 4, 12, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 12, 217, -7.5F, -11.0F, -41.0F, 1, 2, 4, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 22, 217, -7.5F, -23.0F, -41.0F, 1, 2, 4, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 32, 217, -14.5F, -23.0F, -41.0F, 1, 2, 4, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 42, 217, -14.5F, -11.0F, -41.0F, 1, 2, 4, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 150, 218, -14.5F, -11.0F, -37.0F, 1, 1, 4, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 160, 218, -14.5F, -22.0F, -37.0F, 1, 1, 4, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 216, 218, -7.5F, -22.0F, -37.0F, 1, 1, 4, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 220, 70, -8.0F, -11.0F, -37.0F, 1, 1, 4, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 190, 155, -7.5F, -21.0F, -41.0F, 1, 4, 12, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 48, 144, -7.5F, -17.0F, -31.0F, 1, 2, 2, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 48, 148, -14.5F, -17.0F, -31.0F, 1, 2, 2, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 190, 139, -14.5F, -15.0F, -41.0F, 1, 4, 12, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 190, 189, -14.5F, -21.0F, -41.0F, 1, 4, 12, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 206, 127, -12.5F, -23.0F, -37.0F, 4, 1, 7, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 76, 184, -11.5F, -24.0F, -36.0F, 2, 1, 4, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 150, 210, -12.5F, -10.0F, -37.0F, 4, 1, 7, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 210, 35, -11.5F, -9.0F, -36.0F, 2, 1, 4, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 170, 76, -13.5F, -22.0F, -41.0F, 6, 2, 12, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 76, 189, -13.5F, -20.0F, -39.0F, 6, 2, 10, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 88, 175, -13.5F, -12.0F, -41.0F, 6, 2, 12, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 0, 189, -13.5F, -14.0F, -39.0F, 6, 2, 10, 0.0F, false));

		cube_r2 = new ModelRenderer(this);
		cube_r2.setRotationPoint(-10.5F, -14.0F, 18.0F);
		r.addChild(cube_r2);
		setRotationAngle(cube_r2, -2.7053F, 0.0F, 0.0F);
		cube_r2.cubeList.add(new ModelBox(cube_r2, 62, 80, -4.5F, -1.0F, -4.0F, 9, 6, 18, 0.0F, false));

		base = new ModelRenderer(this);
		base.setRotationPoint(0.0F, 0.0F, 0.0F);
		main.addChild(base);
		base.cubeList.add(new ModelBox(base, 216, 147, 8.0F, -17.0F, -3.0F, 1, 6, 6, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 216, 135, -9.0F, -17.0F, -3.0F, 1, 6, 6, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 54, 126, -7.0F, -2.0F, -7.0F, 14, 2, 14, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 110, 128, -7.0F, -5.0F, -7.0F, 14, 2, 14, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 166, 128, -5.0F, -3.0F, -5.0F, 10, 1, 10, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 32, 190, -4.0F, -8.0F, -4.0F, 8, 3, 8, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 0, 0, -8.0F, -17.0F, -14.0F, 16, 6, 25, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 190, 70, -7.0F, -16.0F, -15.0F, 14, 4, 1, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 78, 201, -7.0F, -18.0F, 11.0F, 14, 6, 1, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 0, 56, -7.0F, -11.0F, -13.0F, 14, 1, 23, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 76, 31, -6.0F, -10.0F, -12.0F, 12, 1, 21, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 0, 102, -5.0F, -9.0F, -11.0F, 10, 1, 19, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 0, 80, -5.0F, -20.0F, -11.0F, 10, 1, 21, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 74, 56, -6.0F, -19.0F, -12.0F, 12, 1, 23, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 0, 31, -7.0F, -18.0F, -13.0F, 14, 1, 24, 0.0F, false));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		main.render(f5);
	}

	/**
	 * 渲染炮塔(带动画):俯仰组 turret 绕自身旋转点绕 X 轴旋转,其余分组不动。
	 * 俯仰角语义与 BMPT 一致(MC 语义:正=低头、负=抬头);渲完必须复位,否则污染下一帧。
	 */
	@Override
	public void renderTurret(Entity entity, float f, float f1, float f2, float f3, float f4, float f5,
	                         float turretPitchDeg) {
		float pitchRad = (float) Math.toRadians(turretPitchDeg);
		turret.rotateAngleX = pitchRad;
		main.render(f5);
		turret.rotateAngleX = 0.0F;
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
