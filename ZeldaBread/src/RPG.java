/**
 * @file    RPG.java
 * @brief   Represents the central game controller.
 * Holds the game board, manages players, orchestrates map generation using config values.
 *
 * @author  Jack Schulte  
 * @version 1.5.0 (Config Integration)
 * @date    2025-04-15
 *
 * @copyright Copyright (c) 2025 Jack Schulte . All rights reserved.
 * Strictly confidential and proprietary. Distribution, reproduction,
 * or modification is strictly prohibited without prior written permission.
 *
 * @version History:
 * - 1.5.0 (2025-04-15): Integrated ConfigLoader to use JSON values for room event chances/counts. Removed hardcoded probabilities/divisors.
 * - 1.4.0 (2025-04-15): Refactored RPG to be central controller. Player data moved to Player class. players list now holds Player objects. Increased MAP_SIZE to 35.
 * - (Previous history omitted)
 */
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Iterator;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.io.IOException;


public class RPG {

    // --- Constants ---
    private static final int MAP_SIZE = 35; // Map dimension used for Board
    public static final int DEFAULT_INVENTORY_CAPACITY = 10;
    // Room generation target might also become configurable later
    private static final int ROOM_GENERATION_TARGET = 400;

    // --- Game Board ---
    public Board board;

    // --- Static Variables & Data ---
    private static int numPlayers = 0;
    private static int playerIndex = 0;
    private static ArrayList<Player> players = new ArrayList<>();
    private static boolean showFullMap = false;
    public static Random ran = new Random();
    private static RPG centralGameInstance;

    // Character Image Set Management (Unchanged)
    private static final List<String> IMAGE_SETS = Arrays.asList("bread", "dark", "hobbits", "speedracer");
    public static String currentImageSet;
    static { currentImageSet = "speedracer"; System.out.println("!!! Using image set: " + currentImageSet + " !!!"); }

    /** Constructor for the CENTRAL game instance. */
     public RPG() {
         if (centralGameInstance != null) { throw new IllegalStateException("Central RPG instance exists."); }
         System.out.println("Creating central game instance and board...");
         // Trigger config loading explicitly or ensure static block in ConfigLoader runs
         ConfigLoader.getSettingsForRoom("ENEMY_ROOM"); // Ensures config is loaded
         this.board = new Board(MAP_SIZE, MAP_SIZE);
         centralGameInstance = this;
     }

   // --- Application Entry Point --- (Unchanged)
   public static void main(String[] args) { /* ... unchanged ... */
       SwingUtilities.invokeLater(() -> { new RPG(); if (centralGameInstance != null) { centralGameInstance.buildMap(); centralGameInstance.addEventsToRooms(); } else { System.err.println("FATAL: Failed to create central instance."); return; } UIManagement.setupUI(); setupWindowIcon(); centerWindow(); UIManagement.welcomeScreen(); UIManagement.frame.setVisible(true); System.out.println("Frame visible."); playAudio("audio/game-intro-01.wav"); });
    }
    private static void setupWindowIcon() { /* ... unchanged ... */ String iFN = "bread-man-32.png"; URL iURL = RPG.class.getClassLoader().getResource("icons/" + iFN); if (iURL != null) { try { BufferedImage iI = ImageIO.read(iURL); if (iI != null) UIManagement.frame.setIconImage(iI); else System.err.println("Icon read null."); } catch (Exception e) { System.err.println("Icon load error: " + e.getMessage()); } } else { System.err.println("Icon not found: icons/" + iFN); } }
    private static void centerWindow() { /* ... unchanged ... */ UIManagement.frame.setLocationRelativeTo(null); }
    public static void playAudio(String resourcePath) { /* ... unchanged ... */ try { InputStream aS = RPG.class.getClassLoader().getResourceAsStream(resourcePath); if (aS == null) { System.err.println("Audio not found: " + resourcePath); return; } InputStream bI = new BufferedInputStream(aS); AudioInputStream aSt = AudioSystem.getAudioInputStream(bI); Clip c = AudioSystem.getClip(); c.addLineListener(e -> { if (e.getType() == LineEvent.Type.STOP) { c.close(); try {aSt.close();} catch (IOException i) {}} }); c.open(aSt); c.start(); } catch (Exception e) { System.err.println("Audio error '" + resourcePath + "': " + e.getMessage()); } }

