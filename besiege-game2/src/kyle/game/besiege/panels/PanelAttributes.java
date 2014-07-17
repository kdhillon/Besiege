/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import kyle.game.besiege.Assets;
import kyle.game.besiege.Character;
import kyle.game.besiege.SidePanel;
import kyle.game.besiege.army.ArmyPlayer;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;


public class PanelAttributes extends Panel {
	private final int PAD = 5;
	private final int NEG = -2;
	private final int NEG_TOP = -5;
	private final int OFFSET = 1;
	private final String upTexture = "grey-lm9";
	private final String downTexture = "grey-med9";
	private final int r = 3;
	private final int VALUE_WIDTH = 10; // all for fine tuning aesthetics of buttons and table
	private final int BUTTON_PAD = 6;
	private final int BUTTON_PAD_SMALL = 2;
	private final int BUTTON_HEIGHT = 16;
	
	private SidePanel panel;
	private Character character;
	private ArmyPlayer player;
	private Table text;
	private Array<Label> attributeNameLabels;
	private Array<Label> attributeValueLabels;
	private Array<Button> attributePlusButtons;
	private Label description;
	private Label available;
	private Label.LabelStyle ls;
	private Label.LabelStyle lsBig;
	private Label.LabelStyle lsSmall;
	private Button.ButtonStyle bs;
	private boolean pointsAvailable;

	public PanelAttributes(SidePanel panel) {
		this.panel = panel;
		this.player = panel.getPlayer();
		this.addParentPanel(panel);

		this.character = player.getCharacter();
		
		this.attributeNameLabels = new Array<Label>();
		this.attributeValueLabels = new Array<Label>();
		this.attributePlusButtons = new Array<Button>();
		
		// Create text
		text = new Table();
		text.padRight(PAD);
		text.padLeft(PAD*2);
		//text.debug();
		text.defaults().padTop(NEG);

		lsBig = new LabelStyle();
		lsBig.font = Assets.pixel30;
		
		lsSmall = new LabelStyle();
		lsSmall.font = Assets.pixel16;
		
		ls = new LabelStyle();
		ls.font = Assets.pixel20;
		
		bs = new ButtonStyle();
		bs.up = new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(upTexture), r,r,r,r));
		bs.down = new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(downTexture), r,r,r,r));
		bs.pressedOffsetX = OFFSET;
		bs.pressedOffsetY = -OFFSET;
	
		Label availableC = new Label("Remaining:", ls);
		available = new Label("", ls);
		
		description = new Label("",lsSmall);
		description.setWrap(true);
		description.setWidth(SidePanel.WIDTH-PAD*2);

		Label charC = new Label("Attributes", lsBig);
		charC.setAlignment(0,0);

		text.add(charC).colspan(3).padBottom(PAD).width(SidePanel.WIDTH-PAD*2).fillX().expandX().padTop(NEG_TOP);
		text.row();
		text.add(availableC).left().padBottom(PAD);
		text.add(available).colspan(2).left().padBottom(PAD).padRight(PAD*2);
		
		addAttributes();
		
		pointsAvailable = false;
		
		this.addTopTable(text);

		this.setButton(4, "Back");
	}

	@Override
	public void act(float delta) {
		for (int i = 0; i < attributeValueLabels.size; i++) {
			Label value = attributeValueLabels.get(i);
			value.setText(""+character.attributeValues.get(i));
		}
		if (character.availablePoints > 0 && pointsAvailable == false) {
			for (Button plusB : attributePlusButtons) {
				plusB.setVisible(true);
			}
			pointsAvailable = true;
		}
		if (character.availablePoints <= 0 && pointsAvailable == true) {
			for (Button plusB : attributePlusButtons) {
				plusB.setVisible(false);
			}
			pointsAvailable = false;
		}
		
		available.setText(""+character.availablePoints);
		
		super.act(delta);
	}
	
	public void addAttributes() {
		for (String name : character.attributeNames) {
			Label attribute = new Label(name, ls);
			attribute.addListener(new ClickListener() {
				public void enter(InputEvent event, float x,
						float y, int pointer, Actor fromActor) {
						description.setText(((Label) event.getTarget()).getText() + ": \n" + character.getDescription(((Label) event.getTarget()).getText()));
				}
				public void exit(InputEvent event, float x, float y,
						int pointer, Actor fromActor) {
					description.setText("");
				}
			});
			Label value = new Label(character.getAttributeLevel(name) + "", ls);
			Label plus = new Label("+",ls);
			plus.setTouchable(Touchable.disabled);
			Button plusB = new Button(plus, bs);
			plusB.addListener(new InputListener() {
				public boolean touchDown(InputEvent event, float x,
						float y, int pointer, int button) {
					return true;
				}
				public void touchUp(InputEvent event, float x, float y,
						int pointer, int button) {
					buttonPressed((Button) event.getTarget());
				}
			});
			attributeValueLabels.add(value);
			attributeNameLabels.add(attribute);
			attributePlusButtons.add(plusB);
			text.row();
			text.add(attribute).width(SidePanel.WIDTH*3/5f).left();
			text.add(value).width(VALUE_WIDTH).left();
			plusB.padBottom(BUTTON_PAD);
			text.add(plusB).left().height(BUTTON_HEIGHT).padBottom(BUTTON_PAD_SMALL).padTop(0).padLeft(BUTTON_PAD_SMALL);
		}
		text.row();
		text.add(description).colspan(3).width(SidePanel.WIDTH-2*PAD).padTop(PAD*2).center();
	}
	
	public void buttonPressed(Button plusB) {
		int index = attributePlusButtons.indexOf(plusB, true);
		character.increaseAttribute(index);
	}

	@Override
	public void button4() {
		panel.setActive(panel.character);
	}
}

