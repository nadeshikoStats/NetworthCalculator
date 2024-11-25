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
import com.google.gson.JsonParser;
import io.nadeshiko.networth.item.GemstoneSlotType;
import io.nadeshiko.networth.item.Item;
import lombok.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Container class for item data stored in .json files
 */
public class DataManager {

    /**
     * A map of item ID -> hardcoded base price
     */
    private final Map<String, Double> basePriceMap = new HashMap<>();

    /**
     * A map of reforge names -> reforge stone IDs
     */
    private final Map<String, String> reforgeStones = new HashMap<>();

    /**
     * A map of gemstone slot type IDs -> {@link GemstoneSlotType} instances
     */
    private final Map<String, GemstoneSlotType> gemstoneSlotTypes = new HashMap<>();

    /**
     * A map of Regex expressions for item IDs -> (gemstone slot IDs -> gemstone slot type IDs)
     */
    private final Map<String, Map<String, String>> gemstoneSlots = new HashMap<>();

    /**
     * A list of tiered enchantments
     */
    private final List<String> tieredEnchants = new ArrayList<>();

    /**
     * A list of default reforges
     */
    private final List<String> defaultReforges = new ArrayList<>();

    public DataManager() {
        this.readBasePriceFile("accessories.json");

        this.readReforgeStoneFile();
        this.readGemstoneSlotsFile();
        this.readTieredEnchantsFile();
        this.readDefaultReforgesFile();
    }

