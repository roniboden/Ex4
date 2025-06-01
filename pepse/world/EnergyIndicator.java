package pepse.world;

import danogl.GameObject;
import danogl.components.Component;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Vector2;
import pepse.world.Avatar;

import java.awt.Color;
import java.text.DecimalFormat;

/**
 * Displays the avatar's energy as a numeric percentage ("87 %") in the UI.
 *
 * <p>The text is rendered in <i>camera-space</i>, so it remains anchored to a
 * fixed screen position regardless of camera movement. Every frame, the text
 * is updated to reflect the avatar's current energy.</p>
 */
public final class EnergyIndicator {

	/* ─────────────────  layout / appearance  ───────────────────────── */

	/** Screen-space position (pixels from top-left). */
	private static final Vector2 TEXT_POSITION = new Vector2(20, 18);

	/** Font size in pixels (height). */
	private static final int FONT_SIZE = 20;

	/** Text colour (black). */
	private static final Color TEXT_COLOR = Color.BLACK;

	/** Formatter to show a whole-number percentage (no decimals). */
	private static final DecimalFormat PERCENT_FMT = new DecimalFormat("##0");

	private EnergyIndicator() {}   // utility class – prevent instantiation


	/* ─────────────────  factory  ───────────────────────────────────── */

	/**
	 * Create a {@code GameObject} that shows the avatar's current energy in
	 * percentage form (e.g., "74 %").
	 *
	 * @param avatar the avatar from which to query energy
	 * @return       configured label; caller must add to {@code gameObjects()}
	 */
	public static GameObject create(Avatar avatar, Vector2 pos) {

		TextRenderable renderable =
				new TextRenderable("100%" );

		GameObject label = new GameObject(TEXT_POSITION,
				pos,
				renderable);

		label.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
		label.setTag("energyText");

		label.addComponent(new Component() {
			@Override
			public void update(float deltaTime) {
				int ratio = avatar.getEnergy();
				renderable.setString(PERCENT_FMT.format(ratio) + " %");
			}
		});

		return label;
	}
}
