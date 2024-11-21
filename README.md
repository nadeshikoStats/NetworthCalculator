# NetworthCalculator
A flexible and accurate Hypixel SkyBlock networth library written in Java, designed for [nadeshiko.io](https://nadeshiko.io) ([GitHub](https://github.com/NadeshikoStats/nadeshiko.io)).

` ⚠️ WARNING ` This project is currently early in development, and not ready for use.

## Philosophy
Unlike the SkyHelper networth calculator, this library doesn't rely on magic pre-defined constants to help determine the value of an item, except where absolutely neccessary. This calculator upholds the philosophy of following market value at all times, including niches such as skins and exotics.

We believe that networth should be the definition of how much a player may liquidate their profile for at the current market value. As a result, this calculator prefers to use the market value of items over their raw craft cost, with the former almost always being lower. To prevent networth inflation via Auction House manipulation, this calculator declares the value of an item to be the minimum of the Auction House market value for the item and the raw craft cost of the item. 

## Item Calculation
To calculate the value of a normal auctionable item*, this calculator:
 - Searches the Auction House for the most similar item
 - Computes the raw craft cost of the similar item and compares it to the raw craft cost of the item being analyzed
 - Subtracts the difference between the raw craft cost of the two items from the list price of the Auction House match.

### Specialty items
_*Special items are different, and rely much more on manual data._ The value of skins and rare items (Gamebreakers, Creative Minds, glitched items, etc.) is based on recent sales and offers in the Collectors Hub Discord server. The value of exotics is approximated from recent sales, offers, and market trends in the Exotic Cafe Discord server, as well as the speculative input of experts in the community. In both cases, the estimates tend to be on the lower side to avoid the potential for scamming and contributing to inflation. 

` ⚠️ WARNING `  These specialty items are only priced as **estimates**. The developers of nadeshiko make no guarantee as to their accuracy. Always check before trading using these items. Do not rely on this library.

### Bazaar goods
In valuing Bazaar goods, this calculator averages the price of the highest buy order and lowest sell offer on the Bazaar, updated in real-time. Detection of and mitigation against Bazaar manipulation is in-progress.

## Usage
To start, create an instance of the `NetworthCalculator` class via the `NetworthCalculator#String` constructor. You must provide a valid Hypixel API key.

The `NetworthCalaculator` class provides several public methods:
- `calculateItem`, a general-purpose method to determine the value of an item. The specifics on how this works are discussed in the **Item Calculation** section.
- `calculateItemCraft`, a method to calculate the raw craft cost of an item - how much it would cost to recreate this item exactly from raw materials.
- `calculate`, the big one. Calculates the networth of the provided player on the provided profile, returning a `Networth` object.
