package techguns.events;

import java.util.UUID;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import techguns.TGConfig;
import techguns.TGPackets;
import techguns.TGRadiationSystem;
import techguns.TGSounds;
import techguns.Techguns;
import techguns.api.guns.IGenericGun;
import techguns.api.npc.INPCTechgunsShooter;
import techguns.api.radiation.TGRadiation;
import techguns.api.tginventory.ITGSpecialSlot;
import techguns.capabilities.TGExtendedPlayer;
import techguns.capabilities.TGExtendedPlayerClient;
import techguns.capabilities.TGShooterValues;
import techguns.client.ClientProxy;
import techguns.client.audio.TGSoundCategory;
import techguns.damagesystem.TGDamageSource;
import techguns.deatheffects.EntityDeathUtils.DeathType;
import techguns.items.additionalslots.ItemGasMask;
import techguns.items.armors.GenericArmor;
import techguns.items.armors.PoweredArmor;
import techguns.items.armors.TGArmorBonus;
import techguns.items.guns.GenericGun;
import techguns.items.guns.GenericGunCharge;
import techguns.packets.PacketPlaySound;
import techguns.packets.PacketShootGun;
import techguns.packets.PacketShootGunTarget;
import techguns.packets.PacketSwapWeapon;
import techguns.packets.PacketTGExtendedPlayerSync;
import techguns.radiation.ItemRadiationData;
import techguns.radiation.ItemRadiationRegistry;
import techguns.util.InventoryUtil;

