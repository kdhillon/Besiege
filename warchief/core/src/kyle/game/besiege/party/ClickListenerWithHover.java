package kyle.game.besiege.party;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class ClickListenerWithHover extends ClickListener {
    Label label;

    public ClickListenerWithHover(Label label) {
        this.label = label;
    }

    @Override
    public boolean touchDown(InputEvent event, float x,
                             float y, int pointer, int button) { return true; }

    @Override
    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        if (label.getActions().size == 0) {
            label.setColor(Color.LIGHT_GRAY);
        }
    }

    @Override
    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        if (label.getActions().size == 0) {
            label.setColor(Color.WHITE);
        }
    }
}
