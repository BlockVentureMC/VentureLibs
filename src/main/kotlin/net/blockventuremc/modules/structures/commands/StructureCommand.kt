package net.blockventuremc.modules.structures.commands

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.sendError
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.extensions.sendSuccess
import net.blockventuremc.extensions.teleportAsyncWithPassengers
import net.blockventuremc.modules.structures.RootAttachment
import net.blockventuremc.modules.structures.StructureManager
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
                        sender.sendSuccess("Currently selected structure is ${selectedStructure!!.name} (type ${selectedStructure!!.javaClass.simpleName})")
                        sender.teleportAsyncWithPassengers(selectedStructure!!.bukkitLocation)
                        return true
                    }
                    sender.sendError("Please select a structure first.")
                    return true
                }
            }
            "selectnearest" -> {
                performSelectNearest(sender)
                return true
            }
            "animate" -> {
                if (selectedStructure == null) {
                    sender.sendError("Please select a structure first.")
                    return true
                }
                if(selectedStructure!!.animation == null) {
                    sender.sendError("this structure does not have an animation :(")
                    return true
                }
                if(selectedStructure!!.animation!!.animationMap.isEmpty()) {
                    sender.sendError("This animation does not have custom animation Fields.")
                    return true
                }
                if (args.size == 1) {
                    sender.sendMessagePrefixed("Usage: /structure animate <field> <value>")
                    return true
                }
                if(args.size == 2) {
                    val fieldValue = selectedStructure!!.animation!!.animationMap[args[1]]
                    if(fieldValue != null) {
                        sender.sendMessagePrefixed("field ${args[1]} has value $fieldValue")
                        return true
                    }

                }
                performAnimate(sender, args[1], args[2])
                return true
            }
        }
        sender.sendMessagePrefixed("Usage: /structure <selectnearest/animate>")
        return true
    }

    private fun performSelectNearest(sender: Player) {

        var lastDistance = 10.0
        var foundStructure = false
        StructureManager.structures.values.forEach { structure ->
            if(structure.bukkitLocation.world == sender.location.world) {
                val distance = structure.bukkitLocation.distance(sender.location)
                if (distance < lastDistance) {
                    selectedStructure = structure
                    foundStructure = true
                    lastDistance = distance
                }
            }
        }
        if(!foundStructure) {
            StructureManager.vehicles.values.forEach { structure ->
                if(structure.bukkitLocation.world == sender.location.world) {
                    val distance = structure.bukkitLocation.distance(sender.location)
                    if (distance < lastDistance) {
                        selectedStructure = structure
                        foundStructure = true
                        lastDistance = distance
                    }
                }
            }
        }
        if(!foundStructure) {
            StructureManager.trains.values.forEach { structure ->
                var location = structure.carts.first().bukkitLocation
                if(location.world == sender.location.world) {
                    val distance = location.distance(sender.location)
                    if (distance < lastDistance) {
                        selectedStructure = structure.carts.first()
                        foundStructure = true
                        lastDistance = distance
                    }
                }
            }
        }
        if(foundStructure) {
            sender.sendSuccess("Selected structure ${selectedStructure!!.name} (type ${selectedStructure!!.javaClass.simpleName})")
        } else {
            sender.sendError("No structure found nearby.")
        }
    }

    private fun performAnimate(sender: Player, animationField: String, value: String) {

        selectedStructure!!.animation?.let { animation ->

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
            1 -> listOf("select", "animate", "selectnearest").filter { it.startsWith(args[0]) }
            2 -> if(args[0] == "animate") selectedStructure?.animation?.animationMap?.keys?.toList()?.filter { it.startsWith(args[1]) } ?: emptyList() else emptyList()
            else -> emptyList()
        }
    }

}