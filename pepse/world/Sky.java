package pepse.world;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.*;

/**
 * Represents the sky background in the Pepse world. Creates a solid-color rectangle
 * spanning the entire window, rendered in CAMERA coordinates so it remains fixed
 * behind all other world objects.
 */
public class Sky {
	/** The base color used to fill the sky (hex code #80C6E5). */
	private static final Color BASIC_SKY_COLOR = Color.decode("#80C6E5");

	/**
	 * Builds a GameObject that covers the full window dimensions with a solid sky color.
	 * The resulting GameObject is placed in CAMERA coordinates so that it stays static
	 * relative to the screen. It is tagged "sky" (for debugging) but otherwise has no behavior.
	 *
	 * @param windowDimensions A Vector2 containing (windowWidth, windowHeight).
	 * @return A GameObject representing the sky background.
	 */
	public static GameObject create(Vector2 windowDimensions) {
		GameObject sky = new GameObject(Vector2.ZERO, windowDimensions,
				new RectangleRenderable(BASIC_SKY_COLOR));
		sky.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
		// NEED TO BE REMOVED AFTER DEBUGGING
		sky.setTag("sky");
		return sky;
	}
}
