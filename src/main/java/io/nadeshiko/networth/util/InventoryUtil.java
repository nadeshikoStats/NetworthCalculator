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

package io.nadeshiko.networth.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.dewy.nbt.Nbt;
import dev.dewy.nbt.tags.collection.CompoundTag;
import io.nadeshiko.networth.NetworthCalculator;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

@UtilityClass
public class InventoryUtil {

    public static final Nbt NBT = new Nbt();

    public JsonArray decodeInventory(@NonNull String data) {
        byte[] decodedData = Base64.getDecoder().decode(data);
        JsonObject inventory;
        JsonArray decodedInventory = new JsonArray();

        // Attempt to decode inventory data
        try {
            GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(decodedData));
            CompoundTag parsedNbt = NBT.fromByteArray(gzipInputStream.readAllBytes());
            inventory = parsedNbt.toJson(0, NBT.getTypeRegistry());
        } catch (Exception e) {
            NetworthCalculator.LOGGER.error("Failed to decode inventory data!", e);
            return new JsonArray();
        }

        // Clean up inventory data
        for (JsonElement element : inventory.getAsJsonObject("value").getAsJsonObject("i").getAsJsonArray("value")) {
            JsonObject oldItem = element.getAsJsonObject().getAsJsonObject("value");
            JsonObject newItem = new JsonObject();

            // Check if the slot is air first
            if (oldItem.isEmpty()) {
                decodedInventory.add(newItem);
                continue;
            }

            JsonObject tag = oldItem.getAsJsonObject("tag").getAsJsonObject("value");
            newItem.addProperty("id", oldItem.getAsJsonObject("id").get("value").getAsInt());
            newItem.addProperty("count", oldItem.getAsJsonObject("Count").get("value").getAsInt());
            newItem.addProperty("damage", oldItem.getAsJsonObject("Damage").get("value").getAsInt());
            if (tag.has("display")) {
                newItem.addProperty("name", tag.getAsJsonObject("display").getAsJsonObject("value")
                    .getAsJsonObject("Name").get("value").getAsString());
                newItem.add("lore", tag.getAsJsonObject("display").getAsJsonObject("value")
                    .getAsJsonObject("Lore").getAsJsonArray("value"));
            }
            if (tag.has("ExtraAttributes")) {
                newItem.add("attributes", tag.getAsJsonObject("ExtraAttributes").getAsJsonObject("value"));
            }

            JsonArray lore = new JsonArray();
            for (JsonElement jsonElement : tag.getAsJsonObject("display").getAsJsonObject("value")
                .getAsJsonObject("Lore").getAsJsonArray("value")) {
                lore.add(jsonElement.getAsJsonObject().get("value").getAsString());
            }
            newItem.add("lore", lore);

            decodedInventory.add(newItem);
        }

        return decodedInventory;
    }
}
