package com.spasic.eclipsethorne.Entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.dongbat.jbump.*;
import com.spasic.eclipsethorne.Utils;

import static com.spasic.eclipsethorne.GameSreen.*;

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
    public float deathTimer;
    public Vector2 facing;
    public EnemyType enemyType;

    public Enemy(EnemyType enemyType, float x, float y){
        HP = 1;
        AP = 1;
        this.enemyType = enemyType;
        switch (this.enemyType){
            case OCTO: animation = octoAnimation;
                break;
            case LIZARD: animation = lizardAnimation;
                break;
            case BIRD_MAN: animation = bird_manAnimation;
                break;
            case MUSHROOM: animation = mushroomAnimation;
                break;
            case EYE_FLAYER: animation = eye_flayerAnimation;
                break;
            case GRAY_GOBLIN: animation = gray_goblinAnimation;
                break;
            case BIRD_WARRIOR: animation = bird_warriorAnimation;
                break;
            case FYING_DEAMON: animation = fying_deamonAnimation;
                break;
            case GIANT_MONSTER: animation = giant_monsterAnimation;
        }
        this.x = x;
        this.y = y;
        bboxX = 0f;
        bboxY = 0f;
        this.movementSpeed = 5;
        this.boss = 0.1 >= MathUtils.random(1.0f);
        if(boss){
            color = Color.RED;
            bboxWidth = (float) (animation.getKeyFrames()[0].getRegionWidth() / animation.getKeyFrames()[0].getRegionWidth()) * 2;
            bboxHeight = (float) (animation.getKeyFrames()[0].getRegionHeight() / animation.getKeyFrames()[0].getRegionHeight()) * 2;
        }
        else{
            bboxWidth = (float) (animation.getKeyFrames()[0].getRegionWidth() / animation.getKeyFrames()[0].getRegionWidth());
            bboxHeight = (float) (animation.getKeyFrames()[0].getRegionHeight() / animation.getKeyFrames()[0].getRegionHeight());
        }
        this.angle = 90;
        this.direction = new Vector2();
        this.facing = new Vector2();






        item = new Item<>(this);

        entities.add(this);
        world.add(item, x, y, bboxWidth, bboxHeight);
    }


    @Override
    public void act(float delta) {

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
        }

        //update position based on collisions
        Rect rect = world.getRect(item);
        x = rect.x - bboxX;
        y = rect.y - bboxY;

        //handle death
        if (deathTimer > 0) {
            deathTimer -= delta;
            if (deathTimer <= 0) {
                entities.removeValue(this, true);
                world.remove(item);
            }
        }

        //handle death
        if (deathTimer > 0) {
            deathTimer -= delta;
            if (deathTimer <= 0) {
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
        color = Color.DARK_GRAY;
        deathTimer = DEATH_TIME;
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        if (animation != null) {
            TextureRegion currentFrame = animation.getKeyFrame(animationTime, true);
            spriteBatch.setColor(color);
            if(!boss){
                spriteBatch.draw(currentFrame, x, y, (float) currentFrame.getRegionWidth() / currentFrame.getRegionWidth() / 2, (float) currentFrame.getRegionHeight() / currentFrame.getRegionHeight() / 2, (float) currentFrame.getRegionWidth() / currentFrame.getRegionWidth(), (float) currentFrame.getRegionHeight() / currentFrame.getRegionHeight(), flipX ? -1 : 1, flipY ? -1 : 1, angle - 90);

            }
            else{
                spriteBatch.draw(currentFrame, x, y, (float) currentFrame.getRegionWidth() / currentFrame.getRegionWidth(), (float) currentFrame.getRegionHeight() / currentFrame.getRegionHeight(), (float) currentFrame.getRegionWidth() / currentFrame.getRegionWidth() * 2.0f, (float) currentFrame.getRegionHeight() / currentFrame.getRegionHeight() * 2.0f, flipX ? -1 : 1, flipY ? -1 : 1, angle - 90);

            }
            spriteBatch.setColor(Color.WHITE);
        }
    }

    public static class EnemyCollisionFilter implements CollisionFilter {
        @Override
        public Response filter(Item item, Item other) {
            if (other.userData instanceof  Enemy || other.userData instanceof BasicBlock) return Response.slide;
            else if(other.userData instanceof  Player) return Response.bounce;
            else return null;
        }
    }
}
