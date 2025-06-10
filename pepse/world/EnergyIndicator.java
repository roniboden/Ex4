package pepse.world;

import danogl.GameObject;
import danogl.components.Component;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Vector2;

import java.awt.Color;
import java.text.DecimalFormat;

/**
 * Displays the avatar’s current energy as a percentage (e.g., “NN %”) in the top-left corner of the screen.
 * <p>
 * This class provides a static factory method to create a HUD label that continuously updates each frame
 * to reflect the avatar’s current energy level. The label is rendered in CAMERA coordinates so that it
 * remains fixed on the screen regardless of world movements.
 * @author Noa
 */
public final class EnergyIndicator {

	/** ---------- tuning constants ---------- */
	private static final Vector2 TEXT_POSITION   = new Vector2(20, 18);   // screen coords (px)
	private static final Vector2 TEXT_DIMENSIONS = new Vector2(80, 30);   // logical size
	private static final String  FONT_NAME       = "Arial";               // any installed font
	private static final Color   TEXT_COLOR      = Color.BLACK;
	private static final DecimalFormat PERCENT_FMT = new DecimalFormat("##0");
	private static final String INITIAL_TEXT = "100 %";  // initial text in the label
	/** -------------------------------------- */

	/**
	 * Private constructor to prevent instantiation. Use the static create(...) method instead.
	 */
	private EnergyIndicator() { }   // static factory only

	/**
	 * Builds a GameObject that displays the avatar’s current energy as a percentage in the top-left corner.
	 * <p>
	 * The label is a {@link TextRenderable} with bold Arial font, colored black. It is positioned at
	 * {@code TEXT_POSITION} in CAMERA coordinates with dimensions {@code TEXT_DIMENSIONS}, and is tagged
	 * "energyText". A {@link Component} is attached to the label so that in each {@code update} call, the
	 * string is set to the avatar’s current energy (formatted by {@link #PERCENT_FMT}) followed by " %".
	 *
	 * @param avatar The {@link Avatar} instance whose energy level will be displayed.
	 * @return A {@link GameObject} representing the energy indicator HUD element.
	 */
	public static GameObject create(Avatar avatar) {

		/* The ctor variant: (text, fontName, isItalic, isBold) */
		TextRenderable renderable =
				new TextRenderable(INITIAL_TEXT, FONT_NAME, /*italic*/false, /*bold*/true);
		renderable.setColor(TEXT_COLOR);  // colour is set afterwards

		GameObject label = new GameObject(TEXT_POSITION, TEXT_DIMENSIONS, renderable);
		label.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
		/* Live-update once per frame */
		label.addComponent(new Component() {
			@Override
			public void update(float deltaTime) {
				renderable.setString(PERCENT_FMT.format(avatar.getEnergy()) + " %");
			}
		});

		return label;
	}
}
