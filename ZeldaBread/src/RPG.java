/**
 * @file    RPG.java
 * @brief   Represents game state for a player OR the central dungeon map structure.
 * Handles player stats, inventory, position, exploration/visited status, selected character image, character name,
 * map generation logic (when used as the central map object), static management of character image sets,
 * and serves as the application entry point (including window setup like centering and initial audio playback).
 * Plays monster sounds when monster rooms are newly explored. Ensures adjacent-to-start rooms are not monsters initially.
 *
 * @author  Jack Schulte & AI Assistant & Gemini
 * @version 1.2.1 (Modified)
 * @date    2025-04-15
 *
 * @copyright Copyright (c) 2025 Jack Schulte & AI Assistant & Gemini. All rights reserved.
 * Strictly confidential and proprietary. Distribution, reproduction,
 * or modification is strictly prohibited without prior written permission.
 *
 * @version History:
 * - 1.2.1 (2025-04-15): Modified buildMap to select a random room from the list for expansion instead of the first, increased room target to 300 to improve grid utilization. (Gemini)
 * - 1.2.0 (2025-04-15): Increased mapSize to 30. Increased room generation target in buildMap to 150. Reduced monster room frequency in addEventsToRooms. (Gemini)
 * - 1.1.7 (2025-04-15): Modified addEventsToRooms to prevent monster rooms (type 3 or 4) from being placed adjacent to the start room. Adjacent rooms are set to type 8 (plain). (Gemini)
 * - 1.1.6 (2025-04-15): Modified exploreAround to play random monster sound (1-3) when a monster room (type 3 or 4) is explored for the first time. (Gemini)
 * - 1.1.5 (2025-04-14): Moved audio playback to occur *after* the main frame is set to visible. (Gemini)
 * - 1.1.4 (2025-04-14): Added audio playback for "game-intro-01.mp3" on startup using javax.sound.sampled. (AI Assistant)
 * - 1.1.3 (2025-04-14): Switched icon loading to use a PNG file (bread-man-32.png) with ImageIO.read(). (AI Assistant)
 * - 1.1.2 (2025-04-14): Reverted icon loading to Toolkit + MediaTracker with improved waiting and error checking. (AI Assistant)
 * - 1.1.1 (2025-04-14): Replaced icon loading logic with ImageIO.read() for better .ico handling. (AI Assistant)
 * - 1.1.0 (2025-04-14): Added window centering on startup (`setLocationRelativeTo`). Attempted fix for icon loading error using ImageIcon as primary loader. (AI Assistant)
 * - 1.0.9 (2025-04-14): Added characterName field. Refactored class stat assignment to use CharacterClassInfo. Removed static class templates. (AI Assistant)
 * - 1.0.8 (2025-04-14): Added static image set selection and instance variable/methods to store selected character ImageIcon. (AI Assistant)
 * - 1.0.7 (2025-04-14): Added public static getters for class template HP/Energy to fix access issue from UIManagement. (AI Assistant)
 * - 1.0.6 (2025-04-14): Added Score, Energy, HP stats. Implemented energy decrement on move. Added dialogue trigger on move. Defined stats per class. (AI Assistant)
 * - 1.0.5 (2025-04-14): Moved main method from UIManagement.java to RPG.java. Made UIManagement methods public static for access. (AI Assistant)
 * - 1.0.4 (2025-04-14): Refactored map/explored/visited to be instance variables. Clarified player vs central map roles. Removed player setup from buildMap. (AI Assistant)
 * - 1.0.3 (2025-04-14): Added visitedMap tracking. (AI Assistant)
 * - 1.0.2 (2025-04-14): Added exploredMap tracking, exploreAround logic, and toggleMapVisibility debug feature. (J. Schulte)
 * - 1.0.1 (2025-04-01): Initial version creation with basic map generation and player classes. (J. Schulte)
 */
