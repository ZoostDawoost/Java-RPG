/**
 * @file    CharacterClassInfo.java
 * @brief   Provides static information about character classes, including base stats and lists of potential names.
 *
 * Centralizes default attributes and thematic naming conventions for each playable class in the game.
 *
 * @author  Jack Schulte 
 * @version 1.1.0
 * @date    2025-04-14
 *
 * @copyright Copyright (c) 2025 Jack Schulte . All rights reserved.
 * Strictly confidential and proprietary. Distribution, reproduction,
 * or modification is strictly prohibited without prior written permission.
 *
 * @version History:
 * - 1.1.0 (2025-04-14): Replaced female-coded names with gender-neutral, bread-themed alternatives.
 * - 1.0.0 (2025-04-14): Initial version. Created to store static class data (stats, names).
 */

 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;

 public class CharacterClassInfo {

     // --- Base Stats Constants ---
     // Using a nested class or Map for cleaner organization
     public static class BaseStats {
         public final int VIG, DEF, STR, DEX, AGT, LUCK, MAX_HP, MAX_ENERGY;

         public BaseStats(int vig, int def, int str, int dex, int agt, int luck, int maxHp, int maxEnergy) {
             VIG = vig; DEF = def; STR = str; DEX = dex; AGT = agt; LUCK = luck; MAX_HP = maxHp; MAX_ENERGY = maxEnergy;
         }
     }

     private static final Map<String, BaseStats> CLASS_STATS = new HashMap<>();
     static {
         CLASS_STATS.put("Knight",   new BaseStats(11, 12, 11, 10, 8,  8, 120, 80));
         CLASS_STATS.put("Sentinel", new BaseStats(10, 13, 9,  11, 7, 10, 150, 50));
         CLASS_STATS.put("Assassin", new BaseStats(9,  8,  11, 12, 13, 7,  80,  120));
         CLASS_STATS.put("Wizard",   new BaseStats(8,  7,  8,  13, 12, 12, 70,  150));
         CLASS_STATS.put("Caveman",  new BaseStats(10, 10, 10, 10, 10, 10, 100, 100));
     }

     // --- Static Name Lists (Revised with Bread-Themed Gender-Neutral Names) ---
     private static final Map<String, List<String>> CLASS_NAMES = new HashMap<>();
     static {
         CLASS_NAMES.put("Knight", Arrays.asList(
             "Sir Reginald", "Ironclad Ivan", "Ser Gareth", "The Stalwart", "Chevalier Gerard",
             "Guardian Gregor", "Baron Von Bread", "Knight Rye-an", "Sir Crumb", "Lord Loaf",
             "Ser Doughnald", "Earl of Pumpernickel", "Duke Sourdough", "Crumpet Champion", "Pretzel Paladin",
             "Croissant Cavalier", "Challah Charger", "Cornbread Crusader", "Wheat Warrior", "Ciabatta Captain"
         ));
         CLASS_NAMES.put("Sentinel", Arrays.asList(
             "Aegis Alistair", "Guardian Gideon", "Bastion Boris", "The Unyielding", "Protector Pius",
             "Rye Guard", "Pita Protector", "Sentinel Scone", "Wheat Warden", "The Grain Wall",
             "Matzo Marshal", "Defender Pretzel", "Tortilla Templar", "Guardian Gluten", "Buckler Bagel",
             "Griddle Guardian", "Flatbread Fortress", "Focaccia Frontline", "Naan Neighbor", "Brioche Bulwark"
         ));
         CLASS_NAMES.put("Assassin", Arrays.asList(
             "Shadow Zephyr", "Silent Kael", "Ghostblade Gideon", "Umbra Umberto", "The Crimson Shadow",
             "Phantom Phineas", "Stiletto Slice", "Dagger Dirk", "Kukri Khan", "Stealthy Sourdough",
             "Crouton Cutthroat", "Ninja Naan", "Pumpernickel Poison", "Assassin's Bagel", "Rye Rogue",
             "Matzo Mauler", "Pita Pillager", "Sourdough Shadow", "Challah Cutlass", "Baguette Blade"
         ));
         CLASS_NAMES.put("Wizard", Arrays.asList(
             "Merlin Magnus", "Sage Solomon", "Warlock Wolfram", "Conjurer Corvus", "Elementalist Eldrin",
             "The Thaumaturge", "Grand Alf", "Mingus the Mildewed", "Zalthar the Zesty", "Pyromancer Pumpernickel",
             "Cryo-Ciabatta", "Necromancer Naan", "Rune Raisinbread", "Alchemist Arepa", "Warlock Wholewheat",
             "Sourdough Sorcerer", "Pumpernickel Prophet", "Rye Runemaster", "Pretzel Prestidigitator", "Focaccia Foreseer"
         ));
         CLASS_NAMES.put("Caveman", Arrays.asList(
             "Gronk Stonefist", "Unga Rockthrower", "Barga Skullcrusher", "Zog Mammothhide", "Oog Spearbreaker",
             "Krug Firemaker", "Thag Beastmaster", "Roka Earthshaker", "Mog Tribe-Guardian", "Ugg the Unsliced",
             "Grogg Grainmasher", "Bonk Breadbreaker", "Krum Crustgnawer", "Zodd Doughdragger", "Thump Thickskull",
             "Blor Bitterbite", "Grak Grubfinder", "Womp Wildwheat", "Hunk Hardtack", "Boulder Baguette"
         ));
     }

     private static final Random random = new Random();

     /**
      * Gets the base stats for a given character class.
      * @param className The name of the class (e.g., "Knight").
      * @return The BaseStats object for the class, or null if the class is not found.
      */
     public static BaseStats getStats(String className) {
         return CLASS_STATS.get(className);
     }

     /**
      * Gets a random name from the list associated with the given character class.
      * @param className The name of the class (e.g., "Knight").
      * @return A random name string, or "Unknown Hero" if the class or names are not found.
      */
     public static String getRandomName(String className) {
         List<String> names = CLASS_NAMES.get(className);
         if (names != null && !names.isEmpty()) {
             return names.get(random.nextInt(names.size()));
         }
         return "Unknown Hero"; // Fallback name
     }

     /**
      * Gets the unmodifiable list of names for a given character class.
      * Used primarily for testing or potential future UI features.
      * @param className The name of the class.
      * @return An unmodifiable list of names, or null if the class is not found.
      */
     public static List<String> getAllNames(String className) {
          List<String> names = CLASS_NAMES.get(className);
          return (names != null) ? Collections.unmodifiableList(names) : null;
     }
 }