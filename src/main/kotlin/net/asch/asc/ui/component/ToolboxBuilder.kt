package net.asch.asc.ui.component

import net.asch.asc.as_datapack.triggers.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.layouts.GridLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.network.chat.Component

object ToolboxBuilder {
    private const val GAP = 2
    private val INFO_COLOR = 0xFF5555FF.toInt()
    private val WARNING_COLOR = 0xFFFFFF00.toInt()

    fun styleBuilder(target: LinearLayout, key: String, rebuild: () -> Unit) {
        fun addLine(action: Style, keyTrue: String, keyFalse: String, grid: GridLayout, row: Int) {
            grid.addChild(label("$action"), row, 0)
            grid.addChild(getInfo("$action"), row, 1)
            grid.addChild(
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

    fun positionBuilder(target: LinearLayout, key: String, rebuild: () -> Unit) {
        fun addPositionLine(action: Position, grid: GridLayout, row: Int, infoKey: String? = null, warningKey: String? = null) {
            grid.addChild(label("$action"), row, 0)
            grid.addChild(positionGroup(action, infoKey, warningKey), row, 1)
        }

        fun addAlignmentLine(action: Alignment, flow: LinearLayout, infoKey: String? = null) {
            val layout = LinearLayout.horizontal().spacing(GAP)
            layout.defaultCellSetting().alignVerticallyMiddle()
            layout.addChild(button(action, Unit))
            if (infoKey != null) {
                layout.addChild(getInfo(infoKey))
            }
            flow.addChild(layout)
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

    fun rotationBuilder(target: LinearLayout, key: String, rebuild: () -> Unit) {
        fun addAdjustmentsLine(action: Rotation, grid: GridLayout, row: Int) {
            grid.addChild(label("$action"), row, 0)
            grid.addChild(adjustmentGroup(action), row, 1)
        }

        FlowToolboxBuilder(key)
            .add { flow ->
                flow.addChild(textLabel(Component.translatable("asc.screen.angle_steps")))
                flow.addChild(angleStepGroup())
            }
            .add { innerFlow ->
                val layout = LinearLayout.horizontal().spacing(GAP)
                layout.defaultCellSetting().alignVerticallyMiddle()
                layout.addChild(textLabel(Component.translatable("asc.screen.rotations")))
                innerFlow.addChild(layout)
                innerFlow.addChild(rotationGroup())
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

    fun poseBuilder(target: LinearLayout, key: String, rebuild: () -> Unit) {
        fun addPointingLine(action: Pointing, grid: GridLayout, row: Int, startColumn: Int) {
            grid.addChild(label("$action"), row, startColumn)
            grid.addChild(buttonGroup<Pointing.Direction>(action), row, startColumn + 1)
        }

        fun addSwapMainHandLine(grid: GridLayout, row: Int) {
            grid.addChild(textLabel(Component.translatable("asc.screen.swap_main_hand")), row, 0)
            grid.addChild(buttonGroup<StandUtility.SwapTarget>(StandUtility.swap_main_hand), row, 1)
        }

        fun addMirrorLine(action: StandUtility, grid: GridLayout, row: Int) {
            grid.addChild(label("$action"), row, 0)
            grid.addChild(buttonGroup<StandUtility.MirrorDirection>(action), row, 1)
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
                        val layout = GridLayout().spacing(GAP)
                        layout.defaultCellSetting().alignHorizontallyLeft().alignVerticallyMiddle()
                        addSwapMainHandLine(layout, 0)
                        addMirrorLine(StandUtility.mirror_arms, layout, 1)
                        addMirrorLine(StandUtility.mirror_legs, layout, 2)
                        innerFlow.addChild(layout)
                    }
                    .add { innerFlow -> innerFlow.addChild(button(StandUtility.flip, Unit)) }
                    .buildInto(flow, rebuild)
            }
            .buildInto(target, rebuild)
    }

    private fun tr() = Minecraft.getInstance().font

    private fun textLabel(text: Component): StringWidget = StringWidget(text, tr())

    private fun label(key: String): StringWidget = textLabel(Component.translatable("asc.screen.$key"))

    private fun button(
        trigger: ArmorStatuesTriggers, value: Any, key: String = "$trigger",
        renderer: ButtonRenderer? = null, tooltipKey: String? = null
    ): NineSliceButtonWidget {
        val btn = NineSliceButtonWidget.of(Component.translatable("asc.screen.$key"), renderer ?: ButtonTextures.DEFAULT_RENDERER) {
            trigger.accept(value)
        }
        addTooltip(btn, tooltipKey)
        return btn
    }

    private fun buttonGroup(btnProperties: Collection<ButtonProperties>): LinearLayout {
        val layout = LinearLayout.horizontal().spacing(GAP)
        layout.defaultCellSetting().alignVerticallyMiddle()
        for (btnProperty in btnProperties) {
            layout.addChild(button(btnProperty.action, btnProperty.value, btnProperty.key, btnProperty.renderer))
        }

        return layout
    }

    private inline fun <reified E : Enum<E>> buttonGroup(action: ArmorStatuesTriggers): LinearLayout {
        return buttonGroup(E::class.java.enumConstants.map { e -> BasicButtonProperties(action, e, "$e") })
    }

    private fun angleStepGroup(): LinearLayout {
        val layout = LinearLayout.horizontal().spacing(GAP)
        layout.defaultCellSetting().alignVerticallyMiddle()
        layout.addChild(button(Rotation.set_step_angle, Rotation.StepAngles.deg_1, "deg_1"))
        layout.addChild(button(Rotation.set_step_angle, Rotation.StepAngles.deg_5, "deg_5"))
        layout.addChild(button(Rotation.set_step_angle, Rotation.StepAngles.deg_15, "deg_15"))
        layout.addChild(button(Rotation.set_step_angle, Rotation.StepAngles.deg_45, "deg_45"))

        return layout
    }

    private fun positionGroup(action: Position, infoKey: String? = null, warningKey: String? = null): LinearLayout {
        val layout = LinearLayout.horizontal().spacing(GAP)
        layout.defaultCellSetting().alignVerticallyMiddle()
        if (action == Position.x || action == Position.y || action == Position.z) {
            layout.addChild(button(action, Position.Offset.negative_8, "negative_8"))
            layout.addChild(button(action, Position.Offset.negative_3, "negative_3"))
            layout.addChild(button(action, Position.Offset.negative_1, "negative_1"))
        } else {
            layout.addChild(button(action, Position.AlignedExactOffset.negative_8, "negative_8"))
            layout.addChild(button(action, Position.AlignedExactOffset.negative_3, "negative_3"))
            layout.addChild(button(action, Position.AlignedExactOffset.negative_1, "negative_1"))
        }
        layout.addChild(textLabel(Component.literal("-")))
        if (action == Position.x || action == Position.y || action == Position.z) {
            layout.addChild(button(action, Position.Offset.positive_1, "positive_1"))
            layout.addChild(button(action, Position.Offset.positive_3, "positive_3"))
            layout.addChild(button(action, Position.Offset.positive_8, "positive_8"))
        } else {
            layout.addChild(button(action, Position.AlignedExactOffset.positive_1, "positive_1"))
            layout.addChild(button(action, Position.AlignedExactOffset.positive_3, "positive_3"))
            layout.addChild(button(action, Position.AlignedExactOffset.positive_8, "positive_8"))
        }

        if (infoKey != null) {
            layout.addChild(getInfo(infoKey))
        }

        addWarning(layout, warningKey)

        return layout
    }

    private fun rotationGroup(): LinearLayout {
        val layout = LinearLayout.horizontal().spacing(GAP)
        layout.defaultCellSetting().alignVerticallyMiddle()
        layout.addChild(button(Rotation.rotate, Rotation.Direction.left, "left"))
        layout.addChild(button(Rotation.rotate, Rotation.Direction.right, "right"))
        layout.addChild(button(Rotation.rotate, Rotation.Direction.toward, "toward"))
        layout.addChild(button(Rotation.rotate, Rotation.Direction.away, "away"))
        return layout
    }

    private fun adjustmentGroup(action: Rotation): LinearLayout {
        val layout = LinearLayout.horizontal().spacing(GAP)
        layout.defaultCellSetting().alignVerticallyMiddle()
        layout.addChild(button(action, Rotation.AxisDirection.negative_x, "negative_x"))
        layout.addChild(button(action, Rotation.AxisDirection.positive_x, "positive_x"))
        layout.addChild(textLabel(Component.literal("-")))
        layout.addChild(button(action, Rotation.AxisDirection.negative_y, "negative_y"))
        layout.addChild(button(action, Rotation.AxisDirection.positive_y, "positive_y"))
        layout.addChild(textLabel(Component.literal("-")))
        layout.addChild(button(action, Rotation.AxisDirection.negative_z, "negative_z"))
        layout.addChild(button(action, Rotation.AxisDirection.positive_z, "positive_z"))
        return layout
    }

    private fun addTooltip(component: AbstractWidget, key: String?) {
        if (key != null) {
            component.setTooltip(Tooltip.create(Component.translatable("asc.screen.tooltip.$key")))
        }
    }

    private fun getInfo(key: String?): StringWidget {
        val infoLbl = textLabel(Component.literal("🛈").withColor(INFO_COLOR))
        addTooltip(infoLbl, key)
        return infoLbl
    }

    private fun addWarning(layout: LinearLayout, key: String?) {
        if (key != null) {
            val warningLbl = textLabel(Component.literal("⚠").withColor(WARNING_COLOR))
            addTooltip(warningLbl, key)
            layout.addChild(warningLbl)
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

        fun buildInto(target: LinearLayout, rebuild: () -> Unit) {
            val layout = create()
            populate(layout)

            val section = CollapsibleSection(key, Component.translatable("asc.screen.$key"), defaultExpanded, tooltipKey) { layout }
            section.addTo(target, rebuild)
        }

        protected abstract fun create(): Layout
        protected abstract fun populate(layout: Layout)
    }

    private class GridToolboxBuilder(key: String, private val columns: Int = 2, defaultExpanded: Boolean = true, tooltipKey: String? = null) :
        ToolboxBuilder<(GridLayout, Int) -> Unit>(key, defaultExpanded, tooltipKey) {
        override fun create(): Layout {
            val layout = GridLayout().spacing(GAP)
            layout.defaultCellSetting().alignHorizontallyLeft().alignVerticallyMiddle()
            return layout
        }

        override fun populate(layout: Layout) {
            val grid = layout as GridLayout
            for (row in consumerList.indices) {
                consumerList[row](grid, row)
            }
        }
    }

    private class FlowToolboxBuilder(key: String, defaultExpanded: Boolean = true, tooltipKey: String? = null) :
        ToolboxBuilder<(LinearLayout) -> Unit>(key, defaultExpanded, tooltipKey) {
        override fun create(): Layout {
            val layout = LinearLayout.vertical().spacing(GAP)
            layout.defaultCellSetting().alignHorizontallyLeft()
            return layout
        }

        override fun populate(layout: Layout) {
            val flow = layout as LinearLayout
            for (consumer in consumerList) {
                consumer(flow)
            }
        }
    }
}
