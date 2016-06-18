package kyle.game.besiege;

public class SimpleValue implements Value {
	float value;
	
	public SimpleValue() {
	}
	
	public SimpleValue(float val) {
		this.value = val;
	}
	
	public float getValue() {
		return value;
	}
	
	public void setValue(float val) {
		this.value = val;
	}
}
