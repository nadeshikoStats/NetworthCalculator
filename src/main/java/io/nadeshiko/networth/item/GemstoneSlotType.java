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

import lombok.Data;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A representation of an unlockable gemstone slot on an item
 */
@Data
public class GemstoneSlotType {

    /**
     * The name of the gemstone slot, i.e. COMBAT_0
     */
    private final String name;

    /**
     * The coin cost to open this gemstone slot
     */
    private final int coinCost;

    /**
     * The item cost to open this gemstone slot, mapped as item ID -> quantity
     */
    private final Map<String, Integer> itemCost = new HashMap<>();

    /**
     * Add an item to the {@link GemstoneSlotType#itemCost}
     * @param id The ID of the item to add
     * @param count The quantity of the item to add
     */
    public void addItem(@NonNull String id, int count) {
        this.itemCost.put(id, count);
    }
}
