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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.nadeshiko.networth.exception.InvalidApiKeyException;
import io.nadeshiko.networth.item.ExoticManager;
import io.nadeshiko.networth.item.Gemstone;
import io.nadeshiko.networth.item.Item;
import io.nadeshiko.networth.market.AuctionHandler;
import io.nadeshiko.networth.market.BazaarHandler;
import io.nadeshiko.networth.util.InventoryUtil;
import io.nadeshiko.networth.exception.MalformedProfileException;
import io.nadeshiko.networth.exception.NoSuchProductException;
import io.nadeshiko.networth.item.GemstoneSlotType;
import lombok.Getter;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * The core class of the {@code NetworthCalculator} library.
 */
@Getter
@SuppressWarnings("unused") // it's a library, public API methods are not used by us
public class NetworthCalculator {

    /**
     * Global static library logger
     */
    public static final Logger LOGGER = LoggerFactory.getLogger("NetworthCalculator");

    /**
     * The Hypixel API key used by this instance
     */
    private final String apiKey;

    /**
     * Market handlers
     */
    private final BazaarHandler bazaarHandler = new BazaarHandler();
    private final AuctionHandler auctionHandler = new AuctionHandler();

    /**
     * Managers
     */
    private final DataManager dataManager = new DataManager();
    private final ExoticManager exoticManager = new ExoticManager();

    /**
     * Create a new NetworthCalculator instance given a Hypixel API key
     * @param apiKey The Hypixel API key to use
     * @throws InvalidApiKeyException If the provided API key is invalid or non-functional
     */
    public NetworthCalculator(@NonNull String apiKey) throws InvalidApiKeyException {
       this.apiKey = apiKey;
       // TODO check key
    }

    /**
     * Calculates the value of an item
     * @param item The {@link Item} to calculate the value of
     * @return A best-guess of the value of the provided item
     */
    public double calculateItem(@NonNull Item item) {

        // exotics are handled completely differently
        if (this.exoticManager.isExotic(item)) {
            return this.calculateExoticValue(item);
        }

        AuctionHandler.Auction closestAuction = this.auctionHandler.findClosest(item);

        if (closestAuction != null) { // we found a similar item on the AH, base the value off of that

            // the raw craft cost of this item
            double rawCraftCost = this.calculateItemCraft(item);

            // how much more expensive the AH-based reference is by craft cost
            double rawCraftDifference = this.calculateItemCraft(closestAuction.item()) - rawCraftCost;

            // the estimated value of this item, based on the AH reference
            double ahBasedValue = closestAuction.price() - rawCraftDifference;

            // return the AH-based item value OR the raw craft cost, whichever is cheaper
            // this helps prevent AH manipulation to inflate networth
            return Math.min(ahBasedValue, rawCraftCost);
        }

        else { // we didn't find a similar item on the AH, fall back to the raw craft value
            return this.calculateItemCraft(item);
        }
    }

