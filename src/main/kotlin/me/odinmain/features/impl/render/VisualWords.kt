package me.odinmain.features.impl.render

import me.odinmain.clickgui.settings.impl.MapSetting
import me.odinmain.features.Module

object VisualWords : Module(
    name = "Visual Words",
    description = "Replaces words in the world with other words. (/visualwords)"
) {
    val wordsMap by MapSetting("wordsMap", mutableMapOf<String, String>())

    @JvmStatic
    fun replaceText(text: String?): String? {
        if (!enabled || text == null) return text
        var replacedText = text
        for (actualText in wordsMap.keys) {
            replacedText = wordsMap[actualText]?.let { replacedText?.replace(actualText, it) }
        }
        return replacedText
    }
}