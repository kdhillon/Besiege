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
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import kyle.game.besiege.Assets;
import kyle.game.besiege.Crest;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.party.Soldier;

import java.util.Collection;

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

	// Either one should be used?
	public PanelHolder panelHolder;

	private Kingdom kingdom;
	private int day;
	private int time;

//	private ScrollPane topPane2;
	private ScrollPane.ScrollPaneStyle spStyle;

	private Label timeLabel;
	private Label pausedLabel;
	private Table buttons;

	private ScrollPane masterPane;
	private Table master; // this is the table inside it's own scrollpane. topTables and soldiertables are children of this.
	private float masterHeight;

	private float topTableY;
	private float HALF_Y;
	private float HALF_HEIGHT;

	private Array<Button> buttonArray;

	private MyButton b1;
	private MyButton b2;
	private MyButton b3;
	private MyButton b4;

	private class MyButton extends Button {
		public Label label;
		public MyButton(ButtonStyle ls) {
			super(ls);
		}
	}

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
		b1 = new MyButton(bs);
		b2 = new MyButton(bs);
		b3 = new MyButton(bs);
		b4 = new MyButton(bs);
		initializeButton(1);
		initializeButton(2);
		initializeButton(3);
		initializeButton(4);
		buttons.row();
		buttons.add(b1);
		buttons.add(b2);
		buttons.row();
		buttons.add(b3);
		buttons.add(b4);
		buttons.row();
		if (shouldAddTimeAndPaused()) {
			buttons.add(timeLabel).padTop(PAD).padLeft(PAD).expand(false, false).fill(false).width((SidePanel.WIDTH - PAD * 2) / 2 - PAD);

			buttons.add(pausedLabel).padTop(PAD).padRight(PAD).expand(false, false).fill(false).width((SidePanel.WIDTH - PAD * 2) / 2 - PAD);
		}
		//buttons.debug();

		// TODO remove
		this.addActor(buttons);

		//buttonArray = new Array<Button>();
	}

	// Figure out if we're mousing over any other soldier table and highlight it.
	protected void notifyDragStart(Soldier soldier) {
		// Handled in subclasses
	}

	// Figure out if we're mousing over any other soldier table and highlight it.
	protected void notifyDragRelease(Soldier soldier) {
		// Handled in subclasses
	}

	protected boolean shouldAddTimeAndPaused() {
		return true;
	}

	public void addParentPanel(PanelHolder panelHolder) {
		this.panelHolder = panelHolder;
		this.kingdom = panelHolder.getMapScreen().getKingdom();

		float height = getFullHeight();

		masterHeight = height - BUTTONHEIGHT - PAD*2;

		topTableY = PAD + BUTTONHEIGHT;

		HALF_HEIGHT = masterHeight / 2;
		HALF_Y = PAD + height - HALF_HEIGHT;

		initializeMasterTable();
	}

	public void setButton(int bc, String name) {
		MyButton b;
		if (bc == 1) b = b1;
		else if (bc == 2) b = b2;
		else if (bc == 3) b = b3;
		else b = b4;

		if (name == null) {
//			b.clearChildren();
			b.setVisible(false);
//			System.out.println("setting button " + bc + " to null");
		}
		else {
//			System.out.println("setting text of button " + bc + " to " + name);
			b.setVisible(true);
			b.label.setText(name);
		}
	}

	public void initializeButton(int bc) {
		MyButton b;
		if (bc == 1) b = b1;
		else if (bc == 2) b = b2; 
		else if (bc == 3) b = b3;
		else b = b4;

		Label label = new Label("", ls17);
		b.clearChildren();
		b.add(label);
		b.label = label;
		b.setVisible(false);
//			System.out.println("setting button: " + name);
		// can simplify if make button1() button2() etc into one method with
		// int argument
		if (bc == 1) {
			b1.clearListeners();
			// TODO don't have universal buttons, this is kinda stupid.
			b1.addListener(new InputListener() {
				public boolean touchDown(InputEvent event, float x,
										 float y, int pointer, int button) {
					System.out.println("touchdown b1");
					return true;
				}

				public void touchUp(InputEvent event, float x, float y,
									int pointer, int button) {
					System.out.println("clicking b1");
					panelHolder.press(1);
				}
			});
		} else if (bc == 2) {
			b2.clearListeners();
			b2.addListener(new InputListener() {
				public boolean touchDown(InputEvent event, float x,
										 float y, int pointer, int button) {
					return true;
				}

				public void touchUp(InputEvent event, float x, float y,
									int pointer, int button) {
					panelHolder.press(2);
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
					panelHolder.press(3);
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
					panelHolder.press(4);
				}
			});
		} else
			System.out.println("you done f***ed up");

	}

	@Override
	public void act(float delta) {

		masterHeight = getFullHeight() - BUTTONHEIGHT - PAD*2;
		if (masterPane.getHeight() != masterHeight) {
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
		System.out.println("resizing panel");
		// TODO make this adjust soldiertables?
		this.removeActor(masterPane);
		masterPane = new ScrollPane(master, spStyle);
		masterPane.setScrollingDisabled(true, true);
		masterPane.setFadeScrollBars(false);
		masterPane.setBounds(PAD, topTableY, SidePanel.WIDTH - PAD*2, masterHeight);

		this.addActor(masterPane);
	}

	public void addTopTable2(Table topTable2) {
		addTopTable(topTable2);
	}


	boolean leaveSpaceForMinimap() {
		return true;
	}

	float getFullHeight() {
		if (leaveSpaceForMinimap()) return panelHolder.getHeight() - SidePanel.WIDTH;
		return panelHolder.getHeight();
	}

	public void initializeMasterTable() {
		this.master = new Table();
		master.top();
		spStyle = new ScrollPane.ScrollPaneStyle();

		spStyle.vScroll = new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(barTexture), r,r,r,r));
		spStyle.vScrollKnob = new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(knobTexture), r,r,r,r));

		masterPane = new ScrollPane(master, spStyle);
		float height = getFullHeight();
		masterPane.setY(height);
		masterPane.setX(0);
		masterPane.setWidth(SidePanel.WIDTH-PAD*2);
		masterPane.setHeight(height);

		masterPane.setScrollingDisabled(true, true);

		this.addActor(masterPane);
	}

	public void addTopTable(Table topTable) {
		// Just add toptable to mastertable.
		master.add(topTable).top().padBottom(PAD/2);
		master.row();
	}

	public void updateTopTable(Table prevTable, Table newTable) {
		Cell cell = master.getCell(prevTable);
		cell.setActor(newTable);

//			Cell cell = topTable.getCell(soldierTable);
////			cell.height(panel.getHeight() - DESC_HEIGHT).setWidget(null);
//			soldierTable = new SoldierTable(this, p);
////			soldierTable.setHeight(panel.getHeight() - DESC_HEIGHT);
//			cell.setWidget(soldierTable);
//			garrisonedTables.put(p, soldierTable);
//			soldierTable.update();
		// Just add toptable to mastertable.
//		master.add(topTable).top().padBottom(PAD/2);
//		master.row();
	}


	public void addSoldierTable(SoldierTable table) {
		master.add(table).top().expandY().padTop(0).fillY();
		master.row();
	}

	// this doesn't seem to work?
	public void clearSoldierTables(Collection<SoldierTable> tables) {
		for (SoldierTable s : tables) {
			master.removeActor(s);
		}
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
		panelHolder.returnToPrevious(true);
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

	// to be overridden
	public PanelUnit getPanelUnit() {
	    return null;
    }

	public void beginSaving() {
		// TODO set back button to unclickable
		saving = true;
	}
	public void endSaving() {
		saving = false;
	}
}