    /**
     * Calculates the raw craft cost of an item
     * @param item The {@link Item} to calculate the craft cost of
     * @return The raw craft cost of the provided item
     */
    public double calculateItemCraft(@NonNull Item item) {

        // ================================================================
        //  Step 1: Determine the base value of the item, clean
        // ================================================================

        // first, check if we have a hardcoded baseline price
        double price = this.dataManager.getBasePrice(item);

        // if not, check if there's a price available on the bazaar
        if (price == 0 && this.bazaarHandler.hasProduct(item.getId())) {
            price = this.bazaarHandler.getMedianPriceUnsafe(item.getId());
        }

        // if not, check if there's a price available on the AH
        if (price == 0) {
            AuctionHandler.Auction cheapest = this.auctionHandler.findCheapest(item.getId());
            if (cheapest != null) {
                price = cheapest.price();
            }
        }

//        System.out.println("Base item: " + Networth.formatter.format(price));

        // ================================================================
        //  Step 2: Determine the value of modifiers
        // ================================================================

        if (item.getReforge() != null && !item.getReforge().equals("greater_spook")) {
            String reforgeStone = this.dataManager.getReforgeStone(item.getReforge());

            if (reforgeStone == null) {
                LOGGER.warn("Encountered unknown reforge \"{}\"!", item.getReforge());
            } else {
                price += this.bazaarHandler.getMedianPriceUnsafe(reforgeStone);
//                System.out.println("Reforge: " + item.getReforge() + " (" + reforgeStone + ") (" +
//                    Networth.formatter.format(this.bazaarHandler.getMedianPriceUnsafe(reforgeStone)) + ")");
            }
        }
        if (item.getUpgradeLevel() > 0) {
            price += this.calculateUpgradeLevelValue(item);
//            System.out.println("Upgrade level: " + item.getUpgradeLevel() + " (" + Networth.formatter.format(this.calculateUpgradeLevelValue(item)) + ")");
        }
        if (item.isRecombobulated()) {
            price += this.bazaarHandler.getMedianPriceUnsafe("RECOMBOBULATOR_3000");
//            System.out.println("Recombobulated: Yes (" +
//                Networth.formatter.format(this.bazaarHandler.getMedianPriceUnsafe("RECOMBOBULATOR_3000")) + ")");
        }
        if (item.getHotPotatoBooks() > 0) {
            price += this.bazaarHandler.getMedianPriceUnsafe("HOT_POTATO_BOOK") * item.getHotPotatoBooks();
//            System.out.println("HPBs: " + item.getHotPotatoBooks() + " (" +
//                Networth.formatter.format((this.bazaarHandler.getMedianPriceUnsafe("HOT_POTATO_BOOK") * item.getHotPotatoBooks())) + ")");
        }
        if (item.getFumingPotatoBooks() > 0) {
            price += this.bazaarHandler.getMedianPriceUnsafe("FUMING_POTATO_BOOK") * item.getFumingPotatoBooks();
//            System.out.println("FPBs: " + item.getFumingPotatoBooks() + " (" +
//                Networth.formatter.format((this.bazaarHandler.getMedianPriceUnsafe("FUMING_POTATO_BOOK") * item.getFumingPotatoBooks())) + ")");
        }
        if (item.isArtOfWar()) {
            price += this.bazaarHandler.getMedianPriceUnsafe("THE_ART_OF_WAR");
//            System.out.println("Art of War: Yes (" + Networth.formatter.format(this.bazaarHandler.getMedianPriceUnsafe("THE_ART_OF_WAR")) + ")");
        }
        if (item.isArtOfPeace()) {
            price += this.bazaarHandler.getMedianPriceUnsafe("THE_ART_OF_PEACE");
//            System.out.println("Art of Peace: Yes (" + Networth.formatter.format(this.bazaarHandler.getMedianPriceUnsafe("THE_ART_OF_PEACE")) + ")");
        }
        if (item.isEnriched()) {
            AuctionHandler.Auction cheapestEnrich = this.auctionHandler.findCheapest("^TALISMAN_ENRICHMENT_[A-Z_]+$");
            if (cheapestEnrich != null) {
                price += cheapestEnrich.price();
//                System.out.println("Enriched: Yes (" + Networth.formatter.format(cheapestEnrich.price()) + " on AH)");
            } else {
                // if there somehow aren't any enrichments on the AH, fall back to estimate price
                price += 5000 * Constants.BIT_COST;
//                System.out.println("Enriched: Yes (" + 5000 * Constants.BIT_COST + " with bits)");
            }
        }
        if (item.getDye() != null) {
            AuctionHandler.Auction cheapestDye = this.auctionHandler.findCheapest(item.getDye());
            if (cheapestDye != null) {
                price += cheapestDye.price();
//                System.out.println("Dye: " + cheapestDye.item().getId() + " (" + Networth.formatter.format(cheapestDye.price()) + ")");
            }
        }

        // Enchantments
//        if (!item.getEnchantments().isEmpty()) {
//            System.out.println("Enchantments: ");
//        }
        for (Map.Entry<String, Integer> enchantment : item.getEnchantments().entrySet()) {
            String name = enchantment.getKey();
            int level = enchantment.getValue();

            String id = "ENCHANTMENT_" + name.toUpperCase() + "_" + level;
            price += this.bazaarHandler.getMedianPriceUnsafe(id);

//            System.out.println(" |    " + name + " " + level + " (" + Networth.formatter.format(this.bazaarHandler.getMedianPriceUnsafe(id)) + ")");
        }

        // Gemstone slots
        List<GemstoneSlotType> gemstoneSlots = this.dataManager.getUnlockedGemstoneSlots(item);
//        if (!gemstoneSlots.isEmpty()) {
//            System.out.println("Gemstone Slots: ");
//        }
        for (GemstoneSlotType gemstoneSlot : gemstoneSlots) {
            double slotCost = gemstoneSlot.getCoinCost();

            for (Map.Entry<String, Integer> entry : gemstoneSlot.getItemCost().entrySet()) {
                String id = entry.getKey();
                int amount = entry.getValue();

                slotCost += this.bazaarHandler.getMedianPriceUnsafe(id) * amount;
            }

//            System.out.println(" |    " + gemstoneSlot.getName() + " (" + Networth.formatter.format(slotCost) + ")");
            price += slotCost;
        }

        // Gemstones
//        if (!item.getGemstones().isEmpty()) {
//            System.out.println("Gemstones: ");
//        }
        for (Gemstone gemstone : item.getGemstones()) {
            price += this.bazaarHandler.getMedianPriceUnsafe(gemstone.getId());
//            System.out.println(" |    " + gemstone + " (" + Networth.formatter.format(this.bazaarHandler.getMedianPriceUnsafe(gemstone.getId())) + ")");
        }

        // ================================================================
        //  Step 3: Apply multiplicative modifiers
        // ================================================================

        // todo

        // done
        price *= item.getCount();
        return price;
    }

