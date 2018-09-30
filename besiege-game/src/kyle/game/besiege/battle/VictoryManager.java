package kyle.game.besiege.battle;

import kyle.game.besiege.Kingdom;
import kyle.game.besiege.Siege;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.location.Village;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.panels.SidePanel;
import kyle.game.besiege.party.*;

import java.util.HashMap;

public class VictoryManager {
    private final int BASE_MORALE_REWARD = 25;
    private static final int EXP_FACTOR = 3; // how much more exp is given to winning playerPartyPanel than total atk of enemies
    private static final int BASE_EXP = 1;
    private static final int MIN_EXP = 500;
    private static final float MIN_BALANCE = .3f;
    public static final float BASE_WEAPON_DROP_CHANCE = 0.2f;
    public static final float BASE_ARMOR_DROP_CHANCE = 0.2f;

    // If party has this many healthy troops or fewer at battle end, fully destroy party.
    public static final int PARTY_DESTROY_THRESHOLD = 2;

    private final Kingdom kingdom;
    private final Siege siege;
    private final Battle battle;

    // Initial balance defenders
    private final double initBalanceD;

    // Number of troops at start
    private int initialBattleSize;

    // Exp to be distributed if this team wins
    public int expA;
    public int expD;

    // Wealth to be gained after battle.
    public int spoils;

    // Per-party rewards
    public HashMap<Party, Integer> fameRewards = new HashMap<>();
    public HashMap<Party, Integer> moraleRewards = new HashMap<>();
    public HashMap<Party, Integer> expRewards = new HashMap<>();
    public HashMap<Party, Integer> wealthRewards = new HashMap<>();
//    public HashMap<Party, Integer> troopsKilled = new HashMap<>();
//    public HashMap<Party, Integer> troopsWounded = new HashMap<>();

    // holds troops that were killed/wounded in this battle for each side.
    private HashMap<Party, StrictArray<Soldier>> woundedMap = new HashMap<>();
    private HashMap<Party, StrictArray<Soldier>> killedMap = new HashMap<>();

    public int aTroopsKilled;
    public int aTroopsWounded;
    public int dTroopsKilled;
    public int dTroopsWounded;

    // TODO add prisoners to be gained/lost

    private StrictArray<WeaponType> weaponLoot;
    private StrictArray<RangedWeaponType> rangedLoot;
    private StrictArray<ArmorType> armorLoot;

