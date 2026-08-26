package techguns.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import micdoodle8.mods.galacticraft.api.client.tabs.InventoryTabVanilla;
import micdoodle8.mods.galacticraft.api.client.tabs.TabRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import techguns.CommonProxy;
import techguns.TGArmors;
import techguns.TGBlocks;
import techguns.TGConfig;
import techguns.TGEntities;
import techguns.TGFluids;
import techguns.TGItems;
import techguns.TGSounds;
import techguns.TGuns;
import techguns.Techguns;
import techguns.api.npc.INPCTechgunsShooter;
import techguns.capabilities.TGDeathTypeCap;
import techguns.capabilities.TGDeathTypeCapStorage;
import techguns.capabilities.TGExtendedPlayerClient;
import techguns.capabilities.TGShooterValues;
import techguns.client.audio.TGSound;
import techguns.client.audio.TGSoundCategory;
import techguns.client.models.armor.ModelAdvancedShield;
import techguns.client.models.armor.ModelAntiGravPack;
import techguns.client.models.armor.ModelArmorCoat;
import techguns.client.models.armor.ModelBallisticShield;
import techguns.client.models.armor.ModelBeret;
import techguns.client.models.armor.ModelExoSuit;
import techguns.client.models.armor.ModelFaceMask;
import techguns.client.models.armor.ModelGasMask;
import techguns.client.models.armor.ModelGlider;
import techguns.client.models.armor.ModelGloves;
import techguns.client.models.armor.ModelJetPack;
import techguns.client.models.armor.ModelNightVisionGoggles;
import techguns.client.models.armor.ModelOxygenTanks;
import techguns.client.models.armor.ModelRiotShield;
import techguns.client.models.armor.ModelSteamArmor;
import techguns.client.models.armor.ModelT3PowerArmor;
import techguns.client.models.armor.ModelT4PowerArmorMk2;
import techguns.client.models.guns.ModelAK;
import techguns.client.models.guns.ModelAS50;
import techguns.client.models.guns.ModelAUG;
import techguns.client.models.guns.ModelBaseBaked;
import techguns.client.models.guns.ModelBaseBakedGrenadeLauncher;
import techguns.client.models.guns.ModelBiogun;
import techguns.client.models.guns.ModelBlasterRifle;
import techguns.client.models.guns.ModelBoltaction;
import techguns.client.models.guns.ModelChainsaw;
import techguns.client.models.guns.ModelCombatShotgun;
import techguns.client.models.guns.ModelFlamethrower;
import techguns.client.models.guns.ModelFragGrenade;
import techguns.client.models.guns.ModelGoldenRevolver;
import techguns.client.models.guns.ModelGrimReaper;
import techguns.client.models.guns.ModelGuidedMissileLauncher;
import techguns.client.models.guns.ModelHandgun;
import techguns.client.models.guns.ModelLMG;
import techguns.client.models.guns.ModelLaserPistol;
import techguns.client.models.guns.ModelLasergun;
import techguns.client.models.guns.ModelLasergun2;
import techguns.client.models.guns.ModelM4;
import techguns.client.models.guns.ModelM4Infiltrator;
import techguns.client.models.guns.ModelMac10;
import techguns.client.models.guns.ModelMibGun;
import techguns.client.models.guns.ModelMinigun;
import techguns.client.models.guns.ModelMiningDrill;
import techguns.client.models.guns.ModelNDR;
import techguns.client.models.guns.ModelNetherBlaster;
import techguns.client.models.guns.ModelPDW;
import techguns.client.models.guns.ModelPistol;
import techguns.client.models.guns.ModelPowerHammer;
import techguns.client.models.guns.ModelPulseRifle;
import techguns.client.models.guns.ModelRevolver;
import techguns.client.models.guns.ModelRocketLauncher;
import techguns.client.models.guns.ModelSawedOff;
import techguns.client.models.guns.ModelScar;
import techguns.client.models.guns.ModelShishkebap;
import techguns.client.models.guns.ModelSonicShotgun;
import techguns.client.models.guns.ModelStielgranate;
import techguns.client.models.guns.ModelTFG;
import techguns.client.models.guns.ModelTeslaGun;
import techguns.client.models.guns.ModelThompson;
import techguns.client.models.guns.ModelVector;
import techguns.client.models.items.ModelARMagazine;
import techguns.client.models.items.ModelAS50Mag;
import techguns.client.models.items.ModelLmgMag;
import techguns.client.models.machines.ModelAmmoPress;
import techguns.client.models.machines.ModelChemLab;
import techguns.client.models.machines.ModelMetalPress;
import techguns.client.models.machines.ModelTurretBase;
import techguns.client.models.projectiles.ModelRocket;
import techguns.client.particle.DeathEffect;
import techguns.client.particle.LightPulse;
import techguns.client.particle.TGFX;
import techguns.client.particle.TGParticleManager;
import techguns.client.particle.TGParticleSystem;
import techguns.client.render.AdditionalSlotRenderRegistry;
import techguns.client.render.ItemRenderHack;
import techguns.client.render.RenderAdditionalSlotItem;
import techguns.client.render.RenderAdditionalSlotSharedItem;
import techguns.client.render.entities.TGLayerRendererer;
import techguns.client.render.entities.npcs.RenderAlienBug;
import techguns.client.render.entities.npcs.RenderArmySoldier;
import techguns.client.render.entities.npcs.RenderAttackHelicopter;
import techguns.client.render.entities.npcs.RenderBandit;
import techguns.client.render.entities.npcs.RenderCommando;
import techguns.client.render.entities.npcs.RenderCyberDemon;
import techguns.client.render.entities.npcs.RenderDictatorDave;
import techguns.client.render.entities.npcs.RenderGhastling;
import techguns.client.render.entities.npcs.RenderNPCTurret;
import techguns.client.render.entities.npcs.RenderOutcast;
import techguns.client.render.entities.npcs.RenderPsychoSteve;
import techguns.client.render.entities.npcs.RenderSkeletonSoldier;
import techguns.client.render.entities.npcs.RenderStormTrooper;
import techguns.client.render.entities.npcs.RenderSuperMutant;
import techguns.client.render.entities.npcs.RenderZombieFarmer;
import techguns.client.render.entities.npcs.RenderZombieMiner;
import techguns.client.render.entities.npcs.RenderZombiePigmanSoldier;
import techguns.client.render.entities.npcs.RenderZombiePoliceman;
import techguns.client.render.entities.npcs.RenderZombieSoldier;
import techguns.client.render.entities.projectiles.RenderAdvancedBulletProjectile;
import techguns.client.render.entities.projectiles.RenderBioGunProjectile;
import techguns.client.render.entities.projectiles.RenderBlasterProjectile;
import techguns.client.render.entities.projectiles.RenderFlameThrowerProjectile;
import techguns.client.render.entities.projectiles.RenderFlyingGibs;
import techguns.client.render.entities.projectiles.RenderFragGrenadeProjectile;
import techguns.client.render.entities.projectiles.RenderGenericProjectile;
import techguns.client.render.entities.projectiles.RenderGrenade40mmProjectile;
import techguns.client.render.entities.projectiles.RenderGrenadeProjectile;
import techguns.client.render.entities.projectiles.RenderInvisibleProjectile;
import techguns.client.render.entities.projectiles.RenderLaserProjectile;
import techguns.client.render.entities.projectiles.RenderNDRProjectile;
import techguns.client.render.entities.projectiles.RenderRocketProjectile;
import techguns.client.render.entities.projectiles.RenderSonicShotgunProjectile;
import techguns.client.render.entities.projectiles.RenderStoneBulletProjectile;
import techguns.client.render.entities.projectiles.RenderTeslaProjectile;
import techguns.client.render.fx.IScreenEffect;
import techguns.client.render.fx.ScreenEffect;
import techguns.client.render.item.GunAnimation;
import techguns.client.render.item.RenderArmorItem;
import techguns.client.render.item.RenderGenericSharedItem3D;
import techguns.client.render.item.RenderGrenade;
import techguns.client.render.item.RenderGunBase;
import techguns.client.render.item.RenderGunBase90;
import techguns.client.render.item.RenderGunBaseObj;
import techguns.client.render.item.RenderGunChainsaw;
import techguns.client.render.item.RenderGunFlamethrower;
import techguns.client.render.item.RenderItemBase;
import techguns.client.render.item.RenderItemBaseRocketItem;
import techguns.client.render.item.RenderItemLMGMag;
import techguns.client.render.item.RenderMiningToolMultiTexture;
import techguns.client.render.item.RenderRocketLauncher;
import techguns.client.render.item.TileEntityItemRendererTGShield;
import techguns.client.render.tileentities.RenderBlastfurnace;
import techguns.client.render.tileentities.RenderChargingStation;
import techguns.client.render.tileentities.RenderDoor3x3Fast;
import techguns.client.render.tileentities.RenderDungeonGenerator;
import techguns.client.render.tileentities.RenderDungeonScanner;
import techguns.client.render.tileentities.RenderFabricator;
import techguns.client.render.tileentities.RenderGrinder;
import techguns.client.render.tileentities.RenderMachine;
import techguns.client.render.tileentities.RenderOreDrill;
import techguns.client.render.tileentities.RenderReactionChamber;
import techguns.client.render.tileentities.RenderTurret;
import techguns.deatheffects.EntityDeathUtils;
import techguns.deatheffects.EntityDeathUtils.DeathType;
import techguns.debug.Keybinds;
import techguns.entities.npcs.AlienBug;
import techguns.entities.npcs.ArmySoldier;
import techguns.entities.npcs.AttackHelicopter;
import techguns.entities.npcs.Bandit;
import techguns.entities.npcs.Commando;
import techguns.entities.npcs.CyberDemon;
import techguns.entities.npcs.DictatorDave;
import techguns.entities.npcs.Ghastling;
import techguns.entities.npcs.NPCTurret;
import techguns.entities.npcs.Outcast;
import techguns.entities.npcs.PsychoSteve;
import techguns.entities.npcs.SkeletonSoldier;
import techguns.entities.npcs.StormTrooper;
import techguns.entities.npcs.SuperMutantBasic;
import techguns.entities.npcs.SuperMutantElite;
import techguns.entities.npcs.SuperMutantHeavy;
import techguns.entities.npcs.ZombieFarmer;
import techguns.entities.npcs.ZombieMiner;
import techguns.entities.npcs.ZombiePigmanSoldier;
import techguns.entities.npcs.ZombiePoliceman;
import techguns.entities.npcs.ZombieSoldier;
import techguns.entities.projectiles.AdvancedBulletProjectile;
import techguns.entities.projectiles.BioGunProjectile;
import techguns.entities.projectiles.BlasterProjectile;
import techguns.entities.projectiles.ChainsawProjectile;
import techguns.entities.projectiles.CyberdemonBlasterProjectile;
import techguns.entities.projectiles.DeatomizerProjectile;
import techguns.entities.projectiles.FlamethrowerProjectile;
import techguns.entities.projectiles.FlyingGibs;
import techguns.entities.projectiles.FragGrenadeProjectile;
import techguns.entities.projectiles.GaussProjectile;
import techguns.entities.projectiles.GenericProjectile;
import techguns.entities.projectiles.GenericProjectileExplosive;
import techguns.entities.projectiles.GenericProjectileIncendiary;
import techguns.entities.projectiles.Grenade40mmProjectile;
import techguns.entities.projectiles.GrenadeProjectile;
import techguns.entities.projectiles.GuidedMissileProjectile;
import techguns.entities.projectiles.GuidedMissileProjectileHV;
import techguns.entities.projectiles.LaserProjectile;
import techguns.entities.projectiles.NDRProjectile;
import techguns.entities.projectiles.PowerHammerProjectile;
import techguns.entities.projectiles.RocketProjectile;
import techguns.entities.projectiles.RocketProjectileHV;
import techguns.entities.projectiles.RocketProjectileNuke;
import techguns.entities.projectiles.SonicShotgunProjectile;
import techguns.entities.projectiles.StoneBulletProjectile;
import techguns.entities.projectiles.TFGProjectile;
import techguns.entities.projectiles.TeslaProjectile;
import techguns.events.TGGuiEvents;
import techguns.events.TechgunsGuiHandler;
import techguns.gui.AmmoPressGui;
import techguns.gui.BlastFurnaceGui;
import techguns.gui.CamoBenchGui;
import techguns.gui.ChargingStationGui;
import techguns.gui.ChemLabGui;
import techguns.gui.Door3x3Gui;
import techguns.gui.DungeonGeneratorGui;
import techguns.gui.DungeonScannerGui;
import techguns.gui.ExplosiveChargeGui;
import techguns.gui.FabricatorGui;
import techguns.gui.GrinderGui;
import techguns.gui.MetalPressGui;
import techguns.gui.OreDrillGui;
import techguns.gui.ReactionChamberGui;
import techguns.gui.RepairBenchGui;
import techguns.gui.TurretGui;
import techguns.gui.UpgradeBenchGui;
import techguns.gui.containers.AmmoPressContainer;
import techguns.gui.containers.BlastFurnaceContainer;
import techguns.gui.containers.CamoBenchContainer;
import techguns.gui.containers.ChargingStationContainer;
import techguns.gui.containers.ChemLabContainer;
import techguns.gui.containers.Door3x3Container;
import techguns.gui.containers.DungeonGeneratorContainer;
import techguns.gui.containers.DungeonScannerContainer;
import techguns.gui.containers.ExplosiveChargeContainer;
import techguns.gui.containers.FabricatorContainer;
import techguns.gui.containers.GrinderContainer;
import techguns.gui.containers.MetalPressContainer;
import techguns.gui.containers.OreDrillContainer;
import techguns.gui.containers.ReactionChamberContainer;
import techguns.gui.containers.RepairBenchContainer;
import techguns.gui.containers.TurretContainer;
import techguns.gui.containers.UpgradeBenchContainer;
import techguns.gui.player.tabs.TGPlayerTab;
import techguns.items.guns.GenericGun;
import techguns.keybind.TGKeybinds;
import techguns.tileentities.AmmoPressTileEnt;
import techguns.tileentities.BlastFurnaceTileEnt;
import techguns.tileentities.CamoBenchTileEnt;
import techguns.tileentities.ChargingStationTileEnt;
import techguns.tileentities.ChemLabTileEnt;
import techguns.tileentities.Door3x3TileEntity;
import techguns.tileentities.DungeonGeneratorTileEnt;
import techguns.tileentities.DungeonScannerTileEnt;
import techguns.tileentities.ExplosiveChargeAdvTileEnt;
import techguns.tileentities.ExplosiveChargeTileEnt;
import techguns.tileentities.FabricatorTileEntMaster;
import techguns.tileentities.GrinderTileEnt;
import techguns.tileentities.MetalPressTileEnt;
import techguns.tileentities.OreDrillTileEntMaster;
import techguns.tileentities.ReactionChamberTileEntMaster;
import techguns.tileentities.RepairBenchTileEnt;
import techguns.tileentities.TurretTileEnt;
import techguns.tileentities.UpgradeBenchTileEnt;
import techguns.util.EntityCondition;

