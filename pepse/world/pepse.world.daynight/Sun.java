package pepse.world.pepse.world.daynight;

import danogl.GameObject;
import danogl.components.Transition;
import danogl.components.Transition.TransitionType;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.Color;

/**
 * Section 4: Sun
 *
 * Creates a yellow‐colored circular “sun” that orbits around a fixed center
 * in a perfect circle. One full 360° orbit takes exactly `cycleLength` seconds,
 * and the motion is driven by a Danogl Transition with a LINEAR interpolator.
 * @author Roni
 */
public class Sun {

    /** Desired radius of the sun orbit. */
    private static final float ORBIT_RADIUS = 50f;

    /** A full round which is 360 degrees. */
    private static final float FULL_ROUND = 360f;

    /** The initialization point of the round, at 0 degrees. */
    private static final float INIT_PT = 360f;

    /** Sun Tag. */
    private static final String SUN = "sun";

    /**
     * Creates and returns a GameObject representing the sun.
     *
     * @param windowDimensions The width & height of the window (used to compute orbit center).
     * @param cycleLength How many seconds one full 360° orbit should take.
     * @return A GameObject (circle) tagged "sun" that continuously orbits.
	 *
     */
    public static GameObject create(Vector2 windowDimensions, float cycleLength) {

        // 1) Computing “middle of the sky” as the orbit center:
        Vector2 cycleCenter = new Vector2(
                windowDimensions.x() / 2f,
                windowDimensions.y() / 2f
        );

        // 2) Decide on a radius for the sun (its drawn size):
        float sunRadius = ORBIT_RADIUS;

        // 3) Compute the sun’s initial center so that it sits to the right of cycle center
        float orbitRadius = (windowDimensions.y() / 2f) - sunRadius;
        Vector2 initialSunCenter = new Vector2(cycleCenter.x() + orbitRadius, cycleCenter.y()
        );

        // 4) Build an OvalRenderable that paints a solid yellow circle:
        OvalRenderable renderable = new OvalRenderable(Color.YELLOW);

        // 5) Create the GameObject at that initial center, with diameter = sunRadius*2:
        GameObject sun = new GameObject(initialSunCenter, new Vector2(sunRadius * 2f, sunRadius * 2f)
                , renderable
        );

        // 6) Put the sun in CAMERA space so it is always drawn in screen coordinates:
        sun.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);

        // 7) Tag it "sun":
        sun.setTag(SUN);

        new Transition<>(sun, (Float angleInDegrees) -> {
            Vector2 offset = initialSunCenter.subtract(cycleCenter);
            Vector2 rotated = offset.rotated(angleInDegrees);
            sun.setCenter(cycleCenter.add(rotated));
        },
                /* initialValue = */ INIT_PT, // start at 0°
                /* finalValue = */ FULL_ROUND, // end at 360°
                /* interpolator = */ Transition.LINEAR_INTERPOLATOR_FLOAT,
                /* transitionTime = */ cycleLength,
                /* transitionType = */ TransitionType.TRANSITION_LOOP,
                /* onReachingFinalValue = */ null // no callback
        );

        // 8) Return the fully configured Sun GameObject
        return sun;
    }
}

