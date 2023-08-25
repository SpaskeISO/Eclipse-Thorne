package com.spasic.eclipsethorne;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.eskalon.commons.screen.ManagedScreen;
import lombok.Getter;
import lombok.Setter;

/** First screen of the application. Displayed after the application is created. */
@Getter
@Setter
public class StartMenuScreen extends ManagedScreen {

    private EclipseThorne game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private Stage stage;
    private Skin skin;
    private Table UI;

    public StartMenuScreen(){
        this.game = (EclipseThorne) Gdx.app.getApplicationListener();
    }

    @Override
    protected void create() {
        this.camera = new OrthographicCamera();
        this.viewport = new FillViewport(EclipseThorne.VIEWPORT_WIDTH, EclipseThorne.VIEWPORT_HEIGHT, camera);
        this.stage = new Stage(this.viewport);
        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        this.shapeRenderer = new ShapeRenderer();
        Gdx.input.setInputProcessor(stage);

        createUI();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.circle(EclipseThorne.WORLD_WIDTH / 2.0f, EclipseThorne.WORLD_HEIGHT /2.0f, 15f);
        shapeRenderer.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 60f));
        stage.draw();
        // Draw your screen here. "delta" is the time since last render in seconds.
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        // Resize your screen here. The parameters represent the new window size.
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
    }

    private void createUI(){
        UI = new Table();
        UI.setFillParent(true);
        TextButton.TextButtonStyle style = skin.get(TextButton.TextButtonStyle.class);
        BitmapFont font = style.font;

        font.getData().setScale(2.0f);
        final Label GameLabel = new Label("Eclipse Throne", skin);
        GameLabel.setAlignment(Align.center);
        final TextButton startButton = new TextButton("Start", skin);
        final TextButton quitButton = new TextButton("Quit", skin);
        startButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.getScreenManager().pushScreen("GameScreen", "blendingTransition");
            }
        });



        quitButton.addListener( new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        System.out.println(stage.getRoot().toString());
        UI.add(GameLabel).pad(10).size(Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.05f).row();
        UI.add(startButton).pad(10).size(Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.05f).row();
        UI.add(quitButton).pad(10).size(Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.05f).row();

        stage.addActor(UI);

    }

}