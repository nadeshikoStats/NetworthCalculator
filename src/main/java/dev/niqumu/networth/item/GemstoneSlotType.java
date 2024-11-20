package dev.niqumu.networth.item;

import lombok.Data;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

@Data
public class GemstoneSlotType {
    private final String name;
    private final int coinCost;
    private final Map<String, Integer> itemCost = new HashMap<>();

    public void addItem(@NonNull String id, int count) {
        this.itemCost.put(id, count);
    }
}
