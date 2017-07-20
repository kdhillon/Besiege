/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;

import kyle.game.besiege.Assets;
import kyle.game.besiege.Crest;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.party.Soldier;

public class Panel extends Group {
	private final float PAD = 5;
	private final float B_PAD = 2;
	private final float BUTTONS_HEIGHT = 5;
	private final float BUTTONHEIGHT = 90;
	private final float OFFSET = 1;
	private final String upTexture = "grey-lm9";
	private final String downTexture = "grey-med9";
	private final String barTexture = "grey-d9";
	private final String knobTexture = "grey-med9";
	private final String disabledTexture = "grey-mmd9";
	private final int r = 3; // 9patch offset

	private final String PAUSED = "Paused";
	//	private final String SAVING = "Saving...";

	protected SidePanel sidePanel;

	private Kingdom kingdom;
	private int day;
	private int time;

	private ScrollPane topPane;
	private ScrollPane.ScrollPaneStyle spStyle;

	private Label timeLabel;
	private Label pausedLabel;
	private Table buttons;
	private Table topTable;
	private Array<Button> buttonArray;

	private Button b1;
	private Button b2;
	private Button b3;
	private Button b4;

	private LabelStyle ls17;
	private LabelStyle ls12;
	private ButtonStyle bs;

	boolean saving = false;

