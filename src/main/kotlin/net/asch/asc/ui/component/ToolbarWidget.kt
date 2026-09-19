package net.asch.asc.ui.component

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractButton
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.InputWithModifiers
import net.minecraft.network.chat.Component

/**
 * Wraps a vanilla LinearLayout -- the screen positions/refreshes/registers
 * [widget]'s children directly (vanilla widgets aren't Renderable-on-their-own the way layouts are).
 */
class ToolbarWidget {
    val widget: LinearLayout = LinearLayout.horizontal().spacing(GAP)
    private val exclusiveGroups: MutableMap<String, MutableList<ToolButtonWidget>> = mutableMapOf()

    init {
        widget.defaultCellSetting().alignVerticallyMiddle()
    }

    fun toolbox(
        text: Component,
        id: String,
        exclusiveGroup: String? = null,
        action: (ToolButtonWidget) -> Unit
    ): ToolButtonWidget {
        val btn = ToolButtonWidget(text, id, action)
        widget.addChild(btn)

        if (exclusiveGroup != null) {
            val list = exclusiveGroups.getOrPut(exclusiveGroup) { mutableListOf() }
            list.add(btn)

            btn.onActiveChanged = { newActive ->
                if (newActive) {
                    for (other in list) {
                        if (other !== btn) other.setInactive()
                    }
                }
            }
        }

        return btn
    }

    fun button(text: Component, action: () -> Unit): NineSliceButtonWidget {
        val btn = NineSliceButtonWidget.of(text, ButtonTextures.TOOLBAR_BASE_RENDERER) { action() }
        widget.addChild(btn)
        return btn
    }

    fun press(group: String, id: String?) {
        if (id == null) return
        val groupButtons = exclusiveGroups[group] ?: return
        groupButtons.find { it.toolboxId == id }?.press()
    }

    /** Syncs button active/inactive visuals to [id] without re-invoking the press action. */
    fun syncActive(group: String, id: String?) {
        val groupButtons = exclusiveGroups[group] ?: return
        for (btn in groupButtons) {
            if (btn.toolboxId == id) btn.setActive() else btn.setInactive()
        }
    }

    companion object {
        const val GAP = 2
    }
}

class ToolButtonWidget(
    text: Component,
    val toolboxId: String,
    private val onPressAction: (ToolButtonWidget) -> Unit
) : AbstractButton(
    0, 0,
    Minecraft.getInstance().font.width(text) + NineSliceButtonWidget.PADDING_X * 2,
    NineSliceButtonWidget.DEFAULT_HEIGHT,
    text
) {
    // A toggled-on tool button becomes non-interactable (active = false) until another
    // button in its exclusive group is pressed, preventing re-triggering an
    // already-open toolbox.
    var toggledActive: Boolean = false
        private set
    var onActiveChanged: ((Boolean) -> Unit)? = null

    override fun onPress(input: InputWithModifiers) {
        onPressAction(this)
    }

    override fun extractContents(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        ButtonTextures.TOOLBAR_TOOL_RENDERER(context, this)
        context.centeredText(Minecraft.getInstance().font, message, x + width / 2, y + (height - 8) / 2, -1)
    }

    override fun updateWidgetNarration(output: NarrationElementOutput) {
        defaultButtonNarrationText(output)
    }

    fun setActive() {
        toggledActive = true
        active = false
        onActiveChanged?.invoke(true)
    }

    fun setInactive() {
        toggledActive = false
        active = true
        onActiveChanged?.invoke(false)
    }

    fun press() {
        onPressAction(this)
    }
}
