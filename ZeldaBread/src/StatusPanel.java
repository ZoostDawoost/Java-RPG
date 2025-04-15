/**
 * @file    StatusPanel.java
 * @brief   Custom JPanel to display player status information (Score, HP, Energy).
 * Provides a method to update the displayed values based on a Player object.
 *
 * @author  Jack Schulte
 * @version 1.1.0 (Refactored)
 * @date    2025-04-15
 *
 * @copyright Copyright (c) 2025 Jack Schulte. All rights reserved.
 * Strictly confidential and proprietary. Distribution, reproduction,
 * or modification is strictly prohibited without prior written permission.
 *
 * @version History:
 * - 1.1.0 (2025-04-15): Modified updateStatus to accept a Player object instead of RPG.
 * - 1.0.0 (2025-04-14): Initial version. (J. Schulte)
 */
import javax.swing.*;
import javax.swing.border.Border; // Keep border import
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
    private final Color TEXT_COLOR = Color.BLACK;

    public StatusPanel() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Status"));
        setBackground(UIManagement.backgroundColor);

        GridBagConstraints statusGbc = new GridBagConstraints();
        statusGbc.insets = new Insets(4, 8, 4, 8);
        statusGbc.anchor = GridBagConstraints.WEST;

        JLabel scoreLabel = new JLabel("Score:"); scoreLabel.setFont(statusFont);
        scoreValueLabel = new JLabel("0"); scoreValueLabel.setFont(statusFont);

        JLabel hpLabel = new JLabel("HP:"); hpLabel.setFont(statusFont);
        hpBar = new JProgressBar(0, 100); hpBar.setStringPainted(true);
        hpBar.setFont(statusFont.deriveFont(Font.BOLD, 14f));
        hpBar.setForeground(BAR_COLOR_NORMAL); hpBar.setBackground(BAR_BACKGROUND_COLOR);
        hpBar.setUI(new BasicProgressBarUI() { @Override protected Color getSelectionForeground() { return TEXT_COLOR; } @Override protected Color getSelectionBackground() { return TEXT_COLOR; } });

        JLabel energyLabel = new JLabel("Energy:"); energyLabel.setFont(statusFont);
        energyBar = new JProgressBar(0, 100); energyBar.setStringPainted(true);
        energyBar.setFont(statusFont.deriveFont(Font.BOLD, 14f));
        energyBar.setForeground(BAR_COLOR_NORMAL); energyBar.setBackground(BAR_BACKGROUND_COLOR);
        energyBar.setUI(new BasicProgressBarUI() { @Override protected Color getSelectionForeground() { return TEXT_COLOR; } @Override protected Color getSelectionBackground() { return TEXT_COLOR; } });

        // Add components using GridBagConstraints
        statusGbc.gridx = 0; statusGbc.gridy = 0; statusGbc.weightx = 0.0; statusGbc.fill = GridBagConstraints.NONE; add(scoreLabel, statusGbc);
        statusGbc.gridx = 1; statusGbc.gridy = 0; statusGbc.weightx = 1.0; statusGbc.fill = GridBagConstraints.HORIZONTAL; add(scoreValueLabel, statusGbc);
        statusGbc.gridx = 0; statusGbc.gridy = 1; statusGbc.weightx = 0.0; statusGbc.fill = GridBagConstraints.NONE; add(hpLabel, statusGbc);
        statusGbc.gridx = 1; statusGbc.gridy = 1; statusGbc.weightx = 1.0; statusGbc.fill = GridBagConstraints.HORIZONTAL; add(hpBar, statusGbc);
        statusGbc.gridx = 0; statusGbc.gridy = 2; statusGbc.weightx = 0.0; statusGbc.fill = GridBagConstraints.NONE; add(energyLabel, statusGbc);
        statusGbc.gridx = 1; statusGbc.gridy = 2; statusGbc.weightx = 1.0; statusGbc.fill = GridBagConstraints.HORIZONTAL; add(energyBar, statusGbc);
    }

    /**
     * Updates the displayed status values based on the Player's state.
     * @param player The Player object containing the current stats.
     */
    public void updateStatus(Player player) { // << Takes Player object
        if (player == null) return;

        scoreValueLabel.setText(String.valueOf(player.getScore()));

        // Update HP Bar using Player getters
        int maxHp = player.getMaxHp();
        int currentHp = player.getHp();
        hpBar.setMaximum(maxHp);
        hpBar.setValue(currentHp);
        hpBar.setString(currentHp + "/" + maxHp);
        double hpPercent = (maxHp > 0) ? (double) currentHp / maxHp : 0;
        hpBar.setForeground(hpPercent < 0.2 ? BAR_COLOR_LOW : BAR_COLOR_NORMAL);

        // Update Energy Bar using Player getters
        int maxEnergy = player.getMaxEnergy();
        int currentEnergy = player.getEnergy();
        energyBar.setMaximum(maxEnergy);
        energyBar.setValue(currentEnergy);
        energyBar.setString(currentEnergy + "/" + maxEnergy);
        double energyPercent = (maxEnergy > 0) ? (double) currentEnergy / maxEnergy : 0;
        energyBar.setForeground(energyPercent < 0.2 ? BAR_COLOR_LOW : BAR_COLOR_NORMAL);

        // Revalidate/repaint is generally good practice, though often handled by Swing
        // this.revalidate();
        // this.repaint();
    }
}