	public Panel() {
		this.setPosition(0, 0);
		buttons = new Table();
		//buttons.debug();
		buttons.bottom();
		buttons.defaults().expandX().fillX().pad(B_PAD);

		ls17 = new LabelStyle();
		ls17.font = Assets.pixel17;
		ls12 = new LabelStyle();
		ls12.font = Assets.pixel12;

		bs = new ButtonStyle();
		bs.up = new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(upTexture), r,r,r,r));
		bs.down = new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(downTexture), r,r,r,r));
		bs.disabled = new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(disabledTexture), r,r,r,r));
		bs.pressedOffsetX = OFFSET;
		bs.pressedOffsetY = -OFFSET;

		timeLabel = new Label("Day " + day + " " + time + ":00", ls17);
		timeLabel.setAlignment(Align.left);
		pausedLabel = new Label("", ls17);
		pausedLabel.setAlignment(Align.center);

		buttons.setX(SidePanel.WIDTH / 2);
		buttons.setY(BUTTONS_HEIGHT);
		buttons.add().colspan(2).width((SidePanel.WIDTH - PAD * 2));
		b1 = new Button(bs);
		b2 = new Button(bs);
		b3 = new Button(bs);
		b4 = new Button(bs);
		b1.setVisible(false);
		b2.setVisible(false);
		b3.setVisible(false);
		b4.setVisible(false);
		buttons.row();
		buttons.add(b1);
		buttons.add(b2);
		buttons.row();
		buttons.add(b3);
		buttons.add(b4);
		buttons.row();
		buttons.add(timeLabel).padTop(PAD).padLeft(PAD).expand(false,false).fill(false).width((SidePanel.WIDTH - PAD * 2)/2-PAD);
		buttons.add(pausedLabel).padTop(PAD).padRight(PAD).expand(false,false).fill(false).width((SidePanel.WIDTH - PAD * 2)/2-PAD);
		//buttons.debug();

		// TODO remove
		this.addActor(buttons);

		//buttonArray = new Array<Button>();
	}

	public void addParentPanel(SidePanel panel) {
		this.sidePanel = panel;
		this.kingdom = panel.getMapScreen().getKingdom();
	}

	public void setButton(int bc, String name) {
		Button b;
		if (bc == 1) b = b1;
		else if (bc == 2) b = b2; 
		else if (bc == 3) b = b3;
		else b = b4;

		if (name == null) {
			b.clearChildren();
			b.setVisible(false);
		} 
		else {
			Label label = new Label(name, ls17);
			b.clearChildren();
			b.add(label);
			b.setVisible(true);
			// can simplify if make button1() button2() etc into one method with int argument
			if (bc == 1) {
				b1.clearListeners();
				b1.addListener(new InputListener() {
					public boolean touchDown(InputEvent event, float x,
							float y, int pointer, int button) {
						System.out.println("touchdown b1");
						return true;
					}
					public void touchUp(InputEvent event, float x, float y,
							int pointer, int button) {
						System.out.println("clicking b1");
						button1();
					}
				});
			} 
			else if (bc == 2) {
				b2.clearListeners();
				b2.addListener(new InputListener() {
					public boolean touchDown(InputEvent event, float x,
							float y, int pointer, int button) {
						return true;
					}

					public void touchUp(InputEvent event, float x, float y,
							int pointer, int button) {
						button2();
					}
				});
			} else if (bc == 3) {
				b3.clearListeners();
				b3.addListener(new InputListener() {
					public boolean touchDown(InputEvent event, float x,
							float y, int pointer, int button) {
						return true;
					}

					public void touchUp(InputEvent event, float x, float y,
							int pointer, int button) {
						button3();
					}
				});
			} else if (bc == 4) {
				b4.clearListeners();
				b4.addListener(new InputListener() {
					public boolean touchDown(InputEvent event, float x,
							float y, int pointer, int button) {
						return true;
					}

					public void touchUp(InputEvent event, float x, float y,
							int pointer, int button) {
						button4();
					}
				});
			} else
				System.out.println("you done f***ed up");
		}
	}

	@Override
	public void act(float delta) {	
		if (topPane.getHeight() != sidePanel.getHeight() - SidePanel.WIDTH - BUTTONHEIGHT - PAD*2) {
			resize();
		}

		if (kingdom != null) day = kingdom.getDay();
		if (kingdom != null) time = kingdom.getTime();
		timeLabel.setText("Day: " + day + " " + time + ":00");

		super.act(delta);

		if (kingdom != null && kingdom.isPaused())
			pausedLabel.setText(PAUSED + " (" + Gdx.graphics.getFramesPerSecond() + ")");
		else
			pausedLabel.setText(" (" + Gdx.graphics.getFramesPerSecond() + ")");
	}

	public void resize() {
		this.removeActor(topPane);
		topPane = new ScrollPane(topTable, spStyle);
		topPane.setScrollingDisabled(true, false);
		topPane.setFadeScrollBars(false);
		topPane.setBounds(PAD, PAD + BUTTONHEIGHT, SidePanel.WIDTH - PAD*2, sidePanel.getHeight() - SidePanel.WIDTH - BUTTONHEIGHT - PAD*2);

		this.addActor(topPane);
	}

	public void addTopTable(Table topTable) {
		this.topTable = topTable;
		//topTable.debug();
		topTable.top();
		spStyle = new ScrollPane.ScrollPaneStyle();

		spStyle.vScroll = new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(barTexture), r,r,r,r));
		spStyle.vScrollKnob = new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(knobTexture), r,r,r,r));

		topPane = new ScrollPane(topTable, spStyle);
		topPane.setY(sidePanel.getHeight() - SidePanel.WIDTH);
		topPane.setX(0);
		topPane.setWidth(SidePanel.WIDTH-PAD*2);
		topPane.setHeight(sidePanel.getHeight()-SidePanel.WIDTH);

		topPane.setScrollingDisabled(true, false);

		this.addActor(topPane);
	}

	// All overridden
	public void button1() {
	}

	public void button2() {
	}

	public void button3() {
	}
	// Back button
	public void button4() {
		sidePanel.returnToPrevious();
	}
	public Button getButton(int button) {
		if (button == 1)
			return b1;
		else if (button == 2)
			return b2;
		else if (button == 3)
			return b3;
		else if (button == 4)
			return b4;
		else return b1;
	}
	public Crest getCrest() {
		return null;
	}
	public Soldier getSoldierInsteadOfCrest() {
		return null;
	}
	public TextureRegion getSecondCrest() {
		return null;
	}
	public static String format(final String input, final int cutoff) {
		char[] split = input.toCharArray();//in your case "%d" as delimeter
		final StringBuffer buffer = new StringBuffer();
		int i;
		boolean hasPeriod = false;
		if (cutoff <= 0) return input;
		for (i = 0; i < split.length; i++) {
			if (split[i] == '.') {
				hasPeriod = true;
				break;
			}
			buffer.append(split[i]);
		}
		if (hasPeriod) {
			buffer.append('.');
			i++;
			int j;
			for (j = i; j < split.length && j < i+cutoff; j++) {
				buffer.append(split[j]);
			}
			if (j == split.length) {
				for (; j < i+cutoff; j++)
					buffer.append('0');
			}
			return buffer.toString();
		}
		else {
			buffer.append('.');
			for (int j = 0; j < cutoff; j++) {
				buffer.append('0');
			}
			return buffer.toString();
		}
	}

	public void beginSaving() {
		// TODO set back button to unclickable
		saving = true;
	}
	public void endSaving() {
		saving = false;
	}
}