import javax.imageio.ImageIO; // Needed for reading PNG
import javax.sound.sampled.*; // Needed for audio playback
import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;       // Ensure Random is imported
import java.util.Iterator;     // Needed for safe removal while iterating
import java.awt.Image;
import java.awt.MediaTracker; // Keep import, just in case, but not used for PNG
import java.awt.Toolkit;     // Keep import, just in case, but not used for PNG
import java.awt.image.BufferedImage; // Needed for ImageIO result
import java.io.BufferedInputStream; // Recommended for audio stream reading
import java.io.InputStream;          // For reading resource streams
import java.net.URL;
import java.io.IOException; // Needed for ImageIO and Audio exceptions
import java.io.File;      // If accessing file system directly (use getResourceAsStream instead)


public class RPG {

    // --- Constants ---
    private static final int MAP_SIZE = 30; // Map dimension
    private static final int ROOM_GENERATION_TARGET = 300; // Increased target number of rooms again
    private static final int DIFFICULT_ENEMY_DIVISOR = 10; // Lower proportion of difficult enemies
    private static final int DIFFICULT_ENEMY_CAP = 15;    // Cap for difficult enemies
    private static final int STANDARD_ENEMY_PERCENTAGE = 35; // Lower percentage chance for standard enemies

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
    private String characterName;   // Player's chosen/assigned name
    private String nameClass;       // Assigned class name
    private ImageIcon selectedCharacterIcon; // Image chosen for this player
    private int vig = 0;            // Base vitality
    private int agt = 0;            // Base agility
    private int def = 0;
    private int str = 0;
    private int dex = 0;
    private int luck = 0;
    private int hp = 0;             // Current Hit Points
    private int maxHp = 100;        // Maximum Hit Points
    private int energy = 100;       // Current Energy
    private int maxEnergy = 100;    // Maximum Energy
    private int score = 0;          // Player score
    private InvSlot invSlot1;
    private InvSlot invSlot2;
    private InvSlot invSlot3;
    private InvSlot invSlot4;

    // --- Static Variables & Data ---
    private static int numPlayers = 0;
    private static int playerIndex = 0; // Index for assigning classes
    private static ArrayList<RPG> players = new ArrayList<>(); // List of active player RPG objects
    private static boolean showFullMap = false; // Global debug flag for map visibility
    private static Random ran = new Random();      // Random number generator (reused for sounds)

    // Character Image Set Management
    private static final List<String> IMAGE_SETS = Arrays.asList("bread", "dark", "hobbits", "speedracer");
    public static String currentImageSet; // Publicly accessible selected set

    // Static initializer block to select the image set at startup
    static {
        // *** TEMPORARY OVERRIDE ***
        currentImageSet = "speedracer";
        System.out.println("!!! OVERRIDE: Using image set: " + currentImageSet + " !!!");
    }


