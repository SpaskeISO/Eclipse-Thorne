package com.spasic.eclipsethorne.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spasic.eclipsethorne.EclipseThorne;
import de.eskalon.commons.screen.ManagedScreen;

public class LoadingScreen extends ManagedScreen {

    private EclipseThorne game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private Stage stage;
    private Table UI;
    private static Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
    public static ProgressBar loadingProgressBar = new ProgressBar(0.0f, 100.0f, 1.0f, false, skin);

    public LoadingScreen(){
        this.game = (EclipseThorne) Gdx.app.getApplicationListener();
    }

    @Override
    protected void create() {
        this.camera = new OrthographicCamera();
        this.viewport = new FillViewport(EclipseThorne.VIEWPORT_WIDTH, EclipseThorne.VIEWPORT_HEIGHT, camera);
        this.stage = new Stage(this.viewport);
        this.shapeRenderer = new ShapeRenderer();
        Gdx.input.setInputProcessor(stage);

        createLoadingUI();
    }

    @Override
    public void hide() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 60f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void dispose() {

    }

    public void createLoadingUI(){
        UI = new Table(game.skin);
        UI.setFillParent(true);

        TextButton.TextButtonStyle style = game.skin.get(TextButton.TextButtonStyle.class);
        BitmapFont font = style.font;

        font.getData().setScale(2.0f);

        final Label loadingLabel = new Label("Loading...", game.skin);
        loadingLabel.setAlignment(Align.center);
        loadingProgressBar = new ProgressBar(0.0f, 100.0f, 1.0f, false, game.skin);


        UI.add(loadingLabel).pad(10).size(Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.05f).row();
        UI.add(loadingProgressBar).pad(10).size(Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.05f).row();

        stage.addActor(UI);
    }
}
