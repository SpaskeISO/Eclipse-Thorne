package com.spasic.eclipsethorne.Entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.dongbat.jbump.*;
import com.spasic.eclipsethorne.Utils;

import static com.spasic.eclipsethorne.Screens.GameSreen.*;

public class Enemy extends Entity{

    public static final Animation<TextureRegion> fying_deamonAnimation =  new Animation<TextureRegion>(1, textureAtlas.findRegions("fying_deamon"), Animation.PlayMode.LOOP);
    public static final Animation<TextureRegion> giant_monsterAnimation =  new Animation<TextureRegion>(1, textureAtlas.findRegions("giant_monster"), Animation.PlayMode.LOOP);
    public static final Animation<TextureRegion> gray_goblinAnimation =  new Animation<TextureRegion>(1, textureAtlas.findRegions("gray_goblin"), Animation.PlayMode.LOOP);
    public static final Animation<TextureRegion> lizardAnimation =  new Animation<TextureRegion>(1, textureAtlas.findRegions("lizard"), Animation.PlayMode.LOOP);
    public static final Animation<TextureRegion> mushroomAnimation =  new Animation<TextureRegion>(1, textureAtlas.findRegions("mushroom"), Animation.PlayMode.LOOP);
    public static final Animation<TextureRegion> octoAnimation =  new Animation<TextureRegion>(1, textureAtlas.findRegions("octo"), Animation.PlayMode.LOOP);
    public static final Animation<TextureRegion> bird_manAnimation =  new Animation<TextureRegion>(1, textureAtlas.findRegions("bird_man"), Animation.PlayMode.LOOP);
    public static final Animation<TextureRegion> bird_warriorAnimation =  new Animation<TextureRegion>(1, textureAtlas.findRegions("bird_warrior"), Animation.PlayMode.LOOP);
    public static final Animation<TextureRegion> eye_flayerAnimation =  new Animation<TextureRegion>(1, textureAtlas.findRegions("eye_flayer"), Animation.PlayMode.LOOP);
    public static final EnemyCollisionFilter ENEMY_COLLISION_FILTER = new EnemyCollisionFilter();
    public static final float DEATH_TIME = 1.0f;
    public static final Vector2 vector2 = new Vector2();
    public float rotation;
    public boolean boss;
    public boolean dying = false;
    public float deathTimer;
    public Vector2 facing;
    public EnemyType enemyType;
    public float bounceX, bounceY;

    public Enemy(EnemyType enemyType, float x, float y){
        this.enemyType = enemyType;
        switch (this.enemyType){
            case OCTO: animation = octoAnimation;
                HP = 10;
                AP = 5;
                break;
            case LIZARD: animation = lizardAnimation;
                HP = 20;
                AP = 5;
                break;
            case BIRD_MAN: animation = bird_manAnimation;
                HP = 10;
                AP = 10;
                break;
            case MUSHROOM: animation = mushroomAnimation;
                HP = 10;
                AP = 5;
                break;
            case EYE_FLAYER: animation = eye_flayerAnimation;
                HP = 10;
                AP = 10;
                break;
            case GRAY_GOBLIN: animation = gray_goblinAnimation;
                HP = 20;
                AP = 5;
                break;
            case BIRD_WARRIOR: animation = bird_warriorAnimation;
                HP = 20;
                AP = 15;
                break;
            case FYING_DEAMON: animation = fying_deamonAnimation;
                HP = 20;
                AP = 10;
                break;
            case GIANT_MONSTER: animation = giant_monsterAnimation;
                HP = 30;
                AP = 20;
                SCALE = 1.5f;
                break;
        }
        this.x = x;
        this.y = y;
        bboxX = 0f;
        bboxY = 0f;
        this.movementSpeed = 5;
        this.boss = 0.05f >= MathUtils.random(1.0f);
        if(boss){
            color = Color.RED;
            SCALE = 2.0f;
            HP *= 3;
        }
        bboxWidth = (float) (animation.getKeyFrames()[0].getRegionWidth() / animation.getKeyFrames()[0].getRegionWidth()) * SCALE;
        bboxHeight = (float) (animation.getKeyFrames()[0].getRegionHeight() / animation.getKeyFrames()[0].getRegionHeight()) * SCALE;

        // Level Scaling;
        this.HP += 0.4f * GameLevel;
        this.AP += 0.2f * GameLevel;
        this.movementSpeed += 0.02f * GameLevel;

        this.angle = 90;
        this.direction = new Vector2();
        this.facing = new Vector2();

        bounceX = 0;
        bounceY = 0;



        item = new Item<>(this);

        entities.add(this);
        world.add(item, x, y, bboxWidth, bboxHeight);
    }


