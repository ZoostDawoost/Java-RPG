/**
 * @file    UIManagement.java
 * @brief   Manages the main game window (JFrame), UI panels (using CardLayout), button actions, key bindings, and screen transitions.
 *
 * Sets up the overall graphical user interface, including title/subtitle labels, the central area
 * (which switches between description text and the MapPanel), and the bottom panel with
 * action buttons and comments. Handles user input via buttons and keyboard (arrow keys, F8).
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
 * - 1.1 (2025-04-14): Added F8 key binding to toggle map visibility via RPG.toggleMapVisibility() and mapPanel.refreshMap(). Updated comments. (J. Schulte)
 * - 1.0 (2025-04-01): Initial version with screen setup, character creation flow, and basic movement buttons/key bindings. (J. Schulte)
 * // Add more versions as the file evolves
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener; // Keep if needed elsewhere, but AbstractAction used for keys
import java.awt.event.KeyEvent; // Import KeyEvent for VK_ constants
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap; // Import HashMap
import java.util.Map; // Import Map

import javax.swing.border.LineBorder;

public class UIManagement {

    private static JFrame frame = new JFrame();
    private static JPanel topPanel = new JPanel(new BorderLayout()); // Panel for title/subtitle
    private static JLabel titleLabel = new JLabel("Legend of Splenda: Bread of the Wild", SwingConstants.CENTER);
    private static JLabel subtitleLabel = new JLabel("A roguelike RPG by Jack Schulte", SwingConstants.CENTER);

    // Central panel will hold either text OR the map
    private static JPanel centerPanel = new JPanel(new CardLayout());
    private static JTextArea descArea = new JTextArea();
    private static JScrollPane descScrollPane; // Scroll pane for descArea
    private static MapPanel mapPanel; // Panel to display the map

    // Bottom panel for buttons and comments
    private static JPanel bottomPanel = new JPanel(new BorderLayout());
    private static JLabel commentLabel = new JLabel("", SwingConstants.CENTER);
    private static JPanel buttonPanel = new JPanel(); // For action buttons

    // Fonts and Colors
    private static Font titleFont = new Font("Trebuchet MS", Font.BOLD, 24);
    private static Font subtitleFont = new Font("Trebuchet MS", Font.BOLD, 20);
    private static Font descFont = new Font("Trebuchet MS", Font.PLAIN, 18);
    private static Font commentFont = new Font("Trebuchet MS", Font.ITALIC, 18);
    private static Font buttonFont = new Font("Aharoni", Font.BOLD, 20);
    private static Font buttonFontHover = new Font("Aharoni", Font.BOLD, 22);
    private static Color backgroundColor = Color.decode("#EEEEEE");


    private static GridBagConstraints gbc = new GridBagConstraints(); // For button layout
    // Make rpg non-static to avoid potential issues if multiple games were ever needed
    // And ensure mapPanel uses the same instance.
    private static RPG rpg = new RPG();

    // --- Character Selection Specific ---
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

    // --- Gameplay Specific ---
    private static String[] moveButtonLabels = {"Turn Left", "Forward", "Turn Right", "Backward"}; // Example move buttons
    private static String[] moveButtonColors = {"#44AADD", "#66CCFF"}; // Example colors
    private static boolean isGameScreenActive = false; // Flag for keyboard input


    public static void main(String[] args) {

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600); // Increased height for map potentially
        frame.setLayout(new BorderLayout()); // Main Layout

        // --- Top Panel ---
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

        // --- Center Panel (CardLayout) ---
        // Description Area setup
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setFont(descFont);
        descArea.setBackground(backgroundColor);
        descScrollPane = new JScrollPane(descArea);
        descScrollPane.setBorder(null);
        descScrollPane.setBackground(backgroundColor);

        // Map Panel setup (created LATER, AFTER map generation might start)
        // mapPanel = new MapPanel(rpg); // Create instance, pass RPG logic << MOVED LATER >>

        // Add initial component (description) to center panel
        centerPanel.add(descScrollPane, "description"); // Add with a name
        // centerPanel.add(mapPanel, "map"); // Add map panel later
        centerPanel.setBackground(backgroundColor);


        // --- Bottom Panel ---
        commentLabel.setFont(commentFont);
        commentLabel.setBackground(backgroundColor);
        commentLabel.setOpaque(true);
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.setLayout(new GridBagLayout()); // Use GridBagLayout for buttons
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.gridy = 0;

        bottomPanel.setBackground(backgroundColor);
        bottomPanel.add(commentLabel, BorderLayout.NORTH); // Comment above buttons
        bottomPanel.add(buttonPanel, BorderLayout.CENTER); // Buttons in the middle


        // --- Final Frame Assembly ---
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);


        // --- Initial Screen ---
        titleScreen(); // Sets up title screen UI

        // --- SETUP KEY BINDINGS (AFTER components exist but can be done early) ---
        setupKeyBindings();


        // --- Show Frame ---
        frame.setVisible(true); // Show frame before potentially slow map gen


        // --- Start Game Logic (Build map etc. AFTER UI setup) ---
        // Consider doing this in a background thread if it's slow
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                System.out.println("Building map...");
                rpg.buildMap(); // This now also explores the start area
                rpg.addEventsToRooms();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Handle exceptions from doInBackground
                    System.out.println("Map generation complete.");
                    rpg.printMap(); // Print map to console for debugging

                    // NOW create and add the MapPanel
                    mapPanel = new MapPanel(rpg); // Create MapPanel with the initialized RPG
                    centerPanel.add(mapPanel, "map"); // Add map panel now that it's ready
                    centerPanel.revalidate(); // Update layout
                    centerPanel.repaint();

                    // If the game should start immediately after map gen (e.g., single player)
                    // you might call startGameScreen() here, or wait for player actions.
                    // For now, we transition via button clicks.

                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error generating map: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute(); // Start map generation in background
    }


// ==============================================================================================
// =================== Screen Methods ============================================================

    private static void showScreen(String screenName) {
        // Ensure mapPanel exists before trying to switch to it
        if (screenName.equals("map") && mapPanel == null) {
             System.err.println("Attempted to switch to map screen, but mapPanel is null!");
             // Maybe switch back to description or show an error message?
             // For now, just prevent the switch.
             return;
        }

        CardLayout cl = (CardLayout) (centerPanel.getLayout());
        cl.show(centerPanel, screenName); // Switch view in center panel
        isGameScreenActive = screenName.equals("map"); // Update flag based on screen shown
        System.out.println("Switched to screen: " + screenName + ", isGameScreenActive: " + isGameScreenActive); // Debug

        // Request focus on the root pane when the game screen is active for key bindings
        if (isGameScreenActive) {
            frame.getRootPane().requestFocusInWindow();
            commentLabel.setText("Use buttons or ←↑→↓ keys. Press F8 to toggle map."); // Update help text
        }
    }

    private static void titleScreen() {
        titleLabel.setText("Legend of Splenda: Bread of the Wild");
        subtitleLabel.setText("A roguelike RPG by Jack Schulte");
        descArea.setText("Welcome, adventurer!"); // Set initial text
        commentLabel.setText(""); // Clear comment
        showScreen("description"); // Show the description area

        String[] titleButtons = {"Start!", "#EECC44", "#F7DD77"};
        int[] titleButtonLengths = {120};
        buttonPanel.removeAll(); // Clear previous buttons
        makeButtons(titleButtons, titleButtonLengths, "title"); // Pass screen context
        bottomPanel.revalidate(); // Revalidate bottom panel
        bottomPanel.repaint();
    }

    private static void numPlayersScreen() {
        titleLabel.setText("Number of Players"); // Simpler title
        subtitleLabel.setText("Choose the number of players:");
        descArea.setText("");
        commentLabel.setText("");
        showScreen("description"); // Keep showing description area for now

        String[] numPlayersButtons = {"1 Player", "#48AD48", "#58D558", "2 Players", "#48AD48", "#58D558"};
        int[] numPlayersButtonLengths = {140, 140};
        buttonPanel.removeAll();
        makeButtons(numPlayersButtons, numPlayersButtonLengths, "numPlayers");
        bottomPanel.revalidate();
        bottomPanel.repaint();
    }

    private static void characterCreatorScreen() {
        selectedClass = null; // Reset selection for the new player
        characterButtonMap.clear(); // Clear the map for new buttons

        titleLabel.setText("Player " + (RPG.getPlayerIndex() + 1) + ", Create your Character");
        subtitleLabel.setText("Select a Class:"); // Give instruction
        descArea.setText(""); // Clear description maybe?
        commentLabel.setText("");
        showScreen("description"); // Still showing text area

        // Combine character buttons and done button for makeButtons
        ArrayList<String> buttonConfigList = new ArrayList<>();
        ArrayList<Integer> lengthConfigList = new ArrayList<>();

        for (String label : characterButtonLabels) {
            buttonConfigList.add(label);
            buttonConfigList.add(characterButtonColors[0]); // Normal color
            buttonConfigList.add(characterButtonColors[1]); // Hover color
            lengthConfigList.add(characterButtonLengths[0]); // Use first length for all class buttons
        }
        buttonConfigList.add(doneButtonLabel);
        buttonConfigList.add(doneButtonColors[0]); // Done normal color
        buttonConfigList.add(doneButtonColors[1]); // Done hover color
        lengthConfigList.add(characterButtonLengths[characterButtonLengths.length - 1]); // Use last length for Done

        String[] creatorButtons = buttonConfigList.toArray(new String[0]);
        int[] creatorButtonLengths = lengthConfigList.stream().mapToInt(i -> i).toArray();

        buttonPanel.removeAll();
        makeButtons(creatorButtons, creatorButtonLengths, "characterCreator");
        bottomPanel.revalidate();
        bottomPanel.repaint();
    }

    // Game screen setup
    private static void startGameScreen() {
        if (mapPanel == null) {
             System.err.println("Cannot start game screen, mapPanel is not initialized yet!");
             // Show an error or wait?
             JOptionPane.showMessageDialog(frame, "Map is still loading, please wait.", "Loading", JOptionPane.INFORMATION_MESSAGE);
             return;
        }
        titleLabel.setText("Dungeon Exploration"); // Update title
        RPG currentPlayer = RPG.getPlayers().get(0); // Get player 0 for now
        subtitleLabel.setText("Player 1: " + currentPlayer.getClassName() + " | HP: " + currentPlayer.showHealth()); // Show player info
        commentLabel.setText("Use buttons or arrow keys to move. Press F8 to toggle map."); // Update comment

        showScreen("map"); // << SWITCH TO MAP PANEL >>
        mapPanel.refreshMap(); // Ensure map panel draws initially

        // --- Set up Gameplay Buttons ---
        ArrayList<String> buttonConfigList = new ArrayList<>();
        ArrayList<Integer> lengthConfigList = new ArrayList<>();
        int buttonLength = 110; // Length for move buttons

        for (String label : moveButtonLabels) {
            buttonConfigList.add(label);
            buttonConfigList.add(moveButtonColors[0]); // Normal color
            buttonConfigList.add(moveButtonColors[1]); // Hover color
            lengthConfigList.add(buttonLength);
        }
        // Add other gameplay buttons (Inventory, Attack?) here

        String[] gameplayButtons = buttonConfigList.toArray(new String[0]);
        int[] gameplayButtonLengths = lengthConfigList.stream().mapToInt(i -> i).toArray();

        buttonPanel.removeAll();
        makeButtons(gameplayButtons, gameplayButtonLengths, "gameplay"); // Use "gameplay" context
        bottomPanel.revalidate();
        bottomPanel.repaint();

        System.out.println("Game screen initialized.");
        // Print player stats for confirmation
        for (RPG player : RPG.getPlayers()) {
            System.out.println("Player Class: " + player.getClassName() + " Pos: [" + player.getCurrentPos()[0] + "," + player.getCurrentPos()[1] + "]");
            // Print other stats...
        }
         frame.getRootPane().requestFocusInWindow(); // Crucial for key bindings to work immediately
    }


// ==============================================================================================
// =================== Action Listeners =========================================================

    private static void pickActionListener(JButton button, String buttonLabel, String screenContext) {
        switch (screenContext) {
            case "title":
                if (buttonLabel.equals("Start!")) {
                    button.addActionListener(e -> numPlayersScreen());
                }
                break;

            case "numPlayers":
                if (buttonLabel.equals("1 Player")) {
                    button.addActionListener(e -> {
                        RPG.setNumPlayers(1);
                        RPG.createPlayers();
                        characterCreatorScreen();
                    });
                } else if (buttonLabel.equals("2 Players")) {
                    button.addActionListener(e -> {
                        RPG.setNumPlayers(2);
                        RPG.createPlayers();
                        characterCreatorScreen();
                    });
                }
                break;

            case "characterCreator":
                boolean isClassButton = false;
                for (String classLabel : characterButtonLabels) {
                    if (buttonLabel.equals(classLabel)) {
                        isClassButton = true;
                        break;
                    }
                }

                if (isClassButton) {
                    // Action for Class Selection Buttons (Knight, Sentinel, etc.)
                    button.addActionListener(e -> {
                        selectedClass = buttonLabel; // Store the selected class name
                        commentLabel.setText(buttonLabel + " selected!"); // Provide feedback

                        // Update appearance of all class buttons
                        for (Map.Entry<String, JButton> entry : characterButtonMap.entrySet()) {
                            JButton btn = entry.getValue();
                            String label = entry.getKey();
                            if (label.equals(selectedClass)) {
                                // Highlight selected button
                                btn.setBackground(selectedColor); // Use a distinct selection color
                                btn.setBorder(selectedBorder);
                            } else {
                                // Reset other buttons to default appearance
                                btn.setBackground(Color.decode(characterButtonColors[0])); // Default normal color
                                btn.setBorder(defaultBorder); // Use a default border
                            }
                        }
                    });
                } else if (buttonLabel.equals(doneButtonLabel)) {
                    // Action for "Done" button
                    button.addActionListener(e -> {
                        if (selectedClass == null) {
                            commentLabel.setText("Please select a class before proceeding!");
                        } else {
                            RPG.assignClass(RPG.getPlayerIndex(), selectedClass);
                            RPG.modPlayerIndex();
                            if (RPG.getPlayerIndex() < RPG.getNumPlayers()) {
                                characterCreatorScreen();
                            } else {
                                // Check if mapPanel is ready before starting game screen
                                if (mapPanel != null) {
                                     startGameScreen();
                                } else {
                                     // Should not happen if worker thread logic is correct, but handle defensively
                                     JOptionPane.showMessageDialog(frame, "Map is still loading, please wait before starting the game.", "Loading", JOptionPane.INFORMATION_MESSAGE);
                                     // Potentially re-enable the "Done" button or retry later?
                                }
                            }
                        }
                    });
                }
                break;

            case "gameplay":
                // --- Actions for Gameplay Buttons ---
                switch (buttonLabel) {
                    case "Turn Left":
                        button.addActionListener(e -> handleTurnLeft());
                        break;
                    case "Forward":
                        button.addActionListener(e -> handleMoveForward());
                        break;
                    case "Turn Right":
                        button.addActionListener(e -> handleTurnRight());
                        break;
                    case "Backward":
                        button.addActionListener(e -> handleMoveBackward());
                        break;
                    // Add cases for other gameplay buttons (Attack, Inventory...)
                }
                break;
        }
    }

    // Helper to update subtitle during gameplay (e.g., HP changes)
    private static void updateGameSubtitle() {
        if (RPG.getPlayers() == null || RPG.getPlayers().isEmpty()) return; // Add null/empty check
        RPG currentPlayer = RPG.getPlayers().get(0); // Assuming player 0 for now
        if (currentPlayer == null) return; // Check if player exists
        subtitleLabel.setText("Player 1: " + currentPlayer.getClassName() + " | HP: " + currentPlayer.showHealth());
    }

// ==============================================================================================
// =================== Gameplay Action Handlers (for Buttons and Keys) ==========================
// ==============================================================================================

    private static void handleTurnLeft() {
        if (!isGameScreenActive || mapPanel == null) return; // Only act if game screen is active and map exists
        rpg.turnPlayer(0); // 0 for left turn in RPG class
        mapPanel.refreshMap(); // Redraw map to show new facing direction
        updateGameSubtitle(); // Update HP etc.
        commentLabel.setText("Turned left.");
        frame.getRootPane().requestFocusInWindow(); // Keep focus for keys
    }

    private static void handleTurnRight() {
        if (!isGameScreenActive || mapPanel == null) return;
        rpg.turnPlayer(1); // 1 for right turn
        mapPanel.refreshMap();
        updateGameSubtitle();
        commentLabel.setText("Turned right.");
        frame.getRootPane().requestFocusInWindow();
    }

    private static void handleMoveForward() {
        if (!isGameScreenActive || mapPanel == null) return;
        if (rpg.movePlayer(rpg.getWayFacing())) { // Move in current direction
            mapPanel.refreshMap(); // Redraw map at new position and exploration
            updateGameSubtitle();
            // Update comment with room info (add check for valid pos first)
            int[] pos = rpg.getCurrentPos();
             if (pos != null && pos.length == 2 && pos[0] >= 0 && pos[0] < rpg.map.length && pos[1] >= 0 && pos[1] < rpg.map[0].length) {
                 commentLabel.setText("Moved forward into room type: " + rpg.map[pos[0]][pos[1]]);
             } else {
                 commentLabel.setText("Moved forward."); // Fallback message
             }
            // Add logic here: check room event, trigger combat etc.
        } else {
            commentLabel.setText("Cannot move forward!");
        }
        frame.getRootPane().requestFocusInWindow();
    }

    private static void handleMoveBackward() {
        if (!isGameScreenActive || mapPanel == null) return;
        int oppositeDirection = (rpg.getWayFacing() + 2) % 4; // Calculate opposite direction
        if (rpg.movePlayer(oppositeDirection)) {
            mapPanel.refreshMap();
            updateGameSubtitle();
            // Update comment with room info (add check for valid pos first)
            int[] pos = rpg.getCurrentPos();
             if (pos != null && pos.length == 2 && pos[0] >= 0 && pos[0] < rpg.map.length && pos[1] >= 0 && pos[1] < rpg.map[0].length) {
                  commentLabel.setText("Moved backward into room type: " + rpg.map[pos[0]][pos[1]]);
             } else {
                  commentLabel.setText("Moved backward."); // Fallback message
             }
        } else {
            commentLabel.setText("Cannot move backward!");
        }
         frame.getRootPane().requestFocusInWindow();
    }

    private static void handleToggleMap() {
         if (!isGameScreenActive || mapPanel == null) return;
         rpg.toggleMapVisibility(); // Call the toggle method in RPG
         mapPanel.refreshMap(); // Redraw the map with the new visibility setting
         commentLabel.setText("Map view toggled (Show full: " + rpg.isShowFullMap() + ")");
         frame.getRootPane().requestFocusInWindow();
    }


// ==============================================================================================
// =================== Key Binding Setup ========================================================
// ==============================================================================================

    private static void setupKeyBindings() {
        // Get the InputMap and ActionMap for the frame's root pane
        // WHEN_IN_FOCUSED_WINDOW means the binding works even if a button has focus
        InputMap inputMap = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = frame.getRootPane().getActionMap();

        // Define KeyStrokes for arrow keys
        KeyStroke upKey = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
        KeyStroke downKey = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
        KeyStroke leftKey = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
        KeyStroke rightKey = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
        KeyStroke f8Key = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0); // Added F8 KeyStroke

        // Map keys to action names (strings)
        inputMap.put(upKey, "moveForward");
        inputMap.put(downKey, "moveBackward");
        inputMap.put(leftKey, "turnLeft");
        inputMap.put(rightKey, "turnRight");
        inputMap.put(f8Key, "toggleMap"); // Added mapping for F8

        // Map action names to AbstractAction objects
        actionMap.put("moveForward", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleMoveForward();
            }
        });
        actionMap.put("moveBackward", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleMoveBackward();
            }
        });
        actionMap.put("turnLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleTurnLeft();
            }
        });
        actionMap.put("turnRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleTurnRight();
            }
        });
        actionMap.put("toggleMap", new AbstractAction() { // Added Action for F8
            @Override
            public void actionPerformed(ActionEvent e) {
                handleToggleMap();
            }
        });
    }


// ==============================================================================================
// =================== Mouse Listeners (Hover Effects) ==========================================
// ==============================================================================================

    // Simplified Mouse Listener for hover effects
    private static void addHoverEffect(JButton button, int length, String colorNormal, String colorHover) {
        button.addMouseListener(new MouseAdapter() {
            Color normal = Color.decode(colorNormal);
            Color hover = Color.decode(colorHover);
            Dimension defaultSize = new Dimension(length, 50);
            Dimension hoverSize = new Dimension((int) (length * 1.05), 55);

            @Override
            public void mouseEntered(MouseEvent e) {
                // Only change if not the currently selected button (in character creation)
                boolean isSelectedCharButton = selectedClass != null && button.getText().equals(selectedClass) && characterButtonMap.containsKey(selectedClass);
                if (!isSelectedCharButton) {
                    button.setPreferredSize(hoverSize);
                    button.setBackground(hover);
                    button.setFont(buttonFontHover);
                    buttonPanel.revalidate(); // Revalidate button panel
                    buttonPanel.repaint();
                }
                // Update description based on hovered button (if it's a class button)
                if (characterButtonMap.containsKey(button.getText())) {
                    updateDescription(button.getText());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Only change back if not the currently selected button (in character creation)
                boolean isSelectedCharButton = selectedClass != null && button.getText().equals(selectedClass) && characterButtonMap.containsKey(selectedClass);
                if (!isSelectedCharButton) {
                    button.setPreferredSize(defaultSize);
                    button.setBackground(normal);
                    button.setFont(buttonFont);
                    buttonPanel.revalidate();
                    buttonPanel.repaint();
                }
                // Clear description on exit (or reset to selected class description) during char creation
                if (characterButtonMap.containsKey(button.getText())) { // Only if exiting a class button
                    if (selectedClass != null && characterButtonMap.containsKey(selectedClass)) {
                        updateDescription(selectedClass); // Show selected class desc again
                    } else {
                        // If no class is selected yet, clear the labels
                        if (selectedClass == null) {
                            // Check if still on character creator before resetting labels
                            if (!isGameScreenActive) {
                                subtitleLabel.setText("Select a Class:"); // Reset subtitle
                                commentLabel.setText(""); // Clear comment label
                            }
                        }
                    }
                }
            }
        });
    }

    // Helper to update description/comment based on button label (mainly for char creation)
    private static void updateDescription(String label) {
        // Only update description if we are not in the active game screen and it's a relevant button
        if (!isGameScreenActive && (characterButtonMap.containsKey(label) || label.equals(doneButtonLabel))) {
            switch (label) {
                case "Knight":
                    subtitleLabel.setText("Class: Knight");
                    commentLabel.setText("A strong, reliable warrior that can weather most challenges.");
                    break;
                case "Sentinel":
                    subtitleLabel.setText("Class: Sentinel");
                    commentLabel.setText("A sturdy defender with high resilience.");
                    break;
                case "Assassin":
                    subtitleLabel.setText("Class: Assassin");
                    commentLabel.setText("A nimble rogue specializing in critical strikes.");
                    break;
                case "Caveman":
                    subtitleLabel.setText("Class: Caveman");
                    commentLabel.setText("Unga bunga? Surprisingly balanced stats.");
                    break;
                case "Done":
                    subtitleLabel.setText("Confirm Selection");
                    commentLabel.setText("Finalize character creation for this player.");
                    break;
            }
        }
    }


// ==============================================================================================
// =================== Button Creation Helper ===================================================
// ==============================================================================================

    // Modified makeButtons to accept screen context
    private static void makeButtons(String[] stringArray, int[] intArray, String screenContext) {
        buttonPanel.removeAll(); // Ensure panel is clear before adding

        // Clear character map only if relevant for the screen context
        if (screenContext.equals("characterCreator")) {
            characterButtonMap.clear(); // Clear map when rebuilding buttons for char creator
        }

        for (int i = 0; i < stringArray.length / 3; i++) {
            String buttonLabel = stringArray[i * 3];
            JButton button = new JButton(buttonLabel);
            button.setPreferredSize(new Dimension(intArray[i], 50));
            String normalColor = stringArray[i * 3 + 1];
            String hoverColor = stringArray[i * 3 + 2];
            button.setBackground(Color.decode(normalColor));
            button.setFont(buttonFont);
            button.setBorder(defaultBorder); // Set default border
            //button.setFocusable(false); // Keep as true or default so buttons *can* get focus, but key binds should work anyway.

            // Add hover effects
            addHoverEffect(button, intArray[i], normalColor, hoverColor);

            // Add the specific action listener based on screen context
            pickActionListener(button, buttonLabel, screenContext);

            gbc.gridx = i; // Place buttons horizontally in GridBagLayout
            buttonPanel.add(button, gbc);

            // If it's the character creator screen, add class buttons to the map & style selected
            if (screenContext.equals("characterCreator")) {
                for (String classLabel : characterButtonLabels) {
                    if (buttonLabel.equals(classLabel)) {
                        characterButtonMap.put(buttonLabel, button);
                        break;
                    }
                }
                // Check if this button is the currently selected one and style it
                if (buttonLabel.equals(selectedClass)) {
                    button.setBackground(selectedColor);
                    button.setBorder(selectedBorder);
                }
            }
        }
        buttonPanel.revalidate();
        buttonPanel.repaint();
    }

}