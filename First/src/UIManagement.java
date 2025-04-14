/**
 * @file    UIManagement.java
 * @brief   Manages the main game window (JFrame), UI panels (using CardLayout), button actions, key bindings, and screen transitions. Initializes game state after character creation.
 *
 * Sets up the overall graphical user interface, handles user input via buttons and keyboard (arrow keys, F8),
 * orchestrates player creation, map generation, and starts the game screen.
 *
 * @author  Jack Schulte
 * @version 1.0.2
 * @date    2025-04-14
 *
 * @copyright Copyright (c) 2025 Jack Schulte. All rights reserved.
 * Strictly confidential and proprietary. Distribution, reproduction,
 * or modification is strictly prohibited without prior written permission.
 *
 * @version History:
 * - 1.0.2 (2025-04-14): Refactored initialization: Removed SwingWorker, create/build RPG objects after character creation. Fixed movement calls. (AI Assistant)
 * - 1.0.1 (2025-04-14): Added F8 key binding to toggle map visibility via RPG.toggleMapVisibility() and mapPanel.refreshMap(). Updated comments. (J. Schulte)
 * - 1.0.0 (2025-04-01): Initial version with screen setup, character creation flow, and basic movement buttons/key bindings. (J. Schulte)
 */

 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import javax.swing.border.LineBorder;
 
 public class UIManagement {
 
     // UI Components (static as they belong to the single UI instance)
     private static JFrame frame = new JFrame();
     private static JPanel topPanel = new JPanel(new BorderLayout());
     private static JLabel titleLabel = new JLabel("Legend of Splenda: Bread of the Wild", SwingConstants.CENTER);
     private static JLabel subtitleLabel = new JLabel("A roguelike RPG by Jack Schulte", SwingConstants.CENTER);
     private static JPanel centerPanel = new JPanel(new CardLayout());
     private static JTextArea descArea = new JTextArea();
     private static JScrollPane descScrollPane;
     private static MapPanel mapPanel; // Map display panel (created later)
     private static JPanel bottomPanel = new JPanel(new BorderLayout());
     private static JLabel commentLabel = new JLabel("", SwingConstants.CENTER);
     private static JPanel buttonPanel = new JPanel();
     private static GridBagConstraints gbc = new GridBagConstraints();
 
     // Fonts and Colors
     private static Font titleFont = new Font("Trebuchet MS", Font.BOLD, 24);
     private static Font subtitleFont = new Font("Trebuchet MS", Font.BOLD, 20);
     private static Font descFont = new Font("Trebuchet MS", Font.PLAIN, 18);
     private static Font commentFont = new Font("Trebuchet MS", Font.ITALIC, 18);
     private static Font buttonFont = new Font("Aharoni", Font.BOLD, 20);
     private static Font buttonFontHover = new Font("Aharoni", Font.BOLD, 22);
     private static Color backgroundColor = Color.decode("#EEEEEE");
 
     // Game State Object (central map structure) - Instance variable, created later
     private static RPG rpg; // Holds the main map[][] data
 
     // Character Selection Specific
     private static String[] characterButtonLabels = {"Knight", "Sentinel", "Assassin", "Caveman"};
     private static String[] characterButtonColors = {"#48AD48", "#58D558"}; // Normal, Hover
     private static String doneButtonLabel = "Done";
     private static String[] doneButtonColors = {"#EECC44", "#F7DD77"}; // Normal, Hover
     private static int[] characterButtonLengths = new int[]{140, 140, 140, 140, 100};
     private static Map<String, JButton> characterButtonMap = new HashMap<>();
     private static String selectedClass = null;
     private static Color selectedColor = Color.decode("#58D58D");
     private static LineBorder selectedBorder = new LineBorder(Color.BLACK, 3);
     private static LineBorder defaultBorder = new LineBorder(Color.GRAY, 1);
 
     // Gameplay Specific
     private static String[] moveButtonLabels = {"Turn Left", "Forward", "Turn Right", "Backward"};
     private static String[] moveButtonColors = {"#44AADD", "#66CCFF"}; // Example colors
     private static boolean isGameScreenActive = false;
 
 
     public static void main(String[] args) {
         // Use SwingUtilities.invokeLater to ensure UI creation happens on the Event Dispatch Thread
         SwingUtilities.invokeLater(() -> {
             setupUI();
             titleScreen(); // Start the UI flow
             frame.setVisible(true);
         });
     }
 
     // Method to set up the main UI structure
     private static void setupUI() {
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(800, 600);
         frame.setLayout(new BorderLayout());
 
         // Top Panel (Title/Subtitle)
         titleLabel.setOpaque(true);
         titleLabel.setFont(titleFont);
         titleLabel.setForeground(Color.decode("#B6220E"));
         titleLabel.setBackground(backgroundColor);
         subtitleLabel.setOpaque(true);
         subtitleLabel.setFont(subtitleFont);
         subtitleLabel.setBackground(backgroundColor);
         topPanel.add(titleLabel, BorderLayout.NORTH);
         topPanel.add(subtitleLabel, BorderLayout.SOUTH);
         topPanel.setBackground(backgroundColor);
 
         // Center Panel (CardLayout: Description or Map)
         descArea.setWrapStyleWord(true);
         descArea.setLineWrap(true);
         descArea.setEditable(false);
         descArea.setFont(descFont);
         descArea.setBackground(backgroundColor);
         descScrollPane = new JScrollPane(descArea);
         descScrollPane.setBorder(null);
         descScrollPane.setBackground(backgroundColor);
         centerPanel.add(descScrollPane, "description"); // Map panel added later
         centerPanel.setBackground(backgroundColor);
 
         // Bottom Panel (Comments/Buttons)
         commentLabel.setFont(commentFont);
         commentLabel.setBackground(backgroundColor);
         commentLabel.setOpaque(true);
         buttonPanel.setBackground(backgroundColor);
         buttonPanel.setLayout(new GridBagLayout());
         gbc.insets = new Insets(5, 10, 5, 10);
         gbc.gridy = 0;
         bottomPanel.setBackground(backgroundColor);
         bottomPanel.add(commentLabel, BorderLayout.NORTH);
         bottomPanel.add(buttonPanel, BorderLayout.CENTER);
 
         // Final Frame Assembly
         frame.add(topPanel, BorderLayout.NORTH);
         frame.add(centerPanel, BorderLayout.CENTER);
         frame.add(bottomPanel, BorderLayout.SOUTH);
 
         // Setup Key Bindings
         setupKeyBindings();
     }
 
 
     // ==============================================================================================
     // =================== Screen Management & Game Initialization ==================================
 
     // Switches the view in the center panel
     private static void showScreen(String screenName) {
         // Ensure mapPanel exists ONLY if switching to map
         if (screenName.equals("map") && mapPanel == null) {
              System.err.println("Attempted to switch to map screen, but mapPanel is null!");
              JOptionPane.showMessageDialog(frame, "Error: Map Panel not ready.", "Error", JOptionPane.ERROR_MESSAGE);
              return;
         }
 
         CardLayout cl = (CardLayout) (centerPanel.getLayout());
         cl.show(centerPanel, screenName);
         isGameScreenActive = screenName.equals("map");
         System.out.println("Switched to screen: " + screenName + ", isGameScreenActive: " + isGameScreenActive);
 
         // Focus for key bindings on game screen
         if (isGameScreenActive) {
             frame.getRootPane().requestFocusInWindow();
             commentLabel.setText("Use buttons or ←↑→↓ keys. Press F8 to toggle map.");
         }
     }
 
     // Initial title screen
     private static void titleScreen() {
         titleLabel.setText("Legend of Splenda: Bread of the Wild");
         subtitleLabel.setText("A roguelike RPG by Jack Schulte");
         descArea.setText("Welcome, adventurer!");
         commentLabel.setText("");
         showScreen("description");
 
         String[] titleButtons = {"Start!", "#EECC44", "#F7DD77"};
         int[] titleButtonLengths = {120};
         makeButtons(titleButtons, titleButtonLengths, "title");
     }
 
     // Screen to choose number of players
     private static void numPlayersScreen() {
         titleLabel.setText("Number of Players");
         subtitleLabel.setText("Choose the number of players:");
         descArea.setText("");
         commentLabel.setText("");
         showScreen("description");
 
         String[] numPlayersButtons = {"1 Player", "#48AD48", "#58D558", "2 Players", "#48AD48", "#58D558"};
         int[] numPlayersButtonLengths = {140, 140};
         makeButtons(numPlayersButtons, numPlayersButtonLengths, "numPlayers");
     }
 
     // Screen for character class selection
     private static void characterCreatorScreen() {
         selectedClass = null;
         characterButtonMap.clear();
 
         titleLabel.setText("Player " + (RPG.getPlayerIndex() + 1) + ", Create your Character");
         subtitleLabel.setText("Select a Class:");
         descArea.setText("");
         commentLabel.setText("");
         showScreen("description");
 
         // Combine class buttons and Done button config
         ArrayList<String> buttonConfigList = new ArrayList<>();
         ArrayList<Integer> lengthConfigList = new ArrayList<>();
         for (String label : characterButtonLabels) {
             buttonConfigList.add(label); buttonConfigList.add(characterButtonColors[0]); buttonConfigList.add(characterButtonColors[1]);
             lengthConfigList.add(characterButtonLengths[0]);
         }
         buttonConfigList.add(doneButtonLabel); buttonConfigList.add(doneButtonColors[0]); buttonConfigList.add(doneButtonColors[1]);
         lengthConfigList.add(characterButtonLengths[characterButtonLengths.length - 1]);
 
         makeButtons(buttonConfigList.toArray(new String[0]),
                     lengthConfigList.stream().mapToInt(i -> i).toArray(),
                     "characterCreator");
     }
 
 
     // --- Central Game Initialization Logic ---
     private static void initializeAndStartGame() {
          System.out.println("All players created. Initializing game world...");
 
          // 1. Create the central RPG object to hold the map structure
          rpg = new RPG();
 
          // 2. Build the map structure in the central object
          System.out.println("Building map structure...");
          rpg.buildMap();
          rpg.printMap("Generated Map Structure"); // Debug print
 
          // 3. Add events (enemies, shops etc.) to the map structure
          System.out.println("Adding events to rooms...");
          rpg.addEventsToRooms();
          rpg.printMap("Map With Events"); // Debug print
 
          // 4. Set initial state for Player 0 (position, explored, visited)
          //    (Assuming single player for now, or player 0 starts)
          if (!RPG.getPlayers().isEmpty()) {
              RPG player0 = RPG.getPlayers().get(0);
              int startRow = rpg.map.length / 2; // Get start coords (should match buildMap)
              int startCol = rpg.map[0].length / 2;
 
              System.out.println("Setting initial state for Player 0 at ["+startRow+","+startCol+"]");
              player0.setCurrentPos(new int[]{startRow, startCol});
              player0.setWayFacing(0); // Facing North
              player0.getVisitedMap()[startRow][startCol] = true; // Mark start as visited
              // Initial exploration needs the actual world map
              player0.exploreAround(startRow, startCol, rpg.map);
 
              player0.printBooleanMap("Player 0 Initial Explored", player0.getExploredMap()); // Debug
              player0.printBooleanMap("Player 0 Initial Visited", player0.getVisitedMap());   // Debug
 
          } else {
              System.err.println("ERROR: No players found during game initialization!");
              JOptionPane.showMessageDialog(frame, "Error: Player data not found.", "Initialization Error", JOptionPane.ERROR_MESSAGE);
              return; // Cannot proceed
          }
 
          // 5. Create the MapPanel, passing the central RPG object (for map layout)
          System.out.println("Creating Map Panel...");
          mapPanel = new MapPanel(rpg); // mapPanel needs the rpg object with the map[][]
          centerPanel.add(mapPanel, "map"); // Add map panel to the card layout
 
          // 6. Switch to the game screen
          System.out.println("Starting game screen...");
          startGameScreen(); // Sets up buttons and switches view
          System.out.println("Game initialization complete.");
     }
 
 
     // Sets up the UI elements for the main game screen
     private static void startGameScreen() {
         if (mapPanel == null || rpg == null || RPG.getPlayers().isEmpty()) {
              System.err.println("Cannot start game screen - essential components missing!");
              JOptionPane.showMessageDialog(frame, "Error: Game components not ready.", "Error", JOptionPane.ERROR_MESSAGE);
              return;
         }
         titleLabel.setText("Dungeon Exploration");
         updateGameSubtitle(); // Update HP etc.
         commentLabel.setText("Use buttons or arrow keys. F8 toggles map.");
 
         showScreen("map"); // Switch center panel view to the map
         mapPanel.refreshMap(); // Initial draw
 
         // Set up Gameplay Buttons
         ArrayList<String> buttonConfigList = new ArrayList<>();
         ArrayList<Integer> lengthConfigList = new ArrayList<>();
         int buttonLength = 110;
         for (String label : moveButtonLabels) {
             buttonConfigList.add(label); buttonConfigList.add(moveButtonColors[0]); buttonConfigList.add(moveButtonColors[1]);
             lengthConfigList.add(buttonLength);
         }
         // Add other gameplay buttons here if needed
 
         makeButtons(buttonConfigList.toArray(new String[0]),
                     lengthConfigList.stream().mapToInt(i -> i).toArray(),
                     "gameplay");
 
         frame.getRootPane().requestFocusInWindow(); // Ensure keys work
     }
 
 
     // ==============================================================================================
     // =================== Action Listeners =========================================================
 
     // Assigns appropriate action listeners based on the button label and screen context
     private static void pickActionListener(JButton button, String buttonLabel, String screenContext) {
         // Remove previous listeners to prevent duplicates if screens are revisited
         for (java.awt.event.ActionListener al : button.getActionListeners()) {
             button.removeActionListener(al);
         }
 
         switch (screenContext) {
             case "title":
                 if (buttonLabel.equals("Start!")) {
                     button.addActionListener(e -> numPlayersScreen());
                 }
                 break;
 
             case "numPlayers":
                 if (buttonLabel.equals("1 Player") || buttonLabel.equals("2 Players")) {
                     button.addActionListener(e -> {
                         int num = buttonLabel.equals("1 Player") ? 1 : 2;
                         RPG.setNumPlayers(num);
                         RPG.createPlayers(); // Create player RPG objects
                         characterCreatorScreen(); // Move to class selection
                     });
                 }
                 break;
 
             case "characterCreator":
                  // Check if it's a class button
                  boolean isClassButton = false;
                  for(String classLbl : characterButtonLabels) {
                       if(buttonLabel.equals(classLbl)) {
                            isClassButton = true;
                            break;
                       }
                  }
 
                 if (isClassButton) {
                     // Action for Class Selection Buttons
                     button.addActionListener(e -> {
                         selectedClass = buttonLabel;
                         commentLabel.setText(buttonLabel + " selected!");
                         // Update appearance of class buttons
                         for (Map.Entry<String, JButton> entry : characterButtonMap.entrySet()) {
                             JButton btn = entry.getValue();
                             btn.setBackground(Color.decode(characterButtonColors[0])); // Reset first
                             btn.setBorder(defaultBorder);
                             if (entry.getKey().equals(selectedClass)) { // Highlight selected
                                 btn.setBackground(selectedColor);
                                 btn.setBorder(selectedBorder);
                             }
                         }
                     });
                 } else if (buttonLabel.equals(doneButtonLabel)) {
                     // Action for "Done" button
                     button.addActionListener(e -> {
                         if (selectedClass == null) {
                             commentLabel.setText("Please select a class first!");
                         } else {
                             RPG.assignClass(RPG.getPlayerIndex(), selectedClass); // Assign class to current player index
                             RPG.modPlayerIndex(); // Move to next player index
 
                             if (RPG.getPlayerIndex() < RPG.getNumPlayers()) {
                                 // More players need classes
                                 characterCreatorScreen();
                             } else {
                                 // All players created and assigned classes - Initialize and start!
                                 initializeAndStartGame();
                             }
                         }
                     });
                 }
                 break;
 
             case "gameplay":
                 // Actions for Gameplay Buttons
                 switch (buttonLabel) {
                     case "Turn Left":  button.addActionListener(e -> handleTurnLeft()); break;
                     case "Forward":    button.addActionListener(e -> handleMoveForward()); break;
                     case "Turn Right": button.addActionListener(e -> handleTurnRight()); break;
                     case "Backward":   button.addActionListener(e -> handleMoveBackward()); break;
                     // Add cases for other gameplay buttons
                 }
                 break;
         }
     }
 
     // Helper to update subtitle during gameplay
     private static void updateGameSubtitle() {
         // Assuming single player (player 0) for subtitle for now
         if (RPG.getPlayers() == null || RPG.getPlayers().isEmpty()) return;
         RPG currentPlayer = RPG.getPlayers().get(0);
         if (currentPlayer == null) return;
         subtitleLabel.setText("Player 1: " + currentPlayer.getClassName() + " | HP: " + currentPlayer.showHealth());
     }
 
     // ==============================================================================================
     // =================== Gameplay Action Handlers (for Buttons and Keys) ==========================
 
     // Handles turning left
     private static void handleTurnLeft() {
         if (!isGameScreenActive || mapPanel == null || RPG.getPlayers().isEmpty()) return;
         RPG.getPlayers().get(0).turnPlayer(0); // Turn player 0 left
         mapPanel.refreshMap();
         updateGameSubtitle();
         commentLabel.setText("Turned left.");
         frame.getRootPane().requestFocusInWindow();
     }
 
     // Handles turning right
     private static void handleTurnRight() {
         if (!isGameScreenActive || mapPanel == null || RPG.getPlayers().isEmpty()) return;
         RPG.getPlayers().get(0).turnPlayer(1); // Turn player 0 right
         mapPanel.refreshMap();
         updateGameSubtitle();
         commentLabel.setText("Turned right.");
         frame.getRootPane().requestFocusInWindow();
     }
 
     // Handles moving forward
     private static void handleMoveForward() {
         if (!isGameScreenActive || mapPanel == null || rpg == null || RPG.getPlayers().isEmpty()) return;
         RPG player = RPG.getPlayers().get(0);
         // Player moves based on their facing direction, checking against the central map
         if (player.movePlayer(player.getWayFacing(), rpg.map)) {
             mapPanel.refreshMap();
             updateGameSubtitle();
             int[] pos = player.getCurrentPos();
             commentLabel.setText("Moved forward into room type: " + rpg.map[pos[0]][pos[1]]);
             // Check room event?
         } else {
             commentLabel.setText("Cannot move forward!");
         }
         frame.getRootPane().requestFocusInWindow();
     }
 
     // Handles moving backward
     private static void handleMoveBackward() {
         if (!isGameScreenActive || mapPanel == null || rpg == null || RPG.getPlayers().isEmpty()) return;
         RPG player = RPG.getPlayers().get(0);
         int oppositeDirection = (player.getWayFacing() + 2) % 4; // Calculate opposite
         // Player moves backward, checking against the central map
         if (player.movePlayer(oppositeDirection, rpg.map)) {
             mapPanel.refreshMap();
             updateGameSubtitle();
             int[] pos = player.getCurrentPos();
             commentLabel.setText("Moved backward into room type: " + rpg.map[pos[0]][pos[1]]);
         } else {
             commentLabel.setText("Cannot move backward!");
         }
         frame.getRootPane().requestFocusInWindow();
     }
 
     // Handles toggling the map view
     private static void handleToggleMap() {
          if (!isGameScreenActive || mapPanel == null) return;
          RPG.toggleMapVisibility(); // Toggle static flag
          mapPanel.refreshMap(); // Redraw based on new flag state
          commentLabel.setText("Map view toggled (Show full: " + RPG.isShowFullMap() + ")");
          frame.getRootPane().requestFocusInWindow();
     }
 
 
     // ==============================================================================================
     // =================== Key Binding Setup ========================================================
 
     private static void setupKeyBindings() {
         InputMap inputMap = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
         ActionMap actionMap = frame.getRootPane().getActionMap();
 
         KeyStroke upKey = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
         KeyStroke downKey = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
         KeyStroke leftKey = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
         KeyStroke rightKey = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
         KeyStroke f8Key = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
 
         inputMap.put(upKey, "moveForward");
         inputMap.put(downKey, "moveBackward");
         inputMap.put(leftKey, "turnLeft");
         inputMap.put(rightKey, "turnRight");
         inputMap.put(f8Key, "toggleMap");
 
         actionMap.put("moveForward", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { handleMoveForward(); } });
         actionMap.put("moveBackward", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { handleMoveBackward(); } });
         actionMap.put("turnLeft", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { handleTurnLeft(); } });
         actionMap.put("turnRight", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { handleTurnRight(); } });
         actionMap.put("toggleMap", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { handleToggleMap(); } });
     }
 
 
     // ==============================================================================================
     // =================== UI Helpers (Button Creation, Hover Effects) ==============================
 
     // Creates buttons and adds them to the button panel
     private static void makeButtons(String[] stringArray, int[] intArray, String screenContext) {
         buttonPanel.removeAll(); // Clear previous buttons
 
         if (screenContext.equals("characterCreator")) {
             characterButtonMap.clear(); // Reset map for new set of buttons
         }
 
         for (int i = 0; i < stringArray.length / 3; i++) {
             String buttonLabel = stringArray[i * 3];
             JButton button = new JButton(buttonLabel);
             button.setPreferredSize(new Dimension(intArray[i], 50));
             String normalColor = stringArray[i * 3 + 1];
             String hoverColor = stringArray[i * 3 + 2];
             button.setBackground(Color.decode(normalColor));
             button.setFont(buttonFont);
             button.setBorder(defaultBorder);
             //button.setFocusable(false); // Allow focus, key binds use root pane anyway
 
             addHoverEffect(button, intArray[i], normalColor, hoverColor);
             pickActionListener(button, buttonLabel, screenContext); // Add action listener
 
             gbc.gridx = i;
             buttonPanel.add(button, gbc);
 
             // Store class buttons in map for selection styling
             if (screenContext.equals("characterCreator")) {
                  for(String classLbl : characterButtonLabels) {
                       if(buttonLabel.equals(classLbl)) {
                            characterButtonMap.put(buttonLabel, button);
                            break;
                       }
                  }
                  // Apply selected style if this button matches current selection
                 if (buttonLabel.equals(selectedClass)) {
                     button.setBackground(selectedColor);
                     button.setBorder(selectedBorder);
                 }
             }
         }
         buttonPanel.revalidate();
         buttonPanel.repaint();
     }
 
 
     // Adds mouse hover effects to a button
     private static void addHoverEffect(JButton button, int length, String colorNormal, String colorHover) {
         button.addMouseListener(new MouseAdapter() {
             Color normal = Color.decode(colorNormal);
             Color hover = Color.decode(colorHover);
             Dimension defaultSize = new Dimension(length, 50);
             Dimension hoverSize = new Dimension((int) (length * 1.05), 55); // Slightly larger on hover
 
             @Override
             public void mouseEntered(MouseEvent e) {
                 // Don't apply hover effect if it's the currently selected class button
                 boolean isSelectedCharButton = selectedClass != null && button.getText().equals(selectedClass) && characterButtonMap.containsKey(selectedClass);
                 if (!isSelectedCharButton) {
                     button.setPreferredSize(hoverSize);
                     button.setBackground(hover);
                     button.setFont(buttonFontHover);
                 }
                  // Update description based on hovered button during class selection
                  if (characterButtonMap.containsKey(button.getText()) || button.getText().equals(doneButtonLabel)) {
                       updateDescription(button.getText());
                  }
             }
 
             @Override
             public void mouseExited(MouseEvent e) {
                 // Reset style unless it's the selected class button
                  boolean isSelectedCharButton = selectedClass != null && button.getText().equals(selectedClass) && characterButtonMap.containsKey(selectedClass);
                  if (!isSelectedCharButton) {
                     button.setPreferredSize(defaultSize);
                     button.setBackground(normal);
                     button.setFont(buttonFont);
                  }
                  // Clear description or reset to selected class description
                  if (characterButtonMap.containsKey(button.getText()) || button.getText().equals(doneButtonLabel)) {
                      if(selectedClass != null) {
                           updateDescription(selectedClass); // Restore selected description
                      } else if (!isGameScreenActive) {
                           // If nothing selected and not in game, reset labels
                           subtitleLabel.setText("Select a Class:");
                           commentLabel.setText("");
                      }
                  }
             }
         });
     }
 
     // Updates description/comment labels based on button hover (mainly for char creation)
     private static void updateDescription(String label) {
         if (isGameScreenActive) return; // Don't update during gameplay
 
         switch (label) {
             case "Knight":   subtitleLabel.setText("Class: Knight"); commentLabel.setText("A strong, reliable warrior."); break;
             case "Sentinel": subtitleLabel.setText("Class: Sentinel"); commentLabel.setText("A sturdy defender."); break;
             case "Assassin": subtitleLabel.setText("Class: Assassin"); commentLabel.setText("A nimble rogue."); break;
             case "Caveman":  subtitleLabel.setText("Class: Caveman"); commentLabel.setText("Unga bunga?"); break;
             case "Done":     subtitleLabel.setText("Confirm Selection"); commentLabel.setText("Finalize character."); break;
             // default: // Don't clear subtitle if hovering over non-class button
         }
     }
 }