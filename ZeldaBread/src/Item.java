/**
 * @file    Item.java
 * @brief   Base class for all items in the game.
 *
 * @author  Jack Schulte
 * @version 1.0.0
 * @date    2025-04-15
 *
 * @copyright Copyright (c) 2025 Jack Schulte. All rights reserved.
 * Strictly confidential and proprietary. Distribution, reproduction,
 * or modification is strictly prohibited without prior written permission.
 */

 import java.util.Objects;

 public class Item {
     protected String itemName;
     protected String description;
     protected int cost;
     protected double dropRate; // Probability of dropping (0.0 to 1.0)
 
     // Default constructor for potential serialization or subclasses
     public Item() {
         this.itemName = "Unknown Item";
         this.description = "";
         this.cost = 0;
         this.dropRate = 0.0;
     }
 
     public Item(String itemName, String description, int cost, double dropRate) {
         this.itemName = Objects.requireNonNull(itemName, "Item name cannot be null");
         this.description = description != null ? description : "";
         this.cost = Math.max(0, cost); // Cost cannot be negative
         this.dropRate = Math.max(0.0, Math.min(1.0, dropRate)); // Clamp dropRate between 0 and 1
     }
 
     // --- Getters ---
     public String getItemName() {
         return itemName;
     }
 
     public String getDescription() {
         return description;
     }
 
     public int getCost() {
         return cost;
     }
 
     public double getDropRate() {
         return dropRate;
     }
 
     // --- Basic Object Methods ---
     @Override
     public String toString() {
         return itemName + (description.isEmpty() ? "" : " (" + description + ")");
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
         Item item = (Item) o;
         return cost == item.cost &&
                Double.compare(item.dropRate, dropRate) == 0 &&
                Objects.equals(itemName, item.itemName) &&
                Objects.equals(description, item.description);
     }
 
     @Override
     public int hashCode() {
         return Objects.hash(itemName, description, cost, dropRate);
     }
 
     // Placeholder for a "use" action - subclasses should override
     public void use(RPG player) {
         System.out.println("Cannot use the generic item: " + itemName);
         UIManagement.addDialogue("You can't seem to use the " + itemName + ".");
     }
 }