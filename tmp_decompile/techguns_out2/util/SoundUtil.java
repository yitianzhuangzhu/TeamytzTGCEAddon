package techguns.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import techguns.TGPackets;
import techguns.Techguns;
import techguns.client.ClientProxy;
import techguns.client.audio.TGSoundCategory;
import techguns.packets.PacketPlaySound;

public class SoundUtil {
   public static void playSoundOnEntityGunPosition(World world, Entity entity, SoundEvent soundname, float volume, float pitch, boolean repeat, boolean moving, boolean playOnOwnPlayer, TGSoundCategory category, EntityCondition condition) {
      if (!world.field_72995_K) {
         TGPackets.wrapper.sendToAllAround(new PacketPlaySound(soundname, entity, volume, pitch, repeat, moving, true, playOnOwnPlayer, category, condition), new NetworkRegistry.TargetPoint(entity.field_71093_bK, entity.field_70165_t, entity.field_70163_u, entity.field_70161_v, 100.0D));
      } else {
         Techguns.proxy.playSoundOnEntity(entity, soundname, volume, pitch, repeat, moving, true, true, category, condition);
      }

   }

   public static void playSoundOnEntityGunPosition(World world, Entity entity, SoundEvent soundname, float volume, float pitch, boolean repeat, boolean moving, boolean playOnOwnPlayer, TGSoundCategory category) {
      if (!world.field_72995_K) {
         TGPackets.wrapper.sendToAllAround(new PacketPlaySound(soundname, entity, volume, pitch, repeat, moving, true, playOnOwnPlayer, category), new NetworkRegistry.TargetPoint(entity.field_71093_bK, entity.field_70165_t, entity.field_70163_u, entity.field_70161_v, 100.0D));
      } else {
         Techguns.proxy.playSoundOnEntity(entity, soundname, volume, pitch, repeat, moving, true, true, category);
      }

   }

   public static void playSoundOnEntityGunPosition(World world, Entity entity, SoundEvent soundname, float volume, float pitch, boolean repeat, boolean moving, TGSoundCategory category) {
      playSoundOnEntityGunPosition(world, entity, soundname, volume, pitch, repeat, moving, false, category);
   }

   public static void playReloadSoundOnEntity(World world, Entity entity, SoundEvent soundname, float volume, float pitch, boolean repeat, boolean moving, TGSoundCategory category) {
      if (!world.field_72995_K) {
         TGPackets.wrapper.sendToAllAround(new PacketPlaySound(soundname, entity, volume, pitch, repeat, moving, true, category), new NetworkRegistry.TargetPoint(entity.field_71093_bK, entity.field_70165_t, entity.field_70163_u, entity.field_70161_v, 100.0D));
      } else {
         ClientProxy cp = ClientProxy.get();
         if (cp.lastReloadsoundPlayed - System.currentTimeMillis() < -500L) {
            cp.lastReloadsoundPlayed = System.currentTimeMillis();
            Techguns.proxy.playSoundOnEntity(entity, soundname, volume, pitch, repeat, moving, true, category);
         }
      }

   }

   public static void playSoundAtEntityPos(World world, Entity entity, SoundEvent soundname, float volume, float pitch, boolean repeat, TGSoundCategory category) {
      if (!world.field_72995_K) {
         TGPackets.wrapper.sendToAllAround(new PacketPlaySound(soundname, entity, volume, pitch, repeat, false, category), new NetworkRegistry.TargetPoint(entity.field_71093_bK, entity.field_70165_t, entity.field_70163_u, entity.field_70161_v, 100.0D));
      } else {
         Techguns.proxy.playSoundOnEntity(entity, soundname, volume, pitch, repeat, false, false, category);
      }

   }
}
