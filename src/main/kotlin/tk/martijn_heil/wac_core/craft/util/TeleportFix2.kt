package tk.martijn_heil.wac_core.craft.util

import net.minecraft.server.v1_11_R1.EntityPlayer
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin

class TeleportFix2(private val plugin: Plugin) : Listener {
    private val server = plugin.server

    // Try increasing this. May be dependent on lag.
    private val TELEPORT_FIX_DELAY = 15 // ticks

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerTeleport(event: PlayerTeleportEvent) {

        val player = event.player
        val visibleDistance = server.viewDistance * 16

        // Fix the visibility issue one tick later
        server.scheduler.scheduleSyncDelayedTask(plugin, {
            // Refresh nearby clients
            updateEntities(getPlayersWithin(player, visibleDistance))
        }, TELEPORT_FIX_DELAY.toLong())
    }


    fun updateEntities(observers: List<Player>) {

        // Refresh every single player
        for (player in observers) {
            updateEntity(player, observers)
        }
    }

    fun updateEntity(entity: Entity, observers: List<Player>) {

        val world = entity.world
        val worldServer = (world as CraftWorld).handle

        val tracker = worldServer.tracker

        val nmsPlayers = getNmsPlayers(observers)

        // Force Minecraft to resend packets to the affected clients
        tracker.trackedEntities.get(entity.entityId)!!.trackedPlayers.removeAll(nmsPlayers)
        tracker.trackedEntities.get(entity.entityId)!!.scanPlayers(nmsPlayers)
        tracker.updatePlayers()
    }

    private fun getNmsPlayers(players: List<Player>): List<EntityPlayer> {
        val nsmPlayers = players.map { (it as CraftPlayer).handle }

        return nsmPlayers
    }

    private fun getPlayersWithin(player: Player, distance: Int): List<Player> {
        val d2 = distance * distance
        return server.onlinePlayers.filter { it.world === player.world && it.location.distanceSquared(player.location) <= d2 }
    }
}