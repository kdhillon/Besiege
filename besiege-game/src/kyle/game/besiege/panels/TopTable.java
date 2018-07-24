package kyle.game.besiege.panels;

import java.util.HashMap;

import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

import kyle.game.besiege.Assets;

// Table at the top of the panel. Contains a title, columns, etc.
public class TopTable extends Table {
	private final float PAD = 10;
	private final float MINI_PAD = 5;
	private final float NEG = -5;
	
	private LabelStyle ls;
	
	private Label title;
	private Label subtitle;
	private Label subtitle2; // this is actually above subtitle, (closer to the title)
	private Label subtitle3;
	private float width;
	
	private HashMap<String, Label> labels = new HashMap<String, Label>();
	
	// in a 2 column table, this means that the next label added will be on the left. if false, will be on the right.
	private boolean nextIsLeft = true;

	// Subtitle count can be 0-3
	public TopTable(int subtitleCount) {
		LabelStyle lsBig = new LabelStyle();
		lsBig.font = Assets.pixel24;

		LabelStyle lsFaction = new LabelStyle();
		lsFaction.font = Assets.pixel18;
		 ls = new LabelStyle();
		ls.font = Assets.pixel16;
		
		title = new Label("", lsBig);
		title.setAlignment(0,0);
		title.setWrap(true);
		title.setWidth(SidePanel.WIDTH-PAD*2-MINI_PAD*2);
		subtitle = new Label("", lsFaction);
		subtitle.setAlignment(0,0);
		subtitle2 = new Label("", ls);
		subtitle2.setAlignment(0,0);
		subtitle3 = new Label("",ls);
		subtitle3.setWrap(true);
		subtitle3.setAlignment(Align.center);
		
		this.defaults().padTop(NEG).left();
//		this.debug();
		
		this.add(title).colspan(4).fillX().expandX().padBottom(0);
		this.row();
		this.width = SidePanel.WIDTH-PAD*2;
		this.add().colspan(2).width(width/2);
		this.add().colspan(2).width(width/2);
		this.row();
		if (subtitleCount > 1) {
			this.add(subtitle2).colspan(4).padBottom(MINI_PAD).fillX().expandX();
			this.row();
		}
		if (subtitleCount > 0) {
			this.add(subtitle).colspan(4).padBottom(MINI_PAD).fillX().expandX();
			this.row();
		} 
		if (subtitleCount > 2) {
			this.add(subtitle3).colspan(4).padBottom(MINI_PAD).fillX().expandX();					
			this.row();
		}
	}
	
	public void addSmallLabel(String key, String label) {
		Label constantLabel = new Label(label, ls);
		Label value = new Label("", ls);
		if (nextIsLeft) {
			this.add(constantLabel).padLeft(MINI_PAD).left();
		} else {
			this.add(constantLabel).padLeft(PAD).left();
		}
		this.add(value);
		labels.put(key, value);
		if (nextIsLeft) {
			nextIsLeft = false;
		} else {
			nextIsLeft = true;
			this.row();
		}
	}
	
	public void addBigLabel(String key, String label) {
		Label constantLabel = new Label(label, ls);
		Label value = new Label("", ls);
		value.setWrap(false);
		labels.put(key, value);

		this.add(constantLabel).colspan(2).padLeft(MINI_PAD);
		this.add(value).colspan(2).left().width(width/4);
		this.row();
	}
	
	public void update(String key, String newValue) {
		if (!labels.containsKey(key)) {
			throw new java.lang.AssertionError();
		}
		Label value = labels.get(key);
		value.setText(newValue);
	}
	
	public void updateTitle(String titleText, InputListener listener) {
		title.setText(titleText);
		if (listener != null) {
			title.addListener(listener);
		}
	}

	public void updateSubtitle(String subtitleText, InputListener listener) {
		subtitle.setText(subtitleText);
		if (listener != null) {
			subtitle.addListener(listener);
		}
	}
	
	public void updateSubtitle2(String subtitleText, InputListener listener) {
		subtitle2.setText(subtitleText);
		if (listener != null) {
			subtitle2.addListener(listener);
		}
	}
	
	public void updateSubtitle3(String subtitleText, InputListener listener) {
		subtitle3.setText(subtitleText);
		if (listener != null) {
			subtitle3.addListener(listener);
		}
	}
}
