/*
 * This file is part of RPGInventory.
 * Copyright (C) 2015-2017 Osip Fatkullin
 *
 * RPGInventory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RPGInventory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RPGInventory.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.rpginventory.item;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OsipXD on 18.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class CustomItem extends ClassedItem {
    // Required options
    private final String name;
    private final List<ItemStat> stats = new ArrayList<>();

    // Not required options
    private final List<String> lore;
    private final List<String> permissions;
    private final boolean drop;
    private final boolean unbreakable;
    private final boolean statsHidden;

    // Commands
    @Nullable
    private final ItemAction rightClickAction;
    @Nullable
    private final ItemAction leftClickAction;

    private ItemStack customItem;

    CustomItem(String id, @NotNull ConfigurationSection config) {
        super(config, config.getString("texture"));

        Rarity rarity = Rarity.valueOf(config.getString("rarity"));
        this.name = StringUtils.coloredLine(rarity.getColor() + config.getString("name"));

        if (config.contains("stats")) {
            for (String stat : config.getStringList("stats")) {
                this.stats.add(new ItemStat(ItemStat.StatType.valueOf(stat.split(" ")[0]), stat.split(" ")[1]));
            }
        }

        this.lore = config.contains("lore") ? StringUtils.coloredLines(config.getStringList("lore")) : null;

        this.leftClickAction = config.contains("abilities.left-click.command")
                ? new ItemAction(config.getConfigurationSection("abilities.left-click"))
                : null;
        this.rightClickAction = config.contains("abilities.right-click.command")
                ? new ItemAction(config.getConfigurationSection("abilities.right-click"))
                : null;

        this.permissions = config.contains("abilities.permissions") ? config.getStringList("abilities.permissions") : null;
        this.drop = config.getBoolean("drop", true);
        this.unbreakable = config.getBoolean("unbreakable", false);
        this.statsHidden = config.getBoolean("hide-stats", false);

        this.createItem(id);
    }

    @Contract("null -> false")
    public static boolean isCustomItem(ItemStack itemStack) {
        return !ItemUtils.isEmpty(itemStack) && ItemUtils.hasTag(itemStack, ItemUtils.ITEM_TAG);
    }

    public void onEquip(Player player) {
        if (this.permissions == null) {
            return;
        }

        InventoryManager.get(player).addPermissions(this.permissions);
    }

    public void onRightClick(Player player) {
        if (rightClickAction != null) {
            rightClickAction.doAction(player);
        }
    }

    public void onLeftClick(Player player) {
        if (leftClickAction != null) {
            leftClickAction.doAction(player);
        }
    }

    private void createItem(String id) {
        // Set texture
        ItemStack customItem = ItemUtils.getTexturedItem(this.texture);

        // Set lore and display name
        ItemMeta meta = customItem.getItemMeta();
        meta.setDisplayName(this.name);
        meta.setLore(ItemManager.buildLore(this));
        customItem.setItemMeta(meta);

        if (this.unbreakable) {
            customItem = ItemUtils.setTag(customItem, ItemUtils.UNBREAKABLE_TAG, "1");
        }

        customItem = ItemUtils.setTag(customItem, ItemUtils.HIDE_FLAGS_TAG, "63");
        this.customItem = ItemUtils.setTag(customItem, ItemUtils.ITEM_TAG, id);
    }

    ItemStack getItemStack() {
        return this.customItem;
    }

    @Nullable
    ItemStat getStat(ItemStat.StatType type) {
        for (ItemStat stat : this.stats) {
            if (stat.getType() == type) {
                return stat;
            }
        }

        return null;
    }

    public boolean isDrop() {
        return drop;
    }

    public List<String> getLore() {
        return lore;
    }

    boolean hasLeftClickCaption() {
        return this.leftClickAction != null && this.leftClickAction.getCaption() != null;
    }

    boolean hasRightClickCaption() {
        return this.rightClickAction != null && this.rightClickAction.getCaption() != null;
    }

    String getLeftClickCaption() {
        return this.leftClickAction == null ? "" : this.leftClickAction.getCaption();
    }

    String getRightClickCaption() {
        return this.rightClickAction == null ? "" : this.rightClickAction.getCaption();
    }

    List<ItemStat> getStats() {
        return stats;
    }

    boolean isStatsHidden() {
        return statsHidden;
    }

    boolean isUnbreakable() {
        return unbreakable;
    }

    @SuppressWarnings("unused")
    private enum Rarity {
        COMMON('7'),
        UNCOMMON('6'),
        RARE('9'),
        MYTHICAL('5'),
        LEGENDARY('d');

        private final char color;

        Rarity(char color) {
            this.color = color;
        }

        @Contract(pure = true)
        public String getColor() {
            return "&" + this.color;
        }
    }
}