package com.spasic.eclipsethorne.Entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dongbat.jbump.Item;

import static com.spasic.eclipsethorne.GameSreen.entities;
import static com.spasic.eclipsethorne.GameSreen.world;

public class DoorBlock extends Entity{


    public DoorBlock(float x, float y){
        this.x = x;
        this.y = y;

        bboxX = 0;
        bboxY = 0;
        bboxWidth =  1;
        bboxHeight = 1;
        item = new Item<>(this);
        entities.add(this);
        world.add(item, this.x, this.y, bboxWidth, bboxHeight);

    }

    @Override
    public void act(float delta) {

    }
}