    private void readBasePriceFile(@NonNull String fileName) {
        try (InputStream inputStream = DataManager.class.getResourceAsStream("/base/" + fileName)) {
            if (inputStream == null) {
                throw new Exception("Input stream is null!");
            }

            JsonObject parsedPrices = JsonParser.parseString(new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"))).getAsJsonObject();

            parsedPrices.entrySet().forEach(e -> this.basePriceMap.put(e.getKey(), e.getValue().getAsDouble()));
        } catch (Exception e) {
            NetworthCalculator.LOGGER.error("Failed to read data file \"{}\"!", fileName, e);
        }
    }

    private void readReforgeStoneFile() {
        try (InputStream inputStream = DataManager.class.getResourceAsStream("/reforges.json")) {
            if (inputStream == null) {
                throw new Exception("Input stream is null!");
            }

            JsonObject parsedReforges = JsonParser.parseString(new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"))).getAsJsonObject();

            parsedReforges.entrySet().forEach(e -> this.reforgeStones.put(e.getKey(), e.getValue().getAsString()));
        } catch (Exception e) {
            NetworthCalculator.LOGGER.error("Failed to read reforges.json!", e);
        }
    }

    private void readGemstoneSlotsFile() {
        try (InputStream inputStream = DataManager.class.getResourceAsStream("/gemstone_slots.json")) {
            if (inputStream == null) {
                throw new Exception("Input stream is null!");
            }

            JsonObject parsedFile = JsonParser.parseString(new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"))).getAsJsonObject();

            JsonObject types = parsedFile.getAsJsonObject("types");
            JsonObject items = parsedFile.getAsJsonObject("items");

            // Parse types
            for (Map.Entry<String, JsonElement> entry : types.entrySet()) {
                JsonObject slotType = entry.getValue().getAsJsonObject();
                GemstoneSlotType parsedSlotType = new GemstoneSlotType(entry.getKey(), slotType.get("coins").getAsInt());

                for (Map.Entry<String, JsonElement> entry1 : slotType.entrySet()) {
                    if (entry1.getKey().equals("coins")) {
                        continue;
                    }
                    parsedSlotType.addItem(entry1.getKey(), entry1.getValue().getAsInt());
                }

                this.gemstoneSlotTypes.put(entry.getKey(), parsedSlotType);
            }

            // Parse items
            for (Map.Entry<String, JsonElement> entry : items.entrySet()) {
                JsonObject item = entry.getValue().getAsJsonObject();
                Map<String, String> slotTypes = new HashMap<>();

                for (Map.Entry<String, JsonElement> entry1 : item.entrySet()) {
                    slotTypes.put(entry1.getKey(), entry1.getValue().getAsString());
                }

                this.gemstoneSlots.put(entry.getKey(), slotTypes);
            }

        } catch (Exception e) {
            NetworthCalculator.LOGGER.error("Failed to read gemstone_slots.json!", e);
        }
    }

    private void readTieredEnchantsFile() {
        try (InputStream inputStream = DataManager.class.getResourceAsStream("/tiered_enchants.json")) {
            if (inputStream == null) {
                throw new Exception("Input stream is null!");
            }

            JsonArray parsedTieredEnchants = JsonParser.parseString(new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"))).getAsJsonArray();

            parsedTieredEnchants.forEach(enchant -> this.tieredEnchants.add(enchant.getAsString()));
        } catch (Exception e) {
            NetworthCalculator.LOGGER.error("Failed to read tiered_enchants.json!", e);
        }
    }

    private void readDefaultReforgesFile() {
        try (InputStream inputStream = DataManager.class.getResourceAsStream("/default_reforges.json")) {
            if (inputStream == null) {
                throw new Exception("Input stream is null!");
            }

            JsonArray parsedDefaultReforges = JsonParser.parseString(new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"))).getAsJsonArray();

            parsedDefaultReforges.forEach(reforge -> this.defaultReforges.add(reforge.getAsString()));
        } catch (Exception e) {
            NetworthCalculator.LOGGER.error("Failed to read default_reforges.json!", e);
        }
    }

    /**
     * Get the hardcoded base price of an item, if one exists
     * @param item The {@link Item} to look up the base price of
     * @return The hardcoded base price of the item, or zero if none is set
     */
    public double getBasePrice(@NonNull Item item) {
        return this.getBasePrice(item.getId());
    }

    /**
     * Get the hardcoded base price of an item, if one exists
     * @param id The ID of the item to look up the base price of
     * @return The hardcoded base price of the item, or zero if none is set
     */
    public double getBasePrice(@NonNull String id) {
        return this.basePriceMap.getOrDefault(id, 0D);
    }

    /**
     * Get the reforge stone corresponding to a specific reforge
     * @param reforgeName The name of the reforge to look up
     * @return The ID of the reforge stone associated with the provided reforge, or {@code null} if none exists
     */
    public String getReforgeStone(@NonNull String reforgeName) {
        return this.reforgeStones.getOrDefault(reforgeName, null);
    }

    /**
     * Tests whether a given enchantment is a tiered (stacking) enchant
     * @param enchantId The ID of the enchantment to check
     * @return {@code true} if the provided enchantment is tiered
     */
    public boolean isTieredEnchant(@NonNull String enchantId) {
        return this.tieredEnchants.contains(enchantId);
    }

    /**
     * Tests whether a given reforge is a default (basic) reforge
     * @param reforge The ID of the reforge to check
     * @return {@code true} if the provided reforge is a default reforge
     */
    public boolean isDefaultReforge(@NonNull String reforge) {
        return this.defaultReforges.contains(reforge);
    }

    public List<GemstoneSlotType> getUnlockedGemstoneSlots(@NonNull Item item) {

        // get a map of all unlockable slots on this item
        Map<String, String> slots = null;
        for (Map.Entry<String, Map<String, String>> entry : this.gemstoneSlots.entrySet()) {
            if (item.getId().matches(entry.getKey())) {
                slots = entry.getValue();
            }
        }

        if (slots == null) {
            return List.of(); // the item has no unlockable gemstone slots
        }

        List<GemstoneSlotType> slotTypes = new ArrayList<>();

        for (String unlockedGemstoneSlot : item.getUnlockedGemstoneSlots()) {
            GemstoneSlotType type = this.gemstoneSlotTypes.get(slots.get(unlockedGemstoneSlot));

            if (type == null) {
                continue; // this can happen sometimes with item families with different slot types, like wither blades
            }

            slotTypes.add(type);
        }

        return slotTypes;
    }
}
