package com.spasic.eclipsethorne;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.dongbat.jbump.Item;
import com.dongbat.jbump.Rect;
import com.dongbat.jbump.World;
import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.room.dungeon.DungeonGenerator;
import com.spasic.eclipsethorne.Entities.Enemy;
import com.spasic.eclipsethorne.Entities.EnemyType;
import com.spasic.eclipsethorne.Entities.Entity;
import com.spasic.eclipsethorne.Entities.Player;
import de.eskalon.commons.screen.ManagedScreen;
import lombok.Getter;
import lombok.Setter;
import space.earlygrey.shapedrawer.ShapeDrawer;

@Getter
@Setter
public class GameSreen extends ManagedScreen {

    private EclipseThorne game;

    public SpriteBatch spriteBatch;
    public static TextureAtlas textureAtlas;
    public static ShapeDrawer shapeDrawer;
    public static ExtendViewport viewport;
    public static OrthographicCamera camera;
    public float aspectRatio = (float) Gdx.graphics.getHeight() / (float) Gdx.graphics.getWidth();

    public static SnapshotArray<Entity> entities;
    public static World<Entity> world;
    public static final Vector2 vector2 = new Vector2();
    public static Player player;

    private Stage stage;

    private Sprite arch;
    public static float ENEMY_DELAY = 1.0f;
    public float enemyTimer = -5;
    public Texture mapTexture;











    public GameSreen(){
        this.game = (EclipseThorne) Gdx.app.getApplicationListener();
        create();
    }

    @Override
    protected void create() {
        spriteBatch = new SpriteBatch();
        textureAtlas = new TextureAtlas("textures.atlas");
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(EclipseThorne.WORLD_WIDTH * aspectRatio / 40.0f, EclipseThorne.WORLD_HEIGHT / 40.0f, camera);
        viewport.apply();

        entities = new SnapshotArray<>();
        world = new World<>();
        shapeDrawer = new ShapeDrawer(spriteBatch, textureAtlas.findRegion("white"));



        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        entities = new SnapshotArray<>();

        player = new Player();
        camera.position.set(player.x, player.y, 0);
        camera.zoom = 0.5f;

        Animation<TextureRegion> animation = new Animation<TextureRegion>(0.033f, textureAtlas.findRegions("player-move"), Animation.PlayMode.LOOP);

        final Pixmap map = new Pixmap(512, 512, Pixmap.Format.RGBA8888);
        final Grid grid = new Grid(512); // This algorithm likes odd-sized maps, although it works either way.

        final DungeonGenerator dungeonGenerator = new DungeonGenerator();
        dungeonGenerator.setRoomGenerationAttempts(500);
        dungeonGenerator.setMaxRoomSize(75);
        dungeonGenerator.setTolerance(10); // Max difference between width and height.
        dungeonGenerator.setMinRoomSize(9);
        dungeonGenerator.generate(grid);

        final Color color = new Color();
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                final float cell = 1f - grid.get(x, y);
                color.set(cell, cell, cell, 1f);
                map.drawPixel(x, y, Color.rgba8888(color));
            }
        }

        mapTexture = new Texture(map);
        map.dispose();
    }


    @Override
    public void hide() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.0340f, 0.680f, 0.163f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.position.set(player.x, player.y, 0);
        camera.update();


        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        spriteBatch.draw(mapTexture, EclipseThorne.WORLD_WIDTH / 2, EclipseThorne.WORLD_HEIGHT / 2);

        //create enemies
        enemyTimer -= delta;
        if (enemyTimer < 0) {
            enemyTimer = ENEMY_DELAY;


            vector2.set(camera.viewportWidth / 2, camera.viewportHeight / 2);
            vector2.rotateDeg(MathUtils.random(360f));
            vector2.add(camera.position.x, camera.position.y);
            new Enemy(EnemyType.values()[MathUtils.random(0, 8)], vector2.x, vector2.y);
        }


        //call logic on all entities
        Object[] ents = entities.begin();
        for (int i = 0, n = entities.size; i < n; i++) {
            Entity entity = (Entity) ents[i];
            entity.act(delta);
        }
        entities.end();

        //draw all entities
        for (Entity entity : entities) {
            entity.draw(spriteBatch);
            //System.out.println(entity.x + " | " + entity.y);
        }

        //draw debug
        for (Entity entity : entities) {
            Item item = entity.item;
            if (item != null) {
                shapeDrawer.setColor(Color.RED);
                shapeDrawer.setDefaultLineWidth(0.1f);
                Rect rect = world.getRect(item);
                shapeDrawer.rectangle(rect.x, rect.y, rect.w, rect.h);
            }
        }


        spriteBatch.end();

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void dispose() {
        textureAtlas.dispose();
    }
}
