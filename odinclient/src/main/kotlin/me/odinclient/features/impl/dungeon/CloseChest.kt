package me.odinclient.features.impl.dungeon

import me.odinmain.events.impl.PacketEvent
import me.odinmain.features.Module
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inDungeons
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CloseChest : Module(
    name = "Close Chest",
    description = "Allows you to instantly close chests with any key or automatically."
) {
    @SubscribeEvent
    fun onOpenWindow(event: PacketEvent.Receive) {
        val packet = event.packet as? S2DPacketOpenWindow ?: return
        if (!inDungeons || !packet.windowTitle.unformattedText.noControlCodes.equalsOneOf("Chest", "Large Chest")) return
        mc.netHandler.networkManager.sendPacket(C0DPacketCloseWindow(packet.windowId))
        event.isCanceled = true
    }
}