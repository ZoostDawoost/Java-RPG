/**
 * @file    RoomLegendPanel.java
 * @brief   Displays a legend of room types, counts, visited counts, and colors.
 * Text color changed to black for better readability.
 *
 * @author  Jack Schulte & Gemini
 * @version 1.1.0
 * @date    2025-04-15
 *
 * @copyright Copyright (c) 2025 Jack Schulte & Gemini. All rights reserved.
 * Strictly confidential and proprietary. Distribution, reproduction,
 * or modification is strictly prohibited without prior written permission.
 *
 * @version History:
 * - 1.1.0 (2025-04-15): Changed text color from White to Black. (Gemini)
 * - 1.0.0 (2025-04-15): Initial version. (J. Schulte)
 */

 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import java.awt.*;
 import java.util.EnumMap;
 import java.util.Map;

 public class RoomLegendPanel extends JPanel {

     private final JPanel legendContentPanel;
     private final Font headerFont = new Font("Trebuchet MS", Font.BOLD, 16);
     private final Font dataFont = new Font("Trebuchet MS", Font.PLAIN, 14);
     private final Dimension colorSwatchSize = new Dimension(20, 14);
     // Determine background color - adjust text color if background is very dark
     private final Color panelBackgroundColor = UIManagement.backgroundColor.darker();
     private final Color panelTextColor = Color.BLACK; // *** CHANGED TO BLACK ***
     private final Color swatchBorderColor = Color.DARK_GRAY; // Border for color swatch

     public RoomLegendPanel() {
         setLayout(new BorderLayout(5, 5));
         setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
                 new EmptyBorder(10, 10, 10, 10)
         ));
         setBackground(panelBackgroundColor); // Use variable for background

         JLabel titleLabel = new JLabel("Room Legend (F9 to Close)");
         titleLabel.setFont(headerFont.deriveFont(Font.BOLD, 18f));
         titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
         titleLabel.setForeground(panelTextColor); // *** Use text color variable ***
         add(titleLabel, BorderLayout.NORTH);

         legendContentPanel = new JPanel();
         legendContentPanel.setLayout(new GridBagLayout());
         legendContentPanel.setBackground(getBackground()); // Match panel background
         legendContentPanel.setOpaque(false); // Make transparent if needed

         JScrollPane scrollPane = new JScrollPane(legendContentPanel);
         scrollPane.setBorder(null); // Remove scroll pane border
         scrollPane.getViewport().setBackground(getBackground()); // Match background
         scrollPane.setOpaque(false); // Make scroll pane background transparent
         scrollPane.getViewport().setOpaque(false); // Make viewport background transparent
         add(scrollPane, BorderLayout.CENTER);

         // Initial population with headers (counts will be updated)
         updateLegend(null); // Pass null initially
     }

     /**
      * Updates the legend display based on the current board state.
      * @param board The current game Board.
      */
     public void updateLegend(Board board) {
         legendContentPanel.removeAll(); // Clear previous content
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.gridx = 0;
         gbc.gridy = 0;
         gbc.anchor = GridBagConstraints.WEST;
         gbc.insets = new Insets(3, 5, 3, 5);
         gbc.fill = GridBagConstraints.HORIZONTAL;

         // Add Headers
         addHeaderLabel("Color", gbc);
         gbc.gridx++; addHeaderLabel("Room Type", gbc);
         gbc.gridx++; addHeaderLabel("Total", gbc);
         gbc.gridx++; addHeaderLabel("Visited", gbc);
         gbc.gridy++;

         // Calculate counts if board is valid
         Map<Room.RoomType, Integer> totalCounts = new EnumMap<>(Room.RoomType.class);
         Map<Room.RoomType, Integer> visitedCounts = new EnumMap<>(Room.RoomType.class);
         if (board != null) {
             for (int r = 0; r < board.getHeight(); r++) {
                 for (int c = 0; c < board.getWidth(); c++) {
                     Room room = board.getRoom(r, c);
                     if (room != null) {
                         Room.RoomType type = room.getRoomType();
                         // Only count relevant room types for the legend
                         if (type != Room.RoomType.EMPTY_SPACE && type != Room.RoomType.UNKNOWN) {
                             totalCounts.put(type, totalCounts.getOrDefault(type, 0) + 1);
                             if (room.isVisited()) {
                                 visitedCounts.put(type, visitedCounts.getOrDefault(type, 0) + 1);
                             }
                         }
                     }
                 }
             }
         }

         // Add Row for each relevant RoomType
         for (Room.RoomType type : Room.RoomType.values()) {
             // Skip types we don't want in the legend
             if (type == Room.RoomType.EMPTY_SPACE || type == Room.RoomType.UNKNOWN) continue;

             gbc.gridx = 0;
             gbc.fill = GridBagConstraints.NONE; // Don't stretch color swatch horizontally
             gbc.weightx = 0; // Reset horizontal weight
             String colorHex = ConfigLoader.getColor(type.name(), "#808080"); // Get color from config
             addLabel(createColorSwatch(colorHex), gbc); // Color Swatch

             gbc.gridx++;
             gbc.fill = GridBagConstraints.HORIZONTAL; // Stretch text labels
             gbc.weightx = 1.0; // Give Room Type column extra horizontal space
             addLabel(type.getDescription(), gbc); // Room Type Name

             gbc.gridx++;
             gbc.weightx = 0; // Reset weight for count columns
             addLabel(String.valueOf(totalCounts.getOrDefault(type, 0)), gbc); // Total Count

             gbc.gridx++;
             addLabel(String.valueOf(visitedCounts.getOrDefault(type, 0)), gbc); // Visited Count

             gbc.gridy++; // Move to the next row
         }

         // Add filler component to push rows to the top if content is short
         gbc.gridy++;
         gbc.weighty = 1.0; // Give vertical weight to this filler
         gbc.gridwidth = 4; // Span all columns
         gbc.fill = GridBagConstraints.VERTICAL;
         legendContentPanel.add(Box.createVerticalGlue(), gbc);


         legendContentPanel.revalidate();
         legendContentPanel.repaint();
         // Also repaint parent scrollpane/panel if needed
         this.revalidate();
         this.repaint();
     }

     // Helper to add header labels with specific styling
     private void addHeaderLabel(String text, GridBagConstraints gbc) {
         JLabel label = new JLabel(text);
         label.setFont(headerFont);
         label.setForeground(panelTextColor); // *** Use text color variable ***
         label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, panelTextColor)); // Underline with text color
         legendContentPanel.add(label, gbc);
     }

     // Helper to add data labels with specific styling
     private void addLabel(String text, GridBagConstraints gbc) {
          JLabel label = new JLabel(text);
          label.setFont(dataFont);
          label.setForeground(panelTextColor); // *** Use text color variable ***
          legendContentPanel.add(label, gbc);
      }

     // Overload for adding components like the color swatch
     private void addLabel(Component comp, GridBagConstraints gbc) {
         legendContentPanel.add(comp, gbc);
     }

     // Creates a small colored square panel for the legend
     private JPanel createColorSwatch(String hexColor) {
         JPanel swatch = new JPanel();
         try {
             swatch.setBackground(Color.decode(hexColor));
         } catch (NumberFormatException e) {
             swatch.setBackground(Color.GRAY); // Default if color string invalid
             System.err.println("Invalid hex color in config/legend: " + hexColor);
         }
         swatch.setPreferredSize(colorSwatchSize);
         swatch.setMinimumSize(colorSwatchSize);
         swatch.setMaximumSize(colorSwatchSize);
         swatch.setBorder(BorderFactory.createLineBorder(swatchBorderColor, 1)); // Use border color variable
         return swatch;
     }
 }