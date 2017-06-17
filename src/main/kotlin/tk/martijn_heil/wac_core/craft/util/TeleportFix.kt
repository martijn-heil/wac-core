package tk.martijn_heil.wac_core.craft.util

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin
import java.util.*

class TeleportFix(private val plugin: Plugin) : Listener {
    private val server = plugin.server

    private val TELEPORT_FIX_DELAY = 5 // ticks

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerTeleport(event: PlayerTeleportEvent) {

        val player = event.player
        val visibleDistance = server.viewDistance * 16

        // Fix the visibility issue one tick later
        server.scheduler.scheduleSyncDelayedTask(plugin, {
            // Refresh nearby clients
            val nearby = getPlayersWithin(player, visibleDistance)

            // Hide every player
            updateEntities(nearby, false)

            // Then show them again
            server.scheduler.scheduleSyncDelayedTask(plugin, { updateEntities(nearby, true) }, 1)
        }, TELEPORT_FIX_DELAY.toLong())
    }

    private fun updateEntities(players: List<Player>, visible: Boolean) {

        // Hide every player
        for (observer in players) {
            for (player in players) {
                if (observer.entityId != player.entityId) {
                    if (visible) {
                        observer.showPlayer(player)
                    }
                    else {
                        observer.hidePlayer(player)
                    }
                }
            }
        }
    }

    private fun getPlayersWithin(player: Player, distance: Int): List<Player> {
        val res = ArrayList<Player>()
        val d2 = distance * distance

        for (p in server.onlinePlayers) {
            if (p.world === player.world && p.location.distanceSquared(player.location) <= d2) {
                res.add(p)
            }
        }

        return res
    }
}
