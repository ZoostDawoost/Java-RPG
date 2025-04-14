/**
 * @file    MapPanel.java
 * @brief   Custom JPanel responsible for drawing the game map, explored areas, and player marker/direction.
 *
 * This class takes the game state from the RPG object and visually renders the map grid.
 * It handles drawing based on whether the full map should be shown (debug mode) or only
 * the explored areas, using the `exploredMap` data from the RPG class.
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
 * - 1.1 (2025-04-14): Modified paintComponent to draw based on exploredMap and showFullMap flag. (J. Schulte)
 * - 1.0 (2025-04-01): Initial version drawing the full map and player. (J. Schulte)
 * // Add more versions as the file evolves
 */

import javax.swing.*;
import java.awt.*;

public class MapPanel extends JPanel {

    private RPG rpgGame; // Reference to the main RPG logic
    private final int TILE_SIZE = 20; // Size of each map tile in pixels
    private final int GRID_OFFSET_X = 50; // Offset to center grid horizontally
    private final int GRID_OFFSET_Y = 20; // Offset from top

    // Define colors for different room types
    private final Color COLOR_EMPTY = Color.BLACK;
    private final Color COLOR_UNEXPLORED = new Color(20, 20, 20); // Dark color for unexplored but known to exist
    private final Color COLOR_WALL = Color.DARK_GRAY; // For cells adjacent to empty (Maybe remove if using unexplored?)
    private final Color COLOR_NORMAL = Color.LIGHT_GRAY;
    private final Color COLOR_START = Color.CYAN; // Start room (original value 2)
    // NOTE: Boss also uses value 2. Need unique value in RPG.java if different color desired.
    private final Color COLOR_BOSS = Color.MAGENTA;
    private final Color COLOR_ENEMY_STD = Color.RED; // Standard enemy (value 3)
    private final Color COLOR_ENEMY_DIFF = new Color(139, 0, 0); // Difficult enemy (value 4)
    private final Color COLOR_SHOP = Color.YELLOW; // Market/Shop (value 5)
    private final Color COLOR_SMITH = Color.ORANGE; // Blacksmith (value 6)
    private final Color COLOR_TREASURE = Color.GREEN; // Treasure (value 7)
    private final Color COLOR_PLAIN = Color.WHITE; // Plain room (value 8)
    private final Color COLOR_SHRINE = Color.BLUE; // Shrine (value 9)
    private final Color COLOR_PLAYER = Color.PINK; // Player marker

