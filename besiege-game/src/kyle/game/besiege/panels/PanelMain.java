///*******************************************************************************
// * Besiege
// * by Kyle Dhillon
// * Source Code available under a read-only license. Do not copy, modify, or distribute.
// ******************************************************************************/
//package kyle.game.besiege.panels;
//
//import kyle.game.besiege.Assets;
//import kyle.game.besiege.SidePanel;
//import kyle.game.besiege.army.ArmyPlayer;
//
//import com.badlogic.gdx.graphics.g2d.SpriteBatch;
//import com.badlogic.gdx.scenes.scene2d.ui.Label;
//import com.badlogic.gdx.scenes.scene2d.ui.Table;
//import com.badlogic.gdx.scenes.scene2d.ui.Label.*;
//
//
//// UNUSED
//public class PanelMain extends Panel {
//	private final float PAD = 5;
//	private final float NEG = -5;
//	private SidePanel panel;
//	private ArmyPlayer player;
//	private Table text;
//	private Label level;
//	private Label name;
//	private Label playerPartyPanel;
//	private Label money;
//	
//	public PanelMain(SidePanel panel) {
//		this.panel = panel;
//		
//		this.player = panel.getPlayer();               
//		this.addParentPanel(panel);
//		// Create text
//		text = new Table();
//		//text.debug();
//		text.top();
//		LabelStyle ls30 = new LabelStyle();
//		ls30.font = Assets.pixel30;
//		LabelStyle ls24 = new LabelStyle();
//		ls24.font = Assets.pixel24;
//		
//		name = new Label("", ls30);
//		name.setAlignment(0, 0);
//		level = new Label("", ls24);
//		playerPartyPanel = new Label("", ls24); // change later
//		money = new Label("", ls24);
//
//		Label levelC = new Label("Level:", ls24);
//		Label partyC = new Label("Party:",ls24);
//		Label moneyC = new Label("Money:",ls24);
//		
//		text.defaults().padTop(NEG);
//		text.add(name).colspan(2).padBottom(PAD).width(SidePanel.WIDTH-PAD*2).fillX().expandX();
//		text.row();
//		text.add(levelC).left();
//		text.add(level).right();
//		text.row();
//		text.add(partyC).left();
//		text.add(playerPartyPanel).right();
//		text.row();
//		text.add(moneyC).left();
//		text.add(money).right();
//		
//		this.addTopTable(text);
//		
//		this.setButton(1, "Party");
//		this.setButton(2, "Character");
//		this.setButton(3, "Actions");
//		//this.addButton(null);
//	}
//
//	@Override
//	public void act(float delta) {
//		level.setText(""+player.getLevel());
//		name.setText(player.getName());
//		playerPartyPanel.setText(player.getPartyInfo());
//		money.setText(""+player.getParty().wealth);
//		super.act(delta);
//	}
//	
//	@Override
//	public void button1() {
//		panel.setActive(panel.playerPartyPanel);
//	}
//	@Override
//	public void button2() {
//		panel.setActive(panel.character);
//	}
//	@Override
//	public void button3() {
//		BottomPanel.log("Actions");
//	}
//	@Override
//	public void button4() {
//		BottomPanel.log("nothing!");
//	}
//	
//	@Override
//	public void draw(SpriteBatch batch, float parentAlpha) {
//		super.draw(batch, parentAlpha);
//	}
//}
