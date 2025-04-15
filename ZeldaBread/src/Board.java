/**
 * @file    Board.java
 * @brief   Represents the game board containing a grid of Room objects.
 * Handles board dimensions and accessing individual rooms.
 *
 * @author  Jack Schulte
 * @version 1.0.0
 * @date    2025-04-15
 *
 * @copyright Copyright (c) 2025 Jack Schulte. All rights reserved.
 * Strictly confidential and proprietary. Distribution, reproduction,
 * or modification is strictly prohibited without prior written permission.
 */

 public class Board {
    private final Room[][] grid;
    private final int width;
    private final int height;

    /**
     * Creates a new Board with the specified dimensions.
     * Initializes all rooms to RoomType.EMPTY_SPACE by default.
     * @param width The width of the board.
     * @param height The height of the board.
     */
    public Board(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Board dimensions must be positive.");
        }
        this.width = width;
        this.height = height;
        this.grid = new Room[height][width]; // Note: [row][col] -> [height][width]

        // Initialize all rooms
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                grid[r][c] = new Room(); // Default is EMPTY_SPACE
            }
        }
    }

    /**
     * Gets the Room object at the specified coordinates.
     * Performs bounds checking.
     * @param row The row index.
     * @param col The column index.
     * @return The Room object at [row][col], or null if the coordinates are out of bounds.
     */
    public Room getRoom(int row, int col) {
        if (row >= 0 && row < height && col >= 0 && col < width) {
            return grid[row][col];
        } else {
           // System.err.println("Attempted to access room outside bounds: [" + row + "," + col + "]"); // Can be noisy
            return null; // Indicate out of bounds
        }
    }

     /**
      * A convenience method to directly set the room type at specific coordinates.
      * Performs bounds checking.
      * @param row The row index.
      * @param col The column index.
      * @param type The RoomType to set.
      * @return true if the type was set successfully, false if coordinates were out of bounds.
      */
     public boolean setRoomType(int row, int col, Room.RoomType type) {
         Room room = getRoom(row, col);
         if (room != null) {
             room.setRoomType(type);
             return true;
         }
         return false;
     }

    /**
      * A convenience method to directly set the room type at specific coordinates using an integer value.
      * Performs bounds checking.
      * @param row The row index.
      * @param col The column index.
      * @param typeValue The integer value corresponding to a RoomType.
      * @return true if the type was set successfully, false if coordinates were out of bounds or typeValue invalid.
      */
     public boolean setRoomType(int row, int col, int typeValue) {
         Room room = getRoom(row, col);
         if (room != null) {
             Room.RoomType type = Room.RoomType.fromValue(typeValue);
             if (type != Room.RoomType.UNKNOWN) {
                 room.setRoomType(type);
                 return true;
             }
         }
         return false;
     }


    /**
     * Gets the width of the board.
     * @return The width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of the board.
     * @return The height.
     */
    public int getHeight() {
        return height;
    }

    // --- Utility for debugging ---
    public void printBoardTypes() {
        System.out.println("\n--- Board Room Types ---");
        System.out.print("   "); // Header space
        for (int j = 0; j < width; j++) {
            System.out.printf("%-3d ", j); // Column headers
        }
        System.out.println();
        for (int i = 0; i < height; i++) {
            System.out.printf("%-3d ", i); // Row header
            for (int j = 0; j < width; j++) {
                Room room = getRoom(i, j);
                System.out.printf("%-3d ", (room != null) ? room.getRoomType().getValue() : -9); // Show type value or error code
            }
            System.out.println();
        }
        System.out.println("----------------------");
    }

    public void printBoardVisited() {
         System.out.println("\n--- Board Visited Status ---");
         System.out.print("   "); // Header space
         for (int j = 0; j < width; j++) { System.out.printf("%-2d ", j); }
         System.out.println();
         for (int i = 0; i < height; i++) {
             System.out.printf("%-3d ", i); // Row header
             for (int j = 0; j < width; j++) {
                  Room room = getRoom(i, j);
                  System.out.printf("%-2s ", (room != null && room.isVisited()) ? "T" : "F");
             }
             System.out.println();
         }
         System.out.println("--------------------------");
     }

      public void printBoardExplored() {
         System.out.println("\n--- Board Explored Status ---");
         System.out.print("   "); // Header space
         for (int j = 0; j < width; j++) { System.out.printf("%-2d ", j); }
         System.out.println();
         for (int i = 0; i < height; i++) {
             System.out.printf("%-3d ", i); // Row header
             for (int j = 0; j < width; j++) {
                  Room room = getRoom(i, j);
                  System.out.printf("%-2s ", (room != null && room.isExplored()) ? "T" : "F");
             }
             System.out.println();
         }
         System.out.println("---------------------------");
     }
}