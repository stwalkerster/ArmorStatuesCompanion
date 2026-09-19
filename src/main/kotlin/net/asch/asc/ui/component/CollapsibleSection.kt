package net.asch.asc.ui.component

import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.DirectionalLayoutWidget
import net.minecraft.client.gui.widget.Widget
import net.minecraft.text.Text

/**
 * Vanilla has no collapsible container, so this is a small header button (toggles
 * [expanded], persisted across rebuilds the same way the old EXPANDED map did) plus
 * conditionally-added content.
 */
class CollapsibleSection(
    private val key: String,
    private val title: Text,
    defaultExpanded: Boolean,
    private val tooltipKey: String?,
    private val contentBuilder: () -> Widget
) {
    var expanded: Boolean = EXPANDED.getOrDefault(key, defaultExpanded)
        private set

    fun addTo(target: DirectionalLayoutWidget, onToggle: () -> Unit) {
        val prefix = if (expanded) "▼ " else "▶ "
        val header = NineSliceButtonWidget.of(
            Text.literal(prefix).append(title),
            ButtonTextures.TOOLBAR_TOOL_RENDERER
        ) {
            expanded = !expanded
            EXPANDED[key] = expanded
            onToggle()
        }
        if (tooltipKey != null) {
            header.setTooltip(Tooltip.of(Text.translatable("asc.screen.tooltip.$tooltipKey")))
        }
        target.add(header)

        if (expanded) {
            target.add(contentBuilder())
        }
    }

    companion object {
        private val EXPANDED: MutableMap<String, Boolean> = mutableMapOf()
    }
}
