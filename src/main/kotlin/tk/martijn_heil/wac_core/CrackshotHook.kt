/*
 *     wac-core
 *     Copyright (C) 2016 Martijn Heil
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tk.martijn_heil.wac_core

import com.comphenix.packetwrapper.WrapperPlayServerEntityEquipment
import com.comphenix.packetwrapper.WrapperPlayServerSetSlot
import com.comphenix.packetwrapper.WrapperPlayServerWindowItems
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot.HEAD
import com.shampaggon.crackshot.events.WeaponScopeEvent
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import tk.martijn_heil.wac_core.CustomResourcePackModule.hasCustomResourcePack
import java.util.*
import java.util.logging.Logger

object CrackshotHook {
    lateinit private var plugin: Plugin
    lateinit private var logger: Logger
    private val scopedPlayers = ArrayList<Player>()
    private val weaponTitles: Array<String> = emptyArray()

    fun init(plugin: Plugin, logger: Logger, protocolManager: ProtocolManager) {
        this.plugin = plugin
        this.logger = logger
        plugin.server.pluginManager.registerEvents(CrackshotHookListener, plugin)

        protocolManager.addPacketListener(object : PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_EQUIPMENT) {
            override fun onPacketSending(e: PacketEvent?) {
                if (e == null) return
                if (e.isCancelled) return
                e.isReadOnly = false

                val packet = WrapperPlayServerEntityEquipment(e.packet)
                e.isCancelled = packet.slot == HEAD && scopedPlayers.contains(e.player)
            }
        })

        protocolManager.addPacketListener(object : PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.SET_SLOT) {
            override fun onPacketSending(e: PacketEvent?) {
                if (e == null) return
                if (e.isCancelled) return
                e.isReadOnly = false

                val packet = WrapperPlayServerSetSlot(e.packet)
                e.isCancelled = packet.slot == 5 && scopedPlayers.contains(e.player) && (packet.windowId == 0 || packet.windowId == -2)
            }
        })

        protocolManager.addPacketListener(object : PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.WINDOW_ITEMS) {
            override fun onPacketSending(e: PacketEvent?) {
                if (e == null) return
                if (e.isCancelled) return
                e.isReadOnly = false

                val packet = WrapperPlayServerWindowItems(e.packet)
                if(scopedPlayers.contains(e.player) && (packet.windowId == 0 || packet.windowId == -2)) {
                    val newSlotData = ArrayList(packet.slotData)
                    newSlotData[5] = ItemStack(Material.PUMPKIN, 1)
                    packet.slotData = newSlotData
                }
            }
        })
    }

    private object CrackshotHookListener : Listener {
        @EventHandler(ignoreCancelled = true, priority = MONITOR)
        fun onPlayerScope(e: WeaponScopeEvent) {
            if(!hasCustomResourcePack(e.player) && e.player.name != "Ninjoh") return

            if(e.isZoomIn && weaponTitles.contains(e.weaponTitle)) {
                val packet4 = WrapperPlayServerSetSlot(PacketContainer(PacketType.Play.Server.SET_SLOT))
                packet4.slot = 5 // 5 for the head
                packet4.slotData = ItemStack(Material.PUMPKIN, 1)
                packet4.windowId = 0
                packet4.sendPacket(e.player)
                scopedPlayers.add(e.player) // Important to do this last!
            } else if(weaponTitles.contains(e.weaponTitle)) {
                scopedPlayers.remove(e.player) // Important to do this first!
                val packet4 = WrapperPlayServerSetSlot(PacketContainer(PacketType.Play.Server.SET_SLOT))
                packet4.slot = 5 // 5 for the head
                packet4.slotData = e.player.inventory.helmet
                packet4.windowId = 0
                packet4.sendPacket(e.player)
            }
        }
    }
}