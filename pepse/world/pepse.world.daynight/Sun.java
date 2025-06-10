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

    /**
     * Creates and returns a GameObject representing the sun.
     *
     * @param windowDimensions The width & height of the window (used to compute orbit center).
     * @param cycleLength      How many seconds one full 360° orbit should take.
     * @return A GameObject (circle) tagged "sun" that continuously orbits.
	 *
     */
    public static GameObject create(Vector2 windowDimensions, float cycleLength) {
        // ────────────────────────────────────────────────────────────────────
        // 4.2: Build a static, yellow circle (OvalRenderable) at “middle of the sky”:
        //       - “Middle of the sky” means centerX = windowWidth/2,
        //         and centerY = windowHeight/2 (halfway down the screen).
        //       - Use OvalRenderable(Color.YELLOW) for a simple yellow circle.
        //       - Set CAMERA coordinates so it always appears on‐screen.
        //       - Tag it "sun" for easy lookup.
        // ────────────────────────────────────────────────────────────────────

        // 1) Compute “middle of the sky” as the orbit center:
        Vector2 cycleCenter = new Vector2(
                windowDimensions.x() / 2f,
                windowDimensions.y() / 2f
        );

        // 2) Decide on a radius for the sun (its drawn size):
        //    Here, we give the sun a radius of 50 pixels.
        float sunRadius = 50f;

        // 3) Compute the sun’s initial center so that it sits to the right of cycle center
        //    by exactly “orbitRadius.” Choose orbitRadius so the circle stays on‐screen.
        //    For example: orbitRadius = (windowHeight / 2) - sunRadius,
        //    which places it near the top edge of the screen at start.
        float orbitRadius = (windowDimensions.y() / 2f) - sunRadius;
        Vector2 initialSunCenter = new Vector2(
                cycleCenter.x() + orbitRadius,  // start directly to the right of center
                cycleCenter.y()                 // same vertical level as cycle center
        );

        // 4) Build an OvalRenderable that paints a solid yellow circle:
        OvalRenderable renderable = new OvalRenderable(Color.YELLOW);

        // 5) Create the GameObject at that initial center, with diameter = sunRadius*2:
        GameObject sun = new GameObject(
                initialSunCenter,
                new Vector2(sunRadius * 2f, sunRadius * 2f),
                renderable
        );

        // 6) Put the sun in CAMERA space so it is always drawn in screen coordinates:
        sun.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);

        // 7) Tag it "sun":
        sun.setTag("sun");

        // ────────────────────────────────────────────────────────────────────
        // 4.4: Attach a Transition so that the sun “rotates” 360° around cycleCenter
        //       in `cycleLength` seconds. When the Transition’s angle value changes,
        //       we recompute the sun’s center by taking the vector (initialSunCenter - cycleCenter),
        //       rotating that vector by the current angle (in degrees), and then re‐adding cycleCenter.
        //
        //       - initialValue =   0f   (start at angle 0°, so sun is at initialSunCenter)
        //       - finalValue   = 360f   (one full revolution)
        //       - interpolator = Interpolator.LINEAR_INTERPOLATOR_FLOAT  (constant speed)
        //       - transitionTime = cycleLength
        //       - transitionType = TransitionType.TRANSITION_LOOP    (when it reaches 360°, restart at 0°)
        //       - onReachingFinalValue = null  (no extra callback needed at the moment it completes one revolution)
        // ────────────────────────────────────────────────────────────────────

        new Transition<>(
                /* gameObjectToUpdateThrough= */ sun,
                /* setValueCallback          = */ (Float angleInDegrees) -> {
            // Compute a vector from cycleCenter to initialSunCenter:
            Vector2 offset = initialSunCenter.subtract(cycleCenter);

            // Rotate that offset by the current angle (in degrees):
            Vector2 rotated = offset.rotated(angleInDegrees);

            // Move the sun to (cycleCenter + rotated):
            sun.setCenter(cycleCenter.add(rotated));
        },
                /* initialValue              = */ 0f,                 // start at 0°
                /* finalValue                = */ 360f,               // end at 360°
                /* interpolator              = */ Transition.LINEAR_INTERPOLATOR_FLOAT,
                /* transitionTime            = */ cycleLength,
                /* transitionType            = */ TransitionType.TRANSITION_LOOP,
                /* onReachingFinalValue      = */ null                // no callback
        );

        // 8) Return the fully configured Sun GameObject
        return sun;
    }
}

