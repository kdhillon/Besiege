/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import kyle.game.besiege.army.Army;
import kyle.game.besiege.Assets;
import kyle.game.besiege.SidePanel;
import kyle.game.besiege.battle.Unit;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

public class PanelUnit extends Panel { 
	private final float PAD = 10;
	private final float MINI_PAD = 5;
	private final float NEG = -5;
	private final float DESC_HEIGHT = 530;
	private final int r = 3;
	private final String tablePatch = "grey-d9";
	private final String redPatch = "red9";
	private final String greenPatch = "green9";
	private SidePanel panel;
	private Unit unit;
	private Soldier soldier;
	private int max_hp;
	private Party party;
	
	private Table text;
	private Label title;
	private Label armyName;
	private Label partyName;
	private Label type;
	
	private Table health;
	private Table red;
	private Table green;
	
//	private Table stats;
//	private Label nameS;
	private Label levelS;
	private Label hpS;
	private Label expS;
	private Label atkS;
	private Label defS;
	private Label spdS;
	private Label weaponS;
	private Label equipmentS;
	private Label actionS;
	
//	private Table soldierTable;
//	private ScrollPane soldierPane;
	
	private LabelStyle ls;
	private LabelStyle lsMed;
	private LabelStyle lsG;

	public PanelUnit(SidePanel panel, Unit unit) {
		this.panel = panel;
		this.unit = unit;
		this.soldier = unit.soldier;
		this.party = unit.party;
		this.addParentPanel(panel);
		
		this.max_hp = unit.calcHP();
		
		LabelStyle lsBig = new LabelStyle();
		lsBig.font = Assets.pixel22;
		lsMed = new LabelStyle();
		lsMed.font = Assets.pixel18;
		ls = new LabelStyle();
		ls.font = Assets.pixel16;
		lsG = new LabelStyle();
		lsG.font = Assets.pixel16;
		lsG.fontColor = Color.GRAY;
		
		Label levelSC = 	new Label("Level:", ls);
		Label hpSC = 		new Label("HP:",ls);
		Label expSC =		new Label("Height:",ls);
		Label atkSC =		new Label("Atk:", ls);
		Label defSC = 		new Label("Def:", ls);
		Label spdSC = 		new Label("Spd:", ls); 
		Label weaponSC = 	new Label("Weapon: ", ls);
		Label equipmentSC = new Label("Armor: ", ls);
		
		title = new Label(soldier.name, lsBig);
		title.setAlignment(0,0);
//		title.setWrap(true); // wrapping messes up click listeners... WTF?
		title.setWidth(SidePanel.WIDTH-PAD*2-MINI_PAD*2);
//		title.addListener(new InputListener() {
//			public boolean touchDown(InputEvent event, float x,
//					float y, int pointer, int button) {
//				return true;
//			}
//			public void touchUp(InputEvent event, float x, float y,
//					int pointer, int button) {
//				centerCamera();
//			}
//		});
		
		armyName = new Label("", ls);
		if (party.army != null)
			armyName.setText(party.army.getName());
		armyName.setAlignment(0,0);
		
		levelS = new Label("" + soldier.level, ls);
		hpS = new Label("" + unit.hp, ls);
		expS = new Label("" + soldier.next, ls);
		atkS = new Label("" + soldier.getAtk(), ls);
		defS = new Label("" + soldier.getDef(), ls);
		spdS = new Label("" + soldier.getSpd(), ls);
		weaponS = new Label("" + soldier.weapon.name, ls);
		if (unit.isRanged()) weaponS.setText(soldier.rangedWeapon.name + "(" + unit.quiver + ")");
		weaponS.setAlignment(0,0);
		
		equipmentS = new Label("", ls);
		actionS = new Label("" + unit.getStatus(), ls);
		actionS.setAlignment(0,0);
		
		health = new Table();
		red = new Table();
		red.setBackground(new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(redPatch), r,r,r,r)));
		green = new Table();
		green.setBackground(new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(greenPatch), r,r,r,r)));
		
		// Create text
		text = new Table();
		//text.debug();
		text.defaults().padTop(NEG).left();
		
