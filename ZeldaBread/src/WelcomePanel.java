/**
 * @file    WelcomePanel.java
 * @brief   Custom JPanel for the welcome screen. Displays a scaled image, welcome text,
 * and buttons to select the number of players. Adjusted image size and layout.
 *
 * @author  Jack Schulte
 * @version 1.0.2
 * @date    2025-04-14
 *
 * @copyright Copyright (c) 2025 Jack Schulte . All rights reserved.
 *
 * @version History:
 * - 1.0.2 (2025-04-14): Increased image size, adjusted layout to reduce excess vertical space.
 * - 1.0.1 (2025-04-14): Added image scaling to fit the panel better.
 * - 1.0.0 (2025-04-14): Initial version.
 */
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class WelcomePanel extends JPanel {

    private final Font titleFont = new Font("Trebuchet MS", Font.BOLD, 28);
    private final Font welcomeFont = new Font("Trebuchet MS", Font.PLAIN, 18);
    private final Font buttonFont = new Font("Aharoni", Font.BOLD, 20);
    private final Font buttonFontHover = new Font("Aharoni", Font.BOLD, 22);
    private final Color buttonColorNormal = Color.decode("#48AD48");
    private final Color buttonColorHover = Color.decode("#58D558");
    private final Color backgroundColor = UIManagement.backgroundColor; // Use color from UIManagement

    // Target width for the scaled image - Increased size
    private static final int IMAGE_TARGET_WIDTH = 450; // Adjusted width

    public WelcomePanel() {
        // Reduced vertical gap in BorderLayout
        setLayout(new BorderLayout(10, 10)); // Overall layout with smaller vgap
        setBackground(backgroundColor);
        setBorder(new EmptyBorder(20, 20, 20, 20)); // Padding

        // --- Top Title ---
        JLabel gameTitleLabel = new JLabel("Legend of Splenda: Bread of the Wild", SwingConstants.CENTER);
        gameTitleLabel.setFont(titleFont);
        gameTitleLabel.setForeground(Color.decode("#B6220E"));
        add(gameTitleLabel, BorderLayout.NORTH);

        // --- Center Content (Image + Text) ---
        // Panel using BoxLayout for vertical arrangement of image and text
        JPanel centerContentPanel = new JPanel();
        centerContentPanel.setLayout(new BoxLayout(centerContentPanel, BoxLayout.Y_AXIS));
        centerContentPanel.setBackground(backgroundColor);
        centerContentPanel.setAlignmentX(Component.CENTER_ALIGNMENT); // Align components centrally if needed

        // --- Image Loading & Scaling ---
        try {
            URL imageURL = getClass().getClassLoader().getResource("images/bread-man.png");
            if (imageURL == null) {
                throw new Exception("Resource not found: images/bread-man.png");
            }
            ImageIcon originalIcon = new ImageIcon(imageURL);
            if (originalIcon.getImageLoadStatus() != MediaTracker.COMPLETE) {
                 throw new Exception("Image could not be loaded completely.");
            }
            Image originalImage = originalIcon.getImage();
            int originalWidth = originalIcon.getIconWidth();
            int originalHeight = originalIcon.getIconHeight();
            int scaledHeight = -1;
            if (originalWidth > 0 && originalHeight > 0) {
                 scaledHeight = (int) (((double) IMAGE_TARGET_WIDTH / originalWidth) * originalHeight);
            }

             if (scaledHeight > 0) {
                 Image scaledImage = originalImage.getScaledInstance(IMAGE_TARGET_WIDTH, scaledHeight, Image.SCALE_SMOOTH);
                 ImageIcon scaledIcon = new ImageIcon(scaledImage);
                 JLabel imageLabel = new JLabel(scaledIcon);
                 imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                 centerContentPanel.add(imageLabel); // Add scaled image
             } else {
                  System.err.println("Could not calculate scaled height for image. Displaying original.");
                  JLabel imageLabel = new JLabel(originalIcon); // Fallback to original
                  imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                  centerContentPanel.add(imageLabel);
             }
        } catch (Exception e) {
            System.err.println("Exception loading/scaling image: images/bread-man.png - " + e.getMessage());
            JLabel errorLabel = new JLabel("Could not load/scale bread-man.png", SwingConstants.CENTER);
            errorLabel.setForeground(Color.RED);
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerContentPanel.add(errorLabel);
        }
        // --- End Image Loading ---

        // Welcome Text
        JLabel welcomeLabel = new JLabel("Welcome, adventurer! Choose the number of players to begin.", SwingConstants.CENTER);
        welcomeLabel.setFont(welcomeFont);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerContentPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Space between image and text
        centerContentPanel.add(welcomeLabel);

        // --- Wrapper Panel to prevent vertical stretching ---
        // Use a FlowLayout panel to hold the BoxLayout panel. FlowLayout respects preferred sizes.
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Center the content within this wrapper
        wrapperPanel.setBackground(backgroundColor); // Match background
        wrapperPanel.add(centerContentPanel); // Add the vertically arranged content here

        add(wrapperPanel, BorderLayout.CENTER); // Add wrapper to the main panel's center

        // --- Bottom Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10)); // Centered flow layout
        buttonPanel.setBackground(backgroundColor);

        JButton onePlayerButton = createPlayerButton("1 Player");
        JButton twoPlayerButton = createPlayerButton("2 Players");

        onePlayerButton.addActionListener(e -> startGame(1));
        twoPlayerButton.addActionListener(e -> startGame(2));

        buttonPanel.add(onePlayerButton);
        buttonPanel.add(twoPlayerButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createPlayerButton(String text) {
        JButton button = new JButton(text);
        button.setFont(buttonFont);
        button.setBackground(buttonColorNormal);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setPreferredSize(new Dimension(160, 50));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(buttonColorHover);
                button.setFont(buttonFontHover);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(buttonColorNormal);
                button.setFont(buttonFont);
            }
        });
        return button;
    }

    private void startGame(int numPlayers) {
        System.out.println(numPlayers + " player game selected.");
        RPG.setNumPlayers(numPlayers);
        RPG.createPlayers();
        UIManagement.characterCreatorScreen(); // Call the static method directly
    }
}