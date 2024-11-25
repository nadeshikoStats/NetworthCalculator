/*
 * This file is a part of the nadeshiko project. nadeshiko is free software, licensed under the MIT license.
 *
 * Usage of these works (including, yet not limited to, reuse, modification, copying, distribution, and selling) is
 * permitted, provided that the relevant copyright notice and permission notice (as specified in LICENSE) shall be
 * included in all copies or substantial portions of this software.
 *
 * These works are provided "AS IS" with absolutely no warranty of any kind, either expressed or implied.
 *
 * You should have received a copy of the MIT License alongside this software; refer to LICENSE for information.
 * If not, refer to https://mit-license.org.
 */

package io.nadeshiko.networth;

import com.google.gson.JsonObject;
import lombok.Data;

import java.text.DecimalFormat;

/**
 * Container for a player's networth, broken down into sections
 */
@Data
public class Networth {

    /**
     * Used to pretty-print this Networth instance for debugging purposes
     */
    public static final DecimalFormat formatter = new DecimalFormat("#,###");

    /**
     * The UUID for which this Networth belongs to
     */
    private final String uuid;

    // liquid
    private double purse;
    private double bank;

    // bags
    private double sacks;
    private double accessories;
    private double fishingBag;
    private double quiver;
    private double potionBag;

    // armor
    private double activeArmor;
    private double activeEquipment;
    private double wardrobe;

    // items
    private double inventory;
    private double storage;
    private double enderChest;
    private double vault;

    // other
    private double pets;
    private double essence;
    private double museum;

    /**
     * Create a new Networth instance with default (zero) values
     * @param uuid The UUID of the player that this Networth instance represents
     */
    Networth(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return The liquid networth of this player
     */
    public double getLiquid() {
        return this.purse + this.bank;
    }

    /**
     * @return The networth this player has in bags. This contains sacks, accessories, the fishing bag,
     * the quiver, and the potion bag.
     */
    public double getBags() {
        return this.sacks + this.accessories + this.fishingBag + this.quiver + this.potionBag;
    }

    /**
     * @return The networth this player has in their armor. This includes the wardrobe, as well as the player's
     * active armor and equipment.
     */
    public double getArmor() {
        return this.activeArmor + this.activeEquipment + this.wardrobe;
    }

    /**
     * @return The networth this player has in other items. This includes the inventory, ender chest, backpacks,
     * and personal vault.
     */
    public double getItems() {
        return this.inventory + this.storage + this.enderChest + this.vault;
    }

    /**
     * @return The total networth of this player
     */
    public double getTotal() {
        return this.getLiquid() + this.getBags() + this.getArmor() + this.getItems() +
            this.pets + this.essence + this.museum;
    }

    /**
     * @return A formatted summary of this Networth instance
     */
    @Override
    public String toString() {
        return uuid + "'s Networth: " + formatter.format(this.getTotal()) + " coins";
    }

    /**
     * @return A detailed and formatted breakdown of this Networth instance
     */
    public String getBreakdown() {
        return uuid + "'s Networth: " + formatter.format(this.getTotal()) + " coins" +
            "\n\nLiquid: " + formatter.format(this.getLiquid()) +
            "\n - Purse: " + formatter.format(this.getPurse()) +
            "\n - Bank: " + formatter.format(this.getBank()) +
            "\n\nBags: " + formatter.format(this.getBags()) +
            "\n - Sacks: " + formatter.format(this.getSacks()) +
            "\n - Accessories: " + formatter.format(this.getAccessories()) +
            "\n - Fishing Bag: " + formatter.format(this.getFishingBag()) +
            "\n - Quiver: " + formatter.format(this.getQuiver()) +
            "\n - Potion Bag: " + formatter.format(this.getPotionBag()) +
            "\n\nArmor: " + formatter.format(this.getArmor()) +
            "\n - Active Armor: " + formatter.format(this.getActiveArmor()) +
            "\n - Active Equipment: " + formatter.format(this.getActiveEquipment()) +
            "\n - Wardrobe: " + formatter.format(this.getWardrobe()) +
            "\n\nItems: " + formatter.format(this.getItems()) +
            "\n - Inventory: " + formatter.format(this.getInventory()) +
            "\n - Storage: " + formatter.format(this.getStorage()) +
            "\n - Ender Chest: " + formatter.format(this.getEnderChest()) +
            "\n - Vault: " + formatter.format(this.getVault()) +
            "\n\nOther: " + formatter.format(this.pets + this.essence + this.museum) +
            "\n - Pets: " + formatter.format(this.getPets()) +
            "\n - Essence: " + formatter.format(this.getEssence()) +
            "\n - Museum: " + formatter.format(this.getMuseum());
    }

    /**
     * @return This Networth object, serialized as a {@code JsonObject}
     */
    public JsonObject serialize() {
        JsonObject serialized = new JsonObject();

        serialized.addProperty("calculator", "nadeshiko calculator " + NetworthCalculator.VERSION);
        serialized.addProperty("owner", this.uuid);
        serialized.addProperty("total", this.getTotal());

        JsonObject liquid = new JsonObject();
        liquid.addProperty("total", this.getLiquid());
        liquid.addProperty("purse", this.purse);
        liquid.addProperty("bank", this.bank);
        serialized.add("liquid", liquid);

        JsonObject bags = new JsonObject();
        bags.addProperty("total", this.getBags());
        bags.addProperty("sacks", this.sacks);
        bags.addProperty("accessories", this.accessories);
        bags.addProperty("fishing_bag", this.fishingBag);
        bags.addProperty("quiver", this.quiver);
        bags.addProperty("potion_bag", this.potionBag);
        serialized.add("bags", bags);

        JsonObject armor = new JsonObject();
        armor.addProperty("total", this.getArmor());
        armor.addProperty("active_armor", this.activeArmor);
        armor.addProperty("active_equipment", this.activeEquipment);
        armor.addProperty("wardrobe", this.wardrobe);
        serialized.add("armor", armor);

        JsonObject items = new JsonObject();
        items.addProperty("total", this.getItems());
        items.addProperty("inventory", this.inventory);
        items.addProperty("storage", this.storage);
        items.addProperty("ender_chest", this.enderChest);
        items.addProperty("vault", this.vault);
        serialized.add("items", items);

        JsonObject other = new JsonObject();
        armor.addProperty("total", this.pets + this.essence + this.museum);
        armor.addProperty("pets", this.pets);
        armor.addProperty("essence", this.essence);
        armor.addProperty("museum", this.museum);
        serialized.add("other", other);

        return serialized;
    }
}
