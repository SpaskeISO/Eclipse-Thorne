package com.spasic.eclipsethorne;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.spasic.eclipsethorne.Entities.Entity;

import static com.spasic.eclipsethorne.Screens.GameSreen.camera;

public class Utils {
    public static float approach(float start, float target, float increment) {
        increment = Math.abs(increment);
        if (start < target) {
            start += increment;

            if (start > target) {
                start = target;
            }
        } else {
            start -= increment;

            if (start < target) {
                start = target;
            }
        }
        return start;
    }

    public static float approach360(float start, float target, float increment) {
        float delta = ((target - start + 360 + 180) % 360) - 180;
        return (start + Math.signum(delta) * MathUtils.clamp(increment, 0.0f, Math.abs(delta)) + 360) % 360;
    }

    public static boolean isValidPos(int i, int j, int n, int m)
    {
        return i >= 0 && j >= 0 && i <= n - 1 && j <= m - 1;
    }


    public static boolean isEntityVisible(Entity entity) {
        // Calculate the bounding rectangle of the entity
        float entityX = entity.getX(); // Adjust with your entity's position
        float entityY = entity.getY(); // Adjust with your entity's position
        float entityWidth = entity.bboxWidth; // Adjust with your entity's width
        float entityHeight = entity.bboxHeight; // Adjust with your entity's height

        // Check if the bounding rectangle is within the camera's frustum
        return camera.frustum.pointInFrustum(entityX, entityY, 0) ||
            camera.frustum.pointInFrustum(entityX + entityWidth, entityY, 0) ||
            camera.frustum.pointInFrustum(entityX, entityY + entityHeight, 0) ||
            camera.frustum.pointInFrustum(entityX + entityWidth, entityY + entityHeight, 0);
    }

    public static boolean isTileVisible(Vector3 bound){
        float halfWorldWidth = EclipseThorne.WORLD_WIDTH / 2;
        float halfWorldHeight = EclipseThorne.WORLD_HEIGHT / 2;

        // Calculate the bounding rectangle of the entity
        float rectangleX = bound.x + halfWorldWidth; // Adjust with your entity's position
        float rectangleY = bound.y + halfWorldHeight; // Adjust with your entity's position
        float rectangleWidth = 1; // Adjust with your entity's width
        float rectangleHeight = 1; // Adjust with your entity's height

        // Check if the bounding rectangle is within the camera's frustum
        return camera.frustum.pointInFrustum(rectangleX, rectangleY, 0) ||
            camera.frustum.pointInFrustum(rectangleX + rectangleWidth, rectangleY, 0) ||
            camera.frustum.pointInFrustum(rectangleX, rectangleY + rectangleHeight, 0) ||
            camera.frustum.pointInFrustum(rectangleX + rectangleWidth, rectangleY + rectangleHeight, 0);
    }





}
