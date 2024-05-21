package net.blockventuremc.modules.discord.annotations

/**
 * Represents the SlashCommand annotation used to annotate classes that define slash commands.
 *
 * @property name The name of the slash command.
 * @property description The description of the slash command.
 * @property permissionScope The permission scope required to use the slash command. Default value is [PermissionScope.USER].
 */
annotation class SlashCommand(
    val name: String,
    val description: String,
    val permissionScope: PermissionScope = PermissionScope.USER
)