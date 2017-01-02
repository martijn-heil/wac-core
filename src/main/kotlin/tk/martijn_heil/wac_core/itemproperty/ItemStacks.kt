/*
 * MIT License
 *
 * Copyright (c) 2016 Martijn Heil
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.bukkit.inventory.ItemStack;

/**
 * Check if an [ItemStack] is soulbound.

 * @param item The [ItemStack] to check.
 * *
 * @return Returns true if the [ItemStack] is soulbound.
 * *
 */
fun isSoulBound(item: ItemStack): Boolean {
    return item.itemMeta != null && item.itemMeta.lore != null &&
            item.itemMeta.lore.contains("§6§oSoulbound");
}


/**
 * Check if an [ItemStack] is unbreakable.

 * @param item The [ItemStack] to check.
 * *
 * @return true if this [ItemStack] is unbreakable.
 * *
 */
fun isUnbreakable(item: ItemStack): Boolean {
    return item.itemMeta.lore != null && item.itemMeta.lore != null &&
            item.itemMeta.lore.contains("§6§oUnbreakable");
}


/**
 * Check if an [ItemStack] is use allowed.

 * @param item The [ItemStack] to check.
 * *
 * @return true if the [ItemStack] is use allowed.
 * *
 */
fun isUseAllowed(item: ItemStack): Boolean {
    return item.itemMeta.lore != null && item.itemMeta.lore != null &&
            item.itemMeta.lore.contains("§6§oUse-Allowed");
}


/**
 * Check if an [ItemStack] is combat allowed.

 * @param item The [ItemStack] to check.
 * *
 * @return true if the [ItemStack] is combat allowed.
 * *
 */
fun isCombatAllowed(item: ItemStack): Boolean {
    return item.itemMeta.lore != null && item.itemMeta.lore != null &&
            item.itemMeta.lore.contains("§6§oCombat-Allowed");
}


/**
 * Check if an [ItemStack] is equip allowed.

 * @param item The [ItemStack] to check.
 * *
 * @return true if the [ItemStack] is equip allowed.
 * *
 */
fun isEquipAllowed(item: ItemStack): Boolean {
    return item.itemMeta.lore != null && item.itemMeta.lore != null &&
            item.itemMeta.lore.contains("§6§oEquip-Allowed");
}


/**
 * Check if an [ItemStack] is consume allowed.

 * @param item The [ItemStack] to check.
 * *
 * @return true if the [ItemStack] is consume allowed.
 * *
 */
fun isConsumeAllowed(item: ItemStack): Boolean {
    return item.itemMeta.lore != null && item.itemMeta.lore != null &&
            item.itemMeta.lore.contains("§6§oConsume-Allowed");
}


/**
 * Check if an item is part of a given kit. The item is known to be part of a certain kit if the lore contains
 * &#39;§b§oKitNameHere&#39;

 * @param item    The [ItemStack] to check.
 * *
 * @param kitName The name of the kit to check if this item is part of it.
 * *
 * @return true if the item is part of this kit.
 * *
 * @throws NullPointerException if kitName is null.
 */
fun isPartOfKit(item: ItemStack?, kitName: String): Boolean {
    return item != null && item.itemMeta.lore != null && item.itemMeta.lore != null &&
            item.itemMeta.lore.contains("§b§o" + kitName);
    // NOTE: §b instead of §6
}
