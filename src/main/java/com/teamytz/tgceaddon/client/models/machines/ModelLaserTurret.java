// Made with Blockbench (由 bbmodel2java.js 生成，规则与 Blockbench 1.12 导出一致)
// 组 base 带组旋转 [0,-180,0]（已照抄为 setRotationAngle）
// 组 connection 带组旋转 [0,-180,0]（已照抄为 setRotationAngle）
// 注意: 俯仰分组 connection 带组旋转 [0,-180,0]（局部 X 轴被翻向 -X）
//       => 该炮塔必须在实体里覆写 EntityBaseTurret.getModelPitchSign() 返回 -1.0F

package com.teamytz.tgceaddon.client.models.machines;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import com.teamytz.tgceaddon.client.models.ITurretModel;

public class ModelLaserTurret extends ModelBase implements ITurretModel {
	private final ModelRenderer base;
	private final ModelRenderer connection;

	public ModelLaserTurret() {
		textureWidth = 128;
		textureHeight = 128;

		base = new ModelRenderer(this);
		base.setRotationPoint(0.0F, 24.0F, 0.0F);
		setRotationAngle(base, 0.0F, 3.1416F, 0.0F);
		base.cubeList.add(new ModelBox(base, 72, 85, -5.0F, -4.0F, -3.0F, 1, 2, 7, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 58, 0, -4.0F, -4.0F, -4.0F, 8, 2, 9, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 86, 23, 4.0F, -4.0F, -3.0F, 1, 2, 7, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 64, 34, -2.0F, -6.0F, -5.0F, 4, 2, 6, 0.0F, false));
		base.cubeList.add(new ModelBox(base, 0, 24, -8.0F, -2.0F, -8.0F, 16, 2, 16, 0.0F, false));
		connection = new ModelRenderer(this);
		connection.setRotationPoint(0.0F, 17.0F, -1.0F);
		setRotationAngle(connection, 0.0F, 3.1416F, 0.0F);
		connection.cubeList.add(new ModelBox(connection, 86, 42, -1.0F, -1.0F, -4.0F, 2, 2, 5, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 48, 42, -1.0F, 0.0F, -9.0F, 2, 1, 17, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 0, 42, -3.0F, -1.0F, -9.0F, 6, 1, 18, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 86, 49, -2.0F, -1.0F, 9.0F, 4, 1, 3, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 54, 92, -1.0F, -1.0F, 12.0F, 2, 1, 2, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 0, 0, -3.0F, -2.0F, -10.0F, 6, 1, 23, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 0, 61, -3.0F, -6.0F, -10.0F, 6, 1, 9, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 86, 32, -2.0F, -7.0F, -9.0F, 4, 1, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 88, 89, 2.0F, -7.0F, -8.0F, 1, 1, 4, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 54, 85, -2.0F, -8.0F, -8.0F, 4, 2, 5, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 48, 60, -1.0F, -7.0F, -3.0F, 2, 1, 13, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 58, 11, -2.0F, -6.0F, -1.0F, 4, 1, 11, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 84, 34, -1.0F, -6.0F, 10.0F, 2, 1, 7, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 30, 61, -4.0F, -2.0F, -9.0F, 1, 1, 8, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 0, 71, -3.0F, -3.0F, -1.0F, 1, 1, 10, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 66, 74, -2.0F, -4.0F, -1.0F, 1, 1, 10, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 22, 71, -3.0F, -5.0F, -1.0F, 1, 1, 10, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 32, 91, -4.0F, -4.0F, -9.0F, 1, 2, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 88, 94, -3.0F, -5.0F, -10.0F, 1, 3, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 4, 96, -3.0F, -4.0F, -8.0F, 1, 2, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 16, 94, -4.0F, -4.0F, -7.0F, 1, 2, 2, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 56, 95, -3.0F, -4.0F, -5.0F, 1, 2, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 62, 92, -4.0F, -4.0F, -4.0F, 1, 2, 2, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 26, 95, -3.0F, -4.0F, -2.0F, 1, 2, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 0, 82, -4.0F, -5.0F, -9.0F, 1, 1, 8, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 68, 92, -4.0F, -5.0F, -1.0F, 1, 4, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 44, 70, -4.0F, -4.0F, 0.0F, 1, 3, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 48, 94, -3.0F, -5.0F, 9.0F, 1, 3, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 88, 11, -2.0F, -3.0F, 10.0F, 1, 1, 4, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 88, 74, -2.0F, -4.0F, 10.0F, 1, 1, 4, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 88, 16, -2.0F, -5.0F, 10.0F, 1, 1, 4, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 92, 0, -2.0F, -5.0F, 14.0F, 1, 3, 2, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 60, 96, -2.0F, -5.0F, 16.0F, 1, 2, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 42, 94, -1.0F, -5.0F, 15.0F, 2, 2, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 78, 71, -2.0F, -2.0F, 13.0F, 4, 1, 2, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 86, 58, -1.0F, -2.0F, 15.0F, 2, 1, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 36, 82, -1.0F, -3.0F, 15.0F, 2, 1, 2, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 64, 23, 2.0F, -3.0F, -1.0F, 1, 1, 10, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 86, 53, 1.0F, -3.0F, 10.0F, 1, 1, 4, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 88, 84, 1.0F, -4.0F, 10.0F, 1, 1, 4, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 88, 79, 1.0F, -5.0F, 10.0F, 1, 1, 4, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 80, 94, 2.0F, -5.0F, 9.0F, 1, 3, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 38, 94, 3.0F, -5.0F, -1.0F, 1, 4, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 18, 82, 3.0F, -5.0F, -9.0F, 1, 1, 8, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 22, 95, 3.0F, -4.0F, -9.0F, 1, 2, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 76, 94, 2.0F, -4.0F, -11.0F, 1, 3, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 22, 91, -2.0F, -4.0F, -11.0F, 4, 3, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 90, 71, -2.0F, -1.0F, -11.0F, 4, 1, 2, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 10, 91, -2.0F, -5.0F, -11.0F, 4, 1, 2, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 84, 94, -3.0F, -4.0F, -11.0F, 1, 3, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 52, 95, 2.0F, -4.0F, -2.0F, 1, 2, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 10, 94, 3.0F, -4.0F, -4.0F, 1, 2, 2, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 0, 96, 2.0F, -4.0F, -5.0F, 1, 2, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 32, 94, 3.0F, -4.0F, -7.0F, 1, 2, 2, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 96, 53, 2.0F, -4.0F, -8.0F, 1, 2, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 36, 85, 3.0F, -2.0F, -9.0F, 1, 1, 8, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 44, 74, 2.0F, -5.0F, -1.0F, 1, 1, 10, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 78, 60, 1.0F, -4.0F, -1.0F, 1, 1, 10, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 72, 94, 3.0F, -4.0F, 0.0F, 1, 3, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 96, 56, 1.0F, -5.0F, 16.0F, 1, 2, 1, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 92, 5, 1.0F, -5.0F, 14.0F, 1, 3, 2, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 0, 91, -3.0F, -7.0F, -8.0F, 1, 1, 4, 0.0F, false));
		connection.cubeList.add(new ModelBox(connection, 92, 94, 2.0F, -5.0F, -10.0F, 1, 3, 1, 0.0F, false));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		base.render(f5);
		connection.render(f5);
	}

	/**
	 * 渲染炮塔(带动画):俯仰组 connection 绕自身旋转点绕 X 轴旋转,其余分组不动。
	 * 俯仰角语义与 BMPT 一致(MC 语义:正=低头、负=抬头);渲完必须复位,否则污染下一帧。
	 */
	@Override
	public void renderTurret(Entity entity, float f, float f1, float f2, float f3, float f4, float f5,
	                         float turretPitchDeg) {
		float pitchRad = (float) Math.toRadians(turretPitchDeg);
		connection.rotateAngleX = pitchRad;
		base.render(f5);
		connection.render(f5);
		connection.rotateAngleX = 0.0F;
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
