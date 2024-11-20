package dev.niqumu.networth;

import lombok.Data;

import java.text.DecimalFormat;

@Data
public class Networth {
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

    public String toString() {
        return uuid + "'s Networth: " + formatter.format(this.getTotal()) + " coins";
    }

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
}