    public MapPanel(RPG rpg) {
        this.rpgGame = rpg;
        // Calculate preferred size based on map dimensions and tile size
        if (rpg != null && rpg.map != null && rpg.map.length > 0 && rpg.map[0].length > 0) {
            this.setPreferredSize(new Dimension(rpg.map[0].length * TILE_SIZE + 2 * GRID_OFFSET_X, rpg.map.length * TILE_SIZE + 2 * GRID_OFFSET_Y));
        } else {
            // Default size if map is not ready
            this.setPreferredSize(new Dimension(21 * TILE_SIZE + 2 * GRID_OFFSET_X, 21 * TILE_SIZE + 2 * GRID_OFFSET_Y));
        }
        this.setBackground(COLOR_EMPTY); // Background for areas outside the map
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Clears the panel

        if (rpgGame == null || rpgGame.map == null || rpgGame.exploredMap == null) { // Added check for exploredMap
            g.setColor(Color.WHITE);
            g.drawString("Map not loaded...", 20, 30); // Placeholder text
            return; // Don't draw if map data isn't ready
        }

        int numRows = rpgGame.map.length;
        int numCols = rpgGame.map[0].length;
        boolean showFull = rpgGame.isShowFullMap(); // Get debug flag
        boolean[][] explored = rpgGame.getExploredMap(); // Get explored data

        // Draw the map grid
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                int tileX = GRID_OFFSET_X + col * TILE_SIZE;
                int tileY = GRID_OFFSET_Y + row * TILE_SIZE;
                int roomType = rpgGame.map[row][col];
                boolean isExplored = explored[row][col]; // Check if this tile is explored

                Color tileColor;
                boolean drawBorder = false;

                // Determine tile color based on exploration and debug flag
                if (showFull || isExplored) {
                    // If showing full map OR this tile is explored
                    if (roomType != 0) {
                        // If it's a room (not empty space)
                        tileColor = getRoomColor(roomType);
                        drawBorder = true; // Draw border for non-empty rooms
                    } else {
                        // If it's empty space (within the map grid, but value 0)
                        tileColor = COLOR_EMPTY; // Treat as empty background
                    }
                } else {
                    // If not showing full map AND this tile is NOT explored
                    tileColor = COLOR_EMPTY; // Draw as completely empty/black
                    // Optional: Could use COLOR_UNEXPLORED if you want a different "fog of war" look
                    // for explored adjacent areas vs totally unknown.
                }

                // Draw the tile fill
                g.setColor(tileColor);
                g.fillRect(tileX, tileY, TILE_SIZE, TILE_SIZE);

                // Draw a border if needed
                if (drawBorder) {
                    g.setColor(Color.DARK_GRAY); // Border color
                    g.drawRect(tileX, tileY, TILE_SIZE - 1, TILE_SIZE - 1);
                }
            }
        }

        // Draw the player marker (always drawn)
        int[] playerPos = rpgGame.getCurrentPos();
        if (playerPos != null && playerPos.length == 2) { // Add null and length check
            int playerRow = playerPos[0];
            int playerCol = playerPos[1];

            // Ensure player position is within map bounds before drawing
            if (playerRow >= 0 && playerRow < numRows && playerCol >= 0 && playerCol < numCols) {
                int playerTileX = GRID_OFFSET_X + playerCol * TILE_SIZE;
                int playerTileY = GRID_OFFSET_Y + playerRow * TILE_SIZE;

                g.setColor(COLOR_PLAYER);
                int playerMarkerSize = TILE_SIZE / 2;
                int playerMarkerOffset = (TILE_SIZE - playerMarkerSize) / 2;
                g.fillOval(playerTileX + playerMarkerOffset, playerTileY + playerMarkerOffset, playerMarkerSize, playerMarkerSize);

                // --- Draw direction indicator based on NEW mapping ---
                // 0:N, 1:E, 2:S, 3:W
                g.setColor(Color.BLACK);
                int centerX = playerTileX + TILE_SIZE / 2;
                int centerY = playerTileY + TILE_SIZE / 2;
                int lineLength = TILE_SIZE / 3;
                switch (rpgGame.getWayFacing()) {
                    case 0: // North (Up)
                        g.drawLine(centerX, centerY, centerX, centerY - lineLength);
                        break;
                    case 1: // East (Right)
                        g.drawLine(centerX, centerY, centerX + lineLength, centerY);
                        break;
                    case 2: // South (Down)
                        g.drawLine(centerX, centerY, centerX, centerY + lineLength);
                        break;
                    case 3: // West (Left)
                        g.drawLine(centerX, centerY, centerX - lineLength, centerY);
                        break;
                }
            } else {
                System.err.println("Player position out of bounds: [" + playerRow + "," + playerCol + "]"); // Debug if needed
            }
        } else {
            System.err.println("Player position is null or invalid."); // Debug if needed
        }
    }

    // Helper method to get color based on room type
    private Color getRoomColor(int roomType) {
        // Note: Start and Boss rooms share value '2' in the current RPG code.
        // Assign unique values in RPG.addEventsToRooms if you want different colors.
        switch (roomType) {
            case 0: return COLOR_EMPTY; // Should not happen if called correctly
            case 1: return COLOR_NORMAL;
            case 2: return COLOR_START; // Assuming 2 is primarily Start/Boss
            case 3: return COLOR_ENEMY_STD;
            case 4: return COLOR_ENEMY_DIFF;
            case 5: return COLOR_SHOP;
            case 6: return COLOR_SMITH;
            case 7: return COLOR_TREASURE;
            case 8: return COLOR_PLAIN;
            case 9: return COLOR_SHRINE;
            default: // Any other unexpected value
                System.err.println("Warning: Unknown room type encountered: " + roomType); // Debug
                return COLOR_NORMAL; // Default to normal room color
        }
    }

    // Method to trigger a repaint, e.g., after player moves or turns
    public void refreshMap() {
        this.revalidate(); // Ensure layout is correct
        this.repaint();    // Redraw the component
    }
}