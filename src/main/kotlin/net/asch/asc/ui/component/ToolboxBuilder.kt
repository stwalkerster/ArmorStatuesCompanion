package net.asch.asc.ui.component

import net.asch.asc.as_datapack.triggers.*
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.DirectionalLayoutWidget
import net.minecraft.client.gui.widget.GridWidget
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.client.gui.widget.Widget
import net.minecraft.text.Text

object ToolboxBuilder {
    private const val GAP = 2
    private val INFO_COLOR = 0xFF5555FF.toInt()
    private val WARNING_COLOR = 0xFFFFFF00.toInt()

    fun styleBuilder(target: DirectionalLayoutWidget, key: String, rebuild: () -> Unit) {
        fun addLine(action: Style, keyTrue: String, keyFalse: String, grid: GridWidget, row: Int) {
            grid.add(label("$action"), row, 0)
            grid.add(getInfo("$action"), row, 1)
            grid.add(
                buttonGroup(listOf(
                        OnOffButtonProperties(action, true, keyTrue),
                        OnOffButtonProperties(action, false, keyFalse)
                    )), row, 2
            )
        }

        GridToolboxBuilder(key, columns = 3)
            .add { grid, row -> addLine(Style.base_plate, "show", "hide", grid, row) }
            .add { grid, row -> addLine(Style.arms, "show", "hide", grid, row) }
            .add { grid, row -> addLine(Style.stand, "show", "hide", grid, row) }
            .add { grid, row -> addLine(Style.stand_name, "show", "hide", grid, row) }
            .add { grid, row -> addLine(Style.visual_fire, "show", "hide", grid, row) }
            .add { grid, row -> addLine(Style.small_stand, "enable", "disable", grid, row) }
            .add { grid, row -> addLine(Style.gravity, "enable", "disable", grid, row) }
            .buildInto(target, rebuild)
    }

    fun positionBuilder(target: DirectionalLayoutWidget, key: String, rebuild: () -> Unit) {
        fun addPositionLine(action: Position, grid: GridWidget, row: Int, infoKey: String? = null, warningKey: String? = null) {
            grid.add(label("$action"), row, 0)
            grid.add(positionGroup(action, infoKey, warningKey), row, 1)
        }

        fun addAlignmentLine(action: Alignment, flow: DirectionalLayoutWidget, infoKey: String? = null) {
            val layout = DirectionalLayoutWidget.horizontal().spacing(GAP)
            layout.mainPositioner.alignVerticalCenter()
            layout.add(button(action, Unit))
            if (infoKey != null) {
                layout.add(getInfo(infoKey))
            }
            flow.add(layout)
        }

        FlowToolboxBuilder(key)
            .add { flow ->
                GridToolboxBuilder("position", defaultExpanded = false, tooltipKey = "relative_position")
                    .add { grid, row -> addPositionLine(Position.x, grid, row) }
                    .add { grid, row -> addPositionLine(Position.y, grid, row, warningKey = "warn_gravity") }
                    .add { grid, row -> addPositionLine(Position.z, grid, row) }
                    .buildInto(flow, rebuild)
            }
            .add { flow ->
                GridToolboxBuilder("aligned_position", defaultExpanded = false, tooltipKey = "aligned_position")
                    .add { grid, row -> addPositionLine(Position.aligned_x, grid, row) }
                    .add { grid, row -> addPositionLine(Position.aligned_z, grid, row) }
                    .buildInto(flow, rebuild)
            }
            .add { flow ->
                GridToolboxBuilder("exact_position", defaultExpanded = false, tooltipKey = "exact_position")
                    .add { grid, row -> addPositionLine(Position.exact_x, grid, row) }
                    .add { grid, row -> addPositionLine(Position.exact_y, grid, row, warningKey = "warn_gravity") }
                    .add { grid, row -> addPositionLine(Position.exact_z, grid, row) }
                    .buildInto(flow, rebuild)
            }
            .add { flow ->
                FlowToolboxBuilder("alignment", defaultExpanded = false)
                    .add { innerFlow -> addAlignmentLine(Alignment.block_to_surface, innerFlow) }
                    .add { innerFlow -> addAlignmentLine(Alignment.item_to_surface_upright, innerFlow) }
                    .add { innerFlow -> addAlignmentLine(Alignment.item_to_surface_flat, innerFlow) }
                    .add { innerFlow -> addAlignmentLine(Alignment.tool_to_surface_flat, innerFlow) }
                    .add { innerFlow -> addAlignmentLine(Alignment.rack, innerFlow, infoKey = "rack") }
                    .buildInto(flow, rebuild)
            }
            .buildInto(target, rebuild)
    }

