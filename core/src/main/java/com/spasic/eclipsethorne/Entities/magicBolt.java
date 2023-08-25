package com.spasic.eclipsethorne.Entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.dongbat.jbump.*;
import com.spasic.eclipsethorne.GameSreen;

import static com.spasic.eclipsethorne.GameSreen.*;

public class magicBolt extends Entity{

    public static final Animation<TextureRegion> magicBoltAnimation = new Animation<TextureRegion>(1, textureAtlas.findRegions("firearrow-new"), Animation.PlayMode.LOOP);
    public static final magicBoltCollisionFIlter MAGIC_BOLT_COLLISION_FILTER = new magicBoltCollisionFIlter();

    public magicBolt(float angle, float x, float y){
        AP = 1;
        this.angle = angle;
        this.movementSpeed = 20;
        animation = magicBoltAnimation;
        direction = new Vector2(MathUtils.cosDeg(angle), MathUtils.sinDeg(angle));

        this.x = x + (1.5f * direction.x);
        this.y = y + (1.5f * direction.y);
        bboxX = 0;
        bboxY = 0f;
        bboxWidth = (float) magicBoltAnimation.getKeyFrames()[0].getRegionWidth() / magicBoltAnimation.getKeyFrames()[0].getRegionWidth();
        bboxHeight = (float) magicBoltAnimation.getKeyFrames()[0].getRegionHeight() / magicBoltAnimation.getKeyFrames()[0].getRegionWidth();

        item = new Item<>(this);
    }
    @Override
    public void act(float delta) {
        //update physics
        x += movementSpeed * direction.x * delta;
        y += movementSpeed * direction.y * delta;

        //handle collisions
        Response.Result result = world.move(item, x, y, MAGIC_BOLT_COLLISION_FILTER);
        for (int i = 0; i < result.projectedCollisions.size(); i++) {
            Collision collision = result.projectedCollisions.get(i);
            if (collision.other.userData instanceof Enemy) {
                //ran into enemy: kill magicBolt
                entities.removeValue(this, true);
                if (item != null) {
                    world.remove(item);
                    item = null;
                }

                Enemy enemy = (Enemy) collision.other.userData;
                if (!enemy.isDying()) {
                    //enemy is not dead yet: kill it
                    enemy.die();
                } else {
                    //push the enemy
                    enemy.x += enemy.movementSpeed / 4 * direction.x;
                    enemy.y += enemy.movementSpeed / 4 * direction.y;
                }
            }
        }

        //update position based on collisions
        Rect rect = world.getRect(item);
        if (rect != null) {
            x = rect.x;
            y = rect.y;
        }

        //if outside view
        if (x < camera.position.x - camera.viewportWidth / 2 || x > camera.position.x + camera.viewportWidth / 2 ||
            y < camera.position.y - camera.viewportHeight / 2 || y > camera.position.y + camera.viewportHeight / 2) {
            //destroy the entity
            entities.removeValue(this, true);
            if (item != null) {
                world.remove(item);
                item = null;
            }
        }
    }


    public static class magicBoltCollisionFIlter implements CollisionFilter{
        @Override
        public Response filter(Item item, Item other) {
            if (other.userData instanceof Enemy) return Response.cross;
            else return null;
        }
    }
}
