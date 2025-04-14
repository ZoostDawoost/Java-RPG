/**
 * @file    UIManagement.java
 * @brief   Manages the main game window (JFrame), UI panels (using CardLayout), button actions, key bindings, and screen transitions. Initializes game state after character creation. Instantiates StatusPanel and DialoguePanel.
 *
 * Sets up the overall graphical user interface, handles user input via buttons and keyboard (arrow keys, F8),
 * orchestrates player creation, map generation, and starts the game screen.
 *
 * @author  Jack Schulte & AI Assistant
 * @version 1.0.6
 * @date    2025-04-14
 *
 * @copyright Copyright (c) 2025 Jack Schulte. All rights reserved.
 * Strictly confidential and proprietary. Distribution, reproduction,
 * or modification is strictly prohibited without prior written permission.
 *
 * @version History:
 * - 1.0.6 (2025-04-14): Refactored Status and Dialogue panels into separate classes (StatusPanel, DialoguePanel). Moved panels to the right side. Updated navigation text with arrow symbols. Verified initial HP/Energy update. (AI Assistant)
 * - 1.0.5 (2025-04-14): Corrected updateDescription to use public static getters from RPG for class stats. (AI Assistant)
 * - 1.0.4 (2025-04-14): Added Status and Dialogue panels. Implemented status updates (Score, HP/Energy bars). Added dialogue area. Adjusted layout. Fixed arrow key display. Removed old subtitle HP display. (AI Assistant)
 * - 1.0.3 (2025-04-14): Removed main method (moved to RPG.java). Made setupUI, titleScreen public static. Made frame public static. (AI Assistant)
 * - 1.0.2 (2025-04-14): Refactored initialization: Removed SwingWorker, create/build RPG objects after character creation. Fixed movement calls. (AI Assistant)
 * - 1.0.1 (2025-04-14): Added F8 key binding to toggle map visibility via RPG.toggleMapVisibility() and mapPanel.refreshMap(). Updated comments. (J. Schulte)
 * - 1.0.0 (2025-04-01): Initial version with screen setup, character creation flow, and basic movement buttons/key bindings. (J. Schulte)
 */

 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 // Removed progress bar UI import, now handled in StatusPanel
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;


 public class UIManagement {

     // UI Components
     public static JFrame frame = new JFrame();
     private static JPanel topPanel = new JPanel(new BorderLayout(10, 0));
     private static JLabel playerInfoLabel = new JLabel("Player 1", SwingConstants.LEFT);
     private static JLabel titleLabel = new JLabel("Dungeon Exploration", SwingConstants.CENTER);
     private static JPanel centerPanelContainer = new JPanel(new BorderLayout()); // Main container for map and side panels
     private static JPanel rightPanel = new JPanel(new BorderLayout()); // Container for status and dialogue (MOVED & RENAMED)
     private static StatusPanel statusPanel; // Now an instance of StatusPanel
     private static DialoguePanel dialoguePanel; // Now an instance of DialoguePanel
     private static JPanel centerPanel = new JPanel(new CardLayout()); // Holds map or description
     private static JTextArea descArea = new JTextArea();
     private static JScrollPane descScrollPane;
     private static MapPanel mapPanel; // Map display panel
     private static JPanel bottomPanel = new JPanel(new BorderLayout());
     private static JLabel commentLabel = new JLabel("", SwingConstants.CENTER);
     private static JPanel buttonPanel = new JPanel();
     private static GridBagConstraints gbc = new GridBagConstraints();

     // --- Removed Status Panel Components (now in StatusPanel.java) ---
     // --- Removed Dialogue Panel Components (now in DialoguePanel.java) ---

     // Fonts and Colors
     private static Font titleFont = new Font("Trebuchet MS", Font.BOLD, 24);
     private static Font playerInfoFont = new Font("Trebuchet MS", Font.BOLD, 18);
     // private static Font statusFont = new Font("Trebuchet MS", Font.BOLD, 16); // Moved to StatusPanel
     // private static Font dialogueFont = new Font("Trebuchet MS", Font.PLAIN, 14); // Moved to DialoguePanel
     private static Font descFont = new Font("Trebuchet MS", Font.PLAIN, 18);
     private static Font commentFont = new Font("Trebuchet MS", Font.ITALIC, 18);
     private static Font buttonFont = new Font("Aharoni", Font.BOLD, 20);
     private static Font buttonFontHover = new Font("Aharoni", Font.BOLD, 22);
     public static Color backgroundColor = Color.decode("#EEEEEE"); // Made public for access by panels

     // Game State Object
     private static RPG rpg; // Holds the central map structure

     // Character Selection Specific
     private static String[] characterButtonLabels = {"Knight", "Sentinel", "Assassin", "Caveman"};
     private static String[] characterButtonColors = {"#48AD48", "#58D558"};
     private static String doneButtonLabel = "Done";
     private static String[] doneButtonColors = {"#EECC44", "#F7DD77"};
     private static int[] characterButtonLengths = new int[]{140, 140, 140, 140, 100};
     private static Map<String, JButton> characterButtonMap = new HashMap<>();
     private static String selectedClass = null;
     private static Color selectedColor = Color.decode("#58D58D");
     private static LineBorder selectedBorder = new LineBorder(Color.BLACK, 3);
     private static LineBorder defaultBorder = new LineBorder(Color.GRAY, 1);

     // Gameplay Specific
     private static String[] moveButtonLabels = {"Turn Left", "Forward", "Turn Right", "Backward"};
     private static String[] moveButtonColors = {"#44AADD", "#66CCFF"};
     private static boolean isGameScreenActive = false;

     // Unicode Arrow Characters
     private static final String LEFT_ARROW = "\u2190";
     private static final String UP_ARROW = "\u2191";
     private static final String RIGHT_ARROW = "\u2192";
     private static final String DOWN_ARROW = "\u2193";


     // Method to set up the main UI structure
     public static void setupUI() {
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(1200, 700);
         frame.setLayout(new BorderLayout());

         // Top Panel (Player Info | Title)
         playerInfoLabel.setFont(playerInfoFont);
         playerInfoLabel.setOpaque(true);
         playerInfoLabel.setBackground(backgroundColor);
         playerInfoLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
         titleLabel.setFont(titleFont);
         titleLabel.setOpaque(true);
         titleLabel.setForeground(Color.decode("#B6220E"));
         titleLabel.setBackground(backgroundColor);
         titleLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
         topPanel.add(playerInfoLabel, BorderLayout.WEST);
         topPanel.add(titleLabel, BorderLayout.CENTER);
         topPanel.setBackground(backgroundColor);

         // --- Center Area Panels ---

         // Instantiate New Panels
         statusPanel = new StatusPanel();
         dialoguePanel = new DialoguePanel();

         // Right Panel Container (Status + Dialogue) - MOVED & RENAMED
         rightPanel.setPreferredSize(new Dimension(250, 0)); // Give side panel a width
         rightPanel.add(statusPanel, BorderLayout.NORTH);
         rightPanel.add(dialoguePanel, BorderLayout.CENTER);
         rightPanel.setBackground(backgroundColor);

         // Center Panel (CardLayout: Description or Map)
         descArea.setWrapStyleWord(true);
         descArea.setLineWrap(true);
         descArea.setEditable(false);
         descArea.setFont(descFont);
         descArea.setBackground(backgroundColor);
         descScrollPane = new JScrollPane(descArea);
         descScrollPane.setBorder(null);
         descScrollPane.setBackground(backgroundColor);
         centerPanel.add(descScrollPane, "description"); // Map panel added later in init
         centerPanel.setBackground(backgroundColor);

         // Center Container (Map/Desc + Right Panel) - REORDERED
         centerPanelContainer.add(centerPanel, BorderLayout.CENTER); // Map/Desc in center
         centerPanelContainer.add(rightPanel, BorderLayout.EAST); // Status/Dialogue on right
         centerPanelContainer.setBackground(backgroundColor);

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
         bottomPanel.setBorder(new EmptyBorder(5, 0, 5, 0));

         // Final Frame Assembly
         frame.add(topPanel, BorderLayout.NORTH);
         frame.add(centerPanelContainer, BorderLayout.CENTER);
         frame.add(bottomPanel, BorderLayout.SOUTH);

         // Setup Key Bindings
         setupKeyBindings();
     }


     // ==============================================================================================
     // =================== Screen Management & Game Initialization ==================================

     // Switches the view in the center panel (Map or Description)
     private static void showScreen(String screenName) {
         if (screenName.equals("map") && mapPanel == null) {
              System.err.println("Attempted to switch to map screen, but mapPanel is null!");
              JOptionPane.showMessageDialog(frame, "Error: Map Panel not ready.", "Error", JOptionPane.ERROR_MESSAGE);
              return;
         }

         CardLayout cl = (CardLayout) (centerPanel.getLayout());
         cl.show(centerPanel, screenName);
         isGameScreenActive = screenName.equals("map");
         System.out.println("Switched to screen: " + screenName + ", isGameScreenActive: " + isGameScreenActive);

         // Manage visibility of side panels based on screen
         rightPanel.setVisible(isGameScreenActive); // Show status/dialogue only in game

         if (isGameScreenActive) {
             frame.getRootPane().requestFocusInWindow();
             // Updated comment label text with Unicode arrows
             commentLabel.setText("Use buttons or " + LEFT_ARROW + " " + UP_ARROW + " " + RIGHT_ARROW + " " + DOWN_ARROW + " keys. Press F8 to toggle map.");
         } else {
              commentLabel.setText(""); // Clear comment label on non-game screens
         }
     }

     // Initial title screen
     public static void titleScreen() {
         titleLabel.setText("Legend of Splenda: Bread of the Wild");
         playerInfoLabel.setText("");
         descArea.setText("Welcome, adventurer!");
         showScreen("description"); // Shows description, hides side panels

         String[] titleButtons = {"Start!", "#EECC44", "#F7DD77"};
         int[] titleButtonLengths = {120};
         makeButtons(titleButtons, titleButtonLengths, "title");
     }

     // Screen to choose number of players
     private static void numPlayersScreen() {
         titleLabel.setText("Number of Players");
         playerInfoLabel.setText("");
         descArea.setText("");
         showScreen("description");

         String[] numPlayersButtons = {"1 Player", "#48AD48", "#58D558", "2 Players", "#48AD48", "#58D558"};
         int[] numPlayersButtonLengths = {140, 140};
         makeButtons(numPlayersButtons, numPlayersButtonLengths, "numPlayers");
     }

     // Screen for character class selection
     private static void characterCreatorScreen() {
         selectedClass = null;
         characterButtonMap.clear();

         titleLabel.setText("Create your Character");
         playerInfoLabel.setText("Player " + (RPG.getPlayerIndex() + 1));
         descArea.setText("");
         showScreen("description");

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
         updateDescription(null);
     }


     // --- Central Game Initialization Logic ---
     private static void initializeAndStartGame() {
          System.out.println("All players created. Initializing game world...");

          // 1. Create the central RPG object
          rpg = new RPG();

          // 2. Build the map structure
          System.out.println("Building map structure...");
          rpg.buildMap();
          // rpg.printMap("Generated Map Structure"); // Optional debug

          // 3. Add events to the map structure
          System.out.println("Adding events to rooms...");
          rpg.addEventsToRooms();
          // rpg.printMap("Map With Events"); // Optional debug

          // 4. Set initial state for Player 0
          if (!RPG.getPlayers().isEmpty()) {
              RPG player0 = RPG.getPlayers().get(0);
              int startRow = rpg.map.length / 2;
              int startCol = rpg.map[0].length / 2;

              System.out.println("Setting initial state for Player 0 at ["+startRow+","+startCol+"]");
              player0.setCurrentPos(new int[]{startRow, startCol});
              player0.setWayFacing(0);
              player0.getVisitedMap()[startRow][startCol] = true;
              player0.exploreAround(startRow, startCol, rpg.map);

              // Clear dialogue from previous sessions/screens
              if (dialoguePanel != null) {
                 dialoguePanel.clearDialogue();
              }
              addDialogue("Welcome, " + player0.getClassName() + "!");
              addDialogue("You awaken in the " + RPG.getRoomDescription(rpg.map[startRow][startCol]) + ".");


              // Initial UI update for player 0 *AFTER* setting position and class stats
              // updateGameStatus handles statusPanel update internally
              updateGameStatus(player0);

              // player0.printBooleanMap("Player 0 Initial Explored", player0.getExploredMap()); // Optional debug
              // player0.printBooleanMap("Player 0 Initial Visited", player0.getVisitedMap()); // Optional debug

          } else {
              System.err.println("ERROR: No players found during game initialization!");
              JOptionPane.showMessageDialog(frame, "Error: Player data not found.", "Initialization Error", JOptionPane.ERROR_MESSAGE);
              return;
          }

          // 5. Create the MapPanel, passing the central RPG object
          System.out.println("Creating Map Panel...");
          mapPanel = new MapPanel(rpg);
          centerPanel.add(mapPanel, "map");

          // 6. Switch to the game screen
          System.out.println("Starting game screen...");
          startGameScreen();
          System.out.println("Game initialization complete.");
     }


     // Sets up the UI elements for the main game screen
     private static void startGameScreen() {
         if (mapPanel == null || rpg == null || RPG.getPlayers().isEmpty()) {
              System.err.println("Cannot start game screen - essential components missing!");
              JOptionPane.showMessageDialog(frame, "Error: Game components not ready.", "Error", JOptionPane.ERROR_MESSAGE);
              return;
         }
         // Update top panel for game screen
         titleLabel.setText("Dungeon Exploration");
         updatePlayerInfoLabel(RPG.getPlayers().get(0));

         // Update comment label text for gameplay with Unicode arrows
         commentLabel.setText("Use buttons or " + LEFT_ARROW + " " + UP_ARROW + " " + RIGHT_ARROW + " " + DOWN_ARROW + " keys. Press F8 to toggle map.");

         showScreen("map"); // Switch center panel view to the map, shows side panels
         mapPanel.refreshMap(); // Initial draw

         // Set up Gameplay Buttons
         ArrayList<String> buttonConfigList = new ArrayList<>();
         ArrayList<Integer> lengthConfigList = new ArrayList<>();
         int buttonLength = 110;
         for (String label : moveButtonLabels) {
             buttonConfigList.add(label); buttonConfigList.add(moveButtonColors[0]); buttonConfigList.add(moveButtonColors[1]);
             lengthConfigList.add(buttonLength);
         }

         makeButtons(buttonConfigList.toArray(new String[0]),
                     lengthConfigList.stream().mapToInt(i -> i).toArray(),
                     "gameplay");

         frame.getRootPane().requestFocusInWindow();
     }


     // ==============================================================================================
     // =================== Action Listeners =========================================================

     // Assigns appropriate action listeners based on the button label and screen context
     private static void pickActionListener(JButton button, String buttonLabel, String screenContext) {
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
                         RPG.createPlayers();
                         characterCreatorScreen();
                     });
                 }
                 break;

             case "characterCreator":
                  boolean isClassButton = false;
                  for(String classLbl : characterButtonLabels) { if(buttonLabel.equals(classLbl)) { isClassButton = true; break; } }

                 if (isClassButton) {
                     button.addActionListener(e -> {
                         selectedClass = buttonLabel;
                         updateDescription(selectedClass);
                         for (Map.Entry<String, JButton> entry : characterButtonMap.entrySet()) {
                             JButton btn = entry.getValue();
                             btn.setBackground(Color.decode(characterButtonColors[0]));
                             btn.setBorder(defaultBorder);
                             if (entry.getKey().equals(selectedClass)) {
                                 btn.setBackground(selectedColor);
                                 btn.setBorder(selectedBorder);
                             }
                         }
                         commentLabel.setText(buttonLabel + " selected!");
                     });
                 } else if (buttonLabel.equals(doneButtonLabel)) {
                     button.addActionListener(e -> {
                         if (selectedClass == null) {
                             commentLabel.setText("Please select a class first!");
                             descArea.setText("You must select a class before proceeding.");
                         } else {
                             RPG.assignClass(RPG.getPlayerIndex(), selectedClass);
                             RPG.modPlayerIndex();

                             if (RPG.getPlayerIndex() < RPG.getNumPlayers()) {
                                 characterCreatorScreen();
                             } else {
                                 initializeAndStartGame(); // All players done, start game
                             }
                         }
                     });
                 }
                 break;

             case "gameplay":
                 switch (buttonLabel) {
                     case "Turn Left":  button.addActionListener(e -> handleTurnLeft()); break;
                     case "Forward":    button.addActionListener(e -> handleMoveForward()); break;
                     case "Turn Right": button.addActionListener(e -> handleTurnRight()); break;
                     case "Backward":   button.addActionListener(e -> handleMoveBackward()); break;
                 }
                 break;
         }
     }

     // Helper to update top-left player info label
     private static void updatePlayerInfoLabel(RPG player) {
          if (player == null) {
               playerInfoLabel.setText("");
               return;
          }
          playerInfoLabel.setText("Player 1: " + player.getClassName());
     }

     // ==============================================================================================
     // =================== Gameplay Action Handlers (for Buttons and Keys) ==========================

     private static void handleTurnLeft() {
         if (!isGameScreenActive || mapPanel == null || RPG.getPlayers().isEmpty()) return;
         RPG player = RPG.getPlayers().get(0);
         player.turnPlayer(0); // Updates UI via updateGameStatus called internally
         commentLabel.setText("Turned left.");
         frame.getRootPane().requestFocusInWindow();
     }

     private static void handleTurnRight() {
         if (!isGameScreenActive || mapPanel == null || RPG.getPlayers().isEmpty()) return;
         RPG player = RPG.getPlayers().get(0);
         player.turnPlayer(1); // Updates UI via updateGameStatus called internally
         commentLabel.setText("Turned right.");
         frame.getRootPane().requestFocusInWindow();
     }

     private static void handleMoveForward() {
         if (!isGameScreenActive || mapPanel == null || rpg == null || RPG.getPlayers().isEmpty()) return;
         RPG player = RPG.getPlayers().get(0);
         if (player.movePlayer(player.getWayFacing(), rpg.map)) { // Move logic in RPG, updates UI & dialogue
             commentLabel.setText("Moved forward.");
         } else {
             commentLabel.setText("Cannot move forward!"); // Dialogue handled in movePlayer
         }
         frame.getRootPane().requestFocusInWindow();
     }

     private static void handleMoveBackward() {
         if (!isGameScreenActive || mapPanel == null || rpg == null || RPG.getPlayers().isEmpty()) return;
         RPG player = RPG.getPlayers().get(0);
         int oppositeDirection = (player.getWayFacing() + 2) % 4;
         if (player.movePlayer(oppositeDirection, rpg.map)) { // Move logic in RPG, updates UI & dialogue
             commentLabel.setText("Moved backward.");
         } else {
             commentLabel.setText("Cannot move backward!"); // Dialogue handled in movePlayer
         }
         frame.getRootPane().requestFocusInWindow();
     }

     private static void handleToggleMap() {
          if (!isGameScreenActive || mapPanel == null) return;
          RPG.toggleMapVisibility();
          if (!RPG.getPlayers().isEmpty()) {
              updateGameStatus(RPG.getPlayers().get(0)); // Refreshes map via status update
          }
          commentLabel.setText("Map view toggled (Show full: " + RPG.isShowFullMap() + ")");
          frame.getRootPane().requestFocusInWindow();
     }


     // ==============================================================================================
     // =================== Key Binding Setup ========================================================

     private static void setupKeyBindings() {
         InputMap inputMap = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
         ActionMap actionMap = frame.getRootPane().getActionMap();

         // Use correct KeyEvents for arrows
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
    // =================== UI Update Methods ========================================================

    /**
     * Central method to update game-related UI elements based on player state.
     * This includes the status panel, map panel, and player info label.
     * Ensures updates happen on the Event Dispatch Thread.
     * @param player The current player whose state should be reflected.
     */
    public static void updateGameStatus(RPG player) {
         // Ensure UI updates occur on the Event Dispatch Thread (EDT)
         SwingUtilities.invokeLater(() -> {
             if (player == null || !isGameScreenActive) return;

             // Update Status Panel (uses the StatusPanel instance's method)
             if (statusPanel != null) {
                 statusPanel.updateStatus(player);
             }

             // Update Map Panel
             if (mapPanel != null) {
                 mapPanel.refreshMap();
             }

             // Update Player Info Label (Top Left)
             updatePlayerInfoLabel(player);
         });
    }


    // --- Removed updateStatusPanel method (now in StatusPanel.java) ---


    /**
     * Adds a message to the dialogue panel.
     * Delegates the call to the DialoguePanel instance.
     * @param text The message to add.
     */
    public static void addDialogue(String text) {
        if (dialoguePanel != null) {
            dialoguePanel.addDialogue(text); // Delegate to DialoguePanel instance
        } else {
             System.err.println("Attempted to add dialogue, but dialoguePanel is null!");
        }
    }


     // ==============================================================================================
     // =================== UI Helpers (Button Creation, Hover Effects) ==============================

     // Creates buttons and adds them to the button panel
     private static void makeButtons(String[] stringArray, int[] intArray, String screenContext) {
         buttonPanel.removeAll();

         if (screenContext.equals("characterCreator")) {
             characterButtonMap.clear();
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

             addHoverEffect(button, intArray[i], normalColor, hoverColor);
             pickActionListener(button, buttonLabel, screenContext);

             gbc.gridx = i;
             buttonPanel.add(button, gbc);

             if (screenContext.equals("characterCreator")) {
                  for(String classLbl : characterButtonLabels) { if(buttonLabel.equals(classLbl)) { characterButtonMap.put(buttonLabel, button); break; } }
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
             Dimension hoverSize = new Dimension((int) (length * 1.05), 55);

             @Override
             public void mouseEntered(MouseEvent e) {
                 boolean isSelectedCharButton = selectedClass != null && button.getText().equals(selectedClass) && characterButtonMap.containsKey(selectedClass);
                 if (!isSelectedCharButton) {
                     button.setPreferredSize(hoverSize);
                     button.setBackground(hover);
                     button.setFont(buttonFontHover);
                 }
                  if (characterButtonMap.containsKey(button.getText()) || button.getText().equals(doneButtonLabel)) {
                       updateDescription(button.getText());
                  }
             }

             @Override
             public void mouseExited(MouseEvent e) {
                  boolean isSelectedCharButton = selectedClass != null && button.getText().equals(selectedClass) && characterButtonMap.containsKey(selectedClass);
                  if (!isSelectedCharButton) {
                     button.setPreferredSize(defaultSize);
                     button.setBackground(normal);
                     button.setFont(buttonFont);
                  }
                  if (characterButtonMap.containsKey(button.getText()) || button.getText().equals(doneButtonLabel)) {
                       updateDescription(selectedClass);
                  }
             }
         });
     }

     // Updates description text area based on button hover/selection (mainly for char creation)
     private static void updateDescription(String label) {
         if (isGameScreenActive) return;

         String descText = "Hover over a class to see details or click to select.";
         if (label != null) {
             switch (label) {
                 case "Knight":
                     descText = "Knight:\n\nA balanced warrior clad in sturdy armor. Excels in melee combat with reliable strength and defense.\n\nHP: " + RPG.getKnightMaxHp() + "\nEnergy: " + RPG.getKnightMaxEnergy();
                     break;
                 case "Sentinel":
                    descText = "Sentinel:\n\nA bastion of defense, able to withstand tremendous punishment. Slower, but incredibly resilient.\n\nHP: " + RPG.getSentinelMaxHp() + "\nEnergy: " + RPG.getSentinelMaxEnergy();
                    break;
                 case "Assassin":
                    descText = "Assassin:\n\nA master of shadows and quick strikes. Relies on agility and critical hits, but is more fragile.\n\nHP: " + RPG.getAssassinMaxHp() + "\nEnergy: " + RPG.getAssassinMaxEnergy();
                    break;
                 case "Caveman":
                    descText = "Caveman:\n\nUnga bunga? Surprisingly average stats. Wields a big club. What more do you need?\n\nHP: " + RPG.getCavemanMaxHp() + "\nEnergy: " + RPG.getCavemanMaxEnergy();
                    break;
                 case "Done":
                     if (selectedClass != null) {
                        descText = "Finalize selection: " + selectedClass + ".\nClick 'Done' to confirm and proceed.";
                     } else {
                        descText = "Please select a class before clicking 'Done'.";
                     }
                     break;
             }
         }
         descArea.setText(descText);
         descArea.setCaretPosition(0); // Scroll to top
     }
 }