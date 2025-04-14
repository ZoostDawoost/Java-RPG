/**
 * @file    MapPanel.java
 * @brief   Custom JPanel responsible for drawing the game map, player, and handling visibility.
 * Uses central map data and player-specific visited/explored data.
 *
 * Renders the map based on game state:
 * - Full map mode (F8): Shows all rooms and their colors.
 * - Normal mode:
 * - Shows visited rooms with their actual color.
 * - Shows explored but not visited rooms with a neutral color (outline).
 * - Hides unexplored areas.
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
 * - 1.0.4 (2025-04-14): Updated to use central RPG map and player-specific explored/visited state. (AI Assistant)
 * - 1.0.3 (2025-04-14): Implemented visitedMap logic. (AI Assistant)
 * - 1.0.2 (2025-04-14): Updated paintComponent to hide room color until explored. (AI Assistant)
 * - 1.0.1 (2025-04-14): Modified paintComponent for exploredMap/showFullMap. (J. Schulte)
 * - 1.0.0 (2025-04-01): Initial version drawing the full map and player. (J. Schulte)
 */

 import javax.swing.*;
 import java.awt.*;
 
 public class MapPanel extends JPanel {
 
     private RPG rpgGame; // Reference to the central RPG object (holds map structure)
     private final int TILE_SIZE = 20;
     private final int GRID_OFFSET_X = 50;
     private final int GRID_OFFSET_Y = 20;
 
     // Define colors
     private final Color COLOR_EMPTY = Color.BLACK;
     private final Color COLOR_EXPLORED_NEUTRAL = Color.DARK_GRAY;
     private final Color COLOR_NORMAL = Color.LIGHT_GRAY;
     private final Color COLOR_START = Color.CYAN;
     private final Color COLOR_BOSS = Color.MAGENTA;
     private final Color COLOR_ENEMY_STD = Color.RED;
     private final Color COLOR_ENEMY_DIFF = new Color(139, 0, 0);
     private final Color COLOR_SHOP = Color.YELLOW;
     private final Color COLOR_SMITH = Color.ORANGE;
     private final Color COLOR_TREASURE = Color.GREEN;
     private final Color COLOR_PLAIN = Color.WHITE;
     private final Color COLOR_SHRINE = Color.BLUE;
     private final Color COLOR_PLAYER = Color.PINK;
     private final Color COLOR_BORDER = new Color(80, 80, 80);
 
     // Constructor takes the central RPG object
     public MapPanel(RPG centralRpg) {
         this.rpgGame = centralRpg; // Store reference to the object with the map structure
 
         int mapWidth = 21;
         int mapHeight = 21;
         // Use dimensions from the central map object if available
         if (rpgGame != null && rpgGame.map != null && rpgGame.map.length > 0 && rpgGame.map[0].length > 0) {
              mapHeight = rpgGame.map.length;
              mapWidth = rpgGame.map[0].length;
         }
         this.setPreferredSize(new Dimension(mapWidth * TILE_SIZE + 2 * GRID_OFFSET_X, mapHeight * TILE_SIZE + 2 * GRID_OFFSET_Y));
         this.setBackground(COLOR_EMPTY);
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
 
         // Ensure central map and player list are ready
         if (rpgGame == null || rpgGame.map == null || RPG.getPlayers() == null || RPG.getPlayers().isEmpty()) {
             g.setColor(Color.WHITE);
             g.drawString("Waiting for game data...", 20, 30);
             return;
         }
 
         // Get player 0's state (assuming single player view for now)
         RPG player = RPG.getPlayers().get(0);
         if (player == null || player.getExploredMap() == null || player.getVisitedMap() == null) {
              g.setColor(Color.WHITE);
              g.drawString("Waiting for player data...", 20, 30);
              return;
         }
 
         // Get necessary state for drawing
         int[][] worldMap = rpgGame.map; // Use the central map structure
         boolean[][] explored = player.getExploredMap(); // Use player's explored state
         boolean[][] visited = player.getVisitedMap();   // Use player's visited state
         boolean showFull = RPG.isShowFullMap(); // Use the static global flag
         int numRows = worldMap.length;
         int numCols = worldMap[0].length;
 
         // Draw the map grid
         for (int row = 0; row < numRows; row++) {
             for (int col = 0; col < numCols; col++) {
                 int tileX = GRID_OFFSET_X + col * TILE_SIZE;
                 int tileY = GRID_OFFSET_Y + row * TILE_SIZE;
                 int roomType = worldMap[row][col]; // Type from the central map
 
                 boolean isExplored = explored[row][col]; // From player's map
                 boolean isVisited = visited[row][col];   // From player's map
 
                 Color tileColor = COLOR_EMPTY;
                 boolean drawBorder = false;
 
                 // Determine appearance based on central map type and player state
                 if (roomType != 0) {
                     if (showFull) {
                         tileColor = getRoomColor(roomType);
                         drawBorder = true;
                     } else {
                         if (isVisited) {
                             tileColor = getRoomColor(roomType);
                             drawBorder = true;
                         } else if (isExplored) {
                             tileColor = COLOR_EXPLORED_NEUTRAL;
                             drawBorder = true;
                         }
                     }
                 }
 
                 // Draw fill and border
                 g.setColor(tileColor);
                 g.fillRect(tileX, tileY, TILE_SIZE, TILE_SIZE);
                 if (drawBorder) {
                     g.setColor(COLOR_BORDER);
                     g.drawRect(tileX, tileY, TILE_SIZE - 1, TILE_SIZE - 1);
                 }
             }
         }
 
         // Draw the player marker (using player's position)
         int[] playerPos = player.getCurrentPos();
         if (playerPos != null && playerPos.length == 2 && playerPos[0] >= 0) { // Check if valid position
             int playerRow = playerPos[0];
             int playerCol = playerPos[1];
 
             if (playerRow < numRows && playerCol < numCols) { // Ensure bounds
                 int playerTileX = GRID_OFFSET_X + playerCol * TILE_SIZE;
                 int playerTileY = GRID_OFFSET_Y + playerRow * TILE_SIZE;
 
                 g.setColor(COLOR_PLAYER);
                 int markerSize = TILE_SIZE / 2;
                 int offset = (TILE_SIZE - markerSize) / 2;
                 g.fillOval(playerTileX + offset, playerTileY + offset, markerSize, markerSize);
 
                 // Draw direction indicator (using player's facing)
                 g.setColor(Color.BLACK);
                 int centerX = playerTileX + TILE_SIZE / 2;
                 int centerY = playerTileY + TILE_SIZE / 2;
                 int lineLength = TILE_SIZE / 3;
                 switch (player.getWayFacing()) {
                     case 0: g.drawLine(centerX, centerY, centerX, centerY - lineLength); break; // N
                     case 1: g.drawLine(centerX, centerY, centerX + lineLength, centerY); break; // E
                     case 2: g.drawLine(centerX, centerY, centerX, centerY + lineLength); break; // S
                     case 3: g.drawLine(centerX, centerY, centerX - lineLength, centerY); break; // W
                 }
             }
         }
     }
 
     // Helper method to get color based on room type (no changes here)
     private Color getRoomColor(int roomType) {
         switch (roomType) {
             case 0: return COLOR_EMPTY;
             case 1: return COLOR_NORMAL; // Should ideally not be seen often if events placed
             case 2: return COLOR_START;
             case 3: return COLOR_ENEMY_STD;
             case 4: return COLOR_ENEMY_DIFF;
             case 5: return COLOR_SHOP;
             case 6: return COLOR_SMITH;
             case 7: return COLOR_TREASURE;
             case 8: return COLOR_PLAIN;
             case 9: return COLOR_SHRINE;
             default: return COLOR_NORMAL; // Fallback
         }
     }
 
     // Method to trigger a repaint
     public void refreshMap() {
         this.revalidate();
         this.repaint();
     }
 }