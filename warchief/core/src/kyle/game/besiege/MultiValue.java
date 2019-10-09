package kyle.game.besiege;


// represents a value that's calculated from multiple other values
// can print information about how this was calculated
// for example, a soldiers' attack might be calculated from base attack, weapon, general.

import kyle.game.besiege.party.UnitType;

import java.text.DecimalFormat;

// main issue is storage space, this kinda thing takes up a lot of unnecessary space.
// Don't calculate this for every single unit, you dingus. only when they enter a battle.
public class MultiValue implements Value {
	String multiName;
	String multiDesc;

	public static DecimalFormat df = new DecimalFormat("0.00");

	// hopefully will save some space?
	public enum TypeInfo {
		S_BASE_ATK("Base", "Base Attack"), 
		S_BASE_DEF("Base", "Base Defense"),
		S_BASE_SPD("Base", "Base Speed"),
		S_BASE_HP("Base", "Base HP"),
		S_WEAPON("Weapon", "Weapon Bonus"),
		S_ARMOR("Armor", "Armor Bonus"),
		S_GENERAL("General", "General Bonus"),
		S_HP_DEF("Def", "Defense Bonus"),
		S_SHIELD("Shield", "Shield Bonus"),
;
		
		String name, desc;
		private TypeInfo(String name, String desc) {
			this.name= name;
			this.desc = desc;
		}
	}
	
	public static class SubValue {
		TypeInfo typeInfo;
		
		// this can be either a "live" or a "dead" value
		// live values are updated elsewhere and used mostly in other places. This just keeps a pointer to the value.
		// dead values are owned by this guy.
		Value value;
		
		/**  */
		float factor = 1;
		
		// for kryo
		public SubValue() {
			
		}
		
		public SubValue(TypeInfo type, float initValue, float factor) {
			this.typeInfo = type;
			this.value = new SimpleValue(initValue);
			this.factor = factor;
		}
		
		// only use this for simple values
		public void setValue(float value) {
			((SimpleValue) this.value).setValue(value);
		}
		
		public void setValue(Value value) {
			if (value == null) throw new java.lang.AssertionError();
			this.value = value;
		}
		
		public float getValue() {
			return value.getValue();
		}
	}
	
	private StrictArray<SubValue> values = new StrictArray<SubValue>();
	
	// for kryo
	public MultiValue() {
		
	}
	
	public MultiValue(String name) {
		super();
		this.multiName = name;
	}

	public String getStringSummary() {
		String summary = df.format(this.getValue()) + " " + this.multiName;
		if (values.size > 0) summary += " =";
		for (SubValue s : values) {
			if (s.value.getValue() == 0) continue;

			summary += "\n " + UnitType.formatStat(s.value.getValue()) + " " + s.typeInfo.name;
		}
		return summary;
	}
	
	public SubValue getValue(TypeInfo type) {
		SubValue goal = null;
		for (SubValue s : values) {
			if (s.typeInfo == type) {
				goal = s;
				break;
			}
		} 
		return goal;
	}
	
	public void addSubValue(TypeInfo type, float initValue, float factor) {
		addSubValue(new SubValue(type, initValue, factor));
	}
	
	public void addSubValue(TypeInfo type, float initValue) {
		addSubValue(new SubValue(type, initValue, 1));		
	}
//	
//	
	public void addSubValue(TypeInfo type) {
		addSubValue(new SubValue(type, 0, 1));
	}
	
	public void addSubValue(SubValue value) {
		this.values.add(value);
	}
	
	public void updateValue(TypeInfo type, float value) {
		SubValue goal = getValue(type);
		if (goal != null) goal.setValue(value);
		else throw new java.lang.AssertionError(type.name + " is not found for multivalue " + multiName);
	}
	
	public void updateValue(TypeInfo type, Value value) {
		SubValue goal = getValue(type);
		if (goal != null) goal.setValue(value);
		else throw new java.lang.AssertionError(type.name + " is not found for multivalue " + multiName);
	}
	
	public void incrementValue(TypeInfo type) {
		SubValue val = getValue(type);
		val.setValue(val.getValue() + 1);
	}
	
	/** Gets current value of multivalue.
	 *  Note that this recalculates the value on the fly.
	 * @return
	 */
	@Override
	public float getValue() {
		float total = 0;
		for (SubValue s : values) {
			total += s.factor * s.getValue();
		}
		return total;
	}
}
