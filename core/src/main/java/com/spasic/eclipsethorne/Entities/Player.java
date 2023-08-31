package com.spasic.eclipsethorne.Entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.dongbat.jbump.*;
import com.spasic.eclipsethorne.Screens.GameSreen;
import lombok.Getter;
import lombok.Setter;

import static com.spasic.eclipsethorne.Screens.GameSreen.*;


@Getter
@Setter
public class Player extends Entity{

    public static PlayerCollisionFilter PLAYER_COLLISION_FILTER = new PlayerCollisionFilter();
    public static final float CAST_DELAY = 1.0f;
    public static final float INVULNERABILITY_TIME = 1.5f;

    public static final Animation<TextureRegion> walkingAnimation = new Animation<TextureRegion>(1/16f, textureAtlas.findRegions("player-move"), Animation.PlayMode.LOOP);
    public static final Animation<TextureRegion> dyingAnimation = new Animation<TextureRegion>(1/16f, textureAtlas.findRegions("player-die"), Animation.PlayMode.LOOP);
    public static final Animation<TextureRegion> castingAnimation = new Animation<TextureRegion>(1/8f, textureAtlas.findRegions("player-cast"), Animation.PlayMode.LOOP);
    public static final Animation<TextureRegion> idleAnimation = new Animation<TextureRegion>(1, textureAtlas.findRegions("player-idle"), Animation.PlayMode.LOOP);

    public boolean isDead = false;
    public float castTimer;
    public float invulnerabilityTimer;
    public int maxHP;

    // LVL
    public float currentXP = 0;
    public float nextLevelXP = 10;
    public int Level = 0;


    public Player(float x, float y){
        this.movementSpeed = 7.5f;
        castTimer = CAST_DELAY;
        this.AP = 10;
        maxHP = 100;
        HP = maxHP;

        TextureRegion temp = idleAnimation.getKeyFrame(0, true);
        this.x = x;
        this.y = y;
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

    public Player(float x, float y, Player player){
        this.movementSpeed = player.movementSpeed;
        this.AP = player.AP;
        castTimer = CAST_DELAY;
        maxHP = player.maxHP;
        HP = maxHP;
        this.currentXP = player.currentXP;

        TextureRegion temp = idleAnimation.getKeyFrame(0, true);
        this.x = x;
        this.y = y;
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
            if(deltaX != 0 && deltaY != 0){
                x += deltaX;
                y += deltaY;
            }
            deltaX = 0;
            deltaY = 0;
            animationTime+=delta;

            // lvl up
            if(currentXP >= nextLevelXP){
                Level++;
                currentXP -= nextLevelXP;
                nextLevelXP *= 1.5f;
                HP = maxHP;
                LEVEL_UP = true;
            }



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
            direction = new Vector2(MathUtils.cosDeg(angle), MathUtils.sinDeg(angle));

            // Make player face the cursor
            Vector3 mousePosition = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            GameSreen.camera.unproject(mousePosition);

            Vector2 spritePosition = new Vector2(x, y);

            angle = MathUtils.atan2(mousePosition.y - spritePosition.y, mousePosition.x - spritePosition.x);

            angle *= MathUtils.radiansToDegrees;

            if (castTimer > 0) {
                castTimer -= delta;
                if (castTimer < 0) castTimer = 0;
            }

            if(invulnerabilityTimer > 0){
                invulnerabilityTimer -= delta;
                if(invulnerabilityTimer < 0) invulnerabilityTimer = 0;
            }

            if(invulnerabilityTimer <= 0 && color != Color.WHITE){
                color = Color.WHITE;
            }

            if (leftClick && castTimer == 0) {
                castTimer = CAST_DELAY;
                //arrowSound.play();
                MagicBolt magicBolt = new MagicBolt(angle, (player.x), (player.y));
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
                        if(invulnerabilityTimer <= 0 && color == Color.WHITE){
                            HP -= enemy.AP;
                            if(!hurtSound.isPlaying()){
                                GameSreen.hurtSound.play();
                            }
                            invulnerabilityTimer = INVULNERABILITY_TIME;
                            color = Color.GOLD;
                        }

                        deltaX = movementSpeed * 100 * enemy.facing.x < 0 ? -1 : 1 * enemy.direction.x;
                        deltaY = movementSpeed * 100 * enemy.facing.y < 0 ? -1 : 1 * enemy.direction.y;

                        if(HP <= 0){
                            die();
                        }
                    }


                }
                else if(collision.other.userData instanceof XP){
                    XP xp = (XP) collision.other.userData;
                    player.currentXP += xp.xpValue;
                    xp.die();
                    xpPickUp.play();

                }
                else if(collision.other.userData instanceof Portal){
                    nextLevel = true;
                    GameSreen.Level++;
                    teleport.play();
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

    @Override
    public void draw(SpriteBatch spriteBatch) {
        super.draw(spriteBatch);
        // Draw the background of the health bar
        shapeDrawer.setColor(Color.GRAY);
        shapeDrawer.filledRectangle(player.x, player.y - 0.25f, 1, 0.1f);
        // Calculate the width of the health bar based on the current health value
        float healthBarWidth;
        if(HP == 0) healthBarWidth = 0;
        else healthBarWidth = HP / maxHP;

        // Draw the actual health bar
        shapeDrawer.setColor(Color.RED);
        shapeDrawer.filledRectangle(player.x, player.y - 0.25f, healthBarWidth, 0.1f);

    }

    public void die() {
        isDead = true;
        GameSreen.gameOver = true;
        animation = dyingAnimation;
        animationTime = 0; // Reset animation time to start the dying animation
    }

    public void setPosition(float x, float y){
        this.x = x;
        this.y = y;
    }

    public static class PlayerCollisionFilter implements CollisionFilter {
        @Override
        public Response filter(Item item, Item other) {
            if (other.userData instanceof Enemy || other.userData instanceof DoorBlock
                || other.userData instanceof Portal || other.userData instanceof  XP) return Response.cross;
            else if(other.userData instanceof BasicBlock) return Response.slide;
            else return null;
        }
    }
}