@EventBusSubscriber({Side.CLIENT})
public class ClientProxy extends CommonProxy {
   public TGParticleManager particleManager = new TGParticleManager();
   protected TechgunsGuiHandler.GuiHandlerRegister guihandler = new TechgunsGuiHandler.GuiHandlerRegister();
   public LinkedList activeLightPulses;
   private boolean lightPulsesEnabled = true;
   public boolean keyFirePressedMainhand;
   public boolean keyFirePressedOffhand;
   @SideOnly(Side.CLIENT)
   public float player_zoom = 1.0F;
   public long lastReloadsoundPlayed = 0L;
   public float PARTIAL_TICK_TIME;
   public float muzzleFlashJitterX = 0.0F;
   public float muzzleFlashJitterY = 0.0F;
   public float muzzleFlashJitterAngle = 0.0F;
   public float muzzleFlashJitterScale = 0.0F;
   public boolean hasStepassist = false;
   public boolean hasNightvision = false;
   protected HashMap armorModelRegistry = new HashMap();

   public void registerArmorModel(ResourceLocation key, ModelBiped model) {
      this.armorModelRegistry.put(key.toString(), model);
   }

   public ModelBiped getArmorModel(ResourceLocation key) {
      return (ModelBiped)this.armorModelRegistry.get(key.toString());
   }

   public void preInit(FMLPreInitializationEvent event) {
      super.preInit(event);
      OBJLoader.INSTANCE.addDomain("techguns");
      TGFX.loadFXList();
      if (!Loader.isModLoaded("albedo")) {
         this.lightPulsesEnabled = false;
      } else {
         this.activeLightPulses = new LinkedList();
      }

      this.registerArmorModel(TGArmors.ARMORMODEL_STEAM_ARMOR_0, new ModelSteamArmor(0));
      this.registerArmorModel(TGArmors.ARMORMODEL_STEAM_ARMOR_1, new ModelSteamArmor(1));
      this.registerArmorModel(TGArmors.ARMORMODEL_POWER_ARMOR_0, new ModelT3PowerArmor(0));
      this.registerArmorModel(TGArmors.ARMORMODEL_POWER_ARMOR_1, new ModelT3PowerArmor(1));
      this.registerArmorModel(TGArmors.ARMORMODEL_EXO_SUIT_0, new ModelExoSuit(0, 1.0F));
      this.registerArmorModel(TGArmors.ARMORMODEL_EXO_SUIT_1, new ModelExoSuit(1, 0.5F));
      this.registerArmorModel(TGArmors.ARMORMODEL_EXO_SUIT_2, new ModelExoSuit(0, 0.75F));
      this.registerArmorModel(TGArmors.ARMORMODEL_BERET_0, new ModelBeret());
      this.registerArmorModel(TGArmors.ARMORMODEL_COAT_0, new ModelArmorCoat(0, 1.0F));
      this.registerArmorModel(TGArmors.ARMORMODEL_COAT_1, new ModelArmorCoat(1, 0.51F));
      this.registerArmorModel(TGArmors.ARMORMODEL_COAT_2, new ModelArmorCoat(2, 0.49F));
      this.registerArmorModel(TGArmors.ARMORMODEL_COAT_3, new ModelArmorCoat(3, 0.75F));
      this.registerArmorModel(TGArmors.ARMORMODEL_STEAM_ARMOR_2, new ModelSteamArmor(0, 0.01F));
      this.registerArmorModel(TGArmors.ARMORMODEL_POWER_ARMOR_2, new ModelT3PowerArmor(0, 0.01F));
      this.registerArmorModel(TGArmors.ARMORMODEL_POWER_ARMOR_MK2_0, new ModelT4PowerArmorMk2(0));
      this.registerArmorModel(TGArmors.ARMORMODEL_POWER_ARMOR_MK2_1, new ModelT4PowerArmorMk2(1));
      this.registerArmorModel(TGArmors.ARMORMODEL_POWER_ARMOR_MK2_2, new ModelT4PowerArmorMk2(0, 0.01F));
   }

   public void init(FMLInitializationEvent event) {
      super.init(event);
      if (TGConfig.debug) {
         Keybinds.init();
      }

      TGKeybinds.init();
      MinecraftForge.EVENT_BUS.register(new TGGuiEvents());
      MinecraftForge.EVENT_BUS.register(new TGKeybinds());
      Map skinMap = Minecraft.func_71410_x().func_175598_ae().getSkinMap();
      RenderPlayer slim = (RenderPlayer)skinMap.get("slim");
      this.insertLayerAfterArmor(slim, new TGLayerRendererer(slim));
      RenderPlayer def = (RenderPlayer)skinMap.get("default");
      this.insertLayerAfterArmor(def, new TGLayerRendererer(def));
      ClientRegistry.bindTileEntitySpecialRenderer(AmmoPressTileEnt.class, new RenderMachine(new ModelAmmoPress(), new ResourceLocation("techguns", "textures/blocks/ammopress.png")));
      ClientRegistry.bindTileEntitySpecialRenderer(MetalPressTileEnt.class, new RenderMachine(new ModelMetalPress(), new ResourceLocation("techguns", "textures/blocks/metalpress.png")));
      ClientRegistry.bindTileEntitySpecialRenderer(ChemLabTileEnt.class, new RenderMachine(new ModelChemLab(), new ResourceLocation("techguns", "textures/blocks/chemlab.png")));
      ClientRegistry.bindTileEntitySpecialRenderer(TurretTileEnt.class, new RenderTurret(new ModelTurretBase()));
      ClientRegistry.bindTileEntitySpecialRenderer(FabricatorTileEntMaster.class, new RenderFabricator());
      ClientRegistry.bindTileEntitySpecialRenderer(ChargingStationTileEnt.class, new RenderChargingStation());
      ClientRegistry.bindTileEntitySpecialRenderer(ReactionChamberTileEntMaster.class, new RenderReactionChamber());
      ClientRegistry.bindTileEntitySpecialRenderer(DungeonScannerTileEnt.class, new RenderDungeonScanner());
      ClientRegistry.bindTileEntitySpecialRenderer(DungeonGeneratorTileEnt.class, new RenderDungeonGenerator());
      ClientRegistry.bindTileEntitySpecialRenderer(Door3x3TileEntity.class, new RenderDoor3x3Fast());
      ClientRegistry.bindTileEntitySpecialRenderer(OreDrillTileEntMaster.class, new RenderOreDrill());
      ClientRegistry.bindTileEntitySpecialRenderer(BlastFurnaceTileEnt.class, new RenderBlastfurnace());
      ClientRegistry.bindTileEntitySpecialRenderer(GrinderTileEnt.class, new RenderGrinder());
      this.initGuiHandler();
      if (TabRegistry.getTabList().isEmpty()) {
         MinecraftForge.EVENT_BUS.register(new TabRegistry());
         TabRegistry.registerTab(new InventoryTabVanilla());
      }

      TabRegistry.registerTab(new TGPlayerTab());
   }