    // --- Getters --- (Unchanged)
    public static RPG getCentralGameInstance() { return centralGameInstance; }
    public Board getBoard() { return this.board; }
    public static int getNumPlayers() { return numPlayers; }
    public static int getPlayerIndex() { return playerIndex; }
    public static ArrayList<Player> getPlayers() { return players; }
    public static boolean isShowFullMap() { return showFullMap; }

    // --- Setters --- (Unchanged)
    public static void setNumPlayers(int x) { numPlayers = x; }
    public static void resetPlayerIndex() { playerIndex = 0; }
    public static void modPlayerIndex() { playerIndex++; }

    // --- Player Management --- (Unchanged)
    public static void createPlayers() { /* ... unchanged ... */ players.clear(); for (int i = 0; i < numPlayers; i++) { players.add(new Player()); } resetPlayerIndex(); System.out.println("Created " + numPlayers + " Player(s)."); }
    public static void assignClass(int pIndex, String className) { /* ... unchanged ... */ if (pIndex < 0 || pIndex >= players.size()) { System.err.println("Invalid player index " + pIndex); return; } Player tP = players.get(pIndex); CharacterClassInfo.BaseStats sS = CharacterClassInfo.getStats(className); if (sS == null) { System.err.println("Stats not found for " + className); tP.setClassName("Unknown"); tP.setMaxHp(50); tP.setMaxEnergy(50); tP.setHp(50); tP.setEnergy(50); tP.addScore(-tP.getScore()); tP.getInventory().clear(); tP.setEquippedWeapon(null); return; } tP.setClassName(className); tP.setVig(sS.VIG); tP.setDef(sS.DEF); tP.setStr(sS.STR); tP.setDex(sS.DEX); tP.setAgt(sS.AGT); tP.setLuck(sS.LUCK); tP.setMaxHp(sS.MAX_HP); tP.setMaxEnergy(sS.MAX_ENERGY); tP.setHp(tP.getMaxHp()); tP.setEnergy(tP.getMaxEnergy()); tP.addScore(-tP.getScore()); tP.getInventory().clear(); tP.setEquippedWeapon(null); System.out.println("Player " + (pIndex + 1) + " assigned " + className); }

