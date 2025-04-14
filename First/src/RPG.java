/**
 * @file    RPG.java
 * @brief   Handles core game state, map generation/logic, player stats, exploration tracking, and movement.
 *
 * Contains the main game logic, including map array (`map`), explored areas (`exploredMap`),
 * player attributes, class definitions, map building algorithms, player movement/turning,
 * and the debug map visibility toggle.
 *
 * @author  Jack Schulte
 * @version 1.1
 * @date    2025-04-14
 *
 * @copyright Copyright (c) 2025 Jack Schulte. All rights reserved.
 * Strictly confidential and proprietary. Distribution, reproduction,
 * or modification is strictly prohibited without prior written permission.
 *
 * @version History:
 * - 1.1 (2025-04-14): Added exploredMap tracking, exploreAround logic, and toggleMapVisibility debug feature. (J. Schulte)
 * - 1.0 (2025-04-01): Initial version creation with basic map generation and player classes. (J. Schulte)
 * // Add more versions as the file evolves
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class RPG {

    public int[][] map = new int[21][21];
    public boolean[][] exploredMap = new boolean[21][21]; // Added: Track explored areas
    private boolean showFullMap = false; // Added: Debug flag for map visibility
    private int rooms = 1;
    private ArrayList<int[]> roomList = new ArrayList<>();
    private ArrayList<int[]> middleMapRoomList = new ArrayList<>();
    private Random ran = new Random();
    private int[] currentPos; // Player's current [row, col]
    // UPDATED Direction Mapping: 0:N, 1:E, 2:S, 3:W
    private int wayFacing;
    private int difficulty;

    // Player stats
    private int playerNum;
    private String name;
    private String nameClass;
    private static int numPlayers = 0;
    private static int playerIndex = 0;
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

    private static ArrayList<RPG> players = new ArrayList<>();
    private static RPG knight = new RPG("Knight", 11, 12, 11, 10, 8, 8, InvSlot.empty, InvSlot.empty, InvSlot.empty, InvSlot.empty);
    private static RPG sentinel = new RPG("Sentinel", 10, 13, 9, 11, 7, 10, InvSlot.empty, InvSlot.empty, InvSlot.empty, InvSlot.empty);
    private static RPG assassin = new RPG("Assassin", 9, 8, 11, 12, 13, 7, InvSlot.empty, InvSlot.empty, InvSlot.empty, InvSlot.empty);
    private static RPG caveman = new RPG("Caveman", 10, 10, 10, 10, 10, 10, InvSlot.empty, InvSlot.empty, InvSlot.empty, InvSlot.empty);

    public RPG() {
        // Initialize explored map
        for (int i = 0; i < exploredMap.length; i++) {
            for (int j = 0; j < exploredMap[i].length; j++) {
                exploredMap[i][j] = false;
            }
        }
    }

    private RPG(String nameClass, int vig, int def, int str, int dex, int agt, int luck, InvSlot invSlot1, InvSlot invSlot2, InvSlot invSlot3, InvSlot invSlot4) {
        this(); // Call default constructor to initialize exploredMap
        this.name = "";
        this.nameClass = nameClass;
        this.vig = vig;
        this.def = def;
        this.str = str;
        this.dex = dex;
        this.agt = agt;
        this.luck = luck;
        this.invSlot1 = invSlot1;
        this.invSlot2 = invSlot2;
        this.invSlot3 = invSlot3;
        this.invSlot4 = invSlot4;
    }

    public RPG(String name, String nameClass, int vig, int def, int str, int dex, int agt, int luck, InvSlot invSlot1, InvSlot invSlot2, InvSlot invSlot3, InvSlot invSlot4) {
        this(); // Call default constructor to initialize exploredMap
        this.name = name;
        this.nameClass = nameClass;
        this.vig = vig;
        this.def = def;
        this.str = str;
        this.dex = dex;
        this.agt = agt;
        this.luck = luck;
        this.invSlot1 = invSlot1;
        this.invSlot2 = invSlot2;
        this.invSlot3 = invSlot3;
        this.invSlot4 = invSlot4;
    }

    // --- Getters ---
    public int getStr() { return str; }
    public int getDex() { return dex; }
    public int[] getCurrentPos() { return currentPos; }
    public int getWayFacing() { return wayFacing; }
    public int getDifficulty() { return difficulty; }
    public static int getNumPlayers() { return numPlayers; }
    public static int getPlayerIndex() { return playerIndex; }
    public String getClassName() { return nameClass; }
    public static ArrayList<RPG> getPlayers() { return players; }
    public boolean[][] getExploredMap() { return exploredMap; } // Added getter
    public boolean isShowFullMap() { return showFullMap; } // Added getter

    // --- Setters ---
    public void setCurrentPos(int[] x) { currentPos = x; }
    public void setWayFacing(int x) { wayFacing = x; }
    public void setDifficulty(int x) { difficulty = x; }
    public static void setNumPlayers(int x) { numPlayers = x; }
    public static void resetPlayerIndex() { playerIndex = 0; }

    // --- Player Management ---
    public static void modPlayerIndex() { playerIndex++; }

    public static void createPlayers() {
        players.clear();
        for (int i = 0; i < numPlayers; i++) {
            players.add(new RPG());
        }
        resetPlayerIndex();
    }

    public static void assignClass(int pIndex, String className) {
        if (pIndex < 0 || pIndex >= players.size()) {
            System.err.println("Error: Invalid player index for class assignment.");
            return;
        }

        RPG targetPlayer = players.get(pIndex);
        RPG sourceClass = null;

        switch (className) {
            case "Knight": sourceClass = knight; break;
            case "Sentinel": sourceClass = sentinel; break;
            case "Assassin": sourceClass = assassin; break;
            case "Caveman": sourceClass = caveman; break;
            default:
                System.err.println("Error: Unknown class name: " + className);
                return;
        }

        targetPlayer.nameClass = sourceClass.nameClass;
        targetPlayer.vig = sourceClass.vig;
        targetPlayer.def = sourceClass.def;
        targetPlayer.str = sourceClass.str;
        targetPlayer.dex = sourceClass.dex;
        targetPlayer.agt = sourceClass.agt;
        targetPlayer.luck = sourceClass.luck;
        targetPlayer.hp = targetPlayer.vig * 10;
        targetPlayer.invSlot1 = sourceClass.invSlot1;
        targetPlayer.invSlot2 = sourceClass.invSlot2;
        targetPlayer.invSlot3 = sourceClass.invSlot3;
        targetPlayer.invSlot4 = sourceClass.invSlot4;

        System.out.println("Player " + (pIndex + 1) + " assigned class: " + className);
    }


    // --- Map Building Logic ---
    public void buildMap() {
        map = new int[21][21];
        exploredMap = new boolean[21][21]; // Re-initialize explored map on build
        roomList.clear();
        middleMapRoomList.clear();
        rooms = 1;

        int startRow = 10;
        int startCol = 10;
        map[startRow][startCol] = 2;
        roomList.add(new int[]{startRow, startCol});

        // Set initial player position and facing AFTER map is built
        this.currentPos = new int[]{startRow, startCol};
        this.wayFacing = 0; // Start facing 0 (North)

        // Initial exploration around start position
        exploreAround(startRow, startCol); // <<< Added call

        while (rooms < 50) {
            Collections.shuffle(roomList, ran);
            if (roomList.isEmpty()) {
                System.err.println("Error: Room list became empty during map generation.");
                break;
            }
            int[] room = roomList.get(0);
            int row = room[0];
            int col = room[1];
            ArrayList<int[]> possibleMoves = new ArrayList<>();

            if (isValidRoom(row - 1, col, true)) possibleMoves.add(new int[]{row - 1, col});
            if (isValidRoom(row + 1, col, true)) possibleMoves.add(new int[]{row + 1, col});
            if (isValidRoom(row, col - 1, true)) possibleMoves.add(new int[]{row, col - 1});
            if (isValidRoom(row, col + 1, true)) possibleMoves.add(new int[]{row, col + 1});

            int lineMove = roomLineHelper(row, col);
            if (lineMove != -1) {
                switch (lineMove) {
                    case 0: if (isValidRoom(row + 1, col, true)) possibleMoves.add(new int[]{row + 1, col}); break;
                    case 1: if (isValidRoom(row - 1, col, true)) possibleMoves.add(new int[]{row - 1, col}); break;
                    case 2: if (isValidRoom(row, col + 1, true)) possibleMoves.add(new int[]{row, col + 1}); break;
                    case 3: if (isValidRoom(row, col - 1, true)) possibleMoves.add(new int[]{row, col - 1}); break;
                }
            }

            if (!possibleMoves.isEmpty()) {
                int[] newRoom = possibleMoves.get(ran.nextInt(possibleMoves.size()));
                int newRow = newRoom[0];
                int newCol = newRoom[1];

                map[newRow][newCol] = 1;
                rooms++;
                roomList.add(newRoom);
                int dist = Math.abs(newRow - startRow) + Math.abs(newCol - startCol);
                if (dist <= 3) {
                    middleMapRoomList.add(newRoom);
                }
            } else {
                roomList.remove(0);
            }
            if (rooms >= 100) {
                System.err.println("Warning: Room generation exceeded limit.");
                break;
            }
        }
        System.out.println("Map built with " + rooms + " rooms.");
    }

    public boolean isValidRoom(int row, int col, boolean isBuilding) {
        if (row < 0 || row >= map.length || col < 0 || col >= map[0].length)
            return false;

        if (isBuilding) {
            if (map[row][col] != 0)
                return false;
            return !formsSquare(row, col);
        } else {
            return map[row][col] != 0;
        }
    }

    private boolean formsSquare(int row, int col) {
        if (isValidRoom(row - 1, col, false) && map[row - 1][col] != 0 &&
            isValidRoom(row, col - 1, false) && map[row][col - 1] != 0 &&
            isValidRoom(row - 1, col - 1, false) && map[row - 1][col - 1] != 0) return true;

        if (isValidRoom(row - 1, col, false) && map[row - 1][col] != 0 &&
            isValidRoom(row, col + 1, false) && map[row][col + 1] != 0 &&
            isValidRoom(row - 1, col + 1, false) && map[row - 1][col + 1] != 0) return true;

        if (isValidRoom(row + 1, col, false) && map[row + 1][col] != 0 &&
            isValidRoom(row, col - 1, false) && map[row][col - 1] != 0 &&
            isValidRoom(row + 1, col - 1, false) && map[row + 1][col - 1] != 0) return true;

        if (isValidRoom(row + 1, col, false) && map[row + 1][col] != 0 &&
            isValidRoom(row, col + 1, false) && map[row][col + 1] != 0 &&
            isValidRoom(row + 1, col + 1, false) && map[row + 1][col + 1] != 0) return true;

        return false;
    }

    private int roomLineHelper(int row, int col) {
        boolean existsAbove = isValidRoom(row - 1, col, false) && map[row - 1][col] != 0;
        boolean existsBelow = isValidRoom(row + 1, col, false) && map[row + 1][col] != 0;
        boolean existsLeft = isValidRoom(row, col - 1, false) && map[row][col - 1] != 0;
        boolean existsRight = isValidRoom(row, col + 1, false) && map[row][col + 1] != 0;

        if (existsAbove && isValidRoom(row + 1, col, true)) return 0; // Go down
        if (existsBelow && isValidRoom(row - 1, col, true)) return 1; // Go up
        if (existsLeft && isValidRoom(row, col + 1, true)) return 2; // Go right
        if (existsRight && isValidRoom(row, col - 1, true)) return 3; // Go left
        return -1;
    }

    public void addEventsToRooms() {
        int startRow = (currentPos != null) ? currentPos[0] : 10;
        int startCol = (currentPos != null) ? currentPos[1] : 10;

        int shopRooms = 0;
        int shrineRooms = 0;
        int bossRooms = 0;
        ArrayList<int[]> availableRooms = new ArrayList<>();
        for (int[] roomCoords : roomList) {
            if (!(roomCoords[0] == startRow && roomCoords[1] == startCol)) {
                availableRooms.add(roomCoords);
            }
        }

        Collections.shuffle(availableRooms, ran);

        // --- Place Boss Rooms ---
        int placedBosses = 0;
        ArrayList<int[]> potentialBossRooms = new ArrayList<>();
        for (int[] room : availableRooms) {
            int dist = Math.abs(room[0] - startRow) + Math.abs(room[1] - startCol);
            if (dist > 5) {
                potentialBossRooms.add(room);
            }
        }
        Collections.shuffle(potentialBossRooms, ran);

        for (int[] room : potentialBossRooms) {
            if (placedBosses < 3) {
                map[room[0]][room[1]] = 2; // Boss room type (Same as Start? Needs unique value)
                availableRooms.remove(room);
                placedBosses++;
            } else { break; }
        }
        System.out.println("Placed " + placedBosses + " boss rooms.");

        // --- Place Shrine Rooms ---
        int placedShrines = 0;
        ArrayList<int[]> potentialMiddleShrines = new ArrayList<>(middleMapRoomList);
        potentialMiddleShrines.retainAll(availableRooms);
        Collections.shuffle(potentialMiddleShrines, ran);
        if (!potentialMiddleShrines.isEmpty()) {
            int[] room = potentialMiddleShrines.get(0);
            map[room[0]][room[1]] = 9; // Shrine room type
            availableRooms.remove(room);
            placedShrines++;
        }

        Collections.shuffle(availableRooms, ran);
        for (int[] room : new ArrayList<>(availableRooms)) {
            if (placedShrines < 4) {
                map[room[0]][room[1]] = 9;
                availableRooms.remove(room);
                placedShrines++;
            } else { break; }
        }
        System.out.println("Placed " + placedShrines + " shrine rooms.");

        // --- Place Shop Rooms ---
        int placedShops = 0;
        Collections.shuffle(availableRooms, ran);
        for (int[] room : new ArrayList<>(availableRooms)) {
            if (placedShops < 3) {
                map[room[0]][room[1]] = 5; // Market room type
                availableRooms.remove(room);
                placedShops++;
            } else { break; }
        }
        System.out.println("Placed " + placedShops + " shop rooms.");

        // --- Fill remaining rooms ---
        Collections.shuffle(availableRooms, ran);
        for (int[] room : availableRooms) {
            if (map[room[0]][room[1]] == 1) {
                int event = ran.nextInt(100) + 1;
                if (event <= 35) map[room[0]][room[1]] = 3;      // Standard enemy
                else if (event <= 55) map[room[0]][room[1]] = 4; // Difficult enemy
                else if (event <= 65) map[room[0]][room[1]] = 6; // Blacksmith
                else if (event <= 75) map[room[0]][room[1]] = 7; // Treasure
                else if (event <= 90) map[room[0]][room[1]] = 8; // Plain
                else map[room[0]][room[1]] = 3; // Default to Standard enemy
            }
        }
        System.out.println("Filled remaining rooms with events.");

        // Ensure the starting room type remains correct
        map[startRow][startCol] = 2; // Force start room type back to 2
    }


    public void printMap() {
        System.out.println("\n--- Current Map ---");
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                System.out.printf("%-2d ", map[i][j]);
            }
            System.out.println();
        }
        System.out.println("-------------------");
    }

    public String showHealth() {
        StringBuilder healthBar = new StringBuilder();
        int currentHP = this.hp;
        int maxHP = this.vig * 10;
        maxHP = Math.max(1, maxHP);
        currentHP = Math.max(0, Math.min(currentHP, maxHP));

        int barLength = 20;
        int filledLength = (int) (((double) currentHP / maxHP) * barLength);

        for (int i = 0; i < barLength; i++) {
            if (i < filledLength) {
                healthBar.append("█");
            } else {
                healthBar.append("░");
            }
        }
        return healthBar.append(" ").append(currentHP).append("/").append(maxHP).toString();
    }

    // --- Movement Logic ---
    public boolean movePlayer(int direction) { // direction is 0:N, 1:E, 2:S, 3:W
        if (currentPos == null) return false;

        int currentRow = currentPos[0];
        int currentCol = currentPos[1];
        int nextRow = currentRow;
        int nextCol = currentCol;

        // Adjust nextRow/nextCol based on the NEW direction map
        switch (direction) {
            case 0: nextRow--; break; // North (Up)
            case 1: nextCol++; break; // East (Right)
            case 2: nextRow++; break; // South (Down)
            case 3: nextCol--; break; // West (Left)
            default: return false; // Invalid direction
        }

        // Check if the next position is a valid room
        if (isValidRoom(nextRow, nextCol, false)) {
            currentPos[0] = nextRow;
            currentPos[1] = nextCol;
            // Don't automatically change facing direction on move, only on turn.
            exploreAround(nextRow, nextCol); // <<< Explore around the new position
            System.out.println("Moved to [" + nextRow + ", " + nextCol + "]"); // Debug
            // Trigger room event?
            return true;
        } else {
            System.out.println("Cannot move into [" + nextRow + ", " + nextCol + "]"); // Debug
            return false;
        }
    }

    public void turnPlayer(int turnDirection) { // 0=left (CCW), 1=right (CW)
        if (turnDirection == 0) { // Turn Left
            // Use the (current + 3) % 4 logic for counter-clockwise turn
            wayFacing = (wayFacing + 3) % 4;
        } else if (turnDirection == 1) { // Turn Right
            // Use the (current + 1) % 4 logic for clockwise turn
            wayFacing = (wayFacing + 1) % 4;
        }
        System.out.println("Now facing direction: " + wayFacing); // Debug
    }

    // --- New Exploration Logic ---
    private void exploreAround(int row, int col) {
        // Mark the current cell and its 8 neighbors as explored
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = row + dr;
                int nc = col + dc;
                // Check bounds before marking
                if (nr >= 0 && nr < exploredMap.length && nc >= 0 && nc < exploredMap[0].length) {
                    exploredMap[nr][nc] = true;
                }
            }
        }
    }

    // --- New Debug Map Toggle ---
    public void toggleMapVisibility() {
        showFullMap = !showFullMap;
        System.out.println("Map visibility toggled. Show full map: " + showFullMap); // Debug
    }
}