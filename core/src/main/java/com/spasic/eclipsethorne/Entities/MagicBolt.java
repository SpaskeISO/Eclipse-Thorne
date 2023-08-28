package com.spasic.eclipsethorne.Entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.dongbat.jbump.*;
import lombok.Getter;
import lombok.Setter;

import static com.spasic.eclipsethorne.Screens.GameSreen.*;

@Getter
@Setter
public class MagicBolt extends Entity{

    public static final Animation<TextureRegion> magicBoltAnimation = new Animation<TextureRegion>(1, textureAtlas.findRegions("firearrow-new"), Animation.PlayMode.LOOP);
    public static final magicBoltCollisionFIlter MAGIC_BOLT_COLLISION_FILTER = new magicBoltCollisionFIlter();

    public MagicBolt(float angle, float x, float y){
        AP = 10;
        HP = 1;
        this.angle = angle;
        this.movementSpeed = 12.5f;
        animation = magicBoltAnimation;
        direction = new Vector2(MathUtils.cosDeg(angle), MathUtils.sinDeg(angle));

        TextureRegion temp = animation.getKeyFrame(0, true);
        this.x = x + (1.5f * direction.x);
        this.y = y + (1.5f * direction.y);
        bboxX = (((float) temp.getRegionWidth() / temp.getRegionWidth() - (float) temp.getRegionWidth() / temp.getRegionWidth() / 2.0f * 1.5f)) / 2.0f;;
        bboxY = (((float) temp.getRegionHeight() / temp.getRegionHeight() - (float) temp.getRegionHeight() / temp.getRegionHeight() / 2 * 1.5f)) / 2.0f;
        bboxWidth = (float) temp.getRegionWidth() / temp.getRegionWidth() / 2 * 1.5f;
        bboxHeight = (float) temp.getRegionHeight() / temp.getRegionWidth() / 2 * 1.5f;

        item = new Item<>(this);
    }
    @Override
    public void act(float delta) {
        if(HP == 1){
            //update physics
            x += movementSpeed * direction.x * delta;
            y += movementSpeed * direction.y * delta;

            //handle collisions
            Response.Result result = world.move(item, x, y, MAGIC_BOLT_COLLISION_FILTER);
            for (int i = 0; i < result.projectedCollisions.size(); i++) {
                Collision collision = result.projectedCollisions.get(i);
                if (collision.other.userData instanceof Enemy) {
                    Enemy enemy = (Enemy) collision.other.userData;
                    if (!enemy.isDying()) {
                        enemy.HP -= this.AP;

                        if(enemy.HP < 0) enemy.die();
                    }

                }
                else{
                    if(collision.other.userData instanceof BasicBlock){
                        //ran into wall: kill magicBolt
                        entities.removeValue(this, true);
                        if (this.item != null) {
                            world.remove(this.item);
                            this.item = null;
                        }
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

    }


    public static class magicBoltCollisionFIlter implements CollisionFilter{
        @Override
        public Response filter(Item item, Item other) {
            if (other.userData instanceof Enemy) return Response.cross;
            else if(other.userData instanceof BasicBlock) return Response.cross;
            else return null;
        }
    }
}
