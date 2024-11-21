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

package io.nadeshiko.networth.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.dewy.nbt.tags.collection.CompoundTag;
import io.nadeshiko.networth.util.InventoryUtil;
import lombok.Data;
import lombok.NonNull;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Parsed representation of a SkyBlock item and its modifiers
 */
@Data
public class Item {
    private final int count;
    private final String id;

    private final int hotPotatoBooks;
    private final int fumingPotatoBooks;
    private final boolean artOfWar;
    private final boolean artOfPeace;
    private final boolean recombobulated;
    private final boolean enriched;
    private final String reforge;
    private final String dye;
    private final boolean dungeonized;
    private final int upgradeLevel;

    private final Map<String, Integer> enchantments = new HashMap<>();
    private final List<Gemstone> gemstones = new ArrayList<>();
    private final List<String> unlockedGemstoneSlots = new ArrayList<>();

    public static Item fromAttributes(int count, @NonNull JsonObject attributes) {
        Item parsedItem = new Item(
            count,
            attributes.getAsJsonObject("id").get("value").getAsString(),
            attributes.has("hot_potato_count") ? Math.min(attributes.getAsJsonObject("hot_potato_count").get("value").getAsInt(), 10) : 0,
            attributes.has("hot_potato_count") ? Math.max(attributes.getAsJsonObject("hot_potato_count").get("value").getAsInt() - 10, 0) : 0,
            attributes.has("art_of_war_count"),
            attributes.has("artOfPeaceApplied"),
            attributes.has("rarity_upgrades"),
            attributes.has("talisman_enrichment"),
            attributes.has("modifier") ? attributes.getAsJsonObject("modifier").get("value").getAsString() : null,
            attributes.has("dye_item") ? attributes.getAsJsonObject("dye_item").get("value").getAsString() : null,
            attributes.has("dungeon_item"),
            attributes.has("upgrade_level") ? attributes.getAsJsonObject("upgrade_level").get("value").getAsInt() : 0
        );

        // Enchants
        if (attributes.has("enchantments")) {
            JsonObject rawEnchantments = attributes.getAsJsonObject("enchantments").getAsJsonObject("value");
            for (Map.Entry<String, JsonElement> entry : rawEnchantments.entrySet()) {
                parsedItem.enchantments.put(entry.getKey(), entry.getValue().getAsJsonObject().get("value").getAsInt());
            }
        }

        // Gemstones
        if (attributes.has("gems")) {
            JsonObject gems = attributes.getAsJsonObject("gems").getAsJsonObject("value");

            // Add gemstone slots
            if (gems.has("unlocked_slots")) {
                JsonArray unlockedSlots = gems.getAsJsonObject("unlocked_slots").getAsJsonArray("value");
                for (JsonElement slot : unlockedSlots) {
                    parsedItem.unlockedGemstoneSlots.add(slot.getAsJsonObject().get("value").getAsString());
                }
            }

            // Add gemstones
            for (Map.Entry<String, JsonElement> entry : gems.entrySet()) {
                if (entry.getKey().endsWith("_gem") || entry.getKey().equals("unlocked_slots")) {
                    continue;
                }

                String type = entry.getKey().split("_")[0];

                if (Gemstone.Type.of(type) != null) { // handle simple slots like jade, topaz, aquamarine, etc.
                    if (entry.getValue().getAsJsonObject().get("value") instanceof JsonPrimitive) {
                        parsedItem.gemstones.add(new Gemstone(
                            Gemstone.Type.of(type),
                            Gemstone.Quality.of(entry.getValue().getAsJsonObject().get("value").getAsString())
                        ));
                    } else {
                        parsedItem.gemstones.add(new Gemstone(
                            Gemstone.Type.of(type),
                            Gemstone.Quality.of(entry.getValue().getAsJsonObject().getAsJsonObject("value")
                                .getAsJsonObject("quality").get("value").getAsString())
                        ));
                    }
                } else { // handle complex slots like combat, defensive, offensive, universal, etc.
                    if (entry.getValue().getAsJsonObject().get("value") instanceof JsonPrimitive) {
                        parsedItem.gemstones.add(new Gemstone(
                            Gemstone.Type.of(gems.get(entry.getKey() + "_gem").getAsJsonObject().get("value").getAsString()),
                            Gemstone.Quality.of(entry.getValue().getAsJsonObject().get("value").getAsString())
                        ));
                    } else {
                        parsedItem.gemstones.add(new Gemstone(
                            Gemstone.Type.of(gems.get(entry.getKey() + "_gem").getAsJsonObject().get("value").getAsString()),
                            Gemstone.Quality.of(entry.getValue().getAsJsonObject().getAsJsonObject("value")
                                .getAsJsonObject("quality").get("value").getAsString())
                        ));
                    }
                }
            }
        }

        return parsedItem;
    }

