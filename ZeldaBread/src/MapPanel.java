/**
 * @file    MapPanel.java
 * @brief   Custom JPanel drawing the game map, centered within the panel.
 * Uses Board/Room objects. Gets player info from Player object. Uses config colors.
 *
 * @author  Jack Schulte & Gemini
 * @version 1.4.1 (Fix)
 * @date    2025-04-15
 *
 * @copyright Copyright (c) 2025 Jack Schulte & Gemini. All rights reserved.
 * Strictly confidential and proprietary. Distribution, reproduction,
 * or modification is strictly prohibited without prior written permission.
 *
 * @version History:
 * - 1.4.1 (2025-04-15): Fixed type mismatch error when calling getRoomColor. Passed default hex string instead of Color object. (Gemini)
 * - 1.4.0 (2025-04-15): Implemented centering of map drawing within the panel. Removed fixed GRID_OFFSETs. Uses config colors. (Gemini)
 * - (Previous history omitted)
 */

 import javax.swing.*;
 import java.awt.*;

 public class MapPanel extends JPanel {

     private RPG centralGame;
     private final int TILE_SIZE = 16;

     // Default colors used ONLY if config loading fails or is missing entries
     // Defined as constants for clarity, but getRoomColor uses hex strings primarily.
     private final Color COLOR_PLAYER = Color.PINK;
     private final Color COLOR_BORDER = new Color(80, 80, 80);
     private final String COLOR_FALLBACK_HEX = "#808080"; // Fallback HEX STRING
     private final String COLOR_EXPLORED_HEX = "#696969"; // Default explored hex

     public MapPanel(RPG centralRpg) {
         this.centralGame = centralRpg;
         this.setBackground(Color.BLACK);
         this.setOpaque(true);
     }

     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);

         if (centralGame == null || centralGame.getBoard() == null || RPG.getPlayers() == null || RPG.getPlayers().isEmpty()) {
             g.setColor(Color.WHITE); g.drawString("Waiting...", 10, 20); return;
         }

         Board board = centralGame.getBoard();
         Player player = RPG.getPlayers().get(0);
         if (player == null) { g.setColor(Color.WHITE); g.drawString("No Player...", 10, 20); return; }

         boolean showFull = RPG.isShowFullMap();
         int numRows = board.getHeight();
         int numCols = board.getWidth();

         int totalMapDrawingWidth = numCols * TILE_SIZE;
         int totalMapDrawingHeight = numRows * TILE_SIZE;
         int panelWidth = getWidth();
         int panelHeight = getHeight();
         int offsetX = Math.max(0, (panelWidth - totalMapDrawingWidth) / 2);
         int offsetY = Math.max(0, (panelHeight - totalMapDrawingHeight) / 2);

         // Draw the map grid
         for (int row = 0; row < numRows; row++) {
             for (int col = 0; col < numCols; col++) {
                 int tileX = offsetX + col * TILE_SIZE;
                 int tileY = offsetY + row * TILE_SIZE;

                 Room room = board.getRoom(row, col);
                 if (room == null) continue;

                 Room.RoomType roomType = room.getRoomType();
                 boolean isExplored = room.isExplored();
                 boolean isVisited = room.isVisited();
                 Color tileColor = getBackground(); // Default to panel background
                 boolean drawBorder = false;

                 if (roomType != Room.RoomType.EMPTY_SPACE) {
                      String colorKey = roomType.name();
                      // ** FIX: Define the default color AS A STRING here **
                      String defaultColorHex = COLOR_FALLBACK_HEX; // Default hex string

                      // Assign default color hex based on room type if needed (optional, config should handle)
                      // switch(roomType) { case START_ROOM: defaultColorHex = "#00FFFF"; break; ... etc ... }

                     if (showFull) {
                         tileColor = getRoomColor(colorKey, defaultColorHex); // Pass string default
                         drawBorder = true;
                     } else {
                         if (isVisited) {
                             tileColor = getRoomColor(colorKey, defaultColorHex); // Pass string default
                             drawBorder = true;
                         } else if (isExplored) {
                              tileColor = getRoomColor("EXPLORED_NEUTRAL", COLOR_EXPLORED_HEX); // Pass specific default
                              drawBorder = true;
                         }
                     }
                 }

                 // Draw fill and border only if tileColor is not the background color
                 if (!tileColor.equals(getBackground())) {
                     g.setColor(tileColor);
                     g.fillRect(tileX, tileY, TILE_SIZE, TILE_SIZE);
                     if (drawBorder) {
                         g.setColor(COLOR_BORDER);
                         g.drawRect(tileX, tileY, TILE_SIZE - 1, TILE_SIZE - 1);
                     }
                 }
             }
         }

         // Draw the player marker (using calculated offsets)
         int[] playerPos = player.getCurrentPos();
         if (playerPos != null && playerPos.length == 2 && playerPos[0] >= 0) {
             int playerRow = playerPos[0]; int playerCol = playerPos[1];
             if (playerRow < numRows && playerCol < numCols) {
                 int playerTileX = offsetX + playerCol * TILE_SIZE;
                 int playerTileY = offsetY + playerRow * TILE_SIZE;
                 g.setColor(COLOR_PLAYER);
                 int markerSize = TILE_SIZE / 2; int markerOffset = (TILE_SIZE - markerSize) / 2;
                 g.fillOval(playerTileX + markerOffset, playerTileY + markerOffset, markerSize, markerSize);
                 g.setColor(Color.BLACK);
                 int centerX = playerTileX + TILE_SIZE / 2; int centerY = playerTileY + TILE_SIZE / 2;
                 int lineLength = Math.max(1, TILE_SIZE / 3);
                 switch (player.getWayFacing()) { /* N E S W lines */ case 0: g.drawLine(centerX, centerY, centerX, centerY-lineLength); break; case 1: g.drawLine(centerX, centerY, centerX+lineLength, centerY); break; case 2: g.drawLine(centerX, centerY, centerX, centerY+lineLength); break; case 3: g.drawLine(centerX, centerY, centerX-lineLength, centerY); break; }
             }
         }
     }

     /**
      * Helper method to get color from ConfigLoader.
      * @param roomTypeName The enum name (e.g., "ENEMY_ROOM").
      * @param defaultHexColor Default color HEX STRING (e.g., "#FF0000").
      * @return The loaded Color object or default if not found/invalid.
      */
     private Color getRoomColor(String roomTypeName, String defaultHexColor) { // Signature expects String
         String hexColor = ConfigLoader.getColor(roomTypeName, defaultHexColor);
         try {
             return Color.decode(hexColor);
         } catch (NumberFormatException e) {
             System.err.println("Warning: Invalid color format '" + hexColor + "' for " + roomTypeName + ". Using default " + defaultHexColor);
             try {
                  return Color.decode(defaultHexColor); // Try decoding the passed default hex
             } catch (NumberFormatException e2) {
                  // Ultimate fallback if defaultHexColor itself was bad
                  return Color.decode(COLOR_FALLBACK_HEX);
             }
         }
     }

     public void refreshMap() { this.repaint(); }
 }