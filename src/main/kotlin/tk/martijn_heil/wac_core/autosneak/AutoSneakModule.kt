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

package tk.martijn_heil.wac_core.autosneak

import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import com.sk89q.intake.Command
import com.sk89q.intake.Intake
import com.sk89q.intake.Require
import com.sk89q.intake.fluent.CommandGraph
import com.sk89q.intake.parametric.ParametricBuilder
import com.sk89q.intake.parametric.provider.PrimitivesModule
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import tk.martijn_heil.wac_core.command.CommandModule
import tk.martijn_heil.wac_core.command.common.Sender
import tk.martijn_heil.wac_core.command.common.Target
import tk.martijn_heil.wac_core.command.common.bukkit.BukkitAuthorizer
import tk.martijn_heil.wac_core.command.common.bukkit.provider.BukkitModule
import tk.martijn_heil.wac_core.command.common.bukkit.provider.sender.BukkitSenderModule
import tk.martijn_heil.wac_core.playerFromEntityId
import java.util.*
import java.util.logging.Logger


object AutoSneakModule {
    private val state = HashMap<UUID, Boolean>()
    private val lastFlags = HashMap<UUID, Byte>()
    lateinit private var protocolManager: ProtocolManager
    lateinit private var logger: Logger
    lateinit private var plugin: Plugin


    fun init(protocolManager: ProtocolManager, plugin: Plugin, logger: Logger) {
        this.protocolManager = protocolManager
        this.logger = logger
        this.plugin = plugin

        protocolManager.addPacketListener(object : PacketAdapter(plugin, ListenerPriority.MONITOR, PacketType.Play.Server.ENTITY_METADATA) {
            override fun onPacketSending(e: PacketEvent?) {
                if (e == null) return
                if (e.isCancelled) return
                e.isReadOnly = false

                val packet = WrapperPlayServerEntityMetadata(e.packet)
                val p = playerFromEntityId(packet.entityID) ?: return

                if(isAutoSneaking(p)) {
                    val watcher = WrappedDataWatcher(packet.metadata)
                    val flags = watcher.getByte(0)
                    if(flags != null) {
                        lastFlags.put(p.uniqueId, flags)

                        watcher.setObject(0, (flags.toInt() or 0x02).toByte(), true)
                        packet.metadata = watcher.watchableObjects
                        e.packet = packet.handle
                    }
                }
            }
        })


        logger.info("Building and registering commands..")
        val injector = Intake.createInjector()
        injector.install(PrimitivesModule())
        injector.install(BukkitModule(Bukkit.getServer()))
        injector.install(BukkitSenderModule())

        val builder = ParametricBuilder(injector)
        builder.authorizer = BukkitAuthorizer()


        val dispatcher = CommandGraph()
                .builder(builder)
                .commands()
                .group("sneak")
                .registerMethods(SneakCommands)
                .parent()
                .graph()
                .dispatcher

        CommandModule.registerDispatcher(dispatcher)
    }

    fun setAutoSneaking(op: OfflinePlayer, value: Boolean) {
        state.put(op.uniqueId, value)
        if(op.isOnline && op.player != null) notifyObservers(op.player, value)
    }

    fun isAutoSneaking(op: OfflinePlayer): Boolean {
        return state[op.uniqueId] ?: false
    }


    private fun notifyObservers(p: Player, newValue: Boolean) {
        var flags: Byte = lastFlags[p.uniqueId] ?: 0
        flags = if(newValue) (flags.toInt() or 0x02).toByte() else (flags.toInt() and 0b11111101).toByte()


        val watcher = WrappedDataWatcher.getEntityWatcher(p)
        val serializer = WrappedDataWatcher.Registry.get(Byte::class.java)
        val obj = WrappedDataWatcher.WrappedDataWatcherObject(0, serializer)
        watcher.setObject(obj, flags)


        val packet = WrapperPlayServerEntityMetadata(PacketContainer(PacketType.Play.Server.ENTITY_METADATA))
        packet.entityID = p.entityId
        packet.metadata = watcher.watchableObjects

        for (observer in protocolManager.getEntityTrackers(p)) {
            protocolManager.sendServerPacket(observer, packet.handle)
        }
    }

    private object SneakCommands {
        @Command(aliases = arrayOf("toggle"), desc = "Toggle sneak")
        @Require("wac-core.command.sneak.toggle")
        fun toggle(@Sender sender: CommandSender, @Target("wac-core.command.sneak.toggle.others") target: OfflinePlayer) {
            val newValue = !AutoSneakModule.isAutoSneaking(target)
            AutoSneakModule.setAutoSneaking(target, newValue)
            sender.sendMessage(target.name + if(newValue) " is now sneaking." else " is no longer sneaking.")
        }
    }
}