   protected void initGuiHandler() {
      this.guihandler.addEntry(CamoBenchTileEnt.class, CamoBenchGui::new, CamoBenchContainer::new);
      this.guihandler.addEntry(RepairBenchTileEnt.class, RepairBenchGui::new, RepairBenchContainer::new);
      this.guihandler.addEntry(AmmoPressTileEnt.class, AmmoPressGui::new, AmmoPressContainer::new);
      this.guihandler.addEntry(MetalPressTileEnt.class, MetalPressGui::new, MetalPressContainer::new);
      this.guihandler.addEntry(ChemLabTileEnt.class, ChemLabGui::new, ChemLabContainer::new);
      this.guihandler.addEntry(TurretTileEnt.class, TurretGui::new, TurretContainer::new);
      this.guihandler.addEntry(FabricatorTileEntMaster.class, FabricatorGui::new, FabricatorContainer::new);
      this.guihandler.addEntry(ChargingStationTileEnt.class, ChargingStationGui::new, ChargingStationContainer::new);
      this.guihandler.addEntry(ReactionChamberTileEntMaster.class, ReactionChamberGui::new, ReactionChamberContainer::new);
      this.guihandler.addEntry(DungeonScannerTileEnt.class, DungeonScannerGui::new, DungeonScannerContainer::new);
      this.guihandler.addEntry(DungeonGeneratorTileEnt.class, DungeonGeneratorGui::new, DungeonGeneratorContainer::new);
      this.guihandler.addEntry(Door3x3TileEntity.class, Door3x3Gui::new, Door3x3Container::new);
      this.guihandler.addEntry(ExplosiveChargeTileEnt.class, ExplosiveChargeGui::new, ExplosiveChargeContainer::new);
      this.guihandler.addEntry(ExplosiveChargeAdvTileEnt.class, ExplosiveChargeGui::new, ExplosiveChargeContainer::new);
      this.guihandler.addEntry(OreDrillTileEntMaster.class, OreDrillGui::new, OreDrillContainer::new);
      this.guihandler.addEntry(BlastFurnaceTileEnt.class, BlastFurnaceGui::new, BlastFurnaceContainer::new);
      this.guihandler.addEntry(GrinderTileEnt.class, GrinderGui::new, GrinderContainer::new);
      this.guihandler.addEntry(UpgradeBenchTileEnt.class, UpgradeBenchGui::new, UpgradeBenchContainer::new);
   }

   public TechgunsGuiHandler.GuiHandlerRegister getGuihandlers() {
      return this.guihandler;
   }

   private void insertLayerAfterArmor(RenderPlayer r, LayerRenderer tglayer) {
      List layers = r.field_177097_h;

      for(int i = 0; i < layers.size(); ++i) {
         LayerRenderer layer = (LayerRenderer)layers.get(i);
         if (layer instanceof LayerBipedArmor) {
            layers.add(i + 1, tglayer);
            break;
         }
      }

   }

   public void postInit(FMLPostInitializationEvent event) {
      super.postInit(event);
      DeathEffect.postInit();
   }

   @SubscribeEvent
   public static void registerModels(ModelRegistryEvent event) {
      TGItems.initModels();
      TGArmors.initModels();
      TGuns.initModels();
      TGEntities.initModels();
      TGBlocks.initModels();
      TGFluids.initModels();
   }