    public VictoryManager(Kingdom kingdom, Battle battle, Siege siege, double initBalanceD) {
        this.kingdom = kingdom;
        this.siege = siege;
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

        // add arbitrary value for now.
        // TODO flesh out.
        spoils += 5;

        if (wasInAttackers) {
            if (killed) aTroopsKilled++;
            else aTroopsWounded++;
        } else {
            if (killed) dTroopsKilled++;
            else dTroopsWounded++;
        }

        if (killed) {
//            addTo(troopsKilled, soldier.party, 1);
            addTo(killedMap, soldier.party, soldier);
        } else {
//            addTo(troopsWounded, soldier.party, 1);
            addTo(woundedMap, soldier.party, soldier);
        }

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

    private void addTo(HashMap<Party, Integer> map, Party p, int value) {
        if (map.containsKey(p)) {
            map.put(p, map.get(p) + value);
        } else map.put(p, value);
    }

    private void addTo(HashMap<Party, StrictArray<Soldier>> map, Party p, Soldier s) {
        if (!map.containsKey(p)) {
            map.put(p, new StrictArray<Soldier>());
        }
        map.get(p).add(s);
    }

    public StrictArray<Soldier> getKilledSoldiersIn(Party p) {
        if (killedMap.containsKey(p)) return killedMap.get(p);
        else return new StrictArray<>();
    }

    public StrictArray<Soldier> getWoundedSoldierIn(Party p) {
        if (woundedMap.containsKey(p)) return woundedMap.get(p);
        else return new StrictArray<>();
    }
//    public void handleArmyRetreat(Army army) {
//        double wealthFactor = army.getParty().getWoundedSize() * 1.0 / army.getParty().getTotalSize();
//        int wealthChange = (int) (army.getParty().wealth * wealthFactor);
//        army.getParty().wealth -= wealthChange;
//        spoils += wealthChange;
//    }

    // change faction of city if siegeOrRaid
    public void handleVictory(StrictArray<Party> attackingParties,
                              StrictArray<Party> defendingParties,
                              StrictArray<Party> attackingPartiesRetreated,
                              StrictArray<Party> defendingPartiesRetreated,
                              boolean didAtkWin) {
        StrictArray<Party> victor, loser;
        StrictArray<Party> victorRetreated, loserRetreated;
        if (didAtkWin) {
            victor = attackingParties;
            loser = defendingParties;
            victorRetreated = attackingPartiesRetreated;
            loserRetreated = defendingPartiesRetreated;
        } else {
            victor = defendingParties;
            loser = attackingParties;
            victorRetreated = defendingPartiesRetreated;
            loserRetreated = attackingPartiesRetreated;
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

        //  when army loses all its troops, keep it in battle inside "battle.retreatedparties" until end. then dole out penalties appropriately.
        // Destroy losers
        if (loser.size != 0) throw new AssertionError();
        for (int i = 0; i < loserRetreated.size; i++) {
            Party party = loserRetreated.get(i);
            party.registerBattleLoss();
            System.out.println("Healthy troops: " + party.getHealthySize());

            // Kick losers out of siege
            if (siege != null) {
                if (!didAtkWin) {
                    if (party.army != null)
                        party.army.leaveSiege();
                }
            }
            if (party.player) {
                int playerTroopsKilled = aTroopsKilled;
                int playerTroopsWounded = aTroopsWounded;
                if (battle.playerDefending()) {
                    playerTroopsKilled = dTroopsKilled;
                    playerTroopsWounded = dTroopsWounded;
                }

                BottomPanel.log("Battle lost!", "red");
                BottomPanel.log("Troops lost: " + playerTroopsKilled, "red");
                BottomPanel.log("Troops wounded: " + playerTroopsWounded, "orange");
            }
            if (party.getHealthySize() <= PARTY_DESTROY_THRESHOLD) {
                System.out.println("Destroying losing party");
                Army army = party.army;
                if (army != null) {
                    if (army.player) {
                        BottomPanel.log("PLAYER DESTROYED", "red");
                    }
                    army.endBattle();
                    army.setStopped(false);
                    army.setVisible(false);
                    army.destroy();
                }
            }
        }

        // Handle siegeOrRaid victory
        if (siege != null) {
            if (didAtkWin) {
                System.out.println("handling siege success at " + siege.getName());
                if (siege.location.isVillage()) {
                    Army attackingArmy = battle.getAttackingParties().first().army;
                    if (attackingArmy != null) {
                        ((Village) siege.location).handleRaidVictory(attackingArmy);
                    }
                } else {
                    siege.siegeSuccess();
                }
            } else {
                System.out.println("handling siege failure at " + this.siege.getName());
                siege.siegeFailure();
            }
        }
    }

    private void distributeRewards(Party party, double contribution, boolean attackVictory) {
        int wealthReward = (int) (contribution*spoils);
        int expReward;
        int moraleReward;
        int fameReward = 0;
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
            BottomPanel.log(party.getName() + " receives " + moraleReward + " morale, " + fameReward + " fame, " + wealthReward + " wealth and " + expReward + " experience!", "green");

            if (weaponLoot.size > 0 || rangedLoot.size > 0 || armorLoot.size > 0) {
                String lootString = party.getName() + " looted ";
                if (weaponLoot.size > 0) lootString += weaponLoot.size + " weapons, ";
                if (rangedLoot.size > 0) lootString += rangedLoot.size + " ranged weapons, ";
                if (armorLoot.size > 0) lootString += armorLoot.size + " armor!";
                BottomPanel.log(lootString, "green");
            }
        }

        addTo(expRewards, party, expReward);
        addTo(moraleRewards, party, moraleReward);
        addTo(fameRewards, party, fameReward);
        addTo(wealthRewards, party, wealthReward);

        party.wealth += wealthReward;
        party.distributeExp(expReward);
        // TODO add momentum
//        army.setMomentum(army.getMomentum()+moraleReward);
    }

    private double getBalanceA() {
        return 1-this.initBalanceD;
    }
}
