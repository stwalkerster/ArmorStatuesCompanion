package net.asch.asc.ui.component

import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.network.chat.Component

/**
 * Vanilla has no collapsible container, so this is a small header button (toggles
 * [expanded], persisted across rebuilds the same way the old EXPANDED map did) plus
 * conditionally-added content.
 */
class CollapsibleSection(
    private val key: String,
    private val title: Component,
    defaultExpanded: Boolean,
    private val tooltipKey: String?,
    private val contentBuilder: () -> LayoutElement
) {
    var expanded: Boolean = EXPANDED.getOrDefault(key, defaultExpanded)
        private set

    fun addTo(target: LinearLayout, onToggle: () -> Unit) {
        val prefix = if (expanded) "▼ " else "▶ "
        val header = NineSliceButtonWidget.of(
            Component.literal(prefix).append(title),
            ButtonTextures.TOOLBAR_TOOL_RENDERER
        ) {
            expanded = !expanded
            EXPANDED[key] = expanded
            onToggle()
        }
        if (tooltipKey != null) {
            header.setTooltip(Tooltip.create(Component.translatable("asc.screen.tooltip.$tooltipKey")))
        }
        target.addChild(header)

        if (expanded) {
            target.addChild(contentBuilder())
        }
    }

    companion object {
        private val EXPANDED: MutableMap<String, Boolean> = mutableMapOf()
    }
}
