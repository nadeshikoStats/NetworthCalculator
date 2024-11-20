package dev.niqumu.networth.item;

import dev.niqumu.networth.Constants;
import lombok.NonNull;

public class ExoticManager {

    public boolean isExotic(@NonNull Item item) {
        return false; // TODO
    }

    public double getCleanlinessMultiplier(@NonNull Item item) {
        if (item.isRecombobulated()) {
            return Constants.SCUFFED_EXOTIC_MULTIPLIER;
        } else if (item.getHotPotatoBooks() > 0) {
            return Constants.HPB_EXOTIC_MULTIPLIER;
        } else if (item.getReforge() != null) {
            return Constants.CER_EXOTIC_MULTIPLIER;
        }

        return 1;
    }
}
