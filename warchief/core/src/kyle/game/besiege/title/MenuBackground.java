package kyle.game.besiege.title;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;
import kyle.game.besiege.BesiegeMain;
import kyle.game.besiege.Random;
import kyle.game.besiege.battle.BPoint;

import static kyle.game.besiege.battle.BattleMap.RAINDROP_COLOR;
import static kyle.game.besiege.battle.BattleMap.SNOW_COLOR;

/**
 * Represents the dynamic background drawn behind menu options (main menu, battle selection, etc).
 */
public class MenuBackground extends Group {
    private static TextureRegion region = new TextureRegion(new Texture("whitepixel.png"));

    private static final Color TINT = new Color(0f, 0f, 0f, 0.3f);

    private static final float WIND_SPEED_INTENSITY = 0.2f;

    private static final float DARKNESS = 0.5f;

    enum BackgroundType{Forest, Tundra, Beach, Jungle, Swamp}

    BackgroundType backgroundType;

    private TextureRegion tree;

    private Array<Candle> candles;

    private float wind_speed;

    private int candleX;
    private int candleY;

    boolean drawWithTint;

    public MenuBackground() {
        backgroundType = randomBackgroundType();
		backgroundType = BackgroundType.Jungle;

        tree = getTreeRegion(backgroundType);

        TitleBackground bg = new TitleBackground(backgroundType);
        this.addActor(bg);

        candleX = 0; // will be initialized in Resize
        candleY = 0;

        wind_speed = (float) (Math.random() - .5) * WIND_SPEED_INTENSITY;

        Candle candle = new Candle(candleX, candleY, 2, 1, 3);
        candles = new Array<Candle>();
        candles.add(candle);
        this.addActor(candle);
    }

    private BackgroundType randomBackgroundType() {
        return (BackgroundType) Random.getRandomValue(BackgroundType.values());
    }

    private void updateCandles() {
        if (Math.random() < 0.001f) {
            wind_speed = (float) (Math.random() - .5) * WIND_SPEED_INTENSITY;
        }
//		System.out.println(wind_speed);

        Candle.wind_x = wind_speed;

        // update text lighting
//		if (Math.random() < .15)
//			updateTextLighting();

        for (Candle candle : candles) {
            candle.generateParticles();
            candle.updatePositions();
        }
    }

    public void resize(int width, int height) {
        this.candleX = (int) (width/2);
        this.candleY = (int) (-height * 1/5) + 40;
        this.candles.first().move(candleX, candleY);
    }

    @Override
    public void act(float delta) {
        updateCandles();
    }

    @Override
    public void draw(Batch batch, float alpha){
        super.draw(batch, alpha);

        if (drawWithTint) {
            drawTintOverScreen(batch);
        }
    }

    private void drawTintOverScreen(Batch batch) {
        Color c = batch.getColor();
        batch.setColor(TINT);
        // Draw a dark rect over the whole screen...
        batch.draw(region, 0, 0, BesiegeMain.WIDTH, BesiegeMain.HEIGHT);
        batch.setColor(c);
    }

    private class TitleBackground extends Group {
        // Rain/snow
        private int currentRainIndex;
        private BPoint[] raindrops;
        private Color rainColor;
        private final float SECONDS_PER_DROP = 1f;
        private int raindropsPerFrame; // this needs to be based on raindrop_count
        private int raindrop_count = Random.getRandomInRange(50, 100);
        private float rainDrawOffsetX;
        private float rainDrawOffsetY;
        private TextureRegion white = new TextureRegion(new Texture("whitepixel.png"));
        private float X_SNOW_SPEED = Random.getRandomInRange(1.f, 2.5f);
        private float Y_SNOW_SPEED = Random.getRandomInRange(1.5f, 2f);
        private boolean X_RIGHT = Random.coinflip();

        // colors of rectangles, top to bottom
        private final Color[] sunset = {
//                new Color(68 / 256f, 0 / 256f, 76 / 256f, 1),
                new Color(45 / 256f, 0 / 256f, 50 / 256f, 1),
                new Color(60 / 256f, 0 / 256f, 30 / 256f, 1),
                new Color(80 / 256f, 0 / 256f, 0 / 256f, 1),
//                new Color(199 / 256f, 1 / 256f, 26 / 256f, 1),
                // this color is replaced according to background type.
                Color.BLACK,
        };

