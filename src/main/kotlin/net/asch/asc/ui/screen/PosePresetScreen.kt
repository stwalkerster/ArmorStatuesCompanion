package net.asch.asc.ui.screen

import net.asch.asc.ui.component.PresetPanelWidget
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class PosePresetScreen : Screen(Component.translatable("asc.screen.title")) {
    override fun init() {
        val panel = PresetPanelWidget(width, height)
        addRenderableOnly(panel.backgroundDrawable)
        addRenderableWidget(panel.listWidget)
        addRenderableWidget(panel.previewButton)
    }
}
