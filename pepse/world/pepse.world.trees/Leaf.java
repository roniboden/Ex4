package pepse.world.pepse.world.trees;

import danogl.GameObject;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import danogl.collisions.Layer;
import pepse.world.LayerProvider;

/** Decorative leaf – no physics, goes to BACKGROUND layer. */
public class Leaf extends GameObject implements LayerProvider {
	public Leaf(Vector2 topLeft, float size, java.awt.Color color) {
		super(topLeft, new Vector2(size, size), new RectangleRenderable(color));
		setTag("leaf");            // optional – but harmless
	}

	@Override
	public int defaultLayer() {
		return Layer.BACKGROUND;
	}
}

