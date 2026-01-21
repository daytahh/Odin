package me.odinmain.features.impl.render

import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.ColorSetting
import me.odinmain.clickgui.settings.impl.NumberSetting
import me.odinmain.clickgui.settings.impl.SelectorSetting
import me.odinmain.features.Module
import me.odinmain.utils.render.Color.Companion.withAlpha
import me.odinmain.utils.render.Colors
import me.odinmain.utils.render.HighlightRenderer
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.getBlockAt
import me.odinmain.utils.skyblock.usingEtherWarp
import net.minecraft.block.material.Material
import net.minecraft.util.MovingObjectPosition.MovingObjectType
import net.minecraftforge.client.event.DrawBlockHighlightEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object BlockOverlay : Module(
    name = "Block Overlay",
    description = "Lets you customize the vanilla block overlay.",
) {
    private val blockOverlayToggle by BooleanSetting("Block Overlay", true, desc = "Master toggle for Block Overlay feature.")

    private val style by SelectorSetting("Block Style", Renderer.DEFAULT_STYLE, Renderer.styles, desc = Renderer.STYLE_DESCRIPTION).withDependency { blockOverlayToggle }
    private val color by ColorSetting("Block Color", Colors.BLACK.withAlpha(0.4f), allowAlpha = true, desc = "The color of the box.").withDependency { blockOverlayToggle }
    private val lineWidth by NumberSetting("Block Line Width", 2f, 0.1f, 10f, 0.1f, desc = "The width of the box's lines.").withDependency { blockOverlayToggle }
    private val depthCheck by BooleanSetting("Depth check", true, desc = "Boxes show through walls.").withDependency { blockOverlayToggle }
    private val lineSmoothing by BooleanSetting("Line Smoothing", true, desc = "Makes the lines smoother.").withDependency { blockOverlayToggle && (style == 1 || style == 2) }
    private val disableWhenEtherwarping by BooleanSetting("Disable on Etherwarp", true, desc = "Disables the block overlay when etherwarping.").withDependency { blockOverlayToggle }

    @SubscribeEvent
    fun onRenderBlockOverlay(event: DrawBlockHighlightEvent) {
        if (event.target.typeOfHit != MovingObjectType.BLOCK || mc.gameSettings?.thirdPersonView != 0 || (disableWhenEtherwarping && mc.thePlayer.usingEtherWarp)) return
        event.isCanceled = true

        if (getBlockAt(event.target.blockPos).material === Material.air || event.target.blockPos !in mc.theWorld.worldBorder) return

        Renderer.drawStyledBlock(event.target.blockPos, color, style, lineWidth, depthCheck, lineSmoothing)
    }
}