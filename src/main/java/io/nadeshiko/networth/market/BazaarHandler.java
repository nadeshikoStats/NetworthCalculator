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

package io.nadeshiko.networth.market;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.nadeshiko.networth.NetworthCalculator;
import io.nadeshiko.networth.exception.NoSuchProductException;
import io.nadeshiko.networth.util.HTTPUtil;
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
            NetworthCalculator.LOGGER.warn("No product by the ID of {} exists!", id);
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
                NetworthCalculator.LOGGER.error("Bazaar data didn't return successfully!");
                return;
            }

            jsonResponse.getAsJsonObject("products").entrySet().forEach(e ->
                this.productMap.put(e.getKey(), e.getValue().getAsJsonObject()));

            lastUpdateTime = System.currentTimeMillis();
        } catch (Exception e) {
            NetworthCalculator.LOGGER.error("Failed to fetch Bazaar data!", e);
        }
    }
}
