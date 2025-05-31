// File: src/pepse/world/daynight/Night.java
package pepse.world.pepse.world.daynight;

import danogl.GameObject;
import danogl.components.Transition;
import danogl.components.Transition.TransitionType;
import danogl.components.Transition.Interpolator;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.Color;

/**
 * Section 3: Night (Day/Night Overlay) of the PEPSE exercise.
 *
 * Creates a full-screen black rectangle (in CAMERA coordinates) whose opacity
 * cycles smoothly from 0.0 → MIDNIGHT_OPACITY → 0.0, completing one full
 * day→night→day cycle in exactly `cycleLength` seconds.
 */
public class Night {

    /** The opacity at “midnight” (half-opaque black). */
    private static final Float MIDNIGHT_OPACITY = 0.5f;

    /**
     * Builds a GameObject that covers the entire window with a black overlay.
     * The overlay’s alpha transitions from 0 → 0.5 → 0 over `cycleLength` seconds,
     * using a cubic ease-in/out interpolator.
     *
     * @param windowDimensions  A Vector2 containing (windowWidth, windowHeight).
     * @param cycleLength       The total duration (in seconds) of one day→night→day cycle.
     * @return A GameObject tagged "DayNightOverlay" in CAMERA coordinates.
     */
    public static GameObject create(Vector2 windowDimensions, float cycleLength) {
        // 1. Create a solid black rectangle renderable
        RectangleRenderable blackRenderable = new RectangleRenderable(Color.BLACK);

        // 2. Construct the GameObject at (0, 0) with full window dimensions
        GameObject night = new GameObject(
                Vector2.ZERO,          // Top-left corner = (0, 0)
                windowDimensions,      // Dimensions = full screen
                blackRenderable        // Renderable = black rectangle
        );

        // 3. Ensure this object always draws on top by using CAMERA coordinates
        night.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);

        // 4. Tag the overlay for easy lookup if needed
        night.setTag("DayNightOverlay");

        // 5. Start fully transparent (alpha = 0.0 → “daytime”)
        night.renderer().setOpaqueness(0f);

        // 6. Prepare Transition parameters
        float halfCycle = cycleLength / 2f;
        // Time for one half-cycle (day→midnight or midnight→day)
        TransitionType transitionType = TransitionType.TRANSITION_BACK_AND_FORTH; // Ping-pong loop (0→0.5→0→…)
        Interpolator<Float> interpolator = Transition.CUBIC_INTERPOLATOR_FLOAT; // Smooth ease-in/out curve

        // 7. Attach the Transition that cycles alpha 0.0 → 0.5 → 0.0
        new Transition<>(
                /* gameObjectToUpdateThrough= */ night,
                /* setValueCallback          = */ night.renderer()::setOpaqueness,
                /* initialValue              = */ 0f,                  // Start at alpha = 0.0 (day)
                /* finalValue                = */ MIDNIGHT_OPACITY,    // Fade to alpha = 0.5 (midnight)
                /* interpolator              = */ interpolator,
                /* transitionTime            = */ halfCycle,
                /* transitionType            = */ transitionType,
                /* onReachingFinalValue      = */ null                 // No callback needed at midpoint
        );

        // 8. Return the fully configured GameObject
        return night;
    }
}
