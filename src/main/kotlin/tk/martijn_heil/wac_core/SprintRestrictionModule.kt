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

import org.bukkit.plugin.Plugin
import java.util.*
import java.util.logging.Logger


object SprintRestrictionModule : AutoCloseable {

    lateinit var plugin: Plugin
    lateinit var logger: Logger
    val DECREASE_PER_SECOND_BASE_VALUE = 1
    val INCREASE_PER_SECOND_BASE_VALUE = 1
    val playerMap = HashMap<UUID, Pair<Int /* increase */, Int /* decrease */>>()
    var task1: Int = 0
    var task2: Int = 0

    fun init(plugin: Plugin, logger: Logger) {
        this.plugin = plugin
        this.logger = logger
        logger.info("Initializing SprintRestrictionModule..")

        task1 = plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, {
            plugin.server.onlinePlayers.forEach {
                if(it.isSprinting && it.foodLevel > 0) {
                    val decrease = playerMap[it.uniqueId]?.second ?: DECREASE_PER_SECOND_BASE_VALUE
                    var newLevel = it.foodLevel - decrease
                    if(newLevel < 0) newLevel = 0 // prevent underflow
                    it.foodLevel = newLevel
                }
            }
        }, 0, 20)
        if(task1 == -1) {
            logger.severe("Could not schedule first task.")
            return
        }


        task2 = plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, {
            plugin.server.onlinePlayers.forEach {
                if(!it.isSprinting && it.foodLevel < 20) {
                    val increase = playerMap[it.uniqueId]?.first ?: INCREASE_PER_SECOND_BASE_VALUE
                    var newLevel = it.foodLevel + increase
                    if(newLevel > 20) newLevel = 20 // prevent overflow
                    it.foodLevel = newLevel
                }
            }
        }, 0, 20)
        if(task2 == -1) {
            logger.severe("Could not schedule second task.")
            plugin.server.scheduler.cancelTask(task1)
            return
        }
    }


    override fun close() {
        plugin.server.scheduler.cancelTask(task1)
        plugin.server.scheduler.cancelTask(task2)
    }
}