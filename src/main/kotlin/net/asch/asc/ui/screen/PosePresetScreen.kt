package net.asch.asc.ui.screen

import net.asch.asc.ui.component.PresetPanelWidget
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class PosePresetScreen : Screen(Text.translatable("asc.screen.title")) {
    override fun init() {
        val panel = PresetPanelWidget(width, height)
        addDrawable(panel.backgroundDrawable)
        addDrawableChild(panel.listWidget)
        addDrawableChild(panel.previewButton)
    }
}
