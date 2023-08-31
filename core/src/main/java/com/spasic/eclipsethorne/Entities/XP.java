package com.spasic.eclipsethorne.Entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dongbat.jbump.Item;
import lombok.Getter;
import lombok.Setter;

import static com.spasic.eclipsethorne.Screens.GameSreen.*;

@Getter
@Setter
public class XP extends Entity{

    public static final Animation<TextureRegion> xpAnimation = new Animation<TextureRegion>(1, textureAtlas.findRegions("XP"), Animation.PlayMode.LOOP);

    public float xpValue;

    public XP(float x, float y, float xpModifier){
        this.x = x;
        this.y = y;
        TextureRegion temp = xpAnimation.getKeyFrame(0, true);
        bboxX = 0;
        bboxY = 0;
        bboxWidth = (float) temp.getRegionWidth() / temp.getRegionWidth() / 3.0f;
        bboxHeight = (float) temp.getRegionHeight() / temp.getRegionHeight() / 3.0f;

        xpValue = (1 * xpModifier);
        xpValue += 0.4f * Level;

        item = new Item<>(this);
        entities.add(this);
        world.add(item, this.x, this.y, bboxWidth, bboxHeight);

        animation = xpAnimation;
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        if (animation != null) {
            TextureRegion currentFrame = animation.getKeyFrame(animationTime, true);
            spriteBatch.setColor(color);
            spriteBatch.draw(currentFrame, x + bboxX, y + bboxY,
                ((float) currentFrame.getRegionWidth() / currentFrame.getRegionWidth() / 3) * SCALE,
                ((float) currentFrame.getRegionHeight() / currentFrame.getRegionHeight() / 3) * SCALE,
                (float) currentFrame.getRegionWidth() / currentFrame.getRegionWidth() / 3 * SCALE,
                (float) currentFrame.getRegionHeight() / currentFrame.getRegionHeight() / 3 * SCALE,
                flipX ? -1 : 1, flipY ? -1 : 1, angle);
            spriteBatch.setColor(Color.WHITE);
        }
    }

    public void die(){
        entities.removeValue(this, true);
        world.remove(item);
    }

    @Override
    public void act(float delta) {

    }
}
