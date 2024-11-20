package dev.niqumu.networth.market;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.niqumu.networth.exception.NoSuchProductException;
import dev.niqumu.networth.util.HTTPUtil;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

public class BazaarHandler {
    private final Map<String, JsonObject> productMap = new HashMap<>();
    private long lastUpdateTime = 0;

    public BazaarHandler() {
        this.doUpdate();
    }

    public boolean hasProduct(@NonNull String id) {
        return this.productMap.containsKey(id);
    }

    public JsonObject getProduct(@NonNull String id) {
        if (System.currentTimeMillis() - this.lastUpdateTime > 1000 * 60 * 15) {
            this.doUpdate();
        }
        return this.productMap.getOrDefault(id, null);
    }

    public double getMedianPrice(@NonNull String id) throws NoSuchProductException {
        JsonObject product = this.getProduct(id);

        if (product == null) {
            throw new NoSuchProductException("No product by the ID of " + id + " exists!");
        }

        JsonObject quickStatus = product.getAsJsonObject("quick_status");
        return (quickStatus.get("sellPrice").getAsDouble() + quickStatus.get("buyPrice").getAsDouble()) / 2d;
    }

    public double getMedianPriceUnsafe(@NonNull String id) {
        JsonObject product = this.getProduct(id);

        if (product == null) {
            System.err.println("[Unsafe] No product by the ID of " + id + " exists!");
            return 0;
        }

        JsonObject quickStatus = product.getAsJsonObject("quick_status");
        return (quickStatus.get("sellPrice").getAsDouble() + quickStatus.get("buyPrice").getAsDouble()) / 2d;
    }

    private void doUpdate() {
        try {
            HTTPUtil.Response response = HTTPUtil.get("https://api.hypixel.net/v2/skyblock/bazaar");
            JsonObject jsonResponse = JsonParser.parseString(response.response()).getAsJsonObject();

            if (!jsonResponse.get("success").getAsBoolean()) {
                System.err.println("Bazaar data indicated a failure!");
                return;
            }

            jsonResponse.getAsJsonObject("products").entrySet().forEach(e -> {
                this.productMap.put(e.getKey(), e.getValue().getAsJsonObject());
            });

            lastUpdateTime = System.currentTimeMillis();
        } catch (Exception e) {
            System.err.println("Failed to fetch Bazaar data!");
            e.printStackTrace();
        }
    }
}
