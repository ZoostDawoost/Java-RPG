/**
 * @file    UIManagement.java
 * @brief   Manages the main game window (JFrame), UI panels (using CardLayout), button actions, key bindings, and screen transitions. Handles dynamic character image loading and display during class selection. Initializes game state after character creation. Instantiates StatusPanel and DialoguePanel.
 *
 * Sets up the overall graphical user interface, handles user input via buttons and keyboard (arrow keys, F8),
 * orchestrates player creation (including selecting and storing character images), map generation, and starts the game screen.
 *
 * @author  Jack Schulte & AI Assistant
 * @version 1.0.7
 * @date    2025-04-14
 *
 * @copyright Copyright (c) 2025 Jack Schulte & AI Assistant. All rights reserved.
 * Strictly confidential and proprietary. Distribution, reproduction,
 * or modification is strictly prohibited without prior written permission.
 *
 * @version History:
 * - 1.0.7 (2025-04-14): Implemented dynamic character image loading based on selected set/class. Added logic to find, randomly select, display, scale, and store the chosen ImageIcon during character creation. Replaced static image with dynamic JLabel updates. (AI Assistant)
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
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random; // Needed for random image selection


public class UIManagement {

    // UI Components
    public static JFrame frame = new JFrame();
    private static JPanel topPanel = new JPanel(new BorderLayout(10, 0));
    private static JLabel playerInfoLabel = new JLabel("", SwingConstants.LEFT);
    private static JLabel titleLabel = new JLabel("", SwingConstants.CENTER);
    private static JPanel centerPanelContainer = new JPanel(new BorderLayout());
    private static JPanel rightPanel = new JPanel(new BorderLayout());
    private static StatusPanel statusPanel;
    private static DialoguePanel dialoguePanel;
    private static JPanel centerPanel = new JPanel(new CardLayout());
    private static WelcomePanel welcomePanel;
    private static JPanel characterDescPanel = new JPanel(new BorderLayout(10, 0));
    private static JLabel characterImageLabel = new JLabel(); // Will display character class image
    private static JEditorPane classDetailsPane = new JEditorPane();
    private static MapPanel mapPanel;
    private static JPanel bottomPanel = new JPanel(new BorderLayout());
    private static JLabel commentLabel = new JLabel("", SwingConstants.CENTER);
    private static JPanel buttonPanel = new JPanel();
    private static GridBagConstraints gbc = new GridBagConstraints();


    // Fonts and Colors
    private static Font titleFont = new Font("Trebuchet MS", Font.BOLD, 24);
    private static Font playerInfoFont = new Font("Trebuchet MS", Font.BOLD, 18);
    private static Font commentFont = new Font("Trebuchet MS", Font.ITALIC, 18);
    private static Font buttonFont = new Font("Aharoni", Font.BOLD, 20);
    public static Color backgroundColor = Color.decode("#EEEEEE");
    private static Color characterDetailBackgroundColor = Color.decode("#F5F5DC");
    private static Color buttonDefaultForegroundColor = Color.WHITE;
    private static Color buttonHoverForegroundColor = Color.RED;
    private static Color buttonSelectedBackgroundColor = Color.BLACK;
    private static Color buttonSelectedForegroundColor = Color.WHITE;


    // Game State Object
    private static RPG rpg; // Central map object

    // Character Selection Specific
    private static String[] characterButtonLabels = {"Knight", "Sentinel", "Assassin", "Wizard", "Caveman"};
    private static String[] characterButtonColors = {"#48AD48", "#58D558"}; // Normal, Hover(unused for class buttons)
    private static String doneButtonLabel = "Done";
    private static String[] doneButtonColors = {"#EECC44", "#F7DD77"}; // Normal, Hover BG
    private static int[] characterButtonLengths = new int[]{140, 140, 140, 140, 140, 100}; // Widths for buttons
    private static Map<String, JButton> characterButtonMap = new HashMap<>();
    private static String selectedClass = null; // The currently confirmed selected class
    // --- NEW ---
    private static Map<String, ImageIcon> currentIconForClass = new HashMap<>(); // Stores the randomly chosen icon for each class *during this screen session*
    private static String lastDisplayedClass = null; // Tracks which class's image/details are currently shown (handles hover vs selection)
    private static Random random = new Random(); // For random image selection


    private static LineBorder defaultBorder = new LineBorder(Color.GRAY, 1);


    // Gameplay Specific
    private static String[] moveButtonLabels = {"Turn Left", "Forward", "Turn Right", "Backward"};
    private static String[] moveButtonColors = {"#44AADD", "#66CCFF"};
    private static boolean isGameScreenActive = false;
    private static final String LEFT_ARROW = "\u2190";
    private static final String UP_ARROW = "\u2191";
    private static final String RIGHT_ARROW = "\u2192";
    private static final String DOWN_ARROW = "\u2193";
    private static final int CHAR_IMAGE_TARGET_WIDTH = 350; // Target width for scaling


    // Method to set up the main UI structure (Mostly unchanged)
    public static void setupUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Legend of Splenda: Bread of the Wild");
        frame.setSize(1200, 800);
        frame.setLayout(new BorderLayout());

        // Top Panel (Unchanged)
        playerInfoLabel.setFont(playerInfoFont); playerInfoLabel.setOpaque(true); playerInfoLabel.setBackground(backgroundColor); playerInfoLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        titleLabel.setFont(titleFont); titleLabel.setOpaque(true); titleLabel.setForeground(Color.decode("#B6220E")); titleLabel.setBackground(backgroundColor); titleLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        topPanel.add(playerInfoLabel, BorderLayout.WEST); topPanel.add(titleLabel, BorderLayout.CENTER); topPanel.setBackground(backgroundColor);
        topPanel.setVisible(false);

        // Right Side Panels (Unchanged)
        statusPanel = new StatusPanel(); dialoguePanel = new DialoguePanel();
        rightPanel.setPreferredSize(new Dimension(250, 0)); rightPanel.add(statusPanel, BorderLayout.NORTH); rightPanel.add(dialoguePanel, BorderLayout.CENTER); rightPanel.setBackground(backgroundColor);
        rightPanel.setVisible(false);

        // Center Panel (CardLayout: Welcome, Character Creator, Map)
        welcomePanel = new WelcomePanel(); centerPanel.add(welcomePanel, "welcome");

        // --- Character Creator Panel Setup ---
        characterImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        characterImageLabel.setVerticalAlignment(SwingConstants.CENTER);
        characterImageLabel.setBackground(backgroundColor);
        characterImageLabel.setOpaque(true);
        characterImageLabel.setPreferredSize(new Dimension(CHAR_IMAGE_TARGET_WIDTH + 20, 400)); // Give it some preferred size

        classDetailsPane.setEditable(false);
        classDetailsPane.setContentType("text/html");
        classDetailsPane.setBackground(characterDetailBackgroundColor);
        classDetailsPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        HTMLEditorKit kit = new HTMLEditorKit();
        classDetailsPane.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body {font-family:\"Trebuchet MS\"; font-size: 11pt; margin: 5px;}");
        styleSheet.addRule("h3 { margin-top: 10px; margin-bottom: 5px; text-align: center;}");
        styleSheet.addRule("table { border-collapse: collapse; margin: 10px auto; width: 90%; }");
        styleSheet.addRule("th, td { border: 1px solid #cccccc; padding: 5px; text-align: left; }");
        styleSheet.addRule("th { background-color: #e0e0e0; text-align: center; }");

        JScrollPane detailsScrollPane = new JScrollPane(classDetailsPane);
        detailsScrollPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        detailsScrollPane.setPreferredSize(new Dimension(320, 0)); // Let height be flexible

        characterDescPanel.setBackground(backgroundColor);
        characterDescPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        characterDescPanel.add(characterImageLabel, BorderLayout.CENTER);
        characterDescPanel.add(detailsScrollPane, BorderLayout.EAST);

        centerPanel.add(characterDescPanel, "characterCreator");
        // --- End Character Creator Panel Setup ---

        centerPanel.setBackground(backgroundColor);

        // Center Container (Unchanged)
        centerPanelContainer.add(centerPanel, BorderLayout.CENTER);
        centerPanelContainer.add(rightPanel, BorderLayout.EAST);
        centerPanelContainer.setBackground(backgroundColor);

        // Bottom Panel (Unchanged structure)
        commentLabel.setFont(commentFont); commentLabel.setBackground(backgroundColor); commentLabel.setOpaque(true);
        buttonPanel.setBackground(backgroundColor); buttonPanel.setLayout(new GridBagLayout());
        gbc.insets = new Insets(5, 10, 5, 10); gbc.gridy = 0;
        bottomPanel.setBackground(backgroundColor); bottomPanel.add(commentLabel, BorderLayout.NORTH); bottomPanel.add(buttonPanel, BorderLayout.CENTER); bottomPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Final Frame Assembly (Unchanged)
        frame.add(topPanel, BorderLayout.NORTH); frame.add(centerPanelContainer, BorderLayout.CENTER); frame.add(bottomPanel, BorderLayout.SOUTH);

        // Setup Key Bindings (Unchanged)
        setupKeyBindings();

        // Show Welcome Screen initially (Unchanged)
        showScreen("welcome");
    }


    // ==============================================================================================
    // =================== Screen Management & Game Initialization ==================================

    // Switches the view in the center panel (Minor update for character screen)
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

        topPanel.setVisible(!screenName.equals("welcome"));
        rightPanel.setVisible(isGameScreenActive);
        bottomPanel.setVisible(!screenName.equals("welcome"));

        if (isGameScreenActive) {
            commentLabel.setText("Use buttons or " + LEFT_ARROW + " " + UP_ARROW + " " + RIGHT_ARROW + " " + DOWN_ARROW + " keys. Press F8 to toggle map.");
            frame.getRootPane().requestFocusInWindow();
        } else if (screenName.equals("characterCreator")){
            // Initial state for character screen
            commentLabel.setText("Hover over or click a class button below.");
            lastDisplayedClass = null; // Reset display tracker
            currentIconForClass.clear(); // Clear previously cached icons for this session
            updateCharacterImageAndDetails(null); // Show initial placeholder/prompt
        } else {
             commentLabel.setText("");
        }

        if(screenName.equals("welcome")) {
            buttonPanel.removeAll(); buttonPanel.revalidate(); buttonPanel.repaint();
        }
    }

    // Initial welcome screen setup (Unchanged)
    public static void welcomeScreen() { titleLabel.setText(""); playerInfoLabel.setText(""); showScreen("welcome"); }

    // Screen for character class selection (REVISED - Removed static image load)
    public static void characterCreatorScreen() {
        selectedClass = null; // Reset confirmed selection
        // Reset display trackers handled in showScreen("characterCreator")
        characterButtonMap.clear(); // Clear button references

        titleLabel.setText("Create your Character");
        playerInfoLabel.setText("Player " + (RPG.getPlayerIndex() + 1));
        topPanel.setVisible(true);

        // NO static image loading here - handled by updateCharacterImageAndDetails

        // --- Create Buttons ---
        ArrayList<String> buttonConfigList = new ArrayList<>(); ArrayList<Integer> lengthConfigList = new ArrayList<>();
        for (int i = 0; i < characterButtonLabels.length; i++) {
            String label = characterButtonLabels[i];
            buttonConfigList.add(label); buttonConfigList.add(characterButtonColors[0]); buttonConfigList.add(characterButtonColors[0]); // Normal BG, hover BG (unused for class buttons)
            lengthConfigList.add(characterButtonLengths[i]);
        }
        // Done button config
        buttonConfigList.add(doneButtonLabel); buttonConfigList.add(doneButtonColors[0]); buttonConfigList.add(doneButtonColors[1]); // Normal BG, Hover BG
        lengthConfigList.add(characterButtonLengths[characterButtonLengths.length - 1]);

        makeButtons(buttonConfigList.toArray(new String[0]), lengthConfigList.stream().mapToInt(i -> i).toArray(), "characterCreator");
        // --- End Create Buttons ---

        showScreen("characterCreator"); // This will call updateCharacterImageAndDetails(null)
        bottomPanel.setVisible(true);
    }


    // --- Central Game Initialization Logic --- (Unchanged)
    private static void initializeAndStartGame() {
        System.out.println("All players created. Initializing game world...");
        // Create the central map object
        rpg = new RPG();
        rpg.buildMap();
        rpg.addEventsToRooms();

        if (!RPG.getPlayers().isEmpty()) {
            RPG player0 = RPG.getPlayers().get(0);
            int startRow = rpg.map.length / 2;
            int startCol = rpg.map[0].length / 2;
            System.out.println("Setting initial state for Player 0 at ["+startRow+","+startCol+"]");
            player0.setCurrentPos(new int[]{startRow, startCol});
            player0.setWayFacing(0); // Start facing North
            player0.getVisitedMap()[startRow][startCol] = true; // Mark start as visited
            player0.exploreAround(startRow, startCol, rpg.map); // Explore around start

            // Clear previous dialogue and add welcome messages
            if (dialoguePanel != null) { dialoguePanel.clearDialogue(); }
            addDialogue("Welcome, " + player0.getClassName() + "!");
            addDialogue("You awaken in the " + RPG.getRoomDescription(rpg.map[startRow][startCol]) + ".");
            // Display initial status
            updateGameStatus(player0);
        } else {
            System.err.println("ERROR: No players found!");
            JOptionPane.showMessageDialog(frame, "Error: Player data not found.", "Initialization Error", JOptionPane.ERROR_MESSAGE);
            welcomeScreen(); // Go back to welcome if error
            return;
        }

        // Create the map panel AFTER the central map exists
        System.out.println("Creating Map Panel...");
        mapPanel = new MapPanel(rpg); // Pass the central map object
        centerPanel.add(mapPanel, "map"); // Add map panel to card layout

        System.out.println("Starting game screen...");
        startGameScreen(); // Switch UI to game screen
        System.out.println("Game initialization complete.");
    }

    // Sets up the UI elements for the main game screen (Unchanged)
    private static void startGameScreen() {
        if (mapPanel == null || rpg == null || RPG.getPlayers().isEmpty()) {
            System.err.println("Cannot start game screen - essential components missing!");
            JOptionPane.showMessageDialog(frame, "Error: Game components not ready.", "Error", JOptionPane.ERROR_MESSAGE);
            welcomeScreen(); // Go back if error
            return;
        }
        RPG player = RPG.getPlayers().get(0); // Assuming player 0's view initially
        titleLabel.setText("Dungeon Exploration");
        updatePlayerInfoLabel(player);
        commentLabel.setText("Use buttons or " + LEFT_ARROW + " " + UP_ARROW + " " + RIGHT_ARROW + " " + DOWN_ARROW + " keys. Press F8 to toggle map.");

        // Create gameplay buttons
        ArrayList<String> buttonConfigList = new ArrayList<>(); ArrayList<Integer> lengthConfigList = new ArrayList<>();
        int buttonLength = 110; // Width for gameplay buttons
        for (String label : moveButtonLabels) {
            buttonConfigList.add(label);
            buttonConfigList.add(moveButtonColors[0]); // Normal BG
            buttonConfigList.add(moveButtonColors[1]); // Hover BG
            lengthConfigList.add(buttonLength);
        }
        makeButtons(buttonConfigList.toArray(new String[0]), lengthConfigList.stream().mapToInt(i -> i).toArray(), "gameplay");

        showScreen("map"); // Switch central panel to map view
        mapPanel.refreshMap(); // Initial map draw
        bottomPanel.setVisible(true); // Ensure bottom panel (buttons/comment) is visible
        frame.getRootPane().requestFocusInWindow(); // Request focus for key bindings
    }


    // ==============================================================================================
    // =================== Action Listeners =========================================================

    // Assigns appropriate action listeners based on the button label and screen context
    private static void pickActionListener(JButton button, String buttonLabel, String screenContext) {
        // Remove previous listeners to avoid duplicates
        for (java.awt.event.ActionListener al : button.getActionListeners()) {
            button.removeActionListener(al);
        }

        switch (screenContext) {
            case "characterCreator":
                boolean isClassButton = characterButtonMap.containsKey(buttonLabel);
                if (isClassButton) {
                    // Action for Clicking a Class Button
                    button.addActionListener(e -> {
                        selectedClass = buttonLabel; // Set the *confirmed* selection
                        lastDisplayedClass = buttonLabel; // Sync display tracker
                        commentLabel.setText(buttonLabel + " selected. Click 'Done' to confirm.");

                        // Update button appearances (highlight selected)
                        for (Map.Entry<String, JButton> entry : characterButtonMap.entrySet()) {
                            JButton btn = entry.getValue();
                            String btnLabel = entry.getKey();
                            boolean isCurrentlySelected = btnLabel.equals(selectedClass);
                            btn.setBackground(isCurrentlySelected ? buttonSelectedBackgroundColor : Color.decode(characterButtonColors[0]));
                            btn.setForeground(isCurrentlySelected ? buttonSelectedForegroundColor : buttonDefaultForegroundColor);
                            btn.setBorder(defaultBorder); // Keep border consistent
                        }
                        // Update image and details (will use the icon already loaded/cached by hover)
                        updateCharacterImageAndDetails(selectedClass);
                    });
                } else if (buttonLabel.equals(doneButtonLabel)) {
                    // Action for Clicking the Done Button
                    button.addActionListener(e -> {
                        if (selectedClass == null) {
                            commentLabel.setText("Please select a class first!");
                            updateCharacterImageAndDetails(null); // Show prompt again
                        } else {
                            int currentPlayerIndex = RPG.getPlayerIndex();
                            RPG.assignClass(currentPlayerIndex, selectedClass); // Assign stats

                            // --- Store the chosen Icon ---
                            ImageIcon finalIcon = currentIconForClass.get(selectedClass);
                            if (finalIcon != null && currentPlayerIndex < RPG.getPlayers().size()) {
                                RPG.getPlayers().get(currentPlayerIndex).setSelectedCharacterIcon(finalIcon);
                                System.out.println("Stored icon for Player " + (currentPlayerIndex + 1) + " (" + selectedClass + ")");
                            } else {
                                System.err.println("Warning: Could not find cached icon for " + selectedClass + " or player index out of bounds.");
                            }
                            // --- End Icon Storing ---

                            RPG.modPlayerIndex(); // Move to next player index

                            // Check if more players need to select or if game starts
                            if (RPG.getPlayerIndex() < RPG.getNumPlayers()) {
                                characterCreatorScreen(); // Load screen for the next player
                            } else {
                                initializeAndStartGame(); // All players done, start game
                            }
                        }
                    });
                }
                break;
            case "gameplay": // Unchanged Gameplay Actions
                switch (buttonLabel) {
                    case "Turn Left":  button.addActionListener(e -> handleTurnLeft()); break;
                    case "Forward":    button.addActionListener(e -> handleMoveForward()); break;
                    case "Turn Right": button.addActionListener(e -> handleTurnRight()); break;
                    case "Backward":   button.addActionListener(e -> handleMoveBackward()); break;
                }
                break;
        }
    }

    // Helper to update top-left player info label (Unchanged)
    private static void updatePlayerInfoLabel(RPG player) {
        if (player == null || !isGameScreenActive) {
            playerInfoLabel.setText("");
            return;
        }
        // You might want to display player number later if multiplayer view is added
        playerInfoLabel.setText("Player 1: " + player.getClassName());
    }

    // ==============================================================================================
    // =================== Gameplay Action Handlers (for Buttons and Keys) ==========================
    // (handleTurnLeft, handleTurnRight, handleMoveForward, handleMoveBackward, handleToggleMap remain the same)
    private static void handleTurnLeft() { if (!isGameScreenActive || mapPanel == null || RPG.getPlayers().isEmpty()) return; RPG player = RPG.getPlayers().get(0); player.turnPlayer(0); updateGameStatus(player); frame.getRootPane().requestFocusInWindow(); }
    private static void handleTurnRight() { if (!isGameScreenActive || mapPanel == null || RPG.getPlayers().isEmpty()) return; RPG player = RPG.getPlayers().get(0); player.turnPlayer(1); updateGameStatus(player); frame.getRootPane().requestFocusInWindow(); }
    private static void handleMoveForward() { if (!isGameScreenActive || mapPanel == null || rpg == null || RPG.getPlayers().isEmpty()) return; RPG player = RPG.getPlayers().get(0); if (player.movePlayer(player.getWayFacing(), rpg.map)) {} else { /* Message handled in movePlayer */ } frame.getRootPane().requestFocusInWindow(); }
    private static void handleMoveBackward() { if (!isGameScreenActive || mapPanel == null || rpg == null || RPG.getPlayers().isEmpty()) return; RPG player = RPG.getPlayers().get(0); int oppositeDirection = (player.getWayFacing() + 2) % 4; if (player.movePlayer(oppositeDirection, rpg.map)) {} else { /* Message handled in movePlayer */ } frame.getRootPane().requestFocusInWindow(); }
    private static void handleToggleMap() { if (!isGameScreenActive || mapPanel == null) return; RPG.toggleMapVisibility(); if (!RPG.getPlayers().isEmpty()) { updateGameStatus(RPG.getPlayers().get(0)); } commentLabel.setText("Map view toggled (Show full: " + RPG.isShowFullMap() + ")"); frame.getRootPane().requestFocusInWindow(); }


    // ==============================================================================================
    // =================== Key Binding Setup ========================================================
    private static void setupKeyBindings() { // (Unchanged)
        InputMap inputMap = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW); ActionMap actionMap = frame.getRootPane().getActionMap();
        KeyStroke upKey = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0); KeyStroke downKey = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0); KeyStroke leftKey = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0); KeyStroke rightKey = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0); KeyStroke f8Key = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
        inputMap.put(upKey, "moveForward"); inputMap.put(downKey, "moveBackward"); inputMap.put(leftKey, "turnLeft"); inputMap.put(rightKey, "turnRight"); inputMap.put(f8Key, "toggleMap");
        actionMap.put("moveForward", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { if(isGameScreenActive) handleMoveForward(); } });
        actionMap.put("moveBackward", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { if(isGameScreenActive) handleMoveBackward(); } });
        actionMap.put("turnLeft", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { if(isGameScreenActive) handleTurnLeft(); } });
        actionMap.put("turnRight", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { if(isGameScreenActive) handleTurnRight(); } });
        actionMap.put("toggleMap", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { if(isGameScreenActive) handleToggleMap(); } });
    }


   // ==============================================================================================
   // =================== UI Update Methods ========================================================
   public static void updateGameStatus(RPG player) { SwingUtilities.invokeLater(() -> { if (player == null || !isGameScreenActive) return; if (statusPanel != null) { statusPanel.updateStatus(player); } if (mapPanel != null) { mapPanel.refreshMap(); } updatePlayerInfoLabel(player); }); }
   public static void addDialogue(String text) { if (dialoguePanel != null) { dialoguePanel.addDialogue(text); } else { System.err.println("Attempted to add dialogue, but dialoguePanel is null!"); } }


    // ==============================================================================================
    // =================== UI Helpers (Button Creation, Hover Effects) ==============================

    // Creates buttons and adds them to the bottom button panel (Minor updates for character screen)
    private static void makeButtons(String[] stringArray, int[] intArray, String screenContext) {
        buttonPanel.removeAll();
        if (screenContext.equals("characterCreator")) characterButtonMap.clear(); // Clear map before adding new buttons
        gbc.gridx = 0; // Reset gridx for new buttons

        for (int i = 0; i < stringArray.length / 3; i++) {
            String buttonLabel = stringArray[i * 3];
            JButton button = new JButton(buttonLabel);
            button.setPreferredSize(new Dimension(intArray[i], 50));

            String normalColorHex = stringArray[i * 3 + 1];
            String hoverEffectInfo = stringArray[i * 3 + 2]; // For Done button BG, unused for class buttons

            button.setBackground(Color.decode(normalColorHex));
            button.setFont(buttonFont);
            button.setBorder(defaultBorder);
            button.setFocusPainted(false);

            if (screenContext.equals("characterCreator")) {
                 button.setForeground(buttonDefaultForegroundColor); // Default white text
                 // Check if this button corresponds to the currently selected class
                 if (buttonLabel.equals(selectedClass)) {
                     button.setBackground(buttonSelectedBackgroundColor); // Selected BG
                     button.setForeground(buttonSelectedForegroundColor); // Selected FG
                 }
                 // Store class buttons in the map
                 boolean isClassBtn = false;
                 for(String lbl : characterButtonLabels) { if(lbl.equals(buttonLabel)) { isClassBtn = true; break; } }
                 if(isClassBtn) characterButtonMap.put(buttonLabel, button);
            } else { // Gameplay buttons
                 button.setForeground(Color.BLACK); // Gameplay buttons have black text
            }

            // Add hover effect and action listener
            addHoverEffect(button, normalColorHex, hoverEffectInfo, screenContext);
            pickActionListener(button, buttonLabel, screenContext);

            buttonPanel.add(button, gbc); // Add button to panel
            gbc.gridx++; // Move to next grid position
        }
        buttonPanel.revalidate(); // Update layout
        buttonPanel.repaint(); // Redraw panel
    }


    // Adds mouse hover effects to a button (REVISED for new image logic)
    private static void addHoverEffect(JButton button, String normalBgColorHex, String hoverInfo, String screenContext) {
        button.addMouseListener(new MouseAdapter() {
            Color normalBg = Color.decode(normalBgColorHex);
            Color defaultFg = buttonDefaultForegroundColor; // Store default foreground for char creation buttons

            @Override
            public void mouseEntered(MouseEvent e) {
                String buttonLabel = button.getText();
                boolean isClassBtn = characterButtonMap.containsKey(buttonLabel);
                boolean isDoneBtn = buttonLabel.equals(doneButtonLabel);

                // Apply visual hover effect (foreground/background changes)
                if (!buttonLabel.equals(selectedClass)) { // Don't change appearance if it's the selected one
                    if (screenContext.equals("characterCreator")) {
                        if (isClassBtn) {
                            button.setForeground(buttonHoverForegroundColor); // Red text for hover
                        } else if (isDoneBtn) {
                             button.setBackground(Color.decode(hoverInfo)); // Done button hover BG
                        }
                    } else { // Gameplay screen
                        button.setBackground(Color.decode(hoverInfo)); // Gameplay hover BG change
                    }
                }

                // Update details/image panel based on hovered button
                if (screenContext.equals("characterCreator")) {
                     if (isClassBtn) {
                         updateCharacterImageAndDetails(buttonLabel); // Show details & image for this class
                         // Update comment based on selection state
                         commentLabel.setText(selectedClass == null ? "Hover over or click a class button below." : selectedClass.equals(buttonLabel) ? buttonLabel + " selected. Click 'Done' to confirm." : "Hovering " + buttonLabel + ". Selected: " + selectedClass);
                     } else if (isDoneBtn) {
                         // When hovering Done, show details for the *selected* class (if any)
                         updateCharacterImageAndDetails(selectedClass);
                         commentLabel.setText(selectedClass == null ? "Please select a class first." : "Confirm selection: " + selectedClass + "?");
                     }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                String buttonLabel = button.getText();
                boolean isClassBtn = characterButtonMap.containsKey(buttonLabel);
                boolean isDoneBtn = buttonLabel.equals(doneButtonLabel);

                // Reset visual appearance (if not the selected button)
                if (!buttonLabel.equals(selectedClass)) {
                    if (screenContext.equals("characterCreator")) {
                         if (isClassBtn) {
                             button.setForeground(defaultFg); // Reset text to white
                         } else if (isDoneBtn) {
                             button.setBackground(normalBg); // Reset Done button background
                         }
                    } else { // Gameplay screen
                         button.setBackground(normalBg); // Gameplay always resets background
                    }
                }

                 // Update details/image panel and comment when mouse leaves a button
                 if (screenContext.equals("characterCreator")) {
                     // Revert display to the *selected* class details/image, or prompt if none selected
                     updateCharacterImageAndDetails(selectedClass);
                     commentLabel.setText(selectedClass == null ? "Hover over or click a class button below." : selectedClass + " selected. Click 'Done' to confirm.");
                 }
            }
        });
    }

    // --- NEW HELPER: Load Character Image ---
    /**
     * Finds available character images for a given class and set,
     * randomly selects one, and returns it as an ImageIcon.
     * Handles cases where no image is found.
     *
     * @param className The name of the character class (e.g., "Knight", "Wizard").
     * @return A randomly selected ImageIcon for the class, or null if none found/error.
     */
     private static ImageIcon loadCharacterImage(String className) {
         if (className == null || className.trim().isEmpty()) {
             return null; // No class specified
         }

         List<URL> foundImageUrls = new ArrayList<>();
         String baseImagePath = "images/character-" + RPG.currentImageSet + "-" + className.toLowerCase(); // e.g., images/character-speedracer-knight

         // Try loading images with sequence numbers (e.g., ...knight1.jpg, ...knight2.jpg)
         for (int i = 1; i <= 5; i++) { // Check for up to 5 variations
             String imagePath = baseImagePath + i + ".jpg";
             try {
                 URL imageURL = UIManagement.class.getClassLoader().getResource(imagePath);
                 if (imageURL != null) {
                     foundImageUrls.add(imageURL);
                     System.out.println("Found image: " + imagePath);
                 } else {
                     // Stop searching if a sequence number is missing (optional optimization)
                     // if (i > 1) break;
                 }
             } catch (Exception e) {
                 System.err.println("Error checking for resource: " + imagePath + " - " + e.getMessage());
             }
         }

         // If no numbered images found, try loading without a number (e.g., ...knight.jpg)
         if (foundImageUrls.isEmpty()) {
              String imagePath = baseImagePath + ".jpg";
              try {
                  URL imageURL = UIManagement.class.getClassLoader().getResource(imagePath);
                  if (imageURL != null) {
                      foundImageUrls.add(imageURL);
                      System.out.println("Found base image: " + imagePath);
                  }
              } catch (Exception e) {
                  System.err.println("Error checking for resource: " + imagePath + " - " + e.getMessage());
              }
         }


         // Select a random image from the found URLs
         if (!foundImageUrls.isEmpty()) {
             URL selectedUrl = foundImageUrls.get(random.nextInt(foundImageUrls.size()));
             try {
                  ImageIcon icon = new ImageIcon(selectedUrl);
                   // Check if image loaded correctly (basic check)
                  if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                       System.out.println("Successfully loaded image icon from: " + selectedUrl);
                       return icon;
                  } else {
                       System.err.println("Error: ImageIcon status not complete for " + selectedUrl);
                       return null;
                  }
             } catch (Exception e) {
                  System.err.println("Exception creating ImageIcon from URL: " + selectedUrl + " - " + e.getMessage());
                  return null;
             }
         } else {
             System.out.println("No images found for class '" + className + "' in set '" + RPG.currentImageSet + "'.");
             return null; // No images found
         }
     }


    // --- REVISED: Update Both Image and Class Details ---
    /**
     * Updates the character image display and the details text pane.
     * Handles loading a new random image only when the class label changes.
     *
     * @param label The class label to display ("Knight", "Wizard", etc.), or null to show prompt/placeholder.
     */
    private static void updateCharacterImageAndDetails(String label) {
        ImageIcon iconToShow = null;

        // 1. Determine which icon to show
        if (label != null && characterButtonMap.containsKey(label)) {
            // Check if we need to load a *new* random image for this class
            // This happens if it's a different class than the last one displayed OR
            // if we don't have an icon cached for this class yet in this session.
            if (!label.equals(lastDisplayedClass) || !currentIconForClass.containsKey(label)) {
                iconToShow = loadCharacterImage(label);
                if (iconToShow != null) {
                    currentIconForClass.put(label, iconToShow); // Cache the newly loaded icon
                    System.out.println("Loaded and cached new icon for: " + label);
                } else {
                     // If loading failed, remove any old cache and default to null
                     currentIconForClass.remove(label);
                     System.out.println("Failed to load image for: " + label + ". Cleared cache.");
                }
                lastDisplayedClass = label; // Update the tracker
            } else {
                // Use the already cached icon for this class
                iconToShow = currentIconForClass.get(label);
                 System.out.println("Using cached icon for: " + label);
            }
        } else {
            // No valid class label provided (e.g., initial state, hovering 'Done' with no selection)
            iconToShow = null;
            lastDisplayedClass = null; // Reset display tracker
        }


        // 2. Display the determined icon (or placeholder)
        if (iconToShow != null) {
            try {
                Image originalImage = iconToShow.getImage();
                int originalWidth = iconToShow.getIconWidth();
                int originalHeight = iconToShow.getIconHeight();
                int scaledHeight = -1;

                if (originalWidth > 0 && originalHeight > 0) {
                    scaledHeight = (int) (((double) CHAR_IMAGE_TARGET_WIDTH / originalWidth) * originalHeight);
                }

                if (scaledHeight > 0) {
                    Image scaledImage = originalImage.getScaledInstance(CHAR_IMAGE_TARGET_WIDTH, scaledHeight, Image.SCALE_SMOOTH);
                    characterImageLabel.setIcon(new ImageIcon(scaledImage));
                    characterImageLabel.setText(""); // Clear any error text
                } else {
                    System.err.println("Could not calculate scaled height for image: " + label + ". Displaying original.");
                    characterImageLabel.setIcon(iconToShow); // Fallback to original size
                    characterImageLabel.setText("");
                }
            } catch (Exception e) {
                 System.err.println("Exception scaling/setting image icon for " + label + ": " + e.getMessage());
                 characterImageLabel.setIcon(null);
                 characterImageLabel.setText("<html><center>Error displaying image<br>for " + (label != null ? label : "class") + "</center></html>");
                 characterImageLabel.setForeground(Color.RED);
            }
        } else {
            // Show placeholder text if no icon is available/selected
            characterImageLabel.setIcon(null);
            characterImageLabel.setText("<html><center>Select a Class</center></html>");
            characterImageLabel.setForeground(Color.DARK_GRAY); // Use a neutral color
        }


        // 3. Update the Text Details Pane (based on the input label, not necessarily the one displayed last)
        String detailsHtml;
        if (label != null && characterButtonMap.containsKey(label)) {
             RPG classTemplate = null;
             switch (label) { // Use the *input* label for details lookup
                case "Knight": classTemplate = RPG.getKnightTemplate(); break;
                case "Sentinel": classTemplate = RPG.getSentinelTemplate(); break;
                case "Assassin": classTemplate = RPG.getAssassinTemplate(); break;
                case "Wizard": classTemplate = RPG.getWizardTemplate(); break;
                case "Caveman": classTemplate = RPG.getCavemanTemplate(); break;
             }
             if (classTemplate != null) {
                  detailsHtml = "<html><body><h3>" + classTemplate.getClassName() + "</h3><table>" +
                                "<tr><th>Stat</th><th>Value</th></tr>" +
                                "<tr><td>Max HP</td><td>" + classTemplate.getMaxHp() + "</td></tr>" +
                                "<tr><td>Max Energy</td><td>" + classTemplate.getMaxEnergy() + "</td></tr>" +
                                "<tr><td>Vigor</td><td>" + classTemplate.getVig() + "</td></tr>" +
                                "<tr><td>Defense</td><td>" + classTemplate.getDef() + "</td></tr>" +
                                "<tr><td>Strength</td><td>" + classTemplate.getStr() + "</td></tr>" +
                                "<tr><td>Dexterity</td><td>" + classTemplate.getDex() + "</td></tr>" +
                                "<tr><td>Agility</td><td>" + classTemplate.getAgt() + "</td></tr>" +
                                "<tr><td>Luck</td><td>" + classTemplate.getLuck() + "</td></tr>" +
                                "</table></body></html>";
             } else {
                  detailsHtml = "<html><body><h3>Error</h3><p>Could not find details for " + label + ".</p></body></html>";
             }
        } else {
             // Default prompt when no class label is provided
             detailsHtml = "<html><body><h3 style='margin-top: 50px;'>Class Details</h3><p style='text-align:center; padding: 10px;'>" +
                           "Hover over or click a class button below to see its attributes here." + "</p>" +
                           (selectedClass != null ? "<p style='text-align:center; font-weight: bold;'>Selected: " + selectedClass + "</p>" : "") + "</body></html>";
        }
        classDetailsPane.setText(detailsHtml);
        classDetailsPane.setCaretPosition(0); // Scroll to top

        // Revalidate the panel containing the image label to ensure layout updates
        characterDescPanel.revalidate();
        characterDescPanel.repaint();
    }


} // End of UIManagement class