package com.spasic.eclipsethorne;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.spasic.eclipsethorne.Screens.GameSreen;
import com.spasic.eclipsethorne.Screens.LoadingScreen;
import com.spasic.eclipsethorne.Screens.StartMenuScreen;
import de.eskalon.commons.core.ManagedGame;
import de.eskalon.commons.screen.ManagedScreen;
import de.eskalon.commons.screen.transition.ScreenTransition;
import de.eskalon.commons.screen.transition.impl.BlendingTransition;
import de.eskalon.commons.screen.transition.impl.PushTransition;
import de.eskalon.commons.screen.transition.impl.SlidingDirection;
import de.eskalon.commons.screen.transition.impl.VerticalSlicingTransition;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class EclipseThorne extends ManagedGame<ManagedScreen, ScreenTransition> {

    public static float WORLD_WIDTH = 1200;
    public static float WORLD_HEIGHT = 1200;
    public static int VIEWPORT_WIDTH = 1600;
    public static int VIEWPORT_HEIGHT = 900;

    public Skin skin;



    public SpriteBatch spriteBatch;

    @Override
    public void create() {
        super.create();
        spriteBatch = new SpriteBatch();

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        this.screenManager.addScreen("StartMenu", new StartMenuScreen());
        this.screenManager.addScreen("GameScreen", new GameSreen());
        this.screenManager.addScreen("LoadingScreen", new LoadingScreen());

        PushTransition pushTransition = new PushTransition(spriteBatch, SlidingDirection.RIGHT, 1.0f);
        VerticalSlicingTransition verticalSlicingTransition = new VerticalSlicingTransition(spriteBatch, 8, 1F);
        BlendingTransition blendingTransition = new BlendingTransition(spriteBatch, 1f);

        screenManager.addScreenTransition("pushTransition", pushTransition);
        screenManager.addScreenTransition("blendingTransition", blendingTransition);
        screenManager.addScreenTransition("verticalSlicingTransition", verticalSlicingTransition);

        this.screenManager.pushScreen("StartMenu", "blendingTransition");

    }
}
