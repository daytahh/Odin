package me.odinclient.features.impl.dungeon

import me.odinmain.clickgui.settings.impl.*
import me.odinmain.features.Module
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.WITHER_ESSENCE_ID
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inDungeons
import me.odinmain.utils.skyblock.getBlockAt
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntitySkull
import net.minecraft.util.BlockPos
import org.lwjgl.input.Keyboard

object GhostBlocks : Module(name = "Ghost Blocks", description = "Creates ghost blocks by key") {
    private val gkey by KeybindSetting("Ghost block Keybind", Keyboard.KEY_NONE, "Makes blocks you're looking at disappear.")
    private val speed by NumberSetting("Speed", 50L, 0.0, 300.0, 10.0, unit = "ms", desc = "The speed at which ghost blocks are created.")
    private val skulls by BooleanSetting("Ghost Skulls", true, desc = "If enabled skulls will also be turned into ghost blocks.")
    private val range by NumberSetting("Range", 8.0, 4.5, 80.0, 0.5, desc = "Maximum range at which ghost blocks will be created.")
    private val onlyDungeon by BooleanSetting("Only In Dungeon", false, desc = "Will only work inside of a dungeon.")


    private val blacklist = arrayOf(Blocks.stone_button, Blocks.chest, Blocks.trapped_chest, Blocks.lever)

    init {
        execute({ speed }) {
            if (mc.currentScreen != null || (onlyDungeon && !inDungeons)) return@execute
            if (!gkey.isDown()) return@execute

            toAir(mc.thePlayer?.rayTrace(range, 1f)?.blockPos ?: return@execute)
        }
    }

    private fun toAir(blockPos: BlockPos) = getBlockAt(blockPos).let { block ->
        if (block !in blacklist && (block !== Blocks.skull || (skulls && (mc.theWorld?.getTileEntity(blockPos) as? TileEntitySkull)
                    ?.playerProfile?.id?.toString() != WITHER_ESSENCE_ID))) mc.theWorld?.setBlockToAir(blockPos)
    }
}