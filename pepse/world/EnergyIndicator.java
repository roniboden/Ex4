package pepse.world;

import danogl.GameObject;
import danogl.components.Component;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Vector2;

import java.awt.Color;
import java.text.DecimalFormat;

/**
 * Shows the avatar’s current energy as “NN %” in the top-left corner.
 */
public final class EnergyIndicator {

	/* ---------- tuning constants ---------- */
	private static final Vector2 TEXT_POSITION   = new Vector2(20, 18);   // screen coords (px)
	private static final Vector2 TEXT_DIMENSIONS = new Vector2(80, 30);   // logical size
	private static final String  FONT_NAME       = "Arial";               // any installed font
	private static final Color   TEXT_COLOR      = Color.BLACK;
	private static final DecimalFormat PERCENT_FMT = new DecimalFormat("##0");

	private EnergyIndicator() { }   // static factory only

	/** Build the HUD label and wire it to the avatar. */
	public static GameObject create(Avatar avatar) {

		/* The ctor variant: (text, fontName, isItalic, isBold) */
		TextRenderable renderable =
				new TextRenderable("100 %", FONT_NAME, /*italic*/false, /*bold*/true);
		renderable.setColor(TEXT_COLOR);  // colour is set afterwards

		GameObject label = new GameObject(TEXT_POSITION, TEXT_DIMENSIONS, renderable);
		label.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
		label.setTag("energyText");

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
