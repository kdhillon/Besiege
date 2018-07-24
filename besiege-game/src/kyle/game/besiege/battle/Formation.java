package kyle.game.besiege.battle;

import com.badlogic.gdx.utils.Array;

import kyle.game.besiege.party.Soldier;

public enum Formation {

	SCRAMBLE("Scramble"), LINE("Line"), DEFENSIVE_LINE("Defensive"), VEE("Vee"), FLANKING("Flanking"), SPREAD_LINE("Spread"), SQUARE("Square"), WALL_LINE("Line (Wall)");
	public String name;
	Formation(String name) {
		this.name = name;
	}

	public Soldier.SoldierType[][] getFormation(BattleSubParty party) {
		Soldier.SoldierType[][] formation;

		Array<Soldier> infantry = party.getHealthyInfantry();
		Array<Soldier> cavalry = party.getHealthyCavalry();
		Array<Soldier> archers = party.getHealthyArchers();

		int iCount = party.infantrySizeWithoutGeneral();
		int aCount = party.archersSizeWithoutGeneral();
		int cCount = party.cavalrySizeWithoutGeneral();

		System.out.println("total size: " + iCount + " " + aCount + " " + cCount);

		// no soldiers
		if (iCount + aCount + cCount == 0) {
			formation = new Soldier.SoldierType[1][1];
			formation[0][0] = Soldier.SoldierType.GENERAL;
			return formation;
		};

		// figure out what formation to do
		// Scramble:  C A I C C I A 
		//			  A I C A I C A
		if (this == Formation.SCRAMBLE) {
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
		else if (this == Formation.LINE) {
			int formation_width = iCount + aCount + cCount;
			int formation_height = 2;

			formation = new Soldier.SoldierType[formation_height][formation_width];

			int cavalry_left = (int) (cCount/2.0);
			int cavalry_right = cCount - cavalry_left;
			int infantry_right = (int) (iCount/2.0);
			int infantry_left = iCount - infantry_right;

			for (int i = 0; i < cavalry_left; i++) {
				formation[1][i] = Soldier.SoldierType.CAVALRY;
			}
			for (int i = 0; i < infantry_left; i++) {
				formation[1][i + cavalry_left] = Soldier.SoldierType.INFANTRY;
			}
			for (int i = 0; i < aCount; i++) {
				formation[1][i + infantry_left + cavalry_left] = Soldier.SoldierType.ARCHER;
			}
			for (int i = 0; i < infantry_right; i++) {
				formation[1][i + aCount + infantry_left + cavalry_left] = Soldier.SoldierType.INFANTRY;
			}
			for (int i = 0; i < cavalry_right; i++) {
				formation[1][i + infantry_right + aCount + infantry_left + cavalry_left] = Soldier.SoldierType.CAVALRY;
			}
			formation[0][formation_width/2] = Soldier.SoldierType.GENERAL;
		} 
		// Wall line - put archers on outside, infantry and cavalry in middle
		else if (this == Formation.WALL_LINE) {
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
		else if (this == Formation.SPREAD_LINE) {
			int formation_width = (iCount*2 + aCount*2 + cCount*2);
			int formation_height = 2;

			formation = new Soldier.SoldierType[formation_height][formation_width];

			int cavalry_left = (int) (cCount/2.0)*2;
			int cavalry_right = cCount*2 - cavalry_left;
			int infantry_right = (int) (iCount/2.0)*2;
			int infantry_left = iCount*2 - infantry_right;

			boolean skip = false;

			for (int i = 0; i < cavalry_left; i++) {
				skip = !skip;
				if (skip == true) continue;
				formation[1][i] = Soldier.SoldierType.CAVALRY;
			}
			for (int i = 0; i < infantry_left; i++) {
				skip = !skip;
				if (skip == true) continue;
				formation[1][i + cavalry_left] = Soldier.SoldierType.INFANTRY;
			}
			for (int i = 0; i < aCount*2; i++) {
				skip = !skip;
				if (skip == true) continue;
				formation[1][i + infantry_left + cavalry_left] = Soldier.SoldierType.ARCHER;
			}
			for (int i = 0; i < infantry_right; i++) {
				skip = !skip;
				if (skip == true) continue;
				formation[1][i + aCount*2 + infantry_left + cavalry_left] = Soldier.SoldierType.INFANTRY;
			}
			for (int i = 0; i < cavalry_right; i++) {
				skip = !skip;
				if (skip == true) continue;
				formation[1][i + infantry_right + aCount*2 + infantry_left + cavalry_left] = Soldier.SoldierType.CAVALRY;
			}
			formation[0][formation_width/2] = Soldier.SoldierType.GENERAL;
		} 
		//    Defensive Line
		// CCCCC IIIII CCCCC
		//    AAAAAAAAAAA
		//         G
		else if (this == Formation.DEFENSIVE_LINE) {

			int top_row = iCount + cCount;
			int bottom_row = aCount;

			int bottom_start_left = Math.max(0, top_row - bottom_row)/2;
			int top_start_left = Math.max(0, bottom_row - top_row)/2;

			int formation_width = Math.max(1, Math.max(top_row, bottom_row));
			int formation_height = 3;

			formation = new Soldier.SoldierType[formation_height][formation_width];

			int cavalry_left = (int) (cCount/2.0);
			int cavalry_right = cCount - cavalry_left;
			//			int infantry_right = (int) (iCount/2.0);
			//			int infantry_left = iCount - infantry_right;

			for (int i = 0; i < cavalry_left; i++) {
				formation[formation_height-1][i + top_start_left] = Soldier.SoldierType.CAVALRY;
			}
			for (int i = 0; i < iCount; i++) {
				formation[formation_height-1][i + top_start_left + cavalry_left] = Soldier.SoldierType.INFANTRY;
			}
			for (int i = 0; i < cavalry_right; i++) {
				formation[formation_height-1][i + top_start_left + iCount + cavalry_left] = Soldier.SoldierType.CAVALRY;
			}

			for (int i = 0; i < aCount; i++) {
				formation[1][i + bottom_start_left] = Soldier.SoldierType.ARCHER;
			}
			formation[0][formation_width / 2] = Soldier.SoldierType.GENERAL;
		} 
		//   Vee Formation
		// CC            CC
		//   CC	       CC
		//     II    II 
		//       IIII
		//    AAAAAAAAAA
		else if (this == Formation.VEE) {

			int top_row = iCount + cCount;
			int bottom_row = aCount;

			int archer_separation = 2;

			int bottom_start_left = Math.max(0, top_row - bottom_row)/2;
			int top_start_left = Math.max(0, bottom_row - top_row)/2;

			int formation_width = Math.max(top_row, bottom_row);
			System.out.println("width: " + formation_width);
			int formation_height = top_row / 4 + archer_separation + 2;
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
			for (int i = 0; i < iCount; i++) {
				formation[current_height][i + top_start_left + cavalry_left] = Soldier.SoldierType.INFANTRY;
				next_row = !next_row;
				if (next_row) {
					current_height += height_change;
					if (current_height < archer_separation) height_change = -height_change;
				}
			}
			for (int i = 0; i < cavalry_right; i++) {
				formation[current_height][i + top_start_left + iCount + cavalry_left] = Soldier.SoldierType.CAVALRY;
				next_row = !next_row;
				if (next_row) {
					current_height += height_change;
					if (current_height < archer_separation) height_change = -height_change;
				}
			}

			for (int i = 0; i < aCount; i++) {
				formation[1][i + bottom_start_left] = Soldier.SoldierType.ARCHER;
			}
			
			formation[0][formation_width/2] = Soldier.SoldierType.GENERAL;
		} 
		// Flanking formation
		//  CCCCC            IIIIIII              CCCCC
		//                   AAAAAAA
		//					    G
		else if (this == Formation.FLANKING) {
			int cavalry_separation = Math.max(0, 10 - cCount);
			cavalry_separation = 5;

			if (cCount == 0) cavalry_separation = 0;

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
			for (int i = 0; i < iCount; i++) {
				formation[formation_height-1][i + top_start_left + cavalry_separation + cavalry_left] = Soldier.SoldierType.INFANTRY;
			}
			for (int i = 0; i < cavalry_right; i++) {
				formation[formation_height-1][i + top_start_left + iCount + cavalry_left + cavalry_separation*2] = Soldier.SoldierType.CAVALRY;
			}

			for (int i = 0; i < aCount; i++) {
				formation[1][i + bottom_start_left] = Soldier.SoldierType.ARCHER;
			}
			
			formation[0][formation_width/2] = Soldier.SoldierType.GENERAL;
		} 
		
		// Square: 
		//				CCCCCII
		//				IIIIIII
		//				IIIIIII
		//				IIIIIAA
		//				AAAAAAA
		//				AAAAAAA
		//				AAA
		//				   G
		else if (this == Formation.SQUARE) {
			int formation_width = (int) (Math.max(1, Math.sqrt(iCount + aCount + cCount)));
			double remaining = Math.sqrt(iCount + aCount + cCount) - formation_width;
			if (remaining > 0) formation_width += 1;
			int formation_height = formation_width + 1;

			formation = new Soldier.SoldierType[formation_height][formation_width];

			for (int row = formation_height-1; row > 0; row--) {
				for (int col = 0; col < formation_width; col++) {
					if (cCount > 0) {
						formation[row][col] = Soldier.SoldierType.CAVALRY;
						cCount--;
					}
					else if (iCount > 0) {
						formation[row][col] = Soldier.SoldierType.INFANTRY;
						iCount--;
					}
					else if (aCount > 0) {
						formation[row][col] = Soldier.SoldierType.ARCHER;
						aCount--;
					}
					else break;
				}
			}
			
			formation[0][formation_width/2] = Soldier.SoldierType.GENERAL;
		} 
		else {
		    formation = null;
		    throw new AssertionError();
        }
		return formation;
	}
}
