/**
 * @file    PlayerPanel.java
 * @brief   Custom JPanel to display the player's selected character image.
 * Image now fills the panel area.
 *
 * @author  Jack Schulte
 * @version 1.1.0 (Layout Fix)
 * @date    2025-04-15
 *
 * @copyright Copyright (c) 2025 Jack Schulte . All rights reserved.
 * Strictly confidential and proprietary. Distribution, reproduction,
 * or modification is strictly prohibited without prior written permission.
 *
 * @version History:
 * - 1.1.0 (2025-04-15): Removed internal padding so image fills the panel.
 * - 1.0.0 (2025-04-15): Initial Version.
 */

 import javax.swing.*;
 import javax.swing.border.Border; // Keep
 import java.awt.*;
 
 public class PlayerPanel extends JPanel {
 
     private JLabel playerImageLabel;
     private static final int PANEL_SIZE = 80; // Keep desired panel size
 
     public PlayerPanel() {
         setPreferredSize(new Dimension(PANEL_SIZE, PANEL_SIZE));
         setLayout(new BorderLayout());
         setBorder(BorderFactory.createTitledBorder("Player"));
         setBackground(UIManagement.backgroundColor);
 
         playerImageLabel = new JLabel();
         playerImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
         playerImageLabel.setVerticalAlignment(SwingConstants.CENTER);
         // ** FIX: Label size matches panel size - no internal padding **
         playerImageLabel.setPreferredSize(new Dimension(PANEL_SIZE, PANEL_SIZE));
 
         add(playerImageLabel, BorderLayout.CENTER);
     }
 
     /**
      * Updates the displayed player image. Scales the image to fit the label.
      * @param player The Player object containing the image icon.
      */
     public void updatePlayerImage(Player player) {
         if (player == null || player.getSelectedCharacterIcon() == null) {
             playerImageLabel.setIcon(null);
             playerImageLabel.setText("?");
             return;
         }
 
         ImageIcon originalIcon = player.getSelectedCharacterIcon();
         Image originalImage = originalIcon.getImage();
 
         // Scale image to fit the label (which now fills the panel)
         int labelWidth = this.getWidth() > 0 ? this.getWidth() : PANEL_SIZE; // Use actual width if available
         int labelHeight = this.getHeight() > 0 ? this.getHeight() : PANEL_SIZE; // Use actual height
 
         int originalWidth = originalIcon.getIconWidth();
         int originalHeight = originalIcon.getIconHeight();
 
         if (originalWidth <= 0 || originalHeight <= 0) {
              playerImageLabel.setIcon(null); playerImageLabel.setText("Err"); return;
         }
 
         // Calculate scale factor based on the actual available space (panel size)
         double scale = Math.min((double)labelWidth / originalWidth, (double)labelHeight / originalHeight);
         // Ensure scale doesn't enlarge tiny images excessively if needed, but usually okay
         // scale = Math.min(scale, 2.0); // Optional: Limit upscaling
 
         int scaledWidth = (int)(originalWidth * scale);
         int scaledHeight = (int)(originalHeight * scale);
 
         if (scaledWidth > 0 && scaledHeight > 0) {
             Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
             ImageIcon scaledIcon = new ImageIcon(scaledImage);
             playerImageLabel.setIcon(scaledIcon);
         } else {
              playerImageLabel.setIcon(originalIcon); // Fallback to original if scaling fails
         }
         playerImageLabel.setText("");
     }
 }