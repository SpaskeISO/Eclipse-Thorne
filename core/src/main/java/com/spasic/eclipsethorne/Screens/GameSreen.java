package com.spasic.eclipsethorne.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.dongbat.jbump.Item;
import com.dongbat.jbump.Rect;
import com.dongbat.jbump.World;
import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.room.dungeon.DungeonGenerator;
import com.spasic.eclipsethorne.EclipseThorne;
import com.spasic.eclipsethorne.Entities.*;
import de.eskalon.commons.screen.ManagedScreen;
import lombok.Getter;
import lombok.Setter;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;

import static com.spasic.eclipsethorne.Utils.*;

@Getter
@Setter
public class GameSreen extends ManagedScreen {

    private EclipseThorne game;

    public SpriteBatch spriteBatch;
    public SpriteBatch spriteBatchUI;
    public static TextureAtlas textureAtlas;
    public static ShapeDrawer shapeDrawer;
    public static ShapeDrawer shapeDrawerUI;
    public static boolean nextLevel = false;
    public static int GameLevel = 0;

    public static Sound fireballHit = Gdx.audio.newSound(Gdx.files.internal("sound effects/explosion.wav"));
    public static Sound xpPickUp = Gdx.audio.newSound(Gdx.files.internal("sound effects/XP-PickUp.wav"));
    public static Sound teleport = Gdx.audio.newSound(Gdx.files.internal("sound effects/teleport.wav"));
    public static Sound levelUp = Gdx.audio.newSound(Gdx.files.internal("sound effects/LVLUP.wav"));
    public static Music hurtSound = Gdx.audio.newMusic(Gdx.files.internal("sound effects/hurt2.wav"));

    // Camera
    public static ExtendViewport viewport;
    public static OrthographicCamera camera;

    public static ScreenViewport viewportUI;
    public static OrthographicCamera cameraUI;

    public float aspectRatio = (float) Gdx.graphics.getHeight() / (float) Gdx.graphics.getWidth();
    Interpolation interpolation = Interpolation.smooth;
    Vector3 interpolatedPosition = new Vector3();

    public boolean debug;
    public boolean paused;
    public static boolean gameOver;

    public static SnapshotArray<Entity> entities;
    public static World<Entity> world;
    public static final Vector2 vector2 = new Vector2();
    public static Player player;
    public static Portal portal;

    private Stage stage;

    public static float ENEMY_DELAY = 1.0f;
    public static float CAMERA_MOVEMENT_SPEED = 1;
    public float enemyTimer = -5;
    public Texture mapTexture;

    private Vector3 targetPosition;
    private float followSpeed;

    // Map
    private Grid grid;
    private DungeonGenerator dungeonGenerator;
    public static int[][] neighbors = {
        { -1, -1 }, { -1, 0 }, { -1, 1 },
        { 0, -1 },             { 0, 1 },
        { 1, -1 }, { 1, 0 },  { 1, 1 }
    };
    private ArrayList<Vector3> bounds = new ArrayList<>();
    private TextureRegion[] tiles = new TextureRegion[3];

    // Spawn Points
    private ArrayList<Vector2> roomTiles = new ArrayList<>();
    private ArrayList<Vector2> enemySpawnPoints = new ArrayList<>();
    private ArrayList<Vector2> playerSpawnPoints = new ArrayList<>();
    private Vector2 playerSpawnPoint;
    private Vector2 portalSpawnPoint;

    // Scene2D UI
    public Group PauseGroup;
    public Group GameOverGroup;
    public Group UpgradesGroup;
    public Table PauseUI;
    public Table GameOverUI;
    public Table UpgradesUI;
    public static boolean LEVEL_UP;
    public static boolean levelUpSoundPlayed = false;


    public GameSreen(){
        this.game = (EclipseThorne) Gdx.app.getApplicationListener();


    }

