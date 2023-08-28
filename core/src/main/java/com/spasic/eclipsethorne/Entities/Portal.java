package com.spasic.eclipsethorne.Entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dongbat.jbump.Item;
import com.spasic.eclipsethorne.EclipseThorne;

import static com.spasic.eclipsethorne.Screens.GameSreen.*;

public class Portal extends Entity{

    public static Animation<TextureRegion> portalAnimation = new Animation<TextureRegion>(1, textureAtlas.findRegions("portal-1"), Animation.PlayMode.LOOP);

    public  Portal(float x, float y){
        this.x = x + EclipseThorne.WORLD_WIDTH / 2;
        this.y = y + EclipseThorne.WORLD_HEIGHT / 2;
        TextureRegion temp = portalAnimation.getKeyFrame(1, true);
        bboxX = 0;
        bboxY = 0;
        bboxWidth =  (float) (temp.getRegionWidth() / temp.getRegionWidth());
        bboxHeight = (float) (temp.getRegionHeight() / temp.getRegionHeight());

        item = new Item<>(this);
        entities.add(this);
        world.add(item, this.x, this.y, bboxWidth, bboxHeight);

        animation = portalAnimation;
    }

    @Override
    public void act(float delta) {

    }
}
