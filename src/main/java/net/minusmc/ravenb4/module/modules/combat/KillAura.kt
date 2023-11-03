package net.minusmc.ravenb4.module.modules.combat

import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minusmc.ravenb4.module.Module
import net.minusmc.ravenb4.module.ModuleCategory
import net.minusmc.ravenb4.setting.impl.SliderSetting

class KillAura : Module("KillAura", ModuleCategory.COMBAT) {
    private val mc = Minecraft.getMinecraft()
    private val range: SliderSetting = SliderSetting("Range", 3.2, 1.0, 6.0, 0.1)

    init {
        addSetting(range)
    }

    override fun onUpdate() {
        val playerEntities = mc.theWorld.playerEntities
        for (entity in playerEntities) {
            if (entity != mc.thePlayer && mc.thePlayer.getDistanceToEntity(entity) <= range.getInput()) {
                mc.playerController.attackEntity(mc.thePlayer, entity)
            }
        }
    }
}
