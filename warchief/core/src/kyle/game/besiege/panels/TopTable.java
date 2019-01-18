package kyle.game.besiege.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import kyle.game.besiege.Assets;

import java.util.HashMap;

// Table at the top of the panel. Contains a title, columns, etc.
public class TopTable extends Table {
	private final float PAD = 10;
	private final float MINI_PAD = 5;
	private final float NEG = -5;
	private final float NEG_MINI = -2;

	// for health bar only
	private final int r = 3;
	private final String tablePatch = "grey-d9";
	private final String redPatch = "red9";
	private final String greenPatch = "green9";

	private Table health;
	private Table red;
	private Table green;

	private LabelStyle ls;
	private LabelStyle lsTitle;
	private LabelStyle lsSubtitle;
	
	private Label title;
	private float width;
	
	private HashMap<String, Label> labels = new HashMap<String, Label>();

	// Optional green/red bar to add to the table,
	private Table greenBar;

	// To determine proper padding.
	boolean wasLastSmallLabel = false;
	
	// in a 2 column table, this means that the next label added will be on the left. if false, will be on the right.
	private boolean nextIsLeft = true;

	// Subtitle count can be 0-3
	public TopTable() {
		lsTitle = new LabelStyle();
		lsTitle.font = Assets.pixel24;

		lsSubtitle = new LabelStyle();
		lsSubtitle.font = Assets.pixel18;
		 ls = new LabelStyle();
		ls.font = Assets.pixel16;

		// previously padded with neg
		this.defaults().padTop(NEG_MINI).left();
//		this.debug();

		this.width = SidePanel.WIDTH-PAD*2;
		this.add().colspan(2).width(width/2);
		this.add().colspan(2).width(width/2);
		this.row();
	}

	// Subtitle is a single, centered label (it is the value).
	public void addSubtitle(String key, String label, LabelStyle ls, InputListener listener) {
		Label subtitle = new Label(label, lsSubtitle);
		subtitle.setAlignment(0,0);
		subtitle.setWidth(SidePanel.WIDTH-PAD*2-MINI_PAD*2);
		subtitle.setWrap(true);
		// This isn't changing line spacing
		subtitle.getStyle().font.getData().ascent *= 0.2f;

		if (ls != null)
			subtitle.setStyle(ls);

		if (listener != null)
			subtitle.addListener(listener);
		labels.put(key, subtitle);

		this.add(subtitle).colspan(4).padBottom(0).fillX().expandX();
		this.row();

		wasLastSmallLabel = false;
	}

	public void addSubtitle(String key, String label, InputListener listener) {
		addSubtitle(key, label, null, listener);
	}
	public void addSubtitle(String key, String label) {
		addSubtitle(key, label, null);
	}
	// Big label has a fixed label and a value (e.g. "Troops: 20". It fits two rows across.
	public void addSmallLabel(String key, String label) {
		Label constantLabel = new Label(label, ls);
		Label value = new Label("", ls);
		float padTop = 0;
		if (wasLastSmallLabel) padTop = NEG;
		if (nextIsLeft) {
			this.add(constantLabel).padLeft(MINI_PAD).left().padTop(padTop);
		} else {
			this.add(constantLabel).padLeft(PAD).left().padTop(padTop);
			wasLastSmallLabel = true;
		}
		this.add(value).padTop(padTop);
		labels.put(key+"LABEL", constantLabel);
		labels.put(key, value);
		if (nextIsLeft) {
			nextIsLeft = false;
		} else {
			nextIsLeft = true;
			this.row();
		}
	}

	// Big label has a fixed label and a value (e.g. "Troops: 20". It fits one row across.
	public void addBigLabel(String key, String label) {
		Table table = new Table();
		Label constantLabel = new Label(label, ls);
		Label value = new Label("", ls);
		value.setWrap(false);
		labels.put(key, value);

		table.add(constantLabel).padLeft(MINI_PAD).expandX();
		float afterColon = 5;
		table.add(value).expandX().left().padLeft(afterColon);

		float padTop = 0;
		if (wasLastSmallLabel) padTop = NEG;

		this.add(table).colspan(4).expandX().padTop(padTop);
		this.row();

		wasLastSmallLabel = true;
	}

	public void addTable(Table table) {
		this.add(table).colspan(4).fillX().expandX().padTop(MINI_PAD)
				.padBottom(MINI_PAD);
		this.row();
	}

	public void addGreenBar() {
		health = new Table();
		red = new Table();
		red.setBackground(new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(redPatch), r,r,r,r)));
		green = new Table();
		green.setBackground(new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(greenPatch), r,r,r,r)));
		this.add(health).colspan(4).padTop(MINI_PAD).padBottom(MINI_PAD);
		this.row();

		wasLastSmallLabel = false;
	}

	public void updateGreenBar(double percentage) {
		health.clear();
		float totalWidth = SidePanel.WIDTH - PAD;

		health.add(green).width((float) (totalWidth*percentage));
		if (totalWidth*(1-percentage) > 0)
			health.add(red).width((float) (totalWidth*(1-percentage)));
	}
	
	public void update(String key, String newValue) {
		update(key, newValue, null);
	}

	public void update(String key, String newValue, InputListener listener) {
		if (!labels.containsKey(key)) {
			throw new java.lang.AssertionError();
		}
		Label value = labels.get(key);
		value.setText(newValue);

		if (listener != null) {
			value.clearListeners();
			value.addListener(listener);
		}
	}

	public void update(String key, String newValue, InputListener listener, Color color) {
		if (!labels.containsKey(key)) {
			throw new java.lang.AssertionError();
		}
		Label value = labels.get(key);
		value.setText(newValue);
		if (color != null) value.setColor(color);

		if (listener != null) {
			value.clearListeners();
			value.addListener(listener);
		}
	}

	public void updateLabel(String key, String newLabel) {
		key = key + "LABEL";
		if (!labels.containsKey(key)) {
			throw new java.lang.AssertionError();
		}
		Label value = labels.get(key);
		value.setText(newLabel);
	}
	
	public void updateTitle(String titleText, InputListener listener, Color color) {
		if (title == null) {
			title = new Label("", lsTitle);
			title.setAlignment(0,0);
			title.setWrap(true);
			title.setWidth(SidePanel.WIDTH-PAD*2-MINI_PAD*2);
			this.add(title).colspan(4).fillX().expandX().padBottom(NEG);
			this.row();
		}
		title.setText(titleText);
		if (color != null)
			title.setColor(color);
		if (listener != null) {
			title.addListener(listener);
		}
	}
	public void updateTitle(String titleText, InputListener listener) {
		updateTitle(titleText, listener, null);
	}
}
