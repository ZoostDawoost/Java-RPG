/**
 * @file    Player.java
 * @brief   Represents a player character in the game.
 * Encapsulates player stats, inventory, position, state, and actions.
 *
 * @author  Jack Schulte
 * @version 1.0.0
 * @date    2025-04-15
 *
 * @copyright Copyright (c) 2025 Jack Schulte. All rights reserved.
 * Strictly confidential and proprietary. Distribution, reproduction,
 * or modification is strictly prohibited without prior written permission.
 */

 import javax.swing.ImageIcon;
 import java.util.List; // Required for Item list return type
 
 public class Player {
 
     // --- Player Attributes ---
     private String characterName;
     private String className;
     private ImageIcon selectedCharacterIcon;
     private int[] currentPos;       // Player's current [row, col] on the board
     private int wayFacing;          // 0:N, 1:E, 2:S, 3:W
     private Inventory inventory;
     private Weapon equippedWeapon;
 
     // Player Stats
     private int vig = 0; private int agt = 0; private int def = 0;
     private int str = 0; private int dex = 0; private int luck = 0;
     private int hp = 0; private int maxHp = 100;
     private int energy = 100; private int maxEnergy = 100;
     private int score = 0;
 
     /**
      * Constructor for Player.
      */
     public Player() {
         // Initialize with default values
         this.currentPos = new int[]{-1, -1}; // Invalid initial position
         this.wayFacing = 0; // Default facing North
         this.inventory = new Inventory(RPG.DEFAULT_INVENTORY_CAPACITY); // Use constant from RPG
         this.equippedWeapon = null;
         this.selectedCharacterIcon = null;
         this.characterName = "Adventurer";
         this.className = "Unknown";
         // Base stats are set via assignClass later
         this.maxHp = 1; this.hp = 1;         // Avoid divide by zero before stats assigned
         this.maxEnergy = 1; this.energy = 1; // Avoid divide by zero
         this.score = 0;
     }
 
     // --- Getters ---
     public String getCharacterName() { return characterName; }
     public String getClassName() { return className; }
     public ImageIcon getSelectedCharacterIcon() { return selectedCharacterIcon; }
     public int[] getCurrentPos() { return currentPos; }
     public int getWayFacing() { return wayFacing; }
     public Inventory getInventory() { return inventory; }
     public Weapon getEquippedWeapon() { return equippedWeapon; }
     public int getVig() { return vig; } public int getAgt() { return agt; }
     public int getDef() { return def; } public int getStr() { return str; }
     public int getDex() { return dex; } public int getLuck() { return luck; }
     public int getHp() { return hp; } public int getMaxHp() { return maxHp; }
     public int getEnergy() { return energy; } public int getMaxEnergy() { return maxEnergy; }
     public int getScore() { return score; }
 
     // --- Setters ---
     public void setCharacterName(String name) { this.characterName = (name != null && !name.trim().isEmpty()) ? name : "Adventurer"; }
     public void setClassName(String name) { this.className = name; }
     public void setSelectedCharacterIcon(ImageIcon icon) { this.selectedCharacterIcon = icon; }
     public void setCurrentPos(int[] pos) { this.currentPos = pos; }
     public void setWayFacing(int facing) { this.wayFacing = facing % 4; }
     public void setVig(int v) { this.vig = v; } public void setAgt(int a) { this.agt = a; }
     public void setDef(int d) { this.def = d; } public void setStr(int s) { this.str = s; }
     public void setDex(int d) { this.dex = d; } public void setLuck(int l) { this.luck = l; }
     public void setMaxHp(int mh) { this.maxHp = Math.max(1, mh); } // Ensure maxHP is at least 1
     public void setHp(int h) { this.hp = Math.max(0, Math.min(h, this.maxHp)); }
     public void setMaxEnergy(int me) { this.maxEnergy = Math.max(1, me); } // Ensure maxEnergy is at least 1
     public void setEnergy(int e) { this.energy = Math.max(0, Math.min(e, this.maxEnergy)); }
     public void addScore(int points) { this.score += points; }
     public void setEquippedWeapon(Weapon w) { this.equippedWeapon = w; } // Simple setter for now
 
     // --- Player Actions ---
 
     /**
      * Moves the player instance in the specified direction.
      * Checks for validity against the central game board.
      * Updates the visited status of the entered room.
      * Decrements energy. Triggers exploration. Updates UI.
      * @param direction 0:N, 1:E, 2:S, 3:W
      * @param board The central game board to check against.
      * @return true if the move was successful, false otherwise.
      */
     public boolean movePlayer(int direction, Board board) {
         if (board == null) {
              System.err.println("Error: Board not available for movement check.");
              return false;
         }
          if (this.currentPos == null || this.currentPos[0] < 0) {
               System.err.println("Error: Player position not initialized for movement.");
               return false;
          }
         if (this.energy <= 0) {
             UIManagement.addDialogue("Not enough energy to move!");
             return false;
         }
 
         int nextRow = this.currentPos[0];
         int nextCol = this.currentPos[1];
 
         switch (direction) {
             case 0: nextRow--; break; // N
             case 1: nextCol++; break; // E
             case 2: nextRow++; break; // S
             case 3: nextCol--; break; // W
             default: return false;
         }
 
         // Get the target room from the board
         Room targetRoom = board.getRoom(nextRow, nextCol);
 
         // Check if the target room is valid and traversable
         if (targetRoom != null && targetRoom.isTraversable())
         {
             this.currentPos[0] = nextRow;
             this.currentPos[1] = nextCol;
             targetRoom.setVisited(true); // Mark room as visited ON THE BOARD'S ROOM OBJECT
 
             // Explore around the new position (affects Room.explored status on the board)
             this.exploreAround(nextRow, nextCol, board);
 
             this.setEnergy(this.energy - 1); // Decrease energy
 
             String roomDesc = targetRoom.getDescription();
             System.out.println(this.characterName + " moved to [" + nextRow + ", " + nextCol + "]. Room: " + roomDesc + ". Energy: " + this.energy);
             UIManagement.addDialogue("Entered " + roomDesc + ".");
 
             // Handle Room Contents
             List<Item> itemsInRoom = targetRoom.getItems();
             if (!itemsInRoom.isEmpty()) {
                  UIManagement.addDialogue("You see " + itemsInRoom.get(0).getItemName() + (itemsInRoom.size() > 1 ? " and other items." : "."));
                  // TODO: Add logic for player to interact/pickup items
             }
             // TODO: Handle Monsters / NPCs
 
             UIManagement.updateGameStatus(this); // Update UI
 
             return true;
         } else {
             String reason = (targetRoom == null) ? "the void" : "an obstacle";
             System.out.println(this.characterName + " cannot move into [" + nextRow + ", " + nextCol + "] (" + reason + ")");
              UIManagement.addDialogue("Cannot move that way.");
             return false;
         }
     }
 
     /**
      * Turns the player instance left (0) or right (1).
      * @param turnDirection 0 for left (CCW), 1 for right (CW).
      */
     public void turnPlayer(int turnDirection) {
         if (turnDirection == 0) {
             this.wayFacing = (this.wayFacing + 3) % 4; // Left turn
         } else if (turnDirection == 1) {
             this.wayFacing = (this.wayFacing + 1) % 4; // Right turn
         }
         UIManagement.updateGameStatus(this); // Update UI (map direction indicator)
     }
 
     /**
      * Exploration logic for this player instance.
      * Marks 3x3 area around the player as explored (sets Room.explored = true)
      * on the central game board if it's a valid, non-empty room.
      * Plays a random monster sound if a newly explored room is a monster room.
      *
      * @param playerRow The player's current row.
      * @param playerCol The player's current column.
      * @param board The central game board.
      */
     public void exploreAround(int playerRow, int playerCol, Board board) {
          if (board == null) {
               System.err.println("Error: Board not available for exploration.");
               return;
          }
         // Mark 3x3 area on the central board
         for (int dr = -1; dr <= 1; dr++) {
             for (int dc = -1; dc <= 1; dc++) {
                 int nr = playerRow + dr;
                 int nc = playerCol + dc;
 
                 Room room = board.getRoom(nr, nc);
 
                 // Check if room exists and is not empty space
                 if (room != null && room.getRoomType() != Room.RoomType.EMPTY_SPACE) {
                     boolean wasExplored = room.isExplored(); // Check before setting
 
                     // Set explored status on the Room object
                     room.setExplored(true);
 
                     // Play sound if it's a NEWLY explored MONSTER room
                     if (!wasExplored) {
                         Room.RoomType roomType = room.getRoomType();
                         if (roomType == Room.RoomType.ENEMY_ROOM || roomType == Room.RoomType.DIFFICULT_ENEMY_ROOM) {
                             int soundNum = RPG.ran.nextInt(3) + 1; // Use RPG's static random
                             String soundPath = String.format("audio/sound-monster-%02d.wav", soundNum);
                             System.out.println("Newly explored monster room at [" + nr + "," + nc + "]. Playing sound."); // Debug
                             RPG.playAudio(soundPath); // Use RPG's static method
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Equips a weapon. Replaces the currently equipped weapon.
      * Adds the previously equipped weapon back to inventory if possible.
      * @param weapon The Weapon to equip.
      */
      public void equipWeapon(Weapon weapon) {
          if (weapon == null) {
              // Unequip action
              if (this.equippedWeapon != null) {
                  if (!this.inventory.addItem(this.equippedWeapon)) {
                      UIManagement.addDialogue("Inventory full! Cannot unequip " + this.equippedWeapon.getItemName() + ".");
                      // Item is lost? Or drop on floor? For now, lost.
                  } else {
                      UIManagement.addDialogue("Unequipped " + this.equippedWeapon.getItemName() + ".");
                  }
                  this.equippedWeapon = null;
              }
              return;
          }
 
           // Check requirements
           if (this.getStr() < weapon.getStrReq() || this.getDex() < weapon.getDexReq()) {
               UIManagement.addDialogue("You don't meet the requirements to equip the " + weapon.getItemName() + "!");
               return;
           }
 
          // Unequip old weapon first (if one exists)
          if (this.equippedWeapon != null) {
               if (!this.inventory.addItem(this.equippedWeapon)) {
                    UIManagement.addDialogue("Inventory full! Cannot unequip " + this.equippedWeapon.getItemName() + " to equip new one.");
                    return; // Cannot proceed with equipping
               } else {
                    UIManagement.addDialogue("Unequipped " + this.equippedWeapon.getItemName() + ".");
               }
          }
 
          // Equip the new weapon
          this.equippedWeapon = weapon;
          // Remove the newly equipped weapon from inventory if it was there
          this.inventory.removeItem(weapon);
          System.out.println(this.characterName + " equipped " + weapon.getItemName());
          UIManagement.addDialogue("Equipped " + weapon.getItemName() + ".");
          UIManagement.updateGameStatus(this);
      }
 
      /**
       * Picks up an item from the specified room and adds it to inventory.
       * @param room The room to pick up from.
       * @param itemIndex The index of the item within the room's item list.
       */
      public void pickupItemFromRoom(Room room, int itemIndex) {
          if (room == null) return;
          Item itemToPickup = room.removeItem(itemIndex); // Try to remove from room first
 
          if (itemToPickup != null) {
              if (this.inventory.addItem(itemToPickup)) {
                  // Successfully added to inventory
                  UIManagement.addDialogue("Picked up " + itemToPickup.getItemName() + ".");
                  System.out.println(this.characterName + " picked up " + itemToPickup.getItemName() + ".");
              } else {
                  // Inventory was full, put item back in the room
                  room.addItem(itemToPickup); // Add it back
                  UIManagement.addDialogue("Your inventory is full! Couldn't pick up " + itemToPickup.getItemName() + ".");
                  System.out.println("Inventory full, could not pick up " + itemToPickup.getItemName() + ".");
              }
              UIManagement.updateGameStatus(this);
          } else {
               System.err.println("Failed to remove item at index " + itemIndex + " from room.");
          }
      }
 
      /**
       * Uses an item from the player's inventory.
       * @param inventoryIndex The index of the item in the player's inventory.
       */
      public void useItemFromInventory(int inventoryIndex) {
          Item item = this.inventory.getItem(inventoryIndex);
          if (item != null) {
              // Polymorphic call - Weapon overrides this to equip
              item.use(this);
              // TODO: Add logic for consuming items (e.g., potions)
              // if (item instanceof Consumable) { inventory.removeItem(inventoryIndex); }
              UIManagement.updateGameStatus(this);
          } else {
              System.err.println("No item found at inventory index: " + inventoryIndex);
          }
      }
 
     // --- Utility ---
     public String showHealthText() {
         return this.hp + "/" + this.maxHp;
     }
 
 }