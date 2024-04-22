package net.blockventuremc.annotations

import org.bukkit.permissions.PermissionDefault

@Retention(AnnotationRetention.RUNTIME)
annotation class BlockCommand(
    val name: String,
    val aliases: Array<String> = [],
    val description: String = "",
    val permission: String = "",
    val permissionDefault: PermissionDefault = PermissionDefault.OP,
    val usage: String = "",
)