package me.odinmain.features.impl.skyblock

import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.SelectorSetting
import me.odinmain.events.impl.GuiEvent
import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.features.Module
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Colors
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.toFixed
import me.odinmain.utils.ui.drawStringWidth
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object InvincibilityTimer : Module(
    name = "Invincibility Timer",
    description = "Provides visual information about your invincibility items."
) {
    private val invincibilityAnnounce by BooleanSetting("Announce Invincibility", true, desc = "Announces when you get invincibility.")
    private val showCooldown by BooleanSetting("Durability Cooldown", true, desc = "Shows the durability of the mask in the inventory as a durability bar.")
    private val showWhen by SelectorSetting("Show", "Always", listOf("Always", "Any", "When Active", "On Cooldown"), "Controls when invincibility items are shown.")

    private val showSpirit by BooleanSetting("Show Spirit Mask", true, desc = "Shows the Spirit Mask in the HUD.")
    private val showBonzo by BooleanSetting("Show Bonzo Mask", true, desc = "Shows the Bonzo Mask in the HUD.")
    private val showPhoenix by BooleanSetting("Show Phoenix Pet", true, desc = "Shows the Phoenix Pet in the HUD.")

    private val hud by HUD("Invincibility HUD", "Shows the invincibility time in the HUD.") { example ->
        if ((!DungeonUtils.inDungeons && !example) || (showOnlyInBoss && !DungeonUtils.inBoss)) return@HUD 0f to 0f
        var width = 0f

        val visibleTypes = InvincibilityType.entries.filter { type ->
            when (type) {
                InvincibilityType.SPIRIT -> showSpirit
                InvincibilityType.BONZO -> showBonzo
                InvincibilityType.PHOENIX -> showPhoenix
            } && (when (showWhen) {
                0 -> true
                1 -> type.activeTime > 0 || type.currentCooldown > 0
                2 -> type.activeTime > 0
                3 -> type.currentCooldown > 0
                else -> true
            } || example)
        }.ifEmpty { return@HUD 0f to 0f }

        val head = mc.thePlayer?.getCurrentArmor(3)?.skyblockID
        visibleTypes.forEachIndexed { index, type ->
            val y = index * 9f

            val color = if (type == InvincibilityType.BONZO && head?.equalsOneOf("BONZO_MASK", "STARRED_BONZO_MASK") == true ||
                type == InvincibilityType.SPIRIT && head?.equalsOneOf("SPIRIT_MASK", "STARRED_SPIRIT_MASK") == true ||
                type == InvincibilityType.PHOENIX && hasphoenix) Colors.MINECRAFT_YELLOW
            else if (type.activeTime == 0 && type.currentCooldown == 0) Colors.MINECRAFT_GREEN else Colors.MINECRAFT_RED

            drawStringWidth(text = when {
                type.activeTime > 0 -> "${type.displayname} §8(§6${(type.activeTime / 20f).toFixed()}s§8)"
                type.currentCooldown > 0 -> "${type.displayname} §8(§7${(type.currentCooldown / 20f).toFixed()}s§8)"
                else -> type.displayname }, 0, y, color
            ).let { if (it > width) width = it }
        }

        width to visibleTypes.size * 9
    }
    private val showOnlyInBoss by BooleanSetting("Show In Boss", false, desc = "Only shows invincibility timers during dungeon boss fights.")

    private val phoenixautopet = Regex("^Autopet equipped your \\[Lvl \\d+](?: \\[\\d+✦])? ([^✦!]+)(?: ✦)?! VIEW RULE$")
    private val phoenixequip = Regex("^You summoned your ([^✦!]+)( ✦)?!$")
    private var hasphoenix = false

    init {
        onWorldLoad {
            InvincibilityType.entries.forEach { it.reset() }
        }

        onMessage(Regex("^(?:Second Wind Activated! )?Your ⚚? ?(.+) (?:Mask|Pet) saved (?:your life|you from certain death)!$")) {
            InvincibilityType.entries.firstOrNull { type -> it.value.matches(type.regex) }?.let { type ->
                if (invincibilityAnnounce) partyMessage("${type.name.lowercase()} procced")
                type.proc()
            }
        }

        onMessage(phoenixautopet) { hasphoenix = it.groupValues[1] == "Phoenix" }
        onMessage(phoenixequip) { hasphoenix = it.groupValues[1] == "Phoenix" }
    }

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent) {
        InvincibilityType.entries.forEach { it.tick() }
    }

    @SubscribeEvent
    fun onRenderSlotOverlay(event: GuiEvent.DrawSlotOverlay) {
        if (!LocationUtils.isInSkyblock || !showCooldown) return

        val durability = when (event.stack.skyblockID) {
            "BONZO_MASK", "STARRED_BONZO_MASK" -> InvincibilityType.BONZO.currentCooldown.toDouble() / InvincibilityType.BONZO.maxCooldownTime
            "SPIRIT_MASK", "STARRED_SPIRIT_MASK" -> InvincibilityType.SPIRIT.currentCooldown.toDouble() / InvincibilityType.SPIRIT.maxCooldownTime
            else -> return
        }.takeIf { it < 1.0 } ?: return

        RenderUtils.renderDurabilityBar(event.x ?: return, event.y ?: return, durability)
    }

    enum class InvincibilityType(val regex: Regex, private val maxInvincibilityTime: Int, val maxCooldownTime: Int, val color: Color, val displayname: String) {
        BONZO(Regex("^Your (?:. )?Bonzo's Mask saved your life!$"), 60, 3600, Colors.MINECRAFT_BLUE, "Bonzo"),
        SPIRIT(Regex("^Second Wind Activated! Your Spirit Mask saved your life!$"), 30, 600, Colors.MINECRAFT_DARK_PURPLE, "Spirit"),
        PHOENIX(Regex("^Your Phoenix Pet saved you from certain death!$"), 80, 1200, Colors.MINECRAFT_DARK_RED, "Phoenix");

        var activeTime: Int = 0
            private set
        var currentCooldown: Int = 0
            private set

        fun proc() {
            activeTime = maxInvincibilityTime
            currentCooldown = maxCooldownTime
        }

        fun tick() {
            if (currentCooldown > 0) currentCooldown--
            if (activeTime > 0)      activeTime--
        }

        fun reset() {
            currentCooldown = 0
            activeTime = 0
        }
    }
}