    // --- Map Building Logic --- (BuildMap logic unchanged)
    public void buildMap() { /* ... unchanged from v1.4.0 ... */
         if (this.board == null) { System.err.println("Error: buildMap needs Board."); return; } int mapSize = this.board.getHeight(); for (int i = 0; i < mapSize; i++) { for (int j = 0; j < mapSize; j++) { Room r = this.board.getRoom(i,j); if(r != null) { r.setRoomType(Room.RoomType.EMPTY_SPACE); r.setVisited(false); r.setExplored(false); r.clearItems(); } } } ArrayList<int[]> rL = new ArrayList<>(); int rP = 0; int sR = mapSize / 2; int sC = mapSize / 2; if (this.board.setRoomType(sR, sC, Room.RoomType.START_ROOM)) { rL.add(new int[]{sR, sC}); rP++; } else { System.err.println("Error: Start room place failed!"); return; }
         while (rP < ROOM_GENERATION_TARGET) { if (rL.isEmpty()) { System.out.println("Warn: Room list empty at " + rP); break; } int cRI = ran.nextInt(rL.size()); int[] cC = rL.get(cRI); int r = cC[0]; int c = cC[1]; ArrayList<int[]> pM = findPossibleExpansionMoves(r, c); if (!pM.isEmpty()) { int[] nC = pM.get(ran.nextInt(pM.size())); int nR = nC[0]; int nCl = nC[1]; if(this.board.setRoomType(nR, nCl, Room.RoomType.CORRIDOR)) { rP++; rL.add(nC); } else { System.err.println("Error: set type failed ["+nR+","+nCl+"]"); } } else { rL.remove(cRI); } if (rP >= mapSize * mapSize * 0.8) { System.out.println("Density stop."); break; } } System.out.println("Map built: " + rP + " rooms on " + mapSize + "x" + mapSize);
    }
    private ArrayList<int[]> findPossibleExpansionMoves(int row, int col) { /* ... unchanged ... */ ArrayList<int[]> m = new ArrayList<>(); int[] dr = {-1, 1, 0, 0}; int[] dc = {0, 0, 1, -1}; for(int i=0; i<4; i++) { int nR = row + dr[i]; int nC = col + dc[i]; if (isValidRoomForPlacing(nR, nC)) { m.add(new int[]{nR, nC}); } } return m; }
    private boolean isValidRoomForPlacing(int row, int col) { /* ... unchanged ... */ Room tR = this.board.getRoom(row, col); if (tR == null || tR.getRoomType() != Room.RoomType.EMPTY_SPACE) return false; if (row <= 0 || row >= this.board.getHeight() - 1 || col <= 0 || col >= this.board.getWidth() - 1) return false; return !formsSquareWithExisting(row, col); }
    private boolean formsSquareWithExisting(int row, int col) { /* ... unchanged ... */ int[][] n = { {-1, 0}, {0, -1}, {-1, -1}, {-1, 0}, {0, 1}, {-1, 1}, {1, 0}, {0, -1}, {1, -1}, {1, 0}, {0, 1}, {1, 1} }; for (int i = 0; i < n.length; i += 3) { if (isExistingRoom(row + n[i][0], col + n[i][1]) && isExistingRoom(row + n[i+1][0], col + n[i+1][1]) && isExistingRoom(row + n[i+2][0], col + n[i+2][1])) return true; } return false; }
    private boolean isExistingRoom(int r, int c) { /* ... unchanged ... */ Room rm = this.board.getRoom(r, c); return (rm != null && rm.getRoomType() != Room.RoomType.EMPTY_SPACE); }


