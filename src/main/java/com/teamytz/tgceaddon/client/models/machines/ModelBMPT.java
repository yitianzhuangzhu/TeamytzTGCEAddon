package com.teamytz.tgceaddon.client.models.machines;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.7 - 1.12
/**
 * BMPT 终结者坦克炮塔模型（Blockbench 导出）
 */
public class ModelBMPT extends ModelBase {
	private final ModelRenderer main;
	private final ModelRenderer r;
	private final ModelRenderer cube_r1;
	private final ModelRenderer cube_r2;
	private final ModelRenderer l;
	private final ModelRenderer cube_r3;
	private final ModelRenderer cube_r4;
	private final ModelRenderer base;
	private final ModelRenderer cube_r5;
	private final ModelRenderer gun;
	private final ModelRenderer cube_r6;
	/** 左火箭巢俯仰容器（旋转轴与 gun 一致） */
	private final ModelRenderer pitchL;
	private final ModelRenderer rol;
	private final ModelRenderer rocketleft_r1;
	/** 右火箭巢俯仰容器（旋转轴与 gun 一致） */
	private final ModelRenderer pitchR;
	private final ModelRenderer ror;
	private final ModelRenderer rocketright_r1;

	public ModelBMPT() {
		textureWidth = 256;
		textureHeight = 256;

		main = new ModelRenderer(this);
		main.setRotationPoint(0.0F, 14.0F, 0.0F);
		

		r = new ModelRenderer(this);
		r.setRotationPoint(-8.0F, 5.0F, 4.0F);
		main.addChild(r);
		r.cubeList.add(new ModelBox(r, 162, 8, -3.0F, -8.0F, -7.0F, 5, 4, 15, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 134, 133, -3.0F, -12.0F, -6.0F, 5, 4, 21, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 146, 189, -3.0F, -13.0F, 6.0F, 5, 1, 8, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 40, 194, -3.0F, -20.0F, 8.0F, 5, 7, 5, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 116, 196, -3.0F, -10.0F, -10.0F, 5, 2, 4, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 80, 196, -3.0F, -4.0F, -5.0F, 5, 2, 4, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 110, 74, -3.5F, -10.5F, -11.0F, 6, 3, 1, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 164, 158, -15.0F, -10.0F, -6.0F, 12, 6, 9, 0.0F, false));

		cube_r1 = new ModelRenderer(this);
		cube_r1.setRotationPoint(0.0F, -11.0F, -3.0F);
		r.addChild(cube_r1);
		setRotationAngle(cube_r1, 0.3927F, 0.0F, 0.0F);
		cube_r1.cubeList.add(new ModelBox(cube_r1, 60, 194, -3.0F, -2.0F, -7.0F, 5, 2, 5, 0.0F, false));

		cube_r2 = new ModelRenderer(this);
		cube_r2.setRotationPoint(0.0F, 0.0F, 0.0F);
		r.addChild(cube_r2);
		setRotationAngle(cube_r2, 0.48F, 0.0F, 0.0F);
		cube_r2.cubeList.add(new ModelBox(cube_r2, 152, 74, -3.0F, -4.0F, -1.0F, 5, 4, 18, 0.0F, false));

		l = new ModelRenderer(this);
		l.setRotationPoint(7.0F, 5.0F, 4.0F);
		main.addChild(l);
		l.cubeList.add(new ModelBox(l, 172, 189, -1.0F, -13.0F, 6.0F, 5, 1, 8, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 162, 27, -1.0F, -8.0F, -7.0F, 5, 4, 15, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 164, 174, 4.0F, -10.0F, -6.0F, 12, 6, 9, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 82, 133, -1.0F, -12.0F, -6.0F, 5, 4, 21, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 64, 152, -1.0F, -10.0F, -10.0F, 5, 2, 4, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 124, 74, -1.5F, -10.5F, -11.0F, 6, 3, 1, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 46, 152, -1.0F, -4.0F, -5.0F, 5, 2, 4, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 36, 195, -0.5F, -26.0F, 11.5F, 1, 9, 1, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 178, 126, -1.0F, -17.0F, 11.0F, 2, 4, 2, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 134, 196, 3.0F, -19.0F, 12.0F, 1, 6, 1, 0.0F, false));

		cube_r3 = new ModelRenderer(this);
		cube_r3.setRotationPoint(2.0F, -11.0F, -3.0F);
		l.addChild(cube_r3);
		setRotationAngle(cube_r3, 0.3927F, 0.0F, 0.0F);
		cube_r3.cubeList.add(new ModelBox(cube_r3, 0, 195, -3.0F, -2.0F, -7.0F, 5, 2, 5, 0.0F, false));

		cube_r4 = new ModelRenderer(this);
		cube_r4.setRotationPoint(0.0F, 0.0F, 0.0F);
		l.addChild(cube_r4);
		setRotationAngle(cube_r4, 0.48F, 0.0F, 0.0F);
		cube_r4.cubeList.add(new ModelBox(cube_r4, 0, 136, -1.0F, -4.0F, -1.0F, 5, 4, 18, 0.0F, false));

		base = new ModelRenderer(this);
		base.setRotationPoint(0.0F, 0.0F, 0.0F);
		main.addChild(base);
		base.cubeList.add(new ModelBox(base, 82, 116, -8.0F, 9.0F, -8.0F, 16, 1, 16, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 0, 116, -11.0F, 8.0F, -8.0F, 22, 1, 19, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 146, 116, -11.0F, 3.0F, -1.0F, 22, 5, 5, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 46, 136, -8.0F, 2.0F, -3.0F, 16, 6, 2, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 186, 149, -5.0F, 4.0F, -8.0F, 10, 4, 5, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 162, 0, -11.0F, 5.0F, 4.0F, 22, 3, 5, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 186, 141, -6.0F, 0.0F, -1.0F, 12, 3, 5, 0.0F, false));

		cube_r5 = new ModelRenderer(this);
		cube_r5.setRotationPoint(0.0F, 5.0F, 4.0F);
		base.addChild(cube_r5);
		setRotationAngle(cube_r5, -0.6545F, 0.0F, 0.0F);
		cube_r5.cubeList.add(new ModelBox(cube_r5, 98, 196, -3.0F, -3.0F, 4.0F, 6, 3, 3, 0.0F, false));
		cube_r5.cubeList.add(new ModelBox(cube_r5, 162, 60, -6.0F, -3.0F, -3.0F, 12, 3, 7, 0.0F, false));

		gun = new ModelRenderer(this);
		gun.setRotationPoint(0.0F, 8.0F, 0.0F);
		main.addChild(gun);
		gun.cubeList.add(new ModelBox(gun, 152, 96, -6.0F, -14.0F, 1.0F, 12, 3, 12, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 162, 46, -6.0F, -16.0F, 5.0F, 12, 4, 10, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 0, 179, -3.0F, -14.0F, -18.0F, 6, 2, 14, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 146, 126, -6.0F, -14.0F, -3.0F, 12, 2, 4, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 88, 74, -5.0F, -14.0F, -4.0F, 10, 2, 1, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 118, 181, 3.0F, -14.0F, -17.0F, 1, 2, 13, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 138, 74, -2.0F, -14.0F, -19.0F, 4, 2, 1, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 0, 158, -3.0F, -13.5F, -38.0F, 1, 1, 20, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 42, 158, 2.0F, -13.5F, -38.0F, 1, 1, 20, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 146, 181, 1.5F, -14.0F, -44.0F, 2, 2, 6, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 20, 195, -3.5F, -14.0F, -44.0F, 2, 2, 6, 0.0F, false));
		gun.cubeList.add(new ModelBox(gun, 186, 126, -4.0F, -14.0F, -17.0F, 1, 2, 13, 0.0F, false));

		cube_r6 = new ModelRenderer(this);
		cube_r6.setRotationPoint(0.0F, -11.0F, 2.0F);
		gun.addChild(cube_r6);
		setRotationAngle(cube_r6, 0.3491F, 0.0F, 0.0F);
		cube_r6.cubeList.add(new ModelBox(cube_r6, 46, 144, -6.0F, -3.5F, 0.0F, 12, 3, 5, 0.0F, false));

		// 俯仰容器：旋转轴与 gun 一致 (0,8,0)，保证左右火箭巢与中间机炮绕同一根轴俯仰
		// （原 rol 旋转点 y=10、ror 旋转点 y=0 不同轴，俯仰时左右巢轨迹不对称，左巢会前后窜动）
		pitchL = new ModelRenderer(this);
		pitchL.setRotationPoint(0.0F, 8.0F, 0.0F);
		main.addChild(pitchL);

		rol = new ModelRenderer(this);
		rol.setRotationPoint(0.0F, 2.0F, 0.0F); // 原 (0,10,0) 相对 main -> 相对 pitchL，位置不变
		pitchL.addChild(rol);
		rol.cubeList.add(new ModelBox(rol, 84, 158, 22.0F, -12.0F, -12.0F, 1, 4, 19, 0.0F, false));
		rol.cubeList.add(new ModelBox(rol, 40, 179, 13.0F, -15.0F, -12.0F, 10, 5, 10, 0.0F, false));
		rol.cubeList.add(new ModelBox(rol, 88, 0, 18.5F, -15.0F, -20.5F, 3, 3, 34, 0.0F, false));
		rol.cubeList.add(new ModelBox(rol, 88, 37, 14.5F, -17.5F, -20.5F, 3, 3, 34, 0.0F, false));

		rocketleft_r1 = new ModelRenderer(this);
		rocketleft_r1.setRotationPoint(17.799F, -14.8481F, -3.5F);
		rol.addChild(rocketleft_r1);
		setRotationAngle(rocketleft_r1, 0.0F, 0.0F, 0.5236F);
		rocketleft_r1.cubeList.add(new ModelBox(rocketleft_r1, 0, 0, -5.5F, -3.0F, -16.5F, 11, 6, 33, 0.0F, false));

		pitchR = new ModelRenderer(this);
		pitchR.setRotationPoint(0.0F, 8.0F, 0.0F);
		main.addChild(pitchR);

		ror = new ModelRenderer(this);
		ror.setRotationPoint(0.0F, -8.0F, 0.0F); // 原 (0,0,0) 相对 main -> 相对 pitchR，位置不变
		pitchR.addChild(ror);
		ror.cubeList.add(new ModelBox(ror, 76, 78, -21.5F, -5.5F, -20.5F, 3, 3, 35, 0.0F, false));
		ror.cubeList.add(new ModelBox(ror, 0, 78, -17.5F, -7.8F, -20.5F, 3, 3, 35, 0.0F, false));
		ror.cubeList.add(new ModelBox(ror, 80, 181, -23.0F, -5.0F, -12.0F, 9, 5, 10, 0.0F, false));
		ror.cubeList.add(new ModelBox(ror, 124, 158, -23.0F, -2.0F, -12.0F, 1, 4, 19, 0.0F, false));

		rocketright_r1 = new ModelRenderer(this);
		rocketright_r1.setRotationPoint(-18.201F, -4.3481F, -3.5F);
		ror.addChild(rocketright_r1);
		setRotationAngle(rocketright_r1, 0.0F, 0.0F, -0.5236F);
		rocketright_r1.cubeList.add(new ModelBox(rocketright_r1, 0, 39, -4.799F, -3.5719F, -16.5F, 11, 6, 33, 0.0F, false));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		main.render(f5);
	}

	/**
	 * 渲染炮塔上部（俯仰机制）
	 * 水平转向由渲染器整体旋转整个实体处理（含底座/侧裙），此处只处理俯仰：
	 * 中间机炮(gun)与左右火箭巢(pitchL/pitchR 内的 rol/ror)绕同一根轴单独俯仰，底座(base)与侧裙(r/l)不动
	 *
	 * @param turretPitchDeg 机炮/火箭巢俯仰角（度，正=下俯）
	 */
	public void renderTurret(Entity entity, float f, float f1, float f2, float f3, float f4, float f5,
	                         float turretPitchDeg) {
		float pitchRad = (float) Math.toRadians(turretPitchDeg);
		gun.rotateAngleX = pitchRad;
		pitchL.rotateAngleX = pitchRad;
		pitchR.rotateAngleX = pitchRad;
		main.render(f5);
		// 复原，避免影响其他渲染帧
		gun.rotateAngleX = 0.0F;
		pitchL.rotateAngleX = 0.0F;
		pitchR.rotateAngleX = 0.0F;
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
