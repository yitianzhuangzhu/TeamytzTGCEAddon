package com.teamytz.tgceaddon.client.models.machines;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.7 - 1.12
// Paste this class into your mod and generate all required imports


public class ModelRoland extends ModelBase {
	private final ModelRenderer base;
	private final ModelRenderer main;
	private final ModelRenderer cube_r1;
	private final ModelRenderer cube_r2;
	private final ModelRenderer radar;
	private final ModelRenderer cube_r3;
	private final ModelRenderer cube_r4;
	private final ModelRenderer cube_r5;
	private final ModelRenderer cube_r6;
	private final ModelRenderer cube_r7;
	private final ModelRenderer antenna;
	private final ModelRenderer tr;
	private final ModelRenderer l;
	private final ModelRenderer r;

	public ModelRoland() {
		textureWidth = 256;
		textureHeight = 256;

		base = new ModelRenderer(this);
		base.setRotationPoint(0.2632F, 18.5789F, 5.0263F);
		

		main = new ModelRenderer(this);
		main.setRotationPoint(0.0F, 0.0F, 0.0F);
		base.addChild(main);
		main.cubeList.add(new ModelBox(main, 12, 126, -6.2632F, 0.9211F, -12.0263F, 2, 3, 2, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 130, 72, -6.2632F, 0.9211F, -14.0263F, 2, 1, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 68, 28, -8.2632F, -4.5789F, -10.0263F, 16, 10, 10, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 88, 88, -3.2632F, 2.4211F, -13.0263F, 11, 3, 3, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 0, -2, -7.2632F, -3.5789F, -12.0263F, 0, 8, 2, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 68, 59, -8.2632F, -4.5789F, 1.9737F, 16, 7, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 106, 94, -5.2632F, 2.4211F, 1.9737F, 10, 1, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 24, 96, -4.2632F, -4.5789F, 2.9737F, 8, 6, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 106, 96, -4.2632F, -4.5789F, 3.9737F, 8, 5, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 130, 45, 4.2368F, -2.5789F, 3.9737F, 2, 2, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 68, 48, -3.2632F, -5.5789F, -5.0263F, 6, 1, 10, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 64, 114, -2.2632F, -7.5789F, 0.4737F, 4, 1, 4, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 62, 78, -2.7632F, -6.5789F, -4.0263F, 5, 1, 9, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 118, 56, 3.2368F, -7.0789F, 0.9737F, 4, 2, 2, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 116, 91, 1.7368F, -6.5789F, 1.4737F, 6, 1, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 116, 72, -3.2632F, 3.4211F, 1.9737F, 6, 1, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 30, 112, -2.2632F, 4.4211F, 1.9737F, 4, 1, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 62, 68, -7.2632F, -4.5789F, -0.0263F, 14, 8, 2, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 120, 36, -8.2632F, -4.5789F, -0.0263F, 1, 7, 2, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 38, 120, 6.7368F, -4.5789F, -0.0263F, 1, 7, 2, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 106, 102, -5.2632F, 4.4211F, -0.0263F, 10, 1, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 106, 104, -5.2632F, 3.4211F, 0.9737F, 10, 1, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 90, 78, -7.2632F, 3.4211F, -0.0263F, 14, 1, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 80, 114, -4.2632F, 4.4211F, 0.9737F, 8, 1, 1, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 0, 96, 7.7368F, -8.5789F, -9.0263F, 2, 11, 4, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 12, 117, 7.7368F, -4.5789F, -11.0263F, 2, 7, 2, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 20, 117, -10.2632F, -4.5789F, -11.0263F, 2, 7, 2, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 130, 31, -9.2632F, 3.4211F, -8.0263F, 1, 1, 2, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 12, 96, -10.2632F, -8.5789F, -9.0263F, 2, 11, 4, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 130, 25, 7.7368F, 3.4211F, -8.0263F, 1, 1, 2, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 124, 96, 7.7368F, 2.4211F, -8.5263F, 2, 1, 3, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 124, 106, -10.2632F, 2.4211F, -8.5263F, 2, 1, 3, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 12, 112, -7.7632F, 0.0F, -14.0F, 5, 1, 4, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 116, 84, -7.7632F, -3.0F, -14.0F, 1, 3, 4, 0.0F, false));
		main.cubeList.add(new ModelBox(main, 116, 116, -3.7632F, -3.0F, -14.0F, 1, 3, 4, 0.0F, false));

		cube_r1 = new ModelRenderer(this);
		cube_r1.setRotationPoint(0.7368F, -1.0789F, -10.5263F);
		main.addChild(cube_r1);
		setRotationAngle(cube_r1, -0.3054F, 0.0F, 0.0F);
		cube_r1.cubeList.add(new ModelBox(cube_r1, 52, 124, -1.0F, -3.5F, -1.5F, 2, 1, 3, 0.0F, false));
		cube_r1.cubeList.add(new ModelBox(cube_r1, 120, 31, -1.0F, 2.5F, -1.5F, 2, 2, 3, 0.0F, false));
		cube_r1.cubeList.add(new ModelBox(cube_r1, 0, 111, -4.0F, -3.5F, -1.5F, 3, 8, 3, 0.0F, false));
		cube_r1.cubeList.add(new ModelBox(cube_r1, 8, 122, 1.0F, -3.5F, -3.5F, 0, 8, 2, 0.0F, false));
		cube_r1.cubeList.add(new ModelBox(cube_r1, 88, 94, 1.0F, -3.5F, -1.5F, 6, 8, 3, 0.0F, false));

		cube_r2 = new ModelRenderer(this);
		cube_r2.setRotationPoint(5.2368F, -0.0789F, -12.5263F);
		main.addChild(cube_r2);
		setRotationAngle(cube_r2, -0.3491F, 0.0F, 0.0F);
		cube_r2.cubeList.add(new ModelBox(cube_r2, 30, 114, -2.5F, -1.5F, -2.5F, 5, 3, 3, 0.0F, false));

		radar = new ModelRenderer(this);
		radar.setRotationPoint(0.0F, 10.0F, 7.5F);
		radar.cubeList.add(new ModelBox(radar, 64, 108, 1.0F, -0.5F, -1.0F, 1, 1, 4, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 28, 120, -2.0F, -0.5F, -1.0F, 1, 1, 4, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 106, 106, -2.0F, -0.5F, -6.0F, 4, 1, 5, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 116, 80, -2.0F, -1.5F, -6.0F, 4, 1, 3, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 128, 74, -2.0F, -13.5F, -6.0F, 4, 1, 1, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 112, 129, -4.0F, -8.0F, -7.0F, 3, 2, 1, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 128, 93, -6.0F, -6.0F, -7.0F, 4, 1, 1, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 128, 102, 2.0F, -6.0F, -7.0F, 4, 1, 1, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 130, 15, 2.0F, -9.0F, -7.0F, 3, 1, 1, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 130, 17, -5.0F, -9.0F, -7.0F, 3, 1, 1, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 130, 0, -4.0F, -10.0F, -7.0F, 2, 1, 2, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 130, 3, 2.0F, -10.0F, -7.0F, 2, 1, 2, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 24, 131, -3.0F, -9.0F, -7.0F, 1, 1, 1, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 124, 110, -2.0F, -6.0F, -7.0F, 4, 2, 1, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 104, 129, 1.0F, -8.0F, -7.0F, 3, 2, 1, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 120, 45, -2.0F, -10.0F, -7.0F, 4, 2, 1, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 128, 78, -2.0F, -15.5F, -5.0F, 4, 1, 1, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 70, 129, -1.0F, -16.5F, -5.0F, 2, 1, 2, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 120, 129, -0.5F, -18.0F, -4.5F, 1, 5, 1, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 130, 62, -1.0F, -15.5F, -6.0F, 2, 1, 1, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 106, 112, -3.0F, -15.5F, -4.0F, 6, 1, 3, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 52, 128, -2.0F, -2.5F, -5.0F, 4, 1, 1, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 128, 76, -2.0F, -12.5F, -5.0F, 4, 1, 1, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 120, 28, -2.0F, -13.5F, -5.0F, 4, 1, 2, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 118, 64, -2.0F, -4.5F, -6.0F, 4, 1, 1, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 58, 96, 1.0F, -3.5F, -6.0F, 1, 2, 1, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 130, 80, -2.0F, -3.5F, -6.0F, 1, 2, 1, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 126, 41, -1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 36, 129, 2.0F, -8.5F, 6.5F, 0, 5, 2, 0.0F, false));
		radar.cubeList.add(new ModelBox(radar, 40, 129, -2.0F, -8.5F, 6.5F, 0, 5, 2, 0.0F, false));

		cube_r3 = new ModelRenderer(this);
		cube_r3.setRotationPoint(0.0F, -5.0F, 7.5F);
		radar.addChild(cube_r3);
		setRotationAngle(cube_r3, 0.48F, 0.0F, 0.0F);
		cube_r3.cubeList.add(new ModelBox(cube_r3, 0, 128, -0.5F, 0.0F, -2.0F, 1, 1, 3, 0.0F, false));

		cube_r4 = new ModelRenderer(this);
		cube_r4.setRotationPoint(0.0F, -3.0F, 5.5F);
		radar.addChild(cube_r4);
		setRotationAngle(cube_r4, 1.2217F, 0.0F, 0.0F);
		cube_r4.cubeList.add(new ModelBox(cube_r4, 62, 88, -2.0F, -1.0F, -2.5F, 4, 2, 9, 0.0F, false));

		cube_r5 = new ModelRenderer(this);
		cube_r5.setRotationPoint(0.0F, -1.0F, 4.5F);
		radar.addChild(cube_r5);
		setRotationAngle(cube_r5, 0.3054F, 0.0F, 0.0F);
		cube_r5.cubeList.add(new ModelBox(cube_r5, 62, 129, -1.0F, 0.0F, 4.5F, 2, 1, 2, 0.0F, false));
		cube_r5.cubeList.add(new ModelBox(cube_r5, 94, 67, -2.0F, 0.0F, -2.5F, 4, 1, 7, 0.0F, false));

		cube_r6 = new ModelRenderer(this);
		cube_r6.setRotationPoint(0.0F, -1.0F, 6.5F);
		radar.addChild(cube_r6);
		setRotationAngle(cube_r6, 0.3054F, 0.0F, 0.0F);
		cube_r6.cubeList.add(new ModelBox(cube_r6, 118, 60, -2.0F, -1.65F, -0.69F, 4, 2, 2, 0.0F, false));

		cube_r7 = new ModelRenderer(this);
		cube_r7.setRotationPoint(0.0F, -4.0F, 7.5F);
		radar.addChild(cube_r7);
		setRotationAngle(cube_r7, 0.3054F, 0.0F, 0.0F);
		cube_r7.cubeList.add(new ModelBox(cube_r7, 20, 131, 1.0F, 0.35F, 1.31F, 1, 1, 1, 0.0F, false));
		cube_r7.cubeList.add(new ModelBox(cube_r7, 80, 123, -2.0F, -2.65F, 1.31F, 4, 3, 1, 0.0F, false));
		cube_r7.cubeList.add(new ModelBox(cube_r7, 130, 91, -2.0F, 0.35F, 1.31F, 1, 1, 1, 0.0F, false));

		antenna = new ModelRenderer(this);
		antenna.setRotationPoint(0.0F, 15.0F, -8.5F);
		radar.addChild(antenna);
		antenna.cubeList.add(new ModelBox(antenna, 116, 48, -4.0F, -18.5F, 3.5F, 8, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 128, 64, -2.0F, -25.5F, 2.5F, 4, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 16, 131, -2.0F, -27.5F, 2.5F, 1, 2, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 12, 131, 1.0F, -27.5F, 2.5F, 1, 2, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 128, 104, 6.0F, -19.5F, 4.5F, 3, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 130, 9, -9.0F, -19.5F, 4.5F, 3, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 130, 13, 6.0F, -25.5F, 4.5F, 3, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 114, 127, 4.0F, -26.5F, 4.5F, 4, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 90, 86, -6.0F, -27.5F, 4.5F, 12, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 116, 52, -4.0F, -16.5F, 5.5F, 8, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 94, 75, -6.0F, -17.5F, 4.5F, 12, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 116, 50, -4.0F, -28.5F, 5.5F, 8, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 130, 60, 4.0F, -25.5F, 3.5F, 2, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 130, 58, -4.0F, -25.5F, 3.5F, 2, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 130, 66, 2.0F, -25.5F, 3.5F, 2, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 130, 70, -4.0F, -19.5F, 3.5F, 2, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 130, 68, 2.0F, -19.5F, 3.5F, 2, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 116, 54, -4.0F, -26.5F, 3.5F, 8, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 130, 56, -6.0F, -25.5F, 3.5F, 2, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 104, 127, -8.0F, -26.5F, 4.5F, 4, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 130, 11, -9.0F, -25.5F, 4.5F, 3, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 124, 100, 4.0F, -18.5F, 4.5F, 4, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 126, 89, -8.0F, -18.5F, 4.5F, 4, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 130, 34, 4.0F, -19.5F, 3.5F, 2, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 56, 130, -6.0F, -19.5F, 3.5F, 2, 1, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 90, 80, -6.0F, -24.5F, 2.5F, 12, 5, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 120, 74, 6.0F, -24.5F, 3.5F, 3, 5, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 0, 122, -9.0F, -24.5F, 3.5F, 3, 5, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 130, 19, -10.0F, -24.5F, 4.5F, 1, 5, 1, 0.0F, false));
		antenna.cubeList.add(new ModelBox(antenna, 52, 130, 9.0F, -24.5F, 4.5F, 1, 5, 1, 0.0F, false));

		tr = new ModelRenderer(this);
		tr.setRotationPoint(-4.5F, 16.5F, -9.0F);
		tr.cubeList.add(new ModelBox(tr, 92, 116, -1.5F, -5.5F, -2.0F, 2, 10, 1, 0.0F, false));
		tr.cubeList.add(new ModelBox(tr, 124, 128, -1.5F, -4.5F, -1.0F, 2, 2, 2, 0.0F, false));
		tr.cubeList.add(new ModelBox(tr, 88, 127, -1.5F, -1.5F, 1.0F, 2, 3, 2, 0.0F, false));
		tr.cubeList.add(new ModelBox(tr, 116, 123, -5.5F, -1.0F, -1.0F, 3, 2, 2, 0.0F, false));
		tr.cubeList.add(new ModelBox(tr, 80, 116, -2.5F, -2.5F, -1.0F, 4, 5, 2, 0.0F, false));
		tr.cubeList.add(new ModelBox(tr, 56, 108, -1.5F, -7.0F, -4.0F, 2, 13, 2, 0.0F, false));
		tr.cubeList.add(new ModelBox(tr, 52, 114, -2.5F, -5.0F, -2.0F, 1, 9, 1, 0.0F, false));
		tr.cubeList.add(new ModelBox(tr, 46, 114, -2.5F, -6.0F, -4.0F, 1, 11, 2, 0.0F, false));
		tr.cubeList.add(new ModelBox(tr, 76, 119, 0.5F, -5.0F, -2.0F, 1, 9, 1, 0.0F, false));
		tr.cubeList.add(new ModelBox(tr, 110, 116, 1.5F, -5.0F, -4.0F, 1, 9, 2, 0.0F, false));
		tr.cubeList.add(new ModelBox(tr, 98, 114, 0.5F, -6.0F, -4.0F, 1, 11, 2, 0.0F, false));
		tr.cubeList.add(new ModelBox(tr, 28, 125, -3.5F, -4.5F, -2.0F, 1, 8, 1, 0.0F, false));
		tr.cubeList.add(new ModelBox(tr, 64, 119, -4.5F, -4.5F, -4.0F, 1, 8, 2, 0.0F, false));
		tr.cubeList.add(new ModelBox(tr, 70, 119, 2.5F, -4.5F, -4.0F, 1, 8, 2, 0.0F, false));
		tr.cubeList.add(new ModelBox(tr, 32, 125, 1.5F, -4.5F, -2.0F, 1, 8, 1, 0.0F, false));
		tr.cubeList.add(new ModelBox(tr, 100, 127, 2.5F, -4.0F, -2.0F, 1, 7, 1, 0.0F, false));
		tr.cubeList.add(new ModelBox(tr, 130, 6, 1.5F, -6.0F, -3.5F, 3, 2, 1, 0.0F, false));
		tr.cubeList.add(new ModelBox(tr, 0, 132, 3.5F, -5.5F, -4.5F, 1, 1, 1, 0.0F, false));
		tr.cubeList.add(new ModelBox(tr, 96, 127, -4.5F, -4.0F, -2.0F, 1, 7, 1, 0.0F, false));
		tr.cubeList.add(new ModelBox(tr, 104, 116, -3.5F, -5.0F, -4.0F, 1, 9, 2, 0.0F, false));

		l = new ModelRenderer(this);
		l.setRotationPoint(12.0F, 11.5F, -2.0F);
		l.cubeList.add(new ModelBox(l, 116, 66, -2.0F, -1.5F, -1.5F, 4, 3, 3, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 0, 34, -1.5F, 2.5F, -21.5F, 3, 3, 31, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 68, 0, -2.0F, 1.5F, -17.0F, 4, 1, 27, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 132, 28, 1.0F, 2.5F, -11.0F, 1, 1, 1, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 44, 132, 1.0F, 2.5F, 6.0F, 1, 1, 1, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 64, 132, -2.0F, 2.5F, -11.0F, 1, 1, 1, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 60, 132, -2.0F, 2.5F, 6.0F, 1, 1, 1, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 100, 48, -1.0F, 0.5F, 1.0F, 0, 1, 8, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 102, 57, 1.0F, 0.5F, 1.0F, 0, 1, 8, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 126, 36, 1.0F, -0.5F, 1.0F, 0, 1, 4, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 126, 84, -1.0F, -0.5F, 1.0F, 0, 1, 4, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 90, 105, 1.0F, 0.5F, -9.0F, 0, 1, 8, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 80, 127, 1.0F, -0.5F, -6.0F, 0, 1, 4, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 74, 105, -1.0F, 0.5F, -9.0F, 0, 1, 8, 0.0F, false));
		l.cubeList.add(new ModelBox(l, 44, 127, -1.0F, -0.5F, -6.0F, 0, 1, 4, 0.0F, false));

		r = new ModelRenderer(this);
		r.setRotationPoint(-12.0F, 11.5F, -2.0F);
		r.cubeList.add(new ModelBox(r, 0, 68, -2.0F, 1.5F, -17.0F, 4, 1, 27, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 4, 132, -2.0F, 2.5F, -11.0F, 1, 1, 1, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 56, 132, 1.0F, 2.5F, -11.0F, 1, 1, 1, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 8, 132, -2.0F, 2.5F, 6.0F, 1, 1, 1, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 48, 132, 1.0F, 2.5F, 6.0F, 1, 1, 1, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 0, 0, -1.5F, 2.5F, -21.5F, 3, 3, 31, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 126, 123, -1.0F, -0.5F, 1.0F, 0, 1, 4, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 20, 126, 1.0F, -0.5F, 1.0F, 0, 1, 4, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 58, 99, 1.0F, 0.5F, 1.0F, 0, 1, 8, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 42, 96, -1.0F, 0.5F, 1.0F, 0, 1, 8, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 40, 105, 1.0F, 0.5F, -9.0F, 0, 1, 8, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 126, 118, 1.0F, -0.5F, -5.0F, 0, 1, 4, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 126, 113, -1.0F, -0.5F, -5.0F, 0, 1, 4, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 24, 103, -1.0F, 0.5F, -9.0F, 0, 1, 8, 0.0F, false));
		r.cubeList.add(new ModelBox(r, 74, 99, -2.0F, -1.5F, -1.5F, 4, 3, 3, 0.0F, false));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		base.render(f5);
		radar.render(f5);
		tr.render(f5);
		l.render(f5);
		r.render(f5);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}

