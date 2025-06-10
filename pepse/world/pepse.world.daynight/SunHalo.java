package pepse.world.pepse.world.daynight;


import danogl.GameObject;
import danogl.components.Component;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.OvalRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import java.awt.Color;

/**
 * <p>
 * A soft, semi–transparent halo that “follows” the {@code Sun} object on screen.
 * </p><p>
 * The halo is rendered in <i>camera–space</i> (screen coordinates) so it always
 * appears in the same position relative to the viewport, regardless of camera
 * movement.  Its size is twice the diameter of the supplied Sun and its colour
 * is a light reddish-orange with low alpha, producing a subtle glow effect.
 * </p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * GameObject sun      = Sun.create(windowDimensions);
 * GameObject sunHalo  = SunHalo.create(sun);
 * gameObjects.addGameObject(sun,     SKY_LAYER);
 * gameObjects.addGameObject(sunHalo, SKY_LAYER);
 * }</pre>
 *
 * The halo registers an anonymous {@link Component} that, every frame, copies
 * the Sun’s centre to its own, yielding a lightweight “follow” behaviour with
 * no need for an explicit {@code Transition}.
 * @author Noa
 */
public final class SunHalo {

	/** RGBA colour of the halo – light red, 8 % opacity (alpha = 20 / 255). */
	private static final Color HALO_COLOR = new Color(255, 0, 20, 20);

	/** Scaling factor: halo diameter = {@code Sun} diameter × {@value}.     */
	private static final float SIZE_FACTOR = 2f;

	/** Depth, width and height are never instantiated – utility class only. */
	private SunHalo() { }   // Prevent instantiation

	/**
	 * Create a halo GameObject that constantly overlaps the supplied Sun.
	 *
	 * @param sun the existing Sun {@link GameObject} around which to draw the glow
	 * @return    a fully-configured halo object; caller must add it to {@code gameObjects()}
	 */
	public static GameObject create(GameObject sun) {

		Renderable renderable = new OvalRenderable(HALO_COLOR);
		Vector2 haloDims = sun.getDimensions().mult(SIZE_FACTOR);
		GameObject halo = new GameObject(Vector2.ZERO, haloDims, renderable);
		halo.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
		halo.addComponent(new Component() {
			/** Copy the Sun’s centre into the halo once per frame. */
			@Override
			public void update(float deltaTime) {
				halo.setCenter(sun.getCenter());
			}
		});

		return halo;
	}
}