    fun rotationBuilder(target: DirectionalLayoutWidget, key: String, rebuild: () -> Unit) {
        fun addAdjustmentsLine(action: Rotation, grid: GridWidget, row: Int) {
            grid.add(label("$action"), row, 0)
            grid.add(adjustmentGroup(action), row, 1)
        }

        FlowToolboxBuilder(key)
            .add { flow ->
                flow.add(textLabel(Text.translatable("asc.screen.angle_steps")))
                flow.add(angleStepGroup())
            }
            .add { innerFlow ->
                val layout = DirectionalLayoutWidget.horizontal().spacing(GAP)
                layout.mainPositioner.alignVerticalCenter()
                layout.add(textLabel(Text.translatable("asc.screen.rotations")))
                innerFlow.add(layout)
                innerFlow.add(rotationGroup())
            }
            .add { flow ->
                GridToolboxBuilder("adjustments", defaultExpanded = false)
                    .add { grid, row -> addAdjustmentsLine(Rotation.head, grid, row) }
                    .add { grid, row -> addAdjustmentsLine(Rotation.body, grid, row) }
                    .add { grid, row -> addAdjustmentsLine(Rotation.left_arm, grid, row) }
                    .add { grid, row -> addAdjustmentsLine(Rotation.right_arm, grid, row) }
                    .add { grid, row -> addAdjustmentsLine(Rotation.left_leg, grid, row) }
                    .add { grid, row -> addAdjustmentsLine(Rotation.right_leg, grid, row) }
                    .buildInto(flow, rebuild)
            }
            .buildInto(target, rebuild)
    }

    fun poseBuilder(target: DirectionalLayoutWidget, key: String, rebuild: () -> Unit) {
        fun addPointingLine(action: Pointing, grid: GridWidget, row: Int, startColumn: Int) {
            grid.add(label("$action"), row, startColumn)
            grid.add(buttonGroup<Pointing.Direction>(action), row, startColumn + 1)
        }

        fun addSwapMainHandLine(grid: GridWidget, row: Int) {
            grid.add(textLabel(Text.translatable("asc.screen.swap_main_hand")), row, 0)
            grid.add(buttonGroup<StandUtility.SwapTarget>(StandUtility.swap_main_hand), row, 1)
        }

        fun addMirrorLine(action: StandUtility, grid: GridWidget, row: Int) {
            grid.add(label("$action"), row, 0)
            grid.add(buttonGroup<StandUtility.MirrorDirection>(action), row, 1)
        }

        FlowToolboxBuilder(key)
            .add { flow ->
                GridToolboxBuilder("pointing", columns = 4, defaultExpanded = false, tooltipKey = "pointing")
                    .add { grid, row ->
                        addPointingLine(Pointing.head, grid, row, 0)
                        addPointingLine(Pointing.body, grid, row, 2)
                    }.add { grid, row ->
                        addPointingLine(Pointing.left_arm, grid, row, 0)
                        addPointingLine(Pointing.right_arm, grid, row, 2)
                    }.add { grid, row ->
                        addPointingLine(Pointing.left_leg, grid, row, 0)
                        addPointingLine(Pointing.right_leg, grid, row, 2)
                    }
                    .buildInto(flow, rebuild)
            }
            .add { flow ->
                FlowToolboxBuilder("utility", defaultExpanded = false)
                    .add { innerFlow ->
                        val layout = GridWidget().setSpacing(GAP)
                        layout.mainPositioner.alignLeft().alignVerticalCenter()
                        addSwapMainHandLine(layout, 0)
                        addMirrorLine(StandUtility.mirror_arms, layout, 1)
                        addMirrorLine(StandUtility.mirror_legs, layout, 2)
                        innerFlow.add(layout)
                    }
                    .add { innerFlow -> innerFlow.add(button(StandUtility.flip, Unit)) }
                    .buildInto(flow, rebuild)
            }
            .buildInto(target, rebuild)
    }

