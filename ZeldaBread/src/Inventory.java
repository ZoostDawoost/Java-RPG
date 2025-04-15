/**
 * @file    Inventory.java
 * @brief   Manages a collection of items for a player or container.
 *
 * @author  Jack Schulte
 * @version 1.0.0
 * @date    2025-04-15
 *
 * @copyright Copyright (c) 2025 Jack Schulte. All rights reserved.
 * Strictly confidential and proprietary. Distribution, reproduction,
 * or modification is strictly prohibited without prior written permission.
 */

 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Objects;
 
 public class Inventory {
     private final List<Item> items;
     private final int capacity;
 
     // Default capacity constructor
     public Inventory() {
         this(10); // Default capacity of 10 items
     }
 
     public Inventory(int capacity) {
         if (capacity <= 0) {
             throw new IllegalArgumentException("Inventory capacity must be positive.");
         }
         this.capacity = capacity;
         this.items = new ArrayList<>(capacity);
     }
 
     /**
      * Adds an item to the inventory if space is available.
      * @param item The item to add. Cannot be null.
      * @return true if the item was added successfully, false otherwise (e.g., inventory full).
      */
     public boolean addItem(Item item) {
         Objects.requireNonNull(item, "Cannot add a null item to inventory.");
         if (items.size() < capacity) {
             items.add(item);
             System.out.println("Added " + item.getItemName() + " to inventory.");
             return true;
         } else {
             System.out.println("Inventory is full. Cannot add " + item.getItemName() + ".");
             UIManagement.addDialogue("Inventory is full!");
             return false;
         }
     }
 
     /**
      * Removes the first occurrence of a specific item from the inventory.
      * @param item The item to remove. Cannot be null.
      * @return true if the item was found and removed, false otherwise.
      */
     public boolean removeItem(Item item) {
         Objects.requireNonNull(item, "Cannot remove a null item from inventory.");
         boolean removed = items.remove(item);
         if (removed) {
              System.out.println("Removed " + item.getItemName() + " from inventory.");
         } else {
              System.out.println(item.getItemName() + " not found in inventory.");
         }
         return removed;
     }
 
     /**
      * Removes an item at a specific index.
      * @param index The index of the item to remove.
      * @return The item that was removed, or null if the index was invalid.
      */
      public Item removeItem(int index) {
          if (index >= 0 && index < items.size()) {
               Item removedItem = items.remove(index);
               System.out.println("Removed " + removedItem.getItemName() + " from inventory slot " + index + ".");
               return removedItem;
          } else {
               System.err.println("Invalid index for item removal: " + index);
               return null;
          }
      }
 
 
     /**
      * Checks if the inventory contains a specific item.
      * @param item The item to check for. Cannot be null.
      * @return true if the item is present, false otherwise.
      */
     public boolean hasItem(Item item) {
         Objects.requireNonNull(item, "Cannot check for a null item in inventory.");
         return items.contains(item);
     }
 
     /**
      * Gets the item at a specific index.
      * @param index The index.
      * @return The Item at the index, or null if the index is out of bounds.
      */
     public Item getItem(int index) {
         if (index >= 0 && index < items.size()) {
             return items.get(index);
         }
         return null;
     }
 
     /**
      * Returns an unmodifiable view of the items in the inventory.
      * @return An unmodifiable list of items.
      */
     public List<Item> getItems() {
         return Collections.unmodifiableList(items);
     }
 
     /**
      * Gets the number of items currently in the inventory.
      * @return The current number of items.
      */
     public int getCurrentSize() {
         return items.size();
     }
 
     /**
      * Gets the maximum capacity of the inventory.
      * @return The capacity.
      */
     public int getCapacity() {
         return capacity;
     }
 
     /**
      * Checks if the inventory is full.
      * @return true if the current size equals capacity, false otherwise.
      */
     public boolean isFull() {
         return items.size() >= capacity;
     }
 
     /**
      * Clears all items from the inventory.
      */
     public void clear() {
         items.clear();
         System.out.println("Inventory cleared.");
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder("Inventory [");
         sb.append(getCurrentSize()).append("/").append(capacity).append("]:\n");
         if (items.isEmpty()) {
             sb.append("  (Empty)\n");
         } else {
             for (int i = 0; i < items.size(); i++) {
                 sb.append("  ").append(i).append(": ").append(items.get(i).toString()).append("\n");
             }
         }
         return sb.toString();
     }
 }