package net.asch.asc.ui.component

import net.asch.asc.ModClient
import net.asch.asc.as_datapack.triggers.Presets
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier

/**
 * Replaces PresetPanel.kt. Not itself a widget -- exposes the pieces (a scrollable
 * entry list, a preview button, a background drawable) the owning Screen wires into
 * itself via addDrawable/addDrawableChild, since those are protected on Screen and
 * can't be called from an unrelated helper class.
 */
class PresetPanelWidget(screenWidth: Int, screenHeight: Int) {
    val x: Int
    val y: Int
    val width: Int
    val height: Int

    val listWidget: PresetListWidget
    val previewButton: NineSliceButtonWidget
    val backgroundDrawable: Renderable

    private var selectedPreset: Presets? = null

    init {
        width = (screenWidth * 0.8).toInt()
        height = (screenHeight * 0.8).toInt()
        x = (screenWidth - width) / 2
        y = (screenHeight - height) / 2

        val columnWidth = (width * 0.49).toInt()
        val listX = x + 5
        val listY = y + 5
        val listWidth = columnWidth - 10
        val listHeight = height - 10

        val previewX = x + width - columnWidth
        val previewY = y + 8

        listWidget = PresetListWidget(Minecraft.getInstance(), listWidth, listHeight, listY, ITEM_HEIGHT) { preset ->
            selectedPreset = preset
            val key = if (preset == Presets.randomized) "set_randomized_preset" else "set_preset"
            previewButton.message = Component.translatable("asc.screen.$key")
            previewButton.visible = true
        }
        listWidget.x = listX

        previewButton = NineSliceButtonWidget.of(Component.translatable("asc.screen.set_preset"), ButtonTextures.DEFAULT_RENDERER) {
            selectedPreset?.accept(Unit)
        }
        previewButton.visible = false
        previewButton.x = previewX + (columnWidth - previewButton.width) / 2
        previewButton.y = previewY

        val previewImageX = previewX + (columnWidth - PREVIEW_SIZE) / 2
        val previewImageY = previewY + NineSliceButtonWidget.DEFAULT_HEIGHT + 10

        backgroundDrawable = Renderable { context, _, _, _ ->
            context.fill(x, y, x + width, y + height, PANEL_COLOR)
            context.fill(listX, listY, listX + listWidth, listY + listHeight, LIST_BG_COLOR)
            drawOutline(context, listX, listY, listWidth, listHeight, OUTLINE_COLOR)

            val preset = selectedPreset
            if (preset != null && preset != Presets.randomized) {
                val textureId = Identifier.fromNamespaceAndPath(ModClient.MOD_ID, "textures/presets/${preset.name.lowercase()}.png")
                context.blit(
                    RenderPipelines.GUI_TEXTURED, textureId,
                    previewImageX, previewImageY, 0f, 0f,
                    PREVIEW_SIZE, PREVIEW_SIZE, PREVIEW_SIZE, PREVIEW_SIZE
                )
            }
        }
    }

    private fun drawOutline(context: GuiGraphicsExtractor, x: Int, y: Int, w: Int, h: Int, color: Int) {
        context.fill(x, y, x + w, y + 1, color)
        context.fill(x, y + h - 1, x + w, y + h, color)
        context.fill(x, y, x + 1, y + h, color)
        context.fill(x + w - 1, y, x + w, y + h, color)
    }

    companion object {
        private const val ITEM_HEIGHT = 14
        private const val PREVIEW_SIZE = 160
        private const val PANEL_COLOR = -939524096 // 0xC8101010
        private const val LIST_BG_COLOR = -16777216 // 0xFF000000
        private const val OUTLINE_COLOR = -12303292 // 0xFF404040
    }
}

class PresetListWidget(
    client: Minecraft,
    width: Int, height: Int, y: Int, itemHeight: Int,
    private val onSelected: (Presets) -> Unit
) : ObjectSelectionList<PresetListWidget.PresetEntry>(client, width, height, y, itemHeight) {

    init {
        for (preset in Presets.entries) {
            addEntry(PresetEntry(preset))
        }
    }

    override fun setSelected(entry: PresetEntry?) {
        super.setSelected(entry)
        if (entry != null) onSelected(entry.preset)
    }

    inner class PresetEntry(val preset: Presets) : Entry<PresetEntry>() {
        override fun getNarration(): Component = Component.translatable("asc.screen.preset.$preset")

        override fun extractContent(
            context: GuiGraphicsExtractor,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        ) {
            if (this@PresetListWidget.selected === this) {
                context.fill(x, y, x + width, y + height, 1140916224) // 0x4400FF00
            } else if (hovered) {
                context.fill(x, y, x + width, y + height, 1157627903) // 0x44FFFFFF
            }

            val font = Minecraft.getInstance().font
            context.text(
                font,
                Component.translatable("asc.screen.preset.$preset"),
                x + 2, y + (height - font.lineHeight) / 2,
                -1, false
            )
        }
    }
}
