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

package tk.martijn_heil.wac_core.classes

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack


enum class PlayerClasses : PlayerClass {
    PIRATE_CAPTAIN() {
        override fun giveKit(p: Player) {

        }
        override val playerClassName = "pirate captain"
    },

    PIRATE() {
        override fun giveKit(p: Player) {

        }

        override val playerClassName = "pirate"
    },

    DOCTOR() {
        override fun giveKit(p: Player) {

        }
        override val playerClassName = "doctor"
    },

    SHARPSHOOTER() {
        override fun giveKit(p: Player) {

        }
        override val playerClassName = "sharpshooter"
    },

    OFFICER() {
        override fun giveKit(p: Player) {

        }
        override val playerClassName = "officer"
    },

    GRENADIER() {
        override fun giveKit(p: Player) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "shot give " + p.name + " grenade")

            val sabre = ItemStack(Material.IRON_SWORD)
            sabre.addEnchantment(Enchantment.DAMAGE_ALL, 1)
            p.inventory.addItem(sabre)

            val leatherHelmet = ItemStack(Material.LEATHER_HELMET)
            leatherHelmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
            p.inventory.addItem(leatherHelmet)

            val ironChestplate = ItemStack(Material.LEATHER_CHESTPLATE)
            ironChestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
            p.inventory.addItem(ironChestplate)

            val leatherLeggings = ItemStack(Material.LEATHER_LEGGINGS)
            leatherLeggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
            p.inventory.addItem(leatherLeggings)

            val ironBoots = ItemStack(Material.IRON_BOOTS)
            ironBoots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
            p.inventory.addItem(ironBoots)
        }
        override val playerClassName = "grenadier"
    },

    MUSKETEER() {
        override fun giveKit(p: Player) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "shot give " + p.name + " musket")

            val bayonet = ItemStack(Material.STONE_SWORD)
            bayonet.addEnchantment(Enchantment.DAMAGE_ALL, 1)
            bayonet.itemMeta.displayName = "bayonet"
            p.inventory.addItem(bayonet)

            val leatherHelmet = ItemStack(Material.LEATHER_HELMET)
            leatherHelmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
            p.inventory.addItem(leatherHelmet)

            val leatherChestplate = ItemStack(Material.LEATHER_CHESTPLATE)
            leatherChestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
            p.inventory.addItem(leatherChestplate)

            val chainmailLeggings = ItemStack(Material.CHAINMAIL_LEGGINGS)
            chainmailLeggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
            p.inventory.addItem(chainmailLeggings)

            val ironBoots = ItemStack(Material.IRON_BOOTS)
            ironBoots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
            p.inventory.addItem(ironBoots)
        }
        override val playerClassName = "musketeer"
    },

    CIVILIAN() {
        override fun giveKit(p: Player) {
            val knife = ItemStack(Material.WOOD_SWORD)
            knife.addEnchantment(Enchantment.DAMAGE_ALL, 1)
            knife.itemMeta.displayName = "knife"

            p.inventory.addItem(knife)
            p.inventory.addItem(ItemStack(Material.LEATHER_CHESTPLATE))
            p.inventory.addItem(ItemStack(Material.LEATHER_BOOTS))
        }
        override val playerClassName = "civilian"
    }
}