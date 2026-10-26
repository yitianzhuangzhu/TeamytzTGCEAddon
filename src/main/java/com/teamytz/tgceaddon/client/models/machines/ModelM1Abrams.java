// Made with Blockbench (由 bbmodel2java.js 生成，规则与 Blockbench 1.12 导出一致)

package com.teamytz.tgceaddon.client.models.machines;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelM1Abrams extends ModelBase {
	private final ModelRenderer main;
	private final ModelRenderer dec;
	private final ModelRenderer system;
	private final ModelRenderer view2;
	private final ModelRenderer view;
	private final ModelRenderer aps2;
	private final ModelRenderer cube_r1;
	private final ModelRenderer cube_r2;
	private final ModelRenderer aps1;
	private final ModelRenderer cube_r3;
	private final ModelRenderer cube_r4;
	private final ModelRenderer body;
	private final ModelRenderer cube_r5;
	private final ModelRenderer cube_r6;
	private final ModelRenderer cube_r7;
	private final ModelRenderer cube_r8;
	private final ModelRenderer cube_r9;
	private final ModelRenderer cube_r10;
	private final ModelRenderer cube_r11;
	private final ModelRenderer cube_r12;
	private final ModelRenderer cube_r13;
	private final ModelRenderer cube_r14;
	private final ModelRenderer cube_r15;
	private final ModelRenderer cube_r16;
	private final ModelRenderer cube_r17;
	private final ModelRenderer cube_r18;
	private final ModelRenderer cube_r19;
	private final ModelRenderer cube_r20;
	private final ModelRenderer cube_r21;
	private final ModelRenderer cube_r22;
	private final ModelRenderer weapons;
	private final ModelRenderer cube_r23;
	private final ModelRenderer mainweapon;
	private final ModelRenderer cube_r24;
	private final ModelRenderer cannon_barrel;
	private final ModelRenderer machinegun;
	private final ModelRenderer gun_main;

	public ModelM1Abrams() {
		textureWidth = 512;
		textureHeight = 512;

		main = new ModelRenderer(this);
		main.setRotationPoint(0.0F, 22.0F, -27.0F);

		dec = new ModelRenderer(this);
		dec.setRotationPoint(0.0F, 5.0F, 27.0F);
		main.addChild(dec);
		dec.cubeList.add(new ModelBox(dec, 0, 188, -12.0F, -21.0F, -3.0F, 12, 5, 12, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 144, 150, -11.0F, -18.0F, 14.0F, 22, 2, 14, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 240, 32, -15.0F, -18.0F, 0.0F, 1, 2, 11, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 250, 59, -17.0F, -18.0F, -3.0F, 5, 2, 3, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 196, 28, 11.5F, -23.0F, 10.5F, 6, 1, 16, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 196, 45, 12.0F, -22.0F, 11.0F, 5, 6, 15, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 198, 125, -17.5F, -23.0F, 10.5F, 6, 1, 16, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 48, 200, -17.0F, -22.0F, 11.0F, 5, 6, 15, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 204, 242, -10.0F, -39.0F, 36.0F, 2, 13, 2, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 138, 250, -15.0F, -17.0F, 26.0F, 2, 6, 2, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 172, 236, -14.5F, -47.0F, 26.5F, 1, 30, 1, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 252, 103, 14.0F, -17.0F, 26.0F, 2, 6, 2, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 176, 236, 14.5F, -45.0F, 26.5F, 1, 28, 1, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 164, 249, 0.0F, -28.0F, 30.0F, 2, 12, 2, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 204, 66, -0.5F, -29.0F, 29.5F, 3, 1, 3, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 196, 108, -10.0F, -22.0F, 36.0F, 2, 1, 2, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 218, 203, -10.0F, -17.0F, 36.0F, 2, 1, 2, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 48, 188, -9.5F, -26.0F, 36.5F, 1, 9, 1, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 134, 108, -12.0F, -18.0F, 11.0F, 24, 2, 1, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 54, 168, 3.0F, -18.0F, -14.0F, 10, 2, 2, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 180, 66, 3.0F, -18.0F, -22.0F, 10, 2, 2, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 250, 150, 11.0F, -18.0F, -20.0F, 2, 2, 6, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 250, 158, 3.0F, -18.0F, -20.0F, 2, 2, 6, 0.0F, false));
		dec.cubeList.add(new ModelBox(dec, 242, 139, 7.0F, -17.0F, -18.0F, 2, 1, 2, 0.0F, false));

		system = new ModelRenderer(this);
		system.setRotationPoint(0.0F, 0.0F, 0.0F);
		main.addChild(system);

		view2 = new ModelRenderer(this);
		view2.setRotationPoint(10.5F, -15.0F, 19.4286F);
		system.addChild(view2);
		view2.cubeList.add(new ModelBox(view2, 106, 173, -4.5F, 2.0F, -3.4286F, 9, 2, 9, 0.0F, false));
		view2.cubeList.add(new ModelBox(view2, 238, 142, -3.5F, -4.0F, -3.4286F, 7, 0, 8, 0.0F, false));
		view2.cubeList.add(new ModelBox(view2, 180, 232, -4.0F, 1.0F, -2.9286F, 8, 2, 8, 0.0F, false));
		view2.cubeList.add(new ModelBox(view2, 62, 241, -3.0F, -4.0F, -1.9286F, 6, 6, 6, 0.0F, false));
		view2.cubeList.add(new ModelBox(view2, 142, 173, -2.0F, -4.0F, -2.9286F, 0, 6, 1, 0.0F, false));
		view2.cubeList.add(new ModelBox(view2, 52, 188, 2.0F, -4.0F, -2.9286F, 0, 6, 1, 0.0F, false));

		view = new ModelRenderer(this);
		view.setRotationPoint(-8.5F, -14.125F, 12.875F);
		system.addChild(view);
		view.cubeList.add(new ModelBox(view, 210, 166, -4.5F, -0.875F, 1.125F, 12, 4, 8, 0.0F, false));
		view.cubeList.add(new ModelBox(view, 124, 211, -4.5F, -0.875F, -1.875F, 9, 0, 3, 0.0F, false));
		view.cubeList.add(new ModelBox(view, 150, 194, -4.5F, -0.875F, -4.875F, 0, 4, 6, 0.0F, false));
		view.cubeList.add(new ModelBox(view, 250, 166, 4.5F, -0.875F, -4.875F, 0, 4, 6, 0.0F, false));

		aps2 = new ModelRenderer(this);
		aps2.setRotationPoint(-22.0F, -11.0F, 34.0F);
		system.addChild(aps2);
		aps2.cubeList.add(new ModelBox(aps2, 254, 72, -1.0F, 0.0F, -1.0F, 2, 2, 2, 0.0F, false));

		cube_r1 = new ModelRenderer(this);
		cube_r1.setRotationPoint(0.0F, 0.0F, -0.5F);
		aps2.addChild(cube_r1);
		setRotationAngle(cube_r1, 0.0F, 0.0F, 0.4363F);
		cube_r1.cubeList.add(new ModelBox(cube_r1, 70, 253, 0.0F, -3.0F, -1.0F, 1, 3, 3, 0.0F, false));

		cube_r2 = new ModelRenderer(this);
		cube_r2.setRotationPoint(-1.0F, 0.0F, 0.0F);
		aps2.addChild(cube_r2);
		setRotationAngle(cube_r2, 0.0F, 0.0F, 0.4363F);
		cube_r2.cubeList.add(new ModelBox(cube_r2, 190, 251, 0.0F, -4.0F, -2.0F, 1, 4, 4, 0.0F, false));

		aps1 = new ModelRenderer(this);
		aps1.setRotationPoint(0.0F, 0.0F, 0.0F);
		system.addChild(aps1);
		aps1.cubeList.add(new ModelBox(aps1, 254, 68, 22.0F, -11.0F, 33.0F, 2, 2, 2, 0.0F, false));
		aps1.cubeList.add(new ModelBox(aps1, 244, 199, 21.5F, -9.5F, 32.5F, 3, 1, 3, 0.0F, false));
		aps1.cubeList.add(new ModelBox(aps1, 252, 203, -23.5F, -9.5F, 32.5F, 3, 1, 3, 0.0F, false));

		cube_r3 = new ModelRenderer(this);
		cube_r3.setRotationPoint(22.0937F, -10.5774F, 33.5F);
		aps1.addChild(cube_r3);
		setRotationAngle(cube_r3, 0.0F, 0.0F, -0.4363F);
		cube_r3.cubeList.add(new ModelBox(cube_r3, 252, 207, 0.0F, -3.0F, -1.0F, 1, 3, 3, 0.0F, false));

		cube_r4 = new ModelRenderer(this);
		cube_r4.setRotationPoint(23.0937F, -10.5774F, 34.0F);
		aps1.addChild(cube_r4);
		setRotationAngle(cube_r4, 0.0F, 0.0F, -0.4363F);
		cube_r4.cubeList.add(new ModelBox(cube_r4, 180, 251, 0.0F, -4.0F, -2.0F, 1, 4, 4, 0.0F, false));

		body = new ModelRenderer(this);
		body.setRotationPoint(0.0F, 5.0F, 17.0F);
		main.addChild(body);
		body.cubeList.add(new ModelBox(body, 107, 184, -9.0F, -10.0F, -18.0F, 22, 5, 5, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 140, 62, -6.0F, -6.0F, -18.0F, 15, 3, 5, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 0, 111, -17.0F, -14.0F, -13.0F, 34, 11, 21, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 0, 70, -19.0F, -16.0F, 8.0F, 39, 13, 28, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 134, 70, -19.0F, -11.0F, 36.0F, 39, 8, 6, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 144, 166, -13.0F, -16.0F, 36.0F, 27, 12, 6, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 124, 216, -13.0F, -16.0F, 42.0F, 6, 12, 8, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 180, 216, -19.0F, -16.0F, 46.0F, 4, 12, 4, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 246, 82, 16.0F, -16.0F, 46.0F, 4, 12, 4, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 152, 216, 8.0F, -16.0F, 42.0F, 6, 12, 8, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 180, 242, 9.5F, -17.0F, 41.0F, 4, 1, 8, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 34, 221, -4.0F, -13.0F, 42.5F, 10, 9, 6, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 198, 206, -19.0F, -16.0F, 36.0F, 0, 12, 14, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 210, 0, 20.0F, -16.0F, 36.0F, 0, 12, 14, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 0, 143, -19.0F, -16.0F, 50.0F, 39, 12, 0, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 110, 111, -19.0F, -4.0F, 36.0F, 39, 0, 14, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 140, 0, -4.0F, -16.0F, -18.0F, 9, 2, 26, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 134, 84, -17.0F, -16.0F, -14.0F, 13, 2, 22, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 78, 150, 5.0F, -16.0F, -13.0F, 12, 2, 21, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 94, 226, -21.1F, -14.0F, 11.5F, 3, 7, 10, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 224, 66, 19.9F, -9.7534F, 27.5F, 3, 4, 12, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 146, 249, 19.9F, -11.7534F, 21.5F, 3, 6, 6, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 120, 236, 22.7F, -10.7534F, 27.1444F, 4, 5, 9, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 244, 178, 22.7F, -11.7534F, 21.5F, 4, 6, 6, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 184, 108, 22.7F, -12.7534F, 21.5F, 4, 1, 2, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 0, 241, 22.7F, -14.0F, 14.5F, 4, 7, 7, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 226, 203, 19.7F, -14.0F, 11.5F, 3, 7, 10, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 86, 243, -25.1F, -11.7534F, 21.5F, 4, 6, 6, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 206, 203, -25.1F, -12.7534F, 21.5F, 4, 1, 2, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 42, 247, -21.1F, -11.7534F, 21.5F, 3, 6, 6, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 236, 45, -25.1F, -10.7534F, 27.1444F, 4, 5, 9, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 54, 155, -21.1F, -9.7534F, 27.5F, 3, 4, 9, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 234, 178, -21.1F, -10.0F, 36.5F, 3, 4, 2, 0.0F, false));
		body.cubeList.add(new ModelBox(body, 238, 240, -25.1F, -14.0F, 14.5F, 4, 7, 7, 0.0F, false));

		cube_r5 = new ModelRenderer(this);
		cube_r5.setRotationPoint(-25.1F, -11.831F, 22.0434F);
		body.addChild(cube_r5);
		setRotationAngle(cube_r5, -0.2182F, 0.0F, 0.0F);
		cube_r5.cubeList.add(new ModelBox(cube_r5, 204, 84, 0.0F, -2.0F, -1.0F, 6, 2, 15, 0.0F, false));

		cube_r6 = new ModelRenderer(this);
		cube_r6.setRotationPoint(-25.1F, -6.7836F, 15.4763F);
		body.addChild(cube_r6);
		setRotationAngle(cube_r6, -0.1309F, 0.0F, 0.0F);
		cube_r6.cubeList.add(new ModelBox(cube_r6, 238, 12, 0.0F, -2.0F, -1.0F, 6, 2, 8, 0.0F, false));

		cube_r7 = new ModelRenderer(this);
		cube_r7.setRotationPoint(20.7F, -6.8695F, 15.4914F);
		body.addChild(cube_r7);
		setRotationAngle(cube_r7, -0.1309F, 0.0F, 0.0F);
		cube_r7.cubeList.add(new ModelBox(cube_r7, 240, 22, 0.0F, -2.0F, -1.0F, 6, 2, 8, 0.0F, false));

		cube_r8 = new ModelRenderer(this);
		cube_r8.setRotationPoint(20.7F, -11.831F, 22.0434F);
		body.addChild(cube_r8);
		setRotationAngle(cube_r8, -0.2182F, 0.0F, 0.0F);
		cube_r8.cubeList.add(new ModelBox(cube_r8, 106, 194, -1.0F, -2.0F, -1.0F, 7, 2, 15, 0.0F, false));

		cube_r9 = new ModelRenderer(this);
		cube_r9.setRotationPoint(-19.4431F, -8.2534F, 39.68F);
		body.addChild(cube_r9);
		setRotationAngle(cube_r9, 0.0F, 2.3562F, 0.0F);
		cube_r9.cubeList.add(new ModelBox(cube_r9, 242, 119, -1.5F, -2.5F, -1.5F, 8, 5, 5, 0.0F, false));

		cube_r10 = new ModelRenderer(this);
		cube_r10.setRotationPoint(-19.3F, -11.5F, 10.8571F);
		body.addChild(cube_r10);
		setRotationAngle(cube_r10, 0.0F, 0.7854F, 0.0F);
		cube_r10.cubeList.add(new ModelBox(cube_r10, 242, 129, -6.5F, -2.5F, -1.5F, 9, 7, 3, 0.0F, false));

		cube_r11 = new ModelRenderer(this);
		cube_r11.setRotationPoint(21.4142F, -8.5F, 39.8571F);
		body.addChild(cube_r11);
		setRotationAngle(cube_r11, 0.0F, -2.3562F, 0.0F);
		cube_r11.cubeList.add(new ModelBox(cube_r11, 32, 236, -6.5F, -2.5F, -1.5F, 9, 5, 6, 0.0F, false));

		cube_r12 = new ModelRenderer(this);
		cube_r12.setRotationPoint(20.4142F, -11.5F, 10.8571F);
		body.addChild(cube_r12);
		setRotationAngle(cube_r12, 0.0F, -0.7854F, 0.0F);
		cube_r12.cubeList.add(new ModelBox(cube_r12, 212, 240, -2.5F, -2.5F, -1.5F, 10, 7, 3, 0.0F, false));

		cube_r13 = new ModelRenderer(this);
		cube_r13.setRotationPoint(-16.5F, -9.0F, -2.5F);
		body.addChild(cube_r13);
		setRotationAngle(cube_r13, 0.0F, 0.0F, 0.2618F);
		cube_r13.cubeList.add(new ModelBox(cube_r13, 106, 250, -3.5F, -3.0F, -7.5F, 1, 4, 7, 0.0F, false));
		cube_r13.cubeList.add(new ModelBox(cube_r13, 0, 155, -2.5F, -6.0F, -11.5F, 5, 11, 22, 0.0F, false));

		cube_r14 = new ModelRenderer(this);
		cube_r14.setRotationPoint(17.5F, -9.0F, -2.5F);
		body.addChild(cube_r14);
		setRotationAngle(cube_r14, 0.0F, 0.0F, -0.2618F);
		cube_r14.cubeList.add(new ModelBox(cube_r14, 88, 200, 2.5F, -3.0F, -7.5F, 1, 4, 7, 0.0F, false));
		cube_r14.cubeList.add(new ModelBox(cube_r14, 140, 28, -2.5F, -6.0F, -12.5F, 5, 11, 23, 0.0F, false));

		cube_r15 = new ModelRenderer(this);
		cube_r15.setRotationPoint(7.0F, -14.0F, -20.0F);
		body.addChild(cube_r15);
		setRotationAngle(cube_r15, 0.0F, -0.5672F, 0.0F);
		cube_r15.cubeList.add(new ModelBox(cube_r15, 150, 206, -4.0F, -2.0F, -1.0F, 16, 2, 8, 0.0F, false));

		cube_r16 = new ModelRenderer(this);
		cube_r16.setRotationPoint(-7.0F, -14.0F, -20.0F);
		body.addChild(cube_r16);
		setRotationAngle(cube_r16, 0.0F, 0.5672F, 0.0F);
		cube_r16.cubeList.add(new ModelBox(cube_r16, 226, 230, -4.0F, -2.0F, -1.0F, 8, 2, 8, 0.0F, false));
		cube_r16.cubeList.add(new ModelBox(cube_r16, 204, 101, -12.0F, -2.0F, -1.0F, 16, 2, 8, 0.0F, false));

		cube_r17 = new ModelRenderer(this);
		cube_r17.setRotationPoint(18.0F, -9.3F, -10.3F);
		body.addChild(cube_r17);
		setRotationAngle(cube_r17, 1.5519F, -0.0428F, -0.236F);
		cube_r17.cubeList.add(new ModelBox(cube_r17, -12, -9, -4.0F, -4.0F, -5.0F, 5, 15, 11, 0.0F, false));

		cube_r18 = new ModelRenderer(this);
		cube_r18.setRotationPoint(9.0F, -10.0F, -22.3F);
		body.addChild(cube_r18);
		setRotationAngle(cube_r18, 0.9634F, 0.8468F, -0.7294F);
		cube_r18.cubeList.add(new ModelBox(cube_r18, 0, 204, -6.0F, -5.0F, -4.0F, 7, 17, 10, 0.0F, false));

		cube_r19 = new ModelRenderer(this);
		cube_r19.setRotationPoint(-8.0F, -11.0F, -22.0F);
		body.addChild(cube_r19);
		setRotationAngle(cube_r19, -0.6708F, -0.8452F, 0.7696F);
		cube_r19.cubeList.add(new ModelBox(cube_r19, 54, 173, -1.0F, -5.0F, -5.0F, 8, 9, 18, 0.0F, false));

		cube_r20 = new ModelRenderer(this);
		cube_r20.setRotationPoint(9.0F, -6.0F, -17.0F);
		body.addChild(cube_r20);
		setRotationAngle(cube_r20, 0.0F, -0.6109F, 0.0F);
		cube_r20.cubeList.add(new ModelBox(cube_r20, 198, 142, -6.0F, 0.0F, -1.0F, 15, 3, 5, 0.0F, false));

		cube_r21 = new ModelRenderer(this);
		cube_r21.setRotationPoint(-11.0F, -6.0F, -14.0F);
		body.addChild(cube_r21);
		setRotationAngle(cube_r21, 0.0F, 0.5672F, 0.0F);
		cube_r21.cubeList.add(new ModelBox(cube_r21, 216, 111, -6.0F, 0.0F, -1.0F, 15, 3, 5, 0.0F, false));

		cube_r22 = new ModelRenderer(this);
		cube_r22.setRotationPoint(0.0F, -13.0F, -23.6F);
		body.addChild(cube_r22);
		setRotationAngle(cube_r22, -0.4363F, 0.0F, 0.0F);
		cube_r22.cubeList.add(new ModelBox(cube_r22, 226, 220, -4.0F, 3.0F, 1.0F, 9, 3, 7, 0.0F, false));

		weapons = new ModelRenderer(this);
		weapons.setRotationPoint(0.0F, 0.0F, 0.0F);
		main.addChild(weapons);

		cube_r23 = new ModelRenderer(this);
		cube_r23.setRotationPoint(0.0F, -7.85F, -4.6F);
		weapons.addChild(cube_r23);
		setRotationAngle(cube_r23, -0.4363F, 0.0F, 0.0F);
		cube_r23.cubeList.add(new ModelBox(cube_r23, 88, 211, -4.0F, -3.0F, -1.0F, 9, 6, 9, 0.0F, false));

		mainweapon = new ModelRenderer(this);
		mainweapon.setRotationPoint(0.0F, -5.85F, -5.0F);
		weapons.addChild(mainweapon);
		mainweapon.cubeList.add(new ModelBox(mainweapon, 216, 150, -4.0F, -5.15F, -3.3F, 9, 6, 8, 0.0F, false));

		cube_r24 = new ModelRenderer(this);
		cube_r24.setRotationPoint(0.0F, -2.0F, -3.6F);
		mainweapon.addChild(cube_r24);
		setRotationAngle(cube_r24, -0.4363F, 0.0F, 0.0F);
		cube_r24.cubeList.add(new ModelBox(cube_r24, 238, 0, -4.0F, -3.0F, -1.0F, 9, 8, 4, 0.0F, false));

		cannon_barrel = new ModelRenderer(this);
		cannon_barrel.setRotationPoint(0.0F, 0.0F, 0.0F);
		mainweapon.addChild(cannon_barrel);
		cannon_barrel.cubeList.add(new ModelBox(cannon_barrel, 212, 250, -2.0F, -3.0F, -14.0F, 4, 4, 2, 0.0F, false));
		cannon_barrel.cubeList.add(new ModelBox(cannon_barrel, 15, 15, -1.5F, -2.5F, -59.0F, 3, 3, 52, 0.0F, false));
		cannon_barrel.cubeList.add(new ModelBox(cannon_barrel, 146, 236, -2.0F, -3.0F, -8.0F, 4, 4, 9, 0.0F, false));
		cannon_barrel.cubeList.add(new ModelBox(cannon_barrel, 66, 226, -2.0F, -4.0F, -32.0F, 4, 5, 10, 0.0F, false));

		machinegun = new ModelRenderer(this);
		machinegun.setRotationPoint(-7.6667F, -20.3F, 17.4F);
		weapons.addChild(machinegun);
		machinegun.cubeList.add(new ModelBox(machinegun, 216, 119, -2.3333F, 4.3F, -1.4F, 5, 1, 5, 0.0F, false));
		machinegun.cubeList.add(new ModelBox(machinegun, 78, 143, -5.3333F, 2.3F, -1.4F, 11, 2, 5, 0.0F, false));
		machinegun.cubeList.add(new ModelBox(machinegun, 78, 253, -5.3333F, 2.3F, -3.4F, 2, 2, 2, 0.0F, false));
		machinegun.cubeList.add(new ModelBox(machinegun, 60, 253, -5.3333F, 3.3F, -6.4F, 2, 1, 3, 0.0F, false));
		machinegun.cubeList.add(new ModelBox(machinegun, 180, 62, -5.8333F, 2.3F, -6.9F, 3, 1, 3, 0.0F, false));
		machinegun.cubeList.add(new ModelBox(machinegun, 34, 204, -5.3333F, -2.7F, -8.4F, 2, 5, 5, 0.0F, false));
		machinegun.cubeList.add(new ModelBox(machinegun, 254, 64, -6.3333F, -2.7F, -6.4F, 1, 1, 3, 0.0F, false));
		machinegun.cubeList.add(new ModelBox(machinegun, 236, 119, -5.8333F, -3.2F, -7.4F, 3, 6, 0, 0.0F, false));
		machinegun.cubeList.add(new ModelBox(machinegun, 34, 214, 1.6667F, 0.3F, -1.4F, 2, 2, 5, 0.0F, false));
		machinegun.cubeList.add(new ModelBox(machinegun, 236, 59, -3.3333F, -0.7F, -9.4F, 2, 2, 5, 0.0F, false));
		machinegun.cubeList.add(new ModelBox(machinegun, 106, 243, -3.3333F, -3.2F, -9.4F, 2, 2, 5, 0.0F, false));
		machinegun.cubeList.add(new ModelBox(machinegun, 122, 250, -9.3333F, -3.2F, -7.4F, 3, 3, 5, 0.0F, false));
		machinegun.cubeList.add(new ModelBox(machinegun, 212, 232, -3.8333F, 0.3F, -1.4F, 2, 2, 5, 0.0F, false));
		machinegun.cubeList.add(new ModelBox(machinegun, 22, 247, -3.8333F, -2.7F, -1.4F, 2, 3, 8, 0.0F, false));
		machinegun.cubeList.add(new ModelBox(machinegun, 22, 241, -5.8333F, -2.7F, 3.6F, 2, 3, 3, 0.0F, false));
		machinegun.cubeList.add(new ModelBox(machinegun, 254, 76, -6.8333F, -2.2F, 4.1F, 1, 2, 2, 0.0F, false));
		machinegun.cubeList.add(new ModelBox(machinegun, 252, 213, -0.8333F, -0.2F, 3.6F, 2, 4, 2, 0.0F, false));

		gun_main = new ModelRenderer(this);
		gun_main.setRotationPoint(2.6667F, -0.5571F, 0.8143F);
		machinegun.addChild(gun_main);
		gun_main.cubeList.add(new ModelBox(gun_main, 206, 184, -3.5F, -1.6429F, -8.2143F, 2, 2, 17, 0.0F, false));
		gun_main.cubeList.add(new ModelBox(gun_main, 66, 221, -2.0F, -1.1429F, -2.2143F, 4, 0, 5, 0.0F, false));
		gun_main.cubeList.add(new ModelBox(gun_main, 244, 190, 2.0F, -1.6429F, -2.2143F, 6, 4, 5, 0.0F, false));
		gun_main.cubeList.add(new ModelBox(gun_main, 162, 184, -3.0F, -1.1429F, -29.2143F, 1, 1, 21, 0.0F, false));
		gun_main.cubeList.add(new ModelBox(gun_main, 210, 178, -5.0F, -0.1429F, -2.2143F, 7, 1, 5, 0.0F, false));
		gun_main.cubeList.add(new ModelBox(gun_main, 224, 250, 1.5F, 0.8571F, -2.2143F, 1, 1, 5, 0.0F, false));
		gun_main.cubeList.add(new ModelBox(gun_main, 216, 66, -1.0F, -0.1429F, -5.2143F, 1, 1, 3, 0.0F, false));
		gun_main.cubeList.add(new ModelBox(gun_main, 252, 98, -3.5F, 0.3571F, 4.7857F, 2, 1, 4, 0.0F, false));
	}

	/** 主炮后坐力行程（模型像素）：16 像素 = 1 格，8 像素 = 0.5 格 */
	public static final float RECOIL_TRAVEL_PX = 8.0F;

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		renderM1(f5, 0.0F, 0.0F, 0.0F, 0.0F);
	}

	/**
	 * M1 炮塔专用渲染入口。
	 *
	 * <p>水平转向由渲染器整体旋转实体完成（{@code rotate(yawHead - 180)}），这里只处理 4 个通道：
	 * 主炮俯仰（mainweapon，±15°）、机枪方向机（machinegun，相对炮塔）、
	 * 机枪高低（gun_main，仰 50°/俯 15°）、主炮后坐力（cannon_barrel 后退）。</p>
	 *
	 * <p>角度语义与 BMPT 一致（MC 语义：正 = 低头）。渲完立刻复位，避免污染下一帧。</p>
	 */
	public void renderM1(float scale, float mainPitchDeg, float mgYawDeg, float mgPitchDeg, float recoil) {
		mainweapon.rotateAngleX = (float) Math.toRadians(mainPitchDeg);
		machinegun.rotateAngleY = (float) Math.toRadians(mgYawDeg);
		gun_main.rotateAngleX = (float) Math.toRadians(mgPitchDeg);
		// 炮管朝 -Z，后退即 +Z 方向位移
		cannon_barrel.offsetZ = recoil * RECOIL_TRAVEL_PX;

		main.render(scale);

		mainweapon.rotateAngleX = 0.0F;
		machinegun.rotateAngleY = 0.0F;
		gun_main.rotateAngleX = 0.0F;
		cannon_barrel.offsetZ = 0.0F;
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
