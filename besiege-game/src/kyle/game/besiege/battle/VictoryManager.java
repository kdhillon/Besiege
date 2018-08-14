package kyle.game.besiege.battle;

import kyle.game.besiege.Kingdom;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.location.Village;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.party.*;

public class VictoryManager {
    private final int BASE_MORALE_REWARD = 25;
    private static final int EXP_FACTOR = 3; // how much more exp is given to winning playerPartyPanel than total atk of enemies
    private static final int BASE_EXP = 1;
    private static final int MIN_EXP = 500;
    private static final float MIN_BALANCE = .3f;
    public static final float BASE_WEAPON_DROP_CHANCE = 0.2f;
    public static final float BASE_ARMOR_DROP_CHANCE = 0.2f;

    private final Kingdom kingdom;
    private final Location siegeOf;
    private final Battle battle;

    // Initial balance defenders
    private final double initBalanceD;

    // Number of troops at start
    private int initialBattleSize;

    // Exp to be distributed if this team wins
    private int expA;
    private int expD;

    // Wealth to be gained after battle.
    private int spoils;

    private StrictArray<WeaponType> weaponLoot;
    private StrictArray<RangedWeaponType> rangedLoot;
    private StrictArray<ArmorType> armorLoot;

    public VictoryManager(Kingdom kingdom, Battle battle, Location siegeOf, double initBalanceD) {
        this.kingdom = kingdom;
        this.siegeOf = siegeOf;
        this.battle = battle;
        this.initBalanceD = initBalanceD;

        // TODO if we have an inventory system
        weaponLoot = new StrictArray<WeaponType>();
        rangedLoot = new StrictArray<RangedWeaponType>();
        armorLoot = new StrictArray<ArmorType>();
    }

    // Call this anytime you add troops to the battle (but before the battle has started)
    public void addInitTroopCount(int troops) {
        initialBattleSize += troops;
    }

    // Store loot, etc for this soldier
    public void handleCasualty(Soldier soldier, boolean wasInAttackers, boolean killed) {
        // add to total exp sum
        if (wasInAttackers) expD += soldier.getExpForKill();
        else expA += soldier.getExpForKill();

        // add loot to loot drop
        if (killed) {
            if (Math.random() < BASE_WEAPON_DROP_CHANCE)
                this.weaponLoot.add(soldier.getWeapon());
            if (soldier.getRanged() != null && Math.random() < BASE_WEAPON_DROP_CHANCE)
                this.rangedLoot.add(soldier.getRanged());
            if (!soldier.getArmor().clothes && Math.random() < BASE_ARMOR_DROP_CHANCE)
                this.armorLoot.add(soldier.getArmor());
        }
    }

    public void handleArmyRetreat(Army army) {
        double wealthFactor = army.getParty().getWoundedSize() * 1.0 / army.getParty().getTotalSize();
        int wealthChange = (int) (army.getParty().wealth * wealthFactor);
        army.getParty().wealth -= wealthChange;
        spoils += wealthChange;
    }

    // change faction of city if siegeOrRaid
    public void handleVictory(StrictArray<Party> attackingParties, StrictArray<Party> defendingParties, boolean didAtkWin) {
        StrictArray<Party> victor, loser;
        if (didAtkWin) {
            victor = attackingParties;
            loser = defendingParties;
        } else {
            victor = defendingParties;
            loser = attackingParties;
        }

        int[] victorContribution = new int[victor.size]; // should depend on how much an army sacrificed in battle
        int totalContribution = 0; // maybe number of troops they killed + their own troops killed.

        victor.shrink();

        // Calculate the contribution for each party
        for (int i = 0; i < victor.size; i++) {
            Party party = victor.get(i);
            victorContribution[i] = party.getTotalLevel();
            totalContribution += victorContribution[i];
        }

        // Distribute rewards
        for (int i = 0; i < victor.size; i++) {
            Party party = victor.get(i);

            double contribution = victorContribution[i]/1.0d/totalContribution;
            party.registerBattleVictory();
            distributeRewards(party, contribution, didAtkWin);
        }

        for (int i = 0; i < loser.size; i++) {
            Party party = loser.get(i);
            party.registerBattleLoss();
        }

        // Handle siegeOrRaid victory
        if (siegeOf != null) {
            if (didAtkWin) {
                if (siegeOf.isVillage()) {
                    Army attackingArmy = battle.getAttackingParties().first().army;
                    if (attackingArmy != null) {
                        ((Village) siegeOf).handleRaidVictory(attackingArmy);
                    }
                } else {
                    siegeOf.getSiege().siegeSuccess();
                }
            } else {
                siegeOf.getSiege().siegeFailure();
            }
        }
    }

    private void distributeRewards(Party party, double contribution, boolean attackVictory) {
        int reward = (int) (contribution*spoils);
        int expReward;
        int moraleReward;
        if (attackVictory) {
            expReward = (int) (contribution*expA);
            // Morale increases inversely with initial balance
            moraleReward = (int) (initBalanceD * BASE_MORALE_REWARD);
        }
        else {
            expReward = (int) (contribution*expD);
            // Morale increases inversely with initial balance
            moraleReward = (int) ((1-initBalanceD)* BASE_MORALE_REWARD);
        }
        expReward *= EXP_FACTOR; // just to beef it up
        expReward += MIN_EXP;

        // TODO expand this to non-player parties;
        if (party.player) {
            // also distribute honor and fame
            int fameReward;
            if (attackVictory) {
                fameReward = (int) (this.initBalanceD * this.initialBattleSize) / 5;
                if (this.initBalanceD < MIN_BALANCE) fameReward = 0;
            } else {
                fameReward = (int) (getBalanceA() * this.initialBattleSize) / 5;
                if (getBalanceA() < MIN_BALANCE) fameReward = 0;
            }
            // Give the player some stuff
            if (kingdom != null) {
                kingdom.getMapScreen().getCharacter().addFame(fameReward);
                // Add collected loot
                if (Soldier.WEAPON_NEEDED) {
                    kingdom.getMapScreen().getCharacter().inventory.addWeapons(weaponLoot);
                    kingdom.getMapScreen().getCharacter().inventory.addRanged(rangedLoot);
                    kingdom.getMapScreen().getCharacter().inventory.addArmor(armorLoot);
                }
            }
            BottomPanel.log(party.getName() + " receives " + moraleReward + " morale, " + fameReward + " fame, " + reward + " gold and " + expReward + " experience!", "green");

            if (weaponLoot.size > 0 || rangedLoot.size > 0 || armorLoot.size > 0) {
                String lootString = party.getName() + " looted ";
                if (weaponLoot.size > 0) lootString += weaponLoot.size + " weapons, ";
                if (rangedLoot.size > 0) lootString += rangedLoot.size + " ranged weapons, ";
                if (armorLoot.size > 0) lootString += armorLoot.size + " armor!";
                BottomPanel.log(lootString, "green");
            }
        }
        party.wealth += reward;
        party.distributeExp(expReward);
        // TODO add momentum
//        army.setMomentum(army.getMomentum()+moraleReward);
    }

    private double getBalanceA() {
        return 1-this.initBalanceD;
    }
}
