///*******************************************************************************
// * Besiege
// * by Kyle Dhillon
// * Source Code available under a read-only license. Do not copy, modify, or distribute.
// ******************************************************************************/
//package kyle.game.besiege.panels;
//
//import com.badlogic.gdx.graphics.g2d.TextureRegion;
//import com.badlogic.gdx.scenes.scene2d.ui.Label;
//import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
//import com.badlogic.gdx.scenes.scene2d.ui.Table;
//
//import kyle.game.besiege.Assets;
//import kyle.game.besiege.Character;
//import kyle.game.besiege.army.ArmyPlayer;
//
//public class PanelCharacter extends Panel {
//	private final float PAD = 5;
//	private final float NEG = -5;
//	private SidePanel panel;
//	private ArmyPlayer player;
//	private Table text;
//	private Label name;
//	private Label maxTroops;
//	private Label fame;
//	private Label honor;
//	private Label faction;
//	private Label title;
//	
//	public PanelCharacter(SidePanel panel) {
//		this.panel = panel;
//		this.player = panel.getPlayer();
//		this.addParentPanel(panel);
//				
//		// Create text
//		text = new Table();
//		//text.debug();
//		text.defaults().padTop(NEG);
//		
//		LabelStyle ls30 = new LabelStyle();
//		ls30.font = Assets.pixel30;
//		LabelStyle ls24 = new LabelStyle();
//		ls24.font = Assets.pixel18;
//		
//		Label honorC = new Label("Honor:", ls24);
//		Label factionC = new Label("Faction:", ls24);
//		Label titleC = new Label("Title:", ls24); 
//		Label fameC = new Label("Fame:", ls24);
//		Label maxTroopsC = new Label("Party Limit:", ls24);
//
//		name = new Label("", ls30);
//		name.setAlignment(0,0);
//
//		honor = new Label("" ,ls24);
//		faction = new Label("", ls24);
//		title = new Label("", ls24);
//		fame = new Label("", ls24);
//		maxTroops = new Label("", ls24);
//		
//		text.add(name).colspan(2).padBottom(PAD).width(SidePanel.WIDTH-PAD*2).fillX().expandX();
//		text.row();
//		text.add(factionC).left();
//		text.add(faction).right();
//		text.row();
//		text.add(titleC).left();
//		text.add(title).right();
//		text.row();
//		text.add(honorC).left();
//		text.add(honor).right();
//		text.row();
//		text.add(fameC).left();
//		text.add(fame).right();
//		text.row();
//		text.add(maxTroopsC).left();
//		text.add(maxTroops).right();
//		
//		this.addTopTable(text);
//		
//		this.setButton(1, "Attributes");
//		this.setButton(2, "Holdings");
//		this.setButton(4, "Back");
//	}
//	
//	@Override
//	public void act(float delta) {
//		name.setText(player.getName());
//		honor.setText("" + getCharacter().getHonor());
//		faction.setText(player.getFactionName());
//		title.setText(getCharacter().getTitle());
//		maxTroops.setText(getCharacter().getMaxTroops() +"");
//		fame.setText(""+getCharacter().getFame());
//		super.act(delta);
//	}
//	
//	@Override
//	public void button1() {
//		panel.setActive(panel.attributes);
//	}
//	@Override
//	public void button2() {
//		BottomPanel.log("Holdings");
//	}
//	@Override
//	public void button3() {
//		
//	}
//	@Override
//	public void button4() {
//		panel.setDefault(true);
//	}
//	
//	public Character getCharacter() {
//		return this.panel.getMapScreen().getCharacter();
//	}
//	
//	@Override
//	public TextureRegion getCrest() {
//		return player.getFaction().crest;
//	}
//}
