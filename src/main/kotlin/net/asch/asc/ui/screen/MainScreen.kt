package net.asch.asc.ui.screen

import net.asch.asc.as_datapack.ArmorStatuesHelper
import net.asch.asc.as_datapack.triggers.Utility
import net.asch.asc.ui.component.PresetPanelWidget
import net.asch.asc.ui.component.ToolbarWidget
import net.asch.asc.ui.component.ToolboxBuilder
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class MainScreen : Screen(Component.translatable("asc.screen.title")) {
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
            addRenderableOnly(panel.backgroundDrawable)
            addRenderableWidget(panel.listWidget)
            addRenderableWidget(panel.previewButton)
            return
        }

        mainToolbar.widget.setX(GAP)
        mainToolbar.widget.setY(GAP)
        mainToolbar.widget.arrangeElements()
        mainToolbar.widget.visitWidgets(this::addRenderableWidget)

        utilityToolbar.widget.setX(GAP)
        utilityToolbar.widget.setY(GAP + mainToolbar.widget.height + GAP)
        utilityToolbar.widget.arrangeElements()
        utilityToolbar.widget.visitWidgets(this::addRenderableWidget)

        val mainContent = LinearLayout.vertical().spacing(GAP)
        mainContent.defaultCellSetting().alignHorizontallyLeft()
        mainContent.setX(GAP)
        mainContent.setY(utilityToolbar.widget.y + utilityToolbar.widget.height + GAP)

        when (activeToolbox) {
            "style" -> ToolboxBuilder.styleBuilder(mainContent, "style", ::rebuildWidgets)
            "position" -> ToolboxBuilder.positionBuilder(mainContent, "position", ::rebuildWidgets)
            "rotation" -> ToolboxBuilder.rotationBuilder(mainContent, "rotation", ::rebuildWidgets)
            "pose" -> ToolboxBuilder.poseBuilder(mainContent, "pose", ::rebuildWidgets)
        }

        mainContent.arrangeElements()
        mainContent.visitWidgets(this::addRenderableWidget)

        mainToolbar.syncActive("toolbox", activeToolbox)
    }

    override fun isPauseScreen(): Boolean {
        return false
    }

    // The game's real per-frame entry point unconditionally calls extractBackground()
    // before extractRenderState() -- and the default extractBackground() applies
    // blur/darkening (gated on the player's "Menu Background Blurriness" option) regardless of
    // what extractRenderState() itself does. Override it to a no-op so the live world stays fully
    // visible and unblurred behind the toolbars, matching this screen's isPauseScreen() = false intent.
    override fun extractBackground(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
    }

    private fun buildMainToolbar(toolbar: ToolbarWidget) {
        addToolbarButton(toolbar, "style", "toolbox")
        addToolbarButton(toolbar, "position", "toolbox")
        addToolbarButton(toolbar, "rotation", "toolbox")
        addToolbarButton(toolbar, "pose", "toolbox")

        toolbar.button(Component.translatable("asc.screen.presets")) { openPresetOverlay() }
        toolbar.button(Component.translatable("asc.screen.craft_adjustment_wand")) {
            ArmorStatuesHelper.craftWand(Minecraft.getInstance(), ArmorStatuesHelper.WandTypes.adjustment)
        }
        toolbar.button(Component.translatable("asc.screen.craft_pointer_wand")) {
            ArmorStatuesHelper.craftWand(Minecraft.getInstance(), ArmorStatuesHelper.WandTypes.pointer)
        }
    }

    private fun buildUtilityToolbar(toolbar: ToolbarWidget) {
        toolbar.button(Component.translatable("asc.screen.highlight")) { Utility.highlight.accept(Unit) }
        toolbar.button(Component.translatable("asc.screen.lock")) { Utility.lock.accept(Unit) }
        toolbar.button(Component.translatable("asc.screen.unlock")) { Utility.unlock.accept(Unit) }
        toolbar.button(Component.translatable("asc.screen.copy")) { Utility.copy.accept(Unit) }
        toolbar.button(Component.translatable("asc.screen.paste")) { Utility.paste.accept(Unit) }
        toolbar.button(Component.translatable("asc.screen.undo")) { Utility.undo.accept(Unit) }
        toolbar.button(Component.translatable("asc.screen.redo")) { Utility.redo.accept(Unit) }
        toolbar.button(Component.translatable("asc.screen.repeat")) { ArmorStatuesHelper.repeat(Minecraft.getInstance()) }
    }

    private fun addToolbarButton(toolbar: ToolbarWidget, key: String, exclusiveGroup: String? = null) {
        toolbar.toolbox(Component.translatable("asc.screen.$key"), key, exclusiveGroup) { btn ->
            activeToolbox = key
            btn.setActive()
            rebuildWidgets()
        }
    }

    private fun openPresetOverlay() {
        showingPresets = true
        rebuildWidgets()
    }
}
