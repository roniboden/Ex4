package pepse.world.pepse.world.trees;

import danogl.GameObject;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import danogl.collisions.Layer;
import pepse.world.LayerProvider;

/**
 * A purely decorative leaf tile.
 *
 * <p>The leaf has <strong>no physics component</strong>, so the avatar can
 * pass through it freely.  By default it is placed on the
 * {@link Layer#BACKGROUND} layer so that trunk segments, fruit, and the avatar
 * render in front of it.</p>
 * @author Noa
 */
public class Leaf extends GameObject implements LayerProvider {

	/**
	 * Constructs a square leaf renderable.
	 *
	 * @param topLeft top-left corner in world coordinates
	 * @param size    width&nbsp;=&nbsp;height of the leaf in pixels
	 * @param color   fill colour of the leaf rectangle
	 */
	public Leaf(Vector2 topLeft, float size, java.awt.Color color) {
		super(topLeft,
				new Vector2(size, size),
				new RectangleRenderable(color));
		setTag("leaf");
	}

	/** {@inheritDoc} */
	@Override
	public int defaultLayer() {
		return Layer.BACKGROUND;
	}
}
