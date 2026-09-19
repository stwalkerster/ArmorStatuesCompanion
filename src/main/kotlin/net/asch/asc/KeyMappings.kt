package net.asch.asc

import net.asch.asc.as_datapack.ArmorStatuesHelper
import net.asch.asc.as_datapack.triggers.Utility
import net.asch.asc.ui.screen.MainScreen
import net.asch.asc.ui.screen.PosePresetScreen
import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import java.util.function.Consumer

object KeyMappings {

    private val ASC_CATEGORY = KeyMapping.Category(
        Identifier.fromNamespaceAndPath("armor_statues_companion", "key_category")
    )

    private val OPEN_MAIN_GUI = KeyMapping(
        "asc.key.open_main_gui",
        InputConstants.Type.KEYBOARD,
        InputConstants.KEY_COMMA,
        ASC_CATEGORY
    )

    private val OPEN_POSE_PRESET_GUI = KeyMapping(
        "asc.key.open_pose_preset_gui",
        InputConstants.Type.KEYBOARD,
        -1,
        ASC_CATEGORY
    )

    private val HIGHLIGHT_ARMOR_STAND = KeyMapping(
        "asc.key.highlight_armor_stand",
        InputConstants.Type.KEYBOARD,
        -1,
        ASC_CATEGORY
    )

    private val MAKE_ITEM_FRAME_INVISIBLE = KeyMapping(
        "asc.key.make_item_frame_invisible",
        InputConstants.Type.KEYBOARD,
        -1,
        ASC_CATEGORY
    )

    fun register() {
        KeyMappingHelper.registerKeyMapping(OPEN_MAIN_GUI)
        KeyMappingHelper.registerKeyMapping(OPEN_POSE_PRESET_GUI)
        KeyMappingHelper.registerKeyMapping(HIGHLIGHT_ARMOR_STAND)
        KeyMappingHelper.registerKeyMapping(MAKE_ITEM_FRAME_INVISIBLE)

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            executeKeyMapping(client, OPEN_MAIN_GUI, ::onOpenMainGUI)
            executeKeyMapping(client, OPEN_POSE_PRESET_GUI, ::onOpenPosePresetGUI)
            executeKeyMapping(client, HIGHLIGHT_ARMOR_STAND, ::onHighlightArmorStand)
            executeKeyMapping(client, MAKE_ITEM_FRAME_INVISIBLE, ::onMakeItemFrameInvisible)
        }
    }

    private fun executeKeyMapping(
        client: Minecraft,
        keyMapping: KeyMapping,
        consumer: Consumer<Minecraft>
    ) {
        while (keyMapping.consumeClick()) {
            consumer.accept(client)
        }
    }

    private fun onOpenMainGUI(client: Minecraft) {
        client.setScreenAndShow(MainScreen())
    }

    private fun onOpenPosePresetGUI(client: Minecraft) {
        client.setScreenAndShow(PosePresetScreen())
    }

    private fun onHighlightArmorStand(client: Minecraft) {
        Utility.highlight.accept(Unit)
    }

    private fun onMakeItemFrameInvisible(client: Minecraft) {
        ArmorStatuesHelper.makeItemFrameInvisible(client)
    }
}
