package com.spasic.eclipsethorne.Entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dongbat.jbump.Item;
import lombok.Getter;
import lombok.Setter;

import static com.spasic.eclipsethorne.GameSreen.textureAtlas;

@Getter
@Setter
public class BasicBlock extends Entity{


    public static final Animation<TextureRegion> wall1Animation = new Animation<TextureRegion>(1, textureAtlas.findRegions("wall1"), Animation.PlayMode.LOOP);


    public BasicBlock(){

        bboxX = 0;
        bboxY = 0;
        bboxWidth =  (float) (animation.getKeyFrames()[0].getRegionWidth() / animation.getKeyFrames()[0].getRegionWidth());
        bboxHeight = (float) (animation.getKeyFrames()[0].getRegionHeight() / animation.getKeyFrames()[0].getRegionHeight());
        item = new Item<>(this);
    }

    @Override
    public void act(float delta) {

    }
}
