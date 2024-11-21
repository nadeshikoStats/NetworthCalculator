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

import lombok.NonNull;

/**
 * Representation of a gemstone applied on an item
 * @param type The {@link Type} of the gemstone
 * @param quality The {@link Quality} of the gemstone
 */
public record Gemstone(Type type, Quality quality) {

    /**
     * Create a new {@link Gemstone} instance given the item ID of a gemstone
     * @param id The item ID of a gemstone, such as FLAWED_JASPER_GEM
     * @return A new {@link Gemstone} instance for the given gemstone item ID
     */
    public static Gemstone fromId(@NonNull String id) {
        return new Gemstone(Type.of(id.split("_")[0]), Quality.of(id.split("_")[1]));
    }

    /**
     * @return The item ID of this gemstone, i.e. FLAWED_JASPER_GEM
     */
    public String getId() {
        return this.quality.name() + "_" + this.type.name() + "_GEM";
    }

    /**
     * @return A friendly String representation of this Gemstone, such as "Fine Sapphire Gemstone"
     */
    @Override
    public String toString() {
        return this.quality.name().charAt(0) + this.quality.name().substring(1).toLowerCase() + " " +
            this.type.name().charAt(0) + this.type.name().substring(1).toLowerCase() + " Gemstone";
    }

    /**
     * Compares this {@link Gemstone} instance to another object
     * @param obj   the reference object with which to compare.
     * @return Whether the two objects are equivalent
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Gemstone gem) {
            return this.getId().equals(gem.getId());
        }
        return false;
    }

    /**
     * An enum of gemstone types, i.e. Ruby, Jasper, Sapphire, Opal, etc.
     */
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

    /**
     * An enum of gemstone qualities, i.e. Rough, Flawed, Fine, Flawless, Perfect.
     */
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
