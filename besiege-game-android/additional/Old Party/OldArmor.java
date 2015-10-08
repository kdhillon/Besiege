package kyle.game.besiege.party;

import kyle.game.besiege.party.Equipment.Type;

import com.badlogic.gdx.graphics.Color;

public enum OldArmor {
	//	if (Math.random() < .2f)
	//		this.armorTint = Color.DARK_GRAY;
	//	else if (Math.random() < 0.5f) 
	//		this.armorTint = new Color(120/256.0f, 95/256.0f, 75/256.0f, 1);
	//	else if (Math.random() < 0.2f){
	//		this.armorTint = Color.GRAY;
	//	}
	//	else if (Math.random() < 0.2f) {
	//		this.armorTint = new Color(.4f, .1f, .1f, 1);
	//	}
	//	else if (Math.random() < 0.2f) {
	//		this.armorTint = new Color(.1f, .1f, .4f, 1);
	//	}
	//	else this.armorTint = new Color(.3f, .2f, .15f, 1);

	//	enum ArmorType {Clothes, Leather, Studded, Robes, Chainmail, LightPlate, HeavyPlate};

	// if you want multiple colors for one armor type, specify in "generate color"
	// color, armor bonus, speed bonus, name
	CLOTHES (null, 0, 0, "Clothes"),	
	LEATHER (null, 1, 0, "Leather Armor"),
	STUDDED (new Color(.25f, .2f, .15f, 1), 1, 0, "Studded Armor"),
	ROBES 	(new Color(.22f, .2f, .13f, 1), 1, 0, "Robes"),

	CHAINMAIL (new Color(.3f, .3f, .35f, 1), 1, 0, "Chainmail"),
	LIGHTPLATE (new Color(.4f, .4f, .4f, 1), 1, 0, "Light Platemail"),
	HEAVYPLATE (new Color(.2f, .2f, .2f, 1), 1, 0, "Heavy Platemail"),
	;

	Color color;
	int armorBonus;
	int speedBonus;
	public String name;

	// Tier 2?
	private OldArmor(Color color, int armorBonus, int speedBonus, String name) {
		this.color = color;
		this.armorBonus = armorBonus;
		this.speedBonus = speedBonus;
		this.name = name;
	}

	public static Color generateColor(OldArmor oldArmor) {
		if (oldArmor.color != null) return oldArmor.color;

		double rand = Math.random();
		switch (oldArmor) {
		case CLOTHES: 
			if (rand < 0.33)
				return new Color(.3f, .2f, .15f, 1);
			else if (rand < 0.66) 
				return new Color(.2f, .2f, .35f, 1);
			else 
				return new Color(.35f, .2f, .2f, 1);
		case LEATHER:
			if (rand < 0.33)
				return new Color(.24f, .2f, .13f, 1);
			else if (rand < 0.66) 
				return new Color(.2f, .15f, .10f, 1);
			else 
				return new Color(.3f, .2f, .15f, 1);	
		}
		return null;
	}
	
	public static OldArmor armorForWeapon(OldWeapon oldWeapon) {
		return oldWeapon.oldArmor;
	}
}
