package net.asch.asc.ui.component

import net.asch.asc.ModClient
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractButton
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.InputWithModifiers
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier

/**
 * Nine-slice texture drawing, driven by the corner/center patch sizes (3px corners,
 * 58px tileable center, i.e. 64px cells) the mod's existing button textures were
 * authored with. Keeps the 3 existing PNGs; no per-state JSON wrappers needed.
 */
object NineSliceTexture {
    private const val CORNER = 3
    private const val CENTER = 58

    fun draw(
        context: GuiGraphicsExtractor,
        texture: Identifier,
        textureWidth: Int,
        textureHeight: Int,
        u: Int,
        v: Int,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ) {
        val c = CORNER
        val cs = CENTER

        // corners
        region(context, texture, x, y, u, v, c, c, textureWidth, textureHeight)
        region(context, texture, x + width - c, y, u + c + cs, v, c, c, textureWidth, textureHeight)
        region(context, texture, x, y + height - c, u, v + c + cs, c, c, textureWidth, textureHeight)
        region(context, texture, x + width - c, y + height - c, u + c + cs, v + c + cs, c, c, textureWidth, textureHeight)

        // edges, tiled along their long axis
        tiledHorizontal(context, texture, x + c, y, width - 2 * c, c, u + c, v, cs, textureWidth, textureHeight)
        tiledHorizontal(context, texture, x + c, y + height - c, width - 2 * c, c, u + c, v + c + cs, cs, textureWidth, textureHeight)
        tiledVertical(context, texture, x, y + c, c, height - 2 * c, u, v + c, cs, textureWidth, textureHeight)
        tiledVertical(context, texture, x + width - c, y + c, c, height - 2 * c, u + c + cs, v + c, cs, textureWidth, textureHeight)

        // center, tiled both directions
        tiledBoth(context, texture, x + c, y + c, width - 2 * c, height - 2 * c, u + c, v + c, cs, textureWidth, textureHeight)
    }

    private fun region(
        context: GuiGraphicsExtractor, texture: Identifier,
        x: Int, y: Int, u: Int, v: Int, w: Int, h: Int,
        textureWidth: Int, textureHeight: Int
    ) {
        if (w <= 0 || h <= 0) return
        context.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u.toFloat(), v.toFloat(), w, h, textureWidth, textureHeight)
    }

    private fun tiledHorizontal(
        context: GuiGraphicsExtractor, texture: Identifier,
        x: Int, y: Int, length: Int, thickness: Int,
        u: Int, v: Int, segment: Int,
        textureWidth: Int, textureHeight: Int
    ) {
        var drawn = 0
        while (drawn < length) {
            val w = minOf(segment, length - drawn)
            region(context, texture, x + drawn, y, u, v, w, thickness, textureWidth, textureHeight)
            drawn += w
        }
    }

    private fun tiledVertical(
        context: GuiGraphicsExtractor, texture: Identifier,
        x: Int, y: Int, thickness: Int, length: Int,
        u: Int, v: Int, segment: Int,
        textureWidth: Int, textureHeight: Int
    ) {
        var drawn = 0
        while (drawn < length) {
            val h = minOf(segment, length - drawn)
            region(context, texture, x, y + drawn, u, v, thickness, h, textureWidth, textureHeight)
            drawn += h
        }
    }

    private fun tiledBoth(
        context: GuiGraphicsExtractor, texture: Identifier,
        x: Int, y: Int, width: Int, height: Int,
        u: Int, v: Int, segment: Int,
        textureWidth: Int, textureHeight: Int
    ) {
        var drawnY = 0
        while (drawnY < height) {
            val h = minOf(segment, height - drawnY)
            var drawnX = 0
            while (drawnX < width) {
                val w = minOf(segment, width - drawnX)
                region(context, texture, x + drawnX, y + drawnY, u, v, w, h, textureWidth, textureHeight)
                drawnX += w
            }
            drawnY += h
        }
    }
}

typealias ButtonRenderer = (GuiGraphicsExtractor, AbstractWidget) -> Unit

/**
 * Looks up the active/hovered/disabled cell in the PNGs the mod already ships, keyed
 * by the u/v offsets the old nine-patch JSONs used to encode (each state is a 64x64
 * cell).
 */
object ButtonTextures {
    private val BUTTONS = Identifier.fromNamespaceAndPath(ModClient.MOD_ID, "textures/gui/buttons.png")
    private const val BUTTONS_SIZE = 192

    private val TOOLBAR_BUTTONS = Identifier.fromNamespaceAndPath(ModClient.MOD_ID, "textures/gui/toolbar_buttons.png")
    private val TOOLBAR_BASE_BUTTONS = Identifier.fromNamespaceAndPath(ModClient.MOD_ID, "textures/gui/toolbar_base_buttons.png")
    private const val TOOLBAR_TEX_WIDTH = 64
    private const val TOOLBAR_TEX_HEIGHT = 192

    private fun stateV(widget: AbstractWidget): Int = when {
        !widget.active -> 128
        widget.isHovered -> 64
        else -> 0
    }

    private fun renderer(texture: Identifier, texW: Int, texH: Int, u: Int): ButtonRenderer = { context, widget ->
        NineSliceTexture.draw(context, texture, texW, texH, u, stateV(widget), widget.x, widget.y, widget.width, widget.height)
    }

    val DEFAULT_RENDERER: ButtonRenderer = renderer(BUTTONS, BUTTONS_SIZE, BUTTONS_SIZE, 0)
    val ON_RENDERER: ButtonRenderer = renderer(BUTTONS, BUTTONS_SIZE, BUTTONS_SIZE, 128)
    val OFF_RENDERER: ButtonRenderer = renderer(BUTTONS, BUTTONS_SIZE, BUTTONS_SIZE, 64)

    val TOOLBAR_TOOL_RENDERER: ButtonRenderer = renderer(TOOLBAR_BUTTONS, TOOLBAR_TEX_WIDTH, TOOLBAR_TEX_HEIGHT, 0)
    val TOOLBAR_BASE_RENDERER: ButtonRenderer = renderer(TOOLBAR_BASE_BUTTONS, TOOLBAR_TEX_WIDTH, TOOLBAR_TEX_HEIGHT, 0)
}

/**
 * Generic nine-sliced button widget. Width/height must be known at construction time
 * (vanilla widgets are pixel-sized, not auto-sized to content) -- use [of] to size
 * from the text.
 */
open class NineSliceButtonWidget(
    x: Int, y: Int, width: Int, height: Int,
    text: Component,
    var renderer: ButtonRenderer,
    private val onPressAction: (NineSliceButtonWidget) -> Unit
) : AbstractButton(x, y, width, height, text) {

    override fun onPress(input: InputWithModifiers) {
        onPressAction(this)
    }

    override fun extractContents(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        renderer(context, this)
        context.centeredText(Minecraft.getInstance().font, message, x + width / 2, y + (height - 8) / 2, -1)
    }

    override fun updateWidgetNarration(output: NarrationElementOutput) {
        defaultButtonNarrationText(output)
    }

    companion object {
        const val DEFAULT_HEIGHT = 20
        const val PADDING_X = 8

        fun of(
            text: Component,
            renderer: ButtonRenderer = ButtonTextures.DEFAULT_RENDERER,
            onPress: (NineSliceButtonWidget) -> Unit
        ): NineSliceButtonWidget {
            val font = Minecraft.getInstance().font
            val width = font.width(text) + PADDING_X * 2
            return NineSliceButtonWidget(0, 0, width, DEFAULT_HEIGHT, text, renderer, onPress)
        }
    }
}
