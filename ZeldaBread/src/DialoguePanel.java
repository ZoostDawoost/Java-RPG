/**
 * @file    DialoguePanel.java
 * @brief   Custom JPanel to display game dialogue and messages to the player.
 * Provides a method to append new messages.
 *
 * @author  Jack Schulte
 * @version 1.0.0
 * @date    2025-04-14
 *
 * @copyright Copyright (c) 2025 Jack Schulte. All rights reserved.
 * Strictly confidential and proprietary. Distribution, reproduction,
 * or modification is strictly prohibited without prior written permission.
 */
import javax.swing.*;
import java.awt.*;

public class DialoguePanel extends JPanel {

    private JTextArea dialogueTextArea;
    private final Font dialogueFont = new Font("Trebuchet MS", Font.PLAIN, 14);

    public DialoguePanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Dialogue"));
        setBackground(UIManagement.backgroundColor); // Use color from UIManagement

        dialogueTextArea = new JTextArea(10, 20); // Rows, Columns hint
        dialogueTextArea.setEditable(false);
        dialogueTextArea.setLineWrap(true);
        dialogueTextArea.setWrapStyleWord(true);
        dialogueTextArea.setFont(dialogueFont);
        dialogueTextArea.setBackground(Color.WHITE); // Set a background for readability
        dialogueTextArea.setForeground(Color.BLACK);

        JScrollPane dialogueScrollPane = new JScrollPane(dialogueTextArea);
        dialogueScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        dialogueScrollPane.setBorder(BorderFactory.createEmptyBorder(2,2,2,2)); // Remove inner border if desired

        add(dialogueScrollPane, BorderLayout.CENTER);
    }

    /**
     * Appends a new message to the dialogue text area and scrolls to the bottom.
     * Ensures the update happens on the Event Dispatch Thread.
     * @param text The message to add.
     */
    public void addDialogue(String text) {
        if (text == null) return;

        // Ensure updates happen on the EDT
        SwingUtilities.invokeLater(() -> {
            dialogueTextArea.append(text + "\n");
            // Auto-scroll to the bottom
            dialogueTextArea.setCaretPosition(dialogueTextArea.getDocument().getLength());
        });
    }

     /**
      * Clears all text from the dialogue area.
      */
    public void clearDialogue() {
         SwingUtilities.invokeLater(() -> dialogueTextArea.setText(""));
    }
}