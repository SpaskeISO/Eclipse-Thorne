package com.spasic.eclipsethorne.Entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dongbat.jbump.Item;
import lombok.Getter;
import lombok.Setter;

import static com.spasic.eclipsethorne.GameSreen.*;

@Getter
@Setter
public class BasicBlock extends Entity{


    public static final Animation<TextureRegion> wall1Animation = new Animation<TextureRegion>(1, textureAtlas.findRegions("wall-1"), Animation.PlayMode.LOOP);


    public BasicBlock(float x, float y){
        this.x = x;
        this.y = y;
        TextureRegion temp = wall1Animation.getKeyFrame(0, true);
        bboxX = 0;
        bboxY = 0;
        bboxWidth =  (float) (temp.getRegionWidth() / temp.getRegionWidth());
        bboxHeight = (float) (temp.getRegionHeight() / temp.getRegionHeight());
        item = new Item<>(this);
        entities.add(this);
        world.add(item, this.x, this.y, bboxWidth, bboxHeight);

        animation = wall1Animation;
    }

    @Override
    public void act(float delta) {

    }
}