//		title.addListener(new InputListener() {
//			public boolean touchDown(InputEvent event, float x,
//					float y, int pointer, int button) {
//				return true;
//			}
//			public void touchUp(InputEvent event, float x, float y,
//					int pointer, int button) {
//				centerCamera();
//			}
//		});
		text.add(title).colspan(4).fillX().expandX().padBottom(0);
		text.row();
		text.add().colspan(2).width((SidePanel.WIDTH-PAD*2)/2);
		text.add().colspan(2).width((SidePanel.WIDTH-PAD*2)/2);
		text.row();
		text.add(armyName).colspan(4).fillX().expandX();
		text.row();
		text.add(health).colspan(4).padTop(MINI_PAD).padBottom(MINI_PAD);
		text.row();
		text.add(actionS).colspan(4).fillX().expandX();
		text.row();
		text.add(weaponS).colspan(4).fillX().expandX();
		text.row();
		text.add(atkSC).padLeft(MINI_PAD);
		text.add(atkS);
		text.add(levelSC).padLeft(PAD);
		text.add(levelS);
		text.row();
		text.add(defSC).padLeft(MINI_PAD);
		text.add(defS);
		text.add(hpSC).padLeft(PAD);
		text.add(hpS);
		text.row();
		text.add(spdSC).padLeft(MINI_PAD);
		text.add(spdS);
		text.add(expSC).padLeft(PAD);
		text.add(expS);

		text.row();

		text.padLeft(MINI_PAD);
		this.addTopTable(text);
	}
	
	@Override
	public void act(float delta) {	
		levelS.setText("" + soldier.level);
		hpS.setText("" + unit.hp);
//		expS.setText("" + soldier.exp);
		expS.setText("" + (int) (unit.getFloorHeight()*10) / 10.0);
		atkS.setText("" + unit.atk);
		defS.setText("" + unit.def);
		spdS.setText("" + (int) unit.spd);
		if (unit.isRanged() && unit.attacking == null && unit.quiver > 0) weaponS.setText(soldier.rangedWeapon.name + " (" + unit.quiver + ")");
		else weaponS.setText(soldier.weapon.name);
		
		String mounted = "";
		if (unit.isMounted()) mounted = " (Mounted)";
		actionS.setText(unit.getStatus() + mounted);
		
		health.clear();
		float totalWidth = SidePanel.WIDTH - PAD;

		health.add(green).width((float) (totalWidth*(unit.hp*1.0/max_hp)));
		if (totalWidth*(1-(unit.hp*1.0/max_hp)) > 0)
			health.add(red).width((float) (totalWidth*(1-(unit.hp*1.0/max_hp))));
		
		super.act(delta);
	}
	
//	public void setStats(Soldier s) {
//		stats.setVisible(true);
//		nameS.setText(s.name + "");
//		levelS.setText(s.level + "");
//		expS.setText(s.exp + "");
//		nextS.setText(s.next + "");
//		if (s.bonusAtk >= 0)
//			atkS.setText(s.getAtk() + " (" + s.baseAtk + "+" + s.bonusAtk + ")");
//		else 
//			atkS.setText(s.getAtk() + " (" + s.baseAtk + s.bonusAtk + ")");
//		if (s.bonusDef >= 0)
//			defS.setText(s.getDef() + " (" + s.baseDef + "+" + s.bonusDef + ")");
//		else 
//			defS.setText(s.getDef() + " (" + s.baseDef + s.bonusDef + ")");
//		if (s.bonusSpd >= 0)
//			spdS.setText(s.getSpd() + " (" + s.baseSpd + "+" + s.bonusSpd + ")");
//		else 
//			spdS.setText(s.getSpd() + " (" + s.baseSpd + s.bonusSpd + ")");
//		weaponS.setText(s.weapon.name);
//	}
	
//	public void clearStats() {
//		stats.setVisible(false);
//	}
//	
//	public void setActiveFaction() {
//		panel.setActiveFaction(location.getFaction());
//	}
//	public void centerCamera() {
//		OrthographicCamera camera = panel.getKingdom().getMapScreen().getCamera();
//		camera.translate(new Vector2(location.getCenterX()-camera.position.x, location.getCenterY()-camera.position.y));
//	}
	
	@Override
	public void button1() {
		
	}
	@Override
	public void button2() {
		
	}
	@Override
	public void button3() {
		
	}
	@Override
	public void button4() {
		
	}
	
	@Override
	public TextureRegion getCrest() {
		if (party.army != null)
			return party.army.getFaction().crest;
		return null;
	}
}
