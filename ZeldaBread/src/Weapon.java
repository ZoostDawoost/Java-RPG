/**
 * @file    Weapon.java
 * @brief   Represents a weapon item, inheriting from Item.
 * Handles weapon-specific stats and damage calculation.
 *
 * @author  Jack Schulte
 * @version 1.2.0 (Refactored)
 * @date    2025-04-15
 *
 * @copyright Copyright (c) 2025 Jack Schulte. All rights reserved.
 * Strictly confidential and proprietary. Distribution, reproduction,
 * or modification is strictly prohibited without prior written permission.
 *
 * @version History:
 * - 1.2.0 (2025-04-15): Updated doDamage and use methods to accept Player object.
 * - 1.1.0 (2025-04-15): Modified to extend Item class. Removed redundant fields. Updated constructor.
 * - 1.0.0 (Original): Initial version. (J. Schulte)
 */

 public class Weapon extends Item {

    private String weaponType;
    private double critRate;
    private double strScaling;
    private double dexScaling;
    private int strReq;
    private int dexReq;
    private int dmgLoEnd;
    private int dmgRange;
    private int critDmg;

    public Weapon(String weaponName, String description, String weaponType, int cost, double dropRate,
                  double critRate, double strScaling, double dexScaling, int strReq, int dexReq,
                  int dmgLoEnd, int dmgRange, int critDmg)
    {
        super(weaponName, description, cost, dropRate);
        this.weaponType = weaponType; this.critRate = critRate; this.strScaling = strScaling;
        this.dexScaling = dexScaling; this.strReq = strReq; this.dexReq = dexReq;
        this.dmgLoEnd = dmgLoEnd; this.dmgRange = dmgRange; this.critDmg = critDmg;
    }

    // --- Weapon-Specific Getters ---
    public String getWeaponType() { return weaponType; }
    public double getCritRate() { return this.critRate; }
    public double getStrScaling() { return this.strScaling; }
    public double getDexScaling() { return this.dexScaling; }
    public int getStrReq() { return this.strReq; }
    public int getDexReq() { return this.dexReq; }
    public int getDmgLoEnd() { return this.dmgLoEnd; }
    public int getDmgRange() { return this.dmgRange; }
    public int getCritDmg() { return this.critDmg; }

    /**
     * Calculates damage dealt by this weapon based on the player's stats.
     * @param player The Player wielding the weapon.
     * @param w The Weapon being used (usually 'this', but passed for clarity).
     * @return The calculated damage amount.
     */
    public static double doDamage(Player player, Weapon w) { // << Takes Player object
        if (player == null || w == null) return 0;

        // Check requirements using Player's stats
        if (player.getStr() < w.getStrReq() || player.getDex() < w.getDexReq()) {
             System.out.println(player.getCharacterName() + " lacks skill for " + w.getItemName());
             UIManagement.addDialogue("You aren't skilled enough to wield the " + w.getItemName() + " effectively!");
             return 0; // Minimal damage
        }

        int baseDamage = (int) (Math.random() * w.getDmgRange() + w.getDmgLoEnd());
        int critDamage = 0;
        if (Math.random() < w.getCritRate()) {
            critDamage = w.getCritDmg();
            UIManagement.addDialogue("*** Critical Hit! ***");
        }
        // Example scaling formula using Player's stats
        double scalingBonus = 1.0 + (w.strScaling * player.getStr() * 0.01) + (w.dexScaling * player.getDex() * 0.01);
        double totalDamage = (baseDamage + critDamage) * scalingBonus;
        // System.out.println("Damage: Base=" + baseDamage + ", Crit=" + critDamage + ", Scaling=" + scalingBonus + ", Total=" + totalDamage); // Debug
        return totalDamage;
    }


}