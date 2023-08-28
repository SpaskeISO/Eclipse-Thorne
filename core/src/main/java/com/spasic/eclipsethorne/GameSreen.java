package com.spasic.eclipsethorne;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.dongbat.jbump.Item;
import com.dongbat.jbump.Rect;
import com.dongbat.jbump.World;
import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.room.dungeon.DungeonGenerator;
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
    public static TextureAtlas textureAtlas;
    public static ShapeDrawer shapeDrawer;

    // Camera
    public static ExtendViewport viewport;
    public static OrthographicCamera camera;
    public float aspectRatio = (float) Gdx.graphics.getHeight() / (float) Gdx.graphics.getWidth();
    Interpolation interpolation = Interpolation.smooth;
    Vector3 interpolatedPosition = new Vector3();

    public boolean debug = false;

    public static SnapshotArray<Entity> entities;
    public static World<Entity> world;
    public static final Vector2 vector2 = new Vector2();
    public static Player player;

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


    public GameSreen(){
        this.game = (EclipseThorne) Gdx.app.getApplicationListener();
    }

    @Override
    protected void create() {
        spriteBatch = new SpriteBatch();
        textureAtlas = new TextureAtlas("textures.atlas");
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(EclipseThorne.WORLD_WIDTH / 100, EclipseThorne.WORLD_HEIGHT / 100, camera);
        viewport.apply();

        entities = new SnapshotArray<>();
        world = new World<>();
        shapeDrawer = new ShapeDrawer(spriteBatch, textureAtlas.findRegion("white"));

        grid = new Grid(128); // This algorithm likes odd-sized maps, although it works either way.

        dungeonGenerator = new DungeonGenerator();
        dungeonGenerator.setRoomGenerationAttempts(500);
        //dungeonGenerator.setMaxRoomSize(75);
        dungeonGenerator.setMaxRoomSize(45);
        dungeonGenerator.setTolerance(10); // Max difference between width and height.
        dungeonGenerator.setMinRoomSize(9);
        dungeonGenerator.generate(grid);

        generateMap();
        getTileBounds();
        //getSpawnPoints();
        getEnemySpawnPoints();

        // Spawn Player
        setPlayerSpawnPoint();
        player = new Player(playerSpawnPoint.x + EclipseThorne.WORLD_WIDTH / 2, playerSpawnPoint.y + EclipseThorne.WORLD_HEIGHT / 2);

        camera.position.set(player.x , player.y, 0);



        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);



        // For camera panning
        targetPosition = new Vector3(player.x, player.y, 0); // Set your player's initial position here
        followSpeed = 10.0f; // Adjust this value to control the camera's follow speed





        spawnEnemies();




        tiles[0] = textureAtlas.findRegion("tile-1");
        tiles[1] = textureAtlas.findRegion("tile-2");
        tiles[2] = textureAtlas.findRegion("tile-3");


        spriteBatch.setProjectionMatrix(camera.combined);


    }

    @Override
    public void hide() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        input();

        moveCamera(delta);


        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        drawTiles();

        //createEnemies(delta);

        entityLogic(delta);

        drawEntities();

        if(debug){
            drawDebug();
        }
        spriteBatch.setColor(Color.WHITE);
        spriteBatch.end();

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void dispose() {
        textureAtlas.dispose();
    }

    public void input(){
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            debug = !debug;
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

    public void getTileBounds(){
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

    public void getSpawnPoints(){
        System.out.println(roomTiles.size() / 400);
        int noEnemySpawnPoints = roomTiles.size() / 200;
        while (enemySpawnPoints.size() < noEnemySpawnPoints){
            for(int i = 0; i < roomTiles.size(); i++){
                if(1 >= MathUtils.random(0, 100)){
                    enemySpawnPoints.add((roomTiles.get(i)));
                }
                if(enemySpawnPoints.size() > noEnemySpawnPoints) break;
            }
        }

    }

    public void getEnemySpawnPoints(){
        int width = grid.getWidth();
        int height = grid.getHeight();

        float tempCell = 0;
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                tempCell = 1f - grid.get(i, j);
                if (tempCell == 0.5f && isValidEnemySpawnPoint(i, j) && 0.025f >= MathUtils.random(1.0f)) {
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

}
