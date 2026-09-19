package net.asch.asc.ui.component

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.DirectionalLayoutWidget
import net.minecraft.client.gui.widget.PressableWidget
import net.minecraft.client.input.AbstractInput
import net.minecraft.text.Text

/**
 * Wraps a vanilla DirectionalLayoutWidget -- the screen positions/refreshes/registers
 * [widget]'s children directly (vanilla widgets aren't Drawable themselves).
 */
class ToolbarWidget {
    val widget: DirectionalLayoutWidget = DirectionalLayoutWidget.horizontal().spacing(GAP)
    private val exclusiveGroups: MutableMap<String, MutableList<ToolButtonWidget>> = mutableMapOf()

    init {
        widget.mainPositioner.alignVerticalCenter()
    }

    fun toolbox(
        text: Text,
        id: String,
        exclusiveGroup: String? = null,
        action: (ToolButtonWidget) -> Unit
    ): ToolButtonWidget {
        val btn = ToolButtonWidget(text, id, action)
        widget.add(btn)

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

    fun button(text: Text, action: () -> Unit): NineSliceButtonWidget {
        val btn = NineSliceButtonWidget.of(text, ButtonTextures.TOOLBAR_BASE_RENDERER) { action() }
        widget.add(btn)
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
    text: Text,
    val toolboxId: String,
    private val onPressAction: (ToolButtonWidget) -> Unit
) : PressableWidget(
    0, 0,
    MinecraftClient.getInstance().textRenderer.getWidth(text) + NineSliceButtonWidget.PADDING_X * 2,
    NineSliceButtonWidget.DEFAULT_HEIGHT,
    text
) {
    // A toggled-on tool button becomes non-interactable (active = false) until another
    // button in its exclusive group is pressed, preventing re-triggering an
    // already-open toolbox.
    var toggledActive: Boolean = false
        private set
    var onActiveChanged: ((Boolean) -> Unit)? = null

    override fun onPress(input: AbstractInput) {
        onPressAction(this)
    }

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        ButtonTextures.TOOLBAR_TOOL_RENDERER(context, this)
        drawMessage(context, MinecraftClient.getInstance().textRenderer, -1)
    }

    override fun appendClickableNarrations(builder: net.minecraft.client.gui.screen.narration.NarrationMessageBuilder) {
        appendDefaultNarrations(builder)
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