@EventBusSubscriber(
   modid = "techguns"
)
public class TGTickHandler {
   private static final UUID UUID_SPEED = UUID.fromString("5D8E53EB-DCFA-4121-B4DB-99BCAFA6B70B");
   private static final UUID UUID_HEALTH = UUID.fromString("4CFA49EB-D215-498B-9CC9-4BD0D1350B1F");
   private static final UUID UUID_KNOCKBACK_RESISTANCE = UUID.fromString("3441FC5D-F0B6-47F4-AFBB-DC5005670254");

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public static void localClientPlayerTick(TickEvent.PlayerTickEvent event) {
      TGExtendedPlayer props = TGExtendedPlayer.get(event.player);
      if (event.phase == Phase.START) {
         ClientProxy cp = ClientProxy.get();
         if (event.player == cp.getPlayerClient()) {
            if (Minecraft.func_71410_x().field_71415_G && !event.player.func_175149_v()) {
               ItemStack stack = event.player.func_184614_ca();
               ItemStack stackOff = event.player.func_184592_cb();
               if (!stack.func_190926_b() && stack.func_77973_b() instanceof IGenericGun && ((IGenericGun)stack.func_77973_b()).isShootWithLeftClick()) {
                  if (cp.keyFirePressedMainhand) {
                     IGenericGun gun = (IGenericGun)stack.func_77973_b();
                     if (props.getFireDelay(EnumHand.MAIN_HAND) <= 0) {
                        if (gun instanceof GenericGunCharge && ((GenericGunCharge)gun).getLockOnTicks() > 0 && props.lockOnEntity != null && props.lockOnTicks > ((GenericGunCharge)gun).getLockOnTicks()) {
                           TGPackets.wrapper.sendToServer(new PacketShootGunTarget(gun.isZooming(), EnumHand.MAIN_HAND, props.lockOnEntity));
                           gun.shootGunPrimary(stack, event.player.field_70170_p, event.player, gun.isZooming(), EnumHand.MAIN_HAND, props.lockOnEntity);
                        } else {
                           TGPackets.wrapper.sendToServer(new PacketShootGun(gun.isZooming(), EnumHand.MAIN_HAND));
                           gun.shootGunPrimary(stack, event.player.field_70170_p, event.player, gun.isZooming(), EnumHand.MAIN_HAND, (Entity)null);
                        }
                     }

                     if (gun.isSemiAuto()) {
                        cp.keyFirePressedMainhand = false;
                     }
                  }
               } else {
                  cp.keyFirePressedMainhand = false;
               }

               if (!stackOff.func_190926_b() && stackOff.func_77973_b() instanceof IGenericGun && ((IGenericGun)stackOff.func_77973_b()).isShootWithLeftClick()) {
                  if (cp.keyFirePressedOffhand) {
                     IGenericGun gun = (IGenericGun)stackOff.func_77973_b();
                     if (props.getFireDelay(EnumHand.OFF_HAND) <= 0) {
                        TGPackets.wrapper.sendToServer(new PacketShootGun(gun.isZooming(), EnumHand.OFF_HAND));
                        gun.shootGunPrimary(stackOff, event.player.field_70170_p, event.player, gun.isZooming(), EnumHand.OFF_HAND, (Entity)null);
                     }

                     if (gun.isSemiAuto()) {
                        cp.keyFirePressedOffhand = false;
                     }
                  }
               } else {
                  cp.keyFirePressedOffhand = false;
               }

               if (!stack.func_190926_b() && stack.func_77973_b() instanceof GenericGunCharge && ((GenericGunCharge)stack.func_77973_b()).getLockOnTicks() > 0 && ((GenericGunCharge)stack.func_77973_b()).getAmmoLeft(stack) <= 0 && props.lockOnEntity != null) {
                  props.lockOnEntity = null;
                  props.lockOnTicks = -1;
               }
            } else {
               cp.keyFirePressedMainhand = false;
               cp.keyFirePressedOffhand = false;
            }
         }
      }

   }

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      TGExtendedPlayer props = TGExtendedPlayer.get(event.player);
      if (event.phase == Phase.START) {
         if (props.fireDelayMainhand > 0) {
            --props.fireDelayMainhand;
         }

         if (props.loopSoundDelayMainhand > 0) {
            --props.loopSoundDelayMainhand;
         }

         if (props.fireDelayOffhand > 0) {
            --props.fireDelayOffhand;
         }

         if (props.loopSoundDelayOffhand > 0) {
            --props.loopSoundDelayOffhand;
         }

         if (event.side == Side.SERVER && props.swingSoundDelay > 0) {
            --props.swingSoundDelay;
         }
      } else if (event.phase == Phase.END) {
         if (!event.player.field_70170_p.field_72995_K) {
            event.player.func_184212_Q().func_187227_b(TGExtendedPlayer.DATA_FACE_SLOT, props.tg_inventory.func_70301_a(0));
            event.player.func_184212_Q().func_187227_b(TGExtendedPlayer.DATA_BACK_SLOT, props.tg_inventory.func_70301_a(1));
            event.player.func_184212_Q().func_187227_b(TGExtendedPlayer.DATA_HAND_SLOT, props.tg_inventory.func_70301_a(2));
         } else {
            props.tg_inventory.func_70299_a(0, (ItemStack)event.player.func_184212_Q().func_187225_a(TGExtendedPlayer.DATA_FACE_SLOT));
            props.tg_inventory.func_70299_a(1, (ItemStack)event.player.func_184212_Q().func_187225_a(TGExtendedPlayer.DATA_BACK_SLOT));
            props.tg_inventory.func_70299_a(2, (ItemStack)event.player.func_184212_Q().func_187225_a(TGExtendedPlayer.DATA_HAND_SLOT));
         }

         boolean wearingTechgunsArmor = false;

         for(int i = 0; i < 4; ++i) {
            ItemStack istack = (ItemStack)event.player.field_71071_by.field_70460_b.get(i);
            if (GenericArmor.isTechgunArmor(istack)) {
               wearingTechgunsArmor = true;
               break;
            }
         }

         IAttributeInstance attributeMovespeed = event.player.func_110140_aT().func_111151_a(SharedMonsterAttributes.field_111263_d);
         AttributeModifier mod_speed = attributeMovespeed.func_111127_a(UUID_SPEED);
         if (mod_speed != null) {
            attributeMovespeed.func_111124_b(mod_speed);
         }

         IAttributeInstance attributeMaxHealth = event.player.func_110140_aT().func_111151_a(SharedMonsterAttributes.field_111267_a);
         AttributeModifier mod_health = attributeMaxHealth.func_111127_a(UUID_HEALTH);
         if (mod_health != null) {
            attributeMaxHealth.func_111124_b(mod_health);
         }

         IAttributeInstance attributeKnockbackResist = event.player.func_110140_aT().func_111151_a(SharedMonsterAttributes.field_111266_c);
         AttributeModifier mod_knockback_resistance = attributeKnockbackResist.func_111127_a(UUID_KNOCKBACK_RESISTANCE);
         if (mod_knockback_resistance != null) {
            attributeKnockbackResist.func_111124_b(mod_knockback_resistance);
         }

         if (wearingTechgunsArmor) {
            PoweredArmor.calculateConsumptionTick(event.player);
            if (event.player.func_70027_ad()) {
               float cooling = GenericArmor.getArmorBonusForPlayer(event.player, TGArmorBonus.COOLING_SYSTEM, false);
               if (cooling >= 1.0F) {
                  event.player.func_70066_B();
               }
            }

            float speed = GenericArmor.getArmorBonusForPlayer(event.player, TGArmorBonus.SPEED, false);
            if (event.player.func_70055_a(Material.field_151586_h) || event.player.func_70055_a(Material.field_151587_i)) {
               speed += GenericArmor.getArmorBonusForPlayer(event.player, TGArmorBonus.SPEED_WATER, false);
            }

            if (speed != 0.0F) {
               if (event.player.func_70051_ag()) {
                  if (speed > 0.0F) {
                     speed *= 2.0F;
                  } else {
                     speed /= 2.0F;
                  }
               }

               attributeMovespeed.func_111121_a(new AttributeModifier(UUID_SPEED, "TechgunsSpeedboost", (double)speed, 2));
            }

            float healthBonus = GenericArmor.getArmorBonusForPlayer(event.player, TGArmorBonus.EXTRA_HEART, false);
            if (healthBonus > 0.0F) {
               attributeMaxHealth.func_111121_a(new AttributeModifier(UUID_HEALTH, "TechgunsHealthbonus", (double)healthBonus, 0));
            }

            float knockbackresistance = GenericArmor.getArmorBonusForPlayer(event.player, TGArmorBonus.KNOCKBACK_RESISTANCE, false);
            if (knockbackresistance > 0.0F) {
               attributeKnockbackResist.func_111121_a(new AttributeModifier(UUID_KNOCKBACK_RESISTANCE, "TechgunsKnockbackresistbonus", (double)knockbackresistance, 0));
            }
         }

         if (event.player.field_70170_p.field_72995_K && event.player == ClientProxy.get().getPlayerClient()) {
            float stepassist = GenericArmor.getArmorBonusForPlayer(event.player, TGArmorBonus.STEPASSIST, false);
            Techguns.proxy.setHasStepassist(stepassist > 0.0F);
            if (props.enableStepAssist && stepassist > 0.0F) {
               if (event.player.field_70138_W < 1.1F) {
                  event.player.field_70138_W = 1.1F;
               }
            } else if (event.player.field_70138_W > 0.6F) {
               event.player.field_70138_W = 0.6F;
            }
         }

         boolean enabled = props.enableNightVision;
         float nightvision = GenericArmor.getArmorBonusForPlayer(event.player, TGArmorBonus.NIGHTVISION, enabled);
         Techguns.proxy.setHasNightvision(nightvision > 0.0F);
         if (nightvision > 0.0F && enabled && !event.player.field_70170_p.field_72995_K) {
            event.player.func_70690_d(new PotionEffect(MobEffects.field_76439_r, 220, 0, false, false));
         }

         float flightbonus = GenericArmor.getArmorBonusForPlayer(event.player, TGArmorBonus.CREATIVE_FLIGHT, false);
         if (flightbonus > 0.0F) {
            event.player.field_71075_bZ.field_75101_c = true;
            props.gotCreativeFlightLastTick = true;
            if (event.player.field_71075_bZ.field_75100_b) {
               if (!props.wasFlying) {
                  if (event.player.field_70170_p.field_72995_K) {
                     Techguns.proxy.createFXOnEntity("AntiGravRing", event.player);
                  } else {
                     TGPackets.wrapper.sendToAllAround(new PacketPlaySound(TGSounds.ANTI_GRAV_START, event.player, 1.0F, 1.0F, false, true, false, true, TGSoundCategory.PLAYER_EFFECT), TGPackets.targetPointAroundEnt(event.player, 50.0D));
                  }
               }

               props.wasFlying = true;
            } else {
               props.wasFlying = false;
            }
         } else {
            if (props.gotCreativeFlightLastTick && !event.player.field_71075_bZ.field_75098_d) {
               event.player.field_71075_bZ.field_75101_c = false;
               event.player.field_71075_bZ.field_75100_b = false;
            }

            props.gotCreativeFlightLastTick = false;
            props.wasFlying = false;
         }

         if (event.player.field_70170_p.field_72995_K) {
            float DEFAULT_FLYSPEED = 0.05F;
            if (event.player == ClientProxy.get().getPlayerClient()) {
               float flyspeedBonus = GenericArmor.getArmorBonusForPlayer(event.player, TGArmorBonus.FLYSPEED, false);
               if (flyspeedBonus > 0.0F) {
                  if (props.enableJetpack) {
                     Techguns.proxy.setFlySpeed((1.0F + flyspeedBonus) * 0.05F);
                  } else {
                     Techguns.proxy.setFlySpeed(0.05F);
                  }
               } else if (props.gotCreativeFlightLastTick) {
                  Techguns.proxy.setFlySpeed(0.05F);
               }
            }
         }

         if (!TGConfig.disableAutofeeder && event.player.func_71024_bL().func_75116_a() <= 19) {
            int needed = 20 - event.player.func_71024_bL().func_75116_a();
            if (props.foodleft > 0) {
               if (props.foodleft <= needed) {
                  event.player.func_71024_bL().func_75122_a(props.foodleft, props.lastSaturation);
                  props.foodleft = 0;
                  props.lastSaturation = 0.0F;
               } else {
                  event.player.func_71024_bL().func_75122_a(needed, props.lastSaturation);
                  props.foodleft -= (short)needed;
               }

               if (!event.player.field_70170_p.field_72995_K) {
                  TGPackets.wrapper.sendTo(new PacketTGExtendedPlayerSync(event.player, props, true), (EntityPlayerMP)event.player);
               }
            } else {
               ItemStack stack = InventoryUtil.consumeFood(props.tg_inventory.inventory, 3, 6);
               if (!stack.func_190926_b()) {
                  ItemFood food = (ItemFood)stack.func_77973_b();
                  food.func_77849_c(stack, event.player.field_70170_p, event.player);
                  if (!event.player.field_70170_p.field_72995_K) {
                     event.player.field_70170_p.func_184148_a((EntityPlayer)null, event.player.field_70165_t, event.player.field_70163_u, event.player.field_70161_v, SoundEvents.field_187739_dZ, SoundCategory.PLAYERS, 1.0F, 1.0F);
                  }

                  short left = (short)(food.func_150905_g(stack) - needed);
                  if (left > 0) {
                     event.player.func_71024_bL().func_75122_a(needed, food.func_150906_h(stack));
                     props.foodleft = left;
                     props.lastSaturation = food.func_150906_h(stack);
                  } else {
                     event.player.func_71024_bL().func_75122_a(food.func_150905_g(stack), food.func_150906_h(stack));
                     props.foodleft = 0;
                     props.lastSaturation = 0.0F;
                  }

                  if (!event.player.field_70170_p.field_72995_K) {
                     TGPackets.wrapper.sendTo(new PacketTGExtendedPlayerSync(event.player, props, true), (EntityPlayerMP)event.player);
                  }
               }
            }
         }

         IAttributeInstance attributeRadresistance = event.player.func_110140_aT().func_111151_a(TGRadiation.RADIATION_RESISTANCE);
         AttributeModifier rad_resist_faceslot = attributeRadresistance.func_111127_a(ItemGasMask.UUID_RAD_RESIST_FACE);
         if (rad_resist_faceslot != null) {
            attributeRadresistance.func_111124_b(rad_resist_faceslot);
         }

         props.isGliding = false;
         tickSlot((ItemStack)props.tg_inventory.inventory.get(0), event);
         tickSlot((ItemStack)props.tg_inventory.inventory.get(1), event);
         tickSlot((ItemStack)props.tg_inventory.inventory.get(2), event);
         Techguns.proxy.handlePlayerGliding(event.player);
         if (TGRadiationSystem.isEnabled() && event.side == Side.SERVER && event.player.field_70170_p.func_82737_E() % 30L == 0L) {
            int maxrad = 0;

            for(ItemStack stack : event.player.field_71071_by.field_70462_a) {
               if (!stack.func_190926_b()) {
                  ItemRadiationData data = ItemRadiationRegistry.getRadiationDataFor(stack);
                  if (data != null && data.radamount > maxrad) {
                     maxrad = data.radamount;
                  }
               }
            }

            if (maxrad > 0) {
               event.player.func_70690_d(new PotionEffect(TGRadiationSystem.radiation_effect, 30, maxrad - 1, false, false));
            }
         }

         if (event.side == Side.SERVER && event.player.field_70170_p.func_82737_E() % 20L == 0L) {
            if (props.radlevel >= TGRadiationSystem.MINOR_POISONING && props.radlevel < TGRadiationSystem.SEVERE_POISONING) {
               event.player.func_70690_d(new PotionEffect(MobEffects.field_76438_s, 30, 0, false, false));
            } else if (props.radlevel >= TGRadiationSystem.SEVERE_POISONING) {
               event.player.func_70690_d(new PotionEffect(MobEffects.field_76438_s, 30, 1, false, false));
               event.player.func_70690_d(new PotionEffect(MobEffects.field_76419_f, 30, 1, false, false));
               event.player.func_70690_d(new PotionEffect(MobEffects.field_76437_t, 30, 1, false, false));
            }

            if (props.radlevel >= TGRadiationSystem.LETHAL_POISONING) {
               event.player.func_70690_d(new PotionEffect(MobEffects.field_76431_k, 30, 1, false, false));
               event.player.func_70690_d(new PotionEffect(MobEffects.field_76421_d, 30, 1, false, false));
               event.player.func_70097_a(TGDamageSource.causeLethalRadPoisoningDamage((Entity)null, (Entity)null, DeathType.LASER), 2.0F);
            }
         }

         if (event.side == Side.SERVER) {
            ItemStack gunMH = ItemStack.field_190927_a;
            ItemStack gunOH = ItemStack.field_190927_a;
            if (!event.player.func_184614_ca().func_190926_b() && event.player.func_184614_ca().func_77973_b() instanceof GenericGun) {
               gunMH = event.player.func_184614_ca();
            }

            if (!event.player.func_184592_cb().func_190926_b() && event.player.func_184592_cb().func_77973_b() instanceof GenericGun) {
               gunOH = event.player.func_184592_cb();
            }

            if (!gunOH.func_190926_b() && props.gunMainHand == gunOH || !gunMH.func_190926_b() && props.gunOffHand == gunMH) {
               TGPackets.wrapper.sendToAllAround(new PacketSwapWeapon(event.player), TGPackets.targetPointAroundEnt(event.player, 50.0D));
               int i = props.fireDelayMainhand;
               props.fireDelayMainhand = props.fireDelayOffhand;
               props.fireDelayOffhand = i;
            }

            props.gunMainHand = gunMH;
            props.gunOffHand = gunOH;
         }

         if (event.side == Side.SERVER && props.isChargingWeapon() && (event.player.func_184605_cv() <= 0 || !(event.player.func_184614_ca().func_77973_b() instanceof GenericGunCharge))) {
            props.setChargingWeapon(false);
         }
      }

   }

   protected static void tickSlot(ItemStack slot, TickEvent.PlayerTickEvent event) {
      if (!slot.func_190926_b() && slot.func_77973_b() instanceof ITGSpecialSlot) {
         ITGSpecialSlot item = (ITGSpecialSlot)slot.func_77973_b();
         item.onPlayerTick(slot, event);
      }

   }

   @Method(
      modid = "albedo"
   )
   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public static void clientGameTick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END) {
         ClientProxy.get().activeLightPulses.removeIf((lightPulse) -> !lightPulse.updateGameTick());
      }

   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public static void TickParticleSystems(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END) {
         ClientProxy.get().particleManager.tickParticles();
      } else {
         World w = Minecraft.func_71410_x().field_71441_e;
         if (w != null) {
            w.func_175644_a(EntityLivingBase.class, (input) -> input instanceof INPCTechgunsShooter).forEach((e) -> {
               TGShooterValues shooter_values = TGShooterValues.get(e);
               shooter_values.tickParticles();
            });
            w.func_175661_b(EntityPlayer.class, (input) -> true).forEach((p) -> {
               TGExtendedPlayerClient props = TGExtendedPlayerClient.get(p);
               props.tickParticles();
            });
         }
      }

   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public static void onRenderTick(TickEvent.RenderTickEvent event) {
      if (event.phase == Phase.START) {
         ClientProxy cp = ClientProxy.get();
         cp.PARTIAL_TICK_TIME = event.renderTickTime;
         EntityPlayer player = cp.getPlayerClient();
         if (player != null) {
            ItemStack current_item = player.func_184614_ca();
            if (!current_item.func_190926_b()) {
               if (current_item.func_77973_b() instanceof IGenericGun) {
                  if (((IGenericGun)current_item.func_77973_b()).isHoldZoom()) {
                     if (player.func_70093_af()) {
                        cp.player_zoom = ((IGenericGun)current_item.func_77973_b()).getZoomMult();
                     } else {
                        cp.player_zoom = 1.0F;
                     }
                  } else if (!((IGenericGun)current_item.func_77973_b()).isZooming()) {
                     cp.player_zoom = 1.0F;
                  }
               } else {
                  cp.player_zoom = 1.0F;
               }
            } else {
               cp.player_zoom = 1.0F;
            }
         }
      }

   }
}
