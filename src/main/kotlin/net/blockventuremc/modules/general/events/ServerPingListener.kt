package net.blockventuremc.modules.general.events

import dev.fruxz.stacked.text
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerListPingEvent

class ServerPingListener : Listener {

    /**
     * This function is called when a server list ping event occurs.
     * It sets the server description MOTD using the provided event information.
     *
     * @param event The ServerListPingEvent that triggered this function.
     */
    @EventHandler
    fun onServerPing(event: ServerListPingEvent): Unit = with(event) {
        motd(text("<color:#a4b0be>                 <gradient:#00a8ff:#1e90ff>BlockVenture</gradient>\n" +
                "      <gradient:#a4b0be:#6c768f>ᴡʜᴇʀᴇ ᴇᴠᴇʀʏ ʙʟᴏᴄᴋ ᴛᴇʟʟꜱ ᴀ ꜱᴛᴏʀʏ</gradient>"))
    }

    // Future todos:
    // - Translatable?
    // - Configurable?
}