/**
 * @file    RPG.java
 * @brief   Represents game state for a player OR the central dungeon map structure.
 * Handles player stats, inventory, position, exploration/visited status,
 * and map generation logic (when used as the central map object).
 *
 * @author  Jack Schulte
 * @version 1.0.4
 * @date    2025-04-14
 *
 * @copyright Copyright (c) 2025 Jack Schulte. All rights reserved.
 * Strictly confidential and proprietary. Distribution, reproduction,
 * or modification is strictly prohibited without prior written permission.
 *
 * @version History:
 * - 1.0.4 (2025-04-14): Refactored map/explored/visited to be instance variables. Clarified player vs central map roles. Removed player setup from buildMap. (AI Assistant)
 * - 1.0.3 (2025-04-14): Added visitedMap tracking. (AI Assistant)
 * - 1.0.2 (2025-04-14): Added exploredMap tracking, exploreAround logic, and toggleMapVisibility debug feature. (J. Schulte)
 * - 1.0.1 (2025-04-01): Initial version creation with basic map generation and player classes. (J. Schulte)
 */

 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Random;
 
 public class RPG {
 
     // --- Instance Variables ---
     // For Central Map Object:
     public int[][] map; // Dungeon structure and room types
 
     // For Player Objects:
     public boolean[][] exploredMap; // Explored areas for this player
     public boolean[][] visitedMap;  // Visited areas for this player
     private int[] currentPos;       // Player's current [row, col]
     private int wayFacing;          // 0:N, 1:E, 2:S, 3:W
 
     // Player Stats & Info (Instance variables for each player)
     private int playerNum;          // Assigned player number (if needed)
     private String name;            // Player name (if used)
     private String nameClass;       // Assigned class name
     private int vig = 0;
     private int def = 0;
     private int str = 0;
     private int dex = 0;
     private int agt = 0;
     private int luck = 0;
     private int hp = 0;
     private InvSlot invSlot1;
     private InvSlot invSlot2;
     private InvSlot invSlot3;
     private InvSlot invSlot4;
 
     // --- Static Variables & Data ---
     private static int numPlayers = 0;
     private static int playerIndex = 0; // Index for assigning classes
     private static ArrayList<RPG> players = new ArrayList<>(); // List of active player RPG objects
     private static boolean showFullMap = false; // Global debug flag for map visibility
     private Random ran = new Random();      // Random number generator (can be instance or static)
 
     // Class Definitions (Static templates)
     private static final RPG knight = new RPG("Knight", 11, 12, 11, 10, 8, 8, InvSlot.empty, InvSlot.empty, InvSlot.empty, InvSlot.empty);
     private static final RPG sentinel = new RPG("Sentinel", 10, 13, 9, 11, 7, 10, InvSlot.empty, InvSlot.empty, InvSlot.empty, InvSlot.empty);
     private static final RPG assassin = new RPG("Assassin", 9, 8, 11, 12, 13, 7, InvSlot.empty, InvSlot.empty, InvSlot.empty, InvSlot.empty);
     private static final RPG caveman = new RPG("Caveman", 10, 10, 10, 10, 10, 10, InvSlot.empty, InvSlot.empty, InvSlot.empty, InvSlot.empty);
 
 
     // Default constructor: Used for creating player objects AND the central map object
     public RPG() {
         int mapSize = 21; // Or get from config/constant
         // Initialize maps specific to this instance (player or central map)
         this.map = new int[mapSize][mapSize]; // Only relevant for the central map object
         this.exploredMap = new boolean[mapSize][mapSize];
         this.visitedMap = new boolean[mapSize][mapSize];
 
         // Initialize all map arrays to default values
         for (int i = 0; i < mapSize; i++) {
             for (int j = 0; j < mapSize; j++) {
                 this.map[i][j] = 0; // Default to empty space
                 this.exploredMap[i][j] = false;
                 this.visitedMap[i][j] = false;
             }
         }
         // Initialize other player-specific fields to defaults if needed
         this.currentPos = new int[]{-1, -1}; // Indicate invalid position initially
         this.wayFacing = 0; // Default facing North
     }
 
     // Private constructor: Used ONLY for creating static class templates
     private RPG(String nameClass, int vig, int def, int str, int dex, int agt, int luck, InvSlot invSlot1, InvSlot invSlot2, InvSlot invSlot3, InvSlot invSlot4) {
         // Don't call default constructor here, templates don't need maps initialized
         this.nameClass = nameClass;
         this.vig = vig;
         this.def = def;
         this.str = str;
         this.dex = dex;
         this.agt = agt;
         this.luck = luck;
         this.hp = vig * 10; // Calculate initial HP for template
         this.invSlot1 = invSlot1;
         this.invSlot2 = invSlot2;
         this.invSlot3 = invSlot3;
         this.invSlot4 = invSlot4;
         // No position, explored, visited maps for templates
     }
 
 
     // --- Getters ---
     // Instance Getters (for Player objects)
     public int getStr() { return this.str; }
     public int getDex() { return this.dex; }
     public int[] getCurrentPos() { return this.currentPos; }
     public int getWayFacing() { return this.wayFacing; }
     public String getClassName() { return this.nameClass; }
     public boolean[][] getExploredMap() { return this.exploredMap; }
     public boolean[][] getVisitedMap() { return this.visitedMap; }
 
     // Static Getters (Global game state)
     public static int getNumPlayers() { return numPlayers; }
     public static int getPlayerIndex() { return playerIndex; }
     public static ArrayList<RPG> getPlayers() { return players; }
     public static boolean isShowFullMap() { return showFullMap; }
 
 
     // --- Setters ---
     // Instance Setters (for Player objects)
     public void setCurrentPos(int[] x) { this.currentPos = x; }
     public void setWayFacing(int x) { this.wayFacing = x; }
     // No direct setters for explored/visited, modified by move/explore methods
 
     // Static Setters (Global game state)
     public static void setNumPlayers(int x) { numPlayers = x; }
     public static void resetPlayerIndex() { playerIndex = 0; }
 
 
     // --- Player Management (Static Methods) ---
     public static void modPlayerIndex() { playerIndex++; }
 
     public static void createPlayers() {
         players.clear(); // Clear previous players if any
         for (int i = 0; i < numPlayers; i++) {
             players.add(new RPG()); // Add new player objects (uses default constructor)
         }
         resetPlayerIndex(); // Reset index for class assignment
     }
 
     public static void assignClass(int pIndex, String className) {
         if (pIndex < 0 || pIndex >= players.size()) {
             System.err.println("Error: Invalid player index for class assignment: " + pIndex);
             return;
         }
 
         RPG targetPlayer = players.get(pIndex); // Get the player object from the list
         RPG sourceClass = null;
 
         // Find the correct static class template
         switch (className) {
             case "Knight": sourceClass = knight; break;
             case "Sentinel": sourceClass = sentinel; break;
             case "Assassin": sourceClass = assassin; break;
             case "Caveman": sourceClass = caveman; break;
             default:
                 System.err.println("Error: Unknown class name: " + className);
                 return;
         }
 
         // Copy stats from the template to the player object
         targetPlayer.nameClass = sourceClass.nameClass;
         targetPlayer.vig = sourceClass.vig;
         targetPlayer.def = sourceClass.def;
         targetPlayer.str = sourceClass.str;
         targetPlayer.dex = sourceClass.dex;
         targetPlayer.agt = sourceClass.agt;
         targetPlayer.luck = sourceClass.luck;
         targetPlayer.hp = sourceClass.hp; // Use pre-calculated HP from template
         targetPlayer.invSlot1 = sourceClass.invSlot1; // Should ideally clone items if mutable
         targetPlayer.invSlot2 = sourceClass.invSlot2;
         targetPlayer.invSlot3 = sourceClass.invSlot3;
         targetPlayer.invSlot4 = sourceClass.invSlot4;
 
         System.out.println("Player " + (pIndex + 1) + " assigned class: " + className);
     }
 
 
     // --- Map Building Logic (Instance Methods - call on the central map object) ---
     public void buildMap() {
         // Modifies this instance's map array
         int mapSize = this.map.length; // Use size of the instance's map
         // Reset map to 0
         for (int i = 0; i < mapSize; i++) {
             for (int j = 0; j < mapSize; j++) {
                 this.map[i][j] = 0;
             }
         }
 
         ArrayList<int[]> roomList = new ArrayList<>(); // Local list for generation
         int rooms = 1;
 
         int startRow = mapSize / 2; // Center start
         int startCol = mapSize / 2;
         this.map[startRow][startCol] = 2; // Starting room type (mark on this instance's map)
         roomList.add(new int[]{startRow, startCol});
 
         // Room generation loop (operates on this.map)
         while (rooms < 50) { // Target room count
             Collections.shuffle(roomList, ran);
             if (roomList.isEmpty()) break; // Should not happen ideally
 
             int[] currentRoom = roomList.get(0);
             int r = currentRoom[0];
             int c = currentRoom[1];
             ArrayList<int[]> possibleMoves = findPossibleMoves(r, c); // Use helper
 
             if (!possibleMoves.isEmpty()) {
                 int[] nextRoom = possibleMoves.get(ran.nextInt(possibleMoves.size()));
                 int nr = nextRoom[0];
                 int nc = nextRoom[1];
 
                 this.map[nr][nc] = 1; // Place normal room marker
                 rooms++;
                 roomList.add(nextRoom);
             } else {
                 roomList.remove(0); // No moves from this room, remove it
             }
             if (rooms >= mapSize * mapSize / 2) break; // Safety break if too dense
         }
         System.out.println("Map built with " + rooms + " rooms.");
         // Note: Does NOT set player position or explored/visited status here.
     }
 
     // Helper for buildMap to find valid neighboring spots
     private ArrayList<int[]> findPossibleMoves(int row, int col) {
          ArrayList<int[]> moves = new ArrayList<>();
          int[] dr = {-1, 1, 0, 0}; // Directions: N, S, E, W row changes
          int[] dc = {0, 0, 1, -1}; // Directions: N, S, E, W col changes
 
          for(int i=0; i<4; i++) {
              int nr = row + dr[i];
              int nc = col + dc[i];
              if (isValidRoomForBuilding(nr, nc)) {
                  moves.add(new int[]{nr, nc});
              }
          }
          // Optional: Add logic for lineMoveHelper if desired bias exists
          return moves;
     }
 
     // Check validity during map building (operates on this.map)
     private boolean isValidRoomForBuilding(int row, int col) {
         if (row < 0 || row >= this.map.length || col < 0 || col >= this.map[0].length)
             return false; // Out of bounds
         if (this.map[row][col] != 0)
             return false; // Already occupied
         return !formsSquare(row, col); // Check 2x2 square rule
     }
 
     // Check if adding a room at [row, col] would form a 2x2 block (operates on this.map)
     private boolean formsSquare(int row, int col) {
         int[][] neighbors = {
             {-1, 0}, {0, -1}, {-1, -1}, // Check NW corner
             {-1, 0}, {0, 1}, {-1, 1},   // Check NE corner
             {1, 0}, {0, -1}, {1, -1},   // Check SW corner
             {1, 0}, {0, 1}, {1, 1}     // Check SE corner
         };
         for (int i = 0; i < neighbors.length; i += 3) {
             boolean n1_exists = isOccupied(row + neighbors[i][0], col + neighbors[i][1]);
             boolean n2_exists = isOccupied(row + neighbors[i+1][0], col + neighbors[i+1][1]);
             boolean corner_exists = isOccupied(row + neighbors[i+2][0], col + neighbors[i+2][1]);
             if (n1_exists && n2_exists && corner_exists) {
                 return true; // Forms a square
             }
         }
         return false;
     }
 
     // Helper to check if a map cell is occupied (used by formsSquare)
     private boolean isOccupied(int r, int c) {
          if (r < 0 || r >= this.map.length || c < 0 || c >= this.map[0].length)
              return false; // Out of bounds is not occupied
          return this.map[r][c] != 0;
     }
 
     // Add events to rooms (Instance method - call on the central map object)
     public void addEventsToRooms() {
         // Modifies this instance's map array to add room types like enemies, shops etc.
         int mapSize = this.map.length;
         int startRow = mapSize / 2;
         int startCol = mapSize / 2;
 
         ArrayList<int[]> availableRooms = new ArrayList<>();
         for (int r = 0; r < mapSize; r++) {
             for (int c = 0; c < mapSize; c++) {
                 // Add rooms of type '1' (basic generated rooms) to the available list
                 // Exclude the starting room itself
                 if (this.map[r][c] == 1 && !(r == startRow && c == startCol)) {
                     availableRooms.add(new int[]{r, c});
                 }
             }
         }
         Collections.shuffle(availableRooms, ran);
 
         // Simplified event placement logic (adjust percentages/counts as needed)
         int numRooms = availableRooms.size();
         int shops = Math.min(3, numRooms / 15);
         int shrines = Math.min(4, numRooms / 12);
         int treasures = Math.min(5, numRooms / 10);
         int smiths = Math.min(2, numRooms / 20);
         int difficult = Math.min(10, numRooms / 5);
 
         placeRoomType(availableRooms, 5, shops);     // Place shops
         placeRoomType(availableRooms, 9, shrines);   // Place shrines
         placeRoomType(availableRooms, 7, treasures); // Place treasures
         placeRoomType(availableRooms, 6, smiths);    // Place smiths
         placeRoomType(availableRooms, 4, difficult); // Place difficult enemies
 
         // Fill remaining rooms
         for (int[] room : availableRooms) {
             if (this.map[room[0]][room[1]] == 1) { // If still a basic room
                  int event = ran.nextInt(100);
                  if (event < 60) this.map[room[0]][room[1]] = 3; // Standard enemy (60%)
                  else this.map[room[0]][room[1]] = 8;            // Plain room (40%)
             }
         }
         this.map[startRow][startCol] = 2; // Ensure start room type is correct
         System.out.println("Added events to rooms.");
     }
 
     // Helper for addEventsToRooms
     private void placeRoomType(ArrayList<int[]> availableRooms, int roomType, int count) {
         int placed = 0;
         // Iterate backwards to allow safe removal
         for (int i = availableRooms.size() - 1; i >= 0 && placed < count; i--) {
             int[] room = availableRooms.get(i);
             if (this.map[room[0]][room[1]] == 1) { // Check if it's still a basic room
                 this.map[room[0]][room[1]] = roomType;
                 availableRooms.remove(i); // Remove from list once assigned
                 placed++;
             }
         }
         System.out.println("Placed " + placed + " rooms of type " + roomType);
     }
 
 
     // --- Utility & Action Methods (Instance Methods - call on Player objects) ---
 
     // Display health for this player instance
     public String showHealth() {
         StringBuilder healthBar = new StringBuilder();
         int currentHP = this.hp;
         int maxHP = this.vig * 10;
         maxHP = Math.max(1, maxHP);
         currentHP = Math.max(0, Math.min(currentHP, maxHP));
 
         int barLength = 20;
         int filledLength = (int) (((double) currentHP / maxHP) * barLength);
 
         healthBar.append("[");
         for (int i = 0; i < barLength; i++) {
             if (i < filledLength) healthBar.append("#"); else healthBar.append("-");
         }
         healthBar.append("] ").append(currentHP).append("/").append(maxHP);
         return healthBar.toString();
     }
 
     // Movement logic for this player instance
     // Takes the central map object as input to check validity
     public boolean movePlayer(int direction, int[][] worldMap) {
         if (this.currentPos == null || this.currentPos[0] < 0) return false; // Not initialized
 
         int nextRow = this.currentPos[0];
         int nextCol = this.currentPos[1];
 
         switch (direction) {
             case 0: nextRow--; break; // N
             case 1: nextCol++; break; // E
             case 2: nextRow++; break; // S
             case 3: nextCol--; break; // W
             default: return false;
         }
 
         // Check validity against the worldMap
         if (nextRow >= 0 && nextRow < worldMap.length && nextCol >= 0 && nextCol < worldMap[0].length && worldMap[nextRow][nextCol] != 0)
         {
             this.currentPos[0] = nextRow;
             this.currentPos[1] = nextCol;
             this.visitedMap[nextRow][nextCol] = true; // Mark visited for this player
             this.exploreAround(nextRow, nextCol, worldMap); // Explore around new position for this player
             System.out.println("Player moved to [" + nextRow + ", " + nextCol + "]. Room Type: " + worldMap[nextRow][nextCol]);
             return true;
         } else {
             System.out.println("Cannot move into [" + nextRow + ", " + nextCol + "]");
             return false;
         }
     }
 
     // Turn logic for this player instance
     public void turnPlayer(int turnDirection) { // 0=left (CCW), 1=right (CW)
         if (turnDirection == 0) {
             this.wayFacing = (this.wayFacing + 3) % 4;
         } else if (turnDirection == 1) {
             this.wayFacing = (this.wayFacing + 1) % 4;
         }
         System.out.println("Player facing direction: " + this.wayFacing);
     }
 
     // Exploration logic for this player instance
     // Takes the central map object to check for actual rooms
     public void exploreAround(int row, int col, int[][] worldMap) {
         // Mark 3x3 area on this player's exploredMap if it's a valid room in worldMap
         for (int dr = -1; dr <= 1; dr++) {
             for (int dc = -1; dc <= 1; dc++) {
                 int nr = row + dr;
                 int nc = col + dc;
                 // Check bounds
                 if (nr >= 0 && nr < this.exploredMap.length && nc >= 0 && nc < this.exploredMap[0].length) {
                     // Only explore if it's a room in the actual world map
                     if(nr >= 0 && nr < worldMap.length && nc >= 0 && nc < worldMap[0].length && worldMap[nr][nc] != 0) {
                        this.exploredMap[nr][nc] = true;
                     }
                 }
             }
         }
     }
 
     // --- Static Map Toggle ---
     public static void toggleMapVisibility() {
         showFullMap = !showFullMap;
         System.out.println("Map visibility toggled. Show full map: " + showFullMap);
     }
 
     // --- Print map (for debugging, operates on instance's map) ---
      public void printMap(String title) {
          System.out.println("\n--- " + title + " ---");
          int size = this.map.length;
          for (int i = 0; i < size; i++) {
              for (int j = 0; j < size; j++) {
                  System.out.printf("%-2d ", this.map[i][j]);
              }
              System.out.println();
          }
          System.out.println("-------------------");
      }
       // --- Print boolean map (for debugging explored/visited) ---
       public void printBooleanMap(String title, boolean[][] boolMap) {
           System.out.println("\n--- " + title + " ---");
           int size = boolMap.length;
           for (int i = 0; i < size; i++) {
               for (int j = 0; j < size; j++) {
                   System.out.printf("%-2s ", boolMap[i][j] ? "T" : "F");
               }
               System.out.println();
           }
           System.out.println("-------------------");
       }
 }