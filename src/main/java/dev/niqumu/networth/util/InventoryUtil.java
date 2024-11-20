package dev.niqumu.networth.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.dewy.nbt.Nbt;
import dev.dewy.nbt.tags.collection.CompoundTag;
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
            System.err.println("Failed to decode inventory data!");
            e.printStackTrace();
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
