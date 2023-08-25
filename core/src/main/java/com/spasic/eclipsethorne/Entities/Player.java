package com.spasic.eclipsethorne.Entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.dongbat.jbump.*;
import com.spasic.eclipsethorne.EclipseThorne;
import com.spasic.eclipsethorne.GameSreen;
import lombok.Getter;
import lombok.Setter;

import static com.spasic.eclipsethorne.GameSreen.*;


@Getter
@Setter
public class Player extends Entity{

    public static PlayerCollisionFilter PLAYER_COLLISION_FILTER = new PlayerCollisionFilter();
    public static final float CAST_DELAY = 2.0f;
    public float castTimer;
    public static final Animation<TextureRegion> walkingAnimation = new Animation<TextureRegion>(1/16f, textureAtlas.findRegions("player-move"), Animation.PlayMode.LOOP);
    public static final Animation<TextureRegion> dyingAnimation = new Animation<TextureRegion>(1/16f, textureAtlas.findRegions("player-die"), Animation.PlayMode.LOOP);
    public static final Animation<TextureRegion> castingAnimation = new Animation<TextureRegion>(1/8f, textureAtlas.findRegions("player-cast"), Animation.PlayMode.LOOP);
    public static final Animation<TextureRegion> idleAnimation = new Animation<TextureRegion>(1, textureAtlas.findRegions("player-idle"), Animation.PlayMode.LOOP);
    public boolean isDead = false;
    public Player(){
        this.movementSpeed = 7.5f;
        castTimer = CAST_DELAY;
        HP = 10;

        TextureRegion temp = idleAnimation.getKeyFrame(0, true);
        x = EclipseThorne.WORLD_WIDTH / 2.0f;
        y = EclipseThorne.WORLD_HEIGHT / 2.0f;
        bboxX = (((float) temp.getRegionWidth() / temp.getRegionWidth() - (float) temp.getRegionWidth() / temp.getRegionWidth() / 2.0f * 1.5f)) / 2.0f;
        bboxY = (((float) temp.getRegionHeight() / temp.getRegionHeight() - (float) temp.getRegionHeight() / temp.getRegionHeight() / 2 * 1.5f)) / 2.0f;
        bboxWidth = (float) temp.getRegionWidth() / temp.getRegionWidth() / 2 * 1.5f;
        bboxHeight = (float) temp.getRegionHeight() / temp.getRegionHeight() / 2 * 1.5f;



        item = new Item<>(this);
        entities.add(this);
        world.add(item, x + bboxX, y + bboxY, bboxWidth, bboxHeight);

        animationTime = 0;
        animation = idleAnimation;
    }

    public void act(float delta){
        if (!isDead) {
            animationTime+=delta;

            //Movement
            boolean left = Gdx.input.isKeyPressed(Input.Keys.A);
            boolean right = Gdx.input.isKeyPressed(Input.Keys.D);
            boolean up = Gdx.input.isKeyPressed(Input.Keys.W);
            boolean down = Gdx.input.isKeyPressed(Input.Keys.S);
            boolean leftClick = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
            if((left || right || up || down) && animation.isAnimationFinished(animationTime)){
                animation = walkingAnimation;
            }
            if(left){
                x -= movementSpeed * delta;

            }
            if(right){
                x += movementSpeed * delta;
            }
            if(up){
                y += movementSpeed * delta;
            }
            if(down){
                y -= movementSpeed * delta;
            }


            if(leftClick){
                Vector3 mousePosition = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                GameSreen.camera.unproject(mousePosition);

                Vector2 spritePosition = new Vector2(x, y);

                angle = MathUtils.atan2(mousePosition.y - spritePosition.y, mousePosition.x - spritePosition.x);

                angle *= MathUtils.radiansToDegrees;


            }

            if (castTimer > 0) {
                castTimer -= delta;
                if (castTimer < 0) castTimer = 0;
            }

            if (leftClick && castTimer == 0) {
                castTimer = CAST_DELAY;
                //arrowSound.play();
                magicBolt magicBolt = new magicBolt(angle, (player.x), (player.y));
                magicBolt.x = ((player.x + bboxX + (player.bboxWidth / 2.0f) * magicBolt.direction.x));
                magicBolt.y = ((player.y + bboxY + (player.bboxHeight / 2.0f * magicBolt.direction.y)));
                entities.add(magicBolt);
                world.add(magicBolt.item, magicBolt.x + magicBolt.bboxX,
                    magicBolt.y + magicBolt.bboxY,
                    magicBolt.bboxWidth, magicBolt.bboxHeight);

                animation = castingAnimation;
                animationTime = 0;
            }


            //handle collisions
            Response.Result result = world.move(item, x + bboxX, y + bboxY,
                PLAYER_COLLISION_FILTER);
            for (int i = 0; i < result.projectedCollisions.size(); i++) {
                Collision collision = result.projectedCollisions.get(i);
                if (collision.other.userData instanceof Enemy) {
                    Enemy enemy = (Enemy) collision.other.userData;
                    if (!enemy.isDying()) {
                        //ran into enemy: kill the player
                        die();
//                    entities.removeValue(this, true);
//                    if (item != null) {
//                        world.remove(item);
//                        item = null;
//                    }
                    }
                }
            }

            //update position based on collisions
            Rect rect = world.getRect(item);
            if (rect != null) {
                x = rect.x - bboxX;
                y = rect.y - bboxY;
            }

            if(((animation == castingAnimation && animationTime >= castingAnimation.getAnimationDuration())
                || (animation == walkingAnimation && animationTime >= walkingAnimation.getAnimationDuration()
                && !(left || right || up || down))) && !isDead){
                animation = idleAnimation;
            }
        }
        else {
            // Play the dying animation
            animation = dyingAnimation;
            animationTime += delta;

            // If the dying animation has finished, remove the player and perform cleanup
            if (animation.isAnimationFinished(animationTime)) {
                entities.removeValue(this, true);
                if (item != null) {
                    world.remove(item);
                    item = null;
                }
            }

        }



    }

    public void die() {
        isDead = true;
        animation = dyingAnimation;
        animationTime = 0; // Reset animation time to start the dying animation
    }

    public static class PlayerCollisionFilter implements CollisionFilter {
        @Override
        public Response filter(Item item, Item other) {
            if (other.userData instanceof Enemy) return Response.bounce;
            else return null;
        }
    }
}