	/**
	 * 渲染罗兰防空炮塔(带动画):
	 * - 水平转向由渲染器整体旋转整个实体处理(含底座/主体)
	 * - radar:绕自身 Y 轴顺时针旋转(雷达扫描),角度由服务端每 tick 同步
	 * - tr/l/r:追踪雷达与左右发射架,随目标位置绕各自旋转点俯仰
	 *
	 * @param turretPitchDeg 俯仰角(度,MC 语义:负=抬头、正=低头)
	 * @param radarAngleDeg  雷达旋转角(度,服务端同步值;仅供电+开机时递增)
	 */
	public void renderTurret(Entity entity, float f, float f1, float f2, float f3, float f4, float f5,
	                         float turretPitchDeg, float radarAngleDeg) {
		float pitchRad = (float) Math.toRadians(turretPitchDeg);
		// 雷达顺时针旋转(从上方看);渲染级镜像(scale -1)会反转 Y 旋转方向,故用正角度补偿
		radar.rotateAngleY = (float) Math.toRadians(radarAngleDeg);
		tr.rotateAngleX = pitchRad;
		l.rotateAngleX = pitchRad;
		r.rotateAngleX = pitchRad;
		base.render(f5);
		radar.render(f5);
		tr.render(f5);
		l.render(f5);
		r.render(f5);
		// 复原,避免影响其他渲染帧
		radar.rotateAngleY = 0.0F;
		tr.rotateAngleX = 0.0F;
		l.rotateAngleX = 0.0F;
		r.rotateAngleX = 0.0F;
	}
}