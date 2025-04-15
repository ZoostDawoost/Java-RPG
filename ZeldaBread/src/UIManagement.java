/**
 * @file    UIManagement.java
 * @brief   Manages the main game window (JFrame), UI panels (using CardLayout), button actions, key bindings, and screen transitions. Handles dynamic character image loading and display during class selection, including name generation. Initializes game state after character creation. Instantiates StatusPanel and DialoguePanel.
 *
 * Sets up the overall graphical user interface, handles user input via buttons and keyboard (arrow keys, F8, character screen keys),
 * orchestrates player creation (including selecting and storing character images and names), map generation, and starts the game screen.
 *
 * @author  Jack Schulte & AI Assistant
 * @version 1.2.3 (Modified)
 * @date    2025-04-15
 *
 * @copyright Copyright (c) 2025 Jack Schulte & AI Assistant. All rights reserved.
 * Strictly confidential and proprietary. Distribution, reproduction,
 * or modification is strictly prohibited without prior written permission.
 *
 * @version History:
 * - 1.2.3 (2025-04-15): Changed highlightFocusedButton access modifier to public static to fix compilation error from FocusAdapter. (Gemini)
 * - 1.2.2 (2025-04-15): Added foreground text color change on keyboard focus gain/loss to match mouse hover highlight behavior. Updated comments. (Gemini)
 * - 1.2.1 (2025-04-15): Synchronized keyboard focus behavior with mouse hover for updating character info panel (image/details). Updated comments. (Gemini)
 * - 1.2.0 (2025-04-15): Implemented keyboard navigation (Arrows, Space, Enter) for character creation buttons. Removed "Hovering..." message. Added sound effect for invalid "Done" click. Updated comments. (Gemini)
 * - 1.1.1 (2025-04-14): Further increased font size for class title/name. Set minimum window size to initial size (1200x800). (AI Assistant)
 * - 1.1.0 (2025-04-14): Increased font size for class title and character name in the character details panel. (AI Assistant)
 * - 1.0.9 (2025-04-14): Moved Player Info label to top-right. Updated character creator panel for better image scaling. Integrated CharacterClassInfo for stats and random name generation/display. Updated Player Info label during gameplay to show name. (AI Assistant)
 * - 1.0.8 (2025-04-14): Implemented "sticky" hover effect on character class buttons before the first selection. Hover state persists until a class is clicked. (AI Assistant)
 * - 1.0.7 (2025-04-14): Implemented dynamic character image loading based on selected set/class. Added logic to find, randomly select, display, scale, and store the chosen ImageIcon during character creation. Replaced static image with dynamic JLabel updates. (AI Assistant)
 * - 1.0.6 (2025-04-14): Refactored Status and Dialogue panels into separate classes (StatusPanel, DialoguePanel). Moved panels to right side. Updated navigation text with arrow symbols. Verified initial HP/Energy update. (AI Assistant)
 * - 1.0.5 (2025-04-14): Corrected updateDescription to use public static getters from RPG for class stats (now obsolete). (AI Assistant)
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
import java.awt.event.*; // Import all AWT events
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class UIManagement {

    // UI Components
    public static JFrame frame = new JFrame();
    private static JPanel topPanel = new JPanel(new BorderLayout(10, 0)); // Use BorderLayout
    private static JLabel playerInfoLabel = new JLabel("", SwingConstants.RIGHT); // Right aligned
    private static JLabel titleLabel = new JLabel("", SwingConstants.CENTER);
    private static JPanel centerPanelContainer = new JPanel(new BorderLayout());
    private static JPanel rightPanel = new JPanel(new BorderLayout());
    private static StatusPanel statusPanel;
    private static DialoguePanel dialoguePanel;
    private static JPanel centerPanel = new JPanel(new CardLayout());
    private static WelcomePanel welcomePanel;
    private static JPanel characterDescPanel = new JPanel(new BorderLayout(10, 0)); // Image CENTER, Details EAST
    private static JLabel characterImageLabel = new JLabel() { // Override paintComponent for scaling
         @Override
        protected void paintComponent(Graphics g) {
            ImageIcon icon = (ImageIcon) getIcon();
            if (icon != null) {
                Image img = icon.getImage();
                // Calculate the scaling factor to fit the label bounds while preserving aspect ratio
                float scaleFactor = Math.min(1f, Math.min((float) getWidth() / img.getWidth(null), (float) getHeight() / img.getHeight(null)));
                int scaledWidth = (int) (img.getWidth(null) * scaleFactor);
                int scaledHeight = (int) (img.getHeight(null) * scaleFactor);
                // Center the image within the label
                int x = (getWidth() - scaledWidth) / 2;
                int y = (getHeight() - scaledHeight) / 2;

                Graphics2D g2d = (Graphics2D) g.create();
                // Optional: Use higher quality rendering hints
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(img, x, y, scaledWidth, scaledHeight, this);
                g2d.dispose();
            } else {
                // If no icon, call superclass to draw text (like "Select a Class")
                super.paintComponent(g);
            }
        }
    }; // Will display scaled character class image
    private static JEditorPane classDetailsPane = new JEditorPane();
    private static MapPanel mapPanel;
    private static JPanel bottomPanel = new JPanel(new BorderLayout());
    private static JLabel commentLabel = new JLabel("", SwingConstants.CENTER);
    private static JPanel buttonPanel = new JPanel(); // Holds character/move buttons
    private static GridBagConstraints gbc = new GridBagConstraints();


    // Fonts and Colors
    private static Font titleFont = new Font("Trebuchet MS", Font.BOLD, 24);
    private static Font playerInfoFont = new Font("Trebuchet MS", Font.BOLD, 18); // Increased size
    private static Font commentFont = new Font("Trebuchet MS", Font.ITALIC, 18);
    private static Font buttonFont = new Font("Aharoni", Font.BOLD, 20);
    public static Color backgroundColor = Color.decode("#EEEEEE");
    private static Color characterDetailBackgroundColor = Color.decode("#F5F5DC");
    private static Color buttonDefaultForegroundColor = Color.WHITE;
    private static Color buttonHoverForegroundColor = Color.RED; // Red highlight for hover/focus
    private static Color buttonSelectedBackgroundColor = Color.BLACK;
    private static Color buttonSelectedForegroundColor = Color.WHITE; // White text for selected


    // Game State Object
    private static RPG rpg; // Central map object

    // Character Selection Specific
    private static String[] characterButtonLabels = {"Knight", "Sentinel", "Assassin", "Wizard", "Caveman"};
    private static String[] characterButtonColors = {"#48AD48", "#58D558"}; // Normal, Hover(unused)
    private static String doneButtonLabel = "Done";
    private static String[] doneButtonColors = {"#EECC44", "#F7DD77"}; // Normal, Hover BG
    private static int[] characterButtonLengths = new int[]{140, 140, 140, 140, 140, 100}; // Widths
    private static Map<String, JButton> characterButtonMap = new HashMap<>();
    private static JButton doneButton = null; // Reference to the Done button
    private static String selectedClass = null; // Confirmed selected class
    private static Map<String, ImageIcon> currentIconForClass = new HashMap<>();
    private static String lastDisplayedClass = null; // Tracks image/details shown
    private static Random random = new Random();
    private static boolean stickyHoverActive = true; // Controls initial hover behavior
    private static JButton currentlyStickyHoveredButton = null; // Tracks button for sticky hover
    private static Map<String, String> currentRandomNameMap = new HashMap<>(); // Stores the random name currently associated with a class display


    private static LineBorder defaultBorder = new LineBorder(Color.GRAY, 1);
    private static LineBorder focusBorder = new LineBorder(Color.BLUE, 2); // Border for focused button

    // Gameplay Specific
    private static String[] moveButtonLabels = {"Turn Left", "Forward", "Turn Right", "Backward"};
    private static String[] moveButtonColors = {"#44AADD", "#66CCFF"};
    private static boolean isGameScreenActive = false;
    private static boolean isCharacterCreatorScreenActive = false; // Flag for specific key bindings
    private static final String LEFT_ARROW = "\u2190";
    private static final String UP_ARROW = "\u2191";
    private static final String RIGHT_ARROW = "\u2192";
    private static final String DOWN_ARROW = "\u2193";


    // Method to set up the main UI structure
    public static void setupUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Legend of Splenda: Bread of the Wild");

        // Set initial size
        Dimension initialSize = new Dimension(1200, 800);
        frame.setSize(initialSize);

        // Set minimum size AFTER setting initial size to prevent resizing smaller
        frame.setMinimumSize(initialSize);
        System.out.println("Frame initial size and minimum size set to: " + initialSize.width + "x" + initialSize.height);

        frame.setLayout(new BorderLayout());

        // --- Top Panel ---
        playerInfoLabel.setFont(playerInfoFont);
        playerInfoLabel.setOpaque(true);
        playerInfoLabel.setBackground(backgroundColor);
        playerInfoLabel.setBorder(new EmptyBorder(5, 10, 5, 10)); // Padding

        titleLabel.setFont(titleFont);
        titleLabel.setOpaque(true);
        titleLabel.setForeground(Color.decode("#B6220E"));
        titleLabel.setBackground(backgroundColor);
        titleLabel.setBorder(new EmptyBorder(5, 10, 5, 10));

        // Add title to CENTER, player info to EAST (pushes it right)
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(playerInfoLabel, BorderLayout.EAST);
        topPanel.setBackground(backgroundColor);
        topPanel.setVisible(false); // Initially hidden

        // --- Right Side Panels (Status/Dialogue - unchanged) ---
        statusPanel = new StatusPanel(); dialoguePanel = new DialoguePanel();
        rightPanel.setPreferredSize(new Dimension(250, 0));
        rightPanel.add(statusPanel, BorderLayout.NORTH);
        rightPanel.add(dialoguePanel, BorderLayout.CENTER);
        rightPanel.setBackground(backgroundColor);
        rightPanel.setVisible(false); // Initially hidden

        // --- Center Panel (CardLayout: Welcome, Character Creator, Map) ---
        welcomePanel = new WelcomePanel();
        centerPanel.add(welcomePanel, "welcome");

        // --- Character Creator Panel Setup (Revised Layout) ---
        characterImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        characterImageLabel.setVerticalAlignment(SwingConstants.CENTER);
        characterImageLabel.setBackground(backgroundColor); // Or a distinct background
        characterImageLabel.setOpaque(true);

        classDetailsPane.setEditable(false);
        classDetailsPane.setContentType("text/html");
        classDetailsPane.setBackground(characterDetailBackgroundColor);
        classDetailsPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        HTMLEditorKit kit = new HTMLEditorKit();
        classDetailsPane.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body {font-family:\"Trebuchet MS\"; font-size: 11pt; margin: 5px;}");
        styleSheet.addRule("h2.classname { margin-top: 10px; margin-bottom: 5px; text-align: center; font-size: 20pt; font-weight: bold;}"); // Increased class name size further
        styleSheet.addRule("table { border-collapse: collapse; margin: 10px auto; width: 90%; }");
        styleSheet.addRule("th, td { border: 1px solid #cccccc; padding: 5px; text-align: left; }");
        styleSheet.addRule("th { background-color: #e0e0e0; text-align: center; }");
        styleSheet.addRule("p.charname { text-align: center; font-weight: bold; margin-top: 15px; font-size: 18pt;}"); // Increased character name size further

        JScrollPane detailsScrollPane = new JScrollPane(classDetailsPane);
        detailsScrollPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        detailsScrollPane.setPreferredSize(new Dimension(320, 0)); // Give details a fixed width

        characterDescPanel.setBackground(backgroundColor);
        characterDescPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        characterDescPanel.add(characterImageLabel, BorderLayout.CENTER);
        characterDescPanel.add(detailsScrollPane, BorderLayout.EAST);

        centerPanel.add(characterDescPanel, "characterCreator");
        // --- End Character Creator Panel Setup ---

        centerPanel.setBackground(backgroundColor);

        // Center Container (Holds center panel and right gameplay panels)
        centerPanelContainer.add(centerPanel, BorderLayout.CENTER);
        centerPanelContainer.add(rightPanel, BorderLayout.EAST); // Status/Dialogue
        centerPanelContainer.setBackground(backgroundColor);

        // --- Bottom Panel (Comment/Buttons - unchanged structure) ---
        commentLabel.setFont(commentFont); commentLabel.setBackground(backgroundColor); commentLabel.setOpaque(true);
        buttonPanel.setBackground(backgroundColor); buttonPanel.setLayout(new GridBagLayout());
        // Allow buttonPanel to receive focus for keyboard navigation
        buttonPanel.setFocusable(true); // <-- Important for key bindings on panel

        gbc.insets = new Insets(5, 10, 5, 10); gbc.gridy = 0;
        bottomPanel.setBackground(backgroundColor); bottomPanel.add(commentLabel, BorderLayout.NORTH); bottomPanel.add(buttonPanel, BorderLayout.CENTER); bottomPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Final Frame Assembly
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(centerPanelContainer, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        // Setup Key Bindings (Gameplay and Character Creator)
        setupKeyBindings(); // Add general gameplay keys
        setupCharacterCreatorKeyBindings(); // Add character creation specific keys

        // Show Welcome Screen initially (Called from main after centering)
    }


    // ==============================================================================================
    // =================== Screen Management & Game Initialization ==================================

    // Switches the view in the center panel
    private static void showScreen(String screenName) {
        if (screenName.equals("map") && mapPanel == null) {
             System.err.println("Attempted to switch to map screen, but mapPanel is null!");
             JOptionPane.showMessageDialog(frame, "Error: Map Panel not ready.", "Error", JOptionPane.ERROR_MESSAGE);
             return;
        }
        CardLayout cl = (CardLayout) (centerPanel.getLayout());
        cl.show(centerPanel, screenName);

        // Update screen state flags
        isGameScreenActive = screenName.equals("map");
        isCharacterCreatorScreenActive = screenName.equals("characterCreator");
        System.out.println("Switched to screen: " + screenName + ", isGameScreenActive: " + isGameScreenActive + ", isCharacterCreatorScreenActive: " + isCharacterCreatorScreenActive);

        // Panel Visibility
        topPanel.setVisible(!screenName.equals("welcome"));
        rightPanel.setVisible(isGameScreenActive); // Only show status/dialogue during gameplay
        bottomPanel.setVisible(!screenName.equals("welcome"));

        // Screen specific setup
        if (isGameScreenActive) {
            commentLabel.setText("Use buttons or " + LEFT_ARROW + " " + UP_ARROW + " " + RIGHT_ARROW + " " + DOWN_ARROW + " keys. Press F8 to toggle map.");
            if (RPG.getPlayers() != null && !RPG.getPlayers().isEmpty()) {
                 updateGameStatus(RPG.getPlayers().get(0));
            }
            frame.getRootPane().requestFocusInWindow(); // Focus for gameplay key bindings
        } else if (isCharacterCreatorScreenActive){
            commentLabel.setText("Select a class using mouse or arrow keys & Space/Enter."); // Updated prompt
            lastDisplayedClass = null;
            currentIconForClass.clear();
            currentRandomNameMap.clear();
            stickyHoverActive = true;
            currentlyStickyHoveredButton = null;
            updateCharacterImageAndDetails(null);

            // Request focus for the button panel for character creator keys
            SwingUtilities.invokeLater(() -> { // Ensure it happens after UI updates
                 buttonPanel.requestFocusInWindow();
                 // Optionally set initial focus to the first button
                 if (buttonPanel.getComponentCount() > 0 && buttonPanel.getComponent(0) instanceof JButton) {
                      JButton firstButton = (JButton) buttonPanel.getComponent(0);
                      firstButton.requestFocusInWindow();
                      // Focus listener on the button will handle the initial highlight and panel update
                 }
            });

        } else { // Welcome screen
             commentLabel.setText("");
             playerInfoLabel.setText("");
             titleLabel.setText("");
             buttonPanel.removeAll(); buttonPanel.revalidate(); buttonPanel.repaint(); // Clear buttons
        }

        // Revalidate frame after visibility changes
        frame.revalidate();
        frame.repaint();
    }

    // Initial welcome screen setup (Unchanged)
    public static void welcomeScreen() { showScreen("welcome"); }

    // Screen for character class selection
    public static void characterCreatorScreen() {
        selectedClass = null;
        characterButtonMap.clear();
        doneButton = null; // Reset done button reference

        titleLabel.setText("Create your Character");
        playerInfoLabel.setText("Player " + (RPG.getPlayerIndex() + 1));
        topPanel.setVisible(true);

        // --- Create Buttons ---
        ArrayList<String> buttonConfigList = new ArrayList<>(); ArrayList<Integer> lengthConfigList = new ArrayList<>();
        for (int i = 0; i < characterButtonLabels.length; i++) {
            String label = characterButtonLabels[i];
            buttonConfigList.add(label); buttonConfigList.add(characterButtonColors[0]); buttonConfigList.add(characterButtonColors[0]); // Normal BG, Hover BG (unused by logic now)
            lengthConfigList.add(characterButtonLengths[i]);
        }
        buttonConfigList.add(doneButtonLabel); buttonConfigList.add(doneButtonColors[0]); buttonConfigList.add(doneButtonColors[1]); // Done Normal, Done Hover BG
        lengthConfigList.add(characterButtonLengths[characterButtonLengths.length - 1]);

        makeButtons(buttonConfigList.toArray(new String[0]), lengthConfigList.stream().mapToInt(i -> i).toArray(), "characterCreator");
        // --- End Create Buttons ---

        showScreen("characterCreator"); // This will request focus after showing
        bottomPanel.setVisible(true);
    }


    // --- Central Game Initialization Logic --- (Unchanged)
    private static void initializeAndStartGame() {
        System.out.println("All players created. Initializing game world...");
        rpg = new RPG();
        rpg.buildMap();
        rpg.addEventsToRooms();

        if (!RPG.getPlayers().isEmpty()) {
            RPG player0 = RPG.getPlayers().get(0);
            int startRow = rpg.map.length / 2; int startCol = rpg.map[0].length / 2;
            System.out.println("Setting initial state for Player 0 at ["+startRow+","+startCol+"]");
            player0.setCurrentPos(new int[]{startRow, startCol});
            player0.setWayFacing(0);
            player0.getVisitedMap()[startRow][startCol] = true;
            player0.exploreAround(startRow, startCol, rpg.map);

            if (dialoguePanel != null) { dialoguePanel.clearDialogue(); }
            addDialogue("Welcome, " + player0.getCharacterName() + " the " + player0.getClassName() + "!");
            addDialogue("You awaken in the " + RPG.getRoomDescription(rpg.map[startRow][startCol]) + ".");
            updateGameStatus(player0);
        } else {
            System.err.println("ERROR: No players found!");
            JOptionPane.showMessageDialog(frame, "Error: Player data not found.", "Initialization Error", JOptionPane.ERROR_MESSAGE);
            welcomeScreen(); return;
        }

        System.out.println("Creating Map Panel...");
        mapPanel = new MapPanel(rpg);
        centerPanel.add(mapPanel, "map");

        System.out.println("Starting game screen...");
        startGameScreen();
        System.out.println("Game initialization complete.");
    }

    // Sets up the UI elements for the main game screen (Unchanged)
    private static void startGameScreen() {
        if (mapPanel == null || rpg == null || RPG.getPlayers().isEmpty()) {
            System.err.println("Cannot start game screen - essential components missing!");
            JOptionPane.showMessageDialog(frame, "Error: Game components not ready.", "Error", JOptionPane.ERROR_MESSAGE);
            welcomeScreen(); return;
        }
        RPG player = RPG.getPlayers().get(0);
        titleLabel.setText("Dungeon Exploration");
        updatePlayerInfoLabel(player);
        commentLabel.setText("Use buttons or " + LEFT_ARROW + " " + UP_ARROW + " " + RIGHT_ARROW + " " + DOWN_ARROW + " keys. Press F8 to toggle map.");

        ArrayList<String> buttonConfigList = new ArrayList<>(); ArrayList<Integer> lengthConfigList = new ArrayList<>();
        int buttonLength = 110;
        for (String label : moveButtonLabels) {
            buttonConfigList.add(label); buttonConfigList.add(moveButtonColors[0]); buttonConfigList.add(moveButtonColors[1]); lengthConfigList.add(buttonLength);
        }
        makeButtons(buttonConfigList.toArray(new String[0]), lengthConfigList.stream().mapToInt(i -> i).toArray(), "gameplay");

        showScreen("map");
        mapPanel.refreshMap();
        bottomPanel.setVisible(true);
        frame.getRootPane().requestFocusInWindow();
    }


    // ==============================================================================================
    // =================== Action Listeners =========================================================

    // Assigns appropriate action listeners based on the button label and screen context (Unchanged from previous)
    private static void pickActionListener(JButton button, String buttonLabel, String screenContext) {
        for (java.awt.event.ActionListener al : button.getActionListeners()) { button.removeActionListener(al); } // Remove previous

        switch (screenContext) {
            case "characterCreator":
                boolean isClassButton = characterButtonMap.containsKey(buttonLabel);
                if (isClassButton) {
                    button.addActionListener(e -> { // Click Class Button
                        handleClassSelection(buttonLabel); // Refactored selection logic
                    });
                } else if (buttonLabel.equals(doneButtonLabel)) {
                    button.addActionListener(e -> { // Click Done Button
                        if (selectedClass == null) {
                            // *** Play sound and show message if no class is selected ***
                            RPG.playAudio("audio/action-wrong.wav");
                            commentLabel.setText("Please select a class first!");
                            updateCharacterImageAndDetails(null); // Show prompt
                        } else {
                            // --- Proceed with player creation ---
                            int currentPlayerIndex = RPG.getPlayerIndex();
                            RPG player = RPG.getPlayers().get(currentPlayerIndex);

                            // 1. Assign Class Stats
                            RPG.assignClass(currentPlayerIndex, selectedClass);

                            // 2. Assign Character Name (using the name displayed when selected)
                            String finalName = currentRandomNameMap.get(selectedClass);
                            if (finalName == null) { // Safety fallback
                                finalName = CharacterClassInfo.getRandomName(selectedClass);
                            }
                            player.setCharacterName(finalName);
                            System.out.println("Assigned name: " + finalName + " to Player " + (currentPlayerIndex + 1));

                            // 3. Assign Character Icon
                            ImageIcon finalIcon = currentIconForClass.get(selectedClass);
                            if (finalIcon != null) {
                                player.setSelectedCharacterIcon(finalIcon);
                                System.out.println("Stored icon for Player " + (currentPlayerIndex + 1) + " (" + selectedClass + ")");
                            } else {
                                System.err.println("Warning: Could not find cached icon for " + selectedClass + " to store.");
                            }

                            RPG.modPlayerIndex(); // Move to next player

                            if (RPG.getPlayerIndex() < RPG.getNumPlayers()) {
                                characterCreatorScreen(); // Next player
                            } else {
                                initializeAndStartGame(); // Start game
                            }
                        }
                    });
                }
                break;
            case "gameplay": // Gameplay Actions (unchanged)
                switch (buttonLabel) {
                    case "Turn Left":  button.addActionListener(e1 -> handleTurnLeft()); break;
                    case "Forward":    button.addActionListener(e1 -> handleMoveForward()); break;
                    case "Turn Right": button.addActionListener(e1 -> handleTurnRight()); break;
                    case "Backward":   button.addActionListener(e1 -> handleMoveBackward()); break;
                }
                break;
        }
    }

    // Helper method to handle the logic when a character class button is selected (click or via Space key) (Unchanged from previous)
    private static void handleClassSelection(String buttonLabel) {
         selectedClass = buttonLabel;
         lastDisplayedClass = buttonLabel; // Sync display tracker
         stickyHoverActive = false; // Turn off sticky hover
         currentlyStickyHoveredButton = null;
         commentLabel.setText(buttonLabel + " selected. Press Enter or click 'Done'."); // Updated prompt

         // Update button appearances for all buttons
         updateAllButtonAppearances(); // Update visual state of all buttons based on new selection

         // Update image and details (uses cached name/icon)
         updateCharacterImageAndDetails(selectedClass);
    }

    // Helper to update top-right player info label during gameplay (Unchanged)
    private static void updatePlayerInfoLabel(RPG player) {
        if (player == null || !isGameScreenActive) {
            playerInfoLabel.setText("");
            return;
        }
        playerInfoLabel.setText(player.getCharacterName() + " (" + player.getClassName() + ")");
    }

    // ==============================================================================================
    // =================== Gameplay Action Handlers ================================================
    // (handleTurnLeft, handleTurnRight, handleMoveForward, handleMoveBackward, handleToggleMap remain UNCHANGED)
    private static void handleTurnLeft() { if (!isGameScreenActive || mapPanel == null || RPG.getPlayers().isEmpty()) return; RPG player = RPG.getPlayers().get(0); player.turnPlayer(0); updateGameStatus(player); frame.getRootPane().requestFocusInWindow(); }
    private static void handleTurnRight() { if (!isGameScreenActive || mapPanel == null || RPG.getPlayers().isEmpty()) return; RPG player = RPG.getPlayers().get(0); player.turnPlayer(1); updateGameStatus(player); frame.getRootPane().requestFocusInWindow(); }
    private static void handleMoveForward() { if (!isGameScreenActive || mapPanel == null || rpg == null || RPG.getPlayers().isEmpty()) return; RPG player = RPG.getPlayers().get(0); if (player.movePlayer(player.getWayFacing(), rpg.map)) {} frame.getRootPane().requestFocusInWindow(); }
    private static void handleMoveBackward() { if (!isGameScreenActive || mapPanel == null || rpg == null || RPG.getPlayers().isEmpty()) return; RPG player = RPG.getPlayers().get(0); int oppositeDirection = (player.getWayFacing() + 2) % 4; if (player.movePlayer(oppositeDirection, rpg.map)) {} frame.getRootPane().requestFocusInWindow(); }
    private static void handleToggleMap() { if (!isGameScreenActive || mapPanel == null) return; RPG.toggleMapVisibility(); if (!RPG.getPlayers().isEmpty()) { updateGameStatus(RPG.getPlayers().get(0)); } commentLabel.setText("Map view toggled (Show full: " + RPG.isShowFullMap() + ")"); frame.getRootPane().requestFocusInWindow(); }


    // ==============================================================================================
    // =================== Key Binding Setup ========================================================

    // Setup general GAMEPLAY key bindings (F8, arrows for movement) (Unchanged)
    private static void setupKeyBindings() {
        InputMap inputMap = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = frame.getRootPane().getActionMap();

        // --- Gameplay Keys ---
        KeyStroke upKey = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
        KeyStroke downKey = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
        KeyStroke leftKey = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
        KeyStroke rightKey = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
        KeyStroke f8Key = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);

        inputMap.put(upKey, "gameplayMoveForward");
        inputMap.put(downKey, "gameplayMoveBackward");
        inputMap.put(leftKey, "gameplayTurnLeft");
        inputMap.put(rightKey, "gameplayTurnRight");
        inputMap.put(f8Key, "gameplayToggleMap");

        actionMap.put("gameplayMoveForward", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { if(isGameScreenActive) handleMoveForward(); } });
        actionMap.put("gameplayMoveBackward", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { if(isGameScreenActive) handleMoveBackward(); } });
        actionMap.put("gameplayTurnLeft", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { if(isGameScreenActive) handleTurnLeft(); } });
        actionMap.put("gameplayTurnRight", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { if(isGameScreenActive) handleTurnRight(); } });
        actionMap.put("gameplayToggleMap", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { if(isGameScreenActive) handleToggleMap(); } });
    }

    // Setup CHARACTER CREATOR specific key bindings (Arrows, Space, Enter) (Unchanged from previous)
    private static void setupCharacterCreatorKeyBindings() {
        InputMap inputMap = buttonPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = buttonPanel.getActionMap();

        KeyStroke leftKey = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
        KeyStroke rightKey = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
        KeyStroke spaceKey = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

        // --- Arrow Key Navigation ---
        inputMap.put(leftKey, "navigateLeft");
        inputMap.put(rightKey, "navigateRight");

        actionMap.put("navigateLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isCharacterCreatorScreenActive) navigateButtons(-1);
            }
        });
        actionMap.put("navigateRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 if (isCharacterCreatorScreenActive) navigateButtons(1);
            }
        });

        // --- Space Bar Activation ---
        inputMap.put(spaceKey, "activateFocusedButton");
        actionMap.put("activateFocusedButton", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isCharacterCreatorScreenActive) {
                    Component focusedComponent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                    if (focusedComponent instanceof JButton && buttonPanel.isAncestorOf(focusedComponent)) {
                        ((JButton) focusedComponent).doClick();
                    }
                }
            }
        });

        // --- Enter Key for "Done" ---
        inputMap.put(enterKey, "activateDoneButton");
        actionMap.put("activateDoneButton", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isCharacterCreatorScreenActive && doneButton != null) {
                    doneButton.doClick();
                }
            }
        });
    }

    // Helper for arrow key navigation between buttons in buttonPanel (Unchanged from previous)
    private static void navigateButtons(int direction) {
        Component currentFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (buttonPanel.getComponentCount() == 0) return; // No buttons

        ArrayList<Component> buttons = new ArrayList<>();
        for(Component comp : buttonPanel.getComponents()){
             if(comp instanceof JButton){
                  buttons.add(comp);
             }
        }
        if (buttons.isEmpty()) return; // No JButtons found

        int currentIndex = -1;
        if (currentFocusOwner instanceof JButton && buttons.contains(currentFocusOwner)) {
            currentIndex = buttons.indexOf(currentFocusOwner);
        } else {
            // If focus is not on a button (or not on one of ours), start from the first/last
            currentIndex = (direction > 0) ? -1 : buttons.size();
        }

        int nextIndex = (currentIndex + direction + buttons.size()) % buttons.size(); // Calculate next index with wrap-around

        buttons.get(nextIndex).requestFocusInWindow();
        // Focus listener now handles highlight and panel update
    }

   // ==============================================================================================
   // =================== UI Update Methods ========================================================
   // (updateGameStatus, addDialogue remain UNCHANGED)
   public static void updateGameStatus(RPG player) { SwingUtilities.invokeLater(() -> { if (player == null || !isGameScreenActive) return; if (statusPanel != null) { statusPanel.updateStatus(player); } if (mapPanel != null) { mapPanel.refreshMap(); } updatePlayerInfoLabel(player); }); }
   public static void addDialogue(String text) { if (dialoguePanel != null) { dialoguePanel.addDialogue(text); } else { System.err.println("Attempted to add dialogue, but dialoguePanel is null!"); } }


    // ==============================================================================================
    // =================== UI Helpers (Button Creation, Hover Effects, Focus) =======================

    // Creates buttons and adds them to the bottom button panel
    // *** MODIFIED: FocusAdapter logic now includes foreground color changes ***
    // *** FIXED: Changed highlightFocusedButton to public static ***
     private static void makeButtons(String[] stringArray, int[] intArray, String screenContext) {
         buttonPanel.removeAll(); // Clear previous buttons
         if (screenContext.equals("characterCreator")) {
             characterButtonMap.clear();
             doneButton = null; // Reset done button reference
         }
         gbc.gridx = 0; // Reset grid position

         // Store the normal background colors for reference
         final Map<String, String> normalBgColors = new HashMap<>();
         final Map<String, String> hoverBgColors = new HashMap<>(); // For Done button hover effect
         for (int i = 0; i < stringArray.length / 3; i++) {
             String label = stringArray[i * 3];
             normalBgColors.put(label, stringArray[i * 3 + 1]);
             hoverBgColors.put(label, stringArray[i * 3 + 2]); // Store hover color too
         }


         for (int i = 0; i < stringArray.length / 3; i++) {
             String buttonLabel = stringArray[i * 3];
             JButton button = new JButton(buttonLabel);
             button.setPreferredSize(new Dimension(intArray[i], 50));
             // String normalColorHex = stringArray[i * 3 + 1]; // Use map lookup now
             // String hoverEffectInfo = stringArray[i * 3 + 2]; // Use map lookup now

             button.setBackground(Color.decode(normalBgColors.get(buttonLabel))); // Set initial BG
             button.setFont(buttonFont);
             button.setBorder(defaultBorder); // Start with default border
             button.setFocusPainted(false); // Disable default focus painting

             // Add listener to handle focus changes (border, text color, info panel)
             button.addFocusListener(new FocusAdapter() {
                 @Override
                 public void focusGained(FocusEvent e) {
                    if (!isCharacterCreatorScreenActive) return; // Only act on character screen
                    JButton gainedFocusButton = (JButton) e.getComponent();

                    // 1. Update Border (moved to updateButtonAppearance)
                    // highlightFocusedButton(gainedFocusButton); // Error reported here previously

                    // 2. Update Info Panel (like mouseEnter)
                    updateCharacterImageAndDetails(gainedFocusButton.getText());

                    // 3. Update Button Appearance (Border + Foreground Color)
                    updateButtonAppearance(gainedFocusButton, true); // Indicate it has focus
                 }
                 @Override
                 public void focusLost(FocusEvent e) {
                     if (!isCharacterCreatorScreenActive) return; // Only act on character screen
                      JButton lostFocusButton = (JButton) e.getComponent();

                      // 1. Revert Info Panel (like mouseExit)
                      updateCharacterImageAndDetails(selectedClass);

                      // 2. Update Button Appearance (Border + Foreground Color)
                      updateButtonAppearance(lostFocusButton, false); // Indicate it lost focus
                 }
             });

             // Set initial appearance based on screen context
             if (screenContext.equals("characterCreator")) {
                  // Determine initial colors based on selection state
                  boolean isSelected = buttonLabel.equals(selectedClass);
                  boolean isDone = buttonLabel.equals(doneButtonLabel);

                  if (isDone) {
                      button.setForeground(Color.BLACK); // Done button default text
                      doneButton = button; // Store reference
                  } else { // Class button
                      characterButtonMap.put(buttonLabel, button);
                      button.setForeground(isSelected ? buttonSelectedForegroundColor : buttonDefaultForegroundColor);
                      button.setBackground(isSelected ? buttonSelectedBackgroundColor : Color.decode(normalBgColors.get(buttonLabel)));
                  }

             } else { // Gameplay buttons
                 button.setForeground(Color.BLACK);
             }

             // Add simplified hover effect (mainly for Done button BG change)
             addHoverEffect(button, screenContext, normalBgColors, hoverBgColors);
             pickActionListener(button, buttonLabel, screenContext);

             buttonPanel.add(button, gbc); // Add button to panel
             gbc.gridx++; // Move to next grid cell
         }
         buttonPanel.revalidate();
         buttonPanel.repaint();
     }

    // *** FIXED: Changed access modifier to public static ***
    // Helper to set the border for the currently focused button and remove from others.
    public static void highlightFocusedButton(JButton focusedButton) {
        if (focusedButton == null || !isCharacterCreatorScreenActive) return;
        // Remove focus border from all other buttons in the panel
        for (Component comp : buttonPanel.getComponents()) {
             if (comp instanceof JButton && comp != focusedButton) {
                  // Only reset border if it's not the currently selected class
                  if (!((JButton) comp).getText().equals(selectedClass)) {
                       ((JButton) comp).setBorder(defaultBorder);
                  }
             }
        }
        // Apply focus border to the target button
        focusedButton.setBorder(focusBorder);
    }

    // Updates border and foreground based on focus and selection status.
    private static void updateButtonAppearance(JButton button, boolean hasFocus) {
       if (button == null || !isCharacterCreatorScreenActive) return; // Only act on character screen
       String buttonText = button.getText();
       boolean isSelected = buttonText.equals(selectedClass);
       boolean isDone = buttonText.equals(doneButtonLabel);

       // --- Set Border ---
       if (hasFocus) {
           button.setBorder(focusBorder);
       } else {
           // Selected button keeps its border even when losing focus temporarily
           if (!isSelected) {
                button.setBorder(defaultBorder);
           }
       }

       // --- Set Foreground Color ---
       if (isDone) {
           button.setForeground(Color.BLACK); // Done button always has black text
       } else { // Class Buttons
           if (isSelected) {
               button.setForeground(buttonSelectedForegroundColor); // Selected is always white text
           } else if (hasFocus) {
               button.setForeground(buttonHoverForegroundColor); // Focused (not selected) is red
           } else {
               button.setForeground(buttonDefaultForegroundColor); // Default (not focused, not selected) is white
           }
       }

       // --- Set Background Color --- (Only changes on selection or Done button hover)
       if (isSelected && !isDone) {
            button.setBackground(buttonSelectedBackgroundColor);
       } else if (!isDone) { // Unselected Class Buttons
           // Retrieve normal background color (requires map access or passing config)
           // For simplicity, assuming characterButtonColors[0] is the normal BG
           button.setBackground(Color.decode(characterButtonColors[0]));
       }
       // Note: Done button background hover is handled in addHoverEffect now.
    }

    // Helper to update all button appearances at once (e.g., after selection)
    private static void updateAllButtonAppearances() {
        Component currentFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        for (Component comp : buttonPanel.getComponents()) {
            if (comp instanceof JButton) {
                // Use the helper to update border and foreground based on focus and selection
                updateButtonAppearance((JButton) comp, comp == currentFocusOwner);
            }
        }
    }


    // Adds mouse hover effects to a button
    // *** MODIFIED: Simplified, mainly for Done button BG hover ***
    private static void addHoverEffect(JButton button, String screenContext, final Map<String, String> normalBgColors, final Map<String, String> hoverBgColors) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isCharacterCreatorScreenActive) return; // Only act on character screen

                // Request focus, the focus listener will handle appearance updates (border, text color)
                button.requestFocusInWindow();

                // Specific background hover for Done button
                if (button.getText().equals(doneButtonLabel)) {
                    button.setBackground(Color.decode(hoverBgColors.get(doneButtonLabel))); // Use hover BG for Done
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isCharacterCreatorScreenActive) return; // Only act on character screen

                // Focus listener (focusLost) handles reverting appearance if focus actually moves.
                // Only handle specific background reversion for Done button here.
                if (button.getText().equals(doneButtonLabel)) {
                    button.setBackground(Color.decode(normalBgColors.get(doneButtonLabel))); // Revert Done BG
                }
            }
        });
    }


    // --- Load Character Image --- (Unchanged)
     private static ImageIcon loadCharacterImage(String className) { if (className == null || className.trim().isEmpty()) return null; List<URL> foundImageUrls = new ArrayList<>(); String baseImagePath = "images/character-" + RPG.currentImageSet + "-" + className.toLowerCase(); for (int i = 1; i <= 5; i++) { String imagePath = baseImagePath + i + ".jpg"; try { URL imageURL = UIManagement.class.getClassLoader().getResource(imagePath); if (imageURL != null) { foundImageUrls.add(imageURL); System.out.println("Found image: " + imagePath); } } catch (Exception e) { System.err.println("Error checking for resource: " + imagePath + " - " + e.getMessage()); } } if (foundImageUrls.isEmpty()) { String imagePath = baseImagePath + ".jpg"; try { URL imageURL = UIManagement.class.getClassLoader().getResource(imagePath); if (imageURL != null) { foundImageUrls.add(imageURL); System.out.println("Found base image: " + imagePath); } } catch (Exception e) { System.err.println("Error checking for resource: " + imagePath + " - " + e.getMessage()); } } if (!foundImageUrls.isEmpty()) { URL selectedUrl = foundImageUrls.get(random.nextInt(foundImageUrls.size())); try { ImageIcon icon = new ImageIcon(selectedUrl); if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) { System.out.println("Successfully loaded image icon from: " + selectedUrl); return icon; } else { System.err.println("Error: ImageIcon status not complete for " + selectedUrl); return null; } } catch (Exception e) { System.err.println("Exception creating ImageIcon from URL: " + selectedUrl + " - " + e.getMessage()); return null; } } else { System.out.println("No images found for class '" + className + "' in set '" + RPG.currentImageSet + "'."); return null; } }


    // --- Update Image, Details, and Name --- (Unchanged from previous)
     private static void updateCharacterImageAndDetails(String label) {
         ImageIcon iconToShow = null;
         String classLabelForDisplay = label; // Initially assume we show the label provided (hovered/focused)
         String randomNameToDisplay = null;

         // If label is null, it means we should show the selected class (or default prompt)
         if (classLabelForDisplay == null) {
              classLabelForDisplay = selectedClass; // Revert to selected class if one exists
         }

         // If label is the "Done" button, show the selected class instead
         if (doneButtonLabel.equals(classLabelForDisplay)) {
              classLabelForDisplay = selectedClass;
         }


         // 2. Load/Retrieve Image and Name based on classLabelForDisplay
         if (classLabelForDisplay != null && characterButtonMap.containsKey(classLabelForDisplay)) { // Ensure it's a valid class
             // --- Image ---
             boolean needsNewImage = !classLabelForDisplay.equals(lastDisplayedClass) || !currentIconForClass.containsKey(classLabelForDisplay);
             if (needsNewImage) {
                 iconToShow = loadCharacterImage(classLabelForDisplay);
                 currentIconForClass.put(classLabelForDisplay, iconToShow); // Cache (even if null)
             } else {
                 iconToShow = currentIconForClass.get(classLabelForDisplay);
             }

              // --- Name ---
              boolean needsNewName = !currentRandomNameMap.containsKey(classLabelForDisplay);
              if (needsNewName) {
                  randomNameToDisplay = CharacterClassInfo.getRandomName(classLabelForDisplay);
                  currentRandomNameMap.put(classLabelForDisplay, randomNameToDisplay); // Cache the name
                  System.out.println("Generated and cached name: " + randomNameToDisplay + " for: " + classLabelForDisplay);
              } else {
                  randomNameToDisplay = currentRandomNameMap.get(classLabelForDisplay);
                  System.out.println("Using cached name: " + randomNameToDisplay + " for: " + classLabelForDisplay);
              }
              lastDisplayedClass = classLabelForDisplay; // Track what was last processed

         } else {
             // No valid class selected/hovered/focused -> No image or name to show, revert lastDisplayedClass
             iconToShow = null;
             randomNameToDisplay = null;
             lastDisplayedClass = null; // Reset tracker if showing default
             classLabelForDisplay = null; // Ensure we hit the default HTML below
         }


         // 3. Display the determined icon (or placeholder)
         if (iconToShow != null) {
             characterImageLabel.setIcon(iconToShow);
             characterImageLabel.setText("");
         } else {
             characterImageLabel.setIcon(null);
             characterImageLabel.setText("<html><center>Select a Class</center></html>");
             characterImageLabel.setForeground(Color.DARK_GRAY);
         }
         characterImageLabel.revalidate();
         characterImageLabel.repaint();


         // 4. Update the Text Details Pane
         String detailsHtml;
         if (classLabelForDisplay != null) { // Check again after potential reset above
              CharacterClassInfo.BaseStats stats = CharacterClassInfo.getStats(classLabelForDisplay);
              if (stats != null) {
                   detailsHtml = "<html><body><h2 class='classname'>" + classLabelForDisplay + "</h2><table>" +
                                 "<tr><th>Stat</th><th>Value</th></tr>" +
                                 "<tr><td>Max HP</td><td>" + stats.MAX_HP + "</td></tr>" +
                                 "<tr><td>Max Energy</td><td>" + stats.MAX_ENERGY + "</td></tr>" +
                                 "<tr><td>Vigor</td><td>" + stats.VIG + "</td></tr>" +
                                 "<tr><td>Defense</td><td>" + stats.DEF + "</td></tr>" +
                                 "<tr><td>Strength</td><td>" + stats.STR + "</td></tr>" +
                                 "<tr><td>Dexterity</td><td>" + stats.DEX + "</td></tr>" +
                                 "<tr><td>Agility</td><td>" + stats.AGT + "</td></tr>" +
                                 "<tr><td>Luck</td><td>" + stats.LUCK + "</td></tr>" +
                                 "</table>" +
                                 "<p class='charname'>" + randomNameToDisplay + "</p>" +
                                 "</body></html>";
              } else {
                   detailsHtml = "<html><body><h3>Error</h3><p>Could not find stats for " + classLabelForDisplay + ".</p></body></html>";
              }
         } else {
              // Default prompt
              detailsHtml = "<html><body><h3 style='margin-top: 50px;'>Class Details</h3><p style='text-align:center; padding: 10px;'>" +
                            "Hover over or use arrow keys to navigate class buttons below." + "</p></body></html>"; // Slightly updated prompt
         }
         classDetailsPane.setText(detailsHtml);
         classDetailsPane.setCaretPosition(0);

         characterDescPanel.revalidate();
         characterDescPanel.repaint();
     }


} // End of UIManagement class