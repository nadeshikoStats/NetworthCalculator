package dev.niqumu.networth.market;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.niqumu.networth.util.HTTPUtil;
import dev.niqumu.networth.item.Item;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AuctionHandler {

    /**
     * A list of current BIN auctions on the Auction House
     */
    private List<Auction> currentAuctions = new ArrayList<>();
    private long lastUpdateTime = 0;

    public AuctionHandler() {
        this.doUpdate();
    }

    public Auction findClosest(@NonNull Item item) {
        if (System.currentTimeMillis() - this.lastUpdateTime > 1000 * 60 * 60) {
            new Thread(this::doUpdate).start();
        }

        Auction bestMatch = null;
        double bestScore = 0;

        for (Auction auction : this.currentAuctions) {
            double score = auction.item.compareTo(item);

            if (score == 1) {
                return auction; // we found a perfect match
            }

            if (score > bestScore) {
                bestMatch = auction;
                bestScore = score;
            }
        }

        return bestMatch;
    }

    public Auction findCheapest(@NonNull String id) {
        if (System.currentTimeMillis() - this.lastUpdateTime > 1000 * 60 * 60) {
            new Thread(this::doUpdate).start();
        }

        Auction cheapest = null;

        for (Auction auction : this.currentAuctions) {
            if (!auction.item.getId().equals(id) && !auction.item.getId().matches(id)) {
                continue;
            }

            if (cheapest == null || auction.price < cheapest.price) {
                cheapest = auction;
            }
        }

        return cheapest;
    }

    private void doUpdate() {
        try {
            List<Auction> newCurrentAuctions = new ArrayList<>();

            int pages = this.scanPage(0, newCurrentAuctions);

            for (int i = 0; i < pages; i++) {
                this.scanPage(i, newCurrentAuctions);
            }

            this.currentAuctions = newCurrentAuctions;
            lastUpdateTime = System.currentTimeMillis();
        } catch (Exception e) {
            System.err.println("Failed to fetch AH data!");
            e.printStackTrace();
        }
    }

    private int scanPage(int page, List<Auction> listToAddTo) throws Exception {
        HTTPUtil.Response response = HTTPUtil.get("https://api.hypixel.net/v2/skyblock/auctions?page=" + page);
        JsonObject jsonResponse = JsonParser.parseString(response.response()).getAsJsonObject();

        if (!jsonResponse.get("success").getAsBoolean()) {
            System.err.println("AH data indicated a failure!");
            return 0;
        }

        for (JsonElement auctionElement : jsonResponse.getAsJsonArray("auctions")) {
            JsonObject auction = auctionElement.getAsJsonObject();

            if (auction.has("bin") && auction.get("bin").getAsBoolean()) {
                listToAddTo.add(Auction.fromApiEntry(auction));
            }
        }

        return jsonResponse.get("totalPages").getAsInt();
    }

    public record Auction(Item item, double price) {
        public static Auction fromApiEntry(JsonObject apiEntry) {
            return new Auction(Item.fromBytes(apiEntry.get("item_bytes").getAsString()),
                apiEntry.get("starting_bid").getAsDouble());
        }
    }
}