        private final Color[] night = {
                // Dark blue
                new Color(20 / 256f, 17 / 256f, 42 / 256f, 1),
                new Color(42 / 256f, 14 / 256f, 50 / 256f, 1),
                new Color(60 / 256f, 30 / 256f, 50 / 256f, 1),

                // this color is replaced according to background type.
                Color.BLACK,
        };

        private final Color[][] colorsets = {
//                sunset,
                night
        };

        private Color[] colors;

        private class Star {
            private float x, y, size;
            public Star(double x, double y, double size) {
                this.x = (float) x;
                this.y = (float) y;
                this.size = (float) size;
            }
        }
        private Star[] stars = new Star[40];
        private Star[] starsFewer = new Star[30];

        public TitleBackground(BackgroundType backgroundType) {
//		    colors = getColorSet(backgroundType).clone();
            colors = getRandomColorSet();
            randomizeBrightness(colors);

            colors[colors.length - 1] = getGroundColor(backgroundType);
            for (int i = 0; i < stars.length; i++) {
                stars[i] = new Star(Math.random(), Math.random() * 1 / colors.length , 1.5 + Math.random());
            }
            for (int i = 0; i < starsFewer.length; i++) {
                starsFewer[i] = new Star(Math.random(), Math.random() / colors.length, 0.75 + Math.random() / 2);
            }

            // initialize rain
            raindrops = new BPoint[raindrop_count];
            for (int i = 0; i < raindrop_count; i++) {
                raindrops[i] = new BPoint(0, 0);
            }

            float delta = 1.0f / 60;
            raindropsPerFrame = (int) (raindrop_count * delta / SECONDS_PER_DROP);
            if (raindropsPerFrame < 1) raindropsPerFrame = 1;
            System.out.println("raindrops per frame: " + raindropsPerFrame);

            if (isRaining()) {
                this.rainColor = RAINDROP_COLOR;
                this.rainColor.mul(DARKNESS*1f);
            }
            else {
                this.rainColor = SNOW_COLOR;
            }
        }

        private void randomizeBrightness(Color[] colors) {
            double effect = Random.getRandomInRange(1.25, 1.75);
            for (Color color : colors) {
                color.r *= effect;
                color.g *= effect;
                color.b *= effect;
            }
        }

        private Color[] getColorSet(BackgroundType backgroundType) {
            switch (backgroundType) {
                case Beach:
                    return sunset;
                case Forest:
                    return sunset;
                case Tundra:
                    return sunset;
                default: return night;
            }
        }

