package com.teamytz.tgceaddon.client.models.guns;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.Entity;
import techguns.client.models.ModelMultipart;

public class ModelThunderHammer extends ModelMultipart {
    private final ModelRenderer thunderhammer;
    private final ModelRenderer main;
    private final ModelRenderer lightning1;
    private final ModelRenderer lightning2;

    public ModelThunderHammer() {
        textureWidth = 128;
        textureHeight = 128;

        // 根节点，和链锯剑一样
        thunderhammer = new ModelRenderer(this);
        thunderhammer.setRotationPoint(2.0F, 24.0F, 0.0F);
        setRotationAngle(thunderhammer, -0.25F, 0.0F, -0.1F);

        // main 节点：基于 bbmodel origin [0,-2,0]，rotation [0,0,0]
        // flip_y 导致 Y 取反，所以 rotationPoint 的 Y 是 2.0F
        main = new ModelRenderer(this);
        main.setRotationPoint(0.0F, 2.0F, 0.0F);
        thunderhammer.addChild(main);
        // 无翻转，flip_y已处理Y轴方向
        setRotationAngle(main, 0.0F, 0.0F, 0.0F);

        // ===== handle 组 =====
        // handle 组：origin [0,-2,0]，相对于 main 的 origin [0,-2,0]
        ModelRenderer handle = new ModelRenderer(this);
        handle.setRotationPoint(0.0F, 0.0F, 0.0F);
        main.addChild(handle);

        // handle_bottom (cube): from [-1.5,0,-1.5] to [1.5,4,1.5], origin [0,0,-1]
        // rotationPoint = [0 - 0, -(0 - (-2)), -1 - 0] = [0, -2, -1]
        // addBox: x=-1.5, y=-(4-0)=-4, z=-1.5-(-1)=-0.5
        ModelRenderer handle_bottom = new ModelRenderer(this, 10, 67);
        handle_bottom.setRotationPoint(0.0F, -2.0F, -1.0F);
        handle.addChild(handle_bottom);
        handle_bottom.addBox(-1.5F, -4.0F, -0.5F, 3, 4, 3, 0.0F);

        // handle_main (cube): from [-1,4,-1] to [1,34,1], origin [0,0,0]
        // rotationPoint = [0 - 0, -(0 - (-2)), 0 - 0] = [0, -2, 0]
        // addBox: x=-1, y=-(34-0)=-34, z=-1
        ModelRenderer handle_main = new ModelRenderer(this, 40, 0);
        handle_main.setRotationPoint(0.0F, -2.0F, 0.0F);
        handle.addChild(handle_main);
        handle_main.addBox(-1.0F, -34.0F, -1.0F, 2, 30, 2, 0.0F);

        // handle2: from [-1.5,34,-1.5] to [1.5,35,1.5], origin [0,34,-1]
        // rotationPoint = [0 - 0, -(34 - (-2)), -1 - 0] = [0, -36, -1]
        // addBox: x=-1.5, y=-(35-34)=-1, z=-1.5-(-1)=-0.5
        ModelRenderer handle2 = new ModelRenderer(this, 28, 67);
        handle2.setRotationPoint(0.0F, -36.0F, -1.0F);
        handle.addChild(handle2);
        handle2.addBox(-1.5F, -1.0F, -0.5F, 3, 1, 3, 0.0F);

        // handle7: from [-2,36,-2] to [2,50,2], origin [0,31,0]
        // rotationPoint = [0 - 0, -(31 - (-2)), 0 - 0] = [0, -33, 0]
        // addBox: x=-2, y=-(50-31)=-19, z=-2
        ModelRenderer handle7 = new ModelRenderer(this, 40, 48);
        handle7.setRotationPoint(0.0F, -33.0F, 0.0F);
        handle.addChild(handle7);
        handle7.addBox(-2.0F, -19.0F, -2.0F, 4, 14, 4, 0.0F);

        // handle3: from [-1,35,-1] to [1,36,1], origin [0,31,0]
        // rotationPoint = [0 - 0, -(31 - (-2)), 0 - 0] = [0, -33, 0]
        // addBox: x=-1, y=-(36-31)=-5, z=-1
        ModelRenderer handle3 = new ModelRenderer(this, 28, 71);
        handle3.setRotationPoint(0.0F, -33.0F, 0.0F);
        handle.addChild(handle3);
        handle3.addBox(-1.0F, -5.0F, -1.0F, 2, 1, 2, 0.0F);

        // handle8: from [-2,53,-2] to [2,62,2], origin [0,49,0]
        // rotationPoint = [0 - 0, -(49 - (-2)), 0 - 0] = [0, -51, 0]
        // addBox: x=-2, y=-(62-49)=-13, z=-2
        ModelRenderer handle8 = new ModelRenderer(this, 24, 54);
        handle8.setRotationPoint(0.0F, -51.0F, 0.0F);
        handle.addChild(handle8);
        handle8.addBox(-2.0F, -13.0F, -2.0F, 4, 9, 4, 0.0F);

        // handle6: from [-2.5,50,-2.5] to [2.5,51,2.5], origin [0,50,-1]
        // rotationPoint = [0 - 0, -(50 - (-2)), -1 - 0] = [0, -52, -1]
        // addBox: x=-2.5, y=-(51-50)=-1, z=-2.5-(-1)=-1.5
        ModelRenderer handle6 = new ModelRenderer(this, 0, 61);
        handle6.setRotationPoint(0.0F, -52.0F, -1.0F);
        handle.addChild(handle6);
        handle6.addBox(-2.5F, -1.0F, -1.5F, 5, 1, 5, 0.0F);

        // handle4: from [-2,51,-2] to [2,52,2], origin [0,47,0]
        // rotationPoint = [0 - 0, -(47 - (-2)), 0 - 0] = [0, -49, 0]
        // addBox: x=-2, y=-(52-47)=-5, z=-2
        ModelRenderer handle4 = new ModelRenderer(this, 62, 40);
        handle4.setRotationPoint(0.0F, -49.0F, 0.0F);
        handle.addChild(handle4);
        handle4.addBox(-2.0F, -5.0F, -2.0F, 4, 1, 4, 0.0F);

        // handle5: from [-2.5,52,-2.5] to [2.5,53,2.5], origin [0,52,-1]
        // rotationPoint = [0 - 0, -(52 - (-2)), -1 - 0] = [0, -54, -1]
        // addBox: x=-2.5, y=-(53-52)=-1, z=-2.5-(-1)=-1.5
        ModelRenderer handle5 = new ModelRenderer(this, 56, 55);
        handle5.setRotationPoint(0.0F, -54.0F, -1.0F);
        handle.addChild(handle5);
        handle5.addBox(-2.5F, -1.0F, -1.5F, 5, 1, 5, 0.0F);

        // handle_top: from [-1,62,-1] to [1,64,1], origin [0,62,0]
        // rotationPoint = [0 - 0, -(62 - (-2)), 0 - 0] = [0, -64, 0]
        // addBox: x=-1, y=-(64-62)=-2, z=-1
        ModelRenderer handle_top = new ModelRenderer(this, 70, 10);
        handle_top.setRotationPoint(0.0F, -64.0F, 0.0F);
        handle.addChild(handle_top);
        handle_top.addBox(-1.0F, -2.0F, -1.0F, 2, 2, 2, 0.0F);

        // ===== head 组 (rotation [0,90,0]) =====
        // head 组：origin [0,-2,0]，相对于 main 的 origin [0,-2,0]
        ModelRenderer head = new ModelRenderer(this);
        head.setRotationPoint(0.0F, 0.0F, 0.0F);
        main.addChild(head);
        setRotationAngle(head, 0.0F, 1.5708F, 0.0F);

        // head1: from [-4,54,-9] to [-2,63,9], origin [-2,50,4]
        // rotationPoint = [-2 - 0, -(50 - (-2)), 4 - 0] = [-2, -52, 4]
        // addBox: x=-4-(-2)=-2, y=-(63-50)=-13, z=-9-4=-13
        ModelRenderer head1 = new ModelRenderer(this, 0, 0);
        head1.setRotationPoint(-2.0F, -52.0F, 4.0F);
        head.addChild(head1);
        head1.addBox(-2.0F, -13.0F, -13.0F, 2, 9, 18, 0.0F);

        // head2: from [2,54,-9] to [4,63,9], origin [4,50,4]
        // rotationPoint = [4 - 0, -(50 - (-2)), 4 - 0] = [4, -52, 4]
        // addBox: x=2-4=-2, y=-(63-50)=-13, z=-9-4=-13
        ModelRenderer head2 = new ModelRenderer(this, 0, 27);
        head2.setRotationPoint(4.0F, -52.0F, 4.0F);
        head.addChild(head2);
        head2.addBox(-2.0F, -13.0F, -13.0F, 2, 9, 18, 0.0F);

        // head3: from [-2,54,-9] to [2,63,-2], origin [0,50,4]
        // rotationPoint = [0 - 0, -(50 - (-2)), 4 - 0] = [0, -52, 4]
        // addBox: x=-2-0=-2, y=-(63-50)=-13, z=-9-4=-13
        ModelRenderer head3 = new ModelRenderer(this, 40, 32);
        head3.setRotationPoint(0.0F, -52.0F, 4.0F);
        head.addChild(head3);
        head3.addBox(-2.0F, -13.0F, -13.0F, 4, 9, 7, 0.0F);

        // head4: from [-2,54,2] to [2,63,9], origin [0,50,13]
        // rotationPoint = [0 - 0, -(50 - (-2)), 13 - 0] = [0, -52, 13]
        // addBox: x=-2-0=-2, y=-(63-50)=-13, z=2-13=-11
        ModelRenderer head4 = new ModelRenderer(this, 48, 0);
        head4.setRotationPoint(0.0F, -52.0F, 13.0F);
        head.addChild(head4);
        head4.addBox(-2.0F, -13.0F, -11.0F, 4, 9, 7, 0.0F);

        // head5: from [-3.5,53.5,-10] to [3.5,62.5,-9], origin [0,50,-3]
        // rotationPoint = [0 - 0, -(50 - (-2)), -3 - 0] = [0, -52, -3]
        // addBox: x=-3.5-0=-3.5, y=-(62.5-50)=-12.5, z=-10-(-3)=-7
        ModelRenderer head5 = new ModelRenderer(this, 56, 61);
        head5.setRotationPoint(0.0F, -52.0F, -3.0F);
        head.addChild(head5);
        head5.addBox(-3.5F, -12.5F, -7.0F, 7, 9, 1, 0.0F);

        // head6: from [-3.5,53.5,9] to [3.5,62.5,10], origin [0,50,16]
        // rotationPoint = [0 - 0, -(50 - (-2)), 16 - 0] = [0, -52, 16]
        // addBox: x=-3.5-0=-3.5, y=-(62.5-50)=-12.5, z=9-16=-7
        ModelRenderer head6 = new ModelRenderer(this, 62, 30);
        head6.setRotationPoint(0.0F, -52.0F, 16.0F);
        head.addChild(head6);
        head6.addBox(-3.5F, -12.5F, -7.0F, 7, 9, 1, 0.0F);

        // head8: from [-4,53,-9] to [4,54,-3], origin [0,49,2]
        // rotationPoint = [0 - 0, -(49 - (-2)), 2 - 0] = [0, -51, 2]
        // addBox: x=-4-0=-4, y=-(54-49)=-5, z=-9-2=-11
        ModelRenderer head8 = new ModelRenderer(this, 48, 23);
        head8.setRotationPoint(0.0F, -51.0F, 2.0F);
        head.addChild(head8);
        head8.addBox(-4.0F, -5.0F, -11.0F, 8, 1, 6, 0.0F);

        // head7: from [-4,53,3] to [4,54,9], origin [0,49,14]
        // rotationPoint = [0 - 0, -(49 - (-2)), 14 - 0] = [0, -51, 14]
        // addBox: x=-4-0=-4, y=-(54-49)=-5, z=3-14=-11
        ModelRenderer head7 = new ModelRenderer(this, 48, 16);
        head7.setRotationPoint(0.0F, -51.0F, 14.0F);
        head.addChild(head7);
        head7.addBox(-4.0F, -5.0F, -11.0F, 8, 1, 6, 0.0F);

        // head9: from [-3,63,-9] to [3,64,-3], origin [0,59,1]
        // rotationPoint = [0 - 0, -(59 - (-2)), 1 - 0] = [0, -61, 1]
        // addBox: x=-3-0=-3, y=-(64-59)=-5, z=-9-1=-10
        ModelRenderer head9 = new ModelRenderer(this, 0, 54);
        head9.setRotationPoint(0.0F, -61.0F, 1.0F);
        head.addChild(head9);
        head9.addBox(-3.0F, -5.0F, -10.0F, 6, 1, 6, 0.0F);

        // head10: from [-3,63,3] to [3,64,9], origin [0,59,13]
        // rotationPoint = [0 - 0, -(59 - (-2)), 13 - 0] = [0, -61, 13]
        // addBox: x=-3-0=-3, y=-(64-59)=-5, z=3-13=-10
        ModelRenderer head10 = new ModelRenderer(this, 56, 48);
        head10.setRotationPoint(0.0F, -61.0F, 13.0F);
        head.addChild(head10);
        head10.addBox(-3.0F, -5.0F, -10.0F, 6, 1, 6, 0.0F);

        // ===== Pipeline 组 =====
        // Pipeline 组：origin [0,-2,0]，相对于 main 的 origin [0,-2,0]
        ModelRenderer pipeline = new ModelRenderer(this);
        pipeline.setRotationPoint(0.0F, 0.0F, 0.0F);
        main.addChild(pipeline);

        // pipeline1: from [-4,47.5,-1] to [-2,49.5,1], origin [-3,48,0]
        // rotationPoint = [-3 - 0, -(48 - (-2)), 0 - 0] = [-3, -50, 0]
        // addBox: x=-4-(-3)=-1, y=-(49.5-48)=-1.5, z=-1-0=-1
        ModelRenderer pipeline1 = new ModelRenderer(this, 70, 0);
        pipeline1.setRotationPoint(-3.0F, -50.0F, 0.0F);
        pipeline.addChild(pipeline1);
        pipeline1.addBox(-1.0F, -1.5F, -1.0F, 2, 2, 2, 0.0F);

        // pipeline4: from [-5,48,-0.5] to [-4,49,0.5], origin [-4.5,48,0]
        // rotationPoint = [-4.5 - 0, -(48 - (-2)), 0 - 0] = [-4.5, -50, 0]
        // addBox: x=-5-(-4.5)=-0.5, y=-(49-48)=-1, z=-0.5-0=-0.5
        ModelRenderer pipeline4 = new ModelRenderer(this, 20, 61);
        pipeline4.setRotationPoint(-4.5F, -50.0F, 0.0F);
        pipeline.addChild(pipeline4);
        pipeline4.addBox(-0.5F, -1.0F, -0.5F, 1, 1, 1, 0.0F);

        // pipeline6: from [-6,48,-0.5] to [-5,49,0.5], origin [-5,48.5,0]
        // rotationPoint = [-5 - 0, -(48.5 - (-2)), 0 - 0] = [-5, -50.5, 0]
        // addBox: x=-6-(-5)=-1, y=-(49-48.5)=-0.5, z=-0.5-0=-0.5
        ModelRenderer pipeline6 = new ModelRenderer(this, 20, 65);
        pipeline6.setRotationPoint(-5.0F, -50.5F, 0.0F);
        pipeline.addChild(pipeline6);
        pipeline6.addBox(-1.0F, -0.5F, -0.5F, 1, 1, 1, 0.0F);

        // pipeline5: from [-6,49,-0.5] to [-5,50,0.5], origin [-5,49.5,0]
        // rotationPoint = [-5 - 0, -(49.5 - (-2)), 0 - 0] = [-5, -51.5, 0]
        // addBox: x=-6-(-5)=-1, y=-(50-49.5)=-0.5, z=-0.5-0=-0.5
        ModelRenderer pipeline5 = new ModelRenderer(this, 20, 63);
        pipeline5.setRotationPoint(-5.0F, -51.5F, 0.0F);
        pipeline.addChild(pipeline5);
        pipeline5.addBox(-1.0F, -0.5F, -0.5F, 1, 1, 1, 0.0F);

        // pipeline7: from [-7,49,-0.5] to [-6,50,0.5], origin [-6,49.5,0]
        // rotationPoint = [-6 - 0, -(49.5 - (-2)), 0 - 0] = [-6, -51.5, 0]
        // addBox: x=-7-(-6)=-1, y=-(50-49.5)=-0.5, z=-0.5-0=-0.5
        ModelRenderer pipeline7 = new ModelRenderer(this, 70, 14);
        pipeline7.setRotationPoint(-6.0F, -51.5F, 0.0F);
        pipeline.addChild(pipeline7);
        pipeline7.addBox(-1.0F, -0.5F, -0.5F, 1, 1, 1, 0.0F);

        // pipeline2: from [-7,50,-0.5] to [-6,51,0.5], origin [-6,50.5,0]
        // rotationPoint = [-6 - 0, -(50.5 - (-2)), 0 - 0] = [-6, -52.5, 0]
        // addBox: x=-7-(-6)=-1, y=-(51-50.5)=-0.5, z=-0.5-0=-0.5
        ModelRenderer pipeline2 = new ModelRenderer(this, 58, 30);
        pipeline2.setRotationPoint(-6.0F, -52.5F, 0.0F);
        pipeline.addChild(pipeline2);
        pipeline2.addBox(-1.0F, -0.5F, -0.5F, 1, 1, 1, 0.0F);

        // pipeline3: from [-7.5,51,-1] to [-5.5,53,1], origin [-6,51,0]
        // rotationPoint = [-6 - 0, -(51 - (-2)), 0 - 0] = [-6, -53, 0]
        // addBox: x=-7.5-(-6)=-1.5, y=-(53-51)=-2, z=-1-0=-1
        ModelRenderer pipeline3 = new ModelRenderer(this, 70, 4);
        pipeline3.setRotationPoint(-6.0F, -53.0F, 0.0F);
        pipeline.addChild(pipeline3);
        pipeline3.addBox(-1.5F, -2.0F, -1.0F, 3, 2, 2, 0.0F);

        // ===== vent 组 =====
        // vent 组：origin [0,-2,0]，相对于 main 的 origin [0,-2,0]
        ModelRenderer vent = new ModelRenderer(this);
        vent.setRotationPoint(0.0F, 0.0F, 0.0F);
        main.addChild(vent);

        // vent6: from [-3,38.5,0] to [-2,47.5,2], origin [-2,46,2]
        // rotationPoint = [-2 - 0, -(46 - (-2)), 2 - 0] = [-2, -48, 2]
        // addBox: x=-3-(-2)=-1, y=-(47.5-46)=-1.5, z=0-2=-2
        ModelRenderer vent6 = new ModelRenderer(this, 48, 66);
        vent6.setRotationPoint(-2.0F, -48.0F, 2.0F);
        vent.addChild(vent6);
        vent6.addBox(-1.0F, -1.5F, -2.0F, 1, 9, 2, 0.0F);

        // vent7: from [2,38.5,0] to [3,47.5,2], origin [3,46,2]
        // rotationPoint = [3 - 0, -(46 - (-2)), 2 - 0] = [3, -48, 2]
        // addBox: x=2-3=-1, y=-(47.5-46)=-1.5, z=0-2=-2
        ModelRenderer vent7 = new ModelRenderer(this, 22, 67);
        vent7.setRotationPoint(3.0F, -48.0F, 2.0F);
        vent.addChild(vent7);
        vent7.addBox(-1.0F, -1.5F, -2.0F, 1, 9, 2, 0.0F);

        // vent1: from [-1.5,36.5,2] to [1.5,49.5,3], origin [-1,46,3]
        // rotationPoint = [-1 - 0, -(46 - (-2)), 3 - 0] = [-1, -48, 3]
        // addBox: x=-1.5-(-1)=-0.5, y=-(49.5-46)=-3.5, z=2-3=-1
        ModelRenderer vent1 = new ModelRenderer(this, 40, 66);
        vent1.setRotationPoint(-1.0F, -48.0F, 3.0F);
        vent.addChild(vent1);
        vent1.addBox(-0.5F, -3.5F, -1.0F, 3, 13, 1, 0.0F);

        // vent2: from [-2,38.5,2] to [2,47.5,3], origin [-1,46,3]
        // rotationPoint = [-1 - 0, -(46 - (-2)), 3 - 0] = [-1, -48, 3]
        // addBox: x=-2-(-1)=-1, y=-(47.5-46)=-1.5, z=2-3=-1
        ModelRenderer vent2 = new ModelRenderer(this, 0, 67);
        vent2.setRotationPoint(-1.0F, -48.0F, 3.0F);
        vent.addChild(vent2);
        vent2.addBox(-1.0F, -1.5F, -1.0F, 4, 9, 1, 0.0F);

        // vent5: from [-2,40.5,3] to [2,41.5,4], origin [-1,41,4]
        // rotationPoint = [-1 - 0, -(41 - (-2)), 4 - 0] = [-1, -43, 4]
        // addBox: x=-2-(-1)=-1, y=-(41.5-41)=-0.5, z=3-4=-1
        ModelRenderer vent5 = new ModelRenderer(this, 70, 8);
        vent5.setRotationPoint(-1.0F, -43.0F, 4.0F);
        vent.addChild(vent5);
        vent5.addBox(-1.0F, -0.5F, -1.0F, 4, 1, 1, 0.0F);

        // vent4: from [-2,42.5,3] to [2,43.5,4], origin [-1,43,4]
        // rotationPoint = [-1 - 0, -(43 - (-2)), 4 - 0] = [-1, -45, 4]
        // addBox: x=-2-(-1)=-1, y=-(43.5-43)=-0.5, z=3-4=-1
        ModelRenderer vent4 = new ModelRenderer(this, 62, 45);
        vent4.setRotationPoint(-1.0F, -45.0F, 4.0F);
        vent.addChild(vent4);
        vent4.addBox(-1.0F, -0.5F, -1.0F, 4, 1, 1, 0.0F);

        // vent3: from [-2,44.5,3] to [2,45.5,4], origin [-1,45,4]
        // rotationPoint = [-1 - 0, -(45 - (-2)), 4 - 0] = [-1, -47, 4]
        // addBox: x=-2-(-1)=-1, y=-(45.5-45)=-0.5, z=3-4=-1
        ModelRenderer vent3 = new ModelRenderer(this, 48, 30);
        vent3.setRotationPoint(-1.0F, -47.0F, 4.0F);
        vent.addChild(vent3);
        vent3.addBox(-1.0F, -0.5F, -1.0F, 4, 1, 1, 0.0F);

        // ===== lightning1 组 =====
        // 参照链锯剑 blade1 的实现：作为顶层节点，复制 thunderhammer 的变换链
        lightning1 = new ModelRenderer(this);
        lightning1.setRotationPoint(2.0F, 24.0F, 0.0F);
        setRotationAngle(lightning1, -0.25F, 0.0F, -0.1F);

        ModelRenderer lightning1_main = new ModelRenderer(this);
        lightning1_main.setRotationPoint(0.0F, 2.0F, 0.0F);
        lightning1.addChild(lightning1_main);
        // 无翻转，flip_y已处理Y轴方向
        setRotationAngle(lightning1_main, 0.0F, 0.0F, 0.0F);

        // lightning1_cube: from [-10.5,53,-4.5] to [10.5,65,4.5], origin [0,56,-6]
        // rotationPoint = [0 - 0, -(56 - (-2)), -6 - 0] = [0, -58, -6]
        // addBox: x=-10.5-0=-10.5, y=-(65-56)=-9, z=-4.5-(-6)=1.5
        ModelRenderer lightning1_cube = new ModelRenderer(this, 68, 107);
        lightning1_cube.setRotationPoint(0.0F, -58.0F, -6.0F);
        lightning1_main.addChild(lightning1_cube);
        lightning1_cube.addBox(-10.5F, -9.0F, 1.5F, 21, 12, 9, 0.0F);

        // ===== lightning2 组 =====
        // 参照链锯剑 blade2 的实现：作为顶层节点，复制 thunderhammer 的变换链
        lightning2 = new ModelRenderer(this);
        lightning2.setRotationPoint(2.0F, 24.0F, 0.0F);
        setRotationAngle(lightning2, -0.25F, 0.0F, -0.1F);

        ModelRenderer lightning2_main = new ModelRenderer(this);
        lightning2_main.setRotationPoint(0.0F, 2.0F, 0.0F);
        lightning2.addChild(lightning2_main);
        // 无翻转，flip_y已处理Y轴方向
        setRotationAngle(lightning2_main, 0.0F, 0.0F, 0.0F);

        // lightning2_cube: from [-10.5,53,-4.5] to [10.5,65,4.5], origin [0,56,-6]
        // rotationPoint = [0 - 0, -(56 - (-2)), -6 - 0] = [0, -58, -6]
        // addBox: x=-10.5-0=-10.5, y=-(65-56)=-9, z=-4.5-(-6)=1.5
        ModelRenderer lightning2_cube = new ModelRenderer(this, 0, 107);
        lightning2_cube.setRotationPoint(0.0F, -58.0F, -6.0F);
        lightning2_main.addChild(lightning2_cube);
        lightning2_cube.addBox(-10.5F, -9.0F, 1.5F, 21, 12, 9, 0.0F);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, int ammoLeft, float reloadProgress, ItemCameraTransforms.TransformType transformType, int part, float fireProgress, float chargeProgress) {
        if (part == 0) {
            thunderhammer.render(scale);
        } else if (part == 1) {
            // 在有弹药时始终显示闪电动画
            if (ammoLeft > 0) {
                // 使用 entityIn.ticksExisted 作为帧切换计时器，确保动画持续更新
                int frame = (entityIn != null ? entityIn.ticksExisted : (int)ageInTicks) / 5 % 2;
                if (frame == 0) {
                    lightning1.render(scale);
                } else {
                    lightning2.render(scale);
                }
            }
        }
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}