    private fun tr() = MinecraftClient.getInstance().textRenderer

    private fun textLabel(text: Text): TextWidget = TextWidget(text, tr())

    private fun label(key: String): TextWidget = textLabel(Text.translatable("asc.screen.$key"))

    private fun button(
        trigger: ArmorStatuesTriggers, value: Any, key: String = "$trigger",
        renderer: ButtonRenderer? = null, tooltipKey: String? = null
    ): NineSliceButtonWidget {
        val btn = NineSliceButtonWidget.of(Text.translatable("asc.screen.$key"), renderer ?: ButtonTextures.DEFAULT_RENDERER) {
            trigger.accept(value)
        }
        addTooltip(btn, tooltipKey)
        return btn
    }

    private fun buttonGroup(btnProperties: Collection<ButtonProperties>): DirectionalLayoutWidget {
        val layout = DirectionalLayoutWidget.horizontal().spacing(GAP)
        layout.mainPositioner.alignVerticalCenter()
        for (btnProperty in btnProperties) {
            layout.add(button(btnProperty.action, btnProperty.value, btnProperty.key, btnProperty.renderer))
        }

        return layout
    }

    private inline fun <reified E : Enum<E>> buttonGroup(action: ArmorStatuesTriggers): DirectionalLayoutWidget {
        return buttonGroup(E::class.java.enumConstants.map { e -> BasicButtonProperties(action, e, "$e") })
    }

    private fun angleStepGroup(): DirectionalLayoutWidget {
        val layout = DirectionalLayoutWidget.horizontal().spacing(GAP)
        layout.mainPositioner.alignVerticalCenter()
        layout.add(button(Rotation.set_step_angle, Rotation.StepAngles.deg_1, "deg_1"))
        layout.add(button(Rotation.set_step_angle, Rotation.StepAngles.deg_5, "deg_5"))
        layout.add(button(Rotation.set_step_angle, Rotation.StepAngles.deg_15, "deg_15"))
        layout.add(button(Rotation.set_step_angle, Rotation.StepAngles.deg_45, "deg_45"))

        return layout
    }

    private fun positionGroup(action: Position, infoKey: String? = null, warningKey: String? = null): DirectionalLayoutWidget {
        val layout = DirectionalLayoutWidget.horizontal().spacing(GAP)
        layout.mainPositioner.alignVerticalCenter()
        if (action == Position.x || action == Position.y || action == Position.z) {
            layout.add(button(action, Position.Offset.negative_8, "negative_8"))
            layout.add(button(action, Position.Offset.negative_3, "negative_3"))
            layout.add(button(action, Position.Offset.negative_1, "negative_1"))
        } else {
            layout.add(button(action, Position.AlignedExactOffset.negative_8, "negative_8"))
            layout.add(button(action, Position.AlignedExactOffset.negative_3, "negative_3"))
            layout.add(button(action, Position.AlignedExactOffset.negative_1, "negative_1"))
        }
        layout.add(textLabel(Text.literal("-")))
        if (action == Position.x || action == Position.y || action == Position.z) {
            layout.add(button(action, Position.Offset.positive_1, "positive_1"))
            layout.add(button(action, Position.Offset.positive_3, "positive_3"))
            layout.add(button(action, Position.Offset.positive_8, "positive_8"))
        } else {
            layout.add(button(action, Position.AlignedExactOffset.positive_1, "positive_1"))
            layout.add(button(action, Position.AlignedExactOffset.positive_3, "positive_3"))
            layout.add(button(action, Position.AlignedExactOffset.positive_8, "positive_8"))
        }

        if (infoKey != null) {
            layout.add(getInfo(infoKey))
        }

        addWarning(layout, warningKey)

        return layout
    }

    private fun rotationGroup(): DirectionalLayoutWidget {
        val layout = DirectionalLayoutWidget.horizontal().spacing(GAP)
        layout.mainPositioner.alignVerticalCenter()
        layout.add(button(Rotation.rotate, Rotation.Direction.left, "left"))
        layout.add(button(Rotation.rotate, Rotation.Direction.right, "right"))
        layout.add(button(Rotation.rotate, Rotation.Direction.toward, "toward"))
        layout.add(button(Rotation.rotate, Rotation.Direction.away, "away"))
        return layout
    }

