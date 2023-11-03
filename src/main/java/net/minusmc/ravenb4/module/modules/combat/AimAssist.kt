package net.minusmc.ravenb4.module.modules.combat

import net.minecraft.entity.Entity
import net.minusmc.ravenb4.module.Module
import net.minusmc.ravenb4.module.ModuleCategory
import net.minusmc.ravenb4.setting.impl.SliderSetting
import net.minusmc.ravenb4.setting.impl.TickSetting
import net.minecraft.client.network.NetworkPlayerInfo
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class AimAssist : Module("AimAssist", ModuleCategory.COMBAT) {
    private var speed: SliderSetting? = null
    private var compliment: SliderSetting? = null
    private var fov: SliderSetting? = null
    private var distance: SliderSetting? = null
    private var clickAim: TickSetting? = null
    private var weaponOnly: TickSetting? = null
    private var aimInvis: TickSetting? = null
    private var breakBlocks: TickSetting? = null
    private var blatantMode: TickSetting? = null
    private var ignoreFriends: TickSetting? = null
    private var friends = ArrayList<Entity>()

    init {
        addSetting(speed = SliderSetting("Speed 1", 45.0, 5.0, 100.0, 1.0))
        addSetting(compliment = SliderSetting("Speed 2", 15.0, 2.0, 97.0, 1.0))
        addSetting(fov = SliderSetting("FOV", 90.0, 15.0, 360.0, 1.0))
        addSetting(distance = SliderSetting("Distance", 4.5, 1.0, 10.0, 0.5))
        addSetting(clickAim = TickSetting("Click aim", true))
        addSetting(breakBlocks = TickSetting("Break blocks", true))
        addSetting(ignoreFriends = TickSetting("Ignore Friends", true))
        addSetting(weaponOnly = TickSetting("Weapon only", false))
        addSetting(aimInvis = TickSetting("Aim invis", false))
        addSetting(blatantMode = TickSetting("Blatant mode", false))
    }

   fun update() {
      if(!Utils.Client.currentScreenMinecraft()){
         return;
      }
      if(!Utils.Player.isPlayerInGame()) return;

         if (breakBlocks.isToggled() && mc.objectMouseOver != null) {
            BlockPos p = mc.objectMouseOver.getBlockPos();
            if (p != null) {
               Block bl = mc.theWorld.getBlockState(p).getBlock();
               if (bl != Blocks.air && !(bl instanceof BlockLiquid) && bl instanceof  Block) {
                  return;
               }
            }
         }


         if (!weaponOnly.isToggled() || Utils.Player.isPlayerHoldingWeapon()) {

            Module autoClicker = Raven.moduleManager.getModuleByClazz(RightClicker.class);
            //what if player clicking but mouse not down ????
            if ((clickAim.isToggled() && Utils.Client.autoClickerClicking()) || (Mouse.isButtonDown(0) && autoClicker != null && !autoClicker.isEnabled()) || !clickAim.isToggled()) {
               Entity en = this.getEnemy();
               if (en != null) {
                  if (Raven.debugger) {
                     Utils.Player.sendMessageToSelf(this.getName() + " &e" + en.getName());
                  }

                  if (blatantMode.isToggled()) {
                     Utils.Player.aim(en, 0.0F, false);
                  } else {
                     double n = Utils.Player.fovFromEntity(en);
                     if (n > 1.0D || n < -1.0D) {
                        double complimentSpeed = n*(ThreadLocalRandom.current().nextDouble(compliment.getInput() - 1.47328, compliment.getInput() + 2.48293)/100);
                        double val2 = complimentSpeed + ThreadLocalRandom.current().nextDouble(speed.getInput() - 4.723847, speed.getInput());
                        float val = (float)(-(complimentSpeed + n / (101.0D - (float)ThreadLocalRandom.current().nextDouble(speed.getInput() - 4.723847, speed.getInput()))));
                        mc.thePlayer.rotationYaw += val;
                     }
                  }
               }

            }
         }
      }


    companion object {
    fun isAFriend(entity: Entity): Boolean {
        if(entity == mc.thePlayer) return true;
        for (wut in friends){
            if (wut == entity)
                return true;
        }
        try {
            val bruhentity = entity as EntityPlayer
            if(mc.thePlayer.isOnSameTeam(entity as EntityLivingBase) || mc.thePlayer.displayName.unformattedText.startsWith(bruhentity.displayName.unformattedText.substring(0, 2))) return true;
        } catch (fhwhfhwe: Exception) {
            if(Raven.debugger) {
                Utils.Player.sendMessageToSelf(fhwhfhwe.message);
            }
        }
        return false;
    }

    fun getEnemy(): Entity? {
        val fov = AimAssist.fov.getInput().toInt()
        for (en in mc.theWorld.playerEntities) {
            if (ignoreFriends.isToggled() && isAFriend(en)) continue
            if(en == mc.thePlayer) continue
            if(en.isDead) continue
            if(!aimInvis.isToggled() && en.isInvisible) continue
            if(mc.thePlayer.getDistanceToEntity(en) > distance.getInput()) continue
            if(AntiBot.bot(en)) continue
            if(!blatantMode.isToggled() && !Utils.Player.fov(en, fov.toFloat())) continue
            return en
        }
        return null
    }

    fun addFriend(entityPlayer: Entity) {
        friends.add(entityPlayer)
    }

    fun addFriend(name: String): Boolean {
        var found = false
        for (entity in mc.theWorld.loadedEntityList) {
            if (entity.name.equals(name, ignoreCase = true) || entity.customNameTag.equals(name, ignoreCase = true)) {
                if(!isAFriend(entity)) {
                    addFriend(entity)
                    found = true
                }
            }
        }
        return found
    }

    fun removeFriend(name: String): Boolean {
        var removed = false
        var found = false
        for (networkPlayerInfo in ArrayList(mc.netHandler.playerInfoMap)) {
            val entity = mc.theWorld.getPlayerEntityByName(networkPlayerInfo.displayName.unformattedText)
            if (entity.name.equals(name, ignoreCase = true) || entity.customNameTag.equals(name, ignoreCase = true)) {
                removed = removeFriend(entity)
                found = true
            }
        }
        return found && removed
    }

    fun removeFriend(entityPlayer: Entity): Boolean {
        try {
            friends.remove(entityPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun getFriends(): List<Entity> {
        return friends
    }
}
}