package net.asch.asc.as_datapack

import net.minecraft.client.Minecraft
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack

object ArmorStatuesHelper {
    const val AS_TRIGGER_SCOREBOARD_OBJECTIVE = "as_trigger"

    // Use Identifiers directly instead of raw Strings
    enum class WandTypes(val expectedItemId: Identifier, val triggerValue: String) {
        adjustment(Identifier.fromNamespaceAndPath("minecraft", "warped_fungus_on_a_stick"), "adjustment_wand"),
        pointer(Identifier.fromNamespaceAndPath("minecraft", "stick"), "pointer_wand")
    }

    fun trigger(client: Minecraft, actionId: Int) {
        sendCommandToServer(client, "trigger $AS_TRIGGER_SCOREBOARD_OBJECTIVE set $actionId")
    }


    fun makeItemFrameInvisible(client: Minecraft) {
        sendCommandToServer(client, "trigger if_invisible")
    }

    fun repeat(client: Minecraft) {
        sendCommandToServer(client, "trigger as_repeat set 1")
    }

    fun craftWand(client: Minecraft, wandType: WandTypes) {
        if (!canCraftWand(client, wandType)) {
            val expectedItem = BuiltInRegistries.ITEM.getValue(wandType.expectedItemId)
            client.player?.sendSystemMessage(
                Component.translatable(
                    "asc.chat.cant_craft_wand",
                    Component.translatable("asc.item.${wandType.name.lowercase()}"),
                    expectedItem.getName(ItemStack(expectedItem))
                )
            )
            return
        }

        // Trigger the datapack action
        sendCommandToServer(client, "trigger ${wandType.triggerValue}")
    }

    private fun canCraftWand(client: Minecraft, wandType: WandTypes): Boolean {
        val player = client.player ?: return false
        val expectedItem = BuiltInRegistries.ITEM.getValue(wandType.expectedItemId)

        fun matches(stack: ItemStack): Boolean =
            !stack.isEmpty && stack.item == expectedItem && stack.componentsPatch.isEmpty

        // Do NOT use player.inventory.main (it's private in your mappings).
        val inv = player.inventory
        val size = inv.containerSize
        for (slot in 0 until size) {
            if (matches(inv.getItem(slot))) return true
        }

        if (matches(player.mainHandItem)) return true
        if (matches(player.offhandItem)) return true

        return false
    }

    /**
     * Sends a command as if typed in chat.
     */
    private fun sendCommandToServer(client: Minecraft, command: String) {
        val handler = client.connection ?: return
        handler.sendCommand(command)
    }
}