    private fun adjustmentGroup(action: Rotation): DirectionalLayoutWidget {
        val layout = DirectionalLayoutWidget.horizontal().spacing(GAP)
        layout.mainPositioner.alignVerticalCenter()
        layout.add(button(action, Rotation.AxisDirection.negative_x, "negative_x"))
        layout.add(button(action, Rotation.AxisDirection.positive_x, "positive_x"))
        layout.add(textLabel(Text.literal("-")))
        layout.add(button(action, Rotation.AxisDirection.negative_y, "negative_y"))
        layout.add(button(action, Rotation.AxisDirection.positive_y, "positive_y"))
        layout.add(textLabel(Text.literal("-")))
        layout.add(button(action, Rotation.AxisDirection.negative_z, "negative_z"))
        layout.add(button(action, Rotation.AxisDirection.positive_z, "positive_z"))
        return layout
    }

    private fun addTooltip(component: ClickableWidget, key: String?) {
        if (key != null) {
            component.setTooltip(Tooltip.of(Text.translatable("asc.screen.tooltip.$key")))
        }
    }

    private fun getInfo(key: String?): TextWidget {
        val infoLbl = textLabel(Text.literal("🛈"))
        infoLbl.setTextColor(INFO_COLOR)
        addTooltip(infoLbl, key)
        return infoLbl
    }

    private fun addWarning(layout: DirectionalLayoutWidget, key: String?) {
        if (key != null) {
            val warningLbl = textLabel(Text.literal("⚠"))
            warningLbl.setTextColor(WARNING_COLOR)
            addTooltip(warningLbl, key)
            layout.add(warningLbl)
        }
    }

    private interface ButtonProperties {
        val action: ArmorStatuesTriggers
        val value: Any
        val key: String
        val renderer: ButtonRenderer?
    }

    private data class BasicButtonProperties(
        override val action: ArmorStatuesTriggers,
        override val value: Any,
        override val key: String = "$action",
        override val renderer: ButtonRenderer? = null
    ) : ButtonProperties

    private data class OnOffButtonProperties(
        override val action: ArmorStatuesTriggers,
        override val value: Boolean,
        override val key: String = "$action",
        override val renderer: ButtonRenderer = if (value) ButtonTextures.ON_RENDERER else ButtonTextures.OFF_RENDERER
    ) : ButtonProperties

    private abstract class ToolboxBuilder<C>(
        private val key: String,
        private val defaultExpanded: Boolean = true,
        private val tooltipKey: String? = null
    ) {
        val consumerList: MutableList<C> = mutableListOf()

        fun add(consumer: C): ToolboxBuilder<C> {
            consumerList.add(consumer)
            return this
        }

        fun buildInto(target: DirectionalLayoutWidget, rebuild: () -> Unit) {
            val layout = create()
            populate(layout)

            val section = CollapsibleSection(key, Text.translatable("asc.screen.$key"), defaultExpanded, tooltipKey) { layout }
            section.addTo(target, rebuild)
        }

        protected abstract fun create(): Widget
        protected abstract fun populate(layout: Widget)
    }

    private class GridToolboxBuilder(key: String, private val columns: Int = 2, defaultExpanded: Boolean = true, tooltipKey: String? = null) :
        ToolboxBuilder<(GridWidget, Int) -> Unit>(key, defaultExpanded, tooltipKey) {
        override fun create(): Widget {
            val layout = GridWidget().setSpacing(GAP)
            layout.mainPositioner.alignLeft().alignVerticalCenter()
            return layout
        }

        override fun populate(layout: Widget) {
            val grid = layout as GridWidget
            for (row in consumerList.indices) {
                consumerList[row](grid, row)
            }
        }
    }

    private class FlowToolboxBuilder(key: String, defaultExpanded: Boolean = true, tooltipKey: String? = null) :
        ToolboxBuilder<(DirectionalLayoutWidget) -> Unit>(key, defaultExpanded, tooltipKey) {
        override fun create(): Widget {
            val layout = DirectionalLayoutWidget.vertical().spacing(GAP)
            layout.mainPositioner.alignLeft()
            return layout
        }

        override fun populate(layout: Widget) {
            val flow = layout as DirectionalLayoutWidget
            for (consumer in consumerList) {
                consumer(flow)
            }
        }
    }
}