    /**
     * Add events (special room types) using probabilities/counts from ConfigLoader.
     */
    public void addEventsToRooms() {
         if (this.board == null) { System.err.println("Error: Board null in addEvents."); return; }
        int mapSize = this.board.getHeight();
        int startRow = mapSize / 2; int startCol = mapSize / 2;
        ArrayList<int[]> availableCoords = new ArrayList<>(); ArrayList<int[]> adjacentCoords = new ArrayList<>();

        // Collect CORRIDOR room coordinates (unchanged)
        for (int r = 0; r < mapSize; r++) { for (int c = 0; c < mapSize; c++) { Room room = this.board.getRoom(r, c); if (room != null && room.getRoomType() == Room.RoomType.CORRIDOR) { availableCoords.add(new int[]{r, c}); } } }
        Iterator<int[]> iterator = availableCoords.iterator(); while (iterator.hasNext()) { int[] coord = iterator.next(); if ((Math.abs(coord[0] - startRow) <= 1 && Math.abs(coord[1] - startCol) <= 1) && !(coord[0] == startRow && coord[1] == startCol)) { adjacentCoords.add(coord); iterator.remove(); } }
        System.out.println("Available CORRIDORs for events: " + availableCoords.size());

        Collections.shuffle(availableCoords, ran);
        int numAvailable = availableCoords.size();
        if (numAvailable == 0) { System.out.println("No available corridors to place events in."); return; }

        // ** FIX: Get counts/chances from ConfigLoader **
        int enemyChance = ConfigLoader.getIntSetting("ENEMY_ROOM", "event_chance_percent", 30); // Default 30%
        int difficultCount = calculateCount("DIFFICULT_ENEMY_ROOM", numAvailable);
        int shopCount = calculateCount("SHOP", numAvailable);
        int shrineCount = calculateCount("SHRINE", numAvailable);
        int treasureCount = calculateCount("TREASURE_ROOM", numAvailable);
        int smithyCount = calculateCount("SMITHY", numAvailable);
        int bossCount = calculateCount("BOSS_ROOM", numAvailable);

        // Place rarer events first using calculated counts
        placeRoomTypeByCoord(availableCoords, Room.RoomType.BOSS_ROOM, bossCount);
        placeRoomTypeByCoord(availableCoords, Room.RoomType.SHOP, shopCount);
        placeRoomTypeByCoord(availableCoords, Room.RoomType.SHRINE, shrineCount);
        placeRoomTypeByCoord(availableCoords, Room.RoomType.TREASURE_ROOM, treasureCount);
        placeRoomTypeByCoord(availableCoords, Room.RoomType.SMITHY, smithyCount);
        placeRoomTypeByCoord(availableCoords, Room.RoomType.DIFFICULT_ENEMY_ROOM, difficultCount);

        // Fill remaining with standard enemies (based on chance) or plain rooms
        int standardEnemyCount = 0; int plainRoomCount = 0;
        // Create copy to iterate over as placeRoomTypeByCoord modifies original list
        ArrayList<int[]> remainingCoords = new ArrayList<>(availableCoords);
        for (int[] coord : remainingCoords) {
            Room room = this.board.getRoom(coord[0], coord[1]);
            // Double check it wasn't already assigned (shouldn't happen if placeRoomTypeByCoord works)
            if (room != null && room.getRoomType() == Room.RoomType.CORRIDOR) {
                 int eventRoll = ran.nextInt(100);
                 if (eventRoll < enemyChance) { // Use config chance
                      room.setRoomType(Room.RoomType.ENEMY_ROOM); standardEnemyCount++;
                 } else {
                      room.setRoomType(Room.RoomType.PLAIN_ROOM); plainRoomCount++;
                 }
            }
        }
        System.out.println("Placed StandardEnemy=" + standardEnemyCount + ", Plain=" + plainRoomCount + " (using " + enemyChance + "% chance)");

        // Set adjacent rooms to PLAIN (unchanged)
        for (int[] coord : adjacentCoords) { this.board.setRoomType(coord[0], coord[1], Room.RoomType.PLAIN_ROOM); }
        this.board.setRoomType(startRow, startCol, Room.RoomType.START_ROOM);
        System.out.println("Finished adding events.");
    }

    /** Calculates room count based on config divisor/max */
    private int calculateCount(String roomTypeName, int numAvailable) {
        int divisor = ConfigLoader.getIntSetting(roomTypeName, "event_divisor", 1000); // Default very high divisor
        int maxCount = ConfigLoader.getIntSetting(roomTypeName, "event_max_count", 1); // Default max 1
        if (divisor <= 0) divisor = 1000; // Avoid division by zero
        return Math.min(maxCount, numAvailable / divisor);
    }

    /** Helper for addEventsToRooms - Sets room types at coordinates. (Unchanged) */
    private void placeRoomTypeByCoord(ArrayList<int[]> coordPool, Room.RoomType roomType, int count) { /* ... unchanged ... */ int p = 0; ArrayList<int[]> r = new ArrayList<>(); for (int i = coordPool.size() - 1; i >= 0 && p < count; i--) { int[] c = coordPool.get(i); Room rm = this.board.getRoom(c[0], c[1]); if (rm != null && rm.getRoomType() == Room.RoomType.CORRIDOR) { rm.setRoomType(roomType); r.add(c); p++; } } coordPool.removeAll(r); System.out.println("Placed " + p + "/" + count + " of " + roomType.name()); }

    // --- Static Map Toggle --- (Unchanged)
    public static void toggleMapVisibility() { /* ... unchanged ... */ showFullMap = !showFullMap; System.out.println("Map visibility: " + showFullMap); if (!players.isEmpty()) { SwingUtilities.invokeLater(() -> { if (players.size() > 0 && players.get(0) != null) UIManagement.updateGameStatus(players.get(0)); }); } }

   /** Static helper to get room description. (Unchanged) */
   public static String getRoomDescription(Room.RoomType roomType) { /* ... unchanged ... */ return (roomType != null) ? roomType.getDescription() : "Unknown"; }

} // End of RPG class