    @Override
    protected void create() {
        spriteBatch = new SpriteBatch();
        spriteBatchUI = new SpriteBatch();
        textureAtlas = new TextureAtlas("textures.atlas");
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(EclipseThorne.WORLD_WIDTH / 100, EclipseThorne.WORLD_HEIGHT / 100, camera);
        viewport.apply();

        cameraUI = new OrthographicCamera();
        viewportUI = new ScreenViewport(cameraUI);

        LEVEL_UP = false;
        debug = false;
        paused = false;
        gameOver = false;


        entities = new SnapshotArray<>();
        world = new World<>();
        shapeDrawer = new ShapeDrawer(spriteBatch, textureAtlas.findRegion("white"));
        shapeDrawerUI = new ShapeDrawer(spriteBatchUI, textureAtlas.findRegion("white"));

        tiles[0] = textureAtlas.findRegion("tile-1");
        tiles[1] = textureAtlas.findRegion("tile-2");
        tiles[2] = textureAtlas.findRegion("tile-3");

        stage = new Stage(viewportUI);
        Gdx.input.setInputProcessor(stage);


        spriteBatch.setProjectionMatrix(camera.combined);

        grid = new Grid(128); // This algorithm likes odd-sized maps, although it works either way.
        dungeonGenerator = new DungeonGenerator();
        dungeonGenerator.setRoomGenerationAttempts(500);
        dungeonGenerator.setMaxRoomSize(35);
        dungeonGenerator.setTolerance(10); // Max difference between width and height.
        dungeonGenerator.setMinRoomSize(9);
        //camera.zoom = 4.0f;
        firstLevelSetup();

        // For camera panning
        targetPosition = new Vector3(player.x, player.y, 0); // Set your player's initial position here
        followSpeed = 10.0f; // Adjust this value to control the camera's follow speed

        CreateUI();

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {

    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if(Gdx.input.getInputProcessor() != stage) Gdx.input.setInputProcessor(stage);

        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewportUI.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        camera.update();
        cameraUI.update();


        if(nextLevel){
            levelSetup();
        }
        else{
            spriteBatch.begin();

            spriteBatch.setProjectionMatrix(camera.combined);
            input();

            moveCamera(delta);



            drawTiles();

            if(!paused && !gameOver && !LEVEL_UP){
                entityLogic(delta);
            }


            drawEntities();

            if(debug){
                drawDebug();
            }
            spriteBatch.setColor(Color.WHITE);
            spriteBatch.end();

            spriteBatchUI.begin();
            spriteBatchUI.setProjectionMatrix(cameraUI.combined);
            renderGameUI();


            spriteBatchUI.end();

            PauseGroup.setVisible(paused);
            GameOverGroup.setVisible(gameOver);
            if(LEVEL_UP && !levelUpSoundPlayed){
                levelUp.play();
                levelUpSoundPlayed = true;
            }
            UpgradesGroup.setVisible(LEVEL_UP);

            stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 60f));
            stage.draw();
        }




    }

    @Override
    public void resize(int width, int height) {
        cameraUI.viewportWidth = width;
        cameraUI.viewportHeight = height;
        cameraUI.update();

        PauseGroup.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        GameOverGroup.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        UpgradesGroup.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

    }

    @Override
    public void dispose() {
        fireballHit.dispose();
        xpPickUp.dispose();
        teleport.dispose();
        levelUp.dispose();
        hurtSound.dispose();
        textureAtlas.dispose();
    }

    public void input(){
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            debug = !debug;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            paused = !paused;
        }
    }

    public void moveCamera(float delta){
        targetPosition.set(player.x, player.y, 0);
        // Calculate interpolation value based on follow speed
        float alpha = delta * followSpeed;

        // Interpolate the camera position towards the target position
        interpolatedPosition.x = interpolation.apply(camera.position.x, targetPosition.x, alpha);
        interpolatedPosition.y = interpolation.apply(camera.position.y, targetPosition.y, alpha);
        interpolatedPosition.z = camera.position.z; // Maintain camera's Z position

        // Update the camera position
        camera.position.set(interpolatedPosition);
        camera.update();
    }

    public void createEnemies(float delta){
        enemyTimer -= delta;
        if (enemyTimer < 0) {
            enemyTimer = ENEMY_DELAY;


            vector2.set(camera.viewportWidth / 2, camera.viewportHeight / 2);
            vector2.rotateDeg(MathUtils.random(360f));
            vector2.add(camera.position.x, camera.position.y);
            new Enemy(EnemyType.values()[MathUtils.random(0, 8)], vector2.x, vector2.y);
        }
    }

    public void entityLogic(float delta){
        //call logic on all entities
        Object[] ents = entities.begin();
        for (int i = 0, n = entities.size; i < n; i++) {
            Entity entity = (Entity) ents[i];
            if (isEntityVisible(entity)) {
                entity.act(delta);
            }
        }
        entities.end();
    }

    public void drawEntities(){
        //draw all entities
        for (Entity entity : entities) {
            if (isEntityVisible(entity)) {
                entity.draw(spriteBatch);
            }
        }
    }

    public void drawDebug(){
        //draw debug
        for (Entity entity : entities) {
            Item<Entity> item = entity.item;
            if (item != null) {
                shapeDrawer.setColor(Color.RED);
                shapeDrawer.setDefaultLineWidth(0.05f);
                Rect rect = world.getRect(item);
                shapeDrawer.rectangle(rect.x, rect.y, rect.w, rect.h);
            }
        }
    }





    public void generateMap(){
        float tempCell = 0;
        for(int i = grid.getWidth() - 1; i >= 0; i--){
            for(int j = 0; j < grid.getHeight() ; j++){
                tempCell = 1f - grid.get(i, j);
                if(tempCell == 0){
                    for(int[] offset : neighbors){
                        int newCol = i + offset[0];
                        int newRow = j + offset[1];

                        if(isValidPos(newCol, newRow, grid.getWidth(), grid.getHeight())){
                            tempCell = 1f - grid.get(newCol, newRow);
                            if(tempCell == 0.5f || tempCell == 1.0f){
                                entities.add(new BasicBlock(EclipseThorne.WORLD_WIDTH / 2 + i, EclipseThorne.WORLD_HEIGHT / 2 + j));
                                break;
                            }
                        }
                    }
                }
                else if(tempCell == 0.5f){
                    for(int[] offset : neighbors){
                        int newCol = i + offset[0];
                        int newRow = j + offset[1];

                        if(isValidPos(newCol, newRow, grid.getWidth(), grid.getHeight())){
                            tempCell = 1f - grid.get(newCol, newRow);
                            if(tempCell == 1.0f){
                                entities.add(new DoorBlock(EclipseThorne.WORLD_WIDTH / 2 + i, EclipseThorne.WORLD_HEIGHT / 2 + j));
                                break;
                            }
                        }
                    }
                }

            }
        }

    }

    public void drawTiles() {
        float halfWorldWidth = EclipseThorne.WORLD_WIDTH / 2;
        float halfWorldHeight = EclipseThorne.WORLD_HEIGHT / 2;

        for (Vector3 bound : bounds) {
            if(isTileVisible(bound)){
                spriteBatch.draw(tiles[(int) bound.z], halfWorldWidth + bound.x, halfWorldHeight + bound.y, 1, 1);
            }
        }
    }

    private void getTileBounds(){
        int width = grid.getWidth();
        int height = grid.getHeight();

        float tempCell = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                tempCell = 1f - grid.get(i, j);
                if (tempCell  == 1 || tempCell == 0.5f) {
                    bounds.add(new Vector3(i, j, MathUtils.random(0, 2)));
                    if(tempCell == 0.5){
                        roomTiles.add(new Vector2(i, j));
                    }
                    else {
                        playerSpawnPoints.add(new Vector2(i, j));
                    }
                }

            }
        }
    }

    private void getEnemySpawnPoints(){
        int width = grid.getWidth();
        int height = grid.getHeight();

        float tempCell = 0;
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                tempCell = 1f - grid.get(i, j);
                if (tempCell == 0.5f && isValidEnemySpawnPoint(i, j) && 0.02f >= MathUtils.random(1.0f)) {
                    enemySpawnPoints.add(new Vector2(i, j));
                }
            }
        }
    }

    private void spawnEnemies(){
        for (Vector2 enemySpawnPoint : enemySpawnPoints) {
            new Enemy(EnemyType.values()[MathUtils.random(0, 8)], EclipseThorne.WORLD_WIDTH / 2 + enemySpawnPoint.x,
                                                                EclipseThorne.WORLD_WIDTH / 2 + enemySpawnPoint.y);
        }
    }

    private void setPlayerSpawnPoint(){
        int index = MathUtils.random(playerSpawnPoints.size() - 1);
        playerSpawnPoint = new Vector2(playerSpawnPoints.get(index).x, playerSpawnPoints.get(index).y);
    }

    private void spawnPortal(){
        portal = new Portal(portalSpawnPoint.x, portalSpawnPoint.y);
    }

    private void setPortalSpawnPoint(){
        int index = MathUtils.random(enemySpawnPoints.size() - 1);
        portalSpawnPoint = new Vector2(enemySpawnPoints.get(index).x, enemySpawnPoints.get(index).y);
    }


    public boolean isValidEnemySpawnPoint(int i, int j){
        int width = grid.getWidth();
        int height = grid.getHeight();

        for(int[] offset : neighbors){
            int newCol = i + offset[0];
            int newRow = j + offset[1];
            if(isValidPos(newCol, newRow, width, height) && 1f - grid.get(newCol, newRow) != 0.5f){
                return false;
            }
        }

        return true;
    }

    public void firstLevelSetup(){
        entities.clear();
        world.reset();
        bounds.clear();


        // Spawn Points
        roomTiles.clear();
        enemySpawnPoints.clear();
        playerSpawnPoints.clear();


        dungeonGenerator.generate(grid);
        generateMap();
        getTileBounds();
        getEnemySpawnPoints();

        // Spawn Player
        setPlayerSpawnPoint();
        player = new Player(playerSpawnPoint.x + EclipseThorne.WORLD_WIDTH / 2, playerSpawnPoint.y + EclipseThorne.WORLD_HEIGHT / 2);

        camera.position.set(player.x , player.y, 0);

        setPortalSpawnPoint();
        spawnPortal();

        spawnEnemies();
    }

    public void levelSetup(){
        nextLevel = false;
        LoadingScreen.loading = true;
        game.getScreenManager().pushScreen("LoadingScreen", "verticalSlicingTransition");
        entities.clear();
        world.reset();
        bounds.clear();

        LEVEL_UP = false;
        debug = false;
        paused = false;
        gameOver = false;

        // Spawn Points
        roomTiles.clear();
        enemySpawnPoints.clear();
        playerSpawnPoints.clear();

        dungeonGenerator.generate(grid);
        generateMap();
        getTileBounds();
        getEnemySpawnPoints();

        // Spawn Player
        setPlayerSpawnPoint();
        player = new Player(playerSpawnPoint.x + EclipseThorne.WORLD_WIDTH / 2, playerSpawnPoint.y + EclipseThorne.WORLD_HEIGHT / 2, player);
        camera.position.set(player.x , player.y, 0);


        setPortalSpawnPoint();
        spawnPortal();

        spawnEnemies();
    }

    public void CreateUI(){
        CreatePauseUI();
        CreateGameOverUI();
        CreateUpgradeUI();
    }

    public void CreatePauseUI(){
        PauseGroup = new Group();
        PauseGroup.setVisible(false);
        PauseUI = new Table(game.skin);
        PauseUI.setFillParent(true);


        TextButton.TextButtonStyle style = game.skin.get(TextButton.TextButtonStyle.class);
        BitmapFont font = style.font;
        font.getData().setScale(2.0f);

        final Label pauseLabel = new Label("Game Paused", game.skin);
        pauseLabel.setAlignment(Align.center);

        final TextButton resumeButton = new TextButton("Resume", game.skin);
        resumeButton.addListener( new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                paused = false;
            }
        });

        final TextButton quitButton = new TextButton("Quit", game.skin);
        quitButton.addListener( new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.getScreenManager().pushScreen("StartMenu", "blendingTransition");
            }
        });

        PauseUI.add(pauseLabel).pad(10).size(Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.05f).row();
        PauseUI.add(resumeButton).pad(10).size(Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.05f).row();
        PauseUI.add(quitButton).pad(10).size(Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.05f).row();


        PauseGroup.addActor(PauseUI);
        PauseGroup.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.addActor(PauseGroup);

    }

    public void CreateGameOverUI(){
        GameOverGroup = new Group();
        GameOverGroup.setVisible(false);
        GameOverUI = new Table(game.skin);
        GameOverUI.setFillParent(true);


        TextButton.TextButtonStyle style = game.skin.get(TextButton.TextButtonStyle.class);
        BitmapFont font = style.font;
        font.getData().setScale(2.0f);

        final Label gameOverLabel = new Label("GAME OVER", game.skin);
        gameOverLabel.setAlignment(Align.center);
        final TextButton playAgainButton = new TextButton("Play Again", game.skin);
        playAgainButton.addListener( new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameOver = false;
                firstLevelSetup();
            }
        });
        final TextButton quitButton = new TextButton("QUIT", game.skin);
        quitButton.addListener( new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.getScreenManager().pushScreen("StartMenu", "blendingTransition");
            }
        });

        GameOverUI.add(gameOverLabel).pad(10).size(Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.05f).row();
        GameOverUI.add(playAgainButton).pad(10).size(Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.05f).row();
        GameOverUI.add(quitButton).pad(10).size(Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.05f).row();

        GameOverGroup.addActor(GameOverUI);
        GameOverGroup.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.addActor(GameOverGroup);
    }

    public void CreateUpgradeUI(){
        UpgradesGroup = new Group();
        UpgradesGroup.setVisible(false);
        UpgradesUI = new Table(game.skin);
        UpgradesUI.setFillParent(true);

        TextButton.TextButtonStyle style = game.skin.get(TextButton.TextButtonStyle.class);
        BitmapFont font = style.font;
        font.getData().setScale(2.0f);

        final TextButton attackPowerUpgradeButton = new TextButton("AP +5", game.skin);
        attackPowerUpgradeButton.addListener( new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                player.AP += 5;
                LEVEL_UP = false;
                levelUpSoundPlayed = false;
            }
        });

        final TextButton healthUpgradeButton = new TextButton("HP +5", game.skin);
        healthUpgradeButton.addListener( new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                player.maxHP += 5;
                LEVEL_UP = false;
                levelUpSoundPlayed = false;
            }
        });

        final TextButton movementSpeedUpgradeButton = new TextButton("MS +0.5", game.skin);
        movementSpeedUpgradeButton.addListener( new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                player.movementSpeed += 0.5f;
                LEVEL_UP = false;
                levelUpSoundPlayed = false;
            }
        });

        UpgradesUI.add(attackPowerUpgradeButton).pad(10).size(Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.05f).row();
        UpgradesUI.add(healthUpgradeButton).pad(10).size(Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.05f).row();
        UpgradesUI.add(movementSpeedUpgradeButton).pad(10).size(Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.05f).row();

        UpgradesGroup.addActor(UpgradesUI);
        UpgradesGroup.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.addActor(UpgradesGroup);
    }

    public void renderGameUI(){
        // XP Frame
        shapeDrawerUI.setColor(Color.GOLDENROD);
        shapeDrawerUI.filledRectangle(0, (Gdx.graphics.getHeight() / 40.0f) * 39.0f, Gdx.graphics.getWidth(),
                                    Gdx.graphics.getHeight() / 40.0f);
        // XP background
        shapeDrawerUI.setColor(Color.GRAY);
        shapeDrawerUI.filledRectangle(Gdx.graphics.getWidth() - (Gdx.graphics.getWidth() / 40.0f * 39.0f), ((Gdx.graphics.getHeight() / 40.0f) * 39.2f),
                                    Gdx.graphics.getWidth() - (Gdx.graphics.getWidth() / 40.0f * 2.0f),
                                    Gdx.graphics.getHeight() / 40.0f - Gdx.graphics.getHeight() / 40.0f * 0.4f);
        shapeDrawerUI.setColor(Color.TEAL);

        // Current XP
        float width = player.currentXP / player.nextLevelXP;
        shapeDrawerUI.filledRectangle(Gdx.graphics.getWidth() - (Gdx.graphics.getWidth() / 40.0f * 39.0f), ((Gdx.graphics.getHeight() / 40.0f) * 39.2f),
            (Gdx.graphics.getWidth() - (Gdx.graphics.getWidth() / 40.0f * 2.0f)) * ( player.currentXP / player.nextLevelXP),
            Gdx.graphics.getHeight() / 40.0f - Gdx.graphics.getHeight() / 40.0f * 0.4f);
    }

    public void reset(){
        viewport.apply();


        LEVEL_UP = false;
        debug = false;
        paused = false;
        gameOver = false;

        spriteBatch.setProjectionMatrix(camera.combined);

        //camera.zoom = 4.0f;
        firstLevelSetup();

        // For camera panning
        targetPosition = new Vector3(player.x, player.y, 0); // Set your player's initial position here
        followSpeed = 10.0f; // Adjust this value to control the camera's follow speed

    }

}
