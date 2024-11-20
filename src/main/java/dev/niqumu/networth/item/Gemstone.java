package dev.niqumu.networth.item;

import lombok.NonNull;

public record Gemstone(Type type, Quality quality) {

    public static Gemstone fromId(@NonNull String id) {
        return new Gemstone(Type.of(id.split("_")[0]), Quality.of(id.split("_")[1]));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Gemstone gem) {
            return this.getId().equals(gem.getId());
        }
        return false;
    }

    public String getId() {
        return this.quality.name() + "_" + this.type.name() + "_GEM";
    }

    @Override
    public String toString() {
        return this.quality.name().charAt(0) + this.quality.name().substring(1).toLowerCase() + " " +
            this.type.name().charAt(0) + this.type.name().substring(1).toLowerCase() + " Gemstone";
    }

    public enum Type {
        AMBER,
        TOPAZ,
        SAPPHIRE,
        AMETHYST,
        JASPER,
        RUBY,
        JADE,
        OPAL,
        AQUAMARINE,
        CITRINE,
        ONYX,
        PERIDOT;

        public static Type of(String type) {
            try {
                return Type.valueOf(type.toUpperCase());
            } catch (Exception e) {
                return null;
            }
        }
    }

    public enum Quality {
        ROUGH,
        FLAWED,
        FINE,
        FLAWLESS,
        PERFECT;

        public static Quality of(String rarity) {
            try {
                return Quality.valueOf(rarity.toUpperCase());
            } catch (Exception e) {
                return null;
            }
        }
    }
}