        private Color[] getRandomColorSet() {
            return (Color[]) Random.getRandomValue(colorsets);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            Color prev = batch.getColor();
            int stripeHeight = BesiegeMain.HEIGHT / colors.length;
            for (int i = 0; i < colors.length; i++) {
                Color color = colors[i];
                batch.setColor(color);
                batch.draw(region, 0,  BesiegeMain.HEIGHT - (i + 1) * stripeHeight, BesiegeMain.WIDTH, stripeHeight);
            }
            batch.setColor(prev);

            for (Star star : stars) {
                batch.setColor(1, 1, 1, 0.6f);
                if (Math.random() < 0.3) batch.setColor(1, 1, 1, 0.5f);
                batch.draw(region, star.x * BesiegeMain.WIDTH, BesiegeMain.HEIGHT - star.y  * BesiegeMain.HEIGHT, star.size, star.size);
            }
            for (Star star : starsFewer) {
                // Add flickering effect
                batch.setColor(1, 1, 1, 0.8f);
                if (Math.random() < 0.3) batch.setColor(1, 1, 1, 0.5f);
                batch.draw(region, star.x * BesiegeMain.WIDTH, BesiegeMain.HEIGHT - star.y  * BesiegeMain.HEIGHT - BesiegeMain.HEIGHT *1/colors.length, star.size, star.size);
            }
            // draw trees
            int treeHeight = (int) (stripeHeight * (1 + 1.2f/16));
            int treeWidth = treeHeight;
            for (int i = 0; i * treeWidth < BesiegeMain.WIDTH; i++) {
                batch.setColor(1, 1, 1, 1f);
                batch.draw(tree, i * treeWidth, BesiegeMain.HEIGHT - stripeHeight * 3 - stripeHeight * 1.1f /16, treeWidth, treeHeight);
            }


            // draw rain/snow
            boolean drawSnow = true;
            if (drawSnow) {
                if (isRaining() || isSnowing()) {

                    // Only add new drops while the list is not full. otherwise
                    // recycle drops
                    if (currentRainIndex < raindrops.length) {
                        for (int i = 0; i < raindropsPerFrame; i++) {
                            raindrops[currentRainIndex].pos_x = (int) (Math.random() * BesiegeMain.WIDTH);

                            raindrops[currentRainIndex].pos_y = (int) (Math.random() * BesiegeMain.HEIGHT);
                            // increment current rain index proportionally to the number of drops, otherwise the speed of drops
                            // will be too low.
                            currentRainIndex++;
                        }
                    }

                    Color c = batch.getColor();
                    Color mycolor = rainColor;

                    // we can figure out how much to fade drop by calculating distance between its index and currentrainindex,
                    // then divide by array size to get between 0 and 1 yay

                    // eg if index = 20 and currentIndex = 10, diff is (20-10)/40 = 1/4
                    // eg if index = 20 and currentIndex = 25, diff is 40 + (20 - 25) =

                    // This is nice because it makes the raindrops look "softer"
                    float alpha_minus = .2f;

                    if (isSnowing()) {
                        mycolor = SNOW_COLOR;

                        rainDrawOffsetX += X_SNOW_SPEED;
                        rainDrawOffsetY += Y_SNOW_SPEED;

                        alpha_minus = 0.0f; // makes snow last longer

                        //					if (rainDrawOffsetX >= this.total_width) rainDrawOffsetX = 0;
                    }

                    for (int i = 0; i < currentRainIndex; i++) {
                        BPoint p = raindrops[i];
                        double indexDiff = i - currentRainIndex;
                        if (indexDiff < 0) indexDiff += raindrops.length;

                        // Add a bit of variation based on the index
                        float drawAtY = (p.pos_y + rainDrawOffsetY) % (BesiegeMain.HEIGHT);
                        // IN this case, we make color based on height.
                        mycolor.a = 1 - (drawAtY / BesiegeMain.HEIGHT);

                        float drawAtX = (p.pos_x + rainDrawOffsetX + (mycolor.a * 200 * (i - raindrops.length/2f)/raindrops.length)) % (BesiegeMain.WIDTH);

                        // This makes some drops disappear early for an interesting effect
                        mycolor.a *= (i * 1.f / raindrops.length);
                        if (mycolor.a < 0) mycolor.a = 0;

                        batch.setColor(mycolor);

                        if (X_RIGHT)
                            batch.draw(white, (drawAtX), BesiegeMain.HEIGHT - (drawAtY), 5, 5);
                        else
                            batch.draw(white, (BesiegeMain.WIDTH - drawAtX), BesiegeMain.HEIGHT - (drawAtY), 5, 5);
                    }

                    batch.setColor(c);
                }
            }
        }
    }

    public boolean isSnowing() {
//		return true;
        return backgroundType == BackgroundType.Tundra;
    }

    public boolean isRaining() {
        return false;
//		return !this.isSnowing() && stage.raining;
    }


    private Color getGroundColor(BackgroundType type) {
        switch (type) {
            case Forest:
                return new Color(30 / 256f, 45 / 256f, 30 / 256f, 1);
            case Jungle:
                return new Color(20 / 256f, 40 / 256f, 25 / 256f, 1);
            case Swamp:
                return new Color(20 / 256f, 40 / 256f, 35 / 256f, 1);
            case Tundra:
                return new Color(150 / 256f, 150 / 256f, 150 / 256f, 1);
            case Beach:
                return new Color(160 / 256f, 150 / 256f, 120 / 256f, 1);
            default:
                return Color.BLACK;
        }
    }

    private TextureRegion getTreeRegion(BackgroundType type) {
        switch (type) {
            case Forest:
                return new TextureRegion(new Texture("blacktree5.png"));
            case Tundra:
                return new TextureRegion(new Texture("blacktree5.png"));
            case Beach:
                return new TextureRegion(new Texture("palmtree.png"));
            case Jungle:
                return new TextureRegion(new Texture("palmtree.png"));
            case Swamp:
                return new TextureRegion(new Texture("blacktree3.png"));
            default:
                return new TextureRegion(new Texture("null"));
        }
    }

    public void drawWithTint(boolean drawWithTint) {
        this.drawWithTint = drawWithTint;
    }
}