   public void registerItemRenderers() {
      RenderGenericSharedItem3D sharedRenderer = new RenderGenericSharedItem3D();
      float[][] m4magTranslations = new float[][]{{0.0F, 0.25F, 0.0F}, {0.0F, 0.25F, 0.05F}, {0.1F, 0.25F, 0.0F}, {0.0F, -0.1F, 0.0F}, {0.0F, 0.0F, -0.05F}};
      float[][] as50magTranslations = new float[][]{{0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.05F, 0.0F}, {0.0F, -0.1F, 0.0F}, {0.0F, 0.0F, 0.0F}};
      float[][] lmgmagTranslations = new float[][]{{0.0F, 0.35F, 0.0F}, {0.0F, 0.15F, 0.0F}, {0.1F, 0.25F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.25F, -0.05F}};
      float[][] rocketTranslations = new float[][]{{0.0F, 0.0F, 0.0F}, {0.0F, -0.1F, 0.02F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}};
      sharedRenderer.addRenderForType("assaultriflemagazine", (new RenderItemBase(new ModelARMagazine(false), new ResourceLocation("techguns", "textures/guns/ar_mag.png"))).setBaseScale(1.25F).setGUIScale(0.85F).setBaseTranslation(0.0F, -0.2F, 0.0F).setTransformTranslations(m4magTranslations));
      sharedRenderer.addRenderForType("assaultriflemagazineempty", (new RenderItemBase(new ModelARMagazine(true), new ResourceLocation("techguns", "textures/guns/ar_mag.png"))).setBaseScale(1.25F).setGUIScale(0.85F).setBaseTranslation(0.0F, -0.2F, 0.0F).setTransformTranslations(m4magTranslations));
      sharedRenderer.addRenderForType("assaultriflemagazine_incendiary", (new RenderItemBase(new ModelARMagazine(false), new ResourceLocation("techguns", "textures/guns/ar_mag_inc.png"))).setBaseScale(1.25F).setGUIScale(0.85F).setBaseTranslation(0.0F, -0.2F, 0.0F).setTransformTranslations(m4magTranslations));
      sharedRenderer.addRenderForType("lmgmagazine", (new RenderItemLMGMag(new ModelLmgMag(false), new ResourceLocation("techguns", "textures/guns/lmg_mag.png"))).setBaseScale(1.25F).setGUIScale(0.75F).setBaseTranslation(0.0F, 0.0F, 0.2F).setTransformTranslations(lmgmagTranslations));
      sharedRenderer.addRenderForType("lmgmagazineempty", (new RenderItemLMGMag(new ModelLmgMag(true), new ResourceLocation("techguns", "textures/guns/lmg_mag.png"))).setBaseScale(1.25F).setGUIScale(0.75F).setBaseTranslation(0.0F, 0.0F, 0.2F).setTransformTranslations(lmgmagTranslations));
      sharedRenderer.addRenderForType("lmgmagazine_incendiary", (new RenderItemLMGMag(new ModelLmgMag(false), new ResourceLocation("techguns", "textures/guns/lmg_mag_inc.png"))).setBaseScale(1.25F).setGUIScale(0.75F).setBaseTranslation(0.0F, 0.0F, 0.2F).setTransformTranslations(lmgmagTranslations));
      sharedRenderer.addRenderForType("as50magazine", (new RenderItemBase(new ModelAS50Mag(false), new ResourceLocation("techguns", "textures/guns/as50_mag.png"))).setBaseScale(1.5F).setGUIScale(0.75F).setBaseTranslation(0.0325F, -0.2F, 0.33F).setTransformTranslations(as50magTranslations));
      sharedRenderer.addRenderForType("as50magazineempty", (new RenderItemBase(new ModelAS50Mag(true), new ResourceLocation("techguns", "textures/guns/as50_mag.png"))).setBaseScale(1.5F).setGUIScale(0.75F).setBaseTranslation(0.0325F, -0.2F, 0.33F).setTransformTranslations(as50magTranslations));
      sharedRenderer.addRenderForType("as50magazine_incendiary", (new RenderItemBase(new ModelAS50Mag(false), new ResourceLocation("techguns", "textures/guns/as50_mag_inc.png"))).setBaseScale(1.5F).setGUIScale(0.75F).setBaseTranslation(0.0325F, -0.2F, 0.33F).setTransformTranslations(as50magTranslations));
      sharedRenderer.addRenderForType("as50magazine_explosive", (new RenderItemBase(new ModelAS50Mag(false), new ResourceLocation("techguns", "textures/guns/as50_mag_exp.png"))).setBaseScale(1.5F).setGUIScale(0.75F).setBaseTranslation(0.0325F, -0.2F, 0.33F).setTransformTranslations(as50magTranslations));
      sharedRenderer.addRenderForType("rocket", (new RenderItemBaseRocketItem(new ModelRocket(), new ResourceLocation("techguns", "textures/guns/rocket.png"))).setBaseScale(1.5F).setGUIScale(0.5F).setBaseTranslation(0.0F, 0.0F, 0.1F).setTransformTranslations(rocketTranslations).setFirstPersonScale(0.35F));
      sharedRenderer.addRenderForType("rocket_nuke", (new RenderItemBaseRocketItem(new ModelRocket(), new ResourceLocation("techguns", "textures/guns/rocket_nuke.png"))).setBaseScale(1.5F).setGUIScale(0.5F).setBaseTranslation(0.0F, 0.0F, 0.1F).setTransformTranslations(rocketTranslations).setFirstPersonScale(0.35F));
      sharedRenderer.addRenderForType("rocket_high_velocity", (new RenderItemBaseRocketItem(new ModelRocket(), new ResourceLocation("techguns", "textures/guns/rocket_hv.png"))).setBaseScale(1.5F).setGUIScale(0.5F).setBaseTranslation(0.0F, 0.0F, 0.1F).setTransformTranslations(rocketTranslations).setFirstPersonScale(0.35F));
      ItemRenderHack.registerItemRenderer(TGItems.SHARED_ITEM, sharedRenderer);
      ItemRenderHack.registerItemRenderer(TGuns.m4, (new RenderGunBase(new ModelM4(), 1)).setBaseTranslation(0.03125F, -0.1F, 0.0F).setGUIScale(0.35F).setMuzzleFx(ScreenEffect.muzzleFlash_rifle, 0.0F, 0.18F, -1.29F, 0.75F, 0.0F).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.1F, 4.0F}).setTransformTranslations(new float[][]{{0.0F, 0.0F, -0.05F}, {0.0F, 0.01F, -0.1F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -0.05F}}).setMuzzleFXPos3P(0.13F, -1.0F).setMuzzleFlashJitter(0.02F, 0.02F, 5.0F, 0.1F));
      ItemRenderHack.registerItemRenderer(TGuns.ak47, (new RenderGunBase(new ModelAK(), 1)).setBaseTranslation(0.03125F, -0.1F, 0.0F).setBaseScale(0.75F).setGUIScale(0.35F).setMuzzleFx(ScreenEffect.muzzleFlash_rifle, 0.0F, 0.18F, -1.36F, 0.8F, 0.0F).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.1F, 4.0F}).setTransformTranslations(new float[][]{{0.0F, 0.06F, -0.02F}, {0.0F, 0.0F, -0.08F}, {0.06F, -0.01F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -0.05F}}).setMuzzleFXPos3P(0.08F, -1.02F).setMuzzleFlashJitter(0.02F, 0.02F, 5.0F, 0.1F));
      ItemRenderHack.registerItemRenderer(TGuns.lmg, (new RenderGunBase(new ModelLMG(), 1)).setBaseTranslation(0.03125F, -0.1F, 0.0F).setGUIScale(0.35F).setMuzzleFx(ScreenEffect.muzzleFlash_rifle, 0.0F, 0.21F, -1.5F, 0.78F, 0.0F).setMuzzleFXPos3P(0.13F, -0.98F).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.01F, 2.0F}).setTransformTranslations(new float[][]{{0.0F, 0.02F, -0.09F}, {0.0F, 0.0F, -0.06F}, {0.05F, -0.03F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -0.05F}}).setMuzzleFXPos3P(0.14F, -1.02F).setMuzzleFlashJitter(0.03F, 0.03F, 5.0F, 0.1F));
      ItemRenderHack.registerItemRenderer(TGuns.handcannon, (new RenderGunBase90(new ModelHandgun(), 1)).setBaseTranslation(0.0F, -0.2F, -0.0375F).setGUIScale(0.45F).setMuzzleFx(ScreenEffect.muzzleFlash_gun, 0.0F, 0.16F, -0.75F, 0.9F, 0.0F).setReloadAnim(GunAnimation.breechReload, new float[]{-0.15F, 55.0F}).setReloadAnim3p(GunAnimation.breechReload, new float[]{0.0F, 55.0F}).setTransformTranslations(new float[][]{{0.0F, 0.03F, -0.12F}, {0.0F, -0.05F, -0.09F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}}).setMuzzleFXPos3P(0.03F, -0.59F).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.2F, 25.0F}));
      ItemRenderHack.registerItemRenderer(TGuns.sawedoff, (new RenderGunBase90(new ModelSawedOff(), 1)).setBaseTranslation(0.0F, -0.2F, -0.0375F).setGUIScale(0.45F).setMuzzleFx(ScreenEffect.muzzleFlash_gun, 0.0F, 0.16F, -0.75F, 1.05F, 0.0F).setReloadAnim(GunAnimation.breechReload, new float[]{-0.15F, 55.0F}).setReloadAnim3p(GunAnimation.breechReload, new float[]{0.0F, 55.0F}).setTransformTranslations(new float[][]{{0.0F, 0.0F, 0.0F}, {0.0F, -0.04F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -0.05F}}).setMuzzleFXPos3P(0.06F, -0.49F).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.2F, 20.0F}));
      ItemRenderHack.registerItemRenderer(TGuns.thompson, (new RenderGunBase90(new ModelThompson(), 1)).setBaseTranslation(0.0F, -0.2F, -0.06875F).setGUIScale(0.45F).setMuzzleFx(ScreenEffect.muzzleFlash_rifle, 0.0F, 0.14F, -0.75F, 0.55F, 0.0F).setMuzzleFXPos3P(0.1F, -0.59F).setMuzzleFlashJitter(0.02F, 0.02F, 5.0F, 0.1F));
      ItemRenderHack.registerItemRenderer(TGuns.boltaction, (new RenderGunBase90(new ModelBoltaction(), 1)).setBaseTranslation(0.0F, -0.2F, -0.0375F).setBaseScale(1.35F).setGUIScale(0.35F).setMuzzleFx(ScreenEffect.muzzleFlash_gun, 0.0F, 0.21F, -1.48F, 0.9F, 0.0F).setScope(ScreenEffect.sniperScope).setTransformTranslations(new float[][]{{0.0F, -0.02F, -0.09F}, {0.0F, -0.04F, -0.11F}, {0.1F, -0.08F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -0.025F}}).setMuzzleFXPos3P(0.12F, -1.13F).setScopeRecoilAnim(GunAnimation.scopeRecoil, new float[]{0.2F, 2.0F}));
      ItemRenderHack.registerItemRenderer(TGuns.biogun, (new RenderGunBase90(new ModelBiogun(), 1)).setBaseTranslation(0.35F, -0.2F, -0.0375F).setGUIScale(0.45F).setMuzzleFx(ScreenEffect.muzzleGreenFlare, 0.0F, 0.23F, -0.51F, 0.55F, 0.0F).setTransformTranslations(new float[][]{{0.0F, 0.16F, 0.05F}, {0.0F, 0.07F, -0.05F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -0.05F}}).setMuzzleFXPos3P(0.1F, -0.51F).setChargeTranslationAmount(0.05F));
      ItemRenderHack.registerItemRenderer(TGuns.flamethrower, (new RenderGunFlamethrower(new ModelFlamethrower(), 1)).setBaseTranslation(0.0F, -0.2F, -0.0375F).setGUIScale(0.45F).setMuzzleFx(ScreenEffect.FlamethrowerMuzzleFlash, 0.0F, 0.16F, -0.9F, 0.45F, 0.0F).setTransformTranslations(new float[][]{{0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -0.08F}, {0.05F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -0.05F}}).setMuzzleFXPos3P(0.1F, -0.63F).setRecoilAnim(GunAnimation.swayRecoil, new float[]{0.025F, 2.5F}));
      ItemRenderHack.registerItemRenderer(TGuns.pistol, (new RenderGunBase(new ModelPistol(), 2)).setBaseTranslation(0.03125F, -0.3F, -0.4F).setBaseScale(1.2F).setGUIScale(0.9F).setMuzzleFx(ScreenEffect.muzzleFlash_gun, 0.03F, 0.2F, -0.5F, 0.55F, -0.03F).setTransformTranslations(new float[][]{{0.0F, 0.09F, -0.02F}, {0.0F, -0.03F, 0.0F}, {0.02F, -0.08F, 0.0F}, {0.02F, -0.08F, 0.0F}, {0.0F, 0.0F, 0.0F}}).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.025F, 12.0F}).setMuzzleFlashJitter(0.01F, 0.01F, 5.0F, 0.05F).setMuzzleFXPos3P(0.07F, -0.26F));
      ItemRenderHack.registerItemRenderer(TGuns.rocketlauncher, (new RenderRocketLauncher(new ModelRocketLauncher(), 2)).setBaseTranslation(-0.4F, -0.2F, -0.03125F).setGUIScale(0.45F).setMuzzleFx(ScreenEffect.muzzleFlash_gun, 0.0F, 0.39F, -0.6F, 0.87F, 0.0F).setTransformTranslations(new float[][]{{-0.13F, 0.3F, 0.32F}, {0.0F, 0.02F, 0.07F}, {-0.06F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}}).setMuzzleFXPos3P(0.09F, -0.26F));
      ItemRenderHack.registerItemRenderer(TGuns.minigun, (new RenderGunBase90(new ModelMinigun(), 2)).setBaseTranslation(0.0F, -0.2F, 0.03125F).setBaseScale(1.2F).setGUIScale(0.3F).setMuzzleFx(ScreenEffect.muzzleFlash_minigun, -0.04F, 0.05F, -1.25F, 0.8F, 0.04F).setTransformTranslations(new float[][]{{0.0F, -0.16F, 0.1F}, {0.0F, -0.53F, 0.2F}, {0.12F, -0.02F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -0.05F}}).setMuzzleFXPos3P(-0.38F, -0.78F).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.05F, 3.0F}));
      ItemRenderHack.registerItemRenderer(TGuns.combatshotgun, (new RenderGunBase90(new ModelCombatShotgun(), 2)).setBaseTranslation(0.0F, -0.2F, -0.0375F).setGUIScale(0.45F).setMuzzleFx(ScreenEffect.muzzleFlash_gun, 0.0F, 0.21F, -0.91F, 0.75F, 0.0F).setTransformTranslations(new float[][]{{0.0F, 0.03F, 0.0F}, {0.0F, -0.01F, -0.1F}, {0.05F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -0.05F}}).setMuzzleFXPos3P(0.12F, -0.81F).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.3F, 15.0F}));
      ItemRenderHack.registerItemRenderer(TGuns.revolver, (new RenderGunBase90(new ModelRevolver(), 1)).setBaseTranslation(-0.35F, -0.2F, -0.06875F).setBaseScale(0.75F).setGUIScale(0.75F).setMuzzleFx(ScreenEffect.muzzleFlash_gun, 0.0F, 0.25F, -0.41F, 0.5F, 0.0F).setTransformTranslations(new float[][]{{0.0F, 0.14F, 0.01F}, {0.0F, 0.0F, 0.0F}, {0.05F, 0.01F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}}).setMuzzleFXPos3P(0.09F, -0.3F).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.1F, 10.0F}));
      ItemRenderHack.registerItemRenderer(TGuns.grimreaper, (new RenderGunBase90(new ModelGrimReaper(), 1)).setBaseTranslation(0.3F, -0.2F, 0.03125F).setGUIScale(0.3F).setChargeTranslationAmount(0.025F).setMuzzleFx(ScreenEffect.muzzleFlash_gun, 0.0F, 0.39F, -0.61F, 0.75F, 0.0F).setTransformTranslations(new float[][]{{0.0F, 0.25F, 0.18F}, {0.0F, 0.13F, 0.1F}, {-0.02F, 0.01F, 0.0F}, {0.0F, 0.1F, 0.0F}, {0.0F, 0.0F, 0.0F}}).setMuzzleFXPos3P(0.24F, -0.56F).setBaseScale(1.25F).setFirstPersonScale(0.4F).setGroundAndFrameScale(0.35F));
      ItemRenderHack.registerItemRenderer(TGuns.pdw, (new RenderGunBase90(new ModelPDW(), 1)).setBaseTranslation(0.0F, -0.2F, -0.0062500015F).setGUIScale(0.55F).setMuzzleFx(ScreenEffect.muzzleFlash_blue, 0.0F, 0.13F, -0.58F, 0.55F, 0.0F).setTransformTranslations(new float[][]{{0.0F, 0.09F, -0.04F}, {0.0F, 0.06F, -0.02F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -0.05F}}).setMuzzleFXPos3P(0.11F, -0.41F).setMuzzleFlashJitter(0.01F, 0.01F, 5.0F, 0.1F).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.06F, 4.0F}));
      ItemRenderHack.registerItemRenderer(TGuns.as50, (new RenderGunBase(new ModelAS50(), 1)).setBaseTranslation(0.03125F, -0.1F, 0.0F).setBaseScale(0.85F).setGUIScale(0.3F).setMuzzleFx(ScreenEffect.muzzleFlash_rifle, 0.0F, 0.29F, -1.82F, 1.15F, 0.0F).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.25F, 4.0F}).setTransformTranslations(new float[][]{{0.0F, 0.06F, -0.1F}, {0.0F, 0.0F, -0.05F}, {0.13F, -0.09F, -0.05F}, {0.0F, 0.0F, 0.0F}, {0.0F, -0.2F, -0.05F}}).setMuzzleFXPos3P(0.17F, -1.29F).setScope(ScreenEffect.sniperScope).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.25F, 5.0F}).setScopeRecoilAnim(GunAnimation.scopeRecoil, new float[]{0.2F, 2.0F}));
      ItemRenderHack.registerItemRenderer(TGuns.m4_infiltrator, (new RenderGunBase(new ModelM4Infiltrator(), 1)).setBaseTranslation(0.03125F, -0.1F, 0.0F).setGUIScale(0.35F).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.1F, 4.0F}).setTransformTranslations(new float[][]{{0.0F, 0.0F, -0.05F}, {0.0F, 0.01F, -0.1F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -0.05F}}).setMuzzleFXPos3P(0.14F, -1.15F).setScope(ScreenEffect.sniperScope).setScopeRecoilAnim(GunAnimation.scopeRecoil, new float[]{0.05F, 1.0F}));
      ItemRenderHack.registerItemRenderer(TGuns.goldenrevolver, (new RenderGunBase90(new ModelGoldenRevolver(), 1)).setBaseTranslation(-0.35F, -0.2F, -0.06875F).setBaseScale(0.75F).setGUIScale(0.75F).setMuzzleFx(ScreenEffect.muzzleFlash_gun, 0.0F, 0.25F, -0.41F, 0.5F, 0.0F).setTransformTranslations(new float[][]{{0.0F, 0.14F, 0.01F}, {0.0F, 0.0F, 0.0F}, {0.05F, 0.01F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}}).setMuzzleFXPos3P(0.09F, -0.3F).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.1F, 15.0F}));
      ItemRenderHack.registerItemRenderer(TGuns.pulserifle, (new RenderGunBase90(new ModelPulseRifle(), 1)).setBaseTranslation(0.0F, -0.2F, 0.0037499964F).setGUIScale(0.45F).setMuzzleFx(ScreenEffect.muzzleFlash_blue, 0.0F, 0.22F, -0.76F, 0.6F, 0.0F).setTransformTranslations(new float[][]{{0.0F, 0.16F, 0.01F}, {0.0F, 0.05F, 0.08F}, {0.05F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -0.05F}}).setMuzzleFXPos3P(0.1F, -0.5F).setScope(ScreenEffect.techScope, 2.125F).setScopeRecoilAnim(GunAnimation.scopeRecoil, new float[]{0.1F, 1.5F}).setRecoilAnim(GunAnimation.pulseRifleRecoil, new float[]{0.25F, 10.0F}));
      ItemRenderHack.registerItemRenderer(TGuns.teslagun, (new RenderGunBase90(new ModelTeslaGun(), 1)).setBaseTranslation(0.25F, -0.2F, -0.027500004F).setGUIScale(0.45F).setMuzzleFx(ScreenEffect.muzzleFlashLightning, 0.0F, 0.26F, -0.67F, 0.5F, 0.0F).setTransformTranslations(new float[][]{{0.0F, 0.08F, 0.04F}, {0.0F, 0.05F, -0.08F}, {0.03F, 0.01F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -0.05F}}).setMuzzleFXPos3P(0.12F, -0.65F).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.2F, 5.0F}));
      ItemRenderHack.registerItemRenderer(TGuns.netherblaster, (new RenderGunBase(new ModelNetherBlaster(), 1)).setBaseTranslation(0.0F, -0.2F, -0.027500004F).setGUIScale(0.6F).setMuzzleFx(ScreenEffect.muzzleFlashFireball_alpha, 0.0F, 0.29F, -0.33F, 0.5F, 0.0F).setTransformTranslations(new float[][]{{0.0F, 0.15F, 0.04F}, {0.0F, -0.16F, -0.24F}, {-0.1F, 0.01F, 0.0F}, {0.0F, 0.0F, -0.11F}, {0.16F, -0.07F, -0.16F}}).setMuzzleFXPos3P(-0.06F, -0.45F));
      ItemRenderHack.registerItemRenderer(TGuns.lasergun, (new RenderGunBase90(new ModelLasergun(), 1)).setBaseTranslation(0.25F, -0.2F, -0.06875F).setBaseScale(1.1F).setGUIScale(0.4F).setMuzzleFx(ScreenEffect.muzzleFlashLaser, 0.0F, 0.3F, -1.06F, 0.5F, 0.0F).setTransformTranslations(new float[][]{{0.0F, 0.07F, 0.04F}, {0.0F, 0.02F, 0.01F}, {0.13F, 0.01F, 0.0F}, {0.0F, 0.0F, 0.15F}, {-0.18F, 0.0F, -0.05F}}).setMuzzleFXPos3P(0.11F, -0.83F).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.2F, 5.0F}));
      ItemRenderHack.registerItemRenderer(TGuns.blasterrifle, (new RenderGunBase(new ModelBlasterRifle(), 1)).setBaseTranslation(0.03125F, -0.1F, 0.0F).setBaseScale(0.9F).setGUIScale(0.45F).setMuzzleFx(ScreenEffect.muzzleFlashLaser, 0.0F, 0.24F, -0.93F, 0.75F, 0.0F).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.1F, 4.0F}).setTransformTranslations(new float[][]{{0.0F, 0.11F, -0.2F}, {0.0F, 0.0F, -0.04F}, {0.0F, 0.0F, 0.03F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -0.05F}}).setMuzzleFXPos3P(0.09F, -0.57F).setScope(ScreenEffect.sniperScope).setScopeRecoilAnim(GunAnimation.scopeRecoil, new float[]{0.05F, 1.0F}));
      ResourceLocation[] powerhammer_textures = new ResourceLocation[]{new ResourceLocation("techguns", "textures/guns/powerHammer.png"), new ResourceLocation("techguns", "textures/guns/powerHammer_obsidian.png"), new ResourceLocation("techguns", "textures/guns/powerHammer_carbon.png")};
      ItemRenderHack.registerItemRenderer(TGuns.powerhammer, (new RenderMiningToolMultiTexture(new ModelPowerHammer(), 2, powerhammer_textures)).setBaseTranslation(0.15F, -0.2F, -0.027500004F).setBaseScale(1.25F).setGUIScale(0.45F).setMuzzleFx((IScreenEffect)null, 0.0F, 0.26F, -0.67F, 0.5F, 0.0F).setTransformTranslations(new float[][]{{0.0F, 0.18F, 0.09F}, {0.0F, 0.04F, 0.04F}, {0.03F, 0.01F, 0.0F}, {0.0F, 0.0F, 0.0F}, {-0.07F, -0.03F, -0.05F}}).setMuzzleFXPos3P(0.12F, -0.65F).setChargeTranslationAmount(0.125F));
      ItemRenderHack.registerItemRenderer(TGuns.grenadelauncher, (new RenderGunBaseObj(new ModelBaseBakedGrenadeLauncher(new ResourceLocation("techguns", "textures/guns/grenadelauncher.png"), new ModelResourceLocation[]{new ModelResourceLocation(TGuns.grenadelauncher.getRegistryName(), "inventory"), new ModelResourceLocation(TGuns.grenadelauncher.getRegistryName() + "_1", "inventory")}), 1, 90.0F)).setBaseTranslation(0.0F, 0.0F, 0.0F).setBaseScale(0.125F).setGUIScale(0.45F).setMuzzleFx(ScreenEffect.muzzleFlash_gun, 0.0F, 0.22F, -0.63F, 0.5F, 0.0F).setTransformTranslations(new float[][]{{0.0F, 0.19F, -0.09F}, {0.0F, 0.06F, -0.2F}, {-0.05F, 0.08F, 0.0F}, {0.0F, 0.05F, -0.09F}, {0.11F, 0.01F, -0.05F}}).setMuzzleFXPos3P(0.07F, -0.61F));
      ItemRenderHack.registerItemRenderer(TGuns.aug, (new RenderGunBase(new ModelAUG(), 2)).setBaseTranslation(0.03125F, -0.1F, 0.1F).setGUIScale(0.35F).setMuzzleFx(ScreenEffect.muzzleFlash_rifle, 0.0F, 0.19F, -1.45F, 0.75F, 0.0F).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.1F, 4.0F}).setTransformTranslations(new float[][]{{0.0F, 0.01F, -0.15F}, {0.0F, 0.0F, -0.01F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.02F, 0.0F, -0.05F}}).setMuzzleFXPos3P(0.12F, -0.87F).setMuzzleFlashJitter(0.02F, 0.02F, 5.0F, 0.1F).setScope(ScreenEffect.sniperScope).setScopeRecoilAnim(GunAnimation.scopeRecoil, new float[]{0.075F, 1.0F}));
      ItemRenderHack.registerItemRenderer(TGuns.sonicshotgun, (new RenderGunBase90(new ModelSonicShotgun(), 1)).setBaseTranslation(0.0F, -0.2F, -0.06875F).setBaseScale(1.0F).setGUIScale(0.35F).setMuzzleFx(ScreenEffect.muzzleFlashSonic, 0.0F, 0.28F, -0.98F, 0.5F, 0.0F).setTransformTranslations(new float[][]{{0.0F, 0.1F, 0.04F}, {0.0F, -0.01F, -0.03F}, {0.08F, 0.0F, 0.0F}, {0.0F, 0.05F, 0.15F}, {-0.18F, 0.0F, -0.05F}}).setMuzzleFXPos3P(0.11F, -0.81F));
      ItemRenderHack.registerItemRenderer(TGuns.chainsaw, (new RenderGunChainsaw(new ModelChainsaw(), 2)).setBaseTranslation(-0.4F, -0.2F, -0.027500004F).setBaseScale(0.95F).setGUIScale(0.45F).setTransformTranslations(new float[][]{{0.0F, -0.08F, 0.15F}, {0.0F, -0.5F, 0.04F}, {0.03F, 0.01F, 0.0F}, {0.0F, 0.0F, 0.0F}, {-0.07F, -0.03F, -0.11F}}));
      ItemRenderHack.registerItemRenderer(TGuns.scatterbeamrifle, (new RenderGunBase(new ModelLasergun2(), 1)).setBaseTranslation(0.03125F, -0.1F, 0.1F).setGUIScale(0.35F).setMuzzleFx(ScreenEffect.muzzleFlashLaser, 0.0F, 0.22F, -1.09F, 0.75F, 0.0F).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.1F, 4.0F}).setTransformTranslations(new float[][]{{0.0F, 0.04F, -0.05F}, {0.0F, 0.01F, -0.1F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -0.05F}}).setMuzzleFXPos3P(0.14F, -0.82F));
      ItemRenderHack.registerItemRenderer(TGuns.nucleardeathray, (new RenderGunBase90(new ModelNDR(), 1)).setBaseTranslation(1.0F, -0.2F, 0.0037499964F).setBaseScale(1.2F).setGUIScale(0.4F).setMuzzleFx(ScreenEffect.muzzleFlashNukeBeam, 0.0F, 0.19F, -0.91F, 0.65F, 0.0F).setTransformTranslations(new float[][]{{0.0F, 0.02F, 0.09F}, {-0.01F, 0.04F, 0.3F}, {0.11F, -0.08F, 0.0F}, {0.0F, 0.0F, 0.15F}, {-0.23F, -0.08F, -0.05F}}).setMuzzleFXPos3P(0.11F, -0.83F).setRecoilAnim(GunAnimation.swayRecoil, new float[]{0.025F, 0.75F}));
      ItemRenderHack.registerItemRenderer(TGuns.scar, (new RenderGunBase(new ModelScar(), 2)).setBaseTranslation(0.03125F, -0.1F, 0.1F).setGUIScale(0.35F).setMuzzleFx(ScreenEffect.muzzleFlash_rifle, 0.0F, 0.23F, -1.48F, 0.78F, 0.0F).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.1F, 4.0F}).setTransformTranslations(new float[][]{{0.0F, 0.04F, -0.15F}, {0.0F, 0.02F, -0.11F}, {0.05F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.02F, -0.09F, -0.05F}}).setMuzzleFXPos3P(0.14F, -1.04F).setMuzzleFlashJitter(0.02F, 0.02F, 5.0F, 0.1F).setScope(ScreenEffect.sniperScope).setScopeRecoilAnim(GunAnimation.scopeRecoil, new float[]{0.05F, 2.0F}));
      ItemRenderHack.registerItemRenderer(TGuns.vector, (new RenderGunBase(new ModelVector(), 1)).setBaseTranslation(0.03125F, -0.57F, -0.2F).setBaseScale(1.1F).setGUIScale(0.45F).setMuzzleFx(ScreenEffect.muzzleFlash_rifle, 0.0F, 0.1F, -0.72F, 0.6F, 0.0F).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.05F, 2.0F}).setTransformTranslations(new float[][]{{0.0F, -0.17F, 0.0F}, {0.0F, -0.2F, -0.04F}, {-0.08F, -0.09F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.05F, -0.17F, -0.05F}}).setMuzzleFXPos3P(0.0F, -0.53F).setMuzzleFlashJitter(0.02F, 0.02F, 5.0F, 0.1F));
      ItemRenderHack.registerItemRenderer(TGuns.mac10, (new RenderGunBase(new ModelMac10(), 1)).setBaseTranslation(0.03125F, -0.45F, -0.3F).setBaseScale(1.2F).setGUIScale(0.55F).setMuzzleFx(ScreenEffect.muzzleFlash_rifle, 0.0F, 0.23F, -0.46F, 0.5F, 0.0F).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.06F, 3.0F}).setTransformTranslations(new float[][]{{0.0F, 0.0F, -0.05F}, {0.0F, -0.1F, 0.01F}, {-0.02F, -0.02F, 0.0F}, {0.0F, 0.03F, 0.0F}, {0.0F, -0.05F, -0.05F}}).setMuzzleFXPos3P(0.07F, -0.26F).setMuzzleFlashJitter(0.02F, 0.05F, 5.0F, 0.1F));
      ItemRenderHack.registerItemRenderer(TGuns.mibgun, (new RenderGunBase(new ModelMibGun(), 1)).setBaseTranslation(0.0F, -0.56F, -0.02F).setBaseScale(1.2F).setGUIScale(0.75F).setMuzzleFx(ScreenEffect.muzzleFlashMibGun, 0.0F, 0.26F, -0.42F, 0.55F, 0.0F).setTransformTranslations(new float[][]{{0.0F, 0.1F, -0.02F}, {0.0F, -0.04F, -0.01F}, {0.02F, -0.08F, 0.0F}, {0.02F, -0.08F, 0.0F}, {-0.04F, -0.09F, 0.0F}}).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.025F, 10.0F}).setMuzzleFXPos3P(0.06F, -0.31F).setReloadAnim(GunAnimation.breechReload, new float[]{-0.15F, 55.0F}).setReloadAnim3p(GunAnimation.breechReload, new float[]{0.0F, 55.0F}));
      ItemRenderHack.registerItemRenderer(TGuns.gaussrifle, (new RenderGunBaseObj(new ModelBaseBaked(new ResourceLocation("techguns", "textures/guns/gaussrifle.png"), new ModelResourceLocation[]{new ModelResourceLocation(TGuns.gaussrifle.getRegistryName(), "inventory")}), 1, -90.0F)).setBaseTranslation(0.6F, 0.0F, -0.058750004F).setBaseScale(0.9F).setGUIScale(0.25F).setMuzzleFx(ScreenEffect.muzzleFlashSonic, 0.0F, 0.21F, -1.56F, 1.0F, 0.0F).setTransformTranslations(new float[][]{{0.0F, 0.12F, -0.1F}, {0.0F, 0.05F, -0.17F}, {0.0F, 0.06F, 0.06F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -0.05F}}).setMuzzleFXPos3P(0.09F, -1.26F).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.25F, 7.5F}).setScope(ScreenEffect.techScope, 2.125F).setScopeRecoilAnim(GunAnimation.scopeRecoil, new float[]{0.15F, 1.0F}));
      ItemRenderHack.registerItemRenderer(TGuns.guidedmissilelauncher, (new RenderGunBase90(new ModelGuidedMissileLauncher(), 1)).setBaseTranslation(-0.4F, -0.2F, 0.03125F).setGUIScale(0.35F).setChargeTranslationAmount(0.0F).setMuzzleFx(ScreenEffect.muzzleFlash_gun, 0.0F, 0.39F, -0.6F, 0.87F, 0.0F).setTransformTranslations(new float[][]{{-0.13F, 0.3F, 0.62F}, {0.0F, 0.09F, 0.28F}, {0.0F, 0.03F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -0.04F}}).setMuzzleFXPos3P(0.09F, -0.26F));
      ResourceLocation[] drill_textures = new ResourceLocation[]{new ResourceLocation("techguns", "textures/guns/miningdrill_obsidian.png"), new ResourceLocation("techguns", "textures/guns/miningdrill_carbon.png")};
      ItemRenderHack.registerItemRenderer(TGuns.miningdrill, (new RenderMiningToolMultiTexture(new ModelMiningDrill(), 2, drill_textures)).setBaseTranslation(0.0F, -0.2F, -0.03125F).setBaseScale(2.0F).setGUIScale(0.35F).setTransformTranslations(new float[][]{{0.0F, -0.03F, 0.0F}, {0.0F, -0.57F, 0.08F}, {0.01F, -0.01F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, -0.08F, -0.05F}}).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.05F, 1.0F}));
      ItemRenderHack.registerItemRenderer(TGuns.tfg, (new RenderGunBase90(new ModelTFG(), 1)).setBaseTranslation(-0.46F, -0.38F, -0.0625F).setBaseScale(1.2F).setGUIScale(0.3F).setMuzzleFx(ScreenEffect.muzzleFlashTFG, 0.0F, 0.18F, -0.87F, 0.9F, 0.0F).setTransformTranslations(new float[][]{{0.0F, -0.03F, 0.16F}, {0.0F, -0.09F, -0.26F}, {0.04F, -0.04F, 0.0F}, {0.0F, 0.0F, 0.0F}, {-0.07F, 0.0F, -0.05F}}).setMuzzleFXPos3P(0.09F, -1.14F).setChargeTranslationAmount(0.05F).setFirstPersonScale(0.45F));
      ItemRenderHack.registerItemRenderer(TGuns.shishkebap, (new RenderGunBase(new ModelShishkebap(), 1)).setBaseTranslation(0.0F, 0.0F, 0.0F).setBaseScale(1.0F).setGUIScale(0.3F).setTransformTranslations(new float[][]{{0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, -0.25F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}}).setAmbientParticleFX("ScreenTestFX").setReloadAnim(GunAnimation.breechReload, new float[]{-0.15F, 55.0F}).setReloadAnim3p(GunAnimation.breechReload, new float[]{0.0F, 55.0F}));
      ItemRenderHack.registerItemRenderer(TGuns.laserpistol, (new RenderGunBase(new ModelLaserPistol(), 1)).setBaseTranslation(0.03125F, -0.3F, -0.4F).setBaseScale(1.2F).setGUIScale(0.7F).setMuzzleFx(ScreenEffect.muzzleFlashLaser, 0.03F, 0.2F, -0.5F, 0.55F, -0.03F).setTransformTranslations(new float[][]{{0.0F, 0.09F, -0.02F}, {0.0F, -0.03F, 0.0F}, {0.02F, -0.08F, 0.0F}, {0.02F, -0.08F, 0.0F}, {0.0F, 0.0F, 0.0F}}).setRecoilAnim(GunAnimation.genericRecoil, new float[]{0.0125F, 6.0F}).setMuzzleFXPos3P(0.07F, -0.26F));
      ItemRenderHack.registerItemRenderer(TGArmors.steam_Helmet, new RenderArmorItem(new ModelSteamArmor(0), new ResourceLocation("techguns", "textures/models/armor/steam_armor.png"), EntityEquipmentSlot.HEAD));
      ItemRenderHack.registerItemRenderer(TGArmors.steam_Chestplate, new RenderArmorItem(new ModelSteamArmor(0), new ResourceLocation("techguns", "textures/models/armor/steam_armor.png"), EntityEquipmentSlot.CHEST));
      ItemRenderHack.registerItemRenderer(TGArmors.steam_Leggings, new RenderArmorItem(new ModelSteamArmor(1), new ResourceLocation("techguns", "textures/models/armor/steam_armor.png"), EntityEquipmentSlot.LEGS));
      ItemRenderHack.registerItemRenderer(TGArmors.steam_Boots, new RenderArmorItem(new ModelSteamArmor(0), new ResourceLocation("techguns", "textures/models/armor/steam_armor.png"), EntityEquipmentSlot.FEET));
      ItemRenderHack.registerItemRenderer(TGArmors.t3_power_Helmet, new RenderArmorItem(new ModelT3PowerArmor(0), new ResourceLocation("techguns", "textures/models/armor/powerarmor.png"), EntityEquipmentSlot.HEAD));
      ItemRenderHack.registerItemRenderer(TGArmors.t3_power_Chestplate, new RenderArmorItem(new ModelT3PowerArmor(0), new ResourceLocation("techguns", "textures/models/armor/powerarmor.png"), EntityEquipmentSlot.CHEST));
      ItemRenderHack.registerItemRenderer(TGArmors.t3_power_Leggings, new RenderArmorItem(new ModelT3PowerArmor(1), new ResourceLocation("techguns", "textures/models/armor/powerarmor.png"), EntityEquipmentSlot.LEGS));
      ItemRenderHack.registerItemRenderer(TGArmors.t3_power_Boots, new RenderArmorItem(new ModelT3PowerArmor(0), new ResourceLocation("techguns", "textures/models/armor/powerarmor.png"), EntityEquipmentSlot.FEET));
      ItemRenderHack.registerItemRenderer(TGArmors.t4_power_Helmet, new RenderArmorItem(new ModelT4PowerArmorMk2(0), new ResourceLocation("techguns", "textures/models/armor/powerarmor_mk2_darkgrey.png"), EntityEquipmentSlot.HEAD));
      ItemRenderHack.registerItemRenderer(TGArmors.t4_power_Chestplate, new RenderArmorItem(new ModelT4PowerArmorMk2(0), new ResourceLocation("techguns", "textures/models/armor/powerarmor_mk2_darkgrey.png"), EntityEquipmentSlot.CHEST));
      ItemRenderHack.registerItemRenderer(TGArmors.t4_power_Leggings, new RenderArmorItem(new ModelT4PowerArmorMk2(1), new ResourceLocation("techguns", "textures/models/armor/powerarmor_mk2_darkgrey.png"), EntityEquipmentSlot.LEGS));
      ItemRenderHack.registerItemRenderer(TGArmors.t4_power_Boots, new RenderArmorItem(new ModelT4PowerArmorMk2(0), new ResourceLocation("techguns", "textures/models/armor/powerarmor_mk2_darkgrey.png"), EntityEquipmentSlot.FEET));
      ItemRenderHack.registerItemRenderer(TGuns.stielgranate, (new RenderGrenade(new ModelStielgranate(), new ResourceLocation("techguns", "textures/guns/stielgranate.png"))).setBaseScale(1.0F).setGUIScale(0.8F).setBaseTranslation(-0.03125F, 0.48F, -0.0625F).setTransformTranslations(new float[][]{{0.0F, -0.06F, 0.0F}, {0.0F, -0.15F, 0.06F}, {-0.05F, -0.49F, 0.0F}, {0.0F, -0.15F, 0.0F}, {0.0F, 0.0F, -0.05F}}));
      ItemRenderHack.registerItemRenderer(TGuns.fraggrenade, (new RenderGrenade(new ModelFragGrenade(true), new ResourceLocation("techguns", "textures/guns/frag_grenade_texture.png"), 90.0F)).setBaseScale(1.25F).setGUIScale(1.35F).setBaseTranslation(-0.02F, 0.65F, -0.0425F).setTransformTranslations(new float[][]{{0.0F, -0.06F, 0.0F}, {0.0F, -0.11F, -0.01F}, {-0.05F, -0.49F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -0.05F}}));
      AdditionalSlotRenderRegistry.register(TGItems.GAS_MASK, new RenderAdditionalSlotItem(new ModelGasMask(), new ResourceLocation("techguns", "textures/armors/gasmask.png")));
      AdditionalSlotRenderRegistry.register(TGItems.GLIDER, new RenderAdditionalSlotItem(new ModelGlider(), new ResourceLocation("techguns", "textures/armors/glider.png")));
      AdditionalSlotRenderRegistry.register(TGItems.JUMPPACK, new RenderAdditionalSlotItem(new ModelJetPack(1), "techguns", "textures/armors/jetpack", 4));
      AdditionalSlotRenderRegistry.register(TGItems.JETPACK, new RenderAdditionalSlotItem(new ModelJetPack(0), "techguns", "textures/armors/jetpack", 4));
      AdditionalSlotRenderRegistry.register(TGItems.SCUBA_TANKS, new RenderAdditionalSlotItem(new ModelOxygenTanks(), new ResourceLocation("techguns", "textures/armors/oxygentanks.png")));
      AdditionalSlotRenderRegistry.register(TGItems.NIGHTVISION_GOGGLES, new RenderAdditionalSlotItem(new ModelNightVisionGoggles(), new ResourceLocation("techguns", "textures/armors/nightvisiongoggles.png")));
      AdditionalSlotRenderRegistry.register(TGItems.ANTI_GRAV_PACK, new RenderAdditionalSlotItem(new ModelAntiGravPack(), "techguns", "textures/armors/antigravpack", 5));
      AdditionalSlotRenderRegistry.register(TGItems.TACTICAL_MASK, new RenderAdditionalSlotItem(new ModelFaceMask(true), "techguns", "textures/armors/tacticalmask", 4));
      RenderAdditionalSlotSharedItem sharedItemRenderer = new RenderAdditionalSlotSharedItem();
      sharedItemRenderer.addRenderForSharedItem(TGItems.OXYGEN_MASK.func_77952_i(), new RenderAdditionalSlotItem(new ModelFaceMask(true), new ResourceLocation("techguns", "textures/armors/oxygenmask.png")));
      sharedItemRenderer.addRenderForSharedItem(TGItems.WORKING_GLOVES.func_77952_i(), new RenderAdditionalSlotItem(new ModelGloves(0.45F, false), new ModelGloves(0.45F, true), new ResourceLocation("techguns", "textures/models/armor/working_gloves.png"), new ResourceLocation("techguns", "textures/models/armor/working_gloves_slim.png")));
      AdditionalSlotRenderRegistry.register(TGItems.SHARED_ITEM, sharedItemRenderer);
      TGArmors.riot_shield.setTileEntityItemStackRenderer(new TileEntityItemRendererTGShield(new ModelRiotShield(), new ResourceLocation[]{new ResourceLocation("techguns", "textures/armors/riot_shield.png"), new ResourceLocation("techguns", "textures/armors/riot_shield_black.png"), new ResourceLocation("techguns", "textures/armors/riot_shield_darkgreen.png"), new ResourceLocation("techguns", "textures/armors/riot_shield_grey.png")}));
      TGArmors.ballistic_shield.setTileEntityItemStackRenderer(new TileEntityItemRendererTGShield(new ModelBallisticShield(), new ResourceLocation[]{new ResourceLocation("techguns", "textures/armors/ballistic_shield.png"), new ResourceLocation("techguns", "textures/armors/ballistic_shield_black.png"), new ResourceLocation("techguns", "textures/armors/ballistic_shield_green.png"), new ResourceLocation("techguns", "textures/armors/ballistic_shield_grey.png")}));
      TGArmors.advanced_shield.setTileEntityItemStackRenderer(new TileEntityItemRendererTGShield(new ModelAdvancedShield(), getTextures("textures/armors/advanced_shield", "silver", "green")));
   }

   protected void registerEntityRenderers() {
      RenderingRegistry.registerEntityRenderingHandler(GenericProjectile.class, RenderGenericProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(GenericProjectileIncendiary.class, RenderGenericProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(RocketProjectile.class, RenderRocketProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(StoneBulletProjectile.class, RenderStoneBulletProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(BioGunProjectile.class, RenderBioGunProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(FlamethrowerProjectile.class, RenderFlameThrowerProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(GrenadeProjectile.class, RenderGrenadeProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(FlyingGibs.class, RenderFlyingGibs::new);
      RenderingRegistry.registerEntityRenderingHandler(Grenade40mmProjectile.class, RenderGrenade40mmProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(LaserProjectile.class, RenderLaserProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(TeslaProjectile.class, RenderTeslaProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(NDRProjectile.class, RenderNDRProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(BlasterProjectile.class, RenderBlasterProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(GaussProjectile.class, RenderAdvancedBulletProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(AdvancedBulletProjectile.class, RenderAdvancedBulletProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(GuidedMissileProjectile.class, RenderRocketProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(DeatomizerProjectile.class, RenderInvisibleProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(SonicShotgunProjectile.class, RenderSonicShotgunProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(CyberdemonBlasterProjectile.class, RenderInvisibleProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(PowerHammerProjectile.class, RenderInvisibleProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(ChainsawProjectile.class, RenderInvisibleProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(FragGrenadeProjectile.class, RenderFragGrenadeProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(RocketProjectileNuke.class, RenderRocketProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(TFGProjectile.class, RenderInvisibleProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(GenericProjectileExplosive.class, RenderGenericProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(RocketProjectileHV.class, RenderRocketProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(GuidedMissileProjectileHV.class, RenderRocketProjectile::new);
      RenderingRegistry.registerEntityRenderingHandler(NPCTurret.class, RenderNPCTurret::new);
      RenderingRegistry.registerEntityRenderingHandler(ZombieSoldier.class, RenderZombieSoldier::new);
      RenderingRegistry.registerEntityRenderingHandler(ZombieFarmer.class, RenderZombieFarmer::new);
      RenderingRegistry.registerEntityRenderingHandler(ZombieMiner.class, RenderZombieMiner::new);
      RenderingRegistry.registerEntityRenderingHandler(ArmySoldier.class, RenderArmySoldier::new);
      RenderingRegistry.registerEntityRenderingHandler(Bandit.class, RenderBandit::new);
      RenderingRegistry.registerEntityRenderingHandler(Commando.class, RenderCommando::new);
      RenderingRegistry.registerEntityRenderingHandler(DictatorDave.class, RenderDictatorDave::new);
      RenderingRegistry.registerEntityRenderingHandler(CyberDemon.class, RenderCyberDemon::new);
      RenderingRegistry.registerEntityRenderingHandler(SkeletonSoldier.class, RenderSkeletonSoldier::new);
      RenderingRegistry.registerEntityRenderingHandler(PsychoSteve.class, RenderPsychoSteve::new);
      RenderingRegistry.registerEntityRenderingHandler(StormTrooper.class, RenderStormTrooper::new);
      RenderingRegistry.registerEntityRenderingHandler(Outcast.class, RenderOutcast::new);
      RenderingRegistry.registerEntityRenderingHandler(ZombiePigmanSoldier.class, RenderZombiePigmanSoldier::new);
      RenderingRegistry.registerEntityRenderingHandler(SuperMutantBasic.class, RenderSuperMutant::new);
      RenderingRegistry.registerEntityRenderingHandler(SuperMutantElite.class, RenderSuperMutant::new);
      RenderingRegistry.registerEntityRenderingHandler(SuperMutantHeavy.class, RenderSuperMutant::new);
      RenderingRegistry.registerEntityRenderingHandler(AttackHelicopter.class, RenderAttackHelicopter::new);
      RenderingRegistry.registerEntityRenderingHandler(AlienBug.class, RenderAlienBug::new);
      RenderingRegistry.registerEntityRenderingHandler(Ghastling.class, RenderGhastling::new);
      RenderingRegistry.registerEntityRenderingHandler(ZombiePoliceman.class, RenderZombiePoliceman::new);
   }

   public static ClientProxy get() {
      return (ClientProxy)Techguns.proxy;
   }

   public EntityPlayer getPlayerClient() {
      return Minecraft.func_71410_x().field_71439_g;
   }

   public void setGunTextures(GenericGun gun, String path, int variations) {
      gun.textures = new ArrayList();

      for(int i = 0; i < variations; ++i) {
         gun.textures.add(new ResourceLocation("techguns", path + (i != 0 ? "_" + i : "") + ".png"));
      }

   }

   public void setGunTextures(GenericGun gun, ResourceLocation path, int variations) {
      gun.textures = new ArrayList();

      for(int i = 0; i < variations; ++i) {
         gun.textures.add(new ResourceLocation(path.func_110624_b(), path.func_110623_a() + (i != 0 ? "_" + i : "") + ".png"));
      }

   }

   public void handleSoundEvent(EntityPlayer ply, int entityId, SoundEvent soundname, float volume, float pitch, boolean repeat, boolean moving, boolean gunPosition, boolean playOnOwnPlayer, TGSoundCategory soundCategory, EntityCondition condition) {
      Entity entity = null;
      if (entityId != -1) {
         entity = ply.field_70170_p.func_73045_a(entityId);
      }

      if (entity != null && (entity != ply || playOnOwnPlayer)) {
         Minecraft.func_71410_x().func_147118_V().func_147682_a(new TGSound(soundname, entity, volume, pitch, repeat, moving, gunPosition, soundCategory));
      }

   }

   public void playSoundOnEntity(Entity ent, SoundEvent soundname, float volume, float pitch, boolean repeat, boolean moving, boolean gunPosition, boolean playForOwnPlayer, TGSoundCategory category) {
      if (playForOwnPlayer || ent != this.getPlayerClient()) {
         this.playSoundOnEntity(ent, soundname, volume, pitch, repeat, moving, gunPosition, category);
      }

   }

   public void playSoundOnEntity(Entity ent, SoundEvent soundname, float volume, float pitch, boolean repeat, boolean moving, boolean gunPosition, boolean playForOwnPlayer, TGSoundCategory category, EntityCondition condition) {
      if (playForOwnPlayer || ent != this.getPlayerClient()) {
         this.playSoundOnEntity(ent, soundname, volume, pitch, repeat, moving, gunPosition, category, condition);
      }

   }

   public void playSoundOnEntity(Entity ent, SoundEvent soundname, float volume, float pitch, boolean repeat, boolean moving, boolean gunPosition, TGSoundCategory category) {
      EntityPlayerSP self = Minecraft.func_71410_x().field_71439_g;
      if (self != null && ent == self) {
         gunPosition = false;
      }

      Minecraft.func_71410_x().func_147118_V().func_147682_a(new TGSound(soundname, ent, volume, pitch, repeat, moving, gunPosition, category));
   }

   public void playSoundOnEntity(Entity ent, SoundEvent soundname, float volume, float pitch, boolean repeat, boolean moving, boolean gunPosition, TGSoundCategory category, EntityCondition condition) {
      EntityPlayerSP self = Minecraft.func_71410_x().field_71439_g;
      if (self != null && ent == self) {
         gunPosition = false;
      }

      Minecraft.func_71410_x().func_147118_V().func_147682_a(new TGSound(soundname, ent, volume, pitch, repeat, moving, gunPosition, category, condition));
   }

   public void playSoundOnPosition(SoundEvent soundname, float posx, float posy, float posz, float volume, float pitch, boolean repeat, TGSoundCategory soundCategory) {
      Minecraft.func_71410_x().func_147118_V().func_147682_a(new TGSound(soundname, posx, posy, posz, volume, pitch, repeat, soundCategory));
   }

   public void createFX(String name, World world, double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
      List systems = TGFX.createFX(world, name, posX, posY, posZ, motionX, motionY, motionZ);
      if (systems != null) {
         systems.forEach((s) -> this.particleManager.addEffect(s));
      }

   }

   public void createFX(String name, World world, double posX, double posY, double posZ, double motionX, double motionY, double motionZ, float pitch, float yaw) {
      List systems = TGFX.createFX(world, name, posX, posY, posZ, motionX, motionY, motionZ);
      if (systems != null) {
         for(TGParticleSystem s : systems) {
            s.rotationPitch = pitch;
            s.rotationYaw = yaw;
            this.particleManager.addEffect(s);
         }
      }

   }

   public void createFX(String name, World world, double posX, double posY, double posZ, double motionX, double motionY, double motionZ, float scale) {
      List systems = TGFX.createFX(world, name, posX, posY, posZ, motionX, motionY, motionZ);
      if (systems != null) {
         for(TGParticleSystem s : systems) {
            s.scale = scale;
            this.particleManager.addEffect(s);
         }
      }

   }

   public void createFXOnEntity(String name, Entity ent) {
      List systems = TGFX.createFXOnEntity(ent, name);
      if (systems != null) {
         systems.forEach((s) -> {
            s.condition = EntityCondition.ENTITY_ALIVE;
            this.particleManager.addEffect(s);
         });
      }

   }

   public void createFXOnEntity(String name, Entity ent, float scale) {
      List systems = TGFX.createFXOnEntity(ent, name);
      if (systems != null) {
         for(TGParticleSystem s : systems) {
            s.scale = scale;
            s.condition = EntityCondition.ENTITY_ALIVE;
            this.particleManager.addEffect(s);
         }
      }

   }

   public void createFXOnEntityWithOffset(String name, Entity ent, float offsetX, float offsetY, float offsetZ, boolean attachToHead, EntityCondition condition) {
      List systems = TGFX.createFXOnEntity(ent, name);
      if (systems != null) {
         for(TGParticleSystem s : systems) {
            s.entityOffset = new Vec3d((double)offsetX, (double)offsetY, (double)offsetZ);
            s.attachToHead = attachToHead;
            s.condition = condition;
            this.particleManager.addEffect(s);
         }
      }

   }

   public void setHasStepassist(boolean value) {
      this.hasStepassist = value;
   }

   public void setHasNightvision(boolean value) {
      this.hasNightvision = value;
   }

   public boolean getHasStepassist() {
      return this.hasStepassist;
   }

   public boolean getHasNightvision() {
      return this.hasNightvision;
   }

   public void setFlySpeed(float value) {
      EntityPlayer ply = this.getPlayerClient();
      ply.field_71075_bZ.func_75092_a(value);
   }

   public void registerCapabilities() {
      super.registerCapabilities();
      CapabilityManager.INSTANCE.register(TGDeathTypeCap.class, new TGDeathTypeCapStorage(), () -> new TGDeathTypeCap((EntityLivingBase)null));
   }

   public void setEntityDeathType(EntityLivingBase entity, EntityDeathUtils.DeathType deathtype) {
      TGDeathTypeCap cap = TGDeathTypeCap.get(entity);
      cap.setDeathType(deathtype);
   }

   public EntityDeathUtils.DeathType getEntityDeathType(EntityLivingBase entity) {
      return TGDeathTypeCap.get(entity).getDeathType();
   }

   /** @deprecated */
   @Deprecated
   public boolean hasDeathType(EntityLivingBase entity) {
      return TGDeathTypeCap.get(entity).getDeathType() == DeathType.DEFAULT;
   }

   /** @deprecated */
   @Deprecated
   public void clearEntityDeathType(EntityLivingBase entity) {
      this.setEntityDeathType(entity, DeathType.DEFAULT);
   }

   public boolean isClientPlayerAndIn1stPerson(EntityLivingBase ent) {
      return ent == this.getPlayerClient() && Minecraft.func_71410_x().field_71474_y.field_74320_O == 0;
   }

   public void createLightPulse(double x, double y, double z, int lifetime, float rad_start, float rad_end, float r, float g, float b) {
      if (this.lightPulsesEnabled) {
         this.activeLightPulses.add(new LightPulse(x, y, z, lifetime, rad_start, rad_end, r, g, b));
      }

   }

   public void createLightPulse(double x, double y, double z, int fadeIn, int fadeOut, float rad_large, float rad_small, float r, float g, float b) {
      if (this.lightPulsesEnabled) {
         this.activeLightPulses.add(new LightPulse(x, y, z, fadeIn, fadeOut, rad_large, rad_small, r, g, b));
      }

   }

   public String resolvePlayerNameFromUUID(UUID uuid) {
      String name = UsernameCache.getLastKnownUsername(uuid);
      return name != null ? name : "UNKNOW_PLAYERNAME";
   }

   public void registerFluidModelsForFluidBlock(Block b) {
      if (!(b instanceof IFluidBlock)) {
         System.out.println("Tried to register " + b + " as Fluid, but block is no IFluidBlock");
      } else {
         IFluidBlock f = (IFluidBlock)b;
         Item item = Item.func_150898_a(b);
         if (item == Items.field_190931_a) {
            System.out.println("No item found for IFluidBlock " + b);
         } else {
            ModelBakery.registerItemVariants(item, new ResourceLocation[0]);
            ModelResourceLocation modelResourceLocation = new ModelResourceLocation(new ResourceLocation("techguns", "fluid"), f.getFluid().getName());
            ModelLoader.setCustomMeshDefinition(item, new ClientProxy.1(this, modelResourceLocation));
            ModelLoader.setCustomStateMapper(b, new ClientProxy.2(this, modelResourceLocation));
         }
      }
   }

   public void clearItemParticleSystemsHand(EntityLivingBase elb, EnumHand hand) {
      if (elb instanceof EntityPlayer) {
         TGExtendedPlayerClient props = TGExtendedPlayerClient.get((EntityPlayer)elb);
         props.clearAttachedSystemsHand(hand);
      } else if (elb instanceof INPCTechgunsShooter) {
         TGShooterValues props = TGShooterValues.get(elb);
         props.clearAttachedSystemsHand(hand);
      }

   }

   public void handlePlayerGliding(EntityPlayer player) {
      if (player.field_70170_p.field_72995_K) {
         TGExtendedPlayerClient props = TGExtendedPlayerClient.get(player);
         if (props.isGliding) {
            if (props.gliderLoop == null) {
               props.gliderLoop = new TGSound(TGSounds.GLIDER_LOOP, player, 0.75F, 1.0F, true, true, false, TGSoundCategory.PLAYER_EFFECT);
               Minecraft.func_71410_x().func_147118_V().func_147682_a(props.gliderLoop);
            }
         } else if (props.gliderLoop != null) {
            props.gliderLoop.setDonePlaying();
            props.gliderLoop = null;
         }
      }

   }

   protected static ResourceLocation[] getTextures(String name, String... suffixes) {
      ResourceLocation[] tex = new ResourceLocation[suffixes.length + 1];
      tex[0] = new ResourceLocation("techguns", name + ".png");

      for(int i = 1; i <= suffixes.length; ++i) {
         tex[i] = new ResourceLocation("techguns", name + "_" + suffixes[i - 1] + ".png");
      }

      return tex;
   }
}
