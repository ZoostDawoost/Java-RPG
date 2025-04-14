/**
 * @file    StatusPanel.java
 * @brief   Custom JPanel to display player status information (Score, HP, Energy).
 * Provides a method to update the displayed values.
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
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;

public class StatusPanel extends JPanel {

    private JLabel scoreValueLabel;
    private JProgressBar hpBar;
    private JProgressBar energyBar;

    private final Font statusFont = new Font("Trebuchet MS", Font.BOLD, 16);
    private final Color BAR_COLOR_NORMAL = Color.GREEN.darker();
    private final Color BAR_COLOR_LOW = Color.RED;
    private final Color BAR_BACKGROUND_COLOR = Color.LIGHT_GRAY;
    private final Color TEXT_COLOR = Color.BLACK; // Color for progress bar text

    public StatusPanel() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Status"));
        setBackground(UIManagement.backgroundColor); // Use color from UIManagement

        GridBagConstraints statusGbc = new GridBagConstraints();
        statusGbc.insets = new Insets(4, 8, 4, 8);
        statusGbc.anchor = GridBagConstraints.WEST;

        JLabel scoreLabel = new JLabel("Score:");
        scoreLabel.setFont(statusFont);
        scoreValueLabel = new JLabel("0");
        scoreValueLabel.setFont(statusFont);

        JLabel hpLabel = new JLabel("HP:");
        hpLabel.setFont(statusFont);
        hpBar = new JProgressBar(0, 100); // Initial max value, updated later
        hpBar.setStringPainted(true);
        hpBar.setFont(statusFont.deriveFont(Font.BOLD, 14f));
        hpBar.setForeground(BAR_COLOR_NORMAL);
        hpBar.setBackground(BAR_BACKGROUND_COLOR);
        // Custom UI to ensure text color is consistent
        hpBar.setUI(new BasicProgressBarUI() {
            @Override
            protected Color getSelectionForeground() { return TEXT_COLOR; }
            @Override
            protected Color getSelectionBackground() { return TEXT_COLOR; }
        });


        JLabel energyLabel = new JLabel("Energy:");
        energyLabel.setFont(statusFont);
        energyBar = new JProgressBar(0, 100); // Initial max value, updated later
        energyBar.setStringPainted(true);
        energyBar.setFont(statusFont.deriveFont(Font.BOLD, 14f));
        energyBar.setForeground(BAR_COLOR_NORMAL);
        energyBar.setBackground(BAR_BACKGROUND_COLOR);
        // Custom UI to ensure text color is consistent
        energyBar.setUI(new BasicProgressBarUI() {
            @Override
            protected Color getSelectionForeground() { return TEXT_COLOR; }
            @Override
            protected Color getSelectionBackground() { return TEXT_COLOR; }
        });

        // Add components using GridBagConstraints
        statusGbc.gridx = 0; statusGbc.gridy = 0; statusGbc.weightx = 0.0; statusGbc.fill = GridBagConstraints.NONE; add(scoreLabel, statusGbc);
        statusGbc.gridx = 1; statusGbc.gridy = 0; statusGbc.weightx = 1.0; statusGbc.fill = GridBagConstraints.HORIZONTAL; add(scoreValueLabel, statusGbc);

        statusGbc.gridx = 0; statusGbc.gridy = 1; statusGbc.weightx = 0.0; statusGbc.fill = GridBagConstraints.NONE; add(hpLabel, statusGbc);
        statusGbc.gridx = 1; statusGbc.gridy = 1; statusGbc.weightx = 1.0; statusGbc.fill = GridBagConstraints.HORIZONTAL; add(hpBar, statusGbc);

        statusGbc.gridx = 0; statusGbc.gridy = 2; statusGbc.weightx = 0.0; statusGbc.fill = GridBagConstraints.NONE; add(energyLabel, statusGbc);
        statusGbc.gridx = 1; statusGbc.gridy = 2; statusGbc.weightx = 1.0; statusGbc.fill = GridBagConstraints.HORIZONTAL; add(energyBar, statusGbc);
    }

    /**
     * Updates the displayed status values (Score, HP, Energy) based on the player's state.
     * @param player The RPG player object containing the current stats.
     */
    public void updateStatus(RPG player) {
        if (player == null) return;

        scoreValueLabel.setText(String.valueOf(player.getScore()));

        // Update HP Bar
        hpBar.setMaximum(player.getMaxHp());
        hpBar.setValue(player.getHp());
        hpBar.setString(player.getHp() + "/" + player.getMaxHp());
        double hpPercent = (player.getMaxHp() > 0) ? (double) player.getHp() / player.getMaxHp() : 0;
        hpBar.setForeground(hpPercent < 0.2 ? BAR_COLOR_LOW : BAR_COLOR_NORMAL);

        // Update Energy Bar
        energyBar.setMaximum(player.getMaxEnergy());
        energyBar.setValue(player.getEnergy());
        energyBar.setString(player.getEnergy() + "/" + player.getMaxEnergy());
        double energyPercent = (player.getMaxEnergy() > 0) ? (double) player.getEnergy() / player.getMaxEnergy() : 0;
        energyBar.setForeground(energyPercent < 0.2 ? BAR_COLOR_LOW : BAR_COLOR_NORMAL);

        // It's generally good practice to revalidate/repaint the panel
        // after changing component states, though Swing often handles it.
        this.revalidate();
        this.repaint();
    }
}