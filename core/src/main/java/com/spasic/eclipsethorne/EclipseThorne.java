package com.spasic.eclipsethorne;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import de.eskalon.commons.core.ManagedGame;
import de.eskalon.commons.screen.ManagedScreen;
import de.eskalon.commons.screen.transition.ScreenTransition;
import de.eskalon.commons.screen.transition.impl.BlendingTransition;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class EclipseThorne extends ManagedGame<ManagedScreen, ScreenTransition> {

    public static float WORLD_WIDTH = 1200;
    public static float WORLD_HEIGHT = 1200;
    public static int VIEWPORT_WIDTH = 1600;
    public static int VIEWPORT_HEIGHT = 900;



    public SpriteBatch spriteBatch;

    @Override
    public void create() {
        super.create();
        spriteBatch = new SpriteBatch();

        this.screenManager.addScreen("StartMenu", new StartMenuScreen());
        this.screenManager.addScreen("GameScreen", new GameSreen());

        BlendingTransition blendingTransition = new BlendingTransition(spriteBatch, 1f);
        screenManager.addScreenTransition("blendingTransition", blendingTransition);

        this.screenManager.pushScreen("StartMenu", "blendingTransition");

    }
}