    // Default constructor: Used for creating player objects AND the central map object
    public RPG() {
        // Initialize maps specific to this instance (player or central map) using MAP_SIZE constant
        this.map = new int[MAP_SIZE][MAP_SIZE]; // Only relevant for the central map object
        this.exploredMap = new boolean[MAP_SIZE][MAP_SIZE];
        this.visitedMap = new boolean[MAP_SIZE][MAP_SIZE];

        // Initialize all map arrays to default values
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                this.map[i][j] = 0; // Default to empty space
                this.exploredMap[i][j] = false;
                this.visitedMap[i][j] = false;
            }
        }
        // Initialize other player-specific fields to defaults if needed
        this.currentPos = new int[]{-1, -1}; // Indicate invalid position initially
        this.wayFacing = 0; // Default facing North
        this.score = 0; // Start score at 0
        this.selectedCharacterIcon = null; // No icon initially
        this.characterName = "Adventurer"; // Default name
        // Initialize inventory slots to empty
        this.invSlot1 = InvSlot.empty;
        this.invSlot2 = InvSlot.empty;
        this.invSlot3 = InvSlot.empty;
        this.invSlot4 = InvSlot.empty;
    }


   // --- Application Entry Point ---
   public static void main(String[] args) {
       // --- Play Startup Sound LATER ---
       // Moved playback call to after frame.setVisible(true)

       SwingUtilities.invokeLater(() -> { //
           // Setup UI components first
           UIManagement.setupUI(); //

           // --- Window Icon Setting Attempt (Using PNG with ImageIO) ---
           String iconFileName = "bread-man-32.png"; // Choose an appropriate size
           URL iconURL = RPG.class.getClassLoader().getResource("icons/" + iconFileName); //

           if (iconURL != null) {
               System.out.println("Found icon resource at: " + iconURL);
               try {
                   // Use ImageIO.read() for PNG files - generally reliable
                   BufferedImage iconImage = ImageIO.read(iconURL); //
                   if (iconImage != null) {
                       // Set the image directly
                       UIManagement.frame.setIconImage(iconImage); //
                       System.out.println("Window icon '" + iconFileName + "' loaded and set successfully using ImageIO.");
                   } else {
                       System.err.println("ImageIO.read returned null for the icon URL: " + iconURL + ". Check file integrity/format.");
                   }
               } catch (IOException e) {
                   System.err.println("IOException while reading icon using ImageIO from URL: " + iconURL);
                   e.printStackTrace(); // Print stack trace for detailed debugging
               } catch (Exception e) {
                   // Catch other potential runtime exceptions during image loading/decoding
                   System.err.println("Unexpected error loading icon using ImageIO from URL: " + iconURL);
                   e.printStackTrace();
               }
           } else {
               // Resource URL was not found by the ClassLoader
               System.err.println("Error loading icon: Resource 'icons/" + iconFileName + "' not found in classpath. Ensure it's in the correct 'icons' folder relative to the classpath root.");
           }
           // --- End Icon Setting ---

           // --- Center Window ---
           UIManagement.frame.setLocationRelativeTo(null); //
           System.out.println("Centering window on screen.");

           // Show the initial welcome screen
           UIManagement.welcomeScreen(); //

           // Make the frame visible LAST
           UIManagement.frame.setVisible(true); //
           System.out.println("Frame set to visible.");

           // --- Play Startup Sound NOW ---
           // IMPORTANT: Ensure the 'audio' folder is in your project's classpath
           // (e.g., inside the 'src' or a 'resources' folder that gets included in the build)
           playAudio("audio/game-intro-01.wav"); // Moved here
           // --- End Startup Sound ---
       });
   }

    // --- Audio Playback Method ---
    public static void playAudio(String resourcePath) { //
        try {
            // Use getResourceAsStream to load from classpath
            InputStream audioSrc = RPG.class.getClassLoader().getResourceAsStream(resourcePath); //
            if (audioSrc == null) {
                System.err.println("Audio resource not found: " + resourcePath);
                return;
            }
            // Wrap in BufferedInputStream for efficiency
            InputStream bufferedIn = new BufferedInputStream(audioSrc); //
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn); //
            Clip clip = AudioSystem.getClip(); //
            clip.open(audioStream);
            clip.start(); // Play the sound once
            System.out.println("Playing audio: " + resourcePath);

            // Optional: Add a listener to close resources when done
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) { //
                    try {
                         clip.close();
                         audioStream.close();
                         bufferedIn.close();
                         audioSrc.close(); // Close original stream too
                         System.out.println("Audio resources closed for: " + resourcePath);
                    } catch (IOException e) {
                         System.err.println("IOException closing audio resources: " + e.getMessage());
                         e.printStackTrace();
                    }
                }
            });

        } catch (UnsupportedAudioFileException e) { //
            System.err.println("Unsupported audio file format for: " + resourcePath + " - " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) { //
            System.err.println("IOException loading/playing audio: " + resourcePath + " - " + e.getMessage());
            e.printStackTrace();
        } catch (LineUnavailableException e) { //
            System.err.println("Audio line unavailable for playback: " + resourcePath + " - " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error playing audio: " + resourcePath + " - " + e.getMessage());
            e.printStackTrace();
        }
    }


    // --- Getters ---
    // Instance Getters (for Player objects)
    public int getVig() { return this.vig; }
    public int getDef() { return this.def; }
    public int getAgt() { return this.agt; }
    public int getLuck() { return this.luck; }
    public int getStr() { return this.str; }
    public int getDex() { return this.dex; }
    public int[] getCurrentPos() { return this.currentPos; }
    public int getWayFacing() { return this.wayFacing; }
    public String getClassName() { return this.nameClass; }
    public String getCharacterName() { return this.characterName; } // Getter for name
    public ImageIcon getSelectedCharacterIcon() { return this.selectedCharacterIcon; } // Getter for icon
    public boolean[][] getExploredMap() { return this.exploredMap; }
    public boolean[][] getVisitedMap() { return this.visitedMap; }
    public int getHp() { return this.hp; }
    public int getMaxHp() { return this.maxHp; }
    public int getEnergy() { return this.energy; }
    public int getMaxEnergy() { return this.maxEnergy; }
    public int getScore() { return this.score; }

    // Static Getters (Global game state)
    public static int getNumPlayers() { return numPlayers; }
    public static int getPlayerIndex() { return playerIndex; }
    public static ArrayList<RPG> getPlayers() { return players; }
    public static boolean isShowFullMap() { return showFullMap; }


    // --- Setters ---
    // Instance Setters (for Player objects)
    public void setCurrentPos(int[] x) { this.currentPos = x; }
    public void setWayFacing(int x) { this.wayFacing = x; }
    public void setSelectedCharacterIcon(ImageIcon icon) { this.selectedCharacterIcon = icon; } // Setter for icon
    public void setCharacterName(String name) { this.characterName = (name != null && !name.trim().isEmpty()) ? name : "Adventurer"; } // Setter for name
    public void setHp(int hp) { this.hp = Math.max(0, Math.min(hp, this.maxHp)); }
    public void setEnergy(int energy) { this.energy = Math.max(0, Math.min(energy, this.maxEnergy)); }
    public void addScore(int points) { this.score += points; }

    // Static Setters (Global game state)
    public static void setNumPlayers(int x) { numPlayers = x; }
    public static void resetPlayerIndex() { playerIndex = 0; }


    // --- Player Management (Static Methods) ---
    public static void modPlayerIndex() { playerIndex++; } //

    public static void createPlayers() { //
        players.clear(); // Clear previous players if any
        for (int i = 0; i < numPlayers; i++) {
            players.add(new RPG()); // Add new player objects (uses default constructor)
            players.get(i).playerNum = i + 1; // Assign player number
        }
        resetPlayerIndex(); // Reset index for class assignment
    }

    /**
     * Assigns CLASS STATS to a player object based on the selected className.
     * Uses the CharacterClassInfo module to retrieve base stats.
     * Icon and Name are assigned separately via their respective setters.
     *
     * @param pIndex    The index of the player in the static players list.
     * @param className The name of the class selected (e.g., "Knight").
     */
    public static void assignClass(int pIndex, String className) { //
        if (pIndex < 0 || pIndex >= players.size()) {
            System.err.println("Error: Invalid player index for class assignment: " + pIndex);
            return;
        }

        RPG targetPlayer = players.get(pIndex); // Get the player object from the list
        CharacterClassInfo.BaseStats sourceStats = CharacterClassInfo.getStats(className); // Get stats from new module

        if (sourceStats == null) {
             System.err.println("Error: Unknown class name or stats not found for: " + className);
             // Assign some default safe stats? Or leave as is? Leaving as is for now.
             targetPlayer.nameClass = "Unknown";
             targetPlayer.maxHp = 50;
             targetPlayer.maxEnergy = 50;
             targetPlayer.hp = 50;
             targetPlayer.energy = 50;
             targetPlayer.score = 0;
             // Ensure inventory is empty too
             targetPlayer.invSlot1 = InvSlot.empty;
             targetPlayer.invSlot2 = InvSlot.empty;
             targetPlayer.invSlot3 = InvSlot.empty;
             targetPlayer.invSlot4 = InvSlot.empty;
             return;
        }

        // Copy stats from the CharacterClassInfo to the player object
        targetPlayer.nameClass = className; // Store the class name itself
        targetPlayer.vig = sourceStats.VIG;
        targetPlayer.def = sourceStats.DEF;
        targetPlayer.str = sourceStats.STR;
        targetPlayer.dex = sourceStats.DEX;
        targetPlayer.agt = sourceStats.AGT;
        targetPlayer.luck = sourceStats.LUCK;
        targetPlayer.maxHp = sourceStats.MAX_HP;       // Copy max HP
        targetPlayer.maxEnergy = sourceStats.MAX_ENERGY; // Copy max Energy
        targetPlayer.hp = targetPlayer.maxHp;       // Set current HP to max
        targetPlayer.energy = targetPlayer.maxEnergy; // Set current Energy to max
        targetPlayer.score = 0;                     // Initialize score
        // Reset inventory slots (could be based on class later)
        targetPlayer.invSlot1 = InvSlot.empty;
        targetPlayer.invSlot2 = InvSlot.empty;
        targetPlayer.invSlot3 = InvSlot.empty;
        targetPlayer.invSlot4 = InvSlot.empty;
        // NAME AND ICON ARE SET SEPARATELY by UIManagement

        System.out.println("Player " + (pIndex + 1) + " assigned class: " + className + " (HP: " + targetPlayer.maxHp + ", Energy: " + targetPlayer.maxEnergy + ")");
    }


    // --- Map Building Logic (Instance Methods - call on the central map object) ---
    /**
     * Builds the dungeon map structure. Uses a randomized expansion approach.
     * Selects a random existing room from the list (`roomList`) to expand from
     * in each step, rather than always using the most recently added or first element.
     * Aims for better grid utilization by encouraging growth from different parts
     * of the existing structure.
     */
    public void buildMap() {
        // Modifies this instance's map array
        int mapSize = this.map.length; // Use size of the instance's map (now 30)
        // Reset map to 0
        for (int i = 0; i < mapSize; i++) {
            for (int j = 0; j < mapSize; j++) {
                this.map[i][j] = 0;
            }
        }

        ArrayList<int[]> roomList = new ArrayList<>(); // List of rooms that can be expanded from
        int rooms = 1;

        int startRow = mapSize / 2; // Center start
        int startCol = mapSize / 2;
        this.map[startRow][startCol] = 2; // Starting room type
        roomList.add(new int[]{startRow, startCol}); // Add start room to the list

        // Room generation loop (operates on this.map)
        while (rooms < ROOM_GENERATION_TARGET) { // Target room count (now 300)
            if (roomList.isEmpty()) {
                System.out.println("Warning: Room list became empty during map generation at " + rooms + " rooms.");
                break; // Stop if no more rooms can be expanded from
            }

            // *** CHANGE: Pick a RANDOM room from the list to expand from ***
            int currentRoomIndex = ran.nextInt(roomList.size());
            int[] currentRoom = roomList.get(currentRoomIndex);
            int r = currentRoom[0];
            int c = currentRoom[1];

            // Find possible valid neighbor cells to turn into rooms
            ArrayList<int[]> possibleMoves = findPossibleMoves(r, c); // Use helper

            if (!possibleMoves.isEmpty()) {
                // Pick one valid neighbor randomly to become the next room
                int[] nextRoom = possibleMoves.get(ran.nextInt(possibleMoves.size()));
                int nr = nextRoom[0];
                int nc = nextRoom[1];

                this.map[nr][nc] = 1; // Place normal room marker
                rooms++;
                roomList.add(nextRoom); // Add the newly created room to the list for potential future expansion

                // Optional: Decide if the parent room (currentRoom) should be removed
                // from the list after successfully expanding. Removing it might create
                // more linear/branching structures. Keeping it allows it to potentially
                // branch again later. Let's keep it for now to allow more branching.
                // roomList.remove(currentRoomIndex); // <-- Uncomment to make it more like a pure random traversal

            } else {
                // *** CHANGE: Remove the room AT THE RANDOMLY SELECTED INDEX if it has no valid moves ***
                // This room is a dead end or surrounded, remove it from expansion candidates.
                roomList.remove(currentRoomIndex);
            }

             // Safety break: reduced density check for larger map
            if (rooms >= mapSize * mapSize * 0.7) {
                 System.out.println("Stopping map generation early due to density limit (70%).");
                 break; // Stop if map becomes too dense (70%)
             }
        }
        System.out.println("Map built with " + rooms + " rooms on a " + mapSize + "x" + mapSize + " grid.");
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
        if (row <= 0 || row >= this.map.length - 1 || col <= 0 || col >= this.map[0].length - 1)
            return false; // Avoid building right at the edge (leave a border)
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

    /**
     * Add events to rooms (Instance method - call on the central map object).
     * Ensures that rooms adjacent to the starting room are not monster rooms.
     * Monster room frequency has been reduced.
     */
    public void addEventsToRooms() {
        int mapSize = this.map.length;
        int startRow = mapSize / 2;
        int startCol = mapSize / 2;

        ArrayList<int[]> availableRooms = new ArrayList<>();
        ArrayList<int[]> adjacentRooms = new ArrayList<>(); // Store rooms adjacent to start

        // 1. Collect all basic rooms (type 1)
        for (int r = 0; r < mapSize; r++) {
            for (int c = 0; c < mapSize; c++) {
                if (this.map[r][c] == 1) { // Add only type 1 rooms
                    availableRooms.add(new int[]{r, c});
                }
            }
        }

        // 2. Identify and separate rooms adjacent to the start room
        System.out.println("Identifying rooms adjacent to start [" + startRow + "," + startCol + "]");
        Iterator<int[]> iterator = availableRooms.iterator();
        while (iterator.hasNext()) {
            int[] room = iterator.next();
            int r = room[0];
            int c = room[1];
            boolean isAdjacent = false;
            // Check 8 surrounding cells
             for (int dr = -1; dr <= 1; dr++) {
                 for (int dc = -1; dc <= 1; dc++) {
                     if (dr == 0 && dc == 0) continue; // Skip the center itself
                     if (r == startRow + dr && c == startCol + dc) {
                         isAdjacent = true;
                         break;
                     }
                 }
                 if (isAdjacent) break;
             }

            if (isAdjacent) {
                adjacentRooms.add(room); // Add to adjacent list
                iterator.remove();       // Remove from available list (for event placement)
               // System.out.println("  - Found adjacent room at [" + r + "," + c + "], removed from available pool."); // Less verbose log
            }
        }
        System.out.println("Available rooms for general events: " + availableRooms.size());
        System.out.println("Rooms adjacent to start (will be plain): " + adjacentRooms.size());


        // 3. Shuffle the remaining available rooms for event placement
        Collections.shuffle(availableRooms, ran);

        // 4. Place special events (boss, shop, etc.) in the NON-ADJACENT available rooms
        int numRooms = availableRooms.size(); // Use size of the non-adjacent pool
        // Adjust counts based on the larger map size, maybe slightly higher counts but lower density
        int shops = Math.min(5, numRooms / 20); // Slightly more shops, less dense
        int shrines = Math.min(6, numRooms / 18); // Slightly more shrines, less dense
        int treasures = Math.min(8, numRooms / 15); // Slightly more treasure, less dense
        int smiths = Math.min(3, numRooms / 25); // Slightly more smiths, less dense
        // Reduced difficult enemy frequency
        int difficult = Math.min(DIFFICULT_ENEMY_CAP, numRooms / DIFFICULT_ENEMY_DIVISOR); // Difficult Enemies (type 4)
        int bosses = Math.min(2, numRooms / 50); // Maybe allow 1-2 bosses on large map

        // Place events using the non-adjacent pool
        placeRoomType(availableRooms, 10, bosses);   // Place boss(es) - Use type 10
        placeRoomType(availableRooms, 5, shops);     // Place shops
        placeRoomType(availableRooms, 9, shrines);   // Place shrines
        placeRoomType(availableRooms, 7, treasures); // Place treasures
        placeRoomType(availableRooms, 6, smiths);    // Place smiths
        placeRoomType(availableRooms, 4, difficult); // Place difficult enemies (Type 4) - will not be adjacent

        // 5. Fill remaining NON-ADJACENT rooms with standard enemies (type 3) or plain rooms (type 8)
        System.out.println("Placing standard enemies (type 3) or plain rooms (type 8) in remaining " + availableRooms.size() + " available rooms.");
        int standardEnemyCount = 0;
        int plainRoomCount = 0;
        for (int[] room : availableRooms) {
            // Check if the room wasn't already assigned a special type by placeRoomType
            if (this.map[room[0]][room[1]] == 1) {
                 int event = ran.nextInt(100);
                 // Reduced standard enemy chance
                 if (event < STANDARD_ENEMY_PERCENTAGE) { // Now 35% chance
                      this.map[room[0]][room[1]] = 3; // Standard enemy
                      standardEnemyCount++;
                 } else {
                      this.map[room[0]][room[1]] = 8; // Plain room (65% chance)
                      plainRoomCount++;
                 }
            }
        }
        System.out.println("Placed " + standardEnemyCount + " standard enemy rooms and " + plainRoomCount + " plain rooms.");


        // 6. Explicitly set the ADJACENT rooms to a safe type (e.g., plain room 8)
        System.out.println("Setting adjacent rooms to type 8 (Plain Room).");
        for (int[] room : adjacentRooms) {
            this.map[room[0]][room[1]] = 8; // Ensure adjacent rooms are plain
           // System.out.println("  - Set room [" + room[0] + "," + room[1] + "] to type 8."); // Less verbose log
        }

        // 7. Ensure start room type is correct (just in case)
        this.map[startRow][startCol] = 2;
        System.out.println("Finished adding events to rooms.");
    }


    // Helper for addEventsToRooms - Places specific room types in the provided list
    private void placeRoomType(ArrayList<int[]> roomPool, int roomType, int count) {
        int placed = 0;
        // Iterate backwards to allow safe removal if needed (though we just modify map type here)
        // Need a temporary list to avoid ConcurrentModificationException if removing while iterating forward
        ArrayList<int[]> roomsToRemove = new ArrayList<>();
        for (int i = roomPool.size() - 1; i >= 0 && placed < count; i--) {
            int[] room = roomPool.get(i);
            // Check if it's still a basic room (type 1) before assigning
            if (this.map[room[0]][room[1]] == 1) {
                this.map[room[0]][room[1]] = roomType;
                // Mark for removal instead of removing directly to avoid shifting indices during iteration
                roomsToRemove.add(room);
                placed++;
            }
        }
        // Remove the placed rooms from the main pool *after* iteration
        roomPool.removeAll(roomsToRemove);
        System.out.println("Attempted to place " + count + " rooms of type " + roomType + ", successfully placed " + placed);
    }


    // --- Utility & Action Methods (Instance Methods - call on Player objects) ---

    // Display health (used by UI for text representation if needed, but bars are primary)
    public String showHealthText() {
        return this.hp + "/" + this.maxHp;
    }

    // Movement logic for this player instance
    // Takes the central map object as input to check validity
    public boolean movePlayer(int direction, int[][] worldMap) {
        if (this.currentPos == null || this.currentPos[0] < 0) return false; // Not initialized
        if (this.energy <= 0) { // Check for energy
            System.out.println("Not enough energy to move!");
            UIManagement.addDialogue("Not enough energy to move!"); //
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

        // Check validity against the worldMap
        if (nextRow >= 0 && nextRow < worldMap.length && nextCol >= 0 && nextCol < worldMap[0].length && worldMap[nextRow][nextCol] != 0)
        {
            // Record if the destination was visited BEFORE this move
            boolean wasVisited = this.visitedMap[nextRow][nextCol];

            this.currentPos[0] = nextRow;
            this.currentPos[1] = nextCol;
            this.visitedMap[nextRow][nextCol] = true; // Mark visited AFTER checking wasVisited
            this.exploreAround(nextRow, nextCol, worldMap); // Explore around new position (Monster sounds handled here)

            this.setEnergy(this.energy - 1); // Decrease energy

            int roomType = worldMap[nextRow][nextCol];
            String roomDesc = getRoomDescription(roomType); //
            System.out.println("Player moved to [" + nextRow + ", " + nextCol + "]. Room Type: " + roomDesc + ". Energy: " + this.energy);
            UIManagement.addDialogue("Entered " + roomDesc + "."); // Add dialogue message

            // Update UI after successful move
            UIManagement.updateGameStatus(this); //

            // Trigger room-specific events here later...

            return true;
        } else {
            System.out.println("Cannot move into [" + nextRow + ", " + nextCol + "]");
             UIManagement.addDialogue("Cannot move that way."); //
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
         // No energy cost for turning
        UIManagement.updateGameStatus(this); // Update UI (e.g., map panel direction indicator)
    }

    /**
     * Exploration logic for this player instance.
     * Marks 3x3 area around the player as explored if it's a valid room.
     * Plays a random monster sound if a newly explored room is a monster room (type 3 or 4).
     *
     * @param row       The player's current row.
     * @param col       The player's current column.
     * @param worldMap  The central game map structure.
     */
    public void exploreAround(int row, int col, int[][] worldMap) {
        // Mark 3x3 area on this player's exploredMap if it's a valid room in worldMap
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = row + dr;
                int nc = col + dc;

                // Check bounds for the exploredMap array first
                if (nr >= 0 && nr < this.exploredMap.length && nc >= 0 && nc < this.exploredMap[0].length) {
                    boolean wasExplored = this.exploredMap[nr][nc]; // Check if already explored BEFORE potentially setting it true

                    // Check bounds for worldMap AND if it's an actual room (not 0)
                    if (nr >= 0 && nr < worldMap.length && nc >= 0 && nc < worldMap[0].length && worldMap[nr][nc] != 0) {

                        // Play sound if it's a NEWLY explored MONSTER room
                        if (!wasExplored) {
                            int roomType = worldMap[nr][nc];
                            if (roomType == 3 || roomType == 4) { // Check if it's a monster room (Standard or Difficult)
                                int soundNum = ran.nextInt(3) + 1; // Generate random number 1, 2, or 3
                                String soundPath = String.format("audio/sound-monster-%02d.wav", soundNum); // Format filename
                                System.out.println("Newly explored monster room at [" + nr + "," + nc + "]. Playing: " + soundPath); // Debug
                                playAudio(soundPath); // Play the selected monster sound
                            }
                        }

                        // Now mark as explored (only if it's a valid room in worldMap)
                        this.exploredMap[nr][nc] = true;
                    }
                }
            }
        }
    }


    // --- Static Map Toggle ---
    public static void toggleMapVisibility() { //
        showFullMap = !showFullMap;
        System.out.println("Map visibility toggled. Show full map: " + showFullMap);
        // Need to trigger UI update if a player object is available
        if (!players.isEmpty()) {
           // Ensure UI updates happen on the Event Dispatch Thread
           SwingUtilities.invokeLater(() -> { //
               // Pass the current player (assuming player 0 for view updates)
               if (players.size() > 0 && players.get(0) != null) { // Add null check
                   UIManagement.updateGameStatus(players.get(0));
               }
           });
        }
    }

   // --- Helper to get room description ---
   public static String getRoomDescription(int roomType) { //
       switch (roomType) {
           case 0: return "Empty Space"; // Should not happen in normal movement
           case 1: return "Corridor"; // Should not happen if events are placed correctly
           case 2: return "Starting Room"; //
           case 3: return "Enemy Room"; //
           case 4: return "Difficult Enemy Room"; //
           case 5: return "Shop"; //
           case 6: return "Smithy"; //
           case 7: return "Treasure Room"; //
           case 8: return "Plain Room"; //
           case 9: return "Shrine"; //
           case 10: return "Boss Room"; // Added Boss Room
           default: return "Unknown Room (" + roomType + ")";
       }
   }


    // --- Print map (for debugging, operates on instance's map) ---
     public void printMap(String title) { //
         System.out.println("\n--- " + title + " ---");
         int size = this.map.length;
         System.out.print("   "); // Header space
          for (int j = 0; j < size; j++) {
              System.out.printf("%-3d ", j); // Column headers
          }
          System.out.println();
         for (int i = 0; i < size; i++) {
              System.out.printf("%-3d ", i); // Row header
             for (int j = 0; j < size; j++) {
                 System.out.printf("%-3d ", this.map[i][j]); // Adjusted spacing for potential 2-digit types
             }
             System.out.println();
         }
         System.out.println("-------------------");
     }
      // --- Print boolean map (for debugging explored/visited) ---
      public void printBooleanMap(String title, boolean[][] boolMap) { //
          System.out.println("\n--- " + title + " ---");
          int size = boolMap.length;
          System.out.print("   "); // Header space
           for (int j = 0; j < size; j++) {
               System.out.printf("%-2d ", j); // Column headers
           }
           System.out.println();
          for (int i = 0; i < size; i++) {
               System.out.printf("%-3d ", i); // Row header
              for (int j = 0; j < size; j++) {
                  System.out.printf("%-2s ", boolMap[i][j] ? "T" : "F");
              }
              System.out.println();
          }
          System.out.println("-------------------");
      }

} // End of RPG class