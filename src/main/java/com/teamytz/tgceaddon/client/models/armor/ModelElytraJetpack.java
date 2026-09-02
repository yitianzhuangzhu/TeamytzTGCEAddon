package com.teamytz.tgceaddon.client.models.armor;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import techguns.capabilities.TGExtendedPlayer;
import techguns.client.ClientProxy;
import techguns.client.models.armor.ModelAdditionalSlotBase;
import techguns.util.MathUtil;

/**
 * 鞘翅喷气背包背部模型(基于科技枪 ModelJetPack 复制)
 *
 * 与科技枪原版喷气背包模型的区别:粒子喷口位置针对"鞘翅飞行姿态"修正。
 * 鞘翅飞行时玩家身体前倾俯卧,背部喷口应在玩家后方偏下(而非原版的
 * "身体上方 Y+1.5" 站立喷射位置);粒子仅在鞘翅飞行中加速时生成。
 */
public class ModelElytraJetpack extends ModelAdditionalSlotBase {

    private final ModelRenderer BackBack;
    private final ModelRenderer RocketR;
    private final ModelRenderer RocketL;
    private final ModelRenderer RocketTopR;
    private final ModelRenderer RocketTopL;

    public ModelElytraJetpack(int variant) {
        textureWidth = 32;
        textureHeight = 32;

        BackBack = new ModelRenderer(this, 16, 0);
        BackBack.addBox(-2F, 0F, 0F, 4, 7, 4);
        BackBack.setRotationPoint(0F, 1F, 2F);
        BackBack.setTextureSize(32, 32);
        BackBack.mirror = true;
        setRotation(BackBack, 0F, 0F, 0F);

        RocketR = new ModelRenderer(this, 0, 0);
        RocketR.addBox(-3F, 0F, 0F, 3, 9, 3);
        RocketR.setRotationPoint(-2F, -1F, 0.5F);
        RocketR.setTextureSize(32, 32);
        RocketR.mirror = true;
        setRotation(RocketR, 0F, 0F, 0F);
        this.BackBack.addChild(RocketR);

        RocketL = new ModelRenderer(this, 0, 0);
        RocketL.addBox(-3F, 0F, 0F, 3, 9, 3);
        RocketL.setRotationPoint(5F, -1F, 0.5F);
        RocketL.setTextureSize(32, 32);
        RocketL.mirror = true;
        setRotation(RocketL, 0F, 0F, 0F);
        this.BackBack.addChild(RocketL);

        RocketTopR = new ModelRenderer(this, 0, 13);
        RocketTopR.addBox(1F, 0F, -3.5F, 2, 2, 2);
        RocketTopR.setRotationPoint(-3.5F, -2F, 4F);
        RocketTopR.setTextureSize(32, 32);
        RocketTopR.mirror = true;
        setRotation(RocketTopR, 0F, 0F, 0F);
        this.RocketR.addChild(RocketTopR);

        RocketTopL = new ModelRenderer(this, 0, 13);
        RocketTopL.addBox(-6F, 0F, -3.5F, 2, 2, 2);
        RocketTopL.setRotationPoint(3.5F, -2F, 4F);
        RocketTopL.setTextureSize(32, 32);
        RocketTopL.mirror = true;
        setRotation(RocketTopL, 0F, 0F, 0F);
        this.RocketL.addChild(RocketTopL);
    }

    @Override
    public void render(float scale, Entity entityIn) {
        BackBack.render(scale);

        if (entityIn != null && entityIn instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer) entityIn;

            // 粒子条件:鞘翅飞行中按住空格(加速)才喷焰
            boolean showFlames = false;
            TGExtendedPlayer props = TGExtendedPlayer.get(entityPlayer);
            if (props != null) {
                showFlames = props.isJumpkeyPressed() && entityPlayer.isElytraFlying();
            }

            if (showFlames) {
                float rot = -entityPlayer.renderYawOffset;

                // 鞘翅飞行姿态:喷口在玩家身后偏下(而非原版的"身体上方 Y+1.5")
                double dZ = 0.6;   // 身后距离
                double dX = 0.25;  // 左右喷口偏移

                double offsetZ = dZ * MathUtil.cos360(rot) - dX * MathUtil.sin360(rot);
                double offsetX = dZ * MathUtil.sin360(rot) + dX * MathUtil.cos360(rot);
                double offsetZ2 = dZ * MathUtil.cos360(rot) - (dX * -1) * MathUtil.sin360(rot);
                double offsetX2 = dZ * MathUtil.sin360(rot) + (dX * -1) * MathUtil.cos360(rot);

                float offsetY = 0.7f;  // 背部高度(身体中心略上)

                float motiony = -0.025f;
                ClientProxy.get().createFX("FlamethrowerTrailFlames", entityPlayer.world,
                        entityPlayer.posX + offsetX, entityPlayer.posY + offsetY, entityPlayer.posZ + offsetZ, 0, motiony, 0);
                ClientProxy.get().createFX("FlamethrowerTrailFlames", entityPlayer.world,
                        entityPlayer.posX + offsetX2, entityPlayer.posY + offsetY, entityPlayer.posZ + offsetZ2, 0, motiony, 0);
            }
        }
    }

    @Override
    public void copyRotateAngles() {
        BackBack.rotateAngleX = this.bipedBody.rotateAngleX;
    }
}
