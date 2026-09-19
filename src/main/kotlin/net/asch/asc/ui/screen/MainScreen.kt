package net.asch.asc.ui.screen

import net.asch.asc.as_datapack.ArmorStatuesHelper
import net.asch.asc.as_datapack.triggers.Utility
import net.asch.asc.ui.component.PresetPanelWidget
import net.asch.asc.ui.component.ToolbarWidget
import net.asch.asc.ui.component.ToolboxBuilder
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.DirectionalLayoutWidget
import net.minecraft.text.Text

class MainScreen : Screen(Text.translatable("asc.screen.title")) {
    companion object {
        private const val GAP: Int = 1
        private var activeToolbox: String? = null
    }

    private val mainToolbar = ToolbarWidget()
    private val utilityToolbar = ToolbarWidget()
    private var showingPresets = false

    init {
        buildMainToolbar(mainToolbar)
        buildUtilityToolbar(utilityToolbar)
    }

    override fun init() {
        if (showingPresets) {
            val panel = PresetPanelWidget(width, height)
            addDrawable(panel.backgroundDrawable)
            addDrawableChild(panel.listWidget)
            addDrawableChild(panel.previewButton)
            return
        }

        mainToolbar.widget.setX(GAP)
        mainToolbar.widget.setY(GAP)
        mainToolbar.widget.refreshPositions()
        mainToolbar.widget.forEachChild(this::addDrawableChild)

        utilityToolbar.widget.setX(GAP)
        utilityToolbar.widget.setY(GAP + mainToolbar.widget.height + GAP)
        utilityToolbar.widget.refreshPositions()
        utilityToolbar.widget.forEachChild(this::addDrawableChild)

        val mainContent = DirectionalLayoutWidget.vertical().spacing(GAP)
        mainContent.mainPositioner.alignLeft()
        mainContent.setX(GAP)
        mainContent.setY(utilityToolbar.widget.y + utilityToolbar.widget.height + GAP)

        when (activeToolbox) {
            "style" -> ToolboxBuilder.styleBuilder(mainContent, "style", ::clearAndInit)
            "position" -> ToolboxBuilder.positionBuilder(mainContent, "position", ::clearAndInit)
            "rotation" -> ToolboxBuilder.rotationBuilder(mainContent, "rotation", ::clearAndInit)
            "pose" -> ToolboxBuilder.poseBuilder(mainContent, "pose", ::clearAndInit)
        }

        mainContent.refreshPositions()
        mainContent.forEachChild(this::addDrawableChild)

        mainToolbar.syncActive("toolbox", activeToolbox)
    }

    override fun shouldPause(): Boolean {
        return false
    }

    // The game's real per-frame entry point, Screen.renderWithTooltip(), unconditionally
    // calls renderBackground() before render() -- and the default renderBackground() applies
    // blur/darkening (gated on the player's "Menu Background Blurriness" option) regardless of
    // what render() itself does. Override it to a no-op so the live world stays fully visible
    // and unblurred behind the toolbars, matching this screen's shouldPause() = false intent.
    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
    }

    private fun buildMainToolbar(toolbar: ToolbarWidget) {
        addToolbarButton(toolbar, "style", "toolbox")
        addToolbarButton(toolbar, "position", "toolbox")
        addToolbarButton(toolbar, "rotation", "toolbox")
        addToolbarButton(toolbar, "pose", "toolbox")

        toolbar.button(Text.translatable("asc.screen.presets")) { openPresetOverlay() }
        toolbar.button(Text.translatable("asc.screen.craft_adjustment_wand")) {
            ArmorStatuesHelper.craftWand(MinecraftClient.getInstance(), ArmorStatuesHelper.WandTypes.adjustment)
        }
        toolbar.button(Text.translatable("asc.screen.craft_pointer_wand")) {
            ArmorStatuesHelper.craftWand(MinecraftClient.getInstance(), ArmorStatuesHelper.WandTypes.pointer)
        }
    }

    private fun buildUtilityToolbar(toolbar: ToolbarWidget) {
        toolbar.button(Text.translatable("asc.screen.highlight")) { Utility.highlight.accept(Unit) }
        toolbar.button(Text.translatable("asc.screen.lock")) { Utility.lock.accept(Unit) }
        toolbar.button(Text.translatable("asc.screen.unlock")) { Utility.unlock.accept(Unit) }
        toolbar.button(Text.translatable("asc.screen.copy")) { Utility.copy.accept(Unit) }
        toolbar.button(Text.translatable("asc.screen.paste")) { Utility.paste.accept(Unit) }
        toolbar.button(Text.translatable("asc.screen.undo")) { Utility.undo.accept(Unit) }
        toolbar.button(Text.translatable("asc.screen.redo")) { Utility.redo.accept(Unit) }
        toolbar.button(Text.translatable("asc.screen.repeat")) { ArmorStatuesHelper.repeat(MinecraftClient.getInstance()) }
    }

    private fun addToolbarButton(toolbar: ToolbarWidget, key: String, exclusiveGroup: String? = null) {
        toolbar.toolbox(Text.translatable("asc.screen.$key"), key, exclusiveGroup) { btn ->
            activeToolbox = key
            btn.setActive()
            clearAndInit()
        }
    }

    private fun openPresetOverlay() {
        showingPresets = true
        clearAndInit()
    }
}