    /**
     * Calculates the value of an item's upgrade level
     * @param item The {@link Item} to calculate for
     * @return The cost to upgrade this item to the given upgrade level
     */
    private double calculateUpgradeLevelValue(@NonNull Item item) {
        double value = 0;

        // Dungeon items
        if (item.isDungeonized()) {

            // TODO regular stars

            // master stars
            if (item.getUpgradeLevel() > 5) {
                value += this.bazaarHandler.getMedianPriceUnsafe("FIRST_MASTER_STAR");
            } if (item.getUpgradeLevel() > 6) {
                value += this.bazaarHandler.getMedianPriceUnsafe("SECOND_MASTER_STAR");
            } if (item.getUpgradeLevel() > 7) {
                value += this.bazaarHandler.getMedianPriceUnsafe("THIRD_MASTER_STAR");
            } if (item.getUpgradeLevel() > 8) {
                value += this.bazaarHandler.getMedianPriceUnsafe("FOURTH_MASTER_STAR");
            } if (item.getUpgradeLevel() > 9) {
                value += this.bazaarHandler.getMedianPriceUnsafe("FIFTH_MASTER_STAR");
            }
        }

        // TODO non-dungeon items

        return value;
    }

    /**
     * Calculates a very rough estimate of the value of an exotic armor piece
     * <p>
     * This method deliberately prefers to underestimate rather than overestimate in an attempt to prevent contributing
     * to exotic inflation and scamming with exotics.
     * @param item The {@link Item} to calculate the value of
     * @return An estimate of the value of the armor piece.
     */
    public double calculateExoticValue(@NonNull Item item) {
        return 0; // TODO
    }

    /**
     * Calculates the networth of a player
     * @param profile The profile data of the profile to analyze (from an entry in (/skyblock/profiles)["profiles"])
     * @param uuid The UUID of the player to analyze
     * @return The broken-down networth of the player, as a {@link Networth} object
     */
    public @NonNull Networth calculatePlayer(@NonNull JsonObject profile, @NonNull String uuid)
            throws MalformedProfileException, IllegalArgumentException {

        // Make sure that the provided profile is valid
        if (!profile.has("members")) {
            LOGGER.error("Attempted to handle malformed profile object for {}!", uuid);
            throw new MalformedProfileException("Provided profile object doesn't contain a members list!");
        }

        // Make sure that the player exists within the profile
        uuid = uuid.replace("-", "");
        if (!profile.getAsJsonObject("members").has(uuid)) {
            LOGGER.error("Encountered profile object missing UUID {}!", uuid);
            throw new IllegalArgumentException("UUID \"" + uuid + "\" doesn't exist within the given profile!");
        }

        JsonObject playerData = profile.getAsJsonObject("members").getAsJsonObject(uuid);
        Networth networth = new Networth(uuid);

        // Liquid
        if (profile.has("banking") && profile.getAsJsonObject("banking").has("balance")) {
            networth.setBank(profile.getAsJsonObject("banking").get("balance").getAsDouble());
        }
        networth.setPurse(playerData.getAsJsonObject("currencies").get("coin_purse").getAsDouble());

        // Bags
        networth.setSacks(this.calculateSacks(playerData));
        networth.setAccessories(this.calculateAccessories(playerData));
        networth.setFishingBag(this.calculateFishingBag(playerData));

        // Armor
        networth.setWardrobe(this.calculateWardrobe(playerData));

        // Items

        // Other
        networth.setEssence(this.calculateEssence(playerData));

        return networth;
    }

    // ================================
    //  BAGS
    // ================================

