package net.blockventuremc.modules.structures.commands

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.sendError
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.extensions.sendSuccess
import net.blockventuremc.modules.structures.RootAttachment
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
import kotlin.collections.set


var selectedStructure: RootAttachment? = null

@VentureCommand(
    name = "structure",
    description = "Structure management",
    permission = "blockventure.structure",
    permissionDefault = PermissionDefault.OP,
    usage = "/structure <select/animate>",
)
class StructureCommand : CommandExecutor, TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

        if (args.isEmpty()) {
            sender.sendMessagePrefixed("Usage: /structure <select/animate>")
            return true
        }

        when (args[0]) {
            "select" -> {
                if (args.size == 1) {
                    if(selectedStructure != null) {
                        sender.sendMessage("Currently selected structure is ${selectedStructure!!.name}")
                        return true
                    }

                    sender.sendMessagePrefixed("Usage: /structure select <name||uuid>")
                    return true
                }
            }
            "animate" -> {
                if (args.size < 3) {
                    if (selectedStructure == null) {
                        sender.sendError("Please select a structure first.")
                        return true
                    }
                    sender.sendMessagePrefixed("Usage: /structure animate <field> <value>")
                    return true
                }
                performAnimate(sender, args[1], args[2])
                return true
            }
        }
        sender.sendMessagePrefixed("Usage: /structure <select/animate>")
        return true
    }

    private fun performAnimate(sender: Player, animationField: String, value: String) {

        if (selectedStructure == null) {
            sender.sendError("Please select a structure first.")
            return
        }
        if(selectedStructure!!.animation == null) {
            sender.sendError("this structure does not have an animation")
            return
        }

        selectedStructure!!.animation?.let { animation ->

            if(animation.animationMap.isEmpty()) {
                sender.sendError("This animation does not have custom animation Fields.")
                return
            }

            val fieldValue = animation.animationMap[animationField]

            if (fieldValue == null) {
                sender.sendError("This animation does not have a field $animationField.")
                return
            }


            val isTypeMatching = when (fieldValue) {
                is Int -> value.toIntOrNull() != null
                is Float -> value.toFloatOrNull() != null
                is Double -> value.toDoubleOrNull() != null
                is String -> true
                else -> false
            }

            if (!isTypeMatching) {
                sender.sendError("value type ($value) does not match ${ fieldValue.javaClass.simpleName}.")
                return
            }

            when (fieldValue) {
                is Int -> {
                    val intValue = value.toInt()
                    animation.animationMap[animationField] = intValue
                }
                is Float -> {
                    val floatValue = value.toFloat()
                    animation.animationMap[animationField] = floatValue
                }
                is Double -> {
                    val doubleValue = value.toDouble()
                    animation.animationMap[animationField] = doubleValue
                }
                is String -> {
                    val stringValue = value
                    animation.animationMap[animationField] = stringValue
                }
            }
            sender.sendSuccess("Set animation field $animationField to $value")
        }

    }


    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> listOf("select", "animate").filter { it.startsWith(args[0]) }
            2 -> if(args[0] == "animate") selectedStructure?.animation?.animationMap?.keys?.toList()?.filter { it.startsWith(args[1]) } ?: emptyList() else emptyList()
            else -> emptyList()
        }
    }

}