    @Override
    public void act(float delta) {
        if(bounceX != 0 && bounceY != 0){
            x += bounceX;
            y += bounceY;
        }
        bounceX = 0;
        bounceY = 0;

        if (deathTimer <= 0) {
            //update animation frame
            animationTime += delta;

            //face player
            vector2.set(player.x, player.y);
            vector2.sub(x, y);
            rotation = vector2.angleDeg();

            //move towards player
            vector2.set(movementSpeed, 0);
            vector2.rotateDeg(rotation);
            facing.set(vector2);
            deltaX = vector2.x;
            deltaY = vector2.y;
        } else {
            vector2.set(deltaX, deltaY);
            vector2.setLength(Utils.approach(vector2.len(), 0, 10));
            deltaX = vector2.x;
            deltaY = vector2.y;
        }

        x += delta * deltaX;
        y += delta * deltaY;

        direction = new Vector2(MathUtils.cosDeg(player.angle), MathUtils.sinDeg(player.angle));

        //handle collisions
        Response.Result result = world.move(item, x + bboxX, y + bboxY, ENEMY_COLLISION_FILTER);
        for (int i = 0; i < result.projectedCollisions.size(); i++) {
            Collision collision = result.projectedCollisions.get(i);
            Entity other = (Entity) collision.other.userData;
            if (other instanceof Enemy) {
                Enemy enemy = (Enemy) other;
                if (enemy.isDying()) {

                }
            }
            else if (other instanceof Player){

                Vector2 playerPosition = new Vector2(player.x, player.y);
                Vector2 enemyPosition = new Vector2(x, y);
                float temp = 0;
                temp = MathUtils.atan2(playerPosition.y - enemyPosition.y, playerPosition.x - enemyPosition.x);

                temp *= MathUtils.radiansToDegrees;

                bounceX = movementSpeed / 2 * MathUtils.cosDeg(temp + 180);
                bounceY = movementSpeed / 2 * MathUtils.sinDeg(temp + 180);
            }
            else if(other instanceof MagicBolt){
                MagicBolt magicBolt = (MagicBolt) other;
                magicBolt.HP = 0;
                Vector2 magicBoltPosition = new Vector2(magicBolt.x, magicBolt.y);
                Vector2 enemyPosition = new Vector2(x, y);
                float temp = 0;
                temp = MathUtils.atan2(magicBoltPosition.y - enemyPosition.y, magicBoltPosition.x - enemyPosition.x);

                temp *= MathUtils.radiansToDegrees;

                bounceX = movementSpeed / 4 * MathUtils.cosDeg(temp + 180);
                bounceY = movementSpeed / 4 * MathUtils.sinDeg(temp + 180);

                //ran into enemy: kill magicBolt
                entities.removeValue(magicBolt, true);
                fireballHit.play(0.2f);
                if (magicBolt.item != null) {
                    world.remove(magicBolt.item);
                    magicBolt.item = null;
                }
            }
        }

        //update position based on collisions
        Rect rect = world.getRect(item);
        x = rect.x - bboxX;
        y = rect.y - bboxY;


        //handle death
        if (deathTimer > 0) {
            deathTimer -= delta;
            if (deathTimer <= 0) {
                new XP(this.x, this.y, this.SCALE);
                entities.removeValue(this, true);
                world.remove(item);
            }
        }
        flipX = facing.x < 0;


    }

    public boolean isDying() {
        return deathTimer > 0;
    }

    public void die() {
        dying = true;
        color = Color.DARK_GRAY;
        deathTimer = DEATH_TIME;
    }


    public static class EnemyCollisionFilter implements CollisionFilter {
        @Override
        public Response filter(Item item, Item other) {
            if (other.userData instanceof  Enemy || other.userData instanceof BasicBlock || other.userData instanceof DoorBlock) return Response.slide;
            else if(other.userData instanceof  Player || other.userData instanceof MagicBolt || other.userData instanceof  Portal) return Response.cross;
            else return null;
        }
    }
}