    public static Item fromBytes(@NonNull String bytes) {
        JsonObject item;

        try {
            byte[] decodedData = Base64.getDecoder().decode(bytes);
            GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(decodedData));
            CompoundTag parsedNbt = InventoryUtil.NBT.fromByteArray(gzipInputStream.readAllBytes());
            item = parsedNbt.toJson(0, InventoryUtil.NBT.getTypeRegistry());
        } catch (Exception e) {
            System.err.println("Failed to decode item data!");
            e.printStackTrace();
            return null;
        }

        item = item.getAsJsonObject("value").getAsJsonObject("i").getAsJsonArray("value")
            .get(0).getAsJsonObject().getAsJsonObject("value");

        JsonObject attributes = item.getAsJsonObject("tag").getAsJsonObject("value")
            .getAsJsonObject("ExtraAttributes").getAsJsonObject("value");

        return fromAttributes(item.getAsJsonObject("Count").get("value").getAsInt(), attributes);
    }

    public double compareTo(@NonNull Item that) {
        if (!this.id.equals(that.id)) {
            return 0; // completely different item
        }

        double score = 1;

        if (!Objects.equals(this.reforge, that.reforge)) {
            score -= 0.005;
        }
        if (this.hotPotatoBooks != that.hotPotatoBooks) {
            score -= (Math.abs(this.hotPotatoBooks - that.hotPotatoBooks)) * 0.001;
        }
        if (this.fumingPotatoBooks != that.fumingPotatoBooks) {
            score -= (Math.abs(this.fumingPotatoBooks - that.fumingPotatoBooks)) * 0.012;
        }
        if (this.recombobulated != that.recombobulated) {
            score -= 0.03;
        }
        if (this.artOfPeace != that.artOfPeace) {
            score -= 0.075;
        }
        if (this.enriched != that.enriched) {
            score -= 0.02;
        }
        if (!Objects.equals(this.dye, that.dye)) {
            if (this.dye == null || that.dye == null) {
                score -= 0.09; // one item is dyed the other isn't
            } else {
                score -= 0.075; // the items are both dyed, but with different dyes
            }
        }
        if (this.upgradeLevel != that.upgradeLevel) {
            score -= (Math.abs(this.upgradeLevel - that.upgradeLevel)) * 0.035;
        }

        // Enchantments
        for (Map.Entry<String, Integer> entry : this.enchantments.entrySet()) {
            String enchantment = entry.getKey();
            Integer level = entry.getValue();

            if (!that.hasEnchantment(enchantment)) {
                score -= 0.05; // this item has an enchantment that the other doesn't
            } else if (that.getEnchantment(enchantment) != level) {
                // the items have different enchantment levels
                score -= (Math.abs(level - that.getEnchantment(enchantment))) * 0.02;
            }
        }
        for (Map.Entry<String, Integer> entry : that.enchantments.entrySet()) {
            if (!this.hasEnchantment(entry.getKey())) {
                score -= 0.05; // the other item has an enchantment that this item doesn't
            }
        }

        // Gemstones
        // TODO

        return Math.max(0, score);
    }

    public boolean hasEnchantment(@NonNull String id) {
        return this.enchantments.containsKey(id);
    }

    public int getEnchantment(@NonNull String id) {
        return this.enchantments.getOrDefault(id, 0);
    }

    @Override
    public String toString() {
        return this.count + "x " + this.id + ":" +
            "\nReforge: " + (this.reforge == null ? "(none)" : this.reforge) +
            "\nRecombobulated: " + (this.recombobulated ? "yes" : "no") +
            "\nHPBs: " + this.hotPotatoBooks + ", FPBs: " + this.fumingPotatoBooks +
            "\nGemstones: " + this.gemstones +
            "\nEnchantments: " + this.enchantments;
    }
}