    private double calculateSacks(@NonNull JsonObject playerData) {
        if (!playerData.has("inventory") || !playerData.getAsJsonObject("inventory").has("sacks_counts")) {
            return 0;
        }

        JsonObject sacks = playerData.getAsJsonObject("inventory").getAsJsonObject("sacks_counts");

        double value = 0;

        for (Map.Entry<String, JsonElement> e : sacks.entrySet()) { // iterate over ID:count pairs
            if (e.getValue().getAsInt() > 0) {
                try {
                    value += this.bazaarHandler.getMedianPrice(e.getKey()) * e.getValue().getAsInt();
                } catch (NoSuchProductException ignored) {
                }
            }
        }

        return value;
    }

    private double calculateAccessories(@NonNull JsonObject playerData) {
        if (!playerData.has("inventory") || !playerData.getAsJsonObject("inventory").has("bag_contents")) {
            return 0;
        }

        if (!playerData.getAsJsonObject("inventory").getAsJsonObject("bag_contents").has("talisman_bag")) {
            return 0;
        }

        JsonArray accessoryBag = InventoryUtil.decodeInventory(playerData.getAsJsonObject("inventory")
            .getAsJsonObject("bag_contents").getAsJsonObject("talisman_bag").get("data").getAsString());
        double value = 0;

        for (JsonElement entryElement : accessoryBag) {
            JsonObject entry = entryElement.getAsJsonObject();

            if (!entry.has("id")) {
                continue; // this is an empty slot
            }

            Item item = Item.fromAttributes(entry.get("count").getAsInt(), entry.getAsJsonObject("attributes"));
            value += this.calculateItem(item);
        }

        return value;
    }

    private double calculateFishingBag(@NonNull JsonObject playerData) {
        if (!playerData.has("inventory") || !playerData.getAsJsonObject("inventory").has("bag_contents")) {
            return 0;
        }

        if (!playerData.getAsJsonObject("inventory").getAsJsonObject("bag_contents").has("fishing_bag")) {
            return 0;
        }

        JsonArray fishingBag = InventoryUtil.decodeInventory(playerData.getAsJsonObject("inventory")
            .getAsJsonObject("bag_contents").getAsJsonObject("fishing_bag").get("data").getAsString());
        double value = 0;

        for (JsonElement entryElement : fishingBag) {
            JsonObject entry = entryElement.getAsJsonObject();

            if (entry.has("attributes") && entry.has("count")) {
                try {
                    value += this.bazaarHandler.getMedianPrice(entry.getAsJsonObject("attributes")
                        .getAsJsonObject("id").get("value").getAsString()) * entry.get("count").getAsInt();
                } catch (NoSuchProductException ignored) {
                    // not all baits are on the bazaar!
                }
            }
        }

        return value;
    }

    // ================================
    //  ARMOR
    // ================================

    private double calculateWardrobe(@NonNull JsonObject playerData) {
        if (!playerData.has("inventory") || !playerData.getAsJsonObject("inventory").has("wardrobe_contents")) {
            return 0;
        }

        JsonArray wardrobe = InventoryUtil.decodeInventory(playerData.getAsJsonObject("inventory")
            .getAsJsonObject("wardrobe_contents").get("data").getAsString());
        double value = 0;

        for (JsonElement entryElement : wardrobe) {
            JsonObject entry = entryElement.getAsJsonObject();

            if (entry.has("attributes")) {
                value += this.calculateItem(Item.fromAttributes(1, entry.getAsJsonObject("attributes")));
            }
        }

        return value;
    }

    // ================================
    //  ITEMS
    // ================================

    // ================================
    //  OTHER
    // ================================

    private double calculateEssence(@NonNull JsonObject playerData) {
        JsonObject essence = playerData.getAsJsonObject("currencies").getAsJsonObject("essence");

        if (essence == null || essence.isJsonNull()) {
            return 0; // if the player doesn't have any essence in the API, their essence is worth 0 coins
        }

        try {
            double value = 0;

            // iterate over essence types
            for (Map.Entry<String, JsonElement> e : essence.entrySet()) {
                value += this.bazaarHandler.getMedianPrice("ESSENCE_" + e.getKey()) *
                    e.getValue().getAsJsonObject().get("current").getAsInt();
            }
            return value;
        } catch (NoSuchProductException e) {
            LOGGER.error("Somehow, a type of essence isn't on the Bazaar? This shouldn't happen!", e);
            return 0;
        }
    }
}
