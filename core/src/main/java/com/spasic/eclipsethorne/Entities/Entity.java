package com.spasic.eclipsethorne.Entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.dongbat.jbump.Item;
import com.spasic.eclipsethorne.EclipseThorne;
import com.spasic.eclipsethorne.GameSreen;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Entity {

    public float movementSpeed;

    public Animation<TextureRegion> animation;
    public float animationTime;

    public float x;
    public float y;
    public float bboxX;
    public float bboxY;
    public float bboxWidth;
    public float bboxHeight;
    public float angle = 0;
    public boolean flipX = false;
    public boolean flipY = false;
    public float deltaX;
    public float deltaY;
    public Vector2 direction;

    public int HP; //Health Points
    public int AP; //Attack Power


    public Item<Entity> item;
    public Color color = Color.WHITE;



    public void draw(SpriteBatch spriteBatch){
        if (animation != null) {
            TextureRegion currentFrame = animation.getKeyFrame(animationTime, true);
            spriteBatch.setColor(color);
            spriteBatch.draw(currentFrame, x, y, (float) currentFrame.getRegionWidth() / currentFrame.getRegionWidth() / 2, (float) currentFrame.getRegionHeight() / currentFrame.getRegionHeight() / 2, (float) currentFrame.getRegionWidth() / currentFrame.getRegionWidth(), (float) currentFrame.getRegionHeight() / currentFrame.getRegionHeight(), flipX ? -1 : 1, flipY ? -1 : 1, angle - 90);
            spriteBatch.setColor(Color.WHITE);
        }
    }

    public abstract void act(float delta);



}
