/**
 * @file    UIManagement.java
 * @brief   Manages UI window, panels, layout, actions, key bindings, screen transitions.
 * Includes Room Legend Panel overlay.
 *
 * @author  Jack Schulte & AI Assistant & Gemini
 * @version 1.5.1 (Fix Char Create Layout)
 * @date    2025-04-15
 *
 * @copyright Copyright (c) 2025 Jack Schulte & Gemini. All rights reserved.
 * Strictly confidential and proprietary. Distribution, reproduction,
 * or modification is strictly prohibited without prior written permission.
 *
 * @version History:
 * - 1.5.1 (2025-04-15): Fixed character creator screen by adding characterImageLabel and classDetailsPane to characterDescPanel layout. Added scroll pane to details. (Gemini)
 * - 1.5.0 (2025-04-15): Added RoomLegendPanel overlay toggled by F9. Used CardLayout for overlay effect in rightSidePanel. (Gemini)
 * - 1.4.1 (2025-04-15): Fixed layout for PlayerPanel/StatusPanel (BoxLayout). Increased rightSidePanel width. (Gemini)
 * - 1.4.0 (2025-04-15): Refactored for Player class. Added PlayerPanel. Updated layout. (Gemini)
 * - (Previous history omitted)
 */
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class UIManagement {

    // UI Components
    public static JFrame frame = new JFrame();
    private static JPanel topPanel = new JPanel(new BorderLayout(10, 0));
    private static JLabel playerInfoLabel = new JLabel("", SwingConstants.RIGHT);
    private static JLabel titleLabel = new JLabel("", SwingConstants.CENTER);
    private static JPanel rightSidePanel = new JPanel(new BorderLayout());
    private static JPanel topRightPanel;
    private static PlayerPanel playerPanel;
    private static StatusPanel statusPanel;
    // Panel to hold Dialogue and Legend using CardLayout
    private static JPanel dialogueLegendContainer;
    private static DialoguePanel dialoguePanel;
    private static RoomLegendPanel roomLegendPanel; // New Legend Panel
    private static final String DIALOGUE_CARD = "DialogueCard";
    private static final String LEGEND_CARD = "LegendCard";
    // ------------------------------------------------------------
    private static JPanel centerPanel = new JPanel(new CardLayout()); // Holds Welcome, CharCreate, Map
    private static WelcomePanel welcomePanel;
    private static JPanel characterDescPanel; // Defined here, configured in setupUI
    private static JLabel characterImageLabel; // Defined here, configured in setupUI
    private static JEditorPane classDetailsPane; // Defined here, configured in setupUI
    private static MapPanel mapPanel;
    private static JPanel bottomPanel = new JPanel(new BorderLayout());
    private static JLabel commentLabel = new JLabel("", SwingConstants.CENTER);
    private static JPanel buttonPanel = new JPanel();
    private static GridBagConstraints gbc = new GridBagConstraints();

    // Fonts, Colors, Config
    private static Font titleFont = new Font("Trebuchet MS", Font.BOLD, 24);
    private static Font playerInfoFont = new Font("Trebuchet MS", Font.BOLD, 18);
    private static Font commentFont = new Font("Trebuchet MS", Font.ITALIC, 18);
    private static Font buttonFont = new Font("Aharoni", Font.BOLD, 20);
    public static Color backgroundColor = Color.decode("#EEEEEE");
    private static Color characterDetailBackgroundColor = Color.decode("#F5F5DC"); // Beige background for details
    private static Color buttonDefaultForegroundColor = Color.WHITE;
    private static Color buttonHoverForegroundColor = Color.RED;
    private static Color buttonSelectedBackgroundColor = Color.BLACK;
    private static Color buttonSelectedForegroundColor = Color.WHITE;
    private static String[] characterButtonLabels = {"Knight", "Sentinel", "Assassin", "Wizard", "Caveman"};
    private static String[] characterButtonColors = {"#48AD48", "#58D558"}; // Normal, Hover/Selected (Done uses different)
    private static String doneButtonLabel = "Done";
    private static String[] doneButtonColors = {"#EECC44", "#F7DD77"}; // Normal, Hover (Done button)
    private static int[] characterButtonLengths = new int[]{140, 140, 140, 140, 140, 100}; // Widths for class buttons + Done
    private static Map<String, JButton> characterButtonMap = new HashMap<>();
    private static JButton doneButton = null;
    private static String selectedClass = null;
    private static Map<String, ImageIcon> currentIconForClass = new HashMap<>();
    private static String lastDisplayedClass = null; // Track last class shown to avoid reloading
    private static Random random = new Random();
    private static Map<String, String> currentRandomNameMap = new HashMap<>(); // Cache random names per session
    private static LineBorder defaultBorder = new LineBorder(Color.GRAY, 1);
    private static LineBorder focusBorder = new LineBorder(Color.BLUE, 2); // Highlight for focused button
    private static String[] moveButtonLabels = {"Turn Left", "Forward", "Turn Right", "Backward"};
    private static String[] moveButtonColors = {"#44AADD", "#66CCFF"}; // Normal, Hover (Gameplay buttons)
    private static boolean isGameScreenActive = false;
    private static boolean isCharacterCreatorScreenActive = false;
    // Arrow symbols for button text (optional)
    private static final String LEFT_ARROW = "\u2190"; private static final String UP_ARROW = "\u2191";
    private static final String RIGHT_ARROW = "\u2192"; private static final String DOWN_ARROW = "\u2193";


    // Method to set up the main UI structure
    public static void setupUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Legend of Splenda: Bread of the Wild");
        Dimension initialSize = new Dimension(1300, 900);
        frame.setSize(initialSize);
        frame.setMinimumSize(initialSize); // Prevent resizing smaller
        frame.setLayout(new BorderLayout(5, 5)); // Main layout with gaps

        // --- Top Panel (Title, Player Info) ---
        playerInfoLabel.setFont(playerInfoFont);
        playerInfoLabel.setOpaque(true);
        playerInfoLabel.setBackground(backgroundColor);
        playerInfoLabel.setBorder(new EmptyBorder(5, 10, 5, 10)); // Padding
        titleLabel.setFont(titleFont);
        titleLabel.setOpaque(true);
        titleLabel.setForeground(Color.decode("#B6220E")); // Title color
        titleLabel.setBackground(backgroundColor);
        titleLabel.setBorder(new EmptyBorder(5, 10, 5, 10)); // Padding
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(playerInfoLabel, BorderLayout.EAST);
        topPanel.setBackground(backgroundColor);
        topPanel.setVisible(false); // Initially hidden

        // --- Right Side Panel Setup (Player, Status, Dialogue/Legend) ---
        playerPanel = new PlayerPanel(); // Panel for player image in-game
        statusPanel = new StatusPanel(); // Panel for HP/Energy/Score
        dialoguePanel = new DialoguePanel(); // Panel for game messages
        roomLegendPanel = new RoomLegendPanel(); // Instantiate legend panel
        roomLegendPanel.setVisible(false); // Initially hidden

        // Panel for Player image + Status (Horizontal Box)
        topRightPanel = new JPanel();
        topRightPanel.setLayout(new BoxLayout(topRightPanel, BoxLayout.X_AXIS));
        topRightPanel.setBackground(backgroundColor);
        topRightPanel.add(playerPanel);
        topRightPanel.add(Box.createHorizontalStrut(5)); // Space between player and status
        topRightPanel.add(statusPanel);

        // Container for Dialogue/Legend using CardLayout
        dialogueLegendContainer = new JPanel(new CardLayout());
        dialogueLegendContainer.setBackground(backgroundColor); // Match background
        dialogueLegendContainer.add(dialoguePanel, DIALOGUE_CARD); // Add dialogue panel
        dialogueLegendContainer.add(roomLegendPanel, LEGEND_CARD);  // Add legend panel

        // Assemble the main right-side container panel
        rightSidePanel.setBackground(backgroundColor);
        rightSidePanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0)); // Left padding
        rightSidePanel.add(topRightPanel, BorderLayout.NORTH);              // Player+Status hbox at top
        rightSidePanel.add(dialogueLegendContainer, BorderLayout.CENTER);   // CardLayout container below
        rightSidePanel.setPreferredSize(new Dimension(400, 0)); // Give it a preferred width
        rightSidePanel.setVisible(false); // Initially hidden

       // --- Center Panel (CardLayout: Welcome, Character Creator, Map) ---
        welcomePanel = new WelcomePanel();
        centerPanel.add(welcomePanel, "welcome");

        // Character Creator Panel Setup
        characterDescPanel = new JPanel(new BorderLayout(10, 0)); // Panel holds image and details
        characterDescPanel.setBackground(backgroundColor);

        // Initialize Image Label (with custom painting for scaling)
        characterImageLabel = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                ImageIcon icon = (ImageIcon) getIcon();
                if (icon != null) {
                    Image img = icon.getImage();
                    // Calculate scale factor to fit within the label bounds while maintaining aspect ratio
                    float scaleFactor = Math.min(1f, Math.min((float) getWidth() / img.getWidth(null), (float) getHeight() / img.getHeight(null)));
                    int scaledWidth = (int) (img.getWidth(null) * scaleFactor);
                    int scaledHeight = (int) (img.getHeight(null) * scaleFactor);
                    // Center the image
                    int x = (getWidth() - scaledWidth) / 2;
                    int y = (getHeight() - scaledHeight) / 2;
                    // Draw with better quality hints
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.drawImage(img, x, y, scaledWidth, scaledHeight, this);
                    g2d.dispose();
                } else {
                    super.paintComponent(g); // Draw default text if no icon
                }
            }
        };
        characterImageLabel.setPreferredSize(new Dimension(300, 400)); // Suggest a size
        characterImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        characterImageLabel.setVerticalAlignment(SwingConstants.CENTER);
        characterImageLabel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY)); // Border helps visibility

        // Initialize Details Pane (using JEditorPane for HTML)
        classDetailsPane = new JEditorPane();
        classDetailsPane.setContentType("text/html");
        classDetailsPane.setEditable(false);
        classDetailsPane.setBackground(characterDetailBackgroundColor);
        classDetailsPane.setBorder(new EmptyBorder(10, 10, 10, 10)); // Padding inside details

        // Set up HTML styles for the details pane
        HTMLEditorKit kit = new HTMLEditorKit();
        classDetailsPane.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body {font-family: Trebuchet MS; font-size: 12px;}");
        styleSheet.addRule("h2.classname { text-align: center; color: #B6220E; margin-bottom: 10px; }");
        styleSheet.addRule("p.charname { text-align: center; font-style: italic; font-weight: bold; margin-top: 15px; font-size: 14px; }");
        styleSheet.addRule("table { border-collapse: collapse; margin: 10px auto; width: 90%; }");
        styleSheet.addRule("th, td { border: 1px solid #AAAAAA; padding: 4px 8px; text-align: left; }");
        styleSheet.addRule("th { background-color: #E0E0E0; font-weight: bold; }");
        styleSheet.addRule("td:first-child { font-weight: bold; width: 80px; }"); // Stat name bold
        JScrollPane detailsScrollPane = new JScrollPane(classDetailsPane); // Add scroll bars if needed
        detailsScrollPane.setPreferredSize(new Dimension(250, 0)); // Give details pane a preferred width

        // *** FIX: Add the image label and details pane to the characterDescPanel ***
        characterDescPanel.add(characterImageLabel, BorderLayout.CENTER); // Image in the middle
        characterDescPanel.add(detailsScrollPane, BorderLayout.EAST); // Details on the right (using scroll pane)
        // **************************************************************************

        centerPanel.add(characterDescPanel, "characterCreator"); // Add the prepared panel to CardLayout

        // Map Panel is created later when the game starts
        centerPanel.setBackground(backgroundColor);

        // --- Bottom Panel (Comments, Buttons) ---
        commentLabel.setFont(commentFont);
        commentLabel.setBackground(backgroundColor);
        commentLabel.setOpaque(true);
        commentLabel.setBorder(new EmptyBorder(5,0,5,0)); // Padding top/bottom
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.setLayout(new GridBagLayout()); // Use GridBagLayout for button alignment
        buttonPanel.setFocusable(true); // Allows key bindings to work when panel has focus
        gbc.insets = new Insets(5, 10, 5, 10); // Button spacing
        gbc.gridy = 0; // All buttons on the same row
        bottomPanel.setBackground(backgroundColor);
        bottomPanel.add(commentLabel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.setBorder(new EmptyBorder(10, 0, 10, 0)); // Padding for the whole bottom area

        // --- Final Frame Assembly ---
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(rightSidePanel, BorderLayout.EAST);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        setupKeyBindings(); // General key bindings (Arrows, F8, F9)
        setupCharacterCreatorKeyBindings(); // Character creator specific keys (Enter, Space, Nav)
    }


    // ==============================================================================================
    // =================== Screen Management & Game Initialization ==================================

    /**
     * Switches the central display panel using CardLayout.
     * Manages visibility of other UI elements based on the active screen.
     * @param screenName The name of the card/screen to show ("welcome", "characterCreator", "map").
     */
    private static void showScreen(String screenName) {
        // Check if mapPanel exists before trying to show it
        if (screenName.equals("map")) {
            if (mapPanel == null) {
                System.err.println("Error: MapPanel is null, cannot switch to map screen.");
                return; // Avoid switching if map isn't ready
            }
            // Double-check it's actually added to the centerPanel (should be by initializeAndStartGame)
            boolean found = false;
            for(Component comp : centerPanel.getComponents()) {
                if (comp == mapPanel) {
                    found = true; break;
                }
            }
            if (!found) {
                 System.err.println("Error: MapPanel has not been added to the centerPanel layout.");
                 return;
            }
        }

        CardLayout cl = (CardLayout)(centerPanel.getLayout());
        cl.show(centerPanel, screenName);

        // Update flags for key binding context
        isGameScreenActive = screenName.equals("map");
        isCharacterCreatorScreenActive = screenName.equals("characterCreator");

        System.out.println("Screen: " + screenName); // Log screen transition

        // Control visibility of surrounding panels
        boolean showTopBottom = !screenName.equals("welcome");
        topPanel.setVisible(showTopBottom);
        rightSidePanel.setVisible(isGameScreenActive); // Only show right panel during gameplay
        bottomPanel.setVisible(showTopBottom);

        // Screen-specific setup
        if (isGameScreenActive) {
            commentLabel.setText("Arrows/Buttons move. F8 Map. F9 Legend.");
            if(RPG.getPlayers() != null && !RPG.getPlayers().isEmpty()) {
                updateGameStatus(RPG.getPlayers().get(0)); // Initial update for player 1
            }
            frame.getRootPane().requestFocusInWindow(); // Ensure main frame gets key events
        } else if (isCharacterCreatorScreenActive) {
            commentLabel.setText("Select a class using buttons or arrow keys. Press Enter or click 'Done'.");
            lastDisplayedClass = null; // Reset display cache
            currentIconForClass.clear();
            currentRandomNameMap.clear();
            updateCharacterImageAndDetails(null); // Show default state initially
            // Request focus on the button panel for keyboard navigation
             SwingUtilities.invokeLater(() -> {
                 buttonPanel.requestFocusInWindow();
                 // Try focusing the first button specifically
                 if(buttonPanel.getComponentCount() > 0 && buttonPanel.getComponent(0) instanceof JButton) {
                      buttonPanel.getComponent(0).requestFocusInWindow();
                 }
             });
        } else { // Welcome screen or other future screens
            commentLabel.setText("");
            playerInfoLabel.setText("");
            titleLabel.setText(""); // Reset title/player info
            buttonPanel.removeAll(); // Clear buttons if not welcome/char create/game
            buttonPanel.revalidate();
            buttonPanel.repaint();
        }

        // Ensure layout updates are reflected
        frame.revalidate();
        frame.repaint();
    }

    /** Switches to the welcome screen. */
    public static void welcomeScreen() {
        showScreen("welcome");
    }

    /** Switches to the character creator screen. Sets up title, buttons, and player info. */
    public static void characterCreatorScreen() {
        selectedClass = null; // Reset selection
        characterButtonMap.clear();
        doneButton = null;

        // Update top panel for the current player being created
        titleLabel.setText("Create Character");
        playerInfoLabel.setText("Player " + (RPG.getPlayerIndex() + 1) + " of " + RPG.getNumPlayers());
        topPanel.setVisible(true);

        // Prepare button data (Label, NormalColorHex, HoverColorHex)
        ArrayList<String> buttonCreationList = new ArrayList<>();
        ArrayList<Integer> lengthCreationList = new ArrayList<>();
        for (int i = 0; i < characterButtonLabels.length; i++) {
            String label = characterButtonLabels[i];
            buttonCreationList.add(label);                      // Label
            buttonCreationList.add(characterButtonColors[0]);   // Normal BG color
            buttonCreationList.add(characterButtonColors[1]);   // Hover BG color (unused for class buttons)
            lengthCreationList.add(characterButtonLengths[i]);  // Width
        }
        // Add the "Done" button data
        buttonCreationList.add(doneButtonLabel);
        buttonCreationList.add(doneButtonColors[0]);            // Normal BG color
        buttonCreationList.add(doneButtonColors[1]);            // Hover BG color
        lengthCreationList.add(characterButtonLengths[characterButtonLengths.length - 1]); // Width

        // Create the buttons using the helper method
        makeButtons(buttonCreationList.toArray(new String[0]),
                    lengthCreationList.stream().mapToInt(i->i).toArray(),
                    "characterCreator");

        showScreen("characterCreator");
        bottomPanel.setVisible(true); // Ensure bottom panel is visible
    }

    /** Initializes player positions, map exploration, and transitions to the main game screen. */
    private static void initializeAndStartGame() {
        System.out.println("Initializing game state and map panel...");

        RPG centralGame = RPG.getCentralGameInstance();
        if (centralGame == null || centralGame.getBoard() == null) {
            System.err.println("FATAL ERROR: Central game or board is not initialized. Returning to welcome.");
            welcomeScreen(); // Go back if critical components missing
            return;
        }
        Board board = centralGame.getBoard();

        if (RPG.getPlayers() == null || RPG.getPlayers().isEmpty()) {
             System.err.println("ERROR: No players created. Returning to welcome.");
             welcomeScreen();
             return;
        }

        // Set initial player positions and explore around start
        int startRow = board.getHeight() / 2;
        int startCol = board.getWidth() / 2;
        Room startRoom = board.getRoom(startRow, startCol);

        if (startRoom == null || !startRoom.isTraversable()) {
             System.err.println("ERROR: Start room at [" + startRow + "," + startCol + "] is invalid. Returning to welcome.");
             welcomeScreen();
             return;
        }

        for (Player p : RPG.getPlayers()) {
            p.setCurrentPos(new int[]{startRow, startCol});
            p.setWayFacing(0); // Face North initially

            // Player 1 does initial exploration
            if (RPG.getPlayers().indexOf(p) == 0) {
                startRoom.setVisited(true); // Mark the start room as visited
                p.exploreAround(startRow, startCol, board); // Reveal starting area
            }
        }

        // Prepare UI for game start
        Player player1 = RPG.getPlayers().get(0);
        if (dialoguePanel != null) dialoguePanel.clearDialogue(); // Clear previous messages
        addDialogue("Welcome, " + player1.getCharacterName() + " the " + player1.getClassName() + "!");
        addDialogue("You awaken in the " + startRoom.getDescription() + ".");
        updateGameStatus(player1); // Initial UI update

        // Create and add the MapPanel to the centerPanel's CardLayout
         System.out.println("Creating Map Panel...");
         mapPanel = new MapPanel(centralGame); // Create the panel
         centerPanel.add(mapPanel, "map");     // Add it with the name "map"

        System.out.println("Starting game screen...");
        startGameScreen(); // Switch view and set up game buttons
        System.out.println("Game initialization complete.");
    }

    /** Sets up the UI for the main gameplay screen (map, buttons, labels). */
    private static void startGameScreen() {
         if (mapPanel == null || RPG.getPlayers().isEmpty() || RPG.getCentralGameInstance() == null) {
              System.err.println("Cannot start game screen - required components are missing!");
              welcomeScreen();
              return;
         }
        Player player1 = RPG.getPlayers().get(0);

        // Update top panel
        titleLabel.setText("Dungeon Exploration");
        updatePlayerInfoLabel(player1);

        // Update bottom panel comment
        commentLabel.setText("Arrows/Buttons move. F8 Map. F9 Legend.");

        // Create gameplay buttons
        ArrayList<String> buttonCreationList = new ArrayList<>();
        ArrayList<Integer> lengthCreationList = new ArrayList<>();
        int buttonLength = 110; // Standard width for move buttons
        for (String label : moveButtonLabels) {
            buttonCreationList.add(label);
            buttonCreationList.add(moveButtonColors[0]); // Normal color
            buttonCreationList.add(moveButtonColors[1]); // Hover color
            lengthCreationList.add(buttonLength);
        }
        makeButtons(buttonCreationList.toArray(new String[0]),
                    lengthCreationList.stream().mapToInt(i->i).toArray(),
                    "gameplay");

        showScreen("map"); // Switch the center panel to the map view
        mapPanel.refreshMap(); // Initial map draw
        bottomPanel.setVisible(true);
        frame.getRootPane().requestFocusInWindow(); // Ensure key bindings work
    }


    // ==============================================================================================
    // =================== Action Listeners =========================================================

    /** Central handler for button clicks, delegating based on screen context. */
    private static void pickActionListener(JButton button, String buttonLabel, String screenContext) {
        // Clear existing listeners to prevent duplicates if buttons are remade
        for(ActionListener al : button.getActionListeners()) {
            button.removeActionListener(al);
        }

        switch (screenContext) {
            case "characterCreator":
                boolean isClassButton = characterButtonMap.containsKey(buttonLabel);
                if (isClassButton) {
                    // Lambda expression for class selection action
                    button.addActionListener(e -> handleClassSelection(buttonLabel));
                } else if (buttonLabel.equals(doneButtonLabel)) {
                    // Lambda expression for "Done" button action
                    button.addActionListener(e -> {
                        if (selectedClass == null) {
                            RPG.playAudio("audio/action-wrong.wav"); // Play error sound
                            commentLabel.setText("You must select a class before proceeding!");
                            updateCharacterImageAndDetails(null); // Clear display
                        } else {
                            // Finalize class/name/icon for the current player
                            int currentPlayerIndex = RPG.getPlayerIndex();
                            if (currentPlayerIndex < 0 || currentPlayerIndex >= RPG.getPlayers().size()) {
                                System.err.println("ERROR: Invalid player index " + currentPlayerIndex + " during character finalization.");
                                welcomeScreen(); return;
                            }
                            Player player = RPG.getPlayers().get(currentPlayerIndex);

                            RPG.assignClass(currentPlayerIndex, selectedClass); // Assign stats etc.

                            // Assign cached name and icon
                            String finalName = currentRandomNameMap.get(selectedClass);
                            if (finalName == null) finalName = CharacterClassInfo.getRandomName(selectedClass); // Fallback
                            player.setCharacterName(finalName);

                            ImageIcon finalIcon = currentIconForClass.get(selectedClass);
                            if (finalIcon != null) player.setSelectedCharacterIcon(finalIcon);
                            else System.err.println("Warning: Icon was null for selected class: " + selectedClass);

                            // Move to next player or start game
                            RPG.modPlayerIndex(); // Increment player index
                            if (RPG.getPlayerIndex() < RPG.getNumPlayers()) {
                                characterCreatorScreen(); // Set up screen for next player
                            } else {
                                initializeAndStartGame(); // All players created, start the game
                            }
                        }
                    });
                }
                break;

            case "gameplay":
                // Assign actions for gameplay buttons
                switch (buttonLabel) {
                    case "Turn Left":  button.addActionListener(e1 -> handleTurnLeft()); break;
                    case "Forward":    button.addActionListener(e1 -> handleMoveForward()); break;
                    case "Turn Right": button.addActionListener(e1 -> handleTurnRight()); break;
                    case "Backward":   button.addActionListener(e1 -> handleMoveBackward()); break;
                }
                break;
            // Add cases for other screen contexts if needed
        }
    }

    /** Handles selection of a character class button. Updates state and UI. */
    private static void handleClassSelection(String buttonLabel) {
        if (!isCharacterCreatorScreenActive) return; // Only works on char create screen

        selectedClass = buttonLabel; // Store the selected class name
        lastDisplayedClass = buttonLabel; // Update last displayed to prevent flicker if re-hovered
        commentLabel.setText(buttonLabel + " selected. Press Enter or click 'Done' to confirm.");

        // Update visual state of all buttons (highlight selected, unhighlight others)
        updateAllButtonAppearances();

        // Update the image and details panel
        updateCharacterImageAndDetails(selectedClass);
    }

    /** Updates the player info label in the top panel. */
    private static void updatePlayerInfoLabel(Player player) {
        if (player == null || !isGameScreenActive) {
            playerInfoLabel.setText(""); // Clear if no player or not in game
            return;
        }
        // Display player name and class
        playerInfoLabel.setText(player.getCharacterName() + " (" + player.getClassName() + ")");
    }


    // ==============================================================================================
    // =================== Gameplay Action Handlers ================================================

    // Movement handlers call Player methods and request focus back to frame for keys
    private static void handleTurnLeft() {
        if (!isGameScreenActive || RPG.getPlayers().isEmpty()) return;
        RPG.getPlayers().get(0).turnPlayer(0); // 0 for left turn
        frame.getRootPane().requestFocusInWindow(); // Allow continued key input
    }

    private static void handleTurnRight() {
        if (!isGameScreenActive || RPG.getPlayers().isEmpty()) return;
        RPG.getPlayers().get(0).turnPlayer(1); // 1 for right turn
        frame.getRootPane().requestFocusInWindow();
    }

    private static void handleMoveForward() {
        if (!isGameScreenActive || RPG.getPlayers().isEmpty() || RPG.getCentralGameInstance() == null) return;
        Player player = RPG.getPlayers().get(0);
        Board board = RPG.getCentralGameInstance().getBoard();
        player.movePlayer(player.getWayFacing(), board); // Move in the direction player is facing
        frame.getRootPane().requestFocusInWindow();
    }

    private static void handleMoveBackward() {
        if (!isGameScreenActive || RPG.getPlayers().isEmpty() || RPG.getCentralGameInstance() == null) return;
        Player player = RPG.getPlayers().get(0);
        Board board = RPG.getCentralGameInstance().getBoard();
        int oppositeDirection = (player.getWayFacing() + 2) % 4; // Calculate opposite direction
        player.movePlayer(oppositeDirection, board);
        frame.getRootPane().requestFocusInWindow();
    }

    // Toggle Map visibility handled directly in key binding action below
    // Toggle Legend visibility handled directly in key binding action below


    // ==============================================================================================
    // =================== Key Binding Setup ========================================================

    /** Sets up global key bindings for the application frame. */
    private static void setupKeyBindings() {
        // Get the input and action maps for the root pane (work when window has focus)
        InputMap inputMap = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = frame.getRootPane().getActionMap();

        // Define KeyStrokes for gameplay actions
        KeyStroke upKey = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
        KeyStroke downKey = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
        KeyStroke leftKey = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
        KeyStroke rightKey = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
        KeyStroke f8Key = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
        KeyStroke f9Key = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0); // F9 for Legend

        // Map KeyStrokes to action names
        inputMap.put(upKey, "gameplayMoveForward");
        inputMap.put(downKey, "gameplayMoveBackward");
        inputMap.put(leftKey, "gameplayTurnLeft");
        inputMap.put(rightKey, "gameplayTurnRight");
        inputMap.put(f8Key, "gameplayToggleMap");
        inputMap.put(f9Key, "gameplayToggleLegend"); // Map F9 key

        // Define Actions corresponding to the action names
        actionMap.put("gameplayMoveForward", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { if(isGameScreenActive) handleMoveForward(); }
        });
        actionMap.put("gameplayMoveBackward", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { if(isGameScreenActive) handleMoveBackward(); }
        });
        actionMap.put("gameplayTurnLeft", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { if(isGameScreenActive) handleTurnLeft(); }
        });
        actionMap.put("gameplayTurnRight", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { if(isGameScreenActive) handleTurnRight(); }
        });
        actionMap.put("gameplayToggleMap", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if(isGameScreenActive) {
                    RPG.toggleMapVisibility(); // Call static toggle method
                    commentLabel.setText("Map view toggled (Show full: " + RPG.isShowFullMap() + ")");
                    frame.getRootPane().requestFocusInWindow(); // Refocus frame
                 }
            }
        });
         actionMap.put("gameplayToggleLegend", new AbstractAction() { // Action for F9
            @Override public void actionPerformed(ActionEvent e) {
                if(isGameScreenActive) {
                    handleToggleLegend(); // Call the legend toggle method
                    frame.getRootPane().requestFocusInWindow(); // Refocus frame
                 }
            }
        });
    }

    /** Handles toggling the Room Legend visibility in the right-side panel. */
    private static void handleToggleLegend() {
        if (dialogueLegendContainer == null || roomLegendPanel == null || RPG.getCentralGameInstance() == null) {
             System.err.println("Cannot toggle legend: required components missing.");
             return;
         }

        CardLayout cl = (CardLayout) dialogueLegendContainer.getLayout();
        // Check which card is currently visible by checking component visibility
        boolean isLegendCurrentlyVisible = roomLegendPanel.isVisible();

        if (isLegendCurrentlyVisible) {
            // Switch back to dialogue
            cl.show(dialogueLegendContainer, DIALOGUE_CARD);
            commentLabel.setText("Arrows/Buttons move. F8 Map. F9 Legend."); // Restore default comment
            System.out.println("Switched to Dialogue Panel.");
        } else {
            // Update legend content BEFORE showing it
            Board currentBoard = RPG.getCentralGameInstance().getBoard();
            if (currentBoard != null) {
                roomLegendPanel.updateLegend(currentBoard); // Refresh legend data
            }
            // Switch to legend
            cl.show(dialogueLegendContainer, LEGEND_CARD);
            commentLabel.setText("Room Legend active. Press F9 to close.");
             System.out.println("Switched to Legend Panel.");
        }
        // Ensure the container panel itself is visible (it should be if game is active)
        rightSidePanel.setVisible(isGameScreenActive);
        dialogueLegendContainer.revalidate();
        dialogueLegendContainer.repaint();
    }

    /** Sets up key bindings specific to the character creator screen button panel. */
    private static void setupCharacterCreatorKeyBindings() {
        // Use WHEN_ANCESTOR_OF_FOCUSED_COMPONENT so keys work when panel or buttons have focus
        InputMap inputMap = buttonPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = buttonPanel.getActionMap();

        // Define KeyStrokes for navigation and selection
        KeyStroke leftKey = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
        KeyStroke rightKey = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
        KeyStroke spaceKey = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

        // Map keys to action names
        inputMap.put(leftKey, "navigateLeft");
        inputMap.put(rightKey, "navigateRight");
        inputMap.put(spaceKey, "activateFocusedButton"); // Space to click class buttons
        inputMap.put(enterKey, "activateDoneButton");   // Enter to click Done

        // Define Actions
        actionMap.put("navigateLeft", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { if(isCharacterCreatorScreenActive) navigateButtons(-1); } // -1 for left
        });
        actionMap.put("navigateRight", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { if(isCharacterCreatorScreenActive) navigateButtons(1); } // 1 for right
        });
        actionMap.put("activateFocusedButton", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if(isCharacterCreatorScreenActive) {
                    // Simulate a click on whichever button currently has focus
                    Component focusedComponent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                    if (focusedComponent instanceof JButton && buttonPanel.isAncestorOf(focusedComponent)) {
                        ((JButton) focusedComponent).doClick();
                    }
                }
            }
        });
        actionMap.put("activateDoneButton", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                // Specifically click the "Done" button if it exists
                if(isCharacterCreatorScreenActive && doneButton != null) {
                    doneButton.doClick();
                }
            }
        });
    }

    /** Handles navigating focus between buttons using left/right arrow keys. */
    private static void navigateButtons(int direction) {
        Component currentFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (buttonPanel.getComponentCount() == 0) return; // No buttons to navigate

        ArrayList<Component> buttons = new ArrayList<>();
        for (Component c : buttonPanel.getComponents()) {
            if (c instanceof JButton) {
                buttons.add(c); // Get all JButtons in the panel
            }
        }
        if (buttons.isEmpty()) return;

        int currentIndex = -1;
        // Find the index of the currently focused button
        if (currentFocusOwner instanceof JButton && buttons.contains(currentFocusOwner)) {
            currentIndex = buttons.indexOf(currentFocusOwner);
        } else {
            // If focus is not on a button, start from beginning/end depending on direction
             currentIndex = (direction > 0) ? -1 : buttons.size();
        }

        // Calculate the next index, wrapping around
        int nextIndex = (currentIndex + direction + buttons.size()) % buttons.size();

        // Request focus for the next button
        buttons.get(nextIndex).requestFocusInWindow();
    }


   // ==============================================================================================
   // =================== UI Update Methods ========================================================

   /** Updates all relevant game status UI elements (HP, Energy, Map, Player Info). */
   public static void updateGameStatus(Player player) {
        // Ensure UI updates happen on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            if (player == null || !isGameScreenActive) return; // Do nothing if no player or not in game

            if (statusPanel != null) statusPanel.updateStatus(player);
            if (playerPanel != null) playerPanel.updatePlayerImage(player);
            if (mapPanel != null) mapPanel.refreshMap(); // Refresh map view
            updatePlayerInfoLabel(player); // Update name/class display
        });
    }

    /** Adds a message to the dialogue panel. */
    public static void addDialogue(String text) {
        if (dialoguePanel != null) {
            dialoguePanel.addDialogue(text);
        } else {
            System.err.println("Error: DialoguePanel is null, cannot add message: " + text);
        }
    }


    // ==============================================================================================
    // =================== UI Helpers (Button Creation, Character Details) =========================

    /**
     * Creates buttons and adds them to the bottom button panel.
     * @param stringArray Array containing button data: [Label1, NormalColor1, HoverColor1, Label2, ...]
     * @param intArray Array containing button widths corresponding to labels.
     * @param screenContext String indicating the context ("characterCreator", "gameplay") for actions/styling.
     */
    private static void makeButtons(String[] stringArray, int[] intArray, String screenContext) {
        buttonPanel.removeAll(); // Clear previous buttons
        if (screenContext.equals("characterCreator")) {
            characterButtonMap.clear(); // Reset map for class buttons
            doneButton = null;
        }
        gbc.gridx = 0; // Start adding buttons from the left

        // Pre-parse colors for efficiency (optional, but cleaner)
        final Map<String, String> normalBgColors = new HashMap<>();
        final Map<String, String> hoverBgColors = new HashMap<>();
        for (int i = 0; i < stringArray.length / 3; i++) {
             String label = stringArray[i*3];
             normalBgColors.put(label, stringArray[i*3 + 1]);
             hoverBgColors.put(label, stringArray[i*3 + 2]);
        }

        for (int i = 0; i < stringArray.length / 3; i++) {
            String buttonLabel = stringArray[i * 3];
            JButton button = new JButton(buttonLabel);

            // --- Basic Styling ---
            button.setPreferredSize(new Dimension(intArray[i], 50)); // Set size
            button.setBackground(Color.decode(normalBgColors.get(buttonLabel))); // Normal BG
            button.setFont(buttonFont);
            button.setBorder(defaultBorder); // Default border
            button.setFocusPainted(false); // Don't draw default focus rectangle

             // --- Focus Listener (for Character Creator) ---
            button.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) {
                     if (!isCharacterCreatorScreenActive) return; // Only apply in char creator
                     JButton gainedFocusButton = (JButton) e.getComponent();
                     // Update image/details when a class button gains focus (but not Done)
                     if (!gainedFocusButton.getText().equals(doneButtonLabel)) {
                          updateCharacterImageAndDetails(gainedFocusButton.getText());
                     }
                     updateButtonAppearance(gainedFocusButton, true); // Highlight focused button
                }
                @Override public void focusLost(FocusEvent e) {
                     if (!isCharacterCreatorScreenActive) return;
                     JButton lostFocusButton = (JButton) e.getComponent();
                     // When focus leaves, update display based on the *selected* class (if any)
                     updateCharacterImageAndDetails(selectedClass);
                     updateButtonAppearance(lostFocusButton, false); // Remove highlight unless selected
                }
            });

            // --- Context-Specific Setup ---
            if (screenContext.equals("characterCreator")) {
                 boolean isSelected = buttonLabel.equals(selectedClass);
                 boolean isDoneButton = buttonLabel.equals(doneButtonLabel);

                 if (isDoneButton) {
                     button.setForeground(Color.BLACK); // Done button text color
                     doneButton = button; // Store reference to Done button
                 } else {
                     // This is a class button
                     characterButtonMap.put(buttonLabel, button); // Store reference
                     // Set initial appearance based on selection state
                     button.setForeground(isSelected ? buttonSelectedForegroundColor : buttonDefaultForegroundColor);
                     button.setBackground(isSelected ? buttonSelectedBackgroundColor : Color.decode(normalBgColors.get(buttonLabel)));
                 }
            } else { // Gameplay or other screens
                button.setForeground(Color.BLACK); // Default text color for gameplay buttons
            }

            // Add hover effects (primarily for Done button background)
            addHoverEffect(button, screenContext, normalBgColors, hoverBgColors);
            // Assign the correct action listener based on context
            pickActionListener(button, buttonLabel, screenContext);

            // Add button to the panel
            buttonPanel.add(button, gbc);
            gbc.gridx++; // Move to next grid position
        }

        buttonPanel.revalidate(); // Update layout
        buttonPanel.repaint(); // Redraw panel
    }

    /** Updates the visual appearance (border, text color, background) of a single button. */
    private static void updateButtonAppearance(JButton button, boolean hasFocus) {
        if (button == null || !isCharacterCreatorScreenActive) return;

        String buttonText = button.getText();
        boolean isSelected = buttonText.equals(selectedClass);
        boolean isDoneButton = buttonText.equals(doneButtonLabel);

        // --- Border ---
        button.setBorder(hasFocus ? focusBorder : defaultBorder); // Blue border if focused, gray otherwise
        // Keep border blue if selected even when losing focus (visual cue)
        if (isSelected && !isDoneButton) {
            button.setBorder(focusBorder);
        }


        // --- Foreground (Text Color) ---
        if (isDoneButton) {
            button.setForeground(Color.BLACK); // Done button always black text
        } else {
            // Class Buttons
            if (isSelected) {
                button.setForeground(buttonSelectedForegroundColor); // White text if selected
            } else if (hasFocus) {
                 button.setForeground(buttonHoverForegroundColor); // Red text if focused but not selected
            } else {
                button.setForeground(buttonDefaultForegroundColor); // Default white text
            }
        }

        // --- Background Color ---
        if (isSelected && !isDoneButton) {
            button.setBackground(buttonSelectedBackgroundColor); // Black background if selected
        } else if (!isDoneButton){
             // Reset class button background to normal if not selected
             button.setBackground(Color.decode(characterButtonColors[0]));
        }
        // Done button background handled by hover effect primarily
    }

    /** Iterates through all buttons and updates their appearance. */
    private static void updateAllButtonAppearances() {
         if (!isCharacterCreatorScreenActive) return;
        Component currentFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        for (Component c : buttonPanel.getComponents()) {
            if (c instanceof JButton) {
                updateButtonAppearance((JButton) c, c == currentFocusOwner); // Update based on focus/selection
            }
        }
    }

    /** Adds mouse hover effects (currently only affects Done button background). */
    private static void addHoverEffect(JButton button, String screenContext,
                                        final Map<String, String> normalBgColors,
                                        final Map<String, String> hoverBgColors) {
        // Only apply hover effect logic needed for character creator Done button
        if (!screenContext.equals("characterCreator")) return;

        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                 if (!isCharacterCreatorScreenActive) return;
                 // If the button isn't already focused, request focus on hover
                 if (!button.hasFocus()) {
                      button.requestFocusInWindow();
                 }
                 // Special background hover for Done button
                 if (button.getText().equals(doneButtonLabel)) {
                     button.setBackground(Color.decode(hoverBgColors.get(doneButtonLabel)));
                 }
                 // Note: Focus listener handles foreground color changes on hover for class buttons
            }
            @Override public void mouseExited(MouseEvent e) {
                 if (!isCharacterCreatorScreenActive) return;
                 // Reset Done button background on exit
                 if (button.getText().equals(doneButtonLabel)) {
                     button.setBackground(Color.decode(normalBgColors.get(doneButtonLabel)));
                 }
                 // Note: Focus listener handles foreground color reset when focus is lost
            }
        });
    }

    /** Loads a character image for the given class name. Selects randomly if multiple exist. */
    private static ImageIcon loadCharacterImage(String className) {
        if (className == null || className.trim().isEmpty() || className.equals(doneButtonLabel)) {
            return null; // No image for null, empty, or "Done"
        }

        // Try to find images named like: images/character-<set>-<class><number>.jpg
        List<URL> foundImageURLs = new ArrayList<>();
        String baseImagePath = "images/character-" + RPG.currentImageSet + "-" + className.toLowerCase();

        // Look for numbered images (e.g., -knight1.jpg, -knight2.jpg)
        for (int i = 1; i <= 5; i++) { // Check for up to 5 numbered images
            String imagePath = baseImagePath + i + ".jpg";
            try {
                URL imageURL = UIManagement.class.getClassLoader().getResource(imagePath);
                if (imageURL != null) {
                    foundImageURLs.add(imageURL);
                }
            } catch (Exception e) { /* Ignore exceptions during resource checking */ }
        }

        // If no numbered images found, look for a single base image (e.g., -knight.jpg)
        if (foundImageURLs.isEmpty()) {
            String imagePath = baseImagePath + ".jpg";
             try {
                 URL imageURL = UIManagement.class.getClassLoader().getResource(imagePath);
                 if (imageURL != null) {
                     foundImageURLs.add(imageURL);
                 }
            } catch (Exception e) { /* Ignore */ }
        }

        // If images were found, pick one randomly and load it
        if (!foundImageURLs.isEmpty()) {
            URL selectedURL = foundImageURLs.get(random.nextInt(foundImageURLs.size()));
            try {
                ImageIcon icon = new ImageIcon(selectedURL);
                // Check if the image loaded correctly
                if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                    return icon;
                } else {
                     System.err.println("Image load status not complete for: " + selectedURL);
                     return null; // Image data wasn't fully loaded
                }
            } catch (Exception e) {
                System.err.println("Exception loading image icon from URL " + selectedURL + ": " + e.getMessage());
                return null; // Error during loading
            }
        } else {
            System.err.println("No image found for class: " + className + " with image set: " + RPG.currentImageSet);
            return null; // No suitable image found
        }
    }


    /** Updates the character image and details pane based on the focused/selected class. */
    private static void updateCharacterImageAndDetails(String label) {
         if (!isCharacterCreatorScreenActive) return; // Only run on char create

         ImageIcon iconToShow = null;
         String classForDetails = label; // Start with the label passed (usually from focus gain)
         String randomNameToDisplay = null;

        // Determine the actual class to display (could be selected, or focused)
        if (classForDetails == null) classForDetails = selectedClass; // If label is null, use selected class
        if (doneButtonLabel.equals(classForDetails)) classForDetails = selectedClass; // If focus is on Done, show selected

        // Check if it's a valid class button label
        if (classForDetails != null && characterButtonMap.containsKey(classForDetails))
        {
            // --- Image Loading/Caching ---
            boolean needNewImage = !classForDetails.equals(lastDisplayedClass) || !currentIconForClass.containsKey(classForDetails);
            if (needNewImage) {
                 iconToShow = loadCharacterImage(classForDetails);
                 currentIconForClass.put(classForDetails, iconToShow); // Cache the loaded icon (or null)
            } else {
                 iconToShow = currentIconForClass.get(classForDetails); // Use cached icon
            }

            // --- Name Loading/Caching ---
             boolean needNewName = !currentRandomNameMap.containsKey(classForDetails);
             if (needNewName) {
                 randomNameToDisplay = CharacterClassInfo.getRandomName(classForDetails);
                 currentRandomNameMap.put(classForDetails, randomNameToDisplay); // Cache the name
             } else {
                  randomNameToDisplay = currentRandomNameMap.get(classForDetails); // Use cached name
             }
             lastDisplayedClass = classForDetails; // Update the last class fully processed
         } else {
             // No valid class selected or focused, show defaults
             iconToShow = null;
             randomNameToDisplay = null;
             lastDisplayedClass = null; // Reset last displayed
             classForDetails = null; // Ensure classForDetails is null for HTML generation
         }

         // --- Update Image Label ---
         if (iconToShow != null) {
             characterImageLabel.setIcon(iconToShow);
             characterImageLabel.setText(""); // Clear any placeholder text
         } else {
             characterImageLabel.setIcon(null);
             characterImageLabel.setText("<html><center>Select a Class<br>(Image not found?)</center></html>");
             characterImageLabel.setForeground(Color.DARK_GRAY);
         }
         characterImageLabel.revalidate();
         characterImageLabel.repaint();

         // --- Update Details Pane ---
         String detailsHtml;
         if (classForDetails != null) {
             CharacterClassInfo.BaseStats stats = CharacterClassInfo.getStats(classForDetails);
             if (stats != null) {
                 // Build HTML string for stats table
                 StringBuilder htmlBuilder = new StringBuilder("<html><body>");
                 htmlBuilder.append("<h2 class='classname'>").append(classForDetails).append("</h2>");
                 htmlBuilder.append("<table>");
                 htmlBuilder.append("<tr><th>Stat</th><th>Value</th></tr>");
                 htmlBuilder.append("<tr><td>Max HP</td><td>").append(stats.MAX_HP).append("</td></tr>");
                 htmlBuilder.append("<tr><td>Max Energy</td><td>").append(stats.MAX_ENERGY).append("</td></tr>");
                 htmlBuilder.append("<tr><td>Vigor</td><td>").append(stats.VIG).append("</td></tr>");
                 htmlBuilder.append("<tr><td>Defense</td><td>").append(stats.DEF).append("</td></tr>");
                 htmlBuilder.append("<tr><td>Strength</td><td>").append(stats.STR).append("</td></tr>");
                 htmlBuilder.append("<tr><td>Dexterity</td><td>").append(stats.DEX).append("</td></tr>");
                 htmlBuilder.append("<tr><td>Agility</td><td>").append(stats.AGT).append("</td></tr>");
                 htmlBuilder.append("<tr><td>Luck</td><td>").append(stats.LUCK).append("</td></tr>");
                 htmlBuilder.append("</table>");
                 if (randomNameToDisplay != null) {
                     htmlBuilder.append("<p class='charname'>").append(randomNameToDisplay).append("</p>");
                 }
                 htmlBuilder.append("</body></html>");
                 detailsHtml = htmlBuilder.toString();
             } else {
                 detailsHtml = "<html><body><h3>Error</h3><p>Class stats not found for " + classForDetails + ".</p></body></html>";
             }
         } else {
             // Default message when no class is selected/focused
             detailsHtml = "<html><body><h3 style='text-align:center;'>Class Details</h3><p style='text-align:center;'>Hover over or use arrow keys to select a class.<br>Press Space or Enter to confirm.</p></body></html>";
         }

         classDetailsPane.setText(detailsHtml);
         classDetailsPane.setCaretPosition(0); // Scroll to top
         characterDescPanel.revalidate(); // Revalidate container panel
         characterDescPanel.repaint();
    }

} // End of UIManagement class