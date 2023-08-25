package com.spasic.eclipsethorne;

import com.badlogic.gdx.math.MathUtils;

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
}
