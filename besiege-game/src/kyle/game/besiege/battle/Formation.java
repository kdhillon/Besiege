package kyle.game.besiege.battle;

import kyle.game.besiege.battle.BattleStage;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;

import com.badlogic.gdx.utils.Array;

public enum Formation {

	SCRAMBLE("Scramble"), LINE("Line"), DEFENSIVE_LINE("Defensive"), VEE("Vee"), FLANKING("Flanking"), SPREAD_LINE("Spread"), WALL_LINE("Line (Wall)");
	public String name;
	private Formation(String name) {
		this.name = name;
	}

	public static Soldier.SoldierType[][] getFormation(BattleParty party, Formation formationChoice, int size_x, int size_y) {
		Soldier.SoldierType[][] formation;

		Array<Soldier> infantry = party.getHealthyInfantry();
		Array<Soldier> cavalry = party.getHealthyCavalry();
		Array<Soldier> archers = party.getHealthyArchers();

		int iCount = infantry.size;
		int aCount = archers.size;
		int cCount = cavalry.size;

		// figure out what formation to do
		// Scramble:  C A I C C I A 
		//			  A I C A I C A
		if (formationChoice == Formation.SCRAMBLE) {
			int formation_width = (iCount + aCount + cCount);
			int formation_height = BattleStage.PLACE_HEIGHT;

			formation = new Soldier.SoldierType[formation_height][formation_width];
			
			while (iCount > 0 || aCount > 0 || cCount > 0) {
				System.out.println("randomly creating scramble");
				int x = (int) (Math.random() * formation_width);
				int y = (int) (Math.random() * formation_height);
				if (formation[y][x] == null) {
					if (iCount > 0) {
						formation[y][x] = Soldier.SoldierType.INFANTRY;
						iCount--;
					}
					else if (aCount > 0) {
						formation[y][x] = Soldier.SoldierType.ARCHER;
						aCount--;
					}
					else if (cCount > 0) {
						formation[y][x] = Soldier.SoldierType.CAVALRY;
						cCount--;
					}
				}
			}
		} 
		// Line:   CCCC IIII AAAAA IIII CCCC
		else if (formationChoice == Formation.LINE) {
			int formation_width = iCount + aCount + cCount;
			int formation_height = 1;

			formation = new Soldier.SoldierType[formation_height][formation_width];

			int cavalry_left = (int) (cCount/2.0);
			int cavalry_right = cCount - cavalry_left;
			int infantry_right = (int) (iCount/2.0);
			int infantry_left = iCount - infantry_right;

			for (int i = 0; i < cavalry_left; i++) {
				formation[0][i] = Soldier.SoldierType.CAVALRY;
			}
			for (int i = 0; i < infantry_left; i++) {
				formation[0][i + cavalry_left] = Soldier.SoldierType.INFANTRY;
			}
			for (int i = 0; i < aCount; i++) {
				formation[0][i + infantry_left + cavalry_left] = Soldier.SoldierType.ARCHER;
			}
			for (int i = 0; i < infantry_right; i++) {
				formation[0][i + aCount + infantry_left + cavalry_left] = Soldier.SoldierType.INFANTRY;
			}
			for (int i = 0; i < cavalry_right; i++) {
				formation[0][i + infantry_right + aCount + infantry_left + cavalry_left] = Soldier.SoldierType.CAVALRY;
			}
		} 
		// Wall line - put archers on outside, infantry and cavalry in middle
		else if (formationChoice == Formation.WALL_LINE) {
			int formation_width = iCount + aCount + cCount;
			int formation_height = 1;

			formation = new Soldier.SoldierType[formation_height][formation_width];

			int archers_left = (int) (aCount/2.0);
			int archers_right = aCount - archers_left;
			int cavalry_right = (int) (cCount/2.0);
			int cavalry_left = cCount - cavalry_right;

			for (int i = 0; i < archers_left; i++) {
				formation[0][i] = Soldier.SoldierType.ARCHER;
			}
			for (int i = 0; i < cavalry_left; i++) {
				formation[0][i + archers_left] = Soldier.SoldierType.CAVALRY;
			}
			for (int i = 0; i < iCount; i++) {
				formation[0][i + archers_left + cavalry_left] = Soldier.SoldierType.INFANTRY;
			}
			for (int i = 0; i < cavalry_right; i++) {
				formation[0][i + iCount + archers_left + cavalry_left] = Soldier.SoldierType.CAVALRY;
			}
			for (int i = 0; i < archers_right; i++) {
				formation[0][i + cavalry_left + iCount + archers_left + cavalry_right] = Soldier.SoldierType.ARCHER;
			}
		} 
		// Spread Line:   C C C C I I I I A A A A A I I I I C C C C
		else if (formationChoice == Formation.SPREAD_LINE) {
			int formation_width = (iCount*2 + aCount*2 + cCount*2);
			int formation_height = 1;

			formation = new Soldier.SoldierType[formation_height][formation_width];

			int cavalry_left = (int) (cCount/2.0)*2;
			int cavalry_right = cCount*2 - cavalry_left;
			int infantry_right = (int) (iCount/2.0)*2;
			int infantry_left = iCount*2 - infantry_right;

			boolean skip = false;

			for (int i = 0; i < cavalry_left; i++) {
				skip = !skip;
				if (skip == true) continue;
				formation[0][i] = Soldier.SoldierType.CAVALRY;
			}
			for (int i = 0; i < infantry_left; i++) {
				skip = !skip;
				if (skip == true) continue;
				formation[0][i + cavalry_left] = Soldier.SoldierType.INFANTRY;
			}
			for (int i = 0; i < aCount*2; i++) {
				skip = !skip;
				if (skip == true) continue;
				formation[0][i + infantry_left + cavalry_left] = Soldier.SoldierType.ARCHER;
			}
			for (int i = 0; i < infantry_right; i++) {
				skip = !skip;
				if (skip == true) continue;
				formation[0][i + aCount*2 + infantry_left + cavalry_left] = Soldier.SoldierType.INFANTRY;
			}
			for (int i = 0; i < cavalry_right; i++) {
				skip = !skip;
				if (skip == true) continue;
				formation[0][i + infantry_right + aCount*2 + infantry_left + cavalry_left] = Soldier.SoldierType.CAVALRY;
			}
		} 
		//    Defensive Line
		// CCCCC IIIII CCCCC
		//
		//    AAAAAAAAAAA
		else if (formationChoice == Formation.DEFENSIVE_LINE) {

			int top_row = iCount + cCount;
			int bottom_row = aCount;

			int bottom_start_left = Math.max(0, top_row - bottom_row)/2;
			int top_start_left = Math.max(0, bottom_row - top_row)/2;

			int formation_width = Math.max(top_row, bottom_row);
			int formation_height = 2;

			formation = new Soldier.SoldierType[formation_height][formation_width];


			int cavalry_left = (int) (cCount/2.0);
			int cavalry_right = cCount - cavalry_left;
			//			int infantry_right = (int) (iCount/2.0);
			//			int infantry_left = iCount - infantry_right;

			for (int i = 0; i < cavalry_left; i++) {
				formation[formation_height-1][i + top_start_left] = Soldier.SoldierType.CAVALRY;
			}
			for (int i = 0; i < infantry.size; i++) {
				formation[formation_height-1][i + top_start_left + cavalry_left] = Soldier.SoldierType.INFANTRY;
			}
			for (int i = 0; i < cavalry_right; i++) {
				formation[formation_height-1][i + top_start_left + infantry.size + cavalry_left] = Soldier.SoldierType.CAVALRY;
			}


			for (int i = 0; i < archers.size; i++) {
				formation[0][i + bottom_start_left] = Soldier.SoldierType.ARCHER;
			}
		} 
		//   Vee Formation
		// CC            CC
		//   CC	       CC
		//     II    II 
		//       IIII
		//    AAAAAAAAAAA
		// 
		else if (formationChoice == Formation.VEE) {

			int top_row = iCount + cCount;
			int bottom_row = aCount;

			int archer_separation = 2;

			int bottom_start_left = Math.max(0, top_row - bottom_row)/2;
			int top_start_left = Math.max(0, bottom_row - top_row)/2;

			int formation_width = Math.max(top_row, bottom_row);
			System.out.println("width: " + formation_width);
			int formation_height = top_row / 4 + archer_separation + 1;
			System.out.println("height: " + formation_height);

			formation = new Soldier.SoldierType[formation_height][formation_width];


			int cavalry_left = (int) (cCount/2.0);
			int cavalry_right = cCount - cavalry_left;

			int current_height = formation_height - 1;
			int height_change = -1; // either 1 or -1 
			boolean next_row = true;

			for (int i = 0; i < cavalry_left; i++) {
				formation[current_height][i + top_start_left] = Soldier.SoldierType.CAVALRY;
				next_row = !next_row;
				if (next_row) {
					current_height += height_change;
					if (current_height < archer_separation) height_change = -height_change;
				}
			}
			for (int i = 0; i < infantry.size; i++) {
				formation[current_height][i + top_start_left + cavalry_left] = Soldier.SoldierType.INFANTRY;
				next_row = !next_row;
				if (next_row) {
					current_height += height_change;
					if (current_height < archer_separation) height_change = -height_change;
				}
			}
			for (int i = 0; i < cavalry_right; i++) {
				formation[current_height][i + top_start_left + infantry.size + cavalry_left] = Soldier.SoldierType.CAVALRY;
				next_row = !next_row;
				if (next_row) {
					current_height += height_change;
					if (current_height < archer_separation) height_change = -height_change;
				}
			}

			for (int i = 0; i < archers.size; i++) {
				formation[0][i + bottom_start_left] = Soldier.SoldierType.ARCHER;
			}
		} 
		// Flanking formation
		//  CCCCC            IIIIIII              CCCCC
		//                   AAAAAAA
		else if (formationChoice == Formation.FLANKING) {
			int cavalry_separation = Math.max(0, size_x/2 - cCount);
			cavalry_separation = 5;

			int top_row = iCount + cCount + 2*cavalry_separation;
			int bottom_row = aCount;

			int bottom_start_left = Math.max(0, top_row - bottom_row)/2;
			int top_start_left = Math.max(0, bottom_row - top_row)/2;

			//			System.out.println("top row: " + top_row);

			int formation_width = Math.max(top_row, bottom_row);
			int formation_height = 3;

			formation = new Soldier.SoldierType[formation_height][formation_width];


			int cavalry_left = (int) (cCount/2.0);
			int cavalry_right = cCount - cavalry_left;
			//			int infantry_right = (int) (iCount/2.0);
			//			int infantry_left = iCount - infantry_right;

			for (int i = 0; i < cavalry_left; i++) {
				formation[formation_height-1][i + top_start_left] = Soldier.SoldierType.CAVALRY;
			}
			for (int i = 0; i < infantry.size; i++) {
				formation[formation_height-1][i + top_start_left + cavalry_separation + cavalry_left] = Soldier.SoldierType.INFANTRY;
			}
			for (int i = 0; i < cavalry_right; i++) {
				formation[formation_height-1][i + top_start_left + infantry.size + cavalry_left + cavalry_separation*2] = Soldier.SoldierType.CAVALRY;
			}


			for (int i = 0; i < archers.size; i++) {
				formation[0][i + bottom_start_left] = Soldier.SoldierType.ARCHER;
			}
		} 
		else formation = null;
		return formation;
	}
}
