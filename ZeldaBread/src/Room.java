/**
 * @file    Room.java
 * @brief   Represents a single room (tile) on the game board.
 * Stores its type, visibility status, items, and potentially monsters/NPCs.
 *
 * @author  Jack
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
 
 public class Room {
 
     // --- Room Type Enum (more descriptive than integers) ---
     public enum RoomType {
         EMPTY_SPACE(0, "Empty Space", false), // Value, Description, Is traversable?
         CORRIDOR(1, "Corridor", true),         // Type 1 - Placeholder before events
         START_ROOM(2, "Starting Room", true),
         ENEMY_ROOM(3, "Enemy Room", true),
         DIFFICULT_ENEMY_ROOM(4, "Difficult Enemy Room", true),
         SHOP(5, "Shop", true),
         SMITHY(6, "Smithy", true),
         TREASURE_ROOM(7, "Treasure Room", true),
         PLAIN_ROOM(8, "Plain Room", true),      // Safe room
         SHRINE(9, "Shrine", true),
         BOSS_ROOM(10, "Boss Room", true),
         UNKNOWN(-1, "Unknown Room", false); // Default/Error
 
         private final int value;
         private final String description;
         private final boolean traversable;
 
         RoomType(int value, String description, boolean traversable) {
             this.value = value;
             this.description = description;
             this.traversable = traversable;
         }
 
         public int getValue() { return value; }
         public String getDescription() { return description; }
         public boolean isTraversable() { return traversable; }
 
         public static RoomType fromValue(int value) {
             for (RoomType type : values()) {
                 if (type.value == value) {
                     return type;
                 }
             }
             return UNKNOWN; // Return Unknown if value not found
         }
     }
 
     // --- Room Attributes ---
     private RoomType roomType;
     private boolean visited;   // Has the player entered this specific room?
     private boolean explored;  // Is this room (or adjacent) revealed on the map?
     private final List<Item> items; // Items lying on the floor in this room
     // Placeholders for future expansion:
     // private Monster monster;
     // private ShopKeeper shopKeeper;
     // private QuestNPC questGiver;
 
     // --- Constructor ---
     public Room(RoomType type) {
         this.roomType = Objects.requireNonNull(type, "RoomType cannot be null");
         this.visited = false;
         this.explored = false;
         this.items = new ArrayList<>();
         // Initialize monster/shopkeeper etc. based on type if needed later
     }
 
     // Default constructor creates an EMPTY_SPACE room
     public Room() {
         this(RoomType.EMPTY_SPACE);
     }
 
     // --- Getters ---
     public RoomType getRoomType() {
         return roomType;
     }
 
     public boolean isVisited() {
         return visited;
     }
 
     public boolean isExplored() {
         return explored;
     }
 
     public List<Item> getItems() {
         // Return an unmodifiable list to prevent external modification
         return Collections.unmodifiableList(items);
     }
 
     public String getDescription() {
         return roomType.getDescription();
     }
 
      public boolean isTraversable() {
          return roomType.isTraversable();
      }
 
     // --- Setters ---
     public void setRoomType(RoomType roomType) {
         this.roomType = Objects.requireNonNull(roomType, "RoomType cannot be null");
         // Potentially clear/reset monsters/items/NPCs if type changes drastically
     }
 
     // Convenience setter using integer value
     public void setRoomType(int typeValue) {
         setRoomType(RoomType.fromValue(typeValue));
     }
 
 
     public void setVisited(boolean visited) {
         this.visited = visited;
         if (visited) {
             this.explored = true; // Visiting a room always makes it explored
         }
     }
 
     public void setExplored(boolean explored) {
         this.explored = explored;
     }
 
     // --- Item Management ---
     public void addItem(Item item) {
         if (item != null) {
             this.items.add(item);
         }
     }
 
     public boolean removeItem(Item item) {
         return this.items.remove(item);
     }
 
      public Item removeItem(int index) {
           if (index >= 0 && index < items.size()) {
                return items.remove(index);
           }
           return null;
      }
 
     public void clearItems() {
         this.items.clear();
     }
 
     // --- TODO: Monster/NPC Management ---
     // public Monster getMonster() { return monster; }
     // public void setMonster(Monster monster) { this.monster = monster; }
     // public ShopKeeper getShopKeeper() { return shopKeeper; }
     // ... etc ...
 
     @Override
     public String toString() {
         return "Room [Type=" + roomType + ", Visited=" + visited + ", Explored=" + explored + ", Items=" + items.size() + "]";
     }
 }