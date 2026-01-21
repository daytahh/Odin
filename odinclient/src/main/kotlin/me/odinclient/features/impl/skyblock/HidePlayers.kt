package me.odinclient.features.impl.skyblock

import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.NumberSetting
import me.odinmain.features.Module
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.client.event.RenderPlayerEvent
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object HidePlayers : Module(
    name = "Hide Players",
    description = "Hides players in your vicinity."
) {
    private val hideAll by BooleanSetting("Hide all", false, desc = "Hides all players, regardless of distance.")
    private val distance by NumberSetting("distance", 3.0f, 0.0, 32.0, .5, "The number of blocks away to hide players.").withDependency { !hideAll }
    private val clickThrough by BooleanSetting("Click Through", false, desc = "Allows clicking through players.")
    private val onlyDevs by BooleanSetting("only at Devs", false, desc = "Only hides players when standing at ss or fourth device.")

    @SubscribeEvent
    fun onRenderEntity(event: RenderPlayerEvent.Pre) {
        if (mc.isSingleplayer || clickThrough || event.entity == mc.thePlayer || event.entity.uniqueID.version() == 2) return
        if (onlyDevs && !((mc.thePlayer.getDistance(108.63, 120.0, 94.0) <= 1.8 || mc.thePlayer.getDistance(63.5, 127.0, 35.5) <= 1.8) && DungeonUtils.getF7Phase() == M7Phases.P3)) return
        if (hideAll || event.entity.getDistanceToEntity(mc.thePlayer) <= distance) event.isCanceled = true
    }

    @SubscribeEvent
    fun onPosUpdate(event: LivingEvent.LivingUpdateEvent) {
        if (mc.isSingleplayer || !clickThrough || event.entity !is EntityOtherPlayerMP || event.entity.uniqueID.version() == 2) return
        if (onlyDevs && !((mc.thePlayer.getDistance(108.63, 120.0, 94.0) <= 1.8 || mc.thePlayer.getDistance(63.5, 127.0, 35.5) <= 1.8) && DungeonUtils.getF7Phase() == M7Phases.P3)) return
        if (!hideAll && event.entity.getDistanceToEntity(mc.thePlayer) > distance) return
        event.entity.posX = 9999999.0
        event.isCanceled = true
    }
}