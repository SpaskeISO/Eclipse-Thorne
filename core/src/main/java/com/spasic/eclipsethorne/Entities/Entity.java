package com.spasic.eclipsethorne.Entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.dongbat.jbump.Item;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Entity {



    public Animation<TextureRegion> animation;
    public float animationTime;
    public float SCALE = 1.0f;

    public float x;
    public float y;
    public float bboxX;
    public float bboxY;
    public float bboxWidth;
    public float bboxHeight;
    public float movementSpeed;
    public float angle = 0;
    public float deltaX;
    public float deltaY;
    public boolean flipX = false;
    public boolean flipY = false;
    public Vector2 direction;

    public float HP; //Health Points
    public float AP; //Attack Power


    public Item<Entity> item;
    public Color color = Color.WHITE;



    public void draw(SpriteBatch spriteBatch){
        if (animation != null) {
            TextureRegion currentFrame = animation.getKeyFrame(animationTime, true);
            spriteBatch.setColor(color);
            spriteBatch.draw(currentFrame, x, y,
            ((float) currentFrame.getRegionWidth() / currentFrame.getRegionWidth() / 2) * SCALE,
            ((float) currentFrame.getRegionHeight() / currentFrame.getRegionHeight() / 2) * SCALE,
            (float) currentFrame.getRegionWidth() / currentFrame.getRegionWidth() * SCALE,
            (float) currentFrame.getRegionHeight() / currentFrame.getRegionHeight() * SCALE,
                flipX ? -1 : 1, flipY ? -1 : 1, angle - 90);
            spriteBatch.setColor(Color.WHITE);
        }
    }

    public abstract void act(float